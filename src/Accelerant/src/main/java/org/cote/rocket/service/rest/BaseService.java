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
package org.cote.rocket.service.rest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.INameIdGroupFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.ITypeSanitizer;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.service.util.ServiceUtil;
import org.cote.propellant.objects.FormType;
import org.cote.propellant.objects.ProjectType;
import org.cote.propellant.objects.StageType;
import org.cote.propellant.objects.TaskType;
import org.cote.propellant.objects.WorkType;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.FormFactory;
import org.cote.rocket.services.FormService;
import org.cote.rocket.services.ValidationService;
public class BaseService  {
	public static final Logger logger = LogManager.getLogger(BaseService.class);
	public static boolean enableExtendedAttributes = false;
	
	public static String getDefaultGroupName(AuditEnumType type){
		String out_path = "~";
		logger.warn("TODO: Refactor this method out for type " + type.toString());
		
		switch(type){
			case FORM:
				out_path = "Forms";
				break;
			case TRAIT:
				out_path = "Traits";
				break;
			case LOCATION:
				out_path = "Locations";
				break;
			case EVENT:
				out_path = "Events";
				break;
			case VALIDATIONRULE:
				out_path = "ValidationRules";
				break;
			case FORMELEMENT:
				out_path = "FormElements";
				break;
			case PROJECT:
				out_path = "Projects";
				break;
			case MODULE:
				out_path = "Modules";
				break;
			case STAGE:
				out_path = "Stages";
				break;
			case METHODOLOGY:
				out_path = "Methodologies";
				break;
			case PROCESS:
				out_path = "Processes";
				break;
			case PROCESSSTEP:
				out_path = "ProcessSteps";
				break;
			case WORK:
				out_path = "Work";
				break;
			case TASK:
				out_path = "Tasks";
				break;
			case TICKET:
				out_path = "Tickets";
				break;
			case ESTIMATE:
				out_path = "Estimates";
				break;
			case MODEL:
				out_path = "Models";
				break;
			case ARTIFACT:
				out_path = "Artifacts";
				break;
			case REQUIREMENT:
				out_path = "Requirements";
				break;
			case CASE:
				out_path= "Cases";
				break;
			case LIFECYCLE:
				out_path = "Lifecycles";
				break;
			case NOTE:
				out_path = "Notes";
				break;
			case RESOURCE:
				out_path = "Resources";
				break;
			case BUDGET:
				out_path = "Budgets";
				break;
			case GOAL:
				out_path = "Goals";
				break;
			case TIME:
				out_path = "Times";
				break;
			case COST:
				out_path = "Costs";
				break;
			case SCHEDULE:
				out_path = "Schedules";
				break;
			default:
				out_path = org.cote.accountmanager.service.rest.BaseService.getDefaultGroupName(type);
				break;
		}
		return out_path;
	}
	/*
	public static String getDefaultPath(AuditEnumType type){
		return "~/" + getDefaultGroupName(type);
	}
	*/
	/// Restore organization and group ids from 'path' values on inbound objects
	///
	public static <T> void normalize(UserType user, T object) throws ArgumentException, FactoryException{
		
		if(object == null){
			throw new ArgumentException("Null object");
		}
		NameIdType obj = (NameIdType)object;
		/* obj.getOrganizationPath() == null ||  */
		if(obj.getNameType() == NameEnumType.UNKNOWN){
			throw new ArgumentException("Invalid object");
		}
		if(user != null && obj.getOrganizationPath() == null){
			logger.warn("Organization path not specified. Using context user's organization");
			obj.setOrganizationPath(((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationPath(user.getOrganizationId()));
		}
		/// Everything in Rocket is of this type (right?)
		///
		NameIdGroupFactory fact = Factories.getFactory(FactoryEnumType.valueOf(obj.getNameType().toString()));
		if(user != null){
			BaseGroupType group = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, ((NameIdDirectoryGroupType)obj).getGroupPath(),user.getOrganizationId());
			if(group != null){
				((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(group);
				((NameIdDirectoryGroupType)obj).setGroupPath(group.getPath());
			}
		}
		fact.normalize(object);

	}
	
	/// Apply 'path' values to outbound objects
	///
	public static <T> void denormalize(T object) throws ArgumentException, FactoryException{
		
		if(object == null){
			throw new ArgumentException("Null object");
		}
		NameIdType obj = (NameIdType)object;
		if(obj.getOrganizationId().compareTo(0L) == 0 || obj.getNameType() == NameEnumType.UNKNOWN){
			throw new ArgumentException("Invalid object");
		}

		
		NameIdGroupFactory fact = Factories.getFactory(FactoryEnumType.valueOf(obj.getNameType().toString()));
		fact.denormalize(object);

	}
	

	/// don't blindly accept values 
	///
	private static <T> boolean sanitizeAddNewObject(AuditEnumType type, UserType user, T in_obj) throws ArgumentException, FactoryException, DataException{
		boolean out_bool = false;
		INameIdFactory iFact = getFactory(type);
		ITypeSanitizer sanitizer = Factories.getSanitizer(NameEnumType.valueOf(type.toString()));
		if(sanitizer == null){
			logger.error("Sanitizer is null");
			return false;
		}
		T san_obj = sanitizer.sanitizeNewObject(type, user, in_obj);
		if(sanitizer.useAlternateAdd(type, san_obj)){
			out_bool = sanitizer.add(type, user, san_obj);
		}
		else{
			out_bool = iFact.add(san_obj);
		}

		return out_bool;
	}
	private static <T> boolean updateObject(AuditEnumType type, UserType user, T in_obj) throws ArgumentException, FactoryException, DataAccessException {
		boolean out_bool = false;
		INameIdFactory iFact = getFactory(type);
		if(type.equals(AuditEnumType.FORM)){
			out_bool = ValidationService.validateForm(user,(FormType)in_obj);
			if(out_bool == false){
				logger.warn("Failed to validate form");
				return false;
			}
			out_bool = ((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).update((FormType)in_obj);
			if(out_bool == false){
				logger.warn("Failed to update form");
				return false;
			}
			out_bool = FormService.updateFormValues(user,(FormType)in_obj,false);
			if(out_bool == false) logger.warn("Failed to update form values");

		}
		else{
			out_bool = iFact.update(in_obj);
		}

		if(out_bool && enableExtendedAttributes){
			NameIdType attrUpObj = (NameIdType)in_obj;
			out_bool = Factories.getAttributeFactory().updateAttributes(attrUpObj);
		}

		return out_bool;		
	}
	private static <T> boolean deleteObject(AuditEnumType type, T in_obj) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		if(enableExtendedAttributes){
			out_bool = Factories.getAttributeFactory().deleteAttributes((NameIdType)in_obj);
			if(out_bool == false){
				logger.warn("No extended attributes deleted for " + ((NameIdType)in_obj).getName());
			}
		}
		INameIdFactory iFact = getFactory(type);
		out_bool = iFact.delete(in_obj);
		
		return out_bool;
	}
	public static <T> T getFactory(AuditEnumType type){
		return Factories.getFactory(FactoryEnumType.valueOf(type.toString()));
	}
	private static <T> T getById(AuditEnumType type, long id, long organizationId) throws ArgumentException, FactoryException {
		NameIdGroupFactory factory = getFactory(type);
		T out_obj = factory.getById(id, organizationId);
		if(out_obj != null){
			populate(type, out_obj);
			denormalize(out_obj);
			if(enableExtendedAttributes){
				Factories.getAttributeFactory().populateAttributes((NameIdType)out_obj);
			}

		}
		return out_obj;		
	}
	private static int count(AuditEnumType type, DirectoryGroupType group) throws ArgumentException, FactoryException {
		NameIdGroupFactory factory = getFactory(type);
		return factory.countInGroup(group);		
	}
	private static <T> T getByNameInParent(AuditEnumType type, String name, NameIdDirectoryGroupType parent) throws ArgumentException, FactoryException {
		
		T out_obj = null;
		INameIdGroupFactory iFact = getFactory(type);
		if(iFact.isClusterByGroup()){
			out_obj = iFact.getByNameInGroup(name, parent.getId(), parent.getGroupId());
		}

		if(out_obj != null){
			populate(type, out_obj);
			if(enableExtendedAttributes){
				Factories.getAttributeFactory().populateAttributes((NameIdType)out_obj);
			}

		}
		return out_obj;		
	}
	private static <T> T getByNameInGroup(AuditEnumType type, String name, DirectoryGroupType group) throws ArgumentException, FactoryException {
		INameIdGroupFactory factory = getFactory(type);
		T out_obj = factory.getByNameInGroup(name, group);
		if(out_obj != null){
			populate(type, out_obj);
			denormalize(out_obj);
			if(enableExtendedAttributes){
				Factories.getAttributeFactory().populateAttributes((NameIdType)out_obj);
			}
		}
		return out_obj;		
	}
	
	public static <T> void deepPopulate(AuditEnumType aType, T obj){
		try {
			NameIdFactory fact = Factories.getFactory(FactoryEnumType.fromValue(aType.toString()));
			fact.depopulate(obj);
			populate(aType, obj, true);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		}
	}
	
	public static <T> void populateList(AuditEnumType aType, List<T> list, boolean deep) throws ArgumentException{
		for(int i = 0; i < list.size();i++){
			BaseService.populate(aType, list.get(i),deep);
		}
	}
	public static <T> void populate(AuditEnumType type,T object) throws ArgumentException{
		populate(type, object, false);
	}
	public static <T> void populate(AuditEnumType type,T object, boolean deep) throws ArgumentException{
		if(object == null) return;
		INameIdFactory iFact = getFactory(type);
		if(deep){
			logger.warn("Deep populate currently disabled  while refactoring");
		}
		try {
			iFact.populate(object);
			if(deep){
				switch(type){
					case PROJECT:
						ProjectType p = (ProjectType)object;
						populateList(AuditEnumType.ARTIFACT, p.getArtifacts(),deep);
						populateList(AuditEnumType.MODEL,p.getBlueprints(), deep);
						populateList(AuditEnumType.ARTIFACT,p.getDependencies(), deep);
						populateList(AuditEnumType.MODULE,p.getModules(), deep);
						populateList(AuditEnumType.REQUIREMENT,p.getRequirements(), deep);
						populateList(AuditEnumType.STAGE,p.getStages(), deep);
						populate(AuditEnumType.SCHEDULE,p.getSchedule(),true);
	
						break;
					case STAGE:

						StageType s = (StageType)object;
						populate(AuditEnumType.WORK, s.getWork(),deep);
						populate(AuditEnumType.BUDGET,s.getBudget(),deep);
						populate(AuditEnumType.METHODOLOGY,s.getMethodology(),deep);
						populate(AuditEnumType.SCHEDULE,s.getSchedule(),true);
					
						break;
					case WORK:

						WorkType w = (WorkType)object;
						populateList(AuditEnumType.ARTIFACT, w.getArtifacts(),deep);
						populateList(AuditEnumType.ARTIFACT,w.getDependencies(), deep);
						populateList(AuditEnumType.RESOURCE, w.getResources(),deep);
						populateList(AuditEnumType.TASK,w.getTasks(),deep);
						break;
						
					case TASK:
						TaskType t = (TaskType)object;
						populateList(AuditEnumType.ARTIFACT, t.getArtifacts(),deep);
						populateList(AuditEnumType.RESOURCE, t.getResources(),deep);
						populateList(AuditEnumType.TASK,t.getChildTasks(),deep);
						populateList(AuditEnumType.ARTIFACT,t.getDependencies(), deep);
						populateList(AuditEnumType.NOTE,t.getNotes(),deep);
						populateList(AuditEnumType.REQUIREMENT,t.getRequirements(),deep);
						populateList(AuditEnumType.WORK, t.getWork(),deep);
						populate(AuditEnumType.ESTIMATE,t.getEstimate(),deep);
						break;
				}
			}
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		}
		
		/*
		try{
		switch(type){
			case EVENT:
				((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).populate((EventType)object);
				break;
			case LOCATION:
				((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).populate((LocationType)object);
				break;

			case TRAIT:
				((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).populate((TraitType)object);
				break;
			case VALIDATIONRULE:
				((ValidationRuleFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULE)).populate((ValidationRuleType)object);
				break;
			case FORM:
				((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).populate((FormType)object);
				break;
			case FORMELEMENT:
				((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).populate((FormElementType)object);
				break;
			case PROJECT:
				((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).populate((ProjectType)object);
				if(deep){
					ProjectType p = (ProjectType)object;
					populateList(AuditEnumType.ARTIFACT, p.getArtifacts(),deep);
					populateList(AuditEnumType.MODEL,p.getBlueprints(), deep);
					populateList(AuditEnumType.ARTIFACT,p.getDependencies(), deep);
					populateList(AuditEnumType.MODULE,p.getModules(), deep);
					populateList(AuditEnumType.REQUIREMENT,p.getRequirements(), deep);
					populateList(AuditEnumType.STAGE,p.getStages(), deep);
					populate(AuditEnumType.SCHEDULE,p.getSchedule(),true);
				}
				break;
			case MODULE:
				((ModuleFactory)Factories.getFactory(FactoryEnumType.MODULE)).populate((ModuleType)object);
				break;
			case STAGE:
				((StageFactory)Factories.getFactory(FactoryEnumType.STAGE)).populate((StageType)object);
				if(deep){
					StageType s = (StageType)object;
					populate(AuditEnumType.WORK, s.getWork(),deep);
					populate(AuditEnumType.BUDGET,s.getBudget(),deep);
					populate(AuditEnumType.METHODOLOGY,s.getMethodology(),deep);
					populate(AuditEnumType.SCHEDULE,s.getSchedule(),true);
				}
				break;
			case METHODOLOGY:
				((MethodologyFactory)Factories.getFactory(FactoryEnumType.METHODOLOGY)).populate((MethodologyType)object);
				break;
			case PROCESS:
				((ProcessFactory)Factories.getFactory(FactoryEnumType.PROCESS)).populate((ProcessType)object);
				break;
			case PROCESSSTEP:
				((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).populate((ProcessStepType)object);
				break;
			case WORK:
				
				((WorkFactory)Factories.getFactory(FactoryEnumType.WORK)).populate((WorkType)object);
				if(deep){
					WorkType w = (WorkType)object;
					populateList(AuditEnumType.ARTIFACT, w.getArtifacts(),deep);
					populateList(AuditEnumType.ARTIFACT,w.getDependencies(), deep);
					populateList(AuditEnumType.RESOURCE, w.getResources(),deep);
					populateList(AuditEnumType.TASK,w.getTasks(),deep);
				}
				break;
			case ESTIMATE:
				((EstimateFactory)Factories.getFactory(FactoryEnumType.ESTIMATE)).populate((EstimateType)object);
				break;
			case TASK:
				((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).populate((TaskType)object);
				if(deep){
					TaskType t = (TaskType)object;
					populateList(AuditEnumType.ARTIFACT, t.getArtifacts(),deep);
					populateList(AuditEnumType.RESOURCE, t.getResources(),deep);
					populateList(AuditEnumType.TASK,t.getChildTasks(),deep);
					populateList(AuditEnumType.ARTIFACT,t.getDependencies(), deep);
					populateList(AuditEnumType.NOTE,t.getNotes(),deep);
					populateList(AuditEnumType.REQUIREMENT,t.getRequirements(),deep);
					populateList(AuditEnumType.WORK, t.getWork(),deep);
					populate(AuditEnumType.ESTIMATE,t.getEstimate(),deep);
					
				}
				break;
			case TICKET:
				((TicketFactory)Factories.getFactory(FactoryEnumType.TICKET)).populate((TicketType)object);
				break;
			case MODEL:
				((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).populate((ModelType)object);
				break;
			case ARTIFACT:
				((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).populate((ArtifactType)object);
				break;
			case CASE:
				((CaseFactory)Factories.getFactory(FactoryEnumType.CASE)).populate((CaseType)object);	
				break;
			case REQUIREMENT:
				((RequirementFactory)Factories.getFactory(FactoryEnumType.REQUIREMENT)).populate((RequirementType)object);	
				break;
			case LIFECYCLE:
				((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).populate((LifecycleType)object);	
				break;
			case NOTE:
				((NoteFactory)Factories.getFactory(FactoryEnumType.NOTE)).populate((NoteType)object);
				break;
			case RESOURCE:
				((ResourceFactory)Factories.getFactory(FactoryEnumType.RESOURCE)).populate((ResourceType)object);	
				break;
			case BUDGET:
				((BudgetFactory)Factories.getFactory(FactoryEnumType.BUDGET)).populate((BudgetType)object);
				break;
			case COST:
				((CostFactory)Factories.getFactory(FactoryEnumType.COST)).populate((CostType)object);
				break;
			case TIME:
				((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).populate((TimeType)object);
				break;
			case GOAL:
				((GoalFactory)Factories.getFactory(FactoryEnumType.GOAL)).populate((GoalType)object);
				break;
			case SCHEDULE:
				((ScheduleFactory)Factories.getFactory(FactoryEnumType.SCHEDULE)).populate((ScheduleType)object);
				break;
			}
		}
		catch(FactoryException fe){
			
		}
		*/
		/*
		NameIdDirectoryGroupType nid = (NameIdDirectoryGroupType)object;
		nid.setOrganizationId(nid.getOrganizationId().getId());
		nid.setOrganization(null);
		nid.setGroupId(nid.getGroupId());
		nid.setGroup(null);
		*/
	
	}
	private static <T> List<T> getListByGroup(AuditEnumType type, DirectoryGroupType group,long startRecord, int recordCount) throws ArgumentException, FactoryException {
		NameIdGroupFactory factory = getFactory(type);
		List<T> out_obj = factory.listInGroup(group, startRecord, recordCount, group.getOrganizationId());
		for(int i = 0; i < out_obj.size();i++){
			NameIdDirectoryGroupType ngt = (NameIdDirectoryGroupType)out_obj.get(i);
			denormalize(ngt);
			if(enableExtendedAttributes){
				Factories.getAttributeFactory().populateAttributes(ngt);
			}
		}
		return out_obj;			
	}
	
	
	public static <T> boolean delete(AuditEnumType type, T bean, HttpServletRequest request){
		
		boolean out_bool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.DELETE, "delete", AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		NameIdDirectoryGroupType dirBean = (NameIdDirectoryGroupType)bean;
		AuditService.targetAudit(audit, type, (dirBean == null ? "null" : dirBean.getUrn()));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return false;

		try {
			normalize(user,dirBean);
			if(dirBean.getId() <= 0 || dirBean.getGroupId() == null){
				AuditService.denyResult(audit,"Bean contains invalid data");
				return out_bool;
			}
			T check = readById(type, dirBean.getId(),request);
			if(check == null){
				AuditService.denyResult(audit,"User is not authorized to read the source object for id " + dirBean.getId());
				return false;
			}
			if(AuthorizationService.isMapOwner(user, (NameIdType)check) == false || AuthorizationService.canChange(user,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(((NameIdDirectoryGroupType)check).getGroupId(),((NameIdDirectoryGroupType)check).getOrganizationId())) == false){
				AuditService.denyResult(audit,"User is not authorized to alter the source group for id " + dirBean.getId());
				return false;
			}
			if(AuthorizationService.canChange(user, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(dirBean.getGroupId(), dirBean.getOrganizationId())) == true || AuthorizationService.isMapOwner(user, dirBean)){

				out_bool = deleteObject(type, bean);
				if(out_bool) AuditService.permitResult(audit, "Deleted " + dirBean.getName());
				else AuditService.denyResult(audit, "Unable to delete " + dirBean.getName());
				
			}
			else{
				AuditService.denyResult(audit, "User is not authorized");
				logger.error("User is not authorized to delete an object from the specified group #" + dirBean.getGroupId() + ", or user does not own the specified object");
			}
		} catch (ArgumentException e1) {
			
			logger.error("Error",e1);
			AuditService.denyResult(audit, e1.getMessage());
		} catch (FactoryException e1) {
			
			logger.error("Error",e1);
			AuditService.denyResult(audit, e1.getMessage());
		}

		return out_bool;
	}
	public static <T> boolean add(AuditEnumType addType, T bean, HttpServletRequest request){
		
		boolean out_bool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "add", AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		NameIdDirectoryGroupType dirBean = (NameIdDirectoryGroupType)bean;
		AuditService.targetAudit(audit, addType, (dirBean == null ? "null" : dirBean.getName()));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(dirBean.getNameType() == NameEnumType.UNKNOWN){
			logger.warn("NameType not specified.  Setting as " + addType.toString());
			dirBean.setNameType(NameEnumType.valueOf(addType.toString()));
		}
		if(user==null) return false;

		try {
			if(dirBean.getGroupId() == null || dirBean.getGroupId() <= 0L){
				dirBean.setGroupId(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateUserDirectory(user, getDefaultGroupName(addType)).getId());
			}
			normalize(user,dirBean);
			if(AuthorizationService.canChange(user, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(dirBean.getGroupId(), dirBean.getOrganizationId())) == true){

				out_bool = sanitizeAddNewObject(addType, user, bean);
				
				if(out_bool && enableExtendedAttributes){
					NameIdType obj = readByName(addType,((NameIdDirectoryGroupType)bean).getGroupId(),((NameIdDirectoryGroupType)bean).getName(),request);
					if(obj != null){
						out_bool = Factories.getAttributeFactory().updateAttributes((NameIdType)obj);
					}
					else{
						logger.warn("Failed to update extended attributes");
					}
					
				}
				if(out_bool){
					AuditService.permitResult(audit, "Added " + dirBean.getName());
				}
				else AuditService.denyResult(audit, "Unable to add " + dirBean.getName());
				
			}
			else{
				AuditService.denyResult(audit, "User is not authorized");
				logger.error("User is not authorized to add an object to the specified group #" + dirBean.getGroupId());
			}
		} catch (ArgumentException | FactoryException | DataException e) {
			// TODO Auto-generated catch block
			logger.error("Trace", e);
		}

		return out_bool;
	}
	
	public static <T> boolean update(AuditEnumType type, T bean,HttpServletRequest request){
		boolean out_bool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "update",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		NameIdDirectoryGroupType dirBean = (NameIdDirectoryGroupType)bean;
		AuditService.targetAudit(audit, type, (dirBean == null ? "null" : dirBean.getUrn()));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return false;
		if(dirBean == null){
			AuditService.denyResult(audit, "Null value");
			return false;
		}


		try {
			normalize(user,dirBean);
			
			/// 2015/06/22
			/// Add in restriction to block ownership changes via an update
			///
			NameIdType matBean = readById(type,dirBean.getId(),request);
			if(matBean == null){
				AuditService.denyResult(audit, "Unable to read original object");
				return false;
			}

			if(dirBean.getOwnerId().compareTo(matBean.getOwnerId()) != 0){
				AuditService.denyResult(audit, "Chown operation is forbidden in an update operation");
				return false;
			}
			if(AuthorizationService.isMapOwner(user, matBean)==false || AuthorizationService.canChange(user,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(((NameIdDirectoryGroupType)matBean).getGroupId(),matBean.getOrganizationId())) == false){
				AuditService.denyResult(audit,"User is not authorized to alter the source group for id " + dirBean.getId());
				return false;
			}
			if(AuthorizationService.isMapOwner(user, (NameIdType)dirBean) || AuthorizationService.canChange(user, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(dirBean.getGroupId(),dirBean.getOrganizationId())) == true){
				out_bool = updateObject(type, user, bean); 	
				if(out_bool) AuditService.permitResult(audit, "Updated " + dirBean.getName() + " (#" + dirBean.getId() + ")");
				else AuditService.denyResult(audit, "Unable to update " + dirBean.getName() + " (#" + dirBean.getId() + ")");
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to add an object to the specified group  #" + dirBean.getGroupId());
			}
		} catch (ArgumentException e1) {
			
			logger.error("Error",e1);
			AuditService.denyResult(audit, e1.getMessage());
		} catch (FactoryException e1) {
			
			logger.error("Error",e1);
			AuditService.denyResult(audit, e1.getMessage());
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
			AuditService.denyResult(audit, e.getMessage());
		}

		return out_bool;
	}
	public static <T> T readById(AuditEnumType type, long id,HttpServletRequest request){
		T out_obj = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readById",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, Long.toString(id));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return out_obj;
		
		try {
			
			NameIdDirectoryGroupType dirType = getById(type,id, user.getOrganizationId());
			if(dirType == null){
				AuditService.denyResult(audit, "#" + id + " doesn't exist in organization " + user.getOrganizationId());
				return null;
			}			
			if(AuthorizationService.isMapOwner(user, dirType) || AuthorizationService.canView(user, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(dirType.getGroupId(),dirType.getOrganizationId())) == true){
				out_obj = (T)dirType;
				AuditService.permitResult(audit, "Read " + dirType.getName() + " (#" + dirType.getId() + ")");
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view objects in the specified group '" + dirType.getName() + "' #" + dirType.getId());
			}

		} catch (ArgumentException e1) {
			
			logger.error("Error",e1);
		} catch (FactoryException e1) {
			
			logger.error("Error",e1);
		} 

		return out_obj;
	}	
	public static <T> T readByName(AuditEnumType type, String name,HttpServletRequest request){
		DirectoryGroupType dir = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, name);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return null;

		try{
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateUserDirectory(user, getDefaultGroupName(type));
		}
	 catch (FactoryException e1) {
		
		logger.error("Error",e1);
	} catch (ArgumentException e) {
		
		logger.error("Error",e);
	} 
		return readByName(audit,type, user, dir, name, request);
	}
	public static <T> T readByName(AuditEnumType type, long groupId, String name,HttpServletRequest request){
		DirectoryGroupType dir = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, name);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return null;

		try{
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getById(groupId, user.getOrganizationId());
		}
		 catch (FactoryException e1) {
			
			logger.error("Error",e1);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} 
		return readByName(audit,type, user, dir, name, request);
	}
	public static <T> T readByName(AuditEnumType type, DirectoryGroupType dir, String name,HttpServletRequest request){
		T out_obj = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, name);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return out_obj;
		return readByName(audit,type, user, dir, name, request);
	}
	public static <T> T readByName(AuditType audit,AuditEnumType type, UserType user, DirectoryGroupType dir, String name,HttpServletRequest request){
		T out_obj = null;
		try {
			//DirectoryGroupType group = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateUserDirectory(user, getDefaultGroupName(type));
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(dir);
			if(AuthorizationService.canView(user, dir) == true){

				out_obj = getByNameInGroup(type, name, dir);
				if(out_obj == null){
					AuditService.denyResult(audit, "'" + name + "' doesn't exist in group " + dir.getPath());
					return null;
				}
				AuditService.permitResult(audit, "Read " + name + " (#" + ((NameIdType)out_obj).getId() + ")");

			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view objects in the specified group '" + dir.getName() + "' #" + dir.getId());
			}
		} catch (ArgumentException e1) {
			
			logger.error("Error",e1);
		} catch (FactoryException e1) {
			
			logger.error("Error",e1);
		} 

		return out_obj;
	}
	
