/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
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

import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.rocket.factory.bulk.BulkArtifactFactory;
import org.cote.rocket.factory.bulk.BulkBudgetFactory;
import org.cote.rocket.factory.bulk.BulkCaseFactory;
import org.cote.rocket.factory.bulk.BulkCaseParticipationFactory;
import org.cote.rocket.factory.bulk.BulkCostFactory;
import org.cote.rocket.factory.bulk.BulkEstimateFactory;
import org.cote.rocket.factory.bulk.BulkEventFactory;
import org.cote.rocket.factory.bulk.BulkEventParticipationFactory;
import org.cote.rocket.factory.bulk.BulkFormElementFactory;
import org.cote.rocket.factory.bulk.BulkFormElementParticipationFactory;
import org.cote.rocket.factory.bulk.BulkFormFactory;
import org.cote.rocket.factory.bulk.BulkFormParticipationFactory;
import org.cote.rocket.factory.bulk.BulkGoalFactory;
import org.cote.rocket.factory.bulk.BulkGoalParticipationFactory;
import org.cote.rocket.factory.bulk.BulkLifecycleFactory;
import org.cote.rocket.factory.bulk.BulkLifecycleParticipationFactory;
import org.cote.rocket.factory.bulk.BulkLocationFactory;
import org.cote.rocket.factory.bulk.BulkLocationParticipationFactory;
import org.cote.rocket.factory.bulk.BulkMethodologyFactory;
import org.cote.rocket.factory.bulk.BulkMethodologyParticipationFactory;
import org.cote.rocket.factory.bulk.BulkModelFactory;
import org.cote.rocket.factory.bulk.BulkModelParticipationFactory;
import org.cote.rocket.factory.bulk.BulkModuleFactory;
import org.cote.rocket.factory.bulk.BulkModuleParticipationFactory;
import org.cote.rocket.factory.bulk.BulkNoteFactory;
import org.cote.rocket.factory.bulk.BulkProcessFactory;
import org.cote.rocket.factory.bulk.BulkProcessParticipationFactory;
import org.cote.rocket.factory.bulk.BulkProcessStepFactory;
import org.cote.rocket.factory.bulk.BulkProcessStepParticipationFactory;
import org.cote.rocket.factory.bulk.BulkProjectFactory;
import org.cote.rocket.factory.bulk.BulkProjectParticipationFactory;
import org.cote.rocket.factory.bulk.BulkRequirementFactory;
import org.cote.rocket.factory.bulk.BulkResourceFactory;
import org.cote.rocket.factory.bulk.BulkScheduleFactory;
import org.cote.rocket.factory.bulk.BulkScheduleParticipationFactory;
import org.cote.rocket.factory.bulk.BulkStageFactory;
import org.cote.rocket.factory.bulk.BulkTaskFactory;
import org.cote.rocket.factory.bulk.BulkTaskParticipationFactory;
import org.cote.rocket.factory.bulk.BulkTicketFactory;
import org.cote.rocket.factory.bulk.BulkTicketParticipationFactory;
import org.cote.rocket.factory.bulk.BulkTimeFactory;
import org.cote.rocket.factory.bulk.BulkTraitFactory;
import org.cote.rocket.factory.bulk.BulkValidationRuleFactory;
import org.cote.rocket.factory.bulk.BulkValidationRuleParticipationFactory;
import org.cote.rocket.factory.bulk.BulkWorkFactory;
import org.cote.rocket.factory.bulk.BulkWorkParticipationFactory;

public class BulkFactories extends org.cote.accountmanager.data.BulkFactories {
	
