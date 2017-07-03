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
package org.cote.rocket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.propellant.objects.BudgetType;
import org.cote.propellant.objects.CostType;
import org.cote.propellant.objects.EstimateType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.MethodologyType;
import org.cote.propellant.objects.ProcessStepType;
import org.cote.propellant.objects.ProcessType;
import org.cote.propellant.objects.ProjectType;
import org.cote.propellant.objects.ResourceType;
import org.cote.propellant.objects.ScheduleType;
import org.cote.propellant.objects.StageType;
import org.cote.propellant.objects.TimeType;
import org.cote.propellant.objects.WorkType;
import org.cote.propellant.objects.types.BudgetEnumType;
import org.cote.propellant.objects.types.CurrencyEnumType;
import org.cote.propellant.objects.types.EstimateEnumType;
import org.cote.propellant.objects.types.ResourceEnumType;
import org.cote.propellant.objects.types.TimeEnumType;
import org.cote.rocket.factory.BudgetFactory;
import org.cote.rocket.factory.CostFactory;
import org.cote.rocket.factory.EstimateFactory;
import org.cote.rocket.factory.MethodologyFactory;
import org.cote.rocket.factory.ProcessFactory;
import org.cote.rocket.factory.ProcessStepFactory;
import org.cote.rocket.factory.ProjectFactory;
import org.cote.rocket.factory.ResourceFactory;
import org.cote.rocket.factory.ScheduleFactory;
import org.cote.rocket.factory.StageFactory;
import org.cote.rocket.factory.TimeFactory;
import org.cote.rocket.factory.WorkFactory;

/// Helper for generating out default models, such as Waterfall and Agile, into a Lifecycle or Project
///
public class RocketModel {
	public static final Logger logger = LogManager.getLogger(RocketModel.class);
	public static boolean emitIterations(UserType user, LifecycleType lifecycle, String[] resources, String startTime, String labelPrefix, int numberOfWeeks, int sprintLength) throws FactoryException, ArgumentException, DataAccessException{
		return emitIterations(user, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(lifecycle.getGroupId(), user.getOrganizationId()), null, resources, startTime, labelPrefix, numberOfWeeks, sprintLength);
	}
	public static boolean emitIterations(UserType user, ProjectType project, String[] resources, String startTime, String labelPrefix, int numberOfWeeks, int sprintLength) throws FactoryException, ArgumentException, DataAccessException{
		return emitIterations(user, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(project.getGroupId(), user.getOrganizationId()), project, resources, startTime, labelPrefix, numberOfWeeks, sprintLength);
	}

	private static boolean emitIterations(UserType user, DirectoryGroupType group, ProjectType project, String[] resources,String startTime, String labelPrefix, int numberOfWeeks, int sprintLength) throws FactoryException, ArgumentException, DataAccessException{
		if(project != null) ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).populate(project);
		
		DirectoryGroupType recDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Resources", group, group.getOrganizationId());
		DirectoryGroupType schDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Schedules", group, group.getOrganizationId());
		DirectoryGroupType stgDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Stages", group, group.getOrganizationId());
		DirectoryGroupType methDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Methodologies", group, group.getOrganizationId());
		DirectoryGroupType wrkDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Work", group, group.getOrganizationId());
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		
		List<ResourceType> recList = new ArrayList<ResourceType>();
		for(int i = 0; i < resources.length;i++){
			recList.add(addResource(user, sessionId, resources[i],recDir.getId()));
			
		}
		
		MethodologyType method = ((MethodologyFactory)Factories.getFactory(FactoryEnumType.METHODOLOGY)).getByNameInGroup("Agile - Sprint Only", methDir);
		
