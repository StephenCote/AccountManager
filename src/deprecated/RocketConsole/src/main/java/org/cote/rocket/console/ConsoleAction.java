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
package org.cote.rocket.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.cote.rocket.Factories;
import org.cote.rocket.Rocket;
import org.cote.rocket.RocketSecurity;
import org.cote.accountmanager.data.factory.*;
import org.cote.rocket.factory.*;
public class ConsoleAction {
	public static final Logger logger = LogManager.getLogger(ConsoleAction.class);
	private DirectoryGroupType currentDirectory = null;
	private LifecycleType currentLifecycle = null;
	private ProjectType currentProject = null;
	//private TaskType currentTask = null;
	//private StageType currentStage = null;
	//private TaskType currentStory = null;
	private Map<String,NameIdDirectoryGroupType> currentObjects = new HashMap<String,NameIdDirectoryGroupType>();
	private boolean sprintMode = false;

	/// Pattern and accompanying match code in parseLine based on StackOverflow - http://stackoverflow.com/questions/366202/regex-for-splitting-a-string-using-space-when-not-surrounded-by-single-or-double
	///
	private Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
	private String prompt = null;
	private void setPrompt(UserType user){
		String lifeName = "";
		String projName = "";
		String objName = "";
		if(currentLifecycle != null) lifeName = ":" + currentLifecycle.getName();
		if(currentProject != null) projName = ":" + currentProject.getName();
		if(currentObjects.containsKey(currentDirectory.getName())) objName = ":" + currentObjects.get(currentDirectory.getName()).getName();
		///if(currentStage != null) stgName = ":" + currentStage.getName();
		OrganizationType org = null;
		try {
			org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationById(user.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		prompt = "[" + org.getName() + lifeName + projName + objName + "]:" + currentDirectory.getPath() + " " + user.getName() + "$ ";
	}
	private void setCurrentDirectory(DirectoryGroupType dir) throws FactoryException, ArgumentException{
		((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(dir);
		((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(dir);
		currentDirectory = dir;

	}
	public String[] parseLine(String line){
		List<String> matchList = new ArrayList<String>();
		
		Matcher regexMatcher = regex.matcher(line);
		while (regexMatcher.find()) {
		    if (regexMatcher.group(1) != null) {
		        // Add double-quoted string without the quotes
		        matchList.add(regexMatcher.group(1));
		    } else if (regexMatcher.group(2) != null) {
		        // Add single-quoted string without the quotes
		        matchList.add(regexMatcher.group(2));
		    } else {
		        // Add unquoted word
		        matchList.add(regexMatcher.group());
		    }
		} 
		return matchList.toArray(new String[0]);
	}
	public void runConsole(UserType user){
		BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
		try{

			Factories.getNameIdFactory(FactoryEnumType.USER).populate(user);
			if(currentDirectory == null){
				setCurrentDirectory(user.getHomeDirectory());
			}
			if(prompt == null){
				setPrompt(user);
			}
			String line = "";
			
			while (line.equalsIgnoreCase("quit") == false && line.equalsIgnoreCase("exit") == false) {
				System.out.print(prompt);
				line = is.readLine();
				String[] linePar = parseLine(line);
				handleCommandLine(user, linePar);
				///logger.info(line);
			    
			}
	
			   is.close();
			}
		catch(IOException e){
			logger.error(e.getMessage());
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}

	}
	
	public void handleCommandLine(UserType user, String[] line) throws ArgumentException, FactoryException{
		if(line.length == 0){
			return;
		}
		/*
		for(int i = 0; i < line.length;i++){
			System.out.println("DEBUG: #" + (i + 1) + ": '" + line[i] + "'");
		}
		*/
		if(line.length == 1){
			if(line[0].equals("lifecycles")){
				DirectoryGroupType targDir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA,"/Rocket/Lifecycles", user.getOrganizationId());
				if(targDir == null){
					System.out.println("Invalid path");
				}
				else{
					setCurrentDirectory(targDir);
					setPrompt(user);
				}
			}
			else if(line[0].equals("projects") && currentLifecycle != null){
				setCurrentDirectory(Rocket.getProjectGroup(currentLifecycle));
				setPrompt(user);
			}
			else if(line[0].equals("list")){
				if(currentDirectory.getName().equals("Lifecycles")){
					printList(user, "lifecycles");
				}
				else if(currentDirectory.getName().equals("Projects") && currentLifecycle != null){
					printList(user,"projects");
				}
				/*
				else if(currentDirectory.getName().equals("Stages") && currentLifecycle != null){
					printList(user,"stages");
				}
				*/
				else if(getTypeFromGroupName(currentDirectory.getName()) != FactoryEnumType.UNKNOWN){
					printList(user,currentDirectory.getName());
				}
			}
			else if(currentDirectory.getName().equals("Lifecycles")){
				List<DirectoryGroupType> dirs = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryGroups(Rocket.getLifecycleGroup(user.getOrganizationId()));
				String lifeName = null;
				for(int i = 0; i < dirs.size();i++){
					if(dirs.get(i).getName().equals(line[0])){
						lifeName = line[0];
						break;
					}
				}
				if(lifeName != null){
					currentLifecycle = Rocket.getLifecycle(lifeName, user.getOrganizationId());
					((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).populate(currentLifecycle);
					//setCurrentDirectory(Rocket.getProjectGroup(currentLifecycle));
					setCurrentDirectory(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(currentLifecycle.getGroupId(),currentLifecycle.getOrganizationId()));
					setPrompt(user);
				}
				else{
					System.out.println("Lifecycle '" + line[0] + "' not found in group '" + Rocket.getLifecycleGroup(user.getOrganizationId()).getName() + "'");
				}
			}
			else if(currentDirectory.getName().equals("Projects") && currentLifecycle != null){
				List<DirectoryGroupType> dirs = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryGroups(Rocket.getProjectGroup(currentLifecycle));
				String projName = null;
				for(int i = 0; i < dirs.size();i++){
					if(dirs.get(i).getName().equals(line[0])){
						projName = line[0];
						break;
					}
				}
				if(projName != null){
					currentProject = Rocket.getProject(projName, currentLifecycle, user.getOrganizationId());
					((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).populate(currentProject);
					setCurrentDirectory(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(currentProject.getGroupId(),currentProject.getOrganizationId()));
					setPrompt(user);
				}
				else{
					System.out.println("Lifecycle '" + line[0] + "' not found in group '" + Rocket.getLifecycleGroup(user.getOrganizationId()).getName() + "'");
				}
			}
			/// shorthand change directory
			//else if((line[0].equals("stages") || line[0].equals("sprints")) && currentProject != null && currentLifecycle != null){
			else if(currentProject != null && currentLifecycle != null && getTypeFromGroupName(line[0]) != FactoryEnumType.UNKNOWN){
				
				FactoryEnumType factType = getTypeFromGroupName(line[0]);
				NameIdGroupFactory fact = Factories.getFactory(factType);
				/*
				if(line[0].equals("stages")){
					System.out.println("Viewing stages in non-sprint mode");
					sprintMode = false;
				}
				else if(line[0].equals("sprints")){
					System.out.println("Viewing stages in sprint mode. Use sprint mode to create top-level tasks as psuedo-stories.");
					sprintMode = true;
				}
				*/
				setCurrentDirectory(RocketSecurity.getProjectDirectory(user, currentProject, line[0].substring(0,1).toUpperCase() + line[0].substring(1,line[0].length())));
				setPrompt(user);
			}
			/// Select active object by name
			///else if(currentDirectory.getName().equals("Stages") && currentLifecycle != null && currentProject != null){
			else if(currentLifecycle != null && currentProject != null && getTypeFromGroupName(currentDirectory.getName()) != FactoryEnumType.UNKNOWN){

				FactoryEnumType factType = getTypeFromGroupName(currentDirectory.getName());
				NameIdGroupFactory fact = Factories.getFactory(factType);
				if(fact == null){
					logger.error("Null factory for type " + factType.toString());
					return;
				}
				List<NameIdDirectoryGroupType> objs = fact.listInGroup(currentDirectory, 0, 0, currentDirectory.getOrganizationId());
				String objName = null;
				for(int i = 0; i < objs.size();i++){
					if(objs.get(i).getName().equals(line[0])){
						objName = line[0];
						break;
					}
				}
				if(objName != null){
					NameIdDirectoryGroupType obj = fact.getByNameInGroup(objName, currentDirectory);
					Factories.populate(factType,obj);
					currentObjects.put(currentDirectory.getName(), obj);
					setPrompt(user);
				}
				else{
					System.out.println("Lifecycle '" + line[0] + "' not found in group '" + Rocket.getLifecycleGroup(user.getOrganizationId()).getName() + "'");
				}
			}
		}
		if(line.length >= 2){
			if(line[0].equals("add")){
				if(currentDirectory.getName().equals("Lifecycles")){
					RocketAction.addLifecycle(user, line[1]);
					LifecycleType lc = Rocket.getLifecycle(line[1],user.getOrganizationId());
					if(lc != null){
						System.out.println("Created lifecycle '" + lc.getName() + "'");
						currentLifecycle = lc;
						currentProject = null;
						setCurrentDirectory(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(lc.getGroupId(),lc.getOrganizationId()));
						setPrompt(user);
					}
				}
				else if(currentDirectory.getName().equals("Projects") && currentLifecycle != null){
					RocketAction.addProject(user, currentLifecycle.getName(), line[1]);
					ProjectType proj = Rocket.getProject(line[1], currentLifecycle, user.getOrganizationId());
					if(proj != null){
						System.out.println("Created project '" + line[1] + "'");
						currentProject = proj;
						setCurrentDirectory(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(proj.getGroupId(),proj.getOrganizationId()));
						setPrompt(user);
					}
				}
				if(line[1].equals("agileMethod") && currentLifecycle != null && currentProject != null){
					RocketAction.addAgileMethodology(user, currentLifecycle.getName(), currentProject.getName());
					System.out.println("Added Agile Artifacts");
				}
				if(line[1].equals("waterfallMethod") && currentLifecycle != null && currentProject != null){
					RocketAction.addWaterfallMethodology(user, currentLifecycle.getName(), currentProject.getName());
					System.out.println("Added Waterfall Artifacts");
				}
				if(line.length >= 7 && line[1].equals("sprints") && currentLifecycle != null && currentProject != null){
					RocketAction.addAgileSprints(user, currentLifecycle.getName(), currentProject.getName(), line[2], Integer.parseInt(line[3]), Integer.parseInt(line[4]), line[5], line[6]);
				}
				//cmd.hasOption("addSprints") && cmd.hasOption("sprintLabel") && cmd.hasOption("sprintStart") && cmd.hasOption("sprintLength") && cmd.hasOption("sprints")){
			}
			else if(line[0].equals("cd")){
				DirectoryGroupType targDir = null;
				if(line[1].equals("..")){
					if(currentDirectory.getParentId() > 0L) targDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(currentDirectory.getParentId(), user.getOrganizationId());
				}
				else{
					String path = line[1];
					if(path.startsWith("~") == false && path.startsWith(".") == false && path.startsWith("/") == false){
						path = currentDirectory.getPath() + "/" + path;
					}
					targDir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, path, user.getOrganizationId());
				}
				if(targDir == null){
					System.out.println("Invalid path");
				}
				else{
					setCurrentDirectory(targDir);
					setPrompt(user);
				}
			}
			else if(line[0].equals("list")){
				printList(user, line[1]);
			}
		}
	}
	private FactoryEnumType getTypeFromGroupName(String groupName){
		FactoryEnumType out_type = FactoryEnumType.UNKNOWN;
		String keyName = groupName.substring(0,1).toUpperCase() + groupName.substring(1,groupName.length());
		if(Rocket.rocketGroupToTypeMap.containsKey(keyName)){
			out_type = Rocket.rocketGroupToTypeMap.get(keyName);
		}
		return out_type;
	}
	private void printList(UserType user, String name) throws FactoryException, ArgumentException{
		List<?> items = null;
		if(name.equals("groups")){
			items = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getListByParent(GroupEnumType.DATA,currentDirectory, 0, 0, user.getOrganizationId());
		}
		else if(name.equals("data")){
			items = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataListByGroup(currentDirectory, true, 0, 0, user.getOrganizationId());
		}
		else if(name.equals("lifecycles")){
			items = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryGroups(Rocket.getLifecycleGroup(user.getOrganizationId()));
		}
		else if(name.equals("projects")){
			items = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryGroups(Rocket.getProjectGroup(currentLifecycle));
		}
		else if(getTypeFromGroupName(name) != FactoryEnumType.UNKNOWN){
			FactoryEnumType factType = getTypeFromGroupName(name);
			NameIdGroupFactory fact = Factories.getFactory(factType);
			items = fact.listInGroup(currentDirectory, 0, 0, currentDirectory.getOrganizationId()); 
		}
		/*
		else if(name.equals("stages")){
			items = ((StageFactory)Factories.getFactory(FactoryEnumType.STAGE)).getListByGroup(currentDirectory, 0, 0, currentDirectory.getOrganization());
		}
		*/
		//System.out.println("Name\tType\tId");
		System.out.format("%30s%16s%10s%n", "Name","Type","Id");
		System.out.println("");
		for(int i = 0; items != null && i < items.size();i++){
			NameIdType item = (NameIdType)items.get(i);
			//System.out.println(item.getName() + "\t" + item.getNameType() + "\t" + item.getId());
			System.out.format("%30s%16s%10d%n", item.getName(), item.getNameType(), item.getId());
		}
		System.out.format("%n%30s%n",(items != null ? items.size() : 0) + " items");
	}
	
}
