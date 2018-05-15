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
package org.cote.rocket.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.propellant.objects.FormElementType;
import org.cote.propellant.objects.FormElementValueType;
import org.cote.propellant.objects.FormType;
import org.cote.propellant.objects.types.ElementEnumType;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.FormElementFactory;
import org.cote.rocket.factory.FormFactory;
import org.cote.rocket.services.FormService;
import org.junit.Test;
public class TestFormElementValue extends BaseAccelerantTest{
	public static final Logger logger = LogManager.getLogger(TestFormElementValue.class);
	public static int formId = 0;
	public static int formElementId = 0;
	public static int formElementValueId = 0;
	
	@Test
	public void TestSelectOption(){

		try{
			DirectoryGroupType groupF = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Forms", testUser.getOrganizationId());
			DirectoryGroupType groupE = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/FormElements", testUser.getOrganizationId());

			FormType formT = this.getCreateForm(testUser,groupF,"Test Template 1");
			formT.setIsTemplate(true);
			formT.getElements().clear();
			
			FormElementType formES = this.getCreateFormElement(testUser,groupE,"Select Element");
			formES.setElementType(ElementEnumType.SELECT);
			formES.getElementValues().clear();
			for(int i = 0; i < 5; i++){
				FormElementValueType fvt = new FormElementValueType();
				fvt.setName("Option " + (i + 1));
				fvt.setIsBinary(false);
				fvt.setTextValue("Option " + (i + 1));
				formES.getElementValues().add(fvt);
			}
			((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).update(formES);
			
			FormElementType formMES = this.getCreateFormElement(testUser,groupE,"Multi Select Element");
			formMES.setElementType(ElementEnumType.MULTIPLE_SELECT);
			formMES.getElementValues().clear();
			for(int i = 0; i < 5; i++){
				FormElementValueType fvt = new FormElementValueType();
				fvt.setName("Option " + (i + 1));
				fvt.setIsBinary(false);
				fvt.setTextValue("Option " + (i + 1));
				formMES.getElementValues().add(fvt);
			}
			((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).update(formES);
			((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).update(formMES);

			
			formT.getElements().add(formES);
			formT.getElements().add(formMES);
			((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).update(formT);
			
			FormType form1 = this.getCreateForm(testUser,groupF,"Test Form 1",formT);
			//form1.setTemplate(formT);
			//((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).updateForm(form1);
			form1 = ((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).getByNameInGroup("Test Form 1", groupF);

			((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).populate(form1);
			/// The form will now contain any template values (particularly select and multi-select)
			/// As well as its own values.
			/// The select values need to be reworked because it means you have to clip off the default values before resubmitting, or all option values are potentially recorded as form values
			///
			printForm(form1);
			FormService.updateFormValues(testUser, form1, false);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		}  catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			logger.error(e.getMessage());
		}
	}
	/*
	@Test
	public void TestGetDefaultValues(){
		boolean check = false;
		try{
			DirectoryGroupType groupN = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Notes", testUser.getOrganizationId());
			DirectoryGroupType groupD = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Datas", testUser.getOrganizationId());
			DirectoryGroupType groupF = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Forms", testUser.getOrganizationId());
			DirectoryGroupType groupE = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/FormElements", testUser.getOrganizationId());
			DirectoryGroupType groupV = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/ValidationRules", testUser.getOrganizationId());
			FormType formTemplate = getCreateForm(testUser, groupF,"Form Template");

			if(formTemplate.getElements().isEmpty()){
				
				ValidationRuleType rule1 = getCreateRule(testUser, groupV, "Not Empty", null);

				ValidationRuleType trimBegin = getCreateRule(testUser, groupV, "Trim Begin", "^\\s+");
				trimBegin.setReplacementValue("");
				trimBegin.setIsReplacementRule(true);
				trimBegin.setValidationType(ValidationEnumType.REPLACEMENT);
				((ValidationRuleFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULE)).updateValidationRule(trimBegin);
				
				ValidationRuleType trimEnd = getCreateRule(testUser, groupV, "Trim End", "\\s+$");
				trimEnd.setReplacementValue("");
				trimEnd.setValidationType(ValidationEnumType.REPLACEMENT);
				trimEnd.setIsReplacementRule(true);
				((ValidationRuleFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULE)).updateValidationRule(trimEnd);
				
				rule1.setIsRuleSet(true);
				rule1.getRules().add(trimBegin);
				rule1.getRules().add(trimEnd);
				((ValidationRuleFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULE)).updateValidationRule(rule1);
				
				FormElementType description = getCreateFormElement(testUser, groupE,"description");
				
				
				FormElementType firstName = getCreateFormElement(testUser, groupE,"firstName");
				firstName.setValidationRule(rule1);
				((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).updateFormElement(firstName);
				
				FormElementType lastName = getCreateFormElement(testUser, groupE,"lastName");
				lastName.setValidationRule(rule1);
				((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).updateFormElement(lastName);
				
				FormElementType dataEl = getCreateFormElement(testUser, groupE,"dataLink",ElementEnumType.DATA);
				FormElementType noteEl = getCreateFormElement(testUser, groupE,"noteLink",ElementEnumType.NOTE);

				FormElementType birthdate = getCreateFormElement(testUser, groupE,"birthdate",ElementEnumType.DATE);
				birthdate.setValidationRule(rule1);
				((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).updateFormElement(birthdate);
				
				formTemplate.getElements().add(firstName);
				formTemplate.getElements().add(lastName);
				formTemplate.getElements().add(birthdate);
				formTemplate.getElements().add(description);
				formTemplate.getElements().add(dataEl);
				formTemplate.getElements().add(noteEl);
				((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).updateForm(formTemplate);
			}
			
			FormType form1 = getCreateForm(testUser,groupF,"Form #1",formTemplate);
			logger.info("Populate size: " + form1.getElements().size());
			
			//FormService.cloneFormElements(formTemplate, form1);

			//List<FormElementType> cloneList = new ArrayList<FormElementType>();
			//form1.getElements().addAll(Arrays.asList(ArrayUtils.clone(formTemplate.getElements().toArray(new FormElementType[0]))));
			//form1.getElements().addAll(cloneList);

			//FormElementValueType fev = ((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).newFormElementValue(testUser, form1, form1.getElements().get(0));
			//form1.getElements().get(0).getElementValues().add(fev);
			FormService.setFormElementStringValue(form1, "firstName", new String[]{"Test First"});
			FormService.setFormElementStringValue(form1, "lastName", new String[]{"Test Last"});
			FormService.setFormElementStringValue(form1, "birthdate", new String[]{"Test Date"});
			FormService.setFormElementStringValue(form1, "description", new String[]{"Test Description"});
			
			DataType data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName("Test Data", groupD);
			if(data == null){
				data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(testUser, groupD);
				data.setMimeType("text/plain");
				data.setName("Test Data");
				DataUtil.setValue(data, "This is the data text".getBytes());
				((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).addData(data);
				data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName("Test Data", groupD);
			}
			FormService.setFormElementBinaryValue(form1, "dataLink", new Long[]{data.getId()});
			
			NoteType note = ((NoteFactory)Factories.getFactory(FactoryEnumType.NOTE)).getByNameInGroup("Test Note", groupN);
			if(note == null){
				note = ((NoteFactory)Factories.getFactory(FactoryEnumType.NOTE)).newNote(testUser, groupN);
				note.setName("Test Note");
				note.setText("Demo note");
				((NoteFactory)Factories.getFactory(FactoryEnumType.NOTE)).addNote(note);
				note = ((NoteFactory)Factories.getFactory(FactoryEnumType.NOTE)).getByNameInGroup("Test Note", groupN);
			}
			FormService.setFormElementBinaryValue(form1, "noteLink", new Long[]{note.getId()});
			//boolean valid = ValidationService.validateForm(form1);
			
			//fev.setTextValue("Demo text");
			printForm(form1);
			boolean updated = FormService.updateFormValues(testUser, form1,true);
			assertTrue("Form was not updated", updated);
			
			((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).clearCache();
			
			FormType compType = ((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).getByNameInGroup("Form #1", groupF);
			assertNotNull("Form is null",compType);
			FormService.updateFormValues(testUser, compType,false);
			compType = ((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).getByNameInGroup("Form #1", groupF);
			assertTrue("Unpopulated form should contain no elements",compType.getElements().isEmpty());
			((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).populate(compType);
			assertTrue("Populated form should contain elements",compType.getElements().size() > 0);
			assertTrue("Populated form element should contain a value", compType.getElements().get(0).getElementValues().size() > 0);
			
			logger.info("Trying to sneak new elements into the form");
			FormElementType sneakData = getCreateFormElement(testUser, groupE,"sneakData");
			compType.getElements().add(sneakData);
			FormService.setFormElementStringValue(compType, "sneakData", new String[]{"Sneak Data"});
			assertFalse("Non-template data should not be valid",ValidationService.validateForm(testUser, compType));
			check = true;
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Failed",check);
	}
	*/
	public void printForm(FormType form){
		logger.info("Form: " + form.getName() + (form.getIsTemplate() ? " (Template)" : "") + " (" + form.getElements().size() + " Elements)");
		for(int i = 0; i < form.getElements().size();i++){
			FormElementType fet = form.getElements().get(i);
			StringBuffer buff = new StringBuffer();
			for(int v = 0; v < fet.getElementValues().size();v++){
				if(v > 0) buff.append("\n\t\t");
				FormElementValueType fev = fet.getElementValues().get(v);
				if(fev.getFormId().equals(form.getId()) == false || form.getIsTemplate() == true) buff.append("(default) ");
				buff.append(fev.getFormId() + " " + fev.getFormElementId() + " " + fev.getTextValue());
			}
			logger.info("\t" + fet.getName() + " =\n\t\t" + buff.toString());
		}
	}
	
}