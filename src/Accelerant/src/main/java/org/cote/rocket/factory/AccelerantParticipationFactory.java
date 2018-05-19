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
package org.cote.rocket.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.factory.BaseParticipationFactory;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.GroupParticipantType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.ParticipationEnumType;
import org.cote.propellant.objects.ActorParticipantType;
import org.cote.propellant.objects.ArtifactParticipantType;
import org.cote.propellant.objects.ArtifactType;
import org.cote.propellant.objects.BorderParticipantType;
import org.cote.propellant.objects.BoundaryParticipantType;
import org.cote.propellant.objects.BudgetParticipantType;
import org.cote.propellant.objects.BudgetType;
import org.cote.propellant.objects.CaseParticipantType;
import org.cote.propellant.objects.CaseType;
import org.cote.propellant.objects.CostParticipantType;
import org.cote.propellant.objects.CostType;
import org.cote.propellant.objects.EntryTraitParticipantType;
import org.cote.propellant.objects.EventParticipantType;
import org.cote.propellant.objects.ExitTraitParticipantType;
import org.cote.propellant.objects.FormElementParticipantType;
import org.cote.propellant.objects.FormElementType;
import org.cote.propellant.objects.FormElementValueParticipantType;
import org.cote.propellant.objects.FormElementValueType;
import org.cote.propellant.objects.FormParticipantType;
import org.cote.propellant.objects.FormType;
import org.cote.propellant.objects.GoalParticipantType;
import org.cote.propellant.objects.GoalType;
import org.cote.propellant.objects.InfluencerParticipantType;
import org.cote.propellant.objects.LocationParticipantType;
import org.cote.propellant.objects.LocationType;
import org.cote.propellant.objects.ModelParticipantType;
import org.cote.propellant.objects.ModelType;
import org.cote.propellant.objects.ModuleParticipantType;
import org.cote.propellant.objects.ModuleType;
import org.cote.propellant.objects.NoteParticipantType;
import org.cote.propellant.objects.NoteType;
import org.cote.propellant.objects.ObserverParticipantType;
import org.cote.propellant.objects.OrchestratorParticipantType;
import org.cote.propellant.objects.ProcessParticipantType;
import org.cote.propellant.objects.ProcessStepParticipantType;
import org.cote.propellant.objects.ProcessStepType;
import org.cote.propellant.objects.ProcessType;
import org.cote.propellant.objects.ProjectParticipantType;
import org.cote.propellant.objects.ProjectType;
import org.cote.propellant.objects.RequirementParticipantType;
import org.cote.propellant.objects.RequirementType;
import org.cote.propellant.objects.ResourceParticipantType;
import org.cote.propellant.objects.ResourceType;
import org.cote.propellant.objects.ScheduleParticipantType;
import org.cote.propellant.objects.ScheduleType;
import org.cote.propellant.objects.StageParticipantType;
import org.cote.propellant.objects.StageType;
import org.cote.propellant.objects.TaskParticipantType;
import org.cote.propellant.objects.TaskType;
import org.cote.propellant.objects.ThingParticipantType;
import org.cote.propellant.objects.TicketParticipantType;
import org.cote.propellant.objects.TicketType;
import org.cote.propellant.objects.TimeParticipantType;
import org.cote.propellant.objects.TimeType;
import org.cote.propellant.objects.TraitType;
import org.cote.propellant.objects.ValidationRuleParticipantType;
import org.cote.propellant.objects.ValidationRuleType;
import org.cote.propellant.objects.WorkParticipantType;
import org.cote.propellant.objects.WorkType;
import org.cote.rocket.Factories;

public abstract class AccelerantParticipationFactory extends BaseParticipationFactory {
	public static final Logger logger = LogManager.getLogger(AccelerantParticipationFactory.class);
	public AccelerantParticipationFactory(ParticipationEnumType type, String tableName){
		super(type, tableName);
	}
	