	public static <T> T readByNameInParent(AuditEnumType type, NameIdDirectoryGroupType parent, String name, HttpServletRequest request){
		T out_obj = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByNameInParent",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, name);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return out_obj;
		return readByNameInParent(audit,type, user, parent, name, request);
	}
	public static <T> T readByNameInParent(AuditType audit,AuditEnumType type, UserType user, NameIdDirectoryGroupType parent, String name,HttpServletRequest request){
		T out_obj = null;
		try {

			out_obj = getByNameInParent(type, name, parent);
			if(out_obj == null){
				AuditService.denyResult(audit, "'" + name + "' doesn't exist");
				return null;
			}
			if(AuthorizationService.canView(user, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(parent.getGroupId(),parent.getGroupId()))){
				AuditService.permitResult(audit, "Read " + name + " (#" + ((NameIdType)out_obj).getId() + ")");

			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view object '" + parent.getName() + "' #" + parent.getId());
				out_obj = null;
			}
		} catch (ArgumentException e1) {
			
			logger.error("Error",e1);
		} catch (FactoryException e1) {
			
			logger.error("Error",e1);
		} 

		return out_obj;
	}
	
	public static int count(AuditEnumType type, String path, HttpServletRequest request){
		DirectoryGroupType dir = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "count",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, path);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return 0;

