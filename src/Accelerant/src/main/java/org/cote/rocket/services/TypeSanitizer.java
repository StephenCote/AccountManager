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
package org.cote.rocket.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.INameIdGroupFactory;
import org.cote.accountmanager.data.services.ITypeSanitizer;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.MapUtil;
import org.cote.propellant.objects.ArtifactType;
import org.cote.propellant.objects.BudgetType;
import org.cote.propellant.objects.CaseType;
import org.cote.propellant.objects.CostType;
import org.cote.propellant.objects.EstimateType;
import org.cote.propellant.objects.EventType;
import org.cote.propellant.objects.FormElementType;
import org.cote.propellant.objects.FormType;
import org.cote.propellant.objects.GoalType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.LocationType;
import org.cote.propellant.objects.MethodologyType;
import org.cote.propellant.objects.ModelType;
import org.cote.propellant.objects.ModuleType;
import org.cote.propellant.objects.NoteType;
import org.cote.propellant.objects.ProcessStepType;
import org.cote.propellant.objects.ProcessType;
import org.cote.propellant.objects.ProjectType;
import org.cote.propellant.objects.RequirementType;
import org.cote.propellant.objects.ResourceType;
import org.cote.propellant.objects.ScheduleType;
import org.cote.propellant.objects.StageType;
import org.cote.propellant.objects.TaskType;
import org.cote.propellant.objects.TicketType;
import org.cote.propellant.objects.TimeType;
import org.cote.propellant.objects.TraitType;
import org.cote.propellant.objects.ValidationRuleType;
import org.cote.propellant.objects.WorkType;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.ArtifactFactory;
import org.cote.rocket.factory.BudgetFactory;
import org.cote.rocket.factory.CaseFactory;
import org.cote.rocket.factory.CostFactory;
import org.cote.rocket.factory.EstimateFactory;
import org.cote.rocket.factory.EventFactory;
import org.cote.rocket.factory.FormElementFactory;
import org.cote.rocket.factory.FormFactory;
import org.cote.rocket.factory.GoalFactory;
import org.cote.rocket.factory.LifecycleFactory;
import org.cote.rocket.factory.LocationFactory;
import org.cote.rocket.factory.MethodologyFactory;
import org.cote.rocket.factory.ModelFactory;
import org.cote.rocket.factory.ModuleFactory;
import org.cote.rocket.factory.NoteFactory;
import org.cote.rocket.factory.ProcessFactory;
import org.cote.rocket.factory.ProcessStepFactory;
import org.cote.rocket.factory.ProjectFactory;
import org.cote.rocket.factory.RequirementFactory;
import org.cote.rocket.factory.ResourceFactory;
import org.cote.rocket.factory.ScheduleFactory;
import org.cote.rocket.factory.StageFactory;
import org.cote.rocket.factory.TaskFactory;
import org.cote.rocket.factory.TicketFactory;
import org.cote.rocket.factory.TimeFactory;
import org.cote.rocket.factory.TraitFactory;
import org.cote.rocket.factory.ValidationRuleFactory;
import org.cote.rocket.factory.WorkFactory;

