/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.propellant.objects.FormElementType;
import org.cote.propellant.objects.FormElementValueType;
import org.cote.propellant.objects.FormType;
import org.cote.propellant.objects.types.ElementEnumType;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.FormFactory;
import org.junit.Test;
public class TestFormFactory extends BaseAccelerantTest{
	public static final Logger logger = LogManager.getLogger(TestFormFactory.class);
	public static long formId = 0;
	public static long formElementId = 0;
	public static long formElementValueId = 0;
	
	@Test
	public void TestNewForm(){
		FormType form = newForm(UUID.randomUUID().toString());
		assertNotNull("Form is null", form);
		formId = form.getId();
		logger.info("Form '" + form.getName() + "' id is " + formId);
	}
	@Test
	public void TestGetForm(){
		FormType form = getForm(formId);
		assertNotNull("Form is null", form);
	}
	@Test
	public void TestUpdateForm(){
		FormType Form = getForm(formId);
		Form.setDescription("New desc");
		boolean update = updateForm(Form);
		assertTrue("Form was not updated", update);
	}
	
	@Test
	public void TestNewFormElement(){
		//FormType form = getForm(formId);
		FormElementType FormElement = newFormElement(UUID.randomUUID().toString());
		FormElement.setElementType(ElementEnumType.STRING);
		assertNotNull("FormElement is null", FormElement);
		formElementId = FormElement.getId();
		logger.info("FormElement '" + FormElement.getName() + "' id is " + formElementId);
	}
	@Test
	public void TestGetFormElement(){
		FormElementType FormElement = getFormElement(formElementId);
		assertNotNull("FormElement is null", FormElement);
	}
	@Test
	public void TestUpdateFormElement(){
		FormElementType FormElement = getFormElement(formElementId);
		FormElement.setDescription("New desc");
		boolean update = updateFormElement(FormElement);
		assertTrue("FormElement was not updated", update);
	}
	@Test
	public void TestAssociateFormElement(){
		FormType form = getForm(formId);
		FormElementType formElement = getFormElement(formElementId);
		boolean update = newFormParticipant(form, formElement);
		assertTrue("FormElement was not updated", update);
	}
	@Test
	public void TestNewFormElementValue(){
		//FormType form = getForm(formId);
		FormType form = getForm(formId);
		try {
			((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).populate(form);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Elements not populated", form.getElements().size() > 0);
		FormElementValueType val = newFormElementValue(form.getElements().get(0).getName(), "Example text", false, form, form.getElements().get(0));
		assertNotNull("FormElementValue is null", val);
		formElementValueId = val.getId();
		logger.info("FormElementValue '" + val.getName() + "' id is " + formElementValueId);
	}
	@Test
	public void TestGetFormElementValue(){
		FormElementValueType FormElementValue = getFormElementValue(formElementValueId);
		assertNotNull("FormElementValue is null", FormElementValue);
	}
	@Test
	public void TestUpdateFormElementValue(){
		FormElementValueType FormElementValue = getFormElementValue(formElementValueId);
		FormElementValue.setTextValue("More text");
		boolean update = updateFormElementValue(FormElementValue);
		assertTrue("FormElementValue was not updated", update);
	}
	
	@Test
	public void TestNewFormElementValueBin(){
		//FormType form = getForm(formId);
		FormType form = getForm(formId);
		try {
			((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).populate(form);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Elements not populated", form.getElements().size() > 0);

		FormElementValueType val = newFormElementValue(form.getElements().get(0).getName() + "Bin", "Example text", true, form, form.getElements().get(0));
		assertNotNull("FormElementValue is null", val);
		formElementValueId = val.getId();
		logger.info("FormElementValue '" + val.getName() + "' id is " + formElementValueId);
	}
	@Test
	public void TestGetFormElementValueBin(){
		FormElementValueType FormElementValue = getFormElementValue(formElementValueId);
		assertNotNull("FormElementValue is null", FormElementValue);
	}
	/*
	@Test
	public void TestUpdateFormElementValueBin(){
		FormElementValueType FormElementValue = getFormElementValue(formElementValueId);
		boolean updated = false;
		try {
			DataUtil.setValueString(FormElementValue.getBinaryValue(), "More text");
			updated = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).updateData(FormElementValue.getBinaryValue());
		} catch (DataException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		

		assertTrue("FormElementValue was not updated", updated);
	}
	*/
}