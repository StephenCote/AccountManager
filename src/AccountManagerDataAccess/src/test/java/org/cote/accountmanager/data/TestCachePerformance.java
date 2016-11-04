package org.cote.accountmanager.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
		Factories.getTagParticipationFactory().setAggressiveKeyFlush(true);
		try{
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
			if(Factories.getTagParticipationFactory().isAggressiveKeyFlush() == true) logger.warn("Aggressive Key Flush is enabled on the test factory.  This will iterate through the entire cache looking for all of the keys used to cache a particular object.");
			for(int i = 0; i < 10; i++){
				DataParticipantType dpt = dptList.get(random.nextInt(20000));
				Factories.getTagParticipationFactory().removeFromCache(dpt);
			}
			logger.info("Time to randomly pull from cache: " + (System.currentTimeMillis() - start) + "ms");
		}
		catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
		}
	}
}