		try{
			dir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, path, user.getOrganizationId());
			//dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getById(groupId, user.getOrganizationId());
		}
		 catch (FactoryException e1) {
			
			logger.error("Error",e1);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} 
		if(dir == null){
			AuditService.denyResult(audit, "Path '" + path + "' does not exist");
			return 0;
		}
		return count(audit,type, user, dir, request);
	}
	public static int count(AuditType audit,AuditEnumType type, UserType user, DirectoryGroupType dir, HttpServletRequest request){
		int out_count = 0;
		try {
			if(AuthorizationService.canView(user, dir) == true){
				out_count = count(type, dir);
				AuditService.permitResult(audit, "Count " + out_count + " of " + type.toString() + " in '" + dir.getName() + "' #" + dir.getId());
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view objects in the specified group '" + dir.getName() + "' #" + dir.getId());
			}
		} catch (ArgumentException e1) {
			
			logger.error("Error",e1);
		} catch (FactoryException e1) {
			
			logger.error("Error",e1);
		} 

		return out_count;
	}
	public static <T> List<T> getGroupList(AuditEnumType type, UserType user, String path, long startRecord, int recordCount){
		List<T> out_obj = new ArrayList<T>();

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, path,AuditEnumType.USER,(user == null ? "Null" : user.getName()));
		AuditService.targetAudit(audit, type, path);
		
		if(SessionSecurity.isAuthenticated(user) == false){
			AuditService.denyResult(audit, "User is null or not authenticated");
			return null;
		}
		
		try {
			DirectoryGroupType dir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, path, user.getOrganizationId());
			if(dir == null){
				AuditService.denyResult(audit, "Invalid path: '" + path + "'");
				return out_obj;
			}
			///AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			if(AuthorizationService.canView(user, dir) == true){
				AuditService.permitResult(audit, "Access authorized to group " + dir.getName());
				out_obj = getListByGroup(type,dir,startRecord,recordCount);
				/*
				for(int i = 0; i < out_obj.size();i++){
					delink(out_obj.get(i));
				}
				*/
				//out_Lifecycles = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).getListByGroup(dir, 0, 0, user.getOrganizationId());
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to view group " + dir.getName() + " (#" + dir.getId() + ")");
				return out_obj;
			}
		} catch (ArgumentException | FactoryException e) {
			
			logger.error("Error",e);
		} 

		return out_obj;
	}
	public static boolean flushFactoryCache(AuditEnumType auditType, HttpServletRequest request){
		boolean out_bool = false;
		NameIdGroupFactory f = getFactory(auditType);
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "flushAll",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, auditType, "Flush factory cache");
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return false;
		f.clearCache();
		AuditService.permitResult(audit, "Flushed factory cache");	
		return out_bool;
	}
}
