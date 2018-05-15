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

import java.util.Date;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.propellant.objects.ArtifactParticipantType;
import org.cote.propellant.objects.CostParticipantType;
import org.cote.propellant.objects.NoteParticipantType;
import org.cote.propellant.objects.RequirementParticipantType;
import org.cote.propellant.objects.ResourceParticipantType;
import org.cote.propellant.objects.TaskType;
import org.cote.propellant.objects.TimeParticipantType;
import org.cote.propellant.objects.WorkParticipantType;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.TaskFactory;
import org.cote.rocket.factory.TaskParticipationFactory;
import org.junit.Test;
public class TestTaskParticipationFactory extends BaseAccelerantTest{
	public static final Logger logger = LogManager.getLogger(TestTaskParticipationFactory.class);
	
	@Test
	public void TestNewParts(){

		try {
			for(int i = 0; i< 1;i++){
				Date start = new Date();
				String taskName = UUID.randomUUID().toString();
				TaskType task = newTask(taskName);
				
				ArtifactParticipantType apt = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newArtifactParticipation(task, newArtifact("A" + taskName));
				((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(apt);
				RequirementParticipantType rpt = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newRequirementParticipation(task, newRequirement(taskName));
				((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(rpt);
				WorkParticipantType wpt = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newWorkParticipation(task, newWork(taskName));
				((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(wpt);
				NoteParticipantType npt = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newNoteParticipation(task,  newNote(taskName,"Example note"));
				((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(npt);
				TimeParticipantType tpt = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newTimeParticipation(task,  newTime(taskName,4));
				((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(tpt);
				CostParticipantType cpt = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newCostParticipation(task, newCost(taskName,4));
				((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(cpt);
				ArtifactParticipantType dpt = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newDependencyParticipation(task, newDependency("D" + taskName));
				((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(dpt);
				ResourceParticipantType ppt = ((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).newResourceParticipation(task, newResource(taskName,testUser.getId()));
				((TaskParticipationFactory)Factories.getFactory(FactoryEnumType.TASKPARTICIPATION)).add(ppt);
				
				((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).populate(task);
				
				Date stop = new Date();
				logger.info("Testing task " + task.getName() + " in " + (stop.getTime() - start.getTime()));
			}
			
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} 

		

	}
}