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

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.INameIdGroupFactory;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.propellant.objects.FormElementType;
import org.cote.propellant.objects.types.ElementEnumType;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.FormElementFactory;
import org.cote.rocket.factory.LifecycleFactory;

import org.junit.Test;

public class TestCapabilities extends BaseAccelerantTest {
	
	
	
	@Test
	public void TestInterfaceCast(){
		try{
			LifecycleFactory lf = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE));
			INameIdFactory ilf = (INameIdFactory)lf;
			INameIdGroupFactory iglf = (INameIdGroupFactory)lf;
		}
		catch(FactoryException f){
			logger.error(f);
		}
	}
	
	@Test
	public void TestForms(){
		boolean add = false;
		try {
			DirectoryGroupType formsElDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/FormElements", testUser.getOrganizationId());
			FormElementType el = ((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).newFormElement(testUser, formsElDir.getId());
			String name = UUID.randomUUID().toString();
			el.setName(name);
			el.setElementName("el-" + name);
			el.setElementType(ElementEnumType.STRING);
			BaseService.denormalize(el);
			add = BaseService.add(AuditEnumType.FORMELEMENT, el, getRequestMock(testUser));
		
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Failed to add elements", add);
	}
	
	@Test
	public void TestRequestMock(){
		BaseGroupType group = null;
		try{
		group = org.cote.accountmanager.service.rest.BaseService.findGroup(GroupEnumType.DATA, "~/", getRequestMock(testUser));
		}
		catch(Exception e){
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertNotNull("Group is null",group);
	}
}