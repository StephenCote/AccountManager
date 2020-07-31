package org.cote.rocket.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.ITypeSanitizer;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.propellant.objects.ArtifactType;
import org.cote.propellant.objects.BudgetType;
import org.cote.propellant.objects.EstimateType;
import org.cote.propellant.objects.ProjectType;
import org.cote.propellant.objects.ScheduleType;
import org.cote.propellant.objects.StageType;
import org.cote.propellant.objects.TaskType;
import org.cote.propellant.objects.WorkType;
import org.cote.propellant.objects.types.ArtifactEnumType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.ArtifactFactory;


public class CommunityProjectUtil {

	public static final Logger logger = LogManager.getLogger(CommunityProjectUtil.class);

	public static boolean saveCommunityProject(ProjectType project,UserType user){
		boolean outBool = false;
		if(project == null || user == null){
			logger.error("User or project argument was null");
			return outBool;
		}
		ImportMap importMap = new ImportMap();
		
		long startSave = System.currentTimeMillis();
		
		logger.info("Building URN Map");

		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "Save or update community project",AuditEnumType.USER, user.getUrn());

		if(project == null || project.getName() == null || project.getGroupPath() == null){
			AuditService.denyResult(audit, "Null project reference");
			return outBool;
		}
		AuditService.targetAudit(audit, AuditEnumType.PROJECT, project.getUrn());

		
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		
		try{
			if(AuthorizationService.canChange(user, BaseService.find(AuditEnumType.GROUP, "DATA", project.getGroupPath(),user)) == false){
				AuditService.denyResult(audit, "Not authorized to alter group " + project.getGroupPath());
				return outBool;
			}
			project.setNameType(NameEnumType.PROJECT);

			/// Check for project with same name in same group.  If it exists, and id is different, then give this project a suffix
			/// ProjectType comp = ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).getByNameInGroup(project.getName(), ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(project.getGroupId(),project.getOrganizationId()));

			buildUrnMap(user,FactoryEnumType.PROJECT,project,importMap);
			if(saveStages(audit,importMap,user, project,sessionId)){
				BulkFactories.getBulkFactory().write(sessionId);
				BulkFactories.getBulkFactory().close(sessionId);
				if((project = saveType(audit,AuditEnumType.PROJECT,FactoryEnumType.PROJECT,importMap,user,project,null)) != null){
					logger.info("Saved community project");
					outBool = true;
				}
				else{
					logger.info("Failed to save community project");
				}
			}
			else{
				logger.info("Failed to save community project details");
			}
			
			
			/// conclude by adding/saving project
			
		}
		catch(FactoryException | ArgumentException | DataAccessException | DataException e) {
			logger.error(e);
			
		}
		logger.info("Save Time: " + (System.currentTimeMillis() - startSave));
		return outBool;
	}
	
	
	public static void buildUrnMap(UserType user,FactoryEnumType fType,NameIdType baseType,ImportMap importMap) throws ArgumentException, FactoryException, DataException{
		logger.debug("Mapping " + fType.toString() + " " + baseType.getUrn());
		//// For new objects, put the sanitized object into the map
		/// Then, in the save operation, use the map value
		/// This should allow for the dependency reference for new objects
		///
		ITypeSanitizer sanitizer = Factories.getSanitizer(NameEnumType.valueOf(fType.toString()));
		if(baseType.getUrn() != null) importMap.urnMap.put(baseType.getUrn(),(baseType.getId().compareTo(0L) == 0 ? sanitizer.sanitizeNewObject(AuditEnumType.fromValue(fType.toString()),user,baseType) : baseType));
		switch(fType){
			case PROJECT:
				ProjectType proj = ((ProjectType)baseType);
				for(int s = 0; s < proj.getStages().size();s++){
					buildUrnMap(user,FactoryEnumType.STAGE,proj.getStages().get(s),importMap);
				}
				break;
			case STAGE:
				buildUrnMap(user,FactoryEnumType.WORK,((StageType)baseType).getWork(),importMap);
				break;
			case WORK:
				WorkType wt = ((WorkType)baseType);
				for(int i = 0; i < wt.getTasks().size();i++){
					buildUrnMap(user,FactoryEnumType.TASK,wt.getTasks().get(i),importMap);
				}
				break;
			case TASK:
				TaskType tt = ((TaskType)baseType);
				for(int i = 0; i < tt.getChildTasks().size();i++){
					buildUrnMap(user,FactoryEnumType.TASK,tt.getChildTasks().get(i),importMap);
				}
				break;
			default:
				logger.debug(String.format(FactoryException.UNHANDLED_TYPE, fType.toString()));
				break;

		}
	}
	
	public static boolean saveStages(AuditType audit,ImportMap importMap, UserType user, ProjectType project,String sessionId) throws FactoryException, ArgumentException, DataAccessException, DataException{
		boolean out_bool = false;
		int i = 0;
		int size = project.getStages().size();
		List<StageType> upStages = new ArrayList<>();
		
		for(i = 0; i < size;i++){
			StageType stage = project.getStages().get(i);
			if(stage.getName() == null || stage.getName().length() == 0){
				stage.setName(project.getName() + " Stage " + (i+1));
			}
			if(AuthorizationService.canChange(user, BaseService.find(AuditEnumType.GROUP, "DATA", stage.getGroupPath(),user)) == false){
				AuditService.denyResult(audit, "Not authorized to alter group " + stage.getGroupPath());
				break;
			}

			stage.setBudget(saveBudget(audit,user,stage.getBudget(),sessionId));
			stage.setSchedule(saveSchedule(audit,user,stage,stage.getSchedule(),sessionId));

			if(stage.getWork() == null){
				logger.error("Expected work object on stage " + stage.getName());
				continue;
			}

			stage.setWork(saveWork(audit,importMap,user,stage.getWork(),sessionId));
			stage = saveType(audit,AuditEnumType.STAGE,FactoryEnumType.STAGE,importMap,user,stage,sessionId);

			/// put the stage object back into the array it its position in case the object was swapped out
			if(stage != null) upStages.add(stage);
		}
		project.getStages().clear();
		project.getStages().addAll(upStages);
		if(i == size) out_bool = true;
		return out_bool;


	}
	private static WorkType saveWork(AuditType audit,ImportMap importMap, UserType user, WorkType work,String sessionId) throws ArgumentException, FactoryException, DataAccessException, DataException{
		int tsize = work.getTasks().size();
		List<TaskType> upTasks = new ArrayList<TaskType>();
		for(int i = 0; i < tsize; i++){
			TaskType task = saveTask(audit,importMap,user,null,work.getTasks().get(i),sessionId);
			if(task != null) upTasks.add(task);
		}
		work.getTasks().clear();
		work.getTasks().addAll(upTasks);
		return saveType(audit,AuditEnumType.WORK,FactoryEnumType.WORK,importMap,user,work,sessionId);
	}
	private static TaskType saveTask(AuditType audit,ImportMap importMap, UserType user, TaskType parent, TaskType task, String sessionId) throws ArgumentException, FactoryException, DataAccessException, DataException{
		/// Save the potential parent task first in order to get the ID
		///
		if(parent != null) task.setParentId(parent.getId());
		task.setEstimate(saveEstimate(audit,user,task,task.getEstimate(),sessionId));
		
		List<ArtifactType> newDepends = new ArrayList<>();
		for(int d = 0; d < task.getDependencies().size();d++){
			ArtifactType at = task.getDependencies().get(d);
			String refUrn = at.getReferenceUrn();
			if(refUrn == null){
				logger.warn("Null reference urn for task " + task.getName() + " dependency " + at.getName());
				continue;
			}
			if(importMap.urnMap.containsKey(refUrn) == false){
				logger.warn("Reference urn '" + refUrn + "' was not found in the urn map");
				continue;
			}

			NameIdType refType = importMap.urnMap.get(refUrn);
			logger.info("Attach dependency for object id " + refType.getId());
			
			/// using direct factory read to test for bulk lookup capability
			DirectoryGroupType atDir = BaseService.find(AuditEnumType.GROUP, "DATA", at.getGroupPath(), user);
			ArtifactType mt = BaseService.readByName(AuditEnumType.ARTIFACT, atDir, refUrn, user);
			
			if(mt == null){
				mt = ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).newArtifact(user, atDir.getId());
				mt.setName(refUrn);
				mt.setArtifactType(ArtifactEnumType.fromValue(refType.getNameType().toString()));
				mt.setArtifactDataId(refType.getId());
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ARTIFACT, mt);
			}
			else if(mt.getId().compareTo(0L) < 0){
				logger.info("Entry " + mt.getName() + " already pending add/update");
			}
			else{
				mt.setArtifactType(ArtifactEnumType.TASK);
				mt.setArtifactDataId(refType.getId());
				BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.ARTIFACT, mt);
			}
			newDepends.add(mt);
		}
		task.getDependencies().clear();
		task.getDependencies().addAll(newDepends);
		
		TaskType nTask = saveType(audit,AuditEnumType.TASK,FactoryEnumType.TASK,importMap,user,task,sessionId);

		if(nTask == null){
			logger.warn("Skipping task that failed to save");
			return null;
		}
		logger.info("Task " + nTask.getName() + " estimate is " + (nTask.getEstimate() == null ? "null":nTask.getEstimate().getName() + "-" + nTask.getEstimate().getId()));


		/// read children from original reference
		/// because this value will be cleared when saving/re-reading a new parent
		///
		int tsize = task.getChildTasks().size();
		
		List<TaskType> upTasks = new ArrayList<>();
		for(int i = 0; i < tsize; i++){ 
			TaskType ctask = saveTask(audit,importMap,user,nTask,task.getChildTasks().get(i),sessionId);
			if(ctask != null) upTasks.add(ctask);
		}
		return nTask;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T saveType(AuditType audit,AuditEnumType aType, FactoryEnumType fType, ImportMap importMap, UserType user, T obj,String sessionId) throws ArgumentException, FactoryException, DataAccessException, DataException{

		if(obj == null){
			logger.info("Null object");
			return null;
		}
		NameIdDirectoryGroupType dObj = (NameIdDirectoryGroupType)obj;
		BaseService.normalize(user, dObj);
		DirectoryGroupType objDir = BaseService.find(AuditEnumType.GROUP, "DATA", dObj.getGroupPath(), user);
		if(objDir == null){
			logger.error("Null directory object: " + dObj.getGroupPath());
			return null;
		}
		if(AuthorizationService.canChange(user, objDir) == false){
			AuditService.denyResult(audit, "Not authorized to alter group " + objDir.getUrn());
			return null;
		}
		NameIdGroupFactory fact = Factories.getFactory(fType);
		NameIdDirectoryGroupType comp = fact.getByNameInGroup(dObj.getName(), dObj.getParentId(), objDir.getId());
		if(comp != null && comp.getId().compareTo(dObj.getId()) != 0){
			logger.warn("Assigning a suffix to " + dObj.getNameType().toString() + " " + dObj.getName() + ". Id conflict is " + comp.getId() + "<>" + dObj.getId());
			dObj.setName(dObj.getName() + " - " + System.currentTimeMillis());
		}
		
		if(dObj.getId().compareTo(0L) > 0){
			if(sessionId != null) BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, fType, (NameIdType)obj);
			else if(!BaseService.update(aType, obj, user)){
				((NameIdFactory)Factories.getFactory(fType)).removeFromCache((NameIdType)obj);
				logger.error("Failed to update " + fType.toString() + " object with id #" + dObj.getId());
				obj = null;
			}
		}
		else if(sessionId != null){
			NameIdType nobj = (NameIdType)obj;
			ITypeSanitizer sanitizer = Factories.getSanitizer(NameEnumType.valueOf(fType.toString()));
			obj = (nobj.getUrn() != null && importMap.urnMap.containsKey(nobj.getUrn()) ? (T)importMap.urnMap.get(nobj.getUrn()) : sanitizer.sanitizeNewObject(AuditEnumType.fromValue(fType.toString()), user, obj));
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, fType, (NameIdType)obj);
		}
		else if(BaseService.add(aType, obj, user)){
			obj = fact.getByNameInGroup(dObj.getName(), dObj.getParentId(),objDir);
			if(obj == null){
				logger.error("Failed to retrieve saved object");
				logger.error(JSONUtil.exportObject(dObj));
			}
		}
		else{
			logger.error("Failed to update object " + dObj.getName());
			obj = null;
		}
		return obj;
	}

	private static EstimateType saveEstimate(AuditType audit,UserType user, NameIdDirectoryGroupType relObj, EstimateType estimate,String sessionId) throws ArgumentException, FactoryException, DataAccessException, DataException{
		if(estimate == null) return null;
		if(estimate.getName() != null && estimate.getName().trim().length() == 0 && relObj != null){
			estimate.setName(relObj.getName() + " Estimate");
		}
		return saveType(audit, AuditEnumType.ESTIMATE, FactoryEnumType.ESTIMATE, null, user, estimate, sessionId);
	}
	private static ScheduleType saveSchedule(AuditType audit,UserType user, NameIdDirectoryGroupType relObj, ScheduleType schedule,String sessionId) throws ArgumentException, FactoryException, DataAccessException, DataException{
		if(schedule == null) return null;
		if(schedule.getName() != null && schedule.getName().trim().length() == 0 && relObj != null){
			schedule.setName(relObj.getName() + " Schedule");
		}
		
		return saveType(audit, AuditEnumType.SCHEDULE, FactoryEnumType.SCHEDULE, null,user, schedule, sessionId);
	}
	private static BudgetType saveBudget(AuditType audit,UserType user, BudgetType budget,String sessionId) throws ArgumentException, FactoryException, DataAccessException, DataException{
		return saveType(audit, AuditEnumType.BUDGET, FactoryEnumType.BUDGET, null,user, budget, sessionId);
	}
	
	public static <T> void deepPopulate(AuditEnumType aType, T obj, UserType user){
		try {
			NameIdFactory fact = Factories.getFactory(FactoryEnumType.fromValue(aType.toString()));
			fact.depopulate(obj);
			populate(aType, obj, user, true);
		} catch (ArgumentException | FactoryException e) {
			
			logger.error("Error",e);
		}
	}
	public static <T> void populateList(AuditEnumType aType, List<T> list, UserType user, boolean deep) throws ArgumentException{
		for(int i = 0; i < list.size();i++){
			populate(aType, list.get(i), user, deep);
		}
	}
	public static <T> void populate(AuditEnumType type,T object, UserType user) throws ArgumentException{
		populate(type, object, user, false);
	}
	public static <T> void populate(AuditEnumType type,T object, UserType user, boolean deep) throws ArgumentException{
		if(object == null) return;

		try {
			INameIdFactory iFact = BaseService.getFactory(type);

			iFact.populate(object);
			if(deep){
				switch(type){
					case PROJECT:
						ProjectType p = (ProjectType)object;
						populateList(AuditEnumType.ARTIFACT, p.getArtifacts(),user,deep);
						populateList(AuditEnumType.MODEL,p.getBlueprints(), user, deep);
						populateList(AuditEnumType.ARTIFACT,p.getDependencies(), user, deep);
						populateList(AuditEnumType.MODULE,p.getModules(), user, deep);
						populateList(AuditEnumType.REQUIREMENT,p.getRequirements(), user, deep);
						populateList(AuditEnumType.STAGE,p.getStages(), user, deep);
						populate(AuditEnumType.SCHEDULE,p.getSchedule(),user, deep);
	
						break;
					case STAGE:

						StageType s = (StageType)object;
						populate(AuditEnumType.WORK, s.getWork(),user,deep);
						populate(AuditEnumType.BUDGET,s.getBudget(),user,deep);
						populate(AuditEnumType.METHODOLOGY,s.getMethodology(),user,deep);
						populate(AuditEnumType.SCHEDULE,s.getSchedule(),user,deep);
					
						break;
					case WORK:

						WorkType w = (WorkType)object;
						populateList(AuditEnumType.ARTIFACT, w.getArtifacts(),user,deep);
						populateList(AuditEnumType.ARTIFACT,w.getDependencies(), user, deep);
						populateList(AuditEnumType.RESOURCE, w.getResources(),user,deep);
						populateList(AuditEnumType.TASK,w.getTasks(),user,deep);
						break;
						
					case TASK:
						TaskType t = (TaskType)object;
						populateList(AuditEnumType.ARTIFACT, t.getArtifacts(),user,deep);
						populateList(AuditEnumType.RESOURCE, t.getResources(),user,deep);
						populateList(AuditEnumType.TASK,t.getChildTasks(),user,deep);
						populateList(AuditEnumType.ARTIFACT,t.getDependencies(), user, deep);
						populateList(AuditEnumType.NOTE,t.getNotes(),user,deep);
						populateList(AuditEnumType.REQUIREMENT,t.getRequirements(),user,deep);
						populateList(AuditEnumType.WORK, t.getWork(),user,deep);
						populate(AuditEnumType.ESTIMATE,t.getEstimate(),user,deep);
						break;
					default:
						logger.error(String.format(FactoryException.UNHANDLED_TYPE,type.toString()));
						break;
				}
			}
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		}
		
	}
	
}