public class TypeSanitizer implements ITypeSanitizer{
	public static final Logger logger = LogManager.getLogger(TypeSanitizer.class);
	public TypeSanitizer(){
		
	}
	public <T> boolean useAlternateDelete(AuditEnumType type, T object){
		return false;
	}
	public <T> boolean useAlternateUpdate(AuditEnumType type, T object){
		return (type.equals(AuditEnumType.FORM));
	}
	public <T> boolean useAlternateAdd(AuditEnumType type, T object){
		return (type.equals(AuditEnumType.FORM));
	}
	public <T> boolean usePostFetch(AuditEnumType type, T object){
		return false;
	}
	public <T> T postFetch(AuditEnumType type, UserType user, T object){
		return object;
	}
	public <T> boolean delete(AuditEnumType type, T object) throws FactoryException, ArgumentException{
		return false;
	}
	public <T> boolean update(AuditEnumType type, UserType owner, T object) throws FactoryException, ArgumentException{
		boolean outBool = false;
		INameIdFactory iFact = Factories.getFactory(FactoryEnumType.valueOf(type.toString()));
		if(type.equals(AuditEnumType.FORM)){
			
			outBool = ValidationService.validateForm(owner,(FormType)object);
			if(!outBool){
				logger.warn("Failed to validate form");
				return false;
			}
			outBool = iFact.update((FormType)object);
			if(outBool == false){
				logger.warn("Failed to update form");
				return false;
			}
			outBool = FormService.updateFormValues(owner,(FormType)object,false);
			if(outBool == false) logger.warn("Failed to update form values");
		}
		return outBool;
	}
	public <T> boolean add(AuditEnumType type, UserType owner, T object) throws FactoryException, ArgumentException{
		boolean outBool = false;
		logger.info("Processing alternate add for type " + type.toString());
		INameIdFactory iFact = Factories.getNameIdFactory(FactoryEnumType.valueOf(type.toString()));
		if(type.equals(AuditEnumType.FORM)){
			INameIdGroupFactory iGFact = (INameIdGroupFactory)iFact;
			FormType form = (FormType)object;
			outBool = ValidationService.validateForm(owner,form);
			if(outBool) outBool = iFact.add(form);
			if(outBool){
				FormType new_form = iGFact.getByNameInGroup(form.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(form.getGroupId(),form.getOrganizationId()));
				outBool = FormService.updateFormValues(owner, new_form,false);
			}
		}
		return outBool;
	}
	@SuppressWarnings("unchecked")
	public <T> T sanitizeNewObject(AuditEnumType type, UserType owner, T in_obj) throws ArgumentException, FactoryException{
		T out_obj = null;
		switch(type){
			case VALIDATIONRULE:
				ValidationRuleType vbean = (ValidationRuleType)in_obj;
				ValidationRuleType new_ValidationRule = ((ValidationRuleFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULE)).newValidationRule(owner, vbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(vbean,new_ValidationRule);
				new_ValidationRule.setDescription(vbean.getDescription());
				new_ValidationRule.setExpression(vbean.getExpression());
				new_ValidationRule.setComparison(vbean.getComparison());
				new_ValidationRule.setErrorMessage(vbean.getErrorMessage());
				new_ValidationRule.setIsReplacementRule(vbean.getIsReplacementRule());
				new_ValidationRule.setIsRuleSet(vbean.getIsRuleSet());
				new_ValidationRule.setReplacementValue(vbean.getReplacementValue());
				new_ValidationRule.setValidationType(vbean.getValidationType());
				new_ValidationRule.getRules().addAll(vbean.getRules());
				new_ValidationRule.setAllowNull(vbean.getAllowNull());
				out_obj = (T)new_ValidationRule;
	
				break;
			case FORM:
				FormType fbean = (FormType)in_obj;
				FormType new_Form = ((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).newForm(owner, fbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(fbean,new_Form);
				new_Form.setDescription(fbean.getDescription());
				new_Form.setIsTemplate(fbean.getIsTemplate());
				new_Form.setTemplate(fbean.getTemplate());
				new_Form.setIsGrid(fbean.getIsGrid());
				new_Form.setViewTemplate(fbean.getViewTemplate());
				new_Form.getElements().addAll(fbean.getElements());
				new_Form.getChildForms().addAll(fbean.getChildForms());
				out_obj = (T)new_Form;
				break;
			case FORMELEMENT:
				FormElementType febean = (FormElementType)in_obj;
				FormElementType new_FormElement = ((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).newFormElement(owner, febean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(febean,new_FormElement);
				new_FormElement.setDescription(febean.getDescription());
				new_FormElement.setElementLabel(febean.getElementLabel());
				new_FormElement.setElementName(febean.getElementName());
				new_FormElement.setElementType(febean.getElementType());
				new_FormElement.setValidationRule(febean.getValidationRule());
				new_FormElement.getElementValues().addAll(febean.getElementValues());
				out_obj = (T)new_FormElement;
				break;
			case PROJECT:
				ProjectType pjbean = (ProjectType)in_obj;
				ProjectType new_Project = ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).newProject(owner, pjbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(pjbean,new_Project);
				new_Project.setSchedule(pjbean.getSchedule());
				new_Project.setDescription(pjbean.getDescription());
				new_Project.getArtifacts().addAll(pjbean.getArtifacts());
				new_Project.getDependencies().addAll(pjbean.getDependencies());
				new_Project.getRequirements().addAll(pjbean.getRequirements());
				new_Project.getBlueprints().addAll(pjbean.getBlueprints());
				new_Project.getModules().addAll(pjbean.getModules());
				new_Project.getStages().addAll(pjbean.getStages());
				out_obj = (T)new_Project;
				break;
				
			case MODULE:
				ModuleType mlbean = (ModuleType)in_obj;
				ModuleType new_Module = ((ModuleFactory)Factories.getFactory(FactoryEnumType.MODULE)).newModule(owner, mlbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(mlbean,new_Module);
				new_Module.setModuleType(mlbean.getModuleType());
				new_Module.setDescription(mlbean.getDescription());
				new_Module.getArtifacts().addAll(mlbean.getArtifacts());
				new_Module.getWork().addAll(mlbean.getWork());
				new_Module.setActualCost(mlbean.getActualCost());
				new_Module.setActualTime(mlbean.getActualTime());
				out_obj = (T)new_Module;
				break;
			
			case STAGE:
				StageType sgbean = (StageType)in_obj;
				StageType new_Stage = ((StageFactory)Factories.getFactory(FactoryEnumType.STAGE)).newStage(owner, sgbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(sgbean,new_Stage);
				new_Stage.setMethodology(sgbean.getMethodology());
				new_Stage.setBudget(sgbean.getBudget());
				new_Stage.setWork(sgbean.getWork());
				new_Stage.setSchedule(sgbean.getSchedule());
				new_Stage.setDescription(sgbean.getDescription());
				new_Stage.setLogicalOrder(sgbean.getLogicalOrder());
				out_obj = (T)new_Stage;
				break;		
			
			case METHODOLOGY:
				MethodologyType mybean = (MethodologyType)in_obj;
				MethodologyType new_Methodology = ((MethodologyFactory)Factories.getFactory(FactoryEnumType.METHODOLOGY)).newMethodology(owner, mybean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(mybean,new_Methodology);
				new_Methodology.setDescription(mybean.getDescription());
				new_Methodology.getProcesses().addAll(mybean.getProcesses());
				new_Methodology.getBudgets().addAll(mybean.getBudgets());
				out_obj = (T)new_Methodology;
				break;
		
			case PROCESS:
				ProcessType pcbean = (ProcessType)in_obj;
				ProcessType new_Process = ((ProcessFactory)Factories.getFactory(FactoryEnumType.PROCESS)).newProcess(owner, pcbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(pcbean,new_Process);
				new_Process.setDescription(pcbean.getDescription());
				new_Process.setLogicalOrder(pcbean.getLogicalOrder());
				new_Process.setIterates(pcbean.getIterates());
				new_Process.getSteps().addAll(pcbean.getSteps());
				new_Process.getBudgets().addAll(pcbean.getBudgets());
				out_obj = (T)new_Process;
				break;
				
			case PROCESSSTEP:
				ProcessStepType psbean = (ProcessStepType)in_obj;
				ProcessStepType new_ProcessStep = ((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).newProcessStep(owner, psbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(psbean,new_ProcessStep);
				new_ProcessStep.setDescription(psbean.getDescription());
				new_ProcessStep.setLogicalOrder(psbean.getLogicalOrder());
				new_ProcessStep.getRequirements().addAll(psbean.getRequirements());
				new_ProcessStep.getGoals().addAll(psbean.getGoals());
				new_ProcessStep.getBudgets().addAll(psbean.getBudgets());
				out_obj = (T)new_ProcessStep;
				break;
			
			case WORK:
				WorkType wkbean = (WorkType)in_obj;
				WorkType new_Work = ((WorkFactory)Factories.getFactory(FactoryEnumType.WORK)).newWork(owner, wkbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(wkbean,new_Work);
	
				new_Work.setDescription(wkbean.getDescription());
				new_Work.setLogicalOrder(wkbean.getLogicalOrder());
				new_Work.getTasks().addAll(wkbean.getTasks());
				new_Work.getArtifacts().addAll(wkbean.getArtifacts());
				new_Work.getDependencies().addAll(wkbean.getDependencies());
				new_Work.getResources().addAll(wkbean.getResources());
				out_obj = (T)new_Work;
				break;
			case TICKET:
				TicketType tibean = (TicketType)in_obj;
				TicketType new_Ticket = ((TicketFactory)Factories.getFactory(FactoryEnumType.TICKET)).newTicket(owner, tibean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(tibean,new_Ticket);
				new_Ticket.setTicketStatus(tibean.getTicketStatus());
				new_Ticket.setDescription(tibean.getDescription());
				new_Ticket.setEstimate(tibean.getEstimate());
				new_Ticket.setPriority(tibean.getPriority());
				new_Ticket.setSeverity(tibean.getSeverity());
				new_Ticket.getArtifacts().addAll(tibean.getArtifacts());
				new_Ticket.setActualCost(tibean.getActualCost());
				new_Ticket.setActualTime(tibean.getActualTime());
				new_Ticket.setAssignedResource(tibean.getAssignedResource());
				new_Ticket.getTickets().addAll(tibean.getTickets());
				new_Ticket.getDependencies().addAll(tibean.getDependencies());
				new_Ticket.getNotes().addAll(tibean.getNotes());
				new_Ticket.getRequiredResources().addAll(tibean.getRequiredResources());
				new_Ticket.getForms().addAll(tibean.getForms());
				
				if(tibean.getClosedDate() != null) new_Ticket.setClosedDate(tibean.getClosedDate());
				if(tibean.getReopenedDate() != null) new_Ticket.setReopenedDate(tibean.getReopenedDate());
				if(tibean.getModifiedDate() != null) new_Ticket.setModifiedDate(tibean.getModifiedDate());
				if(tibean.getDueDate() != null) new_Ticket.setDueDate(tibean.getDueDate());
	
				out_obj = (T)new_Ticket;
				
				break;				
			case TASK:
				TaskType ttbean = (TaskType)in_obj;
				TaskType new_task = ((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).newTask(owner, ttbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(ttbean,new_task);
				if(ttbean.getTaskStatus() != null) new_task.setTaskStatus(ttbean.getTaskStatus());
				new_task.setDescription(ttbean.getDescription());
				new_task.setEstimate(ttbean.getEstimate());
				new_task.setLogicalOrder(ttbean.getLogicalOrder());
				new_task.getArtifacts().addAll(ttbean.getArtifacts());
				new_task.getActualCost().addAll(ttbean.getActualCost());
				new_task.getActualTime().addAll(ttbean.getActualTime());
				new_task.getDependencies().addAll(ttbean.getDependencies());
				new_task.getNotes().addAll(ttbean.getNotes());
				new_task.getRequirements().addAll(ttbean.getRequirements());
				new_task.getResources().addAll(ttbean.getResources());
				new_task.getWork().addAll(ttbean.getWork());
				if(ttbean.getStartDate() != null) new_task.setCompletedDate(ttbean.getStartDate());
				if(ttbean.getCompletedDate() != null) new_task.setCompletedDate(ttbean.getCompletedDate());
				if(ttbean.getDueDate() != null) new_task.setDueDate(ttbean.getDueDate());
	
				out_obj = (T)new_task;
				
				break;
			case EVENT:
				EventType evbean = (EventType)in_obj;
				EventType new_event = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).newEvent(owner, evbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(evbean, new_event);
				new_event.getActors().addAll(evbean.getActors());
				new_event.getEntryTraits().addAll(evbean.getEntryTraits());
				new_event.getExitTraits().addAll(evbean.getExitTraits());
				new_event.getGroups().addAll(evbean.getGroups());
				new_event.getInfluencers().addAll(evbean.getInfluencers());
				new_event.getObservers().addAll(evbean.getObservers());
				new_event.getOrchestrators().addAll(evbean.getOrchestrators());
				new_event.getThings().addAll(evbean.getThings());
				new_event.setDescription(evbean.getDescription());
				new_event.setEventType(evbean.getEventType());
				new_event.setEndDate(evbean.getEndDate());
				new_event.setLocation(evbean.getLocation());
				new_event.setStartDate(evbean.getStartDate());
				out_obj = (T)new_event;
				break;
			case LOCATION:
				LocationType locbean = (LocationType)in_obj;
				LocationType new_location = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(owner, locbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(locbean, new_location);
				new_location.getBorders().addAll(locbean.getBorders());
				new_location.getBoundaries().addAll(locbean.getBoundaries());
				new_location.setGeographyType(locbean.getGeographyType());
				new_location.setClassification(locbean.getClassification());
				new_location.setDescription(locbean.getDescription());
				out_obj = (T)new_location;
				break;
			case TRAIT:
				TraitType trbean = (TraitType)in_obj;
				TraitType new_trait = ((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).newTrait(owner, trbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(trbean, new_trait);
				new_trait.setAlignmentType(trbean.getAlignmentType());
				new_trait.setDescription(trbean.getDescription());
				new_trait.setTraitType(trbean.getTraitType());
				out_obj = (T)new_trait;
				break;
			case ESTIMATE:
				EstimateType etbean = (EstimateType)in_obj;
				EstimateType new_estimate = ((EstimateFactory)Factories.getFactory(FactoryEnumType.ESTIMATE)).newEstimate(owner, etbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(etbean, new_estimate);
				new_estimate.setCost(etbean.getCost());
				new_estimate.setDescription(etbean.getDescription());
				new_estimate.setEstimateType(etbean.getEstimateType());
				new_estimate.setTime(etbean.getTime());
				out_obj = (T)new_estimate;
				break;
			case MODEL:
				ModelType mobean = (ModelType)in_obj;
				ModelType new_Model = ((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).newModel(owner, mobean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(mobean, new_Model);
				new_Model.setModelType(mobean.getModelType());
				new_Model.setDescription(mobean.getDescription());
				new_Model.getArtifacts().addAll(mobean.getArtifacts());
				new_Model.getCases().addAll(mobean.getCases());
				new_Model.getDependencies().addAll(mobean.getDependencies());
				new_Model.getModels().addAll(mobean.getModels());
				new_Model.getRequirements().addAll(mobean.getRequirements());
				out_obj = (T)new_Model;
				break;
			case ARTIFACT:
				ArtifactType arbean = (ArtifactType)in_obj;
				ArtifactType new_Artifact = ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).newArtifact(owner, arbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(arbean, new_Artifact);
				new_Artifact.setArtifactType(arbean.getArtifactType());
				new_Artifact.setDescription(arbean.getDescription());
				new_Artifact.setPreviousTransitionId(arbean.getPreviousTransitionId());
				new_Artifact.setNextTransitionId(arbean.getNextTransitionId());
				new_Artifact.setArtifactDataId(arbean.getArtifactDataId());
				//new_Artifact.setCreatedDate(arbean.getCreatedDate());
				//logger.error(arbean.getArtifactType() + " " + arbean.getDescription() + " " + arbean.getPreviousTransitionId() + " " + arbean.getNextTransitionId() + " " + arbean.getArtifactDataId() + " " + arbean.getCreatedDate());
				out_obj = (T)new_Artifact;
				break;			
			case REQUIREMENT:
				RequirementType rqbean = (RequirementType)in_obj;
				RequirementType new_Requirement = ((RequirementFactory)Factories.getFactory(FactoryEnumType.REQUIREMENT)).newRequirement(owner, rqbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(rqbean, new_Requirement);
				new_Requirement.setRequirementType(rqbean.getRequirementType());
				new_Requirement.setRequirementStatus(rqbean.getRequirementStatus());
				new_Requirement.setDescription(rqbean.getDescription());
				new_Requirement.setLogicalOrder(rqbean.getLogicalOrder());
				new_Requirement.setPriority(rqbean.getPriority());
				new_Requirement.setRequirementId(rqbean.getRequirementId());
				new_Requirement.setNote(rqbean.getNote());
				new_Requirement.setForm(rqbean.getForm());
				out_obj = (T)new_Requirement;
				break;
			case CASE:
				CaseType csbean = (CaseType)in_obj;
				CaseType new_Case = ((CaseFactory)Factories.getFactory(FactoryEnumType.CASE)).newCase(owner, csbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(csbean, new_Case);
				new_Case.getActors().addAll(csbean.getActors());
				new_Case.getDiagrams().addAll(csbean.getDiagrams());
				new_Case.getSequence().addAll(csbean.getSequence());
				new_Case.getPrerequisites().addAll(csbean.getPrerequisites());
				new_Case.setCaseType(csbean.getCaseType());
				new_Case.setDescription(csbean.getDescription());
				out_obj = (T)new_Case;
				break;
			case TIME:
				TimeType tbean = (TimeType)in_obj;
				TimeType new_time = ((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).newTime(owner, tbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(tbean, new_time);
				new_time.setBasisType(tbean.getBasisType());
				new_time.setValue(tbean.getValue());
				out_obj = (T)new_time;
				break;
			case GOAL:
				GoalType gbean = (GoalType)in_obj;
				GoalType new_goal = ((GoalFactory)Factories.getFactory(FactoryEnumType.GOAL)).newGoal(owner, gbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(gbean, new_goal);
				new_goal.setDescription(gbean.getDescription());
				new_goal.setGoalType(gbean.getGoalType());
				new_goal.setLogicalOrder(gbean.getLogicalOrder());
				new_goal.setPriority(gbean.getPriority());
				new_goal.setBudget(gbean.getBudget());
				new_goal.setAssigned(gbean.getAssigned());
				new_goal.setSchedule(gbean.getSchedule());
				new_goal.getCases().addAll(gbean.getCases());
				new_goal.getDependencies().addAll(gbean.getDependencies());
				new_goal.getRequirements().addAll(gbean.getRequirements());
				out_obj = (T)new_goal;
				break;
			case SCHEDULE:
				ScheduleType sbean = (ScheduleType)in_obj;
				ScheduleType new_sched = ((ScheduleFactory)Factories.getFactory(FactoryEnumType.SCHEDULE)).newSchedule(owner, sbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(sbean, new_sched);
				new_sched.setStartTime(sbean.getStartTime());
				new_sched.setEndTime(sbean.getEndTime());
				new_sched.getBudgets().addAll(sbean.getBudgets());
				new_sched.getGoals().addAll(sbean.getGoals());
				out_obj = (T)new_sched;
				break;
			case COST:
				CostType cbean = (CostType)in_obj;
				CostType new_cost = ((CostFactory)Factories.getFactory(FactoryEnumType.COST)).newCost(owner, cbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(cbean, new_cost);
				new_cost.setCurrencyType(cbean.getCurrencyType());
				new_cost.setValue(cbean.getValue());
				out_obj = (T)new_cost;
				break;
			case BUDGET:
				BudgetType bbean = (BudgetType)in_obj;
				BudgetType new_bud = ((BudgetFactory)Factories.getFactory(FactoryEnumType.BUDGET)).newBudget(owner, bbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(bbean, new_bud);
				new_bud.setBudgetType(bbean.getBudgetType());
				new_bud.setCost(bbean.getCost());
				new_bud.setTime(bbean.getTime());
				new_bud.setDescription(bbean.getDescription());
				out_obj = (T)new_bud;
				break;
			case LIFECYCLE:
				LifecycleType lbean = (LifecycleType)in_obj;
				LifecycleType newObj = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).newLifecycle(owner, lbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(lbean, newObj);
				newObj.setDescription(lbean.getDescription());
				newObj.getSchedules().addAll(lbean.getSchedules());
				newObj.getBudgets().addAll(lbean.getBudgets());
				newObj.getGoals().addAll(lbean.getGoals());
				newObj.getProjects().addAll(lbean.getProjects());
				out_obj = (T)newObj;
				break;
			case NOTE:
				NoteType nbean = (NoteType)in_obj;
				NoteType new_note = ((NoteFactory)Factories.getFactory(FactoryEnumType.NOTE)).newNote(owner, nbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(nbean, new_note);
				new_note.setText(nbean.getText());
				out_obj = (T)new_note;
				break;
			case RESOURCE:
				ResourceType rbean = (ResourceType)in_obj;
				ResourceType new_rec = ((ResourceFactory)Factories.getFactory(FactoryEnumType.RESOURCE)).newResource(owner, rbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(rbean, new_rec);
	
				new_rec.setDescription(rbean.getDescription());
				new_rec.setResourceType(rbean.getResourceType());
				new_rec.setResourceDataId(rbean.getResourceDataId());
				new_rec.setUtilization(rbean.getUtilization());
				new_rec.setEstimate(rbean.getEstimate());
				new_rec.setSchedule(rbean.getSchedule());
				out_obj = (T)new_rec;
				break;
			default:
				logger.error("Unhandled type: " + type.toString());
				break;
		}
		return out_obj;
	}
}
