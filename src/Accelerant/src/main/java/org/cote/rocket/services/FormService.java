/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.propellant.objects.FormElementType;
import org.cote.propellant.objects.FormElementValueType;
import org.cote.propellant.objects.FormType;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.FormElementFactory;
import org.cote.rocket.factory.FormElementValueFactory;
public class FormService {
	public static final Logger logger = LogManager.getLogger(FormService.class);
	public static void cloneFormElements(FormType formTemplate, FormType form) throws FactoryException{
		form.getElements().clear();
		
		for(FormElementType el : formTemplate.getElements()){
			form.getElements().add(((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).clone(el));
		}

	}
	public static boolean setFormElementStringValue(FormType form, String elementName, String[] elementValues) throws ArgumentException{
		return setFormElementValue(form, elementName, false, elementValues);
	}
	public static <T> boolean setFormElementBinaryValue(FormType form, String elementName, Long[] elementValues) throws ArgumentException{
		return setFormElementValue(form, elementName, true, elementValues);
	}

	protected static <T> boolean setFormElementValue(FormType form, String elementName, boolean isBinary, T[] elementValues) throws ArgumentException{
		boolean out_bool = false;
		FormElementType fet = getFormElementByName(form, elementName);

		if(fet == null){
			logger.warn("Element " + elementName + " does not exist.  Form is populated = " + form.getPopulated());
			return out_bool;
		}
		fet.getElementValues().clear();
		for(int i = 0; i < elementValues.length;i++){
			FormElementValueType fetv = new FormElementValueType();
			fetv.setName(elementName);
			fetv.setIsBinary(isBinary);
			
			if(elementValues[i] instanceof String){
				fetv.setTextValue((String)elementValues[i]);
			}
			else if(elementValues[i] instanceof Date){
				fetv.setTextValue(CalendarUtil.exportDateAsString((Date)elementValues[i]));
			}
			else if(elementValues[i] instanceof Long){
				fetv.setBinaryId((Long)elementValues[i]);
			}
			else{
				throw new ArgumentException("Unsupported object type");
			}
			//else if()
			fet.getElementValues().add(fetv);
		}
		//fet.getElementValues().addAll(Arrays.asList(elementValues));
		return true;
	}
	public static FormElementType getFormElementByName(FormType form, String matchName){
		FormElementType fet = null;
		for(FormElementType fetM : form.getElements()){
			if(fetM.getName().equals(matchName)){
				fet = fetM;
				break;
			}
		}
		return fet;
	}
	public static boolean updateFormValues(UserType user, FormType form, boolean validate) throws FactoryException, ArgumentException{
		boolean out_bool = false;
		if(form.getPopulated() == false){
			logger.info("Not updating fields for an unpopulated form");
			return true;
		}
		if(validate && (out_bool = ValidationService.validateForm(user,form)) == false){
			logger.warn("Form " + form.getName() + " failed validation");
			return out_bool;
		}
		List<FormElementValueType> vals = ((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).getByForm(form);
		Set<Long> set = new HashSet<Long>();
		HashMap<String,FormElementValueType> map = new HashMap<String,FormElementValueType>();
		for(int i = 0; i < vals.size();i++){
			map.put(vals.get(i).getName(), vals.get(i));
			set.add(vals.get(i).getId());
		}
		for(int f = 0; f < form.getChildForms().size();f++){
			updateFormValues(user,form.getChildForms().get(f), validate);
		}

		for(int i = 0; form.getIsTemplate() == false && i < form.getElements().size();i++){
			FormElementType fet = form.getElements().get(i);

			if((fet.getElementValues().size() > 0)){
				for(int v = 0; v < fet.getElementValues().size();v++){
					try{
						FormElementValueType fevt = fet.getElementValues().get(v);
						FormElementValueType fcheck = null;
						//logger.error("Check Val: " + fevt.getName() + ":" + map.containsKey(fevt.getName()));
						if(map.containsKey(fevt.getName())){
							fcheck = map.get(fevt.getName());
							set.remove(fcheck.getId());
							fevt.setId(fcheck.getId());
							fevt.setBinaryId(fevt.getBinaryId());
							fevt.setIsBinary(fevt.getIsBinary());
							fevt.setOwnerId(fcheck.getOwnerId());
							fevt.setTextValue(fevt.getTextValue());
							fevt.setOrganizationId(fcheck.getOrganizationId());
							((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).update(fevt);
						}
						else{
							FormElementValueType fevt_new = ((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).newFormElementValue(user, form, fet);
							fevt_new.setBinaryId(fevt.getBinaryId());
							fevt_new.setIsBinary(fevt.getIsBinary());
							fevt_new.setName(fevt.getName());
							fevt_new.setTextValue(fevt.getTextValue());
							if(!((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).add(fevt_new)){
								 logger.error("Error adding value");
							}
						}
					}
					catch(FactoryException fe){
						logger.error(fe.getMessage());
						logger.error("Error",fe);
						
					} catch (ArgumentException e) {
						
						logger.error("Error",e);
					}
				}

				
			}
		}
		if(set.size() > 0){
			((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).deleteFormElementValuesByIds(ArrayUtils.toPrimitive(set.toArray(new Long[0])), form.getOrganizationId());
		}
		return true;
		
	}
	
}
