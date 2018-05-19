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
package org.cote.rocket.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.factory.BulkFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.util.UrnUtil;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.propellant.objects.ArtifactType;
import org.cote.propellant.objects.CostType;
import org.cote.propellant.objects.EstimateType;
import org.cote.propellant.objects.ProjectType;
import org.cote.propellant.objects.ResourceType;
import org.cote.propellant.objects.ScheduleType;
import org.cote.propellant.objects.StageType;
import org.cote.propellant.objects.TaskType;
import org.cote.propellant.objects.TimeType;
import org.cote.propellant.objects.WorkType;
import org.cote.propellant.objects.types.ArtifactEnumType;
import org.cote.propellant.objects.types.CurrencyEnumType;
import org.cote.propellant.objects.types.EstimateEnumType;
import org.cote.propellant.objects.types.ResourceEnumType;
import org.cote.propellant.objects.types.TimeEnumType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.RocketModel;
import org.cote.rocket.factory.ArtifactFactory;
import org.cote.rocket.factory.CostFactory;
import org.cote.rocket.factory.EstimateFactory;
import org.cote.rocket.factory.ProjectFactory;
import org.cote.rocket.factory.ResourceFactory;
import org.cote.rocket.factory.ScheduleFactory;
import org.cote.rocket.factory.StageFactory;
import org.cote.rocket.factory.TaskFactory;
import org.cote.rocket.factory.TimeFactory;
import org.cote.rocket.factory.WorkFactory;

import net.sf.mpxj.MPXJException;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.Relation;
import net.sf.mpxj.Resource;
import net.sf.mpxj.ResourceAssignment;
import net.sf.mpxj.Task;
import net.sf.mpxj.mpp.MPPReader;

