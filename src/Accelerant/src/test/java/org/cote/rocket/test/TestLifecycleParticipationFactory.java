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

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.propellant.objects.BudgetParticipantType;
import org.cote.propellant.objects.BudgetType;
import org.cote.propellant.objects.CostType;
import org.cote.propellant.objects.GoalParticipantType;
import org.cote.propellant.objects.GoalType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectParticipantType;
import org.cote.propellant.objects.ProjectType;
import org.cote.propellant.objects.ScheduleParticipantType;
import org.cote.propellant.objects.ScheduleType;
import org.cote.propellant.objects.TimeType;
import org.cote.propellant.objects.types.BudgetEnumType;
import org.cote.propellant.objects.types.CurrencyEnumType;
import org.cote.propellant.objects.types.GoalEnumType;
import org.cote.propellant.objects.types.TimeEnumType;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.BudgetFactory;
import org.cote.rocket.factory.CostFactory;
import org.cote.rocket.factory.GoalFactory;
import org.cote.rocket.factory.LifecycleFactory;
import org.cote.rocket.factory.LifecycleParticipationFactory;
import org.cote.rocket.factory.ProjectFactory;
import org.cote.rocket.factory.ScheduleFactory;
import org.cote.rocket.factory.TimeFactory;
import org.junit.Test;
public class TestLifecycleParticipationFactory extends BaseAccelerantTest{
	public static final Logger logger = LogManager.getLogger(TestLifecycleFactory.class);
	private static String testUserName = "RocketQAUser";
	private static String lifecycleName1 = "Lifecycle QA #1";
	private static String lifecycleName2 = "Lifecycle QA #2";
	private static LifecycleType lifecycle1 = null;
	private static LifecycleType lifecycle2 = null;
	

	@Test
	public void testCreateParts(){
		boolean added = false;
		assertTrue("Lifecycles not created",(lifecycle1 != null && lifecycle2 != null));
	
		
		try{
			ScheduleType sched1 = ((ScheduleFactory)Factories.getFactory(FactoryEnumType.SCHEDULE)).newSchedule(testUser, testUser.getHomeDirectory().getId());
			sched1.setName(UUID.randomUUID().toString());
			added = ((ScheduleFactory)Factories.getFactory(FactoryEnumType.SCHEDULE)).add(sched1);
			sched1 = ((ScheduleFactory)Factories.getFactory(FactoryEnumType.SCHEDULE)).getByNameInGroup(sched1.getName(), testUser.getHomeDirectory());
			ScheduleParticipantType spt = ((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).newScheduleParticipation(lifecycle1, sched1);
			added = ((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).add(spt);
			
			TimeType time = ((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).newTime(testUser, testUser.getHomeDirectory().getId());
			time.setBasisType(TimeEnumType.HOUR);
			time.setValue((double)5);
			time.setName(UUID.randomUUID().toString());
			((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).add(time);
			time = ((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup(time.getName(), testUser.getHomeDirectory());
			
			CostType cost = ((CostFactory)Factories.getFactory(FactoryEnumType.COST)).newCost(testUser,  testUser.getHomeDirectory().getId());
			cost.setCurrencyType(CurrencyEnumType.USD);
			cost.setValue((double)5);
			cost.setName(UUID.randomUUID().toString());
			((CostFactory)Factories.getFactory(FactoryEnumType.COST)).add(cost);
			cost = ((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup(cost.getName(), testUser.getHomeDirectory());
			
			BudgetType budget = ((BudgetFactory)Factories.getFactory(FactoryEnumType.BUDGET)).newBudget(testUser, testUser.getHomeDirectory().getId());
			budget.setName(UUID.randomUUID().toString());
			budget.setTime(time);
			budget.setCost(cost);
			budget.setBudgetType(BudgetEnumType.MULTIFACTOR);
			
			((BudgetFactory)Factories.getFactory(FactoryEnumType.BUDGET)).add(budget);
			budget = ((BudgetFactory)Factories.getFactory(FactoryEnumType.BUDGET)).getByNameInGroup(budget.getName(), testUser.getHomeDirectory());
			BudgetParticipantType bpt = ((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).newBudgetParticipation(lifecycle1,  budget);
			((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).add(bpt);
			
			GoalType goal = ((GoalFactory)Factories.getFactory(FactoryEnumType.GOAL)).newGoal(testUser, testUser.getHomeDirectory().getId());
			goal.setName(UUID.randomUUID().toString());
			goal.setGoalType(GoalEnumType.STAKE);
			((GoalFactory)Factories.getFactory(FactoryEnumType.GOAL)).add(goal);
			goal = ((GoalFactory)Factories.getFactory(FactoryEnumType.GOAL)).getByNameInGroup(goal.getName(), testUser.getHomeDirectory());
			
			GoalParticipantType gpt = ((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).newGoalParticipation(lifecycle1,  goal);
			((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).add(gpt);
			
			ProjectType project = ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).newProject(testUser,  testUser.getHomeDirectory().getId());
			project.setName(UUID.randomUUID().toString());
			((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).add(project);
			project = ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).getByNameInGroup(project.getName(), testUser.getHomeDirectory());
			
			ProjectParticipantType ppt = ((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).newProjectParticipation(lifecycle1,  project);
			((LifecycleParticipationFactory)Factories.getFactory(FactoryEnumType.LIFECYCLEPARTICIPATION)).add(ppt);
			
			
			((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).populate(lifecycle1);
			((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).populate(lifecycle2);
			
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			System.out.println(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			System.out.println(e.getMessage());
		} 
		
		logger.info("Lifecycle " + lifecycle1.getName());
		logger.info("Budgets: " + lifecycle1.getBudgets().size());
		logger.info("Projects: " + lifecycle1.getProjects().size());
		logger.info("Goals: " + lifecycle1.getGoals().size());
		logger.info("Schedules: " + lifecycle1.getSchedules().size());
		
		assertTrue("Parts not added",added);
		assertTrue("Lifecycle schedules not populated",lifecycle1.getSchedules().size() > 0);
		assertTrue("Lifecycle parts should not be populated",lifecycle2.getSchedules().size() == 0);
		assertTrue("Lifecycle budgets not populated",lifecycle1.getBudgets().size() > 0);
		assertTrue("Lifecycle parts should not be populated",lifecycle2.getBudgets().size() == 0);
		assertTrue("Lifecycle goals not populated",lifecycle1.getGoals().size() > 0);
		assertTrue("Lifecycle parts should not be populated",lifecycle2.getGoals().size() == 0);
		assertTrue("Lifecycle projects not populated",lifecycle1.getProjects().size() > 0);
		assertTrue("Lifecycle parts should not be populated",lifecycle2.getProjects().size() == 0);

		
	}
	
}