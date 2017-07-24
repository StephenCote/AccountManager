/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cote.rocket.services;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.propellant.objects.FormElementType;
import org.cote.propellant.objects.FormElementValueType;
import org.cote.propellant.objects.FormType;
import org.cote.propellant.objects.ValidationRuleType;
import org.cote.propellant.objects.types.ElementEnumType;
import org.cote.propellant.objects.types.ValidationEnumType;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.ValidationRuleFactory;
public class ValidationService {
	public static final Logger logger = LogManager.getLogger(ValidationService.class);
	private static Map<String,Pattern> patterns = new HashMap<String,Pattern>();
	
	private static Pattern getPattern(String pat){
		if(patterns.containsKey(pat)) return patterns.get(pat);
		Pattern pattern = Pattern.compile(pat,Pattern.MULTILINE);
		patterns.put(pat, pattern);
		return pattern;
	}
	public static boolean validateForm(UserType user, FormType form) throws FactoryException, ArgumentException{
		boolean out_bool = false;
		if(form.getElements().size() == 0){
			logger.warn("Form contains no elements.  Populated = " + form.getPopulated() + ". Returning true for validation");
			return true;
		}
		if(form.getIsTemplate() == true){
			logger.debug("Form templates are always valid. Note: This relies on the logic that no form template values are ever saved.");
			return true;
		}
		
		for(int i = 0; i < form.getElements().size(); i++){
			FormElementType el = form.getElements().get(i);
			FormElementType templateElement = FormService.getFormElementByName(form.getTemplate(), el.getName());
			if(templateElement == null || templateElement.getId().equals(el.getId()) == false){
				logger.error("Element mismatch.  Form Element " + el.getName() + " is not a template element.");
				out_bool = false;
				break;
			}
			out_bool = validateFormElement(user, form.getElements().get(i));
			if(out_bool == false){
				logger.warn("Element " + form.getElements().get(i).getName() + " failed validation");
				break;
			}
		}
		return out_bool;
	}
	public static boolean validateFormElement(UserType user, FormElementType formElement) throws FactoryException, ArgumentException{
		boolean out_bool = false;
		if(formElement.getElementValues().size() == 0){
			if(formElement.getValidationRule() != null && formElement.getValidationRule().getAllowNull() == false){
				logger.warn("Form Element contains no values, and the validation rule prohibits null values.");
				return false;
			}
			else{
				logger.warn("Form Element contains no values.  Returning true for validation");
				return true;
			}
		}
		for(int i = 0; i < formElement.getElementValues().size();i++){
			out_bool = validateFormElementValue(user, formElement, formElement.getElementValues().get(i));
			if(out_bool == false){
				logger.warn("Form element " + formElement.getName() + " value #" + i + " failed validation");
				break;
			}
		}
		return out_bool;
	}
	public static boolean isSupportedBinaryType(ElementEnumType elementType){
		boolean out_bool = false;
		switch(elementType){
			case RESOURCE:
			case SCHEDULE:
			case ESTIMATE:
			case TIME:
			case NOTE:
			case DATA:
			case FORM:
			case TASK:
			case TICKET:
			case MODEL:
			case ARTIFACT:
			case STAGE:
			case CASE:
			case WORK:
			case GOAL:
			case BUDGET:
			case COST:
				out_bool = true;
				break;
			default:
				break;
		}
		return out_bool;
	}
	public static boolean validateFormElementValue(UserType user, FormElementType formElement, FormElementValueType formElementValue) throws FactoryException, ArgumentException{
		boolean out_bool = false;
		if(isSupportedBinaryType(formElement.getElementType())
		){
			if(formElementValue.getBinaryId().equals(0L)){
				if(formElement.getValidationRule() != null && formElement.getValidationRule().getAllowNull() == true){
					logger.warn("Binary " + formElement.getElementType().toString() + " reference id of 0 explicitly permitted with nullable rule.");
					return true;
				}
				/// To validate a binary id of 0, create a rule of type 'NONE' that allows null values.
				/// Otherwise, it will fall out here as an error condition
				///
				logger.warn("Binary " + formElement.getElementType().toString() + " reference id of 0 without an explicit nullable rule is invalid.");
				return false;
			}
			else if(formElementValue.getBinaryId() < 0L){
				logger.warn("Permitting possible bulk insert binary " + formElement.getElementType().toString() + " reference id.  Note: Persisting this value is an error.");
				return true;
			}

			NameIdType checkType = null;
			boolean authZ = false;
			/// Note: Special case for data to only read the meta data
			///
			if(formElement.getElementType() == ElementEnumType.DATA){
				checkType = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataById(formElementValue.getBinaryId(), true, formElement.getOrganizationId());
				if(checkType != null) authZ = AuthorizationService.canView(user, (DataType)checkType);
			}
			else{
				NameIdFactory factory = Factories.getFactory(FactoryEnumType.fromValue(formElement.getElementType().toString()));
				checkType = factory.getById(formElementValue.getBinaryId(), formElement.getOrganizationId());
				if(checkType != null) authZ = AuthorizationService.canView(user,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupById(((NameIdDirectoryGroupType)checkType).getGroupId(),checkType.getOrganizationId()));
			}
			if(checkType == null){
				logger.warn("Invalid binary " + formElement.getElementType().toString() + " reference id.  Id " + formElementValue.getBinaryId() + " " + formElement.getElementType().toString() + " does not exist");
				return false;
			}
			if(authZ == false){
				logger.warn("User " + user.getName() + " is not authorized to read " + formElement.getElementType().toString() + " " + checkType.getName() + " (#" + checkType.getId() + ")");
			}
			logger.debug("Binary " + formElement.getElementType().toString() + " exists and user is authorized to view it.  Returning true for validation.");
			return true;
		}
		if(formElement.getValidationRule() == null){
			logger.debug("Form element validation rule is null. Returning true for validation");
			return true;
		}
		ValidationRuleType rule = formElement.getValidationRule();
		out_bool = validateFormElementValueWithRule(user, formElement, formElementValue, rule);
		return out_bool;
	}
	public static boolean validateFormElementValueWithRule(UserType user, FormElementType formElement, FormElementValueType formElementValue, ValidationRuleType rule) throws FactoryException, ArgumentException{
		boolean out_bool = false;
		boolean out_return_bool = true;
		boolean child_return = false;
		((ValidationRuleFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULE)).populate(rule);
		ValidationEnumType ruleType = rule.getValidationType();
		boolean compare = rule.getComparison();
		boolean allowNull = rule.getAllowNull();
		if(rule.getIsRuleSet()){
			logger.debug("Processing " + rule.getRules().size() + " child rules");
			for(int i = 0; i < rule.getRules().size(); i++){
				ValidationRuleType cRule = rule.getRules().get(i);
				child_return = validateFormElementValueWithRule(user, formElement, formElementValue, cRule);
				if(!child_return && out_return_bool) out_return_bool = false;
			}
		}
		if(formElementValue.getIsBinary()){
			logger.warn("Validating binary values not supported.  Approving validation without looking.");
			return true;
		}
		String val = formElementValue.getTextValue();
		if(rule.getExpression() != null){
			Pattern exp = getPattern(rule.getExpression());
			Matcher m = exp.matcher(val);
			switch(ruleType){
				case REPLACEMENT:
					out_bool = true;
					if(rule.getReplacementValue() != null && m.matches()){
						val = m.replaceAll(rule.getReplacementValue());
						logger.debug("Rule " + rule.getName() + " replaced value with '" + val + "'");
						formElementValue.setTextValue(val);
					}
					else{
						logger.debug("Rule " + rule.getName() + " did not match " + rule.getExpression() + " with value " + val + ".  Marking validation as true because it's a replacement rule, not a pure match rule.");
					}
					break;
				case BOOLEAN:
					if(
						(allowNull && (val == null || val.length() == 0))
						||
						(m.matches() == compare)
					){
						logger.debug("Rule " + rule.getName() + " matched value '" + val + "'");
						out_bool = true;
					}
					else{
						logger.warn("Validation of " + formElement.getName() + " failed pattern " + rule.getExpression() + " because " + m.matches() + " was false or " + allowNull + " is true and " +(val == null || val.length() == 0));
					}
					break;
				default:
					logger.warn("Rule " + rule.getName() + " with type " + ruleType + " was not handled");
					break;
			}
		}
		else if(ruleType == ValidationEnumType.NONE){
			logger.debug("Rule " + rule.getName() + " does not contain an expression and is set to validation type " + ruleType + ".  Marking validation as true.");
			out_bool = true;
		}
		else{
			logger.debug("Rule " + rule.getName() + " does not define a pattern.  Marking validation as true.");
			out_bool = true;
		}
		logger.debug("Rule " + rule.getName() + " returns " + (out_bool && out_return_bool));
		return (out_bool && out_return_bool);
	}
}
