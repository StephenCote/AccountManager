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
package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
public class TestFindOrganization{
	public static final Logger logger = LogManager.getLogger(TestFindOrganization.class);

	
	@Before
	public void setUp() throws Exception {

		ConnectionFactory cf = ConnectionFactory.getInstance();
		cf.setConnectionType(CONNECTION_TYPE.SINGLE);
		cf.setDriverClassName("org.postgresql.Driver");
		cf.setUserName("devuser");
		cf.setUserPassword("password");
		cf.setUrl("jdbc:postgresql://127.0.0.1:5432/devdb");
		
	}

	@After
	public void tearDown() throws Exception {


	}
	
	
	@Test
	public void testFindRoot(){
		
		String path = "/";
		OrganizationType org = null;
		
		try {
			org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(path);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		assertNotNull("Org is null",org);
		assertTrue("Unexpected org: '" + org.getName() + "'", org.getName().equalsIgnoreCase("global"));
	}
	
	@Test
	public void testFindPublic(){
		
		String path = "/Public";
		OrganizationType org = null;
		
		try {
			org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(path);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		assertNotNull("Org is null",org);
		assertTrue("Unexpected org: '" + org.getName() + "'", org.getName().equalsIgnoreCase("public"));
	}
}