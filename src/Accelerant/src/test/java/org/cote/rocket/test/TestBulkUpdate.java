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
package org.cote.rocket.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.propellant.objects.EstimateType;
import org.cote.propellant.objects.TaskType;
import org.cote.propellant.objects.TimeType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.EstimateFactory;
import org.cote.rocket.factory.TaskFactory;
import org.cote.rocket.factory.TimeFactory;
import org.junit.Test;
public class TestBulkUpdate extends BaseAccelerantTest{
	public static final Logger logger = LogManager.getLogger(TestBulkUpdate.class);
	
	@Test
	public void TestBulkUpdate(){
		

		
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		try {
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			TaskType task1 = ((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).getByNameInGroup("Task 1", dir);
			if(task1 == null) task1 = newTask("Task 1");
			TaskType task2 = ((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).getByNameInGroup("Task 2", dir);
			if(task2 == null) task2 = newTask("Task 2");
			TaskType task3 = ((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).getByNameInGroup("Task 3", dir);
			if(task3 == null) task3 = newTask("Task 3");
			
			TimeType time1 = ((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("Time 1", dir);
			if(time1 == null) time1 = newTime("Time 1",4);
			
			TimeType time2 = ((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("Time 2", dir);
			if(time2 == null) time2 = newTime("Time 2",4);

			TimeType time3 = ((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("Time 3", dir);
			if(time3 == null) time3 = newTime("Time 3",4);
			
			EstimateType est1 = ((EstimateFactory)Factories.getFactory(FactoryEnumType.ESTIMATE)).getByNameInGroup("Estimate 1",dir);
			if(est1 == null) est1 = newEstimate("Estimate 1", 8, 100);
			EstimateType est2 = ((EstimateFactory)Factories.getFactory(FactoryEnumType.ESTIMATE)).getByNameInGroup("Estimate 2",dir);
			if(est2 == null) est2 = newEstimate("Estimate 2", 8, 100);
			EstimateType est3 = ((EstimateFactory)Factories.getFactory(FactoryEnumType.ESTIMATE)).getByNameInGroup("Estimate 3",dir);
			if(est3 == null) est3 = newEstimate("Estimate 3", 8, 100);

			est1.setCost(null);
			
			task1.setEstimate(null);
			task2.setEstimate(est2);
			task1.setEstimate(est3);
			task1.getActualTime().clear();
			((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).update(task1);
			task1.getActualTime().add(time1);
			task2.getActualTime().clear();
			((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).update(task2);
			task2.getActualTime().add(time2);
			task3.getActualTime().clear();
			((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).update(task3);
			task3.getActualTime().add(time3);
			
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.TASK, task1);
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.TASK, task2);
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.TASK, task3);
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.ESTIMATE, est1);
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.ESTIMATE, est2);
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.ESTIMATE, est3);
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.TIME, time1);
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.TIME, time2);
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.TIME, time3);
			BulkFactories.getBulkFactory().write(sessionId);

		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}

		
		
		//BulkFactories.getBulkTaskFactory().u
	}

}