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
package org.cote.accountmanager.data;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.cote.accountmanager.objects.AttributeType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdType;
import org.junit.Test;

public class TestAttributes extends BaseDataAccessTest{
	@Test
	public void TestGetAttributes(){
		DataType data = getData(testUser,"Test Data");
		Factories.getAttributeFactory().populateAttributes(data);
	}
	/*
	@Test
	public void TestDeleteAttributes(){
		DataType data = getData(testUser, "Test Data");
		Factories.getAttributeFactory().deleteAttributes(data);
	}
	*/
	@Test
	public void TestAddAttributes(){
		DataType data = getData(testUser, "Test Data");
		DataType data2 = getData(testUser, "Test Data 2");
		Factories.getAttributeFactory().deleteAttributes(data);
		Factories.getAttributeFactory().deleteAttributes(data2);
		AttributeType attr = Factories.getAttributeFactory().newAttribute(data);
		String testName = UUID.randomUUID().toString();
		attr.setName(testName);
		//attr.setDataType(SqlDataEnumType.VARCHAR);
		attr.getValues().add("Test value 1");
		attr.getValues().add("Test value 2");
		attr.getValues().add("Test value 3");
		data.getAttributes().add(attr);
		attr = Factories.getAttributeFactory().newAttribute(data2);
		attr.setName(testName);
		//attr.setDataType(SqlDataEnumType.VARCHAR);
		attr.getValues().add("Test value 1");
		attr.getValues().add("Test value 2");
		attr.getValues().add("Test value 3");
		data2.getAttributes().add(attr);
		assertTrue("Failed to update attributes",Factories.getAttributeFactory().addAttributes(new NameIdType[]{data,data2}));

		Factories.getAttributeFactory().populateAttributes(data);
		Factories.getAttributeFactory().populateAttributes(data2);
		
		assertTrue("Expected one attribute and got " + data.getAttributes().size(),data.getAttributes().size() == 1);
		assertTrue("Expected one attribute and got " + data2.getAttributes().size(),data2.getAttributes().size() == 1);
		
	
	}
}
