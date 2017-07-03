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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.propellant.objects.BudgetParticipantType;
import org.cote.propellant.objects.BudgetType;
import org.cote.propellant.objects.GoalParticipantType;
import org.cote.propellant.objects.GoalType;
import org.cote.propellant.objects.MethodologyType;
import org.cote.propellant.objects.ProcessParticipantType;
import org.cote.propellant.objects.ProcessStepParticipantType;
import org.cote.propellant.objects.ProcessStepType;
import org.cote.propellant.objects.ProcessType;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.MethodologyFactory;
import org.cote.rocket.factory.MethodologyParticipationFactory;
import org.cote.rocket.factory.ProcessParticipationFactory;
import org.cote.rocket.factory.ProcessStepParticipationFactory;
import org.junit.Test;
public class TestMethodologyParticipationFactory extends BaseAccelerantTest{
	public static final Logger logger = LogManager.getLogger(TestMethodologyParticipationFactory.class);
	
	@Test
	public void TestNewParts(){
		MethodologyType work = null;
		try {
			for(int i = 0; i< 1;i++){
				Date start = new Date();
				
				String workName = UUID.randomUUID().toString();
				work = newMethodology(workName);
				
				String processName = UUID.randomUUID().toString();
				ProcessType process = newProcess(processName);
				ProcessStepType processStep = newProcessStep(processName);
				
				GoalType goal = newGoal(processName);
				GoalParticipantType gpt = ((ProcessStepParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEPPARTICIPATION)).newGoalParticipation(processStep, goal);
				((ProcessStepParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEPPARTICIPATION)).add(gpt);
				BudgetType budget = newBudget(workName);
				BudgetParticipantType bpt1 = ((ProcessStepParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEPPARTICIPATION)).newBudgetParticipation(processStep, budget);
				((ProcessStepParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEPPARTICIPATION)).add(bpt1);
				BudgetParticipantType bpt2 = ((ProcessParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSPARTICIPATION)).newBudgetParticipation(process, budget);
				((ProcessParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSPARTICIPATION)).add(bpt2);
				
				ProcessStepParticipantType pst = ((ProcessParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSPARTICIPATION)).newProcessStepParticipation(process, processStep);
				((ProcessParticipationFactory)Factories.getFactory(FactoryEnumType.PROCESSPARTICIPATION)).add(pst);
				ProcessParticipantType ppt = ((MethodologyParticipationFactory)Factories.getFactory(FactoryEnumType.METHODOLOGYPARTICIPATION)).newProcessParticipation(work, process);
				((MethodologyParticipationFactory)Factories.getFactory(FactoryEnumType.METHODOLOGYPARTICIPATION)).add(ppt);
				
				
				BudgetParticipantType bpt = ((MethodologyParticipationFactory)Factories.getFactory(FactoryEnumType.METHODOLOGYPARTICIPATION)).newBudgetParticipation(work, budget);
				((MethodologyParticipationFactory)Factories.getFactory(FactoryEnumType.METHODOLOGYPARTICIPATION)).add(bpt);
	
				//((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).populate(task);
				((MethodologyFactory)Factories.getFactory(FactoryEnumType.METHODOLOGY)).populate(work);
				
				Date stop = new Date();
				logger.info("Testing methodology " + work.getName() + " in " + (stop.getTime() - start.getTime()));
			}
			
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		}
		
		catch (ArgumentException e) {
			
			logger.error("Error",e);
		} 
		
		
		assertNotNull("Methodology is null", work);
		assertTrue("Processes not populated", work.getProcesses().size() > 0);
		

	}
}