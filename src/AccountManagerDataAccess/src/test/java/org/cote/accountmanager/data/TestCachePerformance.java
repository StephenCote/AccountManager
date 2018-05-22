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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cote.accountmanager.data.factory.TagParticipationFactory;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DataParticipantType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.ParticipationEnumType;
import org.junit.Test;
public class TestCachePerformance extends BaseDataAccessTest {
	@Test
	public void TestCachePerformance(){
		List<DataParticipantType> dptList = new ArrayList<DataParticipantType>();
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		Random random = new Random();
		long start = System.currentTimeMillis();

		try{
			((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).setAggressiveKeyFlush(true);
			for(int i = 0; i < 20000; i++){
				DataParticipantType dpt = new DataParticipantType();
				dpt.setNameType(NameEnumType.PARTICIPANT);
				dpt.setOwnerId(testUser.getId());
				dpt.setOrganizationId(testUser.getOrganizationId());
				dpt.setParticipantType(ParticipantEnumType.DATA);
				dpt.setParticipationType(ParticipationEnumType.TAG);
				dpt.setParticipantId(random.nextLong());
				dpt.setParticipationId(random.nextLong());
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.TAGPARTICIPATION, dpt);
				dptList.add(dpt);
			}
			logger.info("Time to create data list: " + (System.currentTimeMillis() - start) + "ms");
			start = System.currentTimeMillis();
			/// Next, pick 10 entries randomly from the list and pop them from the factory cache (added via the bulk create entry method)
			///
			if(((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).isAggressiveKeyFlush()) logger.warn("Aggressive Key Flush is enabled on the test factory.  This will iterate through the entire cache looking for all of the keys used to cache a particular object.");
			for(int i = 0; i < 10; i++){
				DataParticipantType dpt = dptList.get(random.nextInt(20000));
				((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).removeFromCache(dpt);
			}
			logger.info("Time to randomly pull from cache: " + (System.currentTimeMillis() - start) + "ms");
		}
		catch (ArgumentException | FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
	}
}
