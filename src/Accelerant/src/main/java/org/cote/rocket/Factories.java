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
package org.cote.rocket;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.propellant.objects.ArtifactType;
import org.cote.propellant.objects.BudgetType;
import org.cote.propellant.objects.CaseType;
import org.cote.propellant.objects.CostType;
import org.cote.propellant.objects.EstimateType;
import org.cote.propellant.objects.EventType;
import org.cote.propellant.objects.FormElementType;
import org.cote.propellant.objects.FormElementValueType;
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
import org.cote.rocket.factory.ArtifactFactory;
import org.cote.rocket.factory.BudgetFactory;
import org.cote.rocket.factory.CaseFactory;
import org.cote.rocket.factory.CaseParticipationFactory;
import org.cote.rocket.factory.CostFactory;
import org.cote.rocket.factory.EstimateFactory;
import org.cote.rocket.factory.EventFactory;
import org.cote.rocket.factory.EventParticipationFactory;
import org.cote.rocket.factory.FactoryDefaults;
import org.cote.rocket.factory.FormElementFactory;
import org.cote.rocket.factory.FormElementParticipationFactory;
import org.cote.rocket.factory.FormElementValueFactory;
import org.cote.rocket.factory.FormFactory;
import org.cote.rocket.factory.FormParticipationFactory;
import org.cote.rocket.factory.GoalFactory;
import org.cote.rocket.factory.GoalParticipationFactory;
import org.cote.rocket.factory.LifecycleFactory;
import org.cote.rocket.factory.LifecycleParticipationFactory;
import org.cote.rocket.factory.LocationFactory;
import org.cote.rocket.factory.LocationParticipationFactory;
import org.cote.rocket.factory.MethodologyFactory;
import org.cote.rocket.factory.MethodologyParticipationFactory;
import org.cote.rocket.factory.ModelFactory;
import org.cote.rocket.factory.ModelParticipationFactory;
import org.cote.rocket.factory.ModuleFactory;
import org.cote.rocket.factory.ModuleParticipationFactory;
import org.cote.rocket.factory.NoteFactory;
import org.cote.rocket.factory.ProcessFactory;
import org.cote.rocket.factory.ProcessParticipationFactory;
import org.cote.rocket.factory.ProcessStepFactory;
import org.cote.rocket.factory.ProcessStepParticipationFactory;
import org.cote.rocket.factory.ProjectFactory;
import org.cote.rocket.factory.ProjectParticipationFactory;
import org.cote.rocket.factory.RequirementFactory;
import org.cote.rocket.factory.ResourceFactory;
import org.cote.rocket.factory.ScheduleFactory;
import org.cote.rocket.factory.ScheduleParticipationFactory;
import org.cote.rocket.factory.StageFactory;
import org.cote.rocket.factory.TaskFactory;
import org.cote.rocket.factory.TaskParticipationFactory;
import org.cote.rocket.factory.TicketFactory;
import org.cote.rocket.factory.TicketParticipationFactory;
import org.cote.rocket.factory.TimeFactory;
import org.cote.rocket.factory.TraitFactory;
import org.cote.rocket.factory.ValidationRuleFactory;
import org.cote.rocket.factory.ValidationRuleParticipationFactory;
import org.cote.rocket.factory.WorkFactory;
import org.cote.rocket.factory.WorkParticipationFactory;
import org.cote.rocket.services.TypeSanitizer;



