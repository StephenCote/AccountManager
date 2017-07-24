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
/*
 * Repackaged from irocket.js
 */
(function(){
	/// Community mode uses the Rocket organized structure instead of the user's home folder for storing data
	///
	var communityMode = false;
	
	/// LifecycleScope, in community mode, means everything is shared between projects at the lifecycle level
	///
	var lifecycleScope = false;
	
	var autoCreate = false;
	var autoRead = false;
	var contextLifecycle = 0;
	var contextLifecycleGroup = 0;
	var contextProject = 0;
	var contextProjectGroup = 0;
	var dayInMs = (24 * 60 * 60 * 1000);
	/// placeholder for non-community reference
	var currentProject = 0;
	var currentRoleBucket = 0;
	var currentPermissionBucket = 0;

	var lifecyclePath,projectPath,stepsPath,processesPath,methodPath,stagesPath,workPath,taskPath,
		costPath,timePath,estimatePath,budgetPath,personPath,accountPath,resourcePath, dataPath,artifactPath,casePath,formPath,formElementPath
		,validationPath,goalPath,modelPath,modulePath,notePath,requirementPath,schedulePath,storyPath,ticketPath
		,policyPath,patternPath,rulePath,factPath,operationPath,functionPath,permissionPath,rolePath,
		contextBasePath,applicationPath,contactPath,addressPath,tagPath,eventPath,locationPath,traitPath
	;
	/// 
	function updateBase(){
		//communityMode = (b ? true : false);
		var basePath = (communityMode ? "/Rocket" : "~");
		lifecyclePath = basePath + "/Lifecycles";

		if(communityMode && contextLifecycleGroup){
			basePath = contextLifecycleGroup.path;
			projectPath = basePath + "/Projects";
			if(!lifecycleScope && contextProject){
				basePath = contextProjectGroup.path;
			}
		}
		else{
			projectPath = basePath + "/Projects";
		}
		contextBasePath = basePath;
		permissionPath = basePath;//(currentPermissionBucket ? accountManager.getPermissionPath(currentPermissionBucket) : basePath);
		rolePath =  basePath; //(currentRoleBucket ? accountManager.getRolePath(currentRoleBucket) : basePath);
		applicationPath = basePath + "/Applications";
		policyPath = basePath + "/Policies";
		patternPath = basePath + "/Patterns";
		rulePath = basePath + "/Rules";
		factPath = basePath + "/Facts";
		operationPath = basePath + "/Operations";
		functionPath = basePath + "/Functions";
		ticketPath = basePath + "/Tickets";
		storyPath = basePath + "/Stories";
		schedulePath = basePath + "/Schedules";
		requirementPath = basePath + "/Requirements";
		notePath = basePath + "/Notes";
		goalPath = basePath + "/Goals";
		validationPath = basePath + "/ValidationRules";
		formPath = basePath + "/Forms";
		formElementPath = basePath + "/FormElements";
		casePath = basePath + "/Cases";
		artifactPath = basePath + "/Artifacts";
		stepsPath = basePath + "/ProcessSteps";
		processesPath = basePath + "/Processes";
		methodPath = basePath + "/Methodologies";
		stagesPath = basePath + "/Stages";
		dataPath = basePath + "/Data";
		workPath = basePath + "/Work";
		taskPath = basePath + "/Tasks";
		costPath = basePath + "/Costs";
		timePath = basePath + "/Times";
		estimatePath = basePath + "/Estimates";
		budgetPath = basePath + "/Budgets";
		personPath = basePath + "/Persons";
		accountPath = basePath + "/Accounts";
		resourcePath = basePath + "/Resources";
		modelPath = basePath + "/Models";
		modulePath = basePath + "/Modules";
		contactPath = basePath + "/Contacts";
		addressPath = basePath + "/Addresses";
		tagPath = basePath + "/Tags";
		eventPath = basePath + "/Events";
		locationPath = basePath + "/Locations";
		traitPath = basePath + "/Traits";
	}
	function getDefaultParentByType(s,d){
		if(!communityMode || !contextProject) return d;
		var o = d;
		switch(s){
			case "Permission":
				o = rocket.getProjectPermissionBase(contextProject);
				break;
			case "Role":
				o = rocket.getProjectRoleBase(contextProject);
				break;
		}
		return o;
	}
	function getBasePathByType(s){
		switch(s){
			case "Address": return addressPath;
			case "Contact": return contactPath;
			case "Application": return applicationPath;
			case "Group": return contextBasePath;
			case "Ticket": return ticketPath; break;
			case "Story": return storyPath; break;
			case "Requirement": return requirementPath;break;
			case "Note": return notePath;break;
			case "Module": return modulePath;break;
			case "Model": return modelPath;break;
			case "Goal": return goalPath;break;
			case "ValidationRule": return validationPath;break;
			case "Form": return formPath;break;
			case "FormElement": return formElementPath;break;
			case "Case":return casePath;break;
			case "Artifact":return artifactPath;break;
			case "ProcessStep":return stepsPath;break;
			case "Process":return processesPath;break;
			case "Method":return methodPath;break;
			case "Stage":return stagesPath;break;
			case "Data":return dataPath;break;
			case "Work":return workPath;break;
			case "Task":return taskPath;break;
			case "Cost":return costPath;break;
			case "Time":return timePath;break;
			case "Estimate":return estimatePath;break;
			case "Budget":return budgetPath;break;
			case "Person":return personPath;break;
			case "Account":return accountPath;break;
			case "Resource":return resourcePath;break;
			case "Lifecycle":return lifecyclePath;break;
			case "Project":return projectPath;break;
			case "Schedule":return schedulePath;break;
			case "Methodology":return methodPath;break;
			case "Policy": return policyPath;break;
			case "Pattern": return patternPath;break;
			case "Rule": return rulePath;break;
			case "Fact": return factPath;break;
			case "Operation": return operationPath;break;
			case "Function": return functionPath;break;
			case "Permission": return permissionPath;break;
			case "Role": return rolePath;break;
			case "Tag": return tagPath;break;
			case "Event": return eventPath;break;
			case "Location": return locationPath;break;
			case "Trait": return traitPath;break;
		}
	}
	
	
	var oProjectGroup, oStepsGroup, oProcessesGroup, oMethodGroup, oStagesGroup, oWorkGroup, oTaskGroup, oCostGroup,oTimeGroup,oEstimateGroup,oBudgetGroup,oPersonGroup,oResourceGroup,oScheduleGroup;
	function updatePaths(){
		oProjectGroup = AM6Client.make("GROUP","DATA",getBasePathByType("Project"));
		oStepsGroup = AM6Client.make("GROUP","DATA",getBasePathByType("ProcessStep"));
		oProcessesGroup = AM6Client.make("GROUP","DATA",getBasePathByType("Process"));
		oMethodGroup = AM6Client.make("GROUP","DATA",getBasePathByType("Methodology"));
		oStagesGroup = AM6Client.make("GROUP","DATA",getBasePathByType("Stage"));
		oWorkGroup = AM6Client.make("GROUP","DATA",getBasePathByType("Work"));
		oTaskGroup = AM6Client.make("GROUP","DATA",getBasePathByType("Task"));
		oCostGroup = AM6Client.make("GROUP","DATA",getBasePathByType("Cost"));
		oTimeGroup = AM6Client.make("GROUP","DATA",getBasePathByType("Time"));
		oEstimateGroup = AM6Client.make("GROUP","DATA",getBasePathByType("Estimate"));
		oBudgetGroup = AM6Client.make("GROUP","DATA",getBasePathByType("Budget"));
		oPersonGroup = AM6Client.make("GROUP","DATA",getBasePathByType("Person"));
		oResourceGroup = AM6Client.make("GROUP","DATA",getBasePathByType("Resource"));
		oScheduleGroup = AM6Client.make("GROUP","DATA",getBasePathByType("Schedule"));
	}
	
	/// There are a few bugs here related to retrieving child tasks in the same group when multiple tasks of the same name exists
	/// the fix is to add readByParent APIs to the server and client
	/// In the meantime, the task name is prefaced with the parent name
	
	function importTaskModel(vJson, bOverwrite, sPref, oParent){
		if(!sPref) sPref = "";
		var aT = [];
		var aTasks = vJson.tasks;
		for(var i = 0; i < aTasks.length;i++){
			/// if not overwriting, just try to add
			/// this is due to a missing API to read the task by parentid
			var oTask = aTasks[i];
			//var sName = oTask.name;
			var sName = (sPref && sPref.length && !oParent ? sPref + " " : "") + oTask.name;
			var oT = (bOverwrite ? (oParent ? rocket.getTaskByParent(sName,oParent) : rocket.getTask(sName,oTaskGroup)) : 0);
			if(oT != null && bOverwrite && rocket.deleteTask(oT)) oT = 0;
			if(!oT){
				var oE = 0;
				if(oTask.estimate) oE = rocket.getEstimate(oTask.estimate,oEstimateGroup);
				var aR = [];
				if(oTask.resources){
					for(var r = 0;  r < oTask.resources.length;r++){
						var oRec = rocket.getResource(oTask.resources[r], oResourceGroup);
						if(oRec && oRec != null) aR.push(oRec);
					}
				}
				var oD = (oTask.due ? new Date(oTask.due) : new Date());
				var aD = [];
				/*
				 /// these are artifacts
				if(oTask.dependencies){
					for(var d = 0; d < oTask.dependencies.length;d++){
						var oDep = rocket.getTask(oTask.dependencies[d],oTaskGroup);
						if(oDep && oDep != null) aD.push(oDep);
					}
				}
				*/
				if(rocket.addTask(sName, oTask.description, (oE ? "ESTIMATED" : "UNPLANNED"), (i+1), oD, oD, oD, oE, 0, aD, 0, 0, 0, 0, 0, aR, (oParent ? oParent.id : 0),oTaskGroup)){
					if(oParent) oT = rocket.getTaskByParent(sName,oParent);
					else oT = rocket.getTask(sName,oTaskGroup);
					if(oTask.tasks){
						var aC = importTaskModel(oTask,bOverwrite,sPref,oT);
						/*
						for(var c = 0; c < aC.length;c++){
							aC[c].parentId = oT.id;
							rocket.updateTask(aC[c]);
						}
						*/
					}
				}

			}
			if(oT) aT.push(oT);
		}
		/// Flush the cache because the parent-child relationship may have changed
		/// and a cached parent won't be updated to reflect it has children
		///
		rocket.flushCache();
		return aT;
	}

	function importResourceModel(vJson){
		var bPer = vJson.generatePerson;
		for(var i = 0; i < vJson.resources.length;i++){
			var oP = 0;
			var oR = vJson.resources[i];
			var o = rocket.getResource(oR.name, oResourceGroup);
			if(o && o != null) continue;
			if(oR.person){
				oP = accountManager.getPerson(oR.person, oPersonGroup);
				if(!oP && accountManager.addPerson(oR.person, oPersonGroup)){
					oP = accountManager.getPerson(oR.person, oPersonGroup);			}
			}
			var oE = 0;
			if(oR.rate){
				var oC = rocket.getCost(oR.rate,oCostGroup);
				if(!oC && rocket.addCost(oR.rate,"USD",parseFloat(oR.rate),oCostGroup)){
					oC = rocket.getCost(oR.rate,oCostGroup);
				}
				oE = rocket.getEstimate(oR.rate,oEstimateGroup);
				if(!oE && rocket.addEstimate(oR.rate,"","FINE",0,oC,oEstimateGroup)){
					oE = rocket.getEstimate(oR.rate,oEstimateGroup);
				}
			}
			rocket.addResource(oR.name,oR.description,(oP ? "PERSON" : "UNKNOWN"),oR.utilization, (oP ? oP.id : 0), oE, oResourceGroup);
		}
	}

	function importEstimateModel(vJson){
	   for(var i = 0; i < vJson.estimates.length;i++){
		var x = rocket.getEstimate(vJson.estimates[i].name,oEstimateGroup);
		if(!x || x == null) rocket.addEstimate(vJson.estimates[i].name, vJson.estimates[i].description, vJson.estimates[i].estimateType, (vJson.estimates[i].timeName ? rocket.getTime(vJson.estimates[i].timeName,oTimeGroup) : 0), (vJson.estimates[i].costName ? rocket.getCost(vJson.estimates[i].costName,oCostGroup) : 0), oEstimateGroup);
	   }
	   return 1;
	}

	function importCostModel(vJson){
	   for(var i = 0; i < vJson.costs.length;i++){
		var x = rocket.getCost(vJson.costs[i].name,oCostGroup);
		if(!x || x == null) rocket.addCost(vJson.costs[i].name, vJson.costs[i].costType, vJson.costs[i].costValue,oCostGroup);
	   }
	}

	function importBudgetModel(vJson){
	   for(var i = 0; i < vJson.budgets.length;i++){
		var x = rocket.getBudget(vJson.budgets[i].name,oBudgetGroup);
		if(!x || x == null) rocket.addBudget(vJson.budgets[i].name, vJson.budgets[i].description,vJson.budgets[i].budgetType,(vJson.budgets[i].timeName ? rocket.getTime(vJson.budgets[i].timeName,oTimeGroup) : 0), (vJson.budgets[i].costName ? rocket.getCost(vJson.budgets[i].costName,oCostGroup) : 0), oBudgetGroup);
	   }
	}

	function importTimeModel(vJson){
	   for(var i = 0; i < vJson.times.length;i++){
		var x = rocket.getTime(vJson.times[i].name,oTimeGroup);
		if(!x || x == null) rocket.addTime(vJson.times[i].name, vJson.times[i].timeType, vJson.times[i].timeValue,oTimeGroup);
	   }
	}

	function importMethodologyModel(vJson){
	   var aP = [];
	   updatePaths();
	   for(var i = 0; i < vJson.processes.length;i++){
	      var aS = [];
	      for(var p = 0; p < vJson.processes[i].steps.length;p++){
	         rocket.addProcessStep(vJson.processes[i].steps[p],0,(p+1),0,0,0,oStepsGroup);
	         aS.push(rocket.getProcessStep(vJson.processes[i].steps[p],oStepsGroup));
	      }
	      rocket.addProcess(vJson.processes[i].name,0,(i+1),vJson.processes[i].iterates,aS,0,oProcessesGroup);
	      aP.push(rocket.getProcess(vJson.processes[i].name,oProcessesGroup));
	   }
	   rocket.addMethodology(vJson.name,vJson.description,aP,0,oMethodGroup);
	   return rocket.getMethodology(vJson.name, oMethodGroup);
	}

	function importProjectModel(vJson,bOverwrite){
		var aP = [];
		if(!communityMode) updatePaths();
		for(var i = 0; i < vJson.projects.length;i++){
			var oProject = vJson.projects[i];
			var oP = (communityMode ? rocket.getCommunityProject(irocket.getCommunityLifecycle().name,oProject.name) : rocket.getProject(oProject.name,oProjectGroup));
			if(oP != null && bOverwrite){
				if(communityMode) rocket.deleteCommunityProject(oP);
				else rocket.deleteProject(oP)
				oP = 0;
			}
			if(!oP || oP == null){
				var oSch = 0;
				if(!communityMode){
					
					if(oProject.costModel) importCostModel(Hemi.xml.getJSON(oProject.costModel));
					if(oProject.timeModel) importTimeModel(Hemi.xml.getJSON(oProject.timeModel));
					if(oProject.estimateModel) importEstimateModel(Hemi.xml.getJSON(oProject.estimateModel));
					if(oProject.budgetModel) importBudgetModel(Hemi.xml.getJSON(oProject.budgetModel));
					if(oProject.resourceModel) importResourceModel(Hemi.xml.getJSON(oProject.resourceModel));
					
					oSch = rocket.getSchedule(oProject.name + " Schedule", oScheduleGroup);
					if(!oSch){
						var d1 = new Date(),d2 = new Date();
						if(rocket.addSchedule(oProject.name + " Schedule", d1, d2, 0, 0, oScheduleGroup)){
							oSch = rocket.getSchedule(oProject.name + " Schedule", oScheduleGroup);
						}
					}
				}

				if(!communityMode && rocket.addProject(oProject.name,"",0,0,0,0,0,(oProject.stages ? importStageModel(oProject) : []),oSch,oProjectGroup)){
					oP = rocket.getProject(oProject.name,oProjectGroup);
				}
				else if(communityMode && (oP = rocket.createCommunityProject(irocket.getCommunityLifecycle(),oProject.name))){
					irocket.setCommunityProject(oP);
					updatePaths();
					oSch = rocket.getSchedule(oProject.name + " Schedule", oScheduleGroup);
					if(!oSch){
						var d1 = new Date(),d2 = new Date();
						if(rocket.addSchedule(oProject.name + " Schedule", d1, d2, 0, 0, oScheduleGroup)){
							oSch = rocket.getSchedule(oProject.name + " Schedule",oScheduleGroup);
						}
					}
					if(oProject.costModel) importCostModel(Hemi.xml.getJSON(oProject.costModel));
					if(oProject.timeModel) importTimeModel(Hemi.xml.getJSON(oProject.timeModel));
					if(oProject.estimateModel) importEstimateModel(Hemi.xml.getJSON(oProject.estimateModel));
					if(oProject.budgetModel) importBudgetModel(Hemi.xml.getJSON(oProject.budgetModel));
					if(oProject.resourceModel) importResourceModel(Hemi.xml.getJSON(oProject.resourceModel));
					oP.schedule = oSch;
					oP.stages = (oProject.stages ? importStageModel(oProject) : []);
					rocket.updateProject(oP);
				}
			}
			if(oP != null) aP.push(oP);
			
		}
		return aP;
	}

	function importStageModel(vJson,bOverwrite){
		var aS = [];
		var ld = new Date();
		if(!vJson.stages) return aS;
		for(var i = 0; i < vJson.stages.length; i++){
			var oStage = vJson.stages[i];
			var oS = rocket.getStage(oStage.name, oStagesGroup);
			if(oS != null && bOverwrite && rocket.deleteStage(oS)) oS = 0;
			if(!oS){
				var oW = rocket.getWork(oStage.name,oWorkGroup);
				if(oW != null && bOverwrite && rocket.deleteWork(oW)) oW = 0;
				if(!oW){
					var aTasks = (oStage.tasks ? importTaskModel(oStage,0,oStage.name) : []);
					if(rocket.addWork(oStage.name,0,(i+1),0,0,aTasks,0,oWorkGroup)){
						oW = rocket.getWork(oStage.name,oWorkGroup);
					}
				}
				var oSch = rocket.getSchedule(oStage.name, oScheduleGroup);
				if(oSch != null && bOverwrite && rocket.deleteSchedule(oSch)) oSch = 0;
				if(!oSch){
					var d1 = new Date(),d2 = new Date();
					d1.setTime(ld.getTime() + dayInMs);
					d2.setTime(ld.getTime() + (6 * dayInMs));
					ld.setTime(d2.getTime());

					if(oStage.start) d1 = new Date(oStage.start);
					if(oStage.end) d2 = new Date(oStage.end);

					ld = new Date(d2.getTime());
					
					if(rocket.addSchedule(oStage.name + " Schedule", d1, d2, 0, 0, oScheduleGroup)){
						oSch = rocket.getSchedule(oStage.name + " Schedule", oScheduleGroup);
					}
				}
				if(rocket.addStage(oStage.name,"",(i+1),0,oW,0,oSch,oStagesGroup)){
					oS = rocket.getStage(oStage.name, oStagesGroup);
				}
			}
			if(oS != null) aS.push(oS);
		}
		return aS;
	}

	function applyMethodologyToProject(oProj, oMeth){
	           oProj.stages = [];

		var ld = new Date();
		for(var i = 0; i < oMeth.processes.length;i++){
			var oProc = rocket.getProcess(oMeth.processes[i].name,oMeth.processes[i].groupPath);
			var aT = [];
			for(var s = 0; s < oProc.steps.length;s++){
				rocket.addTask(oProc.steps[s].name,0,"UNKNOWN",oProc.steps[s].logicalOrder,0,0,0,0,0,0,0,0,0,0,0,0,oTaskGroup);
				aT.push(rocket.getTask(oProc.steps[s].name,oTaskGroup));
			}
			rocket.addWork(oProc.name,0,oProc.logicalOrder,0,0,aT,0,oWorkGroup);
			var oW = rocket.getWork(oProc.name,oWorkGroup);
			var d1 = new Date(),d2 = new Date();
			d1.setTime(ld.getTime() + dayInMs);
			d2.setTime(ld.getTime() + (6 * dayInMs));
			ld.setTime(d2.getTime());
			rocket.addSchedule(oProc.name + " Schedule", d1, d2, 0, 0, oScheduleGroup);
			var oSch = rocket.getSchedule(oProc.name + " Schedule", oScheduleGroup);
			rocket.addStage(oProc.name,"",oProc.logicalOrder,oMeth,oW,0,oSch,oStagesGroup);
			var oS = rocket.getStage(oProc.name,oStagesGroup);
			oProj.stages.push(oS);
		}
	    return rocket.updateProject(oProj);
	}

	
	function instrumentApi(o,s){
		var _s = o.getProperties(),t,tl,obj = o,_o = o.getObjects();
		t = _s.type;
		tl = t.substring(0,1).toLowerCase() + t.substring(1,t.length);
		_s.typeName = s;
		
		var g = AM6Client.make("GROUP","DATA",_s.path);
		
		o["get" + t] = function(){ return obj.getObjects()[tl];};
		o.modify = function(v){
			var p = obj["get" + t](),b,m;
			if(!p || p == null) return false;
			for(var i in v){
				p[i] = v[i];
				if(i.match(/^name$/gi)) m = p[i];
			}
			b = window[uwm.getApi(obj.getProperties().type)]["update" + t](p);
			if(b && m) _s.viewName = m;
			return b;
		};
		o.delete = function(){
			var _o = obj.getObjects(), b=0;
			if(_o[tl] && _o[tl] != null){
				b = window[uwm.getApi(obj.getProperties().type)]["delete" + t](_o[tl]);
				obj.clearCache();
			}
			obj.destroy();
			return b;
		};
		o.view = function(){
			var _o = obj.getObjects();
			if(!_o[tl] || _o[tl] == null) return false;
			var oProps = {openerId:obj.getObjectId(),listType:t,picker:0,viewType:0,listId:0},p = _o[tl];
			oProps.viewType = p;
			var oW = Hemi.app.createWindow(p.name, uwm.getApiTypeView(t) + "/Forms/" + t + ".xml", "View-" + t + "-" + p.id , 0, 0, oProps);
            if (oW) {
            	oW.resizeTo(475, 400);
            	Hemi.app.getWindowManager().CenterWindow(oW);
            	oW.setHideOnClose(0);
            }
		};
		o.create = function(v){
			var b = false;
			if(_o[tl] && _o[tl] != null) return false;
			if(window[uwm.getApi(_s.type)]["add" + t].apply(null,o.getAddArguments(s,g))){
				obj.read();
				b = true;
				if(v){
					b = obj.modify(v);
				}
			}
			return b;

		};
		o.attribute = function(n,v){
			if(!_o[tl] || _o[tl] == null) return false;
			var a = AM6Client.getAttribute(_o[tl], n);
			/// add a new attribute
			if(!a && v && v != null) a = AM6Client.addAttribute(_o[tl],n,v);
			// remove the attribute
			else if(!v || v == null){
				AM6Client.removeAttribute(_o[tl],n);
			}
			// add a new attribute value
			else{
				a.values.push(v);
			}
		};
		o.clearCache = function(){
			if(_o[tl] != null) uwmServiceCache.removeFromCache(t, _o[tl]);
			delete _o[tl];
		};
		o.read = function(f){
			// If present and not forced, then return local cache
			
			if(_o[tl] && _o[tl] != null && !f) return _o[tl];
			// clear any local service cache and read by id
			else if(f && _o[tl] && _o[tl] != null){
				uwmServiceCache.removeFromCache(t, _o[tl]);
				_o[tl] = window[uwm.getApi(_s.type)]["get" + t + "ById"](_o[tl].id);	
			}
			// read by name
			else{
				_o[tl] = window[uwm.getApi(_s.type)]["get" + t](_s.viewName,g);
			}
			
			return _o[tl];
		};
		if(autoRead){
			o.read();
		}
		if(_o[tl] == null && autoCreate){
			o.create();
		}
	}
	
window.irocket = irocket = Hemi.newObject("Rocket Interface","1.0",true,true,{
	object_create : function(){
		var a = ["data","person","account","task","stage","work","project","lifecycle","process","processStep","methodology","resource","cost","time","budget","schedule","estimate"];
		for(var i = 0; i < a.length; i++) Hemi.object.addObjectAccessor(this,a[i]); 
		updateBase();
		//(communityMode && contextProject ? contextProjectGroup.path : "~"));
	},
	getCommunityMode : function(){ return communityMode;},
	getBasePath : function(s){return getBasePathByType(s);},
	getParentByType : function(s,d){return getDefaultParentByType(s,d);},
	getGroup : function(s){ return AM6Client.find("GROUP","DATA",irocket.getBasePath(s));},
	applyMethodToProject : function(oP, oM){
		return applyMethodologyToProject(oP, oM);
	},
	setCommunityProject : function(p,b){
		currenProject = 0;
		contextProject = (p ? p : 0);
		contextProjectGroup = 0;
		if(contextProject) contextProjectGroup = AM6Client.get("GROUP",contextProject.groupId);
		currentRoleBucket = (contextProject ? rocket.getProjectRoleBase(contextProject) : 0);
		currentPermissionBucket = (contextProject ? rocket.getProjectPermissionBase(contextProject) : 0);

		lifecycleScope = (b ? true : false);
		updateBase();
		Hemi.message.service.publish("onchangecommunity", this);
	},
	setCommunityMode : function(b){
		communityMode = (b ? true : false);
		updateBase();
	},
	setCurrentProject : function(o){
		if(communityMode) return;
		contextProject = 0;
		contextProjectGroup = 0;
		currentProject = (o ? o : 0);
		Hemi.message.service.publish("onchangecommunity", this);
	},
	getCurrentProject : function(){
		if(communityMode) return;
		return currentProject;
	},
	setCommunityLifecycle : function(l){
		if(!communityMode) communityMode = (l ? true : false);
		contextLifecycle = (l ? l : 0);
		contextLifecycleGroup = 0;
		contextProject = 0;
		contextProjectGroup = 0;
		lifecycleScope = true;
		if(contextLifecycle) contextLifecycleGroup = AM6Client.get("GROUP",contextLifecycle.groupId);
		updateBase();
		Hemi.message.service.publish("onchangecommunity", this);
	},
	getCommunityLifecycle : function(){return contextLifecycle;},
	getCommunityProject : function(){return contextProject;},
	importResources : function(s, v){
		if(typeof s == "string") s = Hemi.xml.getJSON(s);
		if(!s || s == null) return false;
		importResourceModel(s);
		return true;
	},
	importEstimates : function(s, v){
		if(typeof s == "string") s = Hemi.xml.getJSON(s);
		if(!s || s == null) return false;
		importEstimateModel(s);
		return true;
	},
	importTimes : function(s, v){
		if(typeof s == "string") s = Hemi.xml.getJSON(s);
		if(!s || s == null) return false;
		importTimeModel(s);
		return true;
	},
	importBudgets : function(s, v){
		if(typeof s == "string") s = Hemi.xml.getJSON(s);
		if(!s || s == null) return false;
		importBudgetModel(s);
		return true;
	},
	importCosts : function(s, v){
		if(typeof s == "string") s = Hemi.xml.getJSON(s);
		if(!s || s == null) return false;
		importCostModel(s);
		return true;
	},
	importMethodology : function(s, v){
		if(typeof s == "string") s = Hemi.xml.getJSON(s);
		if(!s || s == null) return false;
		var o = importMethodologyModel(s);
		return (o && o != null);
	},
	importProject : function(s,v){
		if(typeof s == "string") s = Hemi.xml.getJSON(s);
		if(!s || s == null) return false;
		var o = importProjectModel(s,v);
		return (o && o!=null);
		
	},
	task : function(s){
		if((v = this.getTaskByName(s))) return v;
		v = Hemi.newObject("Task " + s,"1.0",true,true,{
			object_create : function(){
				var _o = this.getObjects(),_s = this.getProperties();
				_s.type = "Task";
				_s.path = taskPath;
				instrumentApi(this,s);
			},
			getAddArguments : function(s, g){
				return [s,0,"UNKNOWN",0,0,0,0,0,0,0,0,0,0,0,0,g];
			}
		});
		this.addNewTask(v,s);
		return v;
	},
	cost : function(s){
		if((v = this.getCostByName(s))) return v;
		v = Hemi.newObject("Cost " + s,"1.0",true,true,{
			object_create : function(){
				var _o = this.getObjects(),_s = this.getProperties();
				_s.type = "Cost";
				_s.path = costPath;
				instrumentApi(this,s);
			},
			getAddArguments : function(s, g){
				return [s, "USD", 0,g];
			}
		});
		this.addNewCost(v,s);
		return v;
	},
	time : function(s){
		if((v = this.getTimeByName(s))) return v;
		v = Hemi.newObject("Time " + s,"1.0",true,true,{
			object_create : function(){
				var _o = this.getObjects(),_s = this.getProperties();
				_s.type = "Time";
				_s.path = timePath;
				instrumentApi(this,s);
			},
			getAddArguments : function(s, g){
				return [s, "MINUTE", 0, g];
			}
		});
		this.addNewTime(v,s);
		return v;
	},
	person : function(s){
		if((v = this.getPersonByName(s))) return v;
		v = Hemi.newObject("Person " + s,"1.0",true,true,{
			object_create : function(){
				var _o = this.getObjects(),_s = this.getProperties();
				_s.type = "Person";
				_s.path = personPath;
				instrumentApi(this,s);
			},
			getAddArguments : function(s, g){
				return [s, g];
			}
		});
		this.addNewPerson(v,s);
		return v;
	},
	account : function(s){
		if((v = this.getAccountByName(s))) return v;
		v = Hemi.newObject("Account " + s,"1.0",true,true,{
			object_create : function(){
				var _o = this.getObjects(),_s = this.getProperties();
				_s.type = "Account";
				_s.path = accountPath;
				instrumentApi(this,s);
			},
			getAddArguments : function(s, g){
				return [s, "NORMAL", "NORMAL", g];
			}
		});
		this.addNewAccount(v,s);
		return v;
	},
	listLifecycles : function(){
		if(!communityMode) return rocket.listLifecycles(lifecyclePath,0,0);
		var lifList = [];
		var a = AM6Client.list("GROUP",AM6Client.find("GROUP","DATA",lifecyclePath).objectId,"DATA",0,0);
		for(var i = 0; i < a.length; i++){
			var oP = rocket.getCommunityLifecycle(a[i].name);
			if(oP != null)  lifList.push(oP);
		}
		return lifList;
	},
	listProjects : function(){
		if(!communityMode) return rocket.listProjects(projectPath,0,0);
		var oL = contextLifecycle;
		if(!oL || oL == null){
			return [];
		}
		var projList = [];
		var a = AM6Client.list("GROUP",AM6Client.find("GROUP","DATA",oL.groupPath + "/Projects").objectId,"DATA",0,0);
		for(var i = 0; i < a.length; i++){
			var oP = rocket.getCommunityProject(oL.name,a[i].name);
			if(oP != null)  projList.push(oP);
		}
		return projList;
	}
});
uwm.pathProvider = irocket.getBasePath;
uwm.defaultParentProvider = irocket.getParentByType;
}());