public class ProjectImportUtil {
	public static final Logger logger = LogManager.getLogger(ProjectImportUtil.class);
	public static boolean importProject(UserType owner, String projectFileName, boolean sprintEmulator) throws FileNotFoundException{
		try {
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(owner);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return importProject(owner,null,projectFileName,sprintEmulator);
	}
	
	public static boolean importProject(UserType owner, ProjectType proj, String projectFileName, boolean sprintEmulator){
		File f = new File(projectFileName);
		boolean outBool = false;
		if(f.exists() == false){
			logger.error("File '" + projectFileName + "' does not exist");
			return false;
		}
		
		try{
			outBool = importProject(owner,proj,f.getName(),new FileInputStream(f),sprintEmulator);
		}
		catch(FileNotFoundException fne){
			logger.error(fne.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fne);
		}
		return outBool;

	}
	public static boolean importProject(UserType owner, ProjectType proj, String dataName,InputStream projectStream, boolean sprintEmulator){
		boolean outBool = false;
		BulkFactory bulkFactory = BulkFactories.getBulkFactory();
		String sessionId = bulkFactory.newBulkSession();
		
		int dupCounter = 1;
		ProjectFile projectFile;
		MPPReader reader = new MPPReader();
		Set<String> taskNames = new HashSet<String>();
		try {

			DirectoryGroupType parentDir = null;
			if(proj != null) parentDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(proj.getGroupId(),proj.getOrganizationId());
			else{
				parentDir = owner.getHomeDirectory();
				proj = ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).newProject(owner, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(owner, "Projects",parentDir,owner.getOrganizationId()).getId());
				proj.setName(dataName);
				bulkFactory.createBulkEntry(sessionId, FactoryEnumType.PROJECT, proj);
			}
			
			HashMap<String,ResourceType> recMap = new HashMap<String,ResourceType>();

			HashMap<Integer,NameIdDirectoryGroupType> objMap = new HashMap<Integer,NameIdDirectoryGroupType>();
			HashMap<String,EstimateType> estMap = new HashMap<String,EstimateType>();
			DirectoryGroupType estDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(owner, "Estimates",parentDir, owner.getOrganizationId());
			DirectoryGroupType tiDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(owner, "Times",parentDir, owner.getOrganizationId());
			DirectoryGroupType coDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(owner, "Costs",parentDir, owner.getOrganizationId());
			DirectoryGroupType shDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(owner, "Schedules",parentDir, owner.getOrganizationId());
			DirectoryGroupType aDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(owner, "Artifacts",parentDir, owner.getOrganizationId());
			DirectoryGroupType gDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(owner, "Stages",parentDir, owner.getOrganizationId());
			DirectoryGroupType wDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(owner, "Work",parentDir, owner.getOrganizationId());
			DirectoryGroupType sDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(owner, "Stories",parentDir, owner.getOrganizationId());
			DirectoryGroupType tDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(owner, "Tasks",parentDir, owner.getOrganizationId());
			DirectoryGroupType rDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(owner, "Resources",parentDir, owner.getOrganizationId());	
			
			TimeType chkTime = ((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("Default Period", tiDir);
			if(chkTime == null){
				RocketModel.addTime(owner, sessionId, "Default Period",TimeEnumType.HOUR,1,tiDir.getId());
				chkTime = ((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("Default Period", tiDir);
			}
			CostType chkCost = ((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Default Cost", coDir);
			if(chkCost == null){
				RocketModel.addCost(owner, sessionId, "Default Cost", CurrencyEnumType.USD, 75.00, coDir.getId());
				chkCost = ((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Default Cost", coDir);
			}
			EstimateType defRecEstimate = ((EstimateFactory)Factories.getFactory(FactoryEnumType.ESTIMATE)).getByNameInGroup("Default Resource Rate", estDir);
			if(defRecEstimate == null){
				RocketModel.addEstimate(owner, sessionId, "Default Resource Rate", "Resource estimate per hour", EstimateEnumType.SWAG, (TimeType)((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup("Default Period", tiDir), (CostType)((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup("Default Cost", coDir), estDir.getId());
				defRecEstimate = ((EstimateFactory)Factories.getFactory(FactoryEnumType.ESTIMATE)).getByNameInGroup("Default Resource Rate", estDir);
			}

			projectFile = reader.read(projectStream);	
			List<Resource> recs = projectFile.getAllResources();
			for(int i = 0; i < recs.size();i++){
				Resource r = recs.get(i);
				if(r == null) continue;
				String name = r.getName();
				if(name == null) continue;
				/// Capture all resources defined for the project,
				/// not just those specified at a task level
				///
				List<ResourceType> rlist = getBulkResourceList(sessionId,owner,rDir,recMap, name,defRecEstimate);

			} // end for i
			List<Task> tasks = projectFile.getAllTasks();


			StageType lastStage = null;

			TaskType lastTask = null;
			HashMap<Integer,TaskType> lastTaskLevel = new HashMap<Integer,TaskType>();
			int sIter = 1;
			int yIter = 1;
			int tIter = 1;
			logger.info("Processing " + tasks.size() + " Project Tasks");
			Date minDate = null;
			Date maxDate = null;
			for(int i = 0; i < tasks.size();i++){
				long startTime = System.currentTimeMillis();
				int pSize = 0;
				Task task = tasks.get(i);

				String taskName = task.getName();
				/// build up stage/work
				///
				int outlineLevel = 0;
				if(task.getOutlineLevel()!=null) outlineLevel = task.getOutlineLevel();
				
				if(taskName == null || taskName.length() == 0){
					logger.debug("Skip empty task at id " + task.getID());
					continue;
				}
				taskName = taskName.trim();
				Date start = task.getStart();
				Date end = task.getFinish();
				if(minDate == null || minDate.getTime() > start.getTime()) minDate = start;
				if(maxDate == null || maxDate.getTime() < end.getTime()) maxDate = end;

				if(outlineLevel <= 0){
					logger.debug("Skip zero level: '" + taskName + "'");
					continue;
				}
				else if(outlineLevel == 1){
					yIter = 1;

						//taskName = task.getName();
						if(taskNames.contains(UrnUtil.getNormalizedString(taskName))) taskName = task.getName() + "-" + (dupCounter++);
						else taskNames.add(UrnUtil.getNormalizedString(taskName));
						StageType stage = ((StageFactory)Factories.getFactory(FactoryEnumType.STAGE)).newStage(owner, gDir.getId());
						stage.setName(taskName);
						stage.setDescription(task.getName());
						stage.setLogicalOrder(sIter++);
						
						ScheduleType schedule = ((ScheduleFactory)Factories.getFactory(FactoryEnumType.SCHEDULE)).newSchedule(owner, shDir.getId());
						schedule.setStartTime(CalendarUtil.getXmlGregorianCalendar(start));
						schedule.setEndTime(CalendarUtil.getXmlGregorianCalendar(end));
						schedule.setName(taskName);
						
						bulkFactory.createBulkEntry(sessionId, FactoryEnumType.SCHEDULE, schedule);
						
						stage.setSchedule(schedule);
						
						WorkType work = ((WorkFactory)Factories.getFactory(FactoryEnumType.WORK)).newWork(owner, wDir.getId());
						work.setName(taskName);
						work.setLogicalOrder(1);
						List<ResourceAssignment> recList = task.getResourceAssignments();
						for(int r = 0; r < recList.size();r++){
							work.getResources().addAll(getBulkResourceList(sessionId,owner,rDir,recMap,recList.get(r).getResource().getName(),defRecEstimate));
						}
						
						bulkFactory.createBulkEntry(sessionId, FactoryEnumType.WORK, work);

						stage.setWork(work);
						bulkFactory.createBulkEntry(sessionId, FactoryEnumType.STAGE, stage);
						proj.getStages().add(stage);
						objMap.put(task.getID(), stage);

					lastStage = stage;
				}
				
				/// >= 3
				else if(outlineLevel >= 2 || task.getName().matches("(?:iteration)")){

						//taskName = task.getName();
						if(taskNames.contains(UrnUtil.getNormalizedString(taskName))) taskName = task.getName() + "-" + (dupCounter++);
						else taskNames.add(UrnUtil.getNormalizedString(taskName));

						TaskType tsk = ((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).newTask(owner, (sprintEmulator && outlineLevel == 2 ? sDir : tDir).getId());
						tsk.setName(taskName);
						tsk.setDescription(task.getName());
						tsk.setLogicalOrder(tIter++);
						tsk.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(task.getStart()));
						tsk.setDueDate(CalendarUtil.getXmlGregorianCalendar(task.getFinish()));
						
						String durStr = task.getDuration().toString();
						if(durStr.length() > 0){
							if(estMap.containsKey(durStr)){
								tsk.setEstimate(estMap.get(durStr));
							}
							else{
								TimeType tt = ((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).newTime(owner, tiDir.getId());
								tt.setName(durStr);
								tt.setBasisType(TimeEnumType.DAY);
								tt.setValue(0.0);
								try{
									//tt.setValue((new BigDecimal(durStr.split(" ")[0])).doubleValue());
									tt.setValue(Double.parseDouble(durStr.split(" ")[0]));
									tt.setValue((Math.round(tt.getValue() * 100.0)/100.0));
								}
								catch(NumberFormatException nfe){
									logger.error(nfe.getMessage());
								}

								
								bulkFactory.createBulkEntry(sessionId, FactoryEnumType.TIME, tt);

								EstimateType est = ((EstimateFactory)Factories.getFactory(FactoryEnumType.ESTIMATE)).newEstimate(owner, estDir.getId());
								est.setName(durStr);
								est.setTime(tt);
								est.setEstimateType(EstimateEnumType.SWAG);

								bulkFactory.createBulkEntry(sessionId, FactoryEnumType.ESTIMATE, est);
								
								tsk.setEstimate(est);
								estMap.put(durStr, est);
								
							}
						}
						else{
							logger.debug("Skip empty duration");
						}
						
						List<ResourceAssignment> recList = task.getResourceAssignments();
						for(int r = 0; r < recList.size();r++){
							ResourceAssignment rec = recList.get(r);
							if(rec == null || rec.getResource() == null || rec.getResource().getName() == null) continue;
							tsk.getResources().addAll(getBulkResourceList(sessionId,owner,rDir,recMap,rec.getResource().getName(),defRecEstimate));
						}

						if(sprintEmulator && outlineLevel==2){
							WorkType work = ((WorkFactory)Factories.getFactory(FactoryEnumType.WORK)).newWork(owner, wDir.getId());
							work.setName(taskName);
							work.setLogicalOrder(tsk.getLogicalOrder());
							bulkFactory.createBulkEntry(sessionId, FactoryEnumType.WORK, work);
							//((WorkFactory)Factories.getFactory(FactoryEnumType.WORK)).addWork(work);
							//work = ((WorkFactory)Factories.getFactory(FactoryEnumType.WORK)).getByNameInGroup(taskName, wDir);
							tsk.getWork().add(work);
						}
						
						bulkFactory.createBulkEntry(sessionId, FactoryEnumType.TASK, tsk);

						objMap.put(task.getID(), tsk);

					lastTask = tsk;
				
					/// == 3
					if(outlineLevel == 2){
						lastStage.getWork().getTasks().add(tsk);
						//upMap.put(lastStage.getWork().getId(),lastStage.getWork());
					}
					else if(lastTaskLevel.containsKey(outlineLevel-1)){
						TaskType parentTask = lastTaskLevel.get(outlineLevel-1);
						
						if(sprintEmulator){
							TaskType sprintTask = lastTaskLevel.get(2);
							sprintTask.getWork().get(0).getTasks().add(tsk);
							//upMap.put(sprintTask.getWork().get(0).getId(), sprintTask.getWork().get(0));
						}

						tsk.setParentId(parentTask.getId());
						/// Nuke the parent task estimate
						
						//parentTask.setEstimate(null);
						//upMap.put(tsk.getId(), tsk);
						
					}
					if(lastTaskLevel.containsKey(outlineLevel)){
						lastTaskLevel.remove(outlineLevel);
					}
					lastTaskLevel.put(outlineLevel, tsk);
				}
				else{
					logger.warn("Unhandled level: " + outlineLevel);
				}
				
				if(task.getPredecessors() != null) pSize = task.getPredecessors().size();
				if(pSize > 0){
					List<Relation> rel = task.getPredecessors();
					for(int r = 0; r < pSize;r++){
						Task pTask = rel.get(r).getTargetTask();

						if(pTask.getID() == task.getID()){
							logger.warn("Skipping circular dependency reference");
							continue;
						}
						if(pTask.getName().equals(task.getName())){
							logger.warn("Dependency has the same name as the source task");
						}
						if(objMap.containsKey(pTask.getID()) == true){
							NameIdDirectoryGroupType obj = objMap.get(pTask.getID());
							ArtifactEnumType depType = ArtifactEnumType.UNKNOWN;
							long depId = 0;
							String depName = null;
							switch(obj.getNameType()){
								case STAGE:
									StageType pstage = (StageType)obj;
									depName = UrnUtil.getUrn(obj);//pstage.getName();
									depId = pstage.getId();
									depType = ArtifactEnumType.WORK;

									break;
								case TASK:
									TaskType ptask = (TaskType)obj;
									depName = UrnUtil.getUrn(obj);//ptask.getName();
									depId = ptask.getId();
									depType = (outlineLevel == 2 ? ArtifactEnumType.STORY : ArtifactEnumType.TASK);


									break;
								default:
									logger.error("Unhandled type: " + obj.getNameType() + " for " + obj.getName());
									break;
							}
							ArtifactType art = ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).getByNameInGroup(depName, aDir);
							if(art == null){
								art = ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).newArtifact(owner, aDir.getId());
								art.setName(depName);
								art.setArtifactDataId(depId);
								art.setArtifactType(depType);
								bulkFactory.createBulkEntry(sessionId, FactoryEnumType.ARTIFACT, art);
							}
							if(outlineLevel == 1){
								if(lastStage.getId().compareTo(art.getArtifactDataId())==0){
									logger.warn("Object is its own dependency");
									continue;
								}
								lastStage.getWork().getDependencies().add(art);
								logger.debug("Adding dependency " + art.getName() + " to stage " + lastStage.getName());
							}
							else{
								TaskType dtask = lastTask;
								if(dtask.getId().compareTo(art.getArtifactDataId())==0){
									logger.warn("Object is its own dependency");
									continue;
								}
								dtask.getDependencies().add(art);
								logger.debug("Adding dependency " + art.getName() + " to task " + dtask.getName());
							}
							break;
						}
						else{
							logger.warn("Dependency to id " + task.getID() + " not found");
						}
					}
				}
				logger.debug("Created Task '" + taskName + " in " + (System.currentTimeMillis() - startTime) + "ms");
			} // end for
			if(minDate != null && maxDate != null){
				ScheduleType sched = ((ScheduleFactory)Factories.getFactory(FactoryEnumType.SCHEDULE)).newSchedule(owner, shDir.getId());
				sched.setName(proj.getName());
				sched.setStartTime(CalendarUtil.getXmlGregorianCalendar(minDate));
				sched.setEndTime(CalendarUtil.getXmlGregorianCalendar(minDate));
				bulkFactory.createBulkEntry(sessionId, FactoryEnumType.SCHEDULE, sched);
				proj.setSchedule(sched);
				
			}
			bulkFactory.write(sessionId);
			bulkFactory.close(sessionId);
			outBool = ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).update(proj);

		}catch (MPXJException e) {
			logger.error(e.getMessage());
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			logger.error(e.getMessage());
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			logger.error(e.getMessage());
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			logger.error(e.getMessage());
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} 
		return outBool;
	}
	
	
	private static List<ResourceType> getBulkResourceList(String sessionId, UserType owner, DirectoryGroupType recDir, HashMap<String,ResourceType> recMap, String name, EstimateType defEstimate){
		List<ResourceType> rlist = new ArrayList<ResourceType>();

		try{
	
			String rname = name.replaceAll("&",", ").replaceAll(" and ", ", ");
			String[] rnames = rname.split(",");
			
			for(int p = 0; p < rnames.length;p++){
				String rnameu = rnames[p].trim();
				
				if(rnameu.length() == 0) continue;
				String lrname = UrnUtil.getNormalizedString(rnameu);
				if(recMap.containsKey(lrname)){
					rlist.add(recMap.get(lrname));
					continue;
				}
				
				ResourceType rt = ((ResourceFactory)Factories.getFactory(FactoryEnumType.RESOURCE)).getByNameInGroup(rnameu, recDir);
				if(rt != null){
					rlist.add(rt);
					recMap.put(lrname, rt);
					logger.info("Skip existing resource");
					continue;
				}
				rt = ((ResourceFactory)Factories.getFactory(FactoryEnumType.RESOURCE)).newResource(owner, recDir.getId());
				rt.setName(rnameu);
				rt.setResourceType(ResourceEnumType.USER);
				rt.setUtilization(100.0);
				rt.setEstimate(defEstimate);
				
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.RESOURCE, rt);
				rlist.add(rt);
				recMap.put(lrname, rt);
	
			} // end for p
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			logger.error(e.getMessage());
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return rlist;
	}

}