public class Factories extends org.cote.accountmanager.data.Factories {
    static{
    	registerClass(FactoryEnumType.ARTIFACT, ArtifactFactory.class); 
	    registerClass(FactoryEnumType.BUDGET, BudgetFactory.class); 
	    registerClass(FactoryEnumType.CASE, CaseFactory.class); 
	    registerClass(FactoryEnumType.CASEPARTICIPATION, CaseParticipationFactory.class); 
	    registerClass(FactoryEnumType.COST, CostFactory.class); 
	    registerClass(FactoryEnumType.ESTIMATE, EstimateFactory.class); 
	    registerClass(FactoryEnumType.EVENT, EventFactory.class); 
	    registerClass(FactoryEnumType.EVENTPARTICIPATION, EventParticipationFactory.class); 
	    registerClass(FactoryEnumType.FORMELEMENT, FormElementFactory.class); 
	    registerClass(FactoryEnumType.FORMELEMENTPARTICIPATION, FormElementParticipationFactory.class); 
	    registerClass(FactoryEnumType.FORMELEMENTVALUE, FormElementValueFactory.class); 
	    registerClass(FactoryEnumType.FORM, FormFactory.class); 
	    registerClass(FactoryEnumType.FORMPARTICIPATION, FormParticipationFactory.class); 
	    registerClass(FactoryEnumType.GOAL, GoalFactory.class); 
	    registerClass(FactoryEnumType.GOALPARTICIPATION, GoalParticipationFactory.class); 
	    registerClass(FactoryEnumType.LIFECYCLE, LifecycleFactory.class); 
	    registerClass(FactoryEnumType.LIFECYCLEPARTICIPATION, LifecycleParticipationFactory.class); 
	    registerClass(FactoryEnumType.LOCATION, LocationFactory.class); 
	    registerClass(FactoryEnumType.LOCATIONPARTICIPATION, LocationParticipationFactory.class); 
	    registerClass(FactoryEnumType.METHODOLOGY, MethodologyFactory.class); 
	    registerClass(FactoryEnumType.METHODOLOGYPARTICIPATION, MethodologyParticipationFactory.class); 
	    registerClass(FactoryEnumType.MODEL, ModelFactory.class); 
	    registerClass(FactoryEnumType.MODELPARTICIPATION, ModelParticipationFactory.class); 
	    registerClass(FactoryEnumType.MODULE, ModuleFactory.class); 
	    registerClass(FactoryEnumType.MODULEPARTICIPATION, ModuleParticipationFactory.class); 
	    registerClass(FactoryEnumType.NOTE, NoteFactory.class); 
	    registerClass(FactoryEnumType.PROCESS, ProcessFactory.class); 
	    registerClass(FactoryEnumType.PROCESSPARTICIPATION, ProcessParticipationFactory.class); 
	    registerClass(FactoryEnumType.PROCESSSTEP, ProcessStepFactory.class); 
	    registerClass(FactoryEnumType.PROCESSSTEPPARTICIPATION, ProcessStepParticipationFactory.class); 
	    registerClass(FactoryEnumType.PROJECT, ProjectFactory.class); 
	    registerClass(FactoryEnumType.PROJECTPARTICIPATION, ProjectParticipationFactory.class); 
	    registerClass(FactoryEnumType.REQUIREMENT, RequirementFactory.class); 
	    registerClass(FactoryEnumType.RESOURCE, ResourceFactory.class); 
	    registerClass(FactoryEnumType.SCHEDULE, ScheduleFactory.class); 
	    registerClass(FactoryEnumType.SCHEDULEPARTICIPATION, ScheduleParticipationFactory.class); 
	    registerClass(FactoryEnumType.STAGE, StageFactory.class); 
	    registerClass(FactoryEnumType.TASK, TaskFactory.class); 
	    registerClass(FactoryEnumType.TASKPARTICIPATION, TaskParticipationFactory.class); 
	    registerClass(FactoryEnumType.TICKET, TicketFactory.class); 
	    registerClass(FactoryEnumType.TICKETPARTICIPATION, TicketParticipationFactory.class); 
	    registerClass(FactoryEnumType.TIME, TimeFactory.class); 
	    registerClass(FactoryEnumType.TRAIT, TraitFactory.class); 
	    registerClass(FactoryEnumType.VALIDATIONRULE, ValidationRuleFactory.class); 
	    registerClass(FactoryEnumType.VALIDATIONRULEPARTICIPATION, ValidationRuleParticipationFactory.class); 
	    registerClass(FactoryEnumType.WORK, WorkFactory.class); 
	    registerClass(FactoryEnumType.WORKPARTICIPATION, WorkParticipationFactory.class);
    }
    
    static{
    	registerTypeClass(FactoryEnumType.ARTIFACT, ArtifactType.class); 
	    registerTypeClass(FactoryEnumType.BUDGET, BudgetType.class); 
	    registerTypeClass(FactoryEnumType.CASE, CaseType.class); 
	    registerTypeClass(FactoryEnumType.CASEPARTICIPATION, BaseParticipantType.class); 
	    registerTypeClass(FactoryEnumType.COST, CostType.class); 
	    registerTypeClass(FactoryEnumType.ESTIMATE, EstimateType.class); 
	    registerTypeClass(FactoryEnumType.EVENT, EventType.class); 
	    registerTypeClass(FactoryEnumType.EVENTPARTICIPATION, BaseParticipantType.class); 
	    registerTypeClass(FactoryEnumType.FORMELEMENT, FormElementType.class); 
	    registerTypeClass(FactoryEnumType.FORMELEMENTPARTICIPATION, BaseParticipantType.class); 
	    registerTypeClass(FactoryEnumType.FORMELEMENTVALUE, FormElementValueType.class); 
	    registerTypeClass(FactoryEnumType.FORM, FormType.class); 
	    registerTypeClass(FactoryEnumType.FORMPARTICIPATION, BaseParticipantType.class); 
	    registerTypeClass(FactoryEnumType.GOAL, GoalType.class); 
	    registerTypeClass(FactoryEnumType.GOALPARTICIPATION, BaseParticipantType.class); 
	    registerTypeClass(FactoryEnumType.LIFECYCLE, LifecycleType.class); 
	    registerTypeClass(FactoryEnumType.LIFECYCLEPARTICIPATION, BaseParticipantType.class); 
	    registerTypeClass(FactoryEnumType.LOCATION, LocationType.class); 
	    registerTypeClass(FactoryEnumType.LOCATIONPARTICIPATION, BaseParticipantType.class); 
	    registerTypeClass(FactoryEnumType.METHODOLOGY, MethodologyType.class); 
	    registerTypeClass(FactoryEnumType.METHODOLOGYPARTICIPATION, BaseParticipantType.class); 
	    registerTypeClass(FactoryEnumType.MODEL, ModelType.class); 
	    registerTypeClass(FactoryEnumType.MODELPARTICIPATION, BaseParticipantType.class); 
	    registerTypeClass(FactoryEnumType.MODULE, ModuleType.class); 
	    registerTypeClass(FactoryEnumType.MODULEPARTICIPATION, BaseParticipantType.class); 
	    registerTypeClass(FactoryEnumType.NOTE, NoteType.class); 
	    registerTypeClass(FactoryEnumType.PROCESS, ProcessType.class); 
	    registerTypeClass(FactoryEnumType.PROCESSPARTICIPATION, BaseParticipantType.class); 
	    registerTypeClass(FactoryEnumType.PROCESSSTEP, ProcessStepType.class); 
	    registerTypeClass(FactoryEnumType.PROCESSSTEPPARTICIPATION, BaseParticipantType.class); 
	    registerTypeClass(FactoryEnumType.PROJECT, ProjectType.class); 
	    registerTypeClass(FactoryEnumType.PROJECTPARTICIPATION, BaseParticipantType.class); 
	    registerTypeClass(FactoryEnumType.REQUIREMENT, RequirementType.class); 
	    registerTypeClass(FactoryEnumType.RESOURCE, ResourceType.class); 
	    registerTypeClass(FactoryEnumType.SCHEDULE, ScheduleType.class); 
	    registerTypeClass(FactoryEnumType.SCHEDULEPARTICIPATION, BaseParticipantType.class); 
	    registerTypeClass(FactoryEnumType.STAGE, StageType.class); 
	    registerTypeClass(FactoryEnumType.TASK, TaskType.class); 
	    registerTypeClass(FactoryEnumType.TASKPARTICIPATION, BaseParticipantType.class); 
	    registerTypeClass(FactoryEnumType.TICKET, TicketType.class); 
	    registerTypeClass(FactoryEnumType.TICKETPARTICIPATION, BaseParticipantType.class); 
	    registerTypeClass(FactoryEnumType.TIME, TimeType.class); 
	    registerTypeClass(FactoryEnumType.TRAIT, TraitType.class); 
	    registerTypeClass(FactoryEnumType.VALIDATIONRULE, ValidationRuleType.class); 
	    registerTypeClass(FactoryEnumType.VALIDATIONRULEPARTICIPATION, BaseParticipantType.class); 
	    registerTypeClass(FactoryEnumType.WORK, WorkType.class); 
	    registerTypeClass(FactoryEnumType.WORKPARTICIPATION, BaseParticipantType.class);
    }
    
    static{
    	registerNameTypeSanitizer(NameEnumType.ARTIFACT, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.BUDGET, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.CASE, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.COST, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.ESTIMATE, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.EVENT, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.FORMELEMENT, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.FORMELEMENTVALUE, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.FORM, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.GOAL, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.LIFECYCLE, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.LOCATION, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.METHODOLOGY, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.MODEL, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.MODULE, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.NOTE, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.PROCESS, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.PROCESSSTEP, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.PROJECT, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.REQUIREMENT, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.RESOURCE, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.SCHEDULE, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.STAGE, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.TASK, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.TICKET, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.TIME, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.TRAIT, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.VALIDATIONRULE, TypeSanitizer.class); 
	    registerNameTypeSanitizer(NameEnumType.WORK, TypeSanitizer.class); 
    }
    
    public static void prepare(){
    	logger.info("Touch Rocket to initialize static registration");
    	BulkFactories.prepare();
    }

	public static boolean cleanupOrphans(){
		
		boolean out_bool = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();
		Statement stat = null;
		try {
			stat = connection.createStatement();
			stat.executeQuery("SELECT * FROM cleanup_rocket_orphans();");
			out_bool = true;
		} catch (SQLException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		finally{
			try {
				if(stat != null) stat.close();
				connection.close();
			} catch (SQLException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		clearCaches();
		return org.cote.accountmanager.data.Factories.cleanupOrphans();
	}
	
	public static void warmUp() throws FactoryException{
		prepare();
		org.cote.accountmanager.data.Factories.warmUp();
	}
	public static boolean clearCaches(){
		Rocket.clearCache();
		FactoryDefaults.clearCache();
		return org.cote.accountmanager.data.Factories.clearCaches();
	}

	
}