	public <T> List<T> getWorkParticipations(NameIdType cycle, ParticipantEnumType type) throws FactoryException, ArgumentException
	{
		return convertList(getParticipations(new NameIdType[]{cycle}, type));
	}	
	
	
	@Override
	protected BaseParticipantType newParticipant(ParticipantEnumType type) throws ArgumentException
	{
		BaseParticipantType new_participant = null;
		switch (type)
		{
			case BOUNDARY:
				new_participant = new BoundaryParticipantType();
				break;
			case BORDER:
				new_participant = new BorderParticipantType();
				break;
			case THING:
				new_participant = new ThingParticipantType();
				break;
			case ENTRYTRAIT:
				new_participant = new EntryTraitParticipantType();
				break;
			case EXITTRAIT:
				new_participant = new ExitTraitParticipantType();
				break;
			case ACTOR:
				new_participant = new ActorParticipantType();
				break;
			case OBSERVER:
				new_participant = new ObserverParticipantType();
				break;
			case INFLUENCER:
				new_participant = new InfluencerParticipantType();
				break;
			case ORCHESTRATOR:
				new_participant = new OrchestratorParticipantType();
				break;
			case SCHEDULE:
				new_participant = new ScheduleParticipantType();
				break;
			case BUDGET:
				new_participant = new BudgetParticipantType();
				break;
			case PROJECT:
				new_participant = new ProjectParticipantType();
				break;
			case GOAL:
				new_participant = new GoalParticipantType();
				break;
			case REQUIREMENT:
				new_participant = new RequirementParticipantType();
				break;
			case DEPENDENCY:
			case ARTIFACT:
				new_participant = new ArtifactParticipantType();
				break;
			case WORK:
				new_participant = new WorkParticipantType();
				break;
			case NOTE:
				new_participant = new NoteParticipantType();
				break;
			case TIME:
				new_participant = new TimeParticipantType();
				break;
			case COST:
				new_participant = new CostParticipantType();
				break;
			case RESOURCE:
				new_participant = new ResourceParticipantType();
				break;
			case CASE:
				new_participant = new CaseParticipantType();
				break;
			case MODEL:
				new_participant = new ModelParticipantType();
				break;
			case TASK:
				new_participant = new TaskParticipantType();
				break;
			case MODULE:
				new_participant = new ModuleParticipantType();
				break;
			case STAGE:
				new_participant = new StageParticipantType();
				break;
			case PROCESS:
				new_participant = new ProcessParticipantType();
				break;
			case PROCESSSTEP:
				new_participant = new ProcessStepParticipantType();
				break;
			case FORM:
				new_participant = new FormParticipantType();
				break;
			case FORMELEMENT:
				new_participant = new FormElementParticipantType();
				break;
			case FORMELEMENTVALUE:
				new_participant = new FormElementValueParticipantType();
				break;
			case TICKET:
				new_participant = new TicketParticipantType();
				break;
			case VALIDATIONRULE:
				new_participant = new ValidationRuleParticipantType();
				break;
			default:
				//throw new ArgumentException("Invalid participant type: '" + type.toString() + "'");
				new_participant = super.newParticipant(type);
				break;
				//new_participant = new BaseParticipantType();
				//break;
		}
		new_participant.setParticipantType(type);
		return new_participant;
	}
	public ValidationRuleParticipantType newValidationRuleParticipation(NameIdType cycle, ValidationRuleType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.VALIDATIONRULE);
	}
	public TicketParticipantType newTicketParticipation(NameIdType cycle, TicketType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.TICKET);
	}

	public BoundaryParticipantType newBoundaryParticipation(NameIdType cycle, LocationType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.BOUNDARY);
	}
	public BorderParticipantType newBorderParticipation(NameIdType cycle, LocationType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.BORDER);
	}
	public ActorParticipantType newActorParticipation(NameIdType cycle, PersonType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.ACTOR);
	}
	
	public ThingParticipantType newThingParticipation(NameIdType cycle, DataType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.THING);
	}

	public ExitTraitParticipantType newExitTraitParticipation(NameIdType cycle, TraitType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.EXITTRAIT);
	}

	public EntryTraitParticipantType newEntryTraitParticipation(NameIdType cycle, TraitType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.ENTRYTRAIT);
	}

	
	public ObserverParticipantType newObserverParticipation(NameIdType cycle, PersonType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.OBSERVER);
	}
	public InfluencerParticipantType newInfluencerParticipation(NameIdType cycle, PersonType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.INFLUENCER);
	}
	public OrchestratorParticipantType newOrchestratorParticipation(NameIdType cycle, PersonType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.ORCHESTRATOR);
	}

	
	
	public FormParticipantType newFormParticipation(NameIdType cycle, FormType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.FORM);
	}
	public FormElementParticipantType newFormElementParticipation(NameIdType cycle, FormElementType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.FORMELEMENT);
	}
	public FormElementValueParticipantType newFormElementValueParticipation(NameIdType cycle, FormElementValueType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.FORMELEMENTVALUE);
	}
	public StageParticipantType newStageParticipation(NameIdType cycle, StageType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.STAGE);
	}
	public ModuleParticipantType newModuleParticipation(NameIdType cycle, ModuleType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.MODULE);
	}
	public ProcessStepParticipantType newProcessStepParticipation(NameIdType cycle, ProcessStepType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.PROCESSSTEP);
	}
	public ProcessParticipantType newProcessParticipation(NameIdType cycle, ProcessType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.PROCESS);
	}
	public TaskParticipantType newTaskParticipation(NameIdType cycle, TaskType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.TASK);
	}	
	public ArtifactParticipantType newArtifactParticipation(NameIdType cycle, ArtifactType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.ARTIFACT);
	}
	public ArtifactParticipantType newDependencyParticipation(NameIdType cycle, ArtifactType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.DEPENDENCY);
	}
	public RequirementParticipantType newRequirementParticipation(NameIdType cycle, RequirementType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.REQUIREMENT);
	}
	public ResourceParticipantType newResourceParticipation(NameIdType cycle, ResourceType sched) throws ArgumentException ,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.RESOURCE);
	}

	public WorkParticipantType newWorkParticipation(NameIdType cycle, WorkType sched) throws ArgumentException ,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.WORK);
	}
	public NoteParticipantType newNoteParticipation(NameIdType cycle, NoteType sched) throws ArgumentException ,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.NOTE);
	}
	public TimeParticipantType newTimeParticipation(NameIdType cycle, TimeType sched) throws ArgumentException ,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.TIME);
	}
	public CostParticipantType newCostParticipation(NameIdType cycle, CostType sched) throws ArgumentException ,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.COST);
	}
	public ModelParticipantType newModelParticipation(NameIdType cycle, ModelType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.MODEL);
	}
	public CaseParticipantType newCaseParticipation(NameIdType cycle, CaseType sched) throws ArgumentException ,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.CASE);
	}
	public ScheduleParticipantType newScheduleParticipation(NameIdType cycle, ScheduleType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.SCHEDULE);
	}
	public BudgetParticipantType newBudgetParticipation(NameIdType cycle, BudgetType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.BUDGET);
	}
	public ProjectParticipantType newProjectParticipation(NameIdType cycle, ProjectType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.PROJECT);
	}
	public GoalParticipantType newGoalParticipation(NameIdType cycle, GoalType sched) throws ArgumentException ,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.GOAL);
	}
	public boolean deleteValidationRuleParticipant(NameIdType cycle, ValidationRuleType sched) throws FactoryException, ArgumentException
	{
		ValidationRuleParticipantType dp = getValidationRuleParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}	
	public boolean deleteValidationRuleParticipations(ValidationRuleType sched) throws FactoryException,ArgumentException
	{

		List<ValidationRuleParticipantType> dp = getValidationRuleParticipants(sched);
		return deleteParticipants(dp.toArray(new ValidationRuleParticipantType[0]), sched.getOrganizationId());
	}	
	public boolean deleteTicketParticipant(NameIdType cycle, TicketType sched) throws FactoryException, ArgumentException
	{
		TicketParticipantType dp = getTicketParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}	
	public boolean deleteTicketParticipations(TicketType sched) throws FactoryException,ArgumentException
	{

		List<TicketParticipantType> dp = getTicketParticipants(sched);
		return deleteParticipants(dp.toArray(new TicketParticipantType[0]), sched.getOrganizationId());
	}	

	public boolean deleteFormParticipant(NameIdType cycle, FormType sched) throws FactoryException, ArgumentException
	{
		FormParticipantType dp = getFormParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}	
	public boolean deleteFormParticipations(FormType sched) throws FactoryException,ArgumentException
	{

		List<FormParticipantType> dp = getFormParticipants(sched);
		return deleteParticipants(dp.toArray(new FormParticipantType[0]), sched.getOrganizationId());
	}
	public boolean deleteFormElementParticipant(NameIdType cycle, FormElementType sched) throws FactoryException, ArgumentException
	{
		FormElementParticipantType dp = getFormElementParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}	
	public boolean deleteFormElementParticipations(FormElementType sched) throws FactoryException,ArgumentException
	{

		List<FormElementParticipantType> dp = getFormElementParticipants(sched);
		return deleteParticipants(dp.toArray(new FormElementParticipantType[0]), sched.getOrganizationId());
	}
	public boolean deleteFormElementValueParticipant(NameIdType cycle, FormElementValueType sched) throws FactoryException, ArgumentException
	{
		FormElementValueParticipantType dp = getFormElementValueParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}	
	public boolean deleteFormElementValueParticipations(FormElementValueType sched) throws FactoryException,ArgumentException
	{

		List<FormElementValueParticipantType> dp = getFormElementValueParticipants(sched);
		return deleteParticipants(dp.toArray(new FormElementValueParticipantType[0]), sched.getOrganizationId());
	}
	
	
	public boolean deleteStageParticipant(NameIdType cycle, StageType sched) throws FactoryException, ArgumentException
	{
		StageParticipantType dp = getStageParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}	
	public boolean deleteStageParticipations(StageType sched) throws FactoryException,ArgumentException
	{

		List<StageParticipantType> dp = getStageParticipants(sched);
		return deleteParticipants(dp.toArray(new StageParticipantType[0]), sched.getOrganizationId());
	}
	public boolean deleteModuleParticipant(NameIdType cycle, ModuleType sched) throws FactoryException, ArgumentException
	{
		ModuleParticipantType dp = getModuleParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}	
	public boolean deleteModuleParticipations(ModuleType sched) throws FactoryException,ArgumentException
	{

		List<ModuleParticipantType> dp = getModuleParticipants(sched);
		return deleteParticipants(dp.toArray(new ModuleParticipantType[0]), sched.getOrganizationId());
	}
	public boolean deleteProcessStepParticipant(NameIdType cycle, ProcessStepType sched) throws FactoryException, ArgumentException
	{
		ProcessStepParticipantType dp = getProcessStepParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}	
	public boolean deleteProcessStepParticipations(ProcessStepType sched) throws FactoryException,ArgumentException
	{

		List<ProcessStepParticipantType> dp = getProcessStepParticipants(sched);
		return deleteParticipants(dp.toArray(new ProcessStepParticipantType[0]), sched.getOrganizationId());
	}
	public boolean deleteProcessParticipant(NameIdType cycle, ProcessType sched) throws FactoryException, ArgumentException
	{
		ProcessParticipantType dp = getProcessParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}	
	public boolean deleteProcessParticipations(ProcessType sched) throws FactoryException,ArgumentException
	{

		List<ProcessParticipantType> dp = getProcessParticipants(sched);
		return deleteParticipants(dp.toArray(new ProcessParticipantType[0]), sched.getOrganizationId());
	}
	public boolean deleteTaskParticipant(NameIdType cycle, TaskType sched) throws FactoryException, ArgumentException
	{
		TaskParticipantType dp = getTaskParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}	
	public boolean deleteTaskParticipations(TaskType sched) throws FactoryException,ArgumentException
	{

		List<TaskParticipantType> dp = getTaskParticipants(sched);
		return deleteParticipants(dp.toArray(new TaskParticipantType[0]), sched.getOrganizationId());
	}
	public boolean deleteScheduleParticipant(NameIdType cycle, ScheduleType sched) throws FactoryException, ArgumentException
	{
		ScheduleParticipantType dp = getScheduleParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deleteScheduleParticipations(ScheduleType sched) throws FactoryException,ArgumentException
	{

		List<ScheduleParticipantType> dp = getScheduleParticipants(sched);
		return deleteParticipants(dp.toArray(new ScheduleParticipantType[0]), sched.getOrganizationId());
	}
	public boolean deleteBudgetParticipant(NameIdType cycle, BudgetType sched) throws FactoryException, ArgumentException
	{
		BudgetParticipantType dp = getBudgetParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deleteBudgetParticipations(BudgetType sched) throws FactoryException,ArgumentException
	{

		List<BudgetParticipantType> dp = getBudgetParticipants(sched);
		return deleteParticipants(dp.toArray(new BudgetParticipantType[0]), sched.getOrganizationId());
	}
	public boolean deleteProjectParticipant(NameIdType cycle, ProjectType sched) throws FactoryException, ArgumentException
	{
		ProjectParticipantType dp = getProjectParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deleteProjectParticipations(ProjectType sched) throws FactoryException,ArgumentException
	{

		List<ProjectParticipantType> dp = getProjectParticipants(sched);
		return deleteParticipants(dp.toArray(new ProjectParticipantType[0]), sched.getOrganizationId());
	}
	public boolean deleteGoalParticipant(NameIdType cycle, GoalType sched) throws FactoryException, ArgumentException
	{
		GoalParticipantType dp = getGoalParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deleteGoalParticipations(GoalType sched) throws FactoryException,ArgumentException
	{

		List<GoalParticipantType> dp = getGoalParticipants(sched);
		return deleteParticipants(dp.toArray(new GoalParticipantType[0]), sched.getOrganizationId());
	}
	
	public boolean deleteModelParticipant(NameIdType cycle, ModelType sched) throws FactoryException, ArgumentException
	{
		ModelParticipantType dp = getModelParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deleteModelParticipations(ModelType sched) throws FactoryException,ArgumentException
	{

		List<ModelParticipantType> dp = getModelParticipants(sched);
		return deleteParticipants(dp.toArray(new ModelParticipantType[0]), sched.getOrganizationId());
	}
	public boolean deleteCaseParticipant(NameIdType cycle, CaseType sched) throws FactoryException, ArgumentException
	{
		CaseParticipantType dp = getCaseParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deleteCaseParticipations(CaseType sched) throws FactoryException,ArgumentException
	{

		List<CaseParticipantType> dp = getCaseParticipants(sched);
		return deleteParticipants(dp.toArray(new CaseParticipantType[0]), sched.getOrganizationId());
	}
	public boolean deleteNoteParticipant(NameIdType cycle, NoteType sched) throws FactoryException, ArgumentException
	{
		NoteParticipantType dp = getNoteParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deleteNoteParticipations(NoteType sched) throws FactoryException,ArgumentException
	{

		List<NoteParticipantType> dp = getNoteParticipants(sched);
		return deleteParticipants(dp.toArray(new NoteParticipantType[0]), sched.getOrganizationId());
	}
	public boolean deleteTimeParticipant(NameIdType cycle, TimeType sched) throws FactoryException, ArgumentException
	{
		TimeParticipantType dp = getTimeParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deleteTimeParticipations(TimeType sched) throws FactoryException,ArgumentException
	{

		List<TimeParticipantType> dp = getTimeParticipants(sched);
		return deleteParticipants(dp.toArray(new TimeParticipantType[0]), sched.getOrganizationId());
	}
	public boolean deleteCostParticipant(NameIdType cycle, CostType sched) throws FactoryException, ArgumentException
	{
		CostParticipantType dp = getCostParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deleteCostParticipations(CostType sched) throws FactoryException,ArgumentException
	{

		List<CostParticipantType> dp = getCostParticipants(sched);
		return deleteParticipants(dp.toArray(new CostParticipantType[0]), sched.getOrganizationId());
	}
	
	
	
	public boolean deleteArtifactParticipant(NameIdType cycle, ArtifactType sched) throws FactoryException, ArgumentException
	{
		ArtifactParticipantType dp = getArtifactParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deleteArtifactParticipations(ArtifactType sched) throws FactoryException,ArgumentException
	{

		List<ArtifactParticipantType> dp = getArtifactParticipants(sched);
		return deleteParticipants(dp.toArray(new ArtifactParticipantType[0]), sched.getOrganizationId());
	}
	public boolean deleteDependencyParticipant(NameIdType cycle, ArtifactType sched) throws FactoryException, ArgumentException
	{
		ArtifactParticipantType dp = getDependencyParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deleteDependencyParticipations(ArtifactType sched) throws FactoryException,ArgumentException
	{

		List<ArtifactParticipantType> dp = getDependencyParticipants(sched);
		return deleteParticipants(dp.toArray(new ArtifactParticipantType[0]), sched.getOrganizationId());
	}
	public boolean deleteRequirementParticipant(NameIdType cycle, RequirementType sched) throws FactoryException, ArgumentException
	{
		RequirementParticipantType dp = getRequirementParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deleteRequirementParticipations(RequirementType sched) throws FactoryException,ArgumentException
	{

		List<RequirementParticipantType> dp = getRequirementParticipants(sched);
		return deleteParticipants(dp.toArray(new RequirementParticipantType[0]), sched.getOrganizationId());
	}
	public boolean deleteResourceParticipant(NameIdType cycle, ResourceType sched) throws FactoryException, ArgumentException
	{
		ResourceParticipantType dp = getResourceParticipant(cycle, sched);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deleteResourceParticipations(ResourceType sched) throws FactoryException,ArgumentException
	{

		List<ResourceParticipantType> dp = getResourceParticipants(sched);
		return deleteParticipants(dp.toArray(new ResourceParticipantType[0]), sched.getOrganizationId());
	}
	public boolean deleteWorkParticipant(NameIdType src, WorkType work) throws FactoryException, ArgumentException
	{
		WorkParticipantType dp = getWorkParticipant(src, work);
		if (dp == null) return true;
		return delete(dp);

	}
	public boolean deleteWorkParticipations(WorkType work) throws FactoryException,ArgumentException
	{

		List<WorkParticipantType> dp = getWorkParticipants(work);
		return deleteParticipants(dp.toArray(new WorkParticipantType[0]), work.getOrganizationId());
	}
	public List<ValidationRuleType> getValidationRulesFromParticipations(ValidationRuleParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((ValidationRuleFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULE)).getValidationRuleList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}	
	public List<TicketType> getTicketsFromParticipations(TicketParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((TicketFactory)Factories.getFactory(FactoryEnumType.TICKET)).getTicketList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<PersonType> getPersonsFromParticipations(EventParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getPersonList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<TraitType> getTraitsFromParticipations(EventParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).getTraitList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<BaseGroupType> getGroupsFromParticipations(GroupParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<DataType> getDatasFromParticipations(EventParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataList(new QueryField[]{ field }, true, startRecord, recordCount, organizationId);
	}

	public List<LocationType> getLocationsFromParticipations(LocationParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).getLocationList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}


	public List<FormType> getFormsFromParticipations(FormParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).getFormList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<FormElementType> getFormElementsFromParticipations(FormElementParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).getFormElementList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<FormElementValueType> getFormElementValuesFromParticipations(FormElementValueParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).getFormElementValueList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	
	
	public List<StageType> getStagesFromParticipations(StageParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((StageFactory)Factories.getFactory(FactoryEnumType.STAGE)).getStageList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<ModuleType> getModulesFromParticipations(ModuleParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((ModuleFactory)Factories.getFactory(FactoryEnumType.MODULE)).getModuleList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<ProcessStepType> getProcessStepsFromParticipations(ProcessStepParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).getProcessStepList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<ProcessType> getProcessesFromParticipations(ProcessParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((ProcessFactory)Factories.getFactory(FactoryEnumType.PROCESS)).getProcessList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<TaskType> getTasksFromParticipations(TaskParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).getTaskList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<GoalType> getGoalsFromParticipations(GoalParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((GoalFactory)Factories.getFactory(FactoryEnumType.GOAL)).getGoalList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<ProjectType> getProjectsFromParticipations(ProjectParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).getProjectList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<BudgetType> getBudgetsFromParticipations(BudgetParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((BudgetFactory)Factories.getFactory(FactoryEnumType.BUDGET)).getBudgetList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<ScheduleType> getSchedulesFromParticipations(ScheduleParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((ScheduleFactory)Factories.getFactory(FactoryEnumType.SCHEDULE)).getScheduleList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	
	public List<CaseType> getCasesFromParticipations(CaseParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((CaseFactory)Factories.getFactory(FactoryEnumType.CASE)).getCaseList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<ModelType> getModelsFromParticipations(ModelParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).getModelList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	
	public List<WorkType> getWorkFromParticipations(WorkParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((WorkFactory)Factories.getFactory(FactoryEnumType.WORK)).getWorkList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<NoteType> getNotesFromParticipations(NoteParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((NoteFactory)Factories.getFactory(FactoryEnumType.NOTE)).getNoteList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<TimeType> getTimesFromParticipations(TimeParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getTimeList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<CostType> getCostsFromParticipations(CostParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getCostList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	
	public List<ResourceType> getResourcesFromParticipations(ResourceParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((ResourceFactory)Factories.getFactory(FactoryEnumType.RESOURCE)).getResourceList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<RequirementType> getRequirementsFromParticipations(RequirementParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((RequirementFactory)Factories.getFactory(FactoryEnumType.REQUIREMENT)).getRequirementList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<ArtifactType> getDependenciesFromParticipations(ArtifactParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).getArtifactList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	public List<ArtifactType> getArtifactsFromParticipations(ArtifactParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).getArtifactList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}
	
	public List<ArtifactType> getDependenciesFromParticipation(NameIdType participation) throws ArgumentException{
		List<ArtifactType> items = new ArrayList<ArtifactType>();
		try{
			ArtifactParticipantType[] parts = getDependencyParticipations(participation).toArray(new ArtifactParticipantType[0]);
			if(parts.length > 0){
				items = getDependenciesFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}	
	public List<ArtifactType> getArtifactsFromParticipation(NameIdType participation) throws ArgumentException{
		List<ArtifactType> items = new ArrayList<ArtifactType>();
		try{
			ArtifactParticipantType[] parts = getArtifactParticipations(participation).toArray(new ArtifactParticipantType[0]);
			if(parts.length > 0){
				items = getArtifactsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<ValidationRuleType> getValidationRulesFromParticipation(NameIdType participation) throws ArgumentException{
		List<ValidationRuleType> items = new ArrayList<ValidationRuleType>();
		try{
			ValidationRuleParticipantType[] parts = getValidationRuleParticipations(participation).toArray(new ValidationRuleParticipantType[0]);
			if(parts.length > 0){
				items = getValidationRulesFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<TicketType> getTicketsFromParticipation(NameIdType participation) throws ArgumentException{
		List<TicketType> items = new ArrayList<TicketType>();
		try{
			TicketParticipantType[] parts = getTicketParticipations(participation).toArray(new TicketParticipantType[0]);
			if(parts.length > 0){
				items = getTicketsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}

	public List<DataType> getThingsFromParticipation(NameIdType participation) throws ArgumentException{
		List<DataType> items = new ArrayList<>();
		try{
			ThingParticipantType[] parts = getThingParticipations(participation).toArray(new ThingParticipantType[0]);
			if(parts.length > 0){
				items = getDatasFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	
	public List<TraitType> getEntryTraitsFromParticipation(NameIdType participation) throws ArgumentException{
		List<TraitType> items = new ArrayList<TraitType>();
		try{
			EntryTraitParticipantType[] parts = getEntryTraitParticipations(participation).toArray(new EntryTraitParticipantType[0]);
			if(parts.length > 0){
				items = getTraitsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<TraitType> getExitTraitsFromParticipation(NameIdType participation) throws ArgumentException{
		List<TraitType> items = new ArrayList<TraitType>();
		try{
			ExitTraitParticipantType[] parts = getExitTraitParticipations(participation).toArray(new ExitTraitParticipantType[0]);
			if(parts.length > 0){
				items = getTraitsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<BaseGroupType> getGroupsFromParticipation(NameIdType participation) throws ArgumentException{
		List<BaseGroupType> items = new ArrayList<BaseGroupType>();
		try{
			GroupParticipantType[] parts = getGroupParticipations(participation).toArray(new GroupParticipantType[0]);
			if(parts.length > 0){
				items = getGroupsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<PersonType> getActorsFromParticipation(NameIdType participation) throws ArgumentException{
		List<PersonType> items = new ArrayList<PersonType>();
		try{
			ActorParticipantType[] parts = getActorParticipations(participation).toArray(new ActorParticipantType[0]);
			if(parts.length > 0){
				items = getPersonsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<PersonType> getObserversFromParticipation(NameIdType participation) throws ArgumentException{
		List<PersonType> items = new ArrayList<PersonType>();
		try{
			ObserverParticipantType[] parts = getObserverParticipations(participation).toArray(new ObserverParticipantType[0]);
			if(parts.length > 0){
				items = getPersonsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<PersonType> getInfluencersFromParticipation(NameIdType participation) throws ArgumentException{
		List<PersonType> items = new ArrayList<PersonType>();
		try{
			InfluencerParticipantType[] parts = getInfluencerParticipations(participation).toArray(new InfluencerParticipantType[0]);
			if(parts.length > 0){
				items = getPersonsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<PersonType> getOrchestratorsFromParticipation(NameIdType participation) throws ArgumentException{
		List<PersonType> items = new ArrayList<PersonType>();
		try{
			OrchestratorParticipantType[] parts = getOrchestratorParticipations(participation).toArray(new OrchestratorParticipantType[0]);
			if(parts.length > 0){
				items = getPersonsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}

	
	public List<LocationType> getBordersFromParticipation(NameIdType participation) throws ArgumentException{
		List<LocationType> items = new ArrayList<LocationType>();
		try{
			BorderParticipantType[] parts = getBorderParticipations(participation).toArray(new BorderParticipantType[0]);
			if(parts.length > 0){
				items = getLocationsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<LocationType> getBoundariesFromParticipation(NameIdType participation) throws ArgumentException{
		List<LocationType> items = new ArrayList<LocationType>();
		try{
			BoundaryParticipantType[] parts = getBoundaryParticipations(participation).toArray(new BoundaryParticipantType[0]);
			if(parts.length > 0){
				items = getLocationsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}

	public List<FormType> getFormsFromParticipation(NameIdType participation) throws ArgumentException{
		List<FormType> items = new ArrayList<FormType>();
		try{
			FormParticipantType[] parts = getFormParticipations(participation).toArray(new FormParticipantType[0]);
			if(parts.length > 0){
				items = getFormsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<FormElementType> getFormElementsFromParticipation(NameIdType participation) throws ArgumentException{
		List<FormElementType> items = new ArrayList<FormElementType>();
		try{
			FormElementParticipantType[] parts = getFormElementParticipations(participation).toArray(new FormElementParticipantType[0]);
			if(parts.length > 0){
				items = getFormElementsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<FormElementValueType> getFormElementValuesFromParticipation(NameIdType participation) throws ArgumentException{
		List<FormElementValueType> items = new ArrayList<FormElementValueType>();
		try{
			FormElementValueParticipantType[] parts = getFormElementValueParticipations(participation).toArray(new FormElementValueParticipantType[0]);
			if(parts.length > 0){
				items = getFormElementValuesFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	
	public List<StageType> getStagesFromParticipation(NameIdType participation) throws ArgumentException{
		List<StageType> items = new ArrayList<StageType>();
		try{
			StageParticipantType[] parts = getStageParticipations(participation).toArray(new StageParticipantType[0]);
			if(parts.length > 0){
				items = getStagesFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<ModuleType> getModulesFromParticipation(NameIdType participation) throws ArgumentException{
		List<ModuleType> items = new ArrayList<ModuleType>();
		try{
			ModuleParticipantType[] parts = getModuleParticipations(participation).toArray(new ModuleParticipantType[0]);
			if(parts.length > 0){
				items = getModulesFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<RequirementType> getRequirementsFromParticipation(NameIdType participation) throws ArgumentException{
		List<RequirementType> items = new ArrayList<RequirementType>();
		try{
			RequirementParticipantType[] parts = getRequirementParticipations(participation).toArray(new RequirementParticipantType[0]);
			if(parts.length > 0){
				items = getRequirementsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<ProcessStepType> getProcessStepsFromParticipation(NameIdType participation) throws ArgumentException{
		List<ProcessStepType> items = new ArrayList<ProcessStepType>();
		try{
			ProcessStepParticipantType[] parts = getProcessStepParticipations(participation).toArray(new ProcessStepParticipantType[0]);
			if(parts.length > 0){
				items = getProcessStepsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<ProcessType> getProcessesFromParticipation(NameIdType participation) throws ArgumentException{
		List<ProcessType> items = new ArrayList<ProcessType>();
		try{
			ProcessParticipantType[] parts = getProcessParticipations(participation).toArray(new ProcessParticipantType[0]);
			if(parts.length > 0){
				items = getProcessesFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<WorkType> getWorkFromParticipation(NameIdType participation) throws ArgumentException{
		List<WorkType> items = new ArrayList<WorkType>();
		try{
			WorkParticipantType[] parts = getWorkParticipations(participation).toArray(new WorkParticipantType[0]);
			if(parts.length > 0){
				items = getWorkFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<NoteType> getNotesFromParticipation(NameIdType participation) throws ArgumentException{
		List<NoteType> items = new ArrayList<NoteType>();
		try{
			NoteParticipantType[] parts = getNoteParticipations(participation).toArray(new NoteParticipantType[0]);
			if(parts.length > 0){
				items = getNotesFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<TaskType> getTasksFromParticipation(NameIdType participation) throws ArgumentException{
		List<TaskType> items = new ArrayList<TaskType>();
		try{
			TaskParticipantType[] parts = getTaskParticipations(participation).toArray(new TaskParticipantType[0]);
			if(parts.length > 0){
				items = getTasksFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<TimeType> getTimesFromParticipation(NameIdType participation) throws ArgumentException{
		List<TimeType> items = new ArrayList<TimeType>();
		try{
			TimeParticipantType[] parts = getTimeParticipations(participation).toArray(new TimeParticipantType[0]);
			if(parts.length > 0){
				items = getTimesFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<CostType> getCostsFromParticipation(NameIdType participation) throws ArgumentException{
		List<CostType> items = new ArrayList<CostType>();
		try{
			CostParticipantType[] parts = getCostParticipations(participation).toArray(new CostParticipantType[0]);
			if(parts.length > 0){
				items = getCostsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<CaseType> getCasesFromParticipation(NameIdType participation) throws ArgumentException{
		List<CaseType> items = new ArrayList<CaseType>();
		try{
			CaseParticipantType[] parts = getCaseParticipations(participation).toArray(new CaseParticipantType[0]);
			if(parts.length > 0){
				items = getCasesFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<ModelType> getModelsFromParticipation(NameIdType participation) throws ArgumentException{
		List<ModelType> items = new ArrayList<ModelType>();
		try{
			ModelParticipantType[] parts = getModelParticipations(participation).toArray(new ModelParticipantType[0]);
			if(parts.length > 0){
				items = getModelsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<ResourceType> getResourcesFromParticipation(NameIdType participation) throws ArgumentException{
		List<ResourceType> items = new ArrayList<ResourceType>();
		try{
			ResourceParticipantType[] parts = getResourceParticipations(participation).toArray(new ResourceParticipantType[0]);
			if(parts.length > 0){
				items = getResourcesFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<ScheduleType> getSchedulesFromParticipation(NameIdType participation) throws ArgumentException{
		List<ScheduleType> items = new ArrayList<ScheduleType>();
		try{
			ScheduleParticipantType[] parts = getScheduleParticipations(participation).toArray(new ScheduleParticipantType[0]);
			if(parts.length > 0){
				items = getSchedulesFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<BudgetType> getBudgetsFromParticipation(NameIdType participation) throws ArgumentException{
		List<BudgetType> items = new ArrayList<BudgetType>();
		try{
			BudgetParticipantType[] parts = getBudgetParticipations(participation).toArray(new BudgetParticipantType[0]);
			if(parts.length > 0){
				items = getBudgetsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<GoalType> getGoalsFromParticipation(NameIdType participation) throws ArgumentException{
		List<GoalType> items = new ArrayList<GoalType>();
		try{
			GoalParticipantType[] parts = getGoalParticipations(participation).toArray(new GoalParticipantType[0]);
			if(parts.length > 0){
				items = getGoalsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<ProjectType> getProjectsFromParticipation(NameIdType participation) throws ArgumentException{
		List<ProjectType> items = new ArrayList<ProjectType>();
		try{
			ProjectParticipantType[] parts = getProjectParticipations(participation).toArray(new ProjectParticipantType[0]);
			if(parts.length > 0){
				items = getProjectsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<ValidationRuleParticipantType> getValidationRuleParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.VALIDATIONRULE));
	}
	public List<TicketParticipantType> getTicketParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.TICKET));
	}
	public List<BoundaryParticipantType> getBoundaryParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.BOUNDARY));
	}

	public List<BorderParticipantType> getBorderParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.BORDER));
	}

	public List<ActorParticipantType> getActorParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.ACTOR));
	}
	public List<GroupParticipantType> getGroupParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.GROUP));
	}

	public List<ThingParticipantType> getThingParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.THING));
	}
	public List<ExitTraitParticipantType> getExitTraitParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.EXITTRAIT));
	}
	public List<EntryTraitParticipantType> getEntryTraitParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.ENTRYTRAIT));
	}

	
	public List<ObserverParticipantType> getObserverParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.OBSERVER));
	}

	public List<InfluencerParticipantType> getInfluencerParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.INFLUENCER));
	}

	public List<OrchestratorParticipantType> getOrchestratorParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.ORCHESTRATOR));
	}

	
	public List<FormParticipantType> getFormParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.FORM));
	}
	public List<FormElementParticipantType> getFormElementParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.FORMELEMENT));
	}
	public List<FormElementValueParticipantType> getFormElementValueParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.FORMELEMENTVALUE));
	}
	
	public List<StageParticipantType> getStageParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.STAGE));
	}
	public List<ModuleParticipantType> getModuleParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.MODULE));
	}
	public List<ProcessStepParticipantType> getProcessStepParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.PROCESSSTEP));
	}
	public List<ProcessParticipantType> getProcessParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.PROCESS));
	}
	public List<TaskParticipantType> getTaskParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.TASK));
	}
	public List<ScheduleParticipantType> getScheduleParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.SCHEDULE));
	}
	public List<BudgetParticipantType> getBudgetParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.BUDGET));
	}
	public List<ProjectParticipantType> getProjectParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.PROJECT));
	}
	public List<GoalParticipantType> getGoalParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.GOAL));
	}
	public List<CaseParticipantType> getCaseParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.CASE));
	}
	public List<ModelParticipantType> getModelParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.MODEL));
	}
	
	public List<WorkParticipantType> getWorkParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.WORK));
	}
	public List<NoteParticipantType> getNoteParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.NOTE));
	}
	public List<TimeParticipantType> getTimeParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.TIME));
	}
	public List<CostParticipantType> getCostParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.COST));
	}
	public List<ArtifactParticipantType> getArtifactParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.ARTIFACT));
	}
	public List<ArtifactParticipantType> getDependencyParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.DEPENDENCY));
	}
	public List<RequirementParticipantType> getRequirementParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.REQUIREMENT));
	}
	public List<ResourceParticipantType> getResourceParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.RESOURCE));
	}
	public List<ValidationRuleParticipantType> getValidationRuleParticipants(ValidationRuleType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.VALIDATIONRULE), sched.getOrganizationId()));
	}	
	public List<TicketParticipantType> getTicketParticipants(TicketType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.TICKET), sched.getOrganizationId()));
	}
	public List<FormParticipantType> getFormParticipants(FormType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.FORM), sched.getOrganizationId()));
	}
	public List<FormElementParticipantType> getFormElementParticipants(FormElementType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.FORMELEMENT), sched.getOrganizationId()));
	}
	public List<FormElementValueParticipantType> getFormElementValueParticipants(FormElementValueType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.FORMELEMENTVALUE), sched.getOrganizationId()));
	}
	public List<StageParticipantType> getStageParticipants(StageType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.STAGE), sched.getOrganizationId()));
	}
	public List<ModuleParticipantType> getModuleParticipants(ModuleType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.MODULE), sched.getOrganizationId()));
	}
	public List<ProcessStepParticipantType> getProcessStepParticipants(ProcessStepType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.PROCESSSTEP), sched.getOrganizationId()));
	}
	public List<ProcessParticipantType> getProcessParticipants(ProcessType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.PROCESS), sched.getOrganizationId()));
	}
	public List<TaskParticipantType> getTaskParticipants(TaskType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.TASK), sched.getOrganizationId()));
	}	
	public List<ScheduleParticipantType> getScheduleParticipants(ScheduleType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.SCHEDULE), sched.getOrganizationId()));
	}
	
	public List<BudgetParticipantType> getBudgetParticipants(BudgetType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.BUDGET), sched.getOrganizationId()));
	}
	public List<ProjectParticipantType> getProjectParticipants(ProjectType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.PROJECT), sched.getOrganizationId()));
	}
	public List<GoalParticipantType> getGoalParticipants(GoalType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.GOAL), sched.getOrganizationId()));
	}
	public List<ModelParticipantType> getModelParticipants(ModelType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.MODEL), sched.getOrganizationId()));
	}
	public List<CaseParticipantType> getCaseParticipants(CaseType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.CASE), sched.getOrganizationId()));
	}
	public List<WorkParticipantType> getWorkParticipants(WorkType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.WORK), sched.getOrganizationId()));
	}
	public List<NoteParticipantType> getNoteParticipants(NoteType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.NOTE), sched.getOrganizationId()));
	}
	public List<TimeParticipantType> getTimeParticipants(TimeType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.TIME), sched.getOrganizationId()));
	}
	public List<CostParticipantType> getCostParticipants(CostType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.COST), sched.getOrganizationId()));
	}
	public List<ArtifactParticipantType> getArtifactParticipants(ArtifactType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.ARTIFACT), sched.getOrganizationId()));
	}	
	public List<ArtifactParticipantType> getDependencyParticipants(ArtifactType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.DEPENDENCY), sched.getOrganizationId()));
	}
	public List<RequirementParticipantType> getRequirementParticipants(RequirementType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.REQUIREMENT), sched.getOrganizationId()));
	}
	public List<ResourceParticipantType> getResourceParticipants(ResourceType sched) throws FactoryException, ArgumentException
	{
		return convertList(getByField(QueryFields.getFieldParticipantMatch(sched,ParticipantEnumType.RESOURCE), sched.getOrganizationId()));
	}

	public ValidationRuleParticipantType getValidationRuleParticipant(NameIdType cycle, ValidationRuleType sched) throws FactoryException, ArgumentException
	{

		return getParticipant(cycle, sched, ParticipantEnumType.VALIDATIONRULE);
	}
	public TicketParticipantType getTicketParticipant(NameIdType cycle, TicketType sched) throws FactoryException, ArgumentException
	{

		return getParticipant(cycle, sched, ParticipantEnumType.TICKET);
	}
	public FormParticipantType getFormParticipant(NameIdType cycle, FormType sched) throws FactoryException, ArgumentException
	{

		return getParticipant(cycle, sched, ParticipantEnumType.FORM);
	}
	public FormElementParticipantType getFormElementParticipant(NameIdType cycle, FormElementType sched) throws FactoryException, ArgumentException
	{

		return getParticipant(cycle, sched, ParticipantEnumType.FORMELEMENT);
	}
	public FormElementValueParticipantType getFormElementValueParticipant(NameIdType cycle, FormElementValueType sched) throws FactoryException, ArgumentException
	{

		return getParticipant(cycle, sched, ParticipantEnumType.FORMELEMENTVALUE);
	}
	
	
	public StageParticipantType getStageParticipant(NameIdType cycle, StageType sched) throws FactoryException, ArgumentException
	{

		return getParticipant(cycle, sched, ParticipantEnumType.STAGE);
	}
	public ModuleParticipantType getModuleParticipant(NameIdType cycle, ModuleType sched) throws FactoryException, ArgumentException
	{

		return getParticipant(cycle, sched, ParticipantEnumType.MODULE);
	}
	
	public ProcessStepParticipantType getProcessStepParticipant(NameIdType cycle, ProcessStepType sched) throws FactoryException, ArgumentException
	{

		return getParticipant(cycle, sched, ParticipantEnumType.PROCESSSTEP);
	}
	public ProcessParticipantType getProcessParticipant(NameIdType cycle, ProcessType sched) throws FactoryException, ArgumentException
	{

		return getParticipant(cycle, sched, ParticipantEnumType.PROCESS);
	}
	public TaskParticipantType getTaskParticipant(NameIdType cycle, TaskType sched) throws FactoryException, ArgumentException
	{

		return getParticipant(cycle, sched, ParticipantEnumType.TASK);
	}
	public ModelParticipantType getModelParticipant(NameIdType cycle, ModelType sched) throws FactoryException, ArgumentException
	{

		return getParticipant(cycle, sched, ParticipantEnumType.MODEL);
	}

	public WorkParticipantType getWorkParticipant(NameIdType cycle, WorkType sched) throws FactoryException, ArgumentException
	{
		return getParticipant(cycle, sched, ParticipantEnumType.WORK);
	}
	public CaseParticipantType getCaseParticipant(NameIdType cycle, CaseType sched) throws FactoryException, ArgumentException
	{
		return getParticipant(cycle, sched, ParticipantEnumType.CASE);

	}
	public NoteParticipantType getNoteParticipant(NameIdType cycle, NoteType sched) throws FactoryException, ArgumentException
	{
		return getParticipant(cycle, sched, ParticipantEnumType.NOTE);
	}
	public TimeParticipantType getTimeParticipant(NameIdType cycle, TimeType sched) throws FactoryException, ArgumentException
	{
		return getParticipant(cycle, sched, ParticipantEnumType.TIME);
	}
	public CostParticipantType getCostParticipant(NameIdType cycle, CostType sched) throws FactoryException, ArgumentException
	{
		return getParticipant(cycle, sched, ParticipantEnumType.COST);
	}
	public ArtifactParticipantType getArtifactParticipant(NameIdType cycle, ArtifactType sched) throws FactoryException, ArgumentException
	{
		return getParticipant(cycle, sched, ParticipantEnumType.ARTIFACT);
	}

	public ArtifactParticipantType getDependencyParticipant(NameIdType cycle, ArtifactType sched) throws FactoryException, ArgumentException
	{

		return getParticipant(cycle, sched, ParticipantEnumType.DEPENDENCY);
	}
	public RequirementParticipantType getRequirementParticipant(NameIdType cycle, RequirementType sched) throws FactoryException, ArgumentException
	{

		return getParticipant(cycle, sched, ParticipantEnumType.REQUIREMENT);
	}
	public ResourceParticipantType getResourceParticipant(NameIdType cycle, ResourceType sched) throws FactoryException, ArgumentException
	{
		return getParticipant(cycle, sched, ParticipantEnumType.RESOURCE);
	}
	public ScheduleParticipantType getScheduleParticipant(NameIdType cycle, ScheduleType sched) throws FactoryException, ArgumentException
	{
		return getParticipant(cycle, sched, ParticipantEnumType.SCHEDULE);
	}

	public BudgetParticipantType getBudgetParticipant(NameIdType cycle, BudgetType sched) throws FactoryException, ArgumentException
	{

		return getParticipant(cycle, sched, ParticipantEnumType.BUDGET);
	}
	public ProjectParticipantType getProjectParticipant(NameIdType cycle, ProjectType sched) throws FactoryException, ArgumentException
	{

		return getParticipant(cycle, sched, ParticipantEnumType.PROJECT);
	}
	public GoalParticipantType getGoalParticipant(NameIdType cycle, GoalType sched) throws FactoryException, ArgumentException
	{
		return getParticipant(cycle, sched, ParticipantEnumType.GOAL);
	}
}