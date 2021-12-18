/// packaging script to condense all of the HemiFramework templates and fragments into a publishable DWAC project
///
(function(){
	var d = {
		fragment : {
			type : "Fragments",
			files : ["FormViewTools.xml","ProjectRowSection.xml","ProjectStageSection.xml","SharingFrag.xml"],
			source : "/AccountManagerService/Forms/Fragments/",
		},
		form : {
			type : "Fragments",
			files : ["Account.xml", "AccountFields.xml", "AccountPicker.xml", "AccountPickerSingle.xml", "Address.xml", "AddressFields.xml", "Application.xml", "ApplicationFields.xml", "Artifact.xml", "ArtifactFields.xml", "ArtifactPicker.xml", "Attribute.xml", "AttributePicker.xml", "BaseFields.xml", "BaseTemplate.xml", "BaseType.xml", "BlueprintPicker.xml", "Budget.xml", "BudgetFields.xml", "BudgetPicker.xml", "BudgetPickerSingle.xml", "CacheUtility.xml", "CanvasGallery.xml", "Case.xml", "CaseFields.xml", "CasePicker.xml", "CompleteRegistration.xml", "Contact.xml", "ContactFields.xml", "ContactInformation.xml", "Cost.xml", "CostFields.xml", "CostPicker.xml", "CostPickerSingle.xml", "CredentialForm.xml", "Data.xml", "DataDesign.xml", "DataDnd.xml", "DataDndPicker.xml", "DataFields.xml", "DataPicker.xml", "DependencyPicker.xml", "DndFormFragment.xml",  "Estimate.xml", "EstimateFields.xml", "EstimatePicker.xml", "EvaluatePolicy.xml", "Event.xml", "EventFields.xml", "EventPicker.xml", "Fact.xml", "FactFields.xml", "FeedBack.xml", "Form.xml", "FormElement.xml", "FormElementFields.xml", "FormFields.xml", "FormPicker.xml", "FormPickerSingle.xml", "FormView.xml", "Function.xml", "FunctionFields.xml", "GalleryShare.xml", "Goal.xml", "GoalFields.xml", "GoalPicker.xml", "GridType.xml", "Group.xml", "GroupFields.xml", "GroupPicker.xml", "ImageViewer.xml", "JWTPicker.xml", "Lifecycle.xml", "LifecycleFields.xml", "ListAccounts.xml", "ListAddresss.xml", "ListApplications.xml", "ListArtifacts.xml", "ListBudgets.xml", "ListCases.xml", "ListContacts.xml", "ListCosts.xml", "ListDatas.xml", "ListEstimates.xml", "ListEvents.xml", "ListFacts.xml", "ListFormElements.xml", "ListForms.xml", "ListFunctions.xml", "ListGoals.xml", "ListGroups.xml", "ListLifecycles.xml", "ListLocations.xml", "ListMethodologys.xml", "ListModels.xml", "ListModules.xml", "ListNotes.xml", "ListOperations.xml", "ListPatterns.xml", "ListPermissions.xml", "ListPersons.xml", "ListPolicys.xml", "ListProcesss.xml", "ListProcessSteps.xml", "ListProjects.xml", "ListRequirements.xml", "ListResources.xml", "ListRoles.xml", "ListRules.xml", "ListSchedules.xml", "ListStages.xml", "ListStorys.xml", "ListTags.xml", "ListTasks.xml", "ListTickets.xml", "ListTimes.xml", "ListTraits.xml", "ListType.xml", "ListUsers.xml", "ListValidationRules.xml", "ListWorks.xml", "Location.xml", "LocationFields.xml", "LocationPicker.xml", "LocationPickerSingle.xml", "Login.xml", "LoginIn.xml", "Main.xml", "MainIn.xml", "MainProject.xml",  "Methodology.xml", "MethodologyFields.xml", "MethodologyPickerSingle.xml", "Model.xml", "ModelFields.xml", "ModelPicker.xml", "Module.xml", "ModuleFields.xml", "ModulePicker.xml", "Note.xml", "NoteFields.xml", "NotePicker.xml", "NotePickerSingle.xml", "Operation.xml", "OperationFields.xml", "OperationPickerSingle.xml", "OrganizationPicker.xml", "OrganizationPickerIn.xml", "ParentPicker.xml", "Pattern.xml", "PatternFields.xml", "Permission.xml", "PermissionFields.xml", "PermissionPicker.xml", "Person.xml", "PersonFields.xml", "PersonPicker.xml", "PersonPickerSingle.xml", "Policy.xml", "PolicyFields.xml", "Process.xml", "ProcessFields.xml", "ProcessPicker.xml", "ProcessStep.xml", "ProcessStepFields.xml", "ProcessStepPicker.xml", "Profile.xml", "ProfileFields.xml", "Project.xml", "ProjectDesign.xml", "ProjectFields.xml", "ProjectPicker.xml", "Register.xml", "Requirement.xml", "RequirementFields.xml", "RequirementPicker.xml", "Resource.xml", "ResourceFields.xml", "ResourcePicker.xml", "ResourcePickerSingle.xml", "Role.xml", "RoleFields.xml", "RolePicker.xml", "Rule.xml", "RuleFields.xml", "Schedule.xml", "ScheduleFields.xml", "SchedulePicker.xml", "SchedulePickerSingle.xml", "Sharing.xml", "Stage.xml", "StageFields.xml", "StagePicker.xml", "StatusFrag.xml", "Story.xml", "Tag.xml", "TagFields.xml", "TagPicker.xml", "TagSearch.xml", "Task.xml", "TaskFields.xml", "TaskPicker.xml", "TextPicker.xml", "Ticket.xml", "TicketFields.xml", "Time.xml", "TimeFields.xml", "TimePicker.xml", "TimePickerSingle.xml", "Trait.xml", "TraitFields.xml", "UnauthIn.xml", "UnauthMain.xml", "User.xml", "UserFields.xml", "UserPicker.xml", "UserPickerSingle.xml", "ValidationRule.xml", "ValidationRuleFields.xml", "WonderForm.xml", "Work.xml", "WorkFields.xml", "WorkPicker.xml", "WorkPickerSingle.xml"],
			source : "/AccountManagerService/Forms/",
		},
		template : {
			type  : "Templates",
			source : "/AccountManagerService/Templates/",
			files : ["AlertActivities.xml", "AlertMain.xml", "AssetMain.xml", "Blank.xml", "BusinessMain.xml", "Console.xml", "Dashboard.xml", "Designer.xml", "DevelopmentMain.xml", "EventMain.xml", "FormMain.xml", "IdentityMain.xml", "MethodMain.xml", "PolicyMain.xml", "ProjectDesigner.xml", "ProjectMain.xml", "RecentArticles.xml", "ResourceMain.xml", "SelectCommunity.xml", "TestField1.xml", "TestField2.xml", "TestTemplate.xml"],
		}
	};
	
	/// x - struct; b delete if exists; w don't create
	function popIt(x2, b, w){
		var x = x2;
		if(!x.files.length){
			popOut();
			return;
		}
		var sName = x.files.pop();
		var sPath = "~/DWAC/" + x.type;
			
		if(!sName || !sName.length){
			console.error("Invalid name");
			popOut();
			return;
		};
		AM6Client.make("GROUP","DATA",sPath,function(i,j){
			//console.log(j);
			var oG = j;
			if(j != null && j.json != null) oG = j.json;
			if(oG == null){
				console.error("Failed to find group " + sPath);
				popOut();
				return;
			}
	
	
			AM6Client.getByName("DATA", oG.objectId, sName, function(k,l){
				var oD = (l != null ? l.json : null);
				if(oD != null && !b){
					console.log("Skip existing " + sName);
					linkElement(x.type, sName);
					popIt(x, b, w);
				}
				else{
	
					(oD != null && b ? 
						new Promise((res,rej)=>{ console.log("Delete existing " + sName); AM6Client.delete("DATA", oD.objectId, function(){res();})}) 
						:
						Promise.resolve()
					).then(()=>{
						Hemi.xml.getText(x.source + sName,function(s, v){
							if(!v && !v.text){
								console.error("Failed to load " + sName);
								popIt(x, b, w);
							}
							else{
								if(w){
									console.log("Don't create " + sName);
									popIt(x, b, w);
									
								}
								else{
									var o = model.primitive("DATA");
									o.mimeType = "application/xml";
	
									o.dataBytesStore = uwm.base64Encode(filterData(v.text, x.type, sName));
									o.name = sName;
									o.groupPath = sPath;
									AM6Client.update("DATA", o, function(a, c){
										if(c && c.json){
											console.log("Created " + sName + " " + c.json);
											linkElement(x.type, sName);
										}
										else{
											console.error("Failed to create " + sName);
											console.log(j);
										}
										popIt(x, b, w);
									});
								}
							}
						}, 1);
					});
				}
			});
		});
	}
	function filterData(sText,sType, sName){
		var sV = sText
			.replaceAll(/\/AccountManagerService\/Forms\/Fragments/gi,getDWACPath("Fragments"))
			.replaceAll(/\/AccountManagerService\/Forms/gi,getDWACPath("Fragments"))
			.replaceAll(/\/AccountManagerService\/Templates/gi,getDWACPath("Templates"))
		;
		if(sType == 'Templates' && sName){
			sV = sV.replace(/<Template>/,'<Template id="' + sName + '">');
		}
		return sV;
	}
	function getDWACPath(sType){
		return "/AccountManagerService/media/" + sDotPath + "/Data/Home/" + oU.name + "/DWAC/" + sType;
	}
	
	function popOut(){
		iSet++;
		if(iSet < aSet.length) popSet();
		else{
			console.log("all done");
			stopProject();
			populateProject();
		}
	}
	function startProject(){
		vProject = ['<?xml version="1.0" encoding="UTF-8"?>\n<DWAC Title = "' + sProjectTitle + '" DWacTemplateId = "' + sDefaultTemplate + '">\n'];
	}
	function linkElement(sType, sName){
		vProject.push('\t<module type = "' + sType.toLowerCase().replace(/s$/,"") + '" uri = "' + getDWACPath(sType) + '/' + sName + '" id = "' + sName + '" name = "' + sName + '" />\n');
	}
	function stopProject(){
		vProject.push("</DWAC>");
		return vProject.join("");
	}
	function populate(){
		startProject();
		popSet();
	}
	function popSet(){
	
		(oU ? Promise.resolve(oU) : AM6Client.user()).then((u)=>{
			if(!oU){
						
				sDotPath = AM6Client.dotPath(u.organizationPath);
				oU = u;
			}
			console.log("Working with " + oU.name);
			popIt(d[aSet[iSet]], bDelete, bNoCreate);
		});
	}
	
		var oU;
	var sDotPath = "";
	var sProjectTitle = "Account Manager Main";
	var sDefaultTemplate = "Dashboard.xml";
	var bDelete = false;
	var bNoCreate = false;
	var iSet = 0;
	var aSet = ["fragment","form","template"];
	var vProject;
	var gProject;
	function getProps() {
		var oP = gProject = AM6Client.find("GROUP","DATA",uwm.getPathForType("DWAC") + "/Projects");
		
		return {
			application: "DWAC",
			current_group_id: (oP ? oP.objectId : 0),
			current_group_name: (oP ? oP.name : null),
			current_path: (oP ? oP.path : null)
		};
	}
	function projectLoaded(v){
		v.getElementByRID("project_text").value = (vProject ? vProject.join("") : "");
		v.LoadProjectText();
	}
	function populateProject(){
		var sId = Hemi.guid(), oProps = getProps();
		var oWp = Hemi.app.createWindow("Project Builder", "Templates/ProjectBuilder.xml", "project-" + sId, 0, 0, oProps, projectLoaded);
		if(oWp){
			oWp.then((oW)=>{
				//oW.resizeTo(650, 650);
				Hemi.app.getWindowManager().then((oM)=>{oM.CenterWindow(oW);});;
				// Destroy the window when finished
				//
				oW.setHideOnClose(0);
			});
		}	
	}
	window.am5condenser = {
		delete : function(b){bDelete = b;},
		create : function(b){bNoCreate = b;},
		populate : populate
	};
	


}());