    static{
    	registerClass(FactoryEnumType.ARTIFACT, BulkArtifactFactory.class); 
	    registerClass(FactoryEnumType.BUDGET, BulkBudgetFactory.class); 
	    registerClass(FactoryEnumType.CASE, BulkCaseFactory.class); 
	    registerClass(FactoryEnumType.CASEPARTICIPATION, BulkCaseParticipationFactory.class); 
	    registerClass(FactoryEnumType.COST, BulkCostFactory.class); 
	    registerClass(FactoryEnumType.ESTIMATE, BulkEstimateFactory.class); 
	    registerClass(FactoryEnumType.EVENT, BulkEventFactory.class); 
	    registerClass(FactoryEnumType.EVENTPARTICIPATION, BulkEventParticipationFactory.class); 
	    registerClass(FactoryEnumType.FORMELEMENT, BulkFormElementFactory.class); 
	    registerClass(FactoryEnumType.FORMELEMENTPARTICIPATION, BulkFormElementParticipationFactory.class); 
	    registerClass(FactoryEnumType.FORM, BulkFormFactory.class); 
	    registerClass(FactoryEnumType.FORMPARTICIPATION, BulkFormParticipationFactory.class); 
	    registerClass(FactoryEnumType.GOAL, BulkGoalFactory.class); 
	    registerClass(FactoryEnumType.GOALPARTICIPATION, BulkGoalParticipationFactory.class); 
	    registerClass(FactoryEnumType.LIFECYCLE, BulkLifecycleFactory.class); 
	    registerClass(FactoryEnumType.LIFECYCLEPARTICIPATION, BulkLifecycleParticipationFactory.class); 
	    registerClass(FactoryEnumType.LOCATION, BulkLocationFactory.class); 
	    registerClass(FactoryEnumType.LOCATIONPARTICIPATION, BulkLocationParticipationFactory.class); 
	    registerClass(FactoryEnumType.METHODOLOGY, BulkMethodologyFactory.class); 
	    registerClass(FactoryEnumType.METHODOLOGYPARTICIPATION, BulkMethodologyParticipationFactory.class); 
	    registerClass(FactoryEnumType.MODEL, BulkModelFactory.class); 
	    registerClass(FactoryEnumType.MODELPARTICIPATION, BulkModelParticipationFactory.class); 
	    registerClass(FactoryEnumType.MODULE, BulkModuleFactory.class); 
	    registerClass(FactoryEnumType.MODULEPARTICIPATION, BulkModuleParticipationFactory.class); 
	    registerClass(FactoryEnumType.NOTE, BulkNoteFactory.class); 
	    registerClass(FactoryEnumType.PROCESS, BulkProcessFactory.class); 
	    registerClass(FactoryEnumType.PROCESSPARTICIPATION, BulkProcessParticipationFactory.class); 
	    registerClass(FactoryEnumType.PROCESSSTEP, BulkProcessStepFactory.class); 
	    registerClass(FactoryEnumType.PROCESSSTEPPARTICIPATION, BulkProcessStepParticipationFactory.class); 
	    registerClass(FactoryEnumType.PROJECT, BulkProjectFactory.class); 
	    registerClass(FactoryEnumType.PROJECTPARTICIPATION, BulkProjectParticipationFactory.class); 
	    registerClass(FactoryEnumType.REQUIREMENT, BulkRequirementFactory.class); 
	    registerClass(FactoryEnumType.RESOURCE, BulkResourceFactory.class); 
	    registerClass(FactoryEnumType.SCHEDULE, BulkScheduleFactory.class); 
	    registerClass(FactoryEnumType.SCHEDULEPARTICIPATION, BulkScheduleParticipationFactory.class); 
	    registerClass(FactoryEnumType.STAGE, BulkStageFactory.class); 
	    registerClass(FactoryEnumType.TASK, BulkTaskFactory.class); 
	    registerClass(FactoryEnumType.TASKPARTICIPATION, BulkTaskParticipationFactory.class); 
	    registerClass(FactoryEnumType.TICKET, BulkTicketFactory.class); 
	    registerClass(FactoryEnumType.TICKETPARTICIPATION, BulkTicketParticipationFactory.class); 
	    registerClass(FactoryEnumType.TIME, BulkTimeFactory.class); 
	    registerClass(FactoryEnumType.TRAIT, BulkTraitFactory.class); 
	    registerClass(FactoryEnumType.VALIDATIONRULE, BulkValidationRuleFactory.class); 
	    registerClass(FactoryEnumType.VALIDATIONRULEPARTICIPATION, BulkValidationRuleParticipationFactory.class); 
	    registerClass(FactoryEnumType.WORK, BulkWorkFactory.class); 
	    registerClass(FactoryEnumType.WORKPARTICIPATION, BulkWorkParticipationFactory.class);
    }

    public static void prepare(){
    	logger.debug("Touch Rocket Bulk Factories to initialize static registration");
    }

	
}