		boolean out_bool = false;
		int blocks = (int)Math.ceil((double)numberOfWeeks / (double)sprintLength);
		//Date start = startTime;
		Calendar startCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		Date start = CalendarUtil.importDateFromString(startTime);
		startCal.setTime(start);
		endCal.setTime(start);
		endCal.add(Calendar.DATE, sprintLength - 3);
		for(int i = 0; i < blocks; i++){
			
			ScheduleType sched = addSchedule(user, sessionId, labelPrefix + (i + 1), startCal, endCal, schDir.getId());
			WorkType work = addWork(user, sessionId, labelPrefix + (i + 1), 1, recList, wrkDir.getId());
			StageType stage = addStage(user, sessionId, labelPrefix + (i + 1),(i+1),method,work,sched,stgDir.getId());
			
			if(project != null) project.getStages().add(stage);
			
			startCal.add(Calendar.DATE, (sprintLength * 7));
			endCal.add(Calendar.DATE, (sprintLength * 7));
		}
		//Date end = start.set
		
		/*
		 * 
		 * function emitSprint(sLabel,oSched,iOrder,aTeam){
//function (sName, sDesc, iOrder,aDep, aArt, aTask, aRec, oGroup){
rocket.addWork(sLabel,"",1,0,0,0,aTeam,oSprintWorkGroup);
var oWork = rocket.getWork(sLabel,oSprintWorkGroup);
/// function (sName, sDesc, iOrder, oMeth, oWork, oBud, oSch, oGroup){
rocket.addStage(sLabel, "", iOrder,oMeth,oWork,0,oSched,oSprintStageGroup);
}

for(var i = 0; i < 26;i++){
var oSched = rocket.getSchedule("Two Week Iteration " + (i + 1));
emitSprint("Sprint " + (i + 1),oSched,(i + 1), aScrumTeam);
}
		 * 
		 * 
		 * 
		function emitIterations(sLabel,iWeeks, iIterLen){
			var iBlocks = iWeeks / iIterLen;
			var iDays = iIterLen * 7;
			var oStart = new Date(2012,10,05);
			var oEnd = new Date(2012,10,05);
			var oneDay = 1000 * 60 * 60 * 24;
			oEnd.setTime(oStart.getTime() + (oneDay * (iDays - 3)));

			for(var i = 0; i < iBlocks;i++){
			rocket.addSchedule(sLabel + " " + (i + 1),oStart,oEnd);
			oStart.setTime(oEnd.getTime() + (oneDay * 3));
			oEnd.setTime(oStart.getTime() + (oneDay * (iDays - 3)));
			}
			}
			//emitIterations("Four Week Iteration",52,4);
			emitIterations("Two Week Iteration",52,2);
			//emitIterations("One Week Iteration",52,1);
		*/
		BulkFactories.getBulkFactory().write(sessionId);
		BulkFactories.getBulkFactory().close(sessionId);
		if(project != null) ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).update(project);
		return out_bool;
		
	}
	
	public static boolean addAgileArtifacts(UserType user, LifecycleType lifecycle) throws FactoryException, ArgumentException, DataAccessException{
		return addAgileArtifacts(user,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(lifecycle.getGroupId(), user.getOrganizationId()));
	}
	public static boolean addAgileArtifacts(UserType user, ProjectType project) throws FactoryException, ArgumentException, DataAccessException{
		return addAgileArtifacts(user,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(project.getGroupId(), user.getOrganizationId()));
	}
	private static boolean addAgileArtifacts(UserType user, DirectoryGroupType lcGroup) throws FactoryException, ArgumentException, DataAccessException{
		boolean out_bool = false;
		
		DirectoryGroupType psDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("ProcessSteps", lcGroup, lcGroup.getOrganizationId());
		DirectoryGroupType pcDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Processes", lcGroup, lcGroup.getOrganizationId());
		DirectoryGroupType costDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Costs", lcGroup, lcGroup.getOrganizationId());
		DirectoryGroupType timeDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Times", lcGroup, lcGroup.getOrganizationId());
		DirectoryGroupType budDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Budgets", lcGroup, lcGroup.getOrganizationId());
		DirectoryGroupType estDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Estimates", lcGroup, lcGroup.getOrganizationId());
		DirectoryGroupType methDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Methodologies", lcGroup, lcGroup.getOrganizationId());
		
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		
		addProcessStep(user, sessionId,"Agile - Initial Planning","Pre iteration planning step",1,psDir.getId());
		addProcessStep(user, sessionId,"Agile - Deployment","Post iteration deployment step",1,psDir.getId());
		addProcessStep(user, sessionId,"Agile - Planning","Iteration planning",1,psDir.getId());
		addProcessStep(user, sessionId,"Agile - Gather Requirements","Iteration requirements gathering",2,psDir.getId());
		addProcessStep(user, sessionId,"Agile - Analysis and Design","Iteration analysis and design",3,psDir.getId());
		addProcessStep(user, sessionId,"Agile - Implementation","Iteration implementation",4,psDir.getId());
		addProcessStep(user, sessionId,"Agile - Testing","Iteration testing",5,psDir.getId());
		addProcessStep(user, sessionId,"Agile - Evaluation","Iteration evaluation",6,psDir.getId());
		
		
		addProcess(user, sessionId, "Agile - Initial Planning", "Agile initial planning process", 1, false, new ProcessStepType[]{(ProcessStepType)((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).getByNameInGroup("Agile - Initial Planning", psDir)}, new BudgetType[]{}, pcDir.getId());
		addProcess(user, sessionId, "Agile - Iteration", "Agile deployment process", 2, true, new ProcessStepType[]{(ProcessStepType)((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).getByNameInGroup("Agile - Planning", psDir),(ProcessStepType)((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).getByNameInGroup("Agile - Gather Requirements", psDir),(ProcessStepType)((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).getByNameInGroup("Agile - Analysis and Design", psDir),(ProcessStepType)((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).getByNameInGroup("Agile - Implementation", psDir),(ProcessStepType)((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).getByNameInGroup("Agile - Testing", psDir),(ProcessStepType)((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).getByNameInGroup("Agile - Evaluation", psDir)}, new BudgetType[]{}, pcDir.getId());
		addProcess(user, sessionId, "Agile - Deployment", "Agile deployment process", 3, false, new ProcessStepType[]{(ProcessStepType)((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).getByNameInGroup("Agile - Deployment", psDir)}, new BudgetType[]{}, pcDir.getId());		
		
		addTime(user, sessionId, "Agile - Standard Sprint",TimeEnumType.WEEK,2,timeDir.getId());
		addTime(user, sessionId, "Agile - 2x Sprint",TimeEnumType.WEEK,4,timeDir.getId());
		
		addCost(user, sessionId, "Agile - Standard Sprint", CurrencyEnumType.USD,40000,costDir.getId());
		addCost(user, sessionId, "Agile - 2x Sprint", CurrencyEnumType.USD,80000,costDir.getId());
		
		addBudget(user, sessionId, "Agile - Standard Sprint", "Standard sprint budget", BudgetEnumType.MULTIFACTOR, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("Agile - Standard Sprint",timeDir), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Agile - Standard Sprint",costDir), budDir.getId());
		addBudget(user, sessionId, "Agile - 2x Sprint", "Double-sized sprint budget", BudgetEnumType.MULTIFACTOR, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("Agile - 2x Sprint", timeDir), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Agile - 2x Sprint",costDir), budDir.getId());
		
		addEstimate(user, sessionId, "Agile - Sprint", "Coarse estimate for an agile sprint", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("Agile - Standard Sprint", timeDir), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Agile - Standard Sprint", costDir), estDir.getId());
		
		
		addMethodology(user, sessionId, "Agile","Agile project methodology",new ProcessType[]{(ProcessType)((ProcessFactory)Factories.getFactory(FactoryEnumType.PROCESS)).getByNameInGroup("Agile - Initial Planning",pcDir),(ProcessType)((ProcessFactory)Factories.getFactory(FactoryEnumType.PROCESS)).getByNameInGroup("Agile - Iteration", pcDir),(ProcessType)((ProcessFactory)Factories.getFactory(FactoryEnumType.PROCESS)).getByNameInGroup("Agile - Deployment", pcDir)},new BudgetType[0],methDir.getId());
		addMethodology(user, sessionId, "Agile - Sprint Only","Agile project methodology",new ProcessType[]{(ProcessType)((ProcessFactory)Factories.getFactory(FactoryEnumType.PROCESS)).getByNameInGroup("Agile - Iteration",pcDir)},new BudgetType[0],methDir.getId());
		
		
		BulkFactories.getBulkFactory().write(sessionId);
		BulkFactories.getBulkFactory().close(sessionId);
		out_bool = true;
		return out_bool;
	}
	
	public static boolean addWaterfallArtifacts(UserType user, LifecycleType lifecycle) throws FactoryException, ArgumentException, DataAccessException{
		return addWaterfallArtifacts(user,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(lifecycle.getGroupId(), user.getOrganizationId()));
	}
	public static boolean addWaterfallArtifacts(UserType user, ProjectType project) throws FactoryException, ArgumentException, DataAccessException{
		return addWaterfallArtifacts(user, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(project.getGroupId(), user.getOrganizationId()));
	}
	private static boolean addWaterfallArtifacts(UserType user, DirectoryGroupType lcGroup) throws FactoryException, ArgumentException, DataAccessException{
		boolean out_bool = false;
		
		DirectoryGroupType psDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("ProcessSteps", lcGroup, lcGroup.getOrganizationId());
		DirectoryGroupType pcDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Processes", lcGroup, lcGroup.getOrganizationId());
		DirectoryGroupType methDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Methodologies", lcGroup, lcGroup.getOrganizationId());
		
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		
		addProcessStep(user, sessionId,"Waterfall - Design","Waterfall design",1,psDir.getId());
		addProcessStep(user, sessionId,"Waterfall - Gather Requirements","Waterfall gather requirements",2,psDir.getId());
		addProcessStep(user, sessionId,"Waterfall - Implementation","Waterfall implementation",3,psDir.getId());
		addProcessStep(user, sessionId,"Waterfall - Maintenance","Waterfall maintenance",4,psDir.getId());
		addProcessStep(user, sessionId,"Waterfall - Verification","Waterfall verification",5,psDir.getId());
		
		addProcess(user, sessionId, "Waterfall", "Standard waterfall process", 1, false, new ProcessStepType[]{(ProcessStepType)((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).getByNameInGroup("Waterfall - Design", psDir),(ProcessStepType)((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).getByNameInGroup("Waterfall - Gather Requirements", psDir),(ProcessStepType)((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).getByNameInGroup("Waterfall - Implementation", psDir),(ProcessStepType)((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).getByNameInGroup("Waterfall - Maintenance", psDir),(ProcessStepType)((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).getByNameInGroup("Waterfall - Verification", psDir)}, new BudgetType[]{}, pcDir.getId());
		addMethodology(user, sessionId, "Waterfall","Waterfall project methodology",new ProcessType[]{(ProcessType)((ProcessFactory)Factories.getFactory(FactoryEnumType.PROCESS)).getByNameInGroup("Waterfall",pcDir)},new BudgetType[0],methDir.getId());
		
		BulkFactories.getBulkFactory().write(sessionId);
		BulkFactories.getBulkFactory().close(sessionId);
		
		out_bool = true;
		return out_bool;
	}
	
	public static boolean addDefaults(UserType user, long groupId) throws FactoryException, ArgumentException, DataAccessException{
		boolean out_bool = false;
		logger.info("****** Adding process model default values under parent group #" + groupId);
		DirectoryGroupType costDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Costs", ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(groupId, user.getOrganizationId()), user.getOrganizationId());
		DirectoryGroupType timeDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Times", ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(groupId, user.getOrganizationId()), user.getOrganizationId());
		DirectoryGroupType estDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Estimates", ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(groupId, user.getOrganizationId()), user.getOrganizationId());
		
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		addDefaultTimes(user, sessionId, timeDir.getId());
		addDefaultCosts(user, sessionId, costDir.getId());
		addDefaultEstimates(user, sessionId, timeDir, costDir, estDir.getId());
		BulkFactories.getBulkFactory().write(sessionId);
		BulkFactories.getBulkFactory().close(sessionId);
		return out_bool;
	}
	
	private static void addDefaultEstimates(UserType user, String sessionId, DirectoryGroupType timeGroup, DirectoryGroupType costGroup, long groupId) throws ArgumentException, FactoryException{

		addEstimate(user, sessionId, "25 USD", "Resource estimate per hour", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("One Hour", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Twenty Five", costGroup), groupId);
		addEstimate(user, sessionId, "50 USD", "Resource estimate per hour", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("One Hour", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Fifty", costGroup), groupId);
		addEstimate(user, sessionId, "75 USD", "Resource estimate per hour", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("One Hour", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Seventy Five", costGroup), groupId);
		addEstimate(user, sessionId, "100 USD", "Resource estimate per hour", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("One Hour", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("One Hundred", costGroup), groupId);
		addEstimate(user, sessionId, "125 USD", "Resource estimate per hour", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("One Hour", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("One Hundred Twenty Five", costGroup), groupId);
		addEstimate(user, sessionId, "150 USD", "Resource estimate per hour", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("One Hour", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("One Hundred Fifty", costGroup), groupId);
		addEstimate(user, sessionId, "175 USD", "Resource estimate per hour", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("One Hour", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("One Hundred Seventy Five", costGroup), groupId);
		addEstimate(user, sessionId, "200 USD", "Resource estimate per hour", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("One Hour", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Two Hundred", costGroup), groupId);
		
		addEstimate(user, sessionId, "Zero", "Estimate with no value", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("Zero", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Zero", costGroup), groupId);
		addEstimate(user, sessionId, "One Hour", "Coarse estimate for one hour", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("One Hour", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Zero", costGroup), groupId);
		addEstimate(user, sessionId, "Half Day", "Coarse estimate for a half day", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("Half Day", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Zero", costGroup), groupId);
		addEstimate(user, sessionId, "One Day", "Coarse estimate for a day", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("One Day", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Zero", costGroup), groupId);
		addEstimate(user, sessionId, "Two Days", "Coarse estimate for two days", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("Two Days", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Zero", costGroup), groupId);
		addEstimate(user, sessionId, "Three Days", "Coarse estimate for three days", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("Three Days", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Zero", costGroup), groupId);
		addEstimate(user, sessionId, "Four Days", "Coarse estimate for four days", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("Four Days", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Zero", costGroup), groupId);
		addEstimate(user, sessionId, "Five Days", "Coarse estimate for five days", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("One Week", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Zero", costGroup), groupId);
		addEstimate(user, sessionId, "Ten Days", "Coarse estimate for ten days", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("Two Weeks", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Zero", costGroup), groupId);
		
		addEstimate(user, sessionId, "One Week", "Coarse estimate for one week", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("One Week", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Zero", costGroup), groupId);
		addEstimate(user, sessionId, "Two Weeks", "Coarse estimate for two weeks", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("Two Weeks", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Zero", costGroup), groupId);
		addEstimate(user, sessionId, "Three Weeks", "Coarse estimate for three weeks", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("Three Weeks", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Zero", costGroup), groupId);
		addEstimate(user, sessionId, "Four Weeks", "Coarse estimate for four weeks", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("Four Weeks", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Zero", costGroup), groupId);
		addEstimate(user, sessionId, "One Month", "Coarse estimate for one month", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("One Month", timeGroup), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Zero", costGroup), groupId);
		
	}
	private static void addDefaultTimes(UserType user, String sessionId, long groupId) throws ArgumentException{
		addTime(user, sessionId, "Zero",TimeEnumType.HOUR,0,groupId);
		addTime(user, sessionId, "Half Hour",TimeEnumType.MINUTE,30,groupId);
		addTime(user, sessionId, "One Hour",TimeEnumType.HOUR,1,groupId);
		addTime(user, sessionId, "Half Day",TimeEnumType.HOUR,4,groupId);
		addTime(user, sessionId, "One Day",TimeEnumType.DAY,1,groupId);
		addTime(user, sessionId, "Two Days",TimeEnumType.DAY,2,groupId);
		addTime(user, sessionId, "Three Days",TimeEnumType.DAY,3,groupId);
		addTime(user, sessionId, "Four Days",TimeEnumType.DAY,4,groupId);
		addTime(user, sessionId, "One Week",TimeEnumType.WEEK,1,groupId);
		addTime(user, sessionId, "Two Weeks",TimeEnumType.WEEK,2,groupId);
		addTime(user, sessionId, "Three Weeks",TimeEnumType.WEEK,3,groupId);
		addTime(user, sessionId, "Four Weeks",TimeEnumType.WEEK,4,groupId);
		addTime(user, sessionId, "One Month",TimeEnumType.MONTH,1,groupId);
		addTime(user, sessionId, "Two Months",TimeEnumType.MONTH,2,groupId);
		addTime(user, sessionId, "One Year",TimeEnumType.YEAR,1,groupId);


	}
	
	private static void addDefaultCosts(UserType user, String sessionId, long groupId) throws ArgumentException{
		addCost(user, sessionId, "Zero", CurrencyEnumType.USD,0,groupId);
		addCost(user, sessionId, "Twenty Five", CurrencyEnumType.USD,25,groupId);
		addCost(user, sessionId, "Fifty", CurrencyEnumType.USD,100,groupId);
		addCost(user, sessionId, "Seventy Five", CurrencyEnumType.USD,75,groupId);
		addCost(user, sessionId, "One Hundred", CurrencyEnumType.USD,100,groupId);
		addCost(user, sessionId, "One Hundred Twenty Five", CurrencyEnumType.USD,125,groupId);
		addCost(user, sessionId, "One Hundred Fifty", CurrencyEnumType.USD,150,groupId);
		addCost(user, sessionId, "One Hundred Seventy Five", CurrencyEnumType.USD,175,groupId);
		addCost(user, sessionId, "Two Hundred", CurrencyEnumType.USD,200,groupId);
		addCost(user, sessionId, "One Thousand", CurrencyEnumType.USD,1000,groupId);
		addCost(user, sessionId, "Ten Thousand", CurrencyEnumType.USD,10000,groupId);
		addCost(user, sessionId, "Fifty Thousand", CurrencyEnumType.USD,10000,groupId);
		addCost(user, sessionId, "One Hundred Thousand", CurrencyEnumType.USD,100000,groupId);
		addCost(user, sessionId, "One Million", CurrencyEnumType.USD,1000000,groupId);

	}
	
	
	public static EstimateType addEstimate(UserType user, String sessionId, String name, String description, EstimateEnumType estimateType, TimeType time, CostType cost, long groupId) throws ArgumentException{
		if(time == null || cost == null) throw new ArgumentException("Invalid time or cost for estimate " + name);
		EstimateType estimate = ((EstimateFactory)Factories.getFactory(FactoryEnumType.ESTIMATE)).newEstimate(user, groupId);
		estimate.setName(name);
		estimate.setDescription(description);
		estimate.setEstimateType(estimateType);
		estimate.setCost(cost);
		estimate.setTime(time);
		
		BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ESTIMATE, estimate);
		return estimate;
	}

	public static BudgetType addBudget(UserType user, String sessionId, String name, String description, BudgetEnumType budgetType, TimeType time, CostType cost, long groupId) throws ArgumentException{
		BudgetType budget = ((BudgetFactory)Factories.getFactory(FactoryEnumType.BUDGET)).newBudget(user,groupId);
		budget.setName(name);
		budget.setDescription(description);
		budget.setBudgetType(budgetType);
		budget.setCost(cost);
		budget.setTime(time);
		
		BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.BUDGET, budget);
		return budget;
	}

	
	public static CostType addCost(UserType user, String sessionId, String name, CurrencyEnumType currencyType, double value, long groupId) throws ArgumentException{
		CostType cost = ((CostFactory)Factories.getFactory(FactoryEnumType.COST)).newCost(user, groupId);
		cost.setName(name);
		cost.setCurrencyType(currencyType);
		cost.setValue(value);
		BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.COST, cost);
		return cost;
	}

	
	
	public static TimeType addTime(UserType user, String sessionId, String name, TimeEnumType timeType, double value, long groupId) throws ArgumentException{
		TimeType time = ((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).newTime(user, groupId);
		time.setName(name);
		time.setBasisType(timeType);
		time.setValue(value);
		BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.TIME, time);
		return time;
	}

	private static ProcessType addProcess(UserType user, String sessionId, String name, String desc, int order, boolean iterates, ProcessStepType[] steps, BudgetType[] budgets, long groupId) throws ArgumentException{
		ProcessType out_step = ((ProcessFactory)Factories.getFactory(FactoryEnumType.PROCESS)).newProcess(user, groupId);
		out_step.setName(name);
		out_step.setDescription(desc);
		out_step.setLogicalOrder(order);
		out_step.setIterates(iterates);
		out_step.getSteps().addAll(Arrays.asList(steps));
		out_step.getBudgets().addAll(Arrays.asList(budgets));
		BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PROCESS, out_step);
		return out_step;
	}
	
	private static ProcessStepType addProcessStep(UserType user, String sessionId, String name, String desc, int order, long groupId) throws ArgumentException{
		ProcessStepType out_step = ((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).newProcessStep(user, groupId);
		out_step.setName(name);
		out_step.setDescription(desc);
		out_step.setLogicalOrder(order);
		BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PROCESSSTEP, out_step);
		return out_step;
	}
	private static MethodologyType addMethodology(UserType user, String sessionId, String name, String desc, ProcessType[] processes, BudgetType[] budgets, long groupId) throws ArgumentException{
		MethodologyType method = ((MethodologyFactory)Factories.getFactory(FactoryEnumType.METHODOLOGY)).newMethodology(user, groupId);
		method.setName(name);
		method.setDescription(desc);
		method.getBudgets().addAll(Arrays.asList(budgets));
		method.getProcesses().addAll(Arrays.asList(processes));
		BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.METHODOLOGY, method);
		return method;
	}
	
	private static ScheduleType addSchedule(UserType user, String sessionId, String name, Calendar startCal, Calendar endCal, long groupId) throws ArgumentException{
		ScheduleType sched = ((ScheduleFactory)Factories.getFactory(FactoryEnumType.SCHEDULE)).newSchedule(user, groupId);
		sched.setName(name);
		sched.setStartTime(CalendarUtil.getXmlGregorianCalendar(startCal.getTime()));
		sched.setEndTime(CalendarUtil.getXmlGregorianCalendar(endCal.getTime()));
		BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.SCHEDULE,sched);
		return sched;
	}

	private static ResourceType addResource(UserType user, String sessionId, String name, long groupId) throws ArgumentException{
		ResourceType rec = ((ResourceFactory)Factories.getFactory(FactoryEnumType.RESOURCE)).newResource(user, groupId);
		rec.setName(name);
		rec.setResourceType(ResourceEnumType.UNKNOWN);
		BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.RESOURCE,rec);
		return rec;
	}

	private static WorkType addWork(UserType user, String sessionId, String name, int logicalOrder, List<ResourceType> resources, long groupId) throws ArgumentException{
		WorkType wrk = ((WorkFactory)Factories.getFactory(FactoryEnumType.WORK)).newWork(user, groupId);
		wrk.setName(name);
		wrk.getResources().addAll(resources);
		wrk.setLogicalOrder(logicalOrder);
		BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.WORK,wrk);
		return wrk;
	}

	private static StageType addStage(UserType user, String sessionId, String name, int logicalOrder, MethodologyType method, WorkType work, ScheduleType schedule, long groupId) throws ArgumentException{
		StageType stg = ((StageFactory)Factories.getFactory(FactoryEnumType.STAGE)).newStage(user, groupId);
		stg.setName(name);
		stg.setMethodology(method);
		stg.setSchedule(schedule);
		stg.setLogicalOrder(logicalOrder);
		stg.setWork(work);
		BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.STAGE,stg);
		return stg;
	}

	
	
}
