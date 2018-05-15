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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.junit.Test;
public class TestBulkDelete extends BaseDataAccessTest {
	
	@Test
	public void TestDeletePerson(){
		boolean success = false;
		DirectoryGroupType pDir = null;
		try{
			pDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganizationId());
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			
			String uid = UUID.randomUUID().toString();
			PersonType person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).newPerson(testUser, pDir.getId());
			person.setName(uid);
			assertTrue("Failed to add person",((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).add(person));
			person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getByNameInGroup(uid, pDir);
			assertNotNull("Person is null",person);
			
			BulkFactories.getBulkFactory().deleteBulkEntry(sessionId, FactoryEnumType.PERSON, person);

			BulkFactories.getBulkFactory().write(sessionId);
			
			assertNull("Person should be null",((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getByNameInGroup(uid, pDir));
			
		}
		catch(NullPointerException | FactoryException | ArgumentException | DataAccessException  e){
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
}
