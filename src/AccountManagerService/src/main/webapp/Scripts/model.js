(function(){
	
window.model = model = Hemi.newObject("AMM","1.0",true,true,{
	subType : [
		"Address","Contact","Application","Group","Ticket","Story","Requirement","Note","Module","Model","Goal","ValidationRule","Form","FormElement","Case","Artifact","ProcessStep","Process","Method","Stage","Data","Work","Task","Cost","Time","Estimate","Budget","Person","Account","Resource","Lifecycle","Project","Schedule","Methodology","Policy","Pattern","Rule","Fact","Operation","Function","Permission","Role","Tag","Event","Location","Trait"
	],
	dependencies : ["hemi.event"],
	object_create : function(){
		Hemi.event.addScopeBuffer(this);
		Hemi.message.service.subscribe(this,"onchangedirectory", this.scopeHandler("change_directory",0,0,1));

	},
	_handle_change_directory : function(v){
		if(v && v.getCurrentGroup){
			this.getObjects().currentGroup = v.getCurrentGroup();
		}
	},
	primitive : function(sLType){
		var sType = sLType.toUpperCase();
		var o = {
			name : "",
			nameType : sType,
			id : 0,
			parentId : 0,
			ownerId : 0,
			populated : false,
			organizationId : 0,
			attributesPopulated : false,
			attributes : []
		};
		if(!sType.match(/^(role|permission)$/gi)){
			
			var sDP = irocket.getBasePath(sLType);
			if(sType == "GROUP") o.path = sDP;
			else o.groupPath = sDP;
		}
		switch(sType){
			case "ACCOUNT":
				o.accountId = "";
				o.accountType = "UNKNOWN";
				o.accountStatus = "UNKNOWN";
				o.referenceId = 0;
				break;
			case "ADDRESS":
				o.locationType = "UNKNOWN";
				o.preferred = false;
				o.addressLine1 = "";
				o.addressLine2 = "";
				o.city = "";
				o.state = "";
				o.region = "";
				o.state = "";
				o.postalCode = "";
				o.country = "";
				o.description = "";
				break;
				
			case "ARTIFACT":
				o.description = "";
				o.artifactType = "UNKNOWN";
				o.createdDate = new Date();
				o.previousTransitionId = 0;
				o.nextTransitionId = 0;
				o.artifactDataId = 0;
				break;
			case "BUDGET":
				o.description = "";
				o.budgetType = "UNKNOWN";
				break;
			case "CASE":
				o.description = "";
				o.caseType = "UNKNOWN";
				break;
			case "CONTACT":
				o.locationType = "UNKNOWN";
				o.contactType = "UNKNOWN";
				o.preferred = false;
				o.description = "";
				o.contactValue = "";
				break;
			case "COST":
				o.value = 0;
				o.currencyType = "USD";
				break;
			case "DATA":
				o.blob = true;
				o.dataBytesStore = "";
				o.mimeType = "text/plain";
				o.description = "";
				o.createdDate = new Date();
				o.modifiedDate = o.createdDate;
				o.expiryDate = o.createdDate;
				break;
			case "ESTIMATE":
				o.description = "";
				o.estimateType = "UNKNOWN";
				break;
			case "EVENT":
				o.description = "";
				o.eventType = "UNKNOWN";
				o.actors = [];
				o.entryTraits = [];
				o.exitTraits = [];
				o.groups = [];
				o.influencers = [];
				o.observers = [];
				o.orchestrators = [];
				o.things = [];
				o.startDate = new Date();
				o.endDate = o.startDate;
				break;

			case "FACT":
				o.description = "";
				o.sourceUrl = "";
				o.sourceUrn = "";
				o.sourceDataType = "VARCHAR";
				o.factData = "";
				o.sourceType = "";
				o.score = 0;
				o.factType = "UNKNOWN";
				o.factoryType = "UNKNOWN";
				break;
			case "FORM":
				o.description = "";
				o.isTemplate = false;
				break;
			case "FORMELEMENT":
				o.description = "";
				o.elementType = "STRING";
				o.elementName = "";
				o.elementLabel = "";
				o.elementValues = [];
				break;
			case "FUNCTION":
				o.description = "";
				o.sourceUrl = "";
				o.sourceUrn = "";
				o.functionType = "JAVA";
				o.logicalOrder = 0;
				o.score = 0;
				break;
			case "GOAL":
				o.priority = "UNKNOWN";
				o.logicalOrder = 0;
				o.description = "";
				o.goalType = "UNKNOWN";
				break;
			case "GROUP":
				o.groupType = "DATA";
				break;
			case "LIFECYCLE":
				o.description = "";
				break;
			case "LOCATION":
				o.description = "";
				o.boundaries = [];
				o.borders = [];
				o.geographyType = "UNKNOWN";
				o.classification = "";
				break;
			case "METHODOLOGY":
				o.description = "";
				break;
			case "MODEL":
				o.modelType = "UNKNOWN";
				o.description = "";
				break;
			case "MODULE":
				o.moduleType = "UNKNOWN";
				o.description = "";
				break;
			case "NOTE":
				o.text = "";
				o.createdDate = new Date();
				o.modifiedDate = o.createdDate;
				o.childNotes = [];
				break;
			case "OPERATION":
				o.score = 0;
				o.nameType = "OPERATION";
				o.description = "";
				o.operation = "";
				o.operationType = "UNKNOWN";
				break;
			case "PATTERN":
				o.score = 0;
				o.nameType = "PATTERN";
				o.logicalOrder = 0;
				o.description = "";
				o.matchUrn = "";
				o.factUrn = "";
				o.operationUrn = "";
				o.comparator = "EQUALS";
				o.patternType = "EXPRESSION";
				break;
			case "PERMISSION":
				o.permissionType = "UNKNOWN";
				break;
			case "PERSON":
				o.description = "";
				o.suffix = "";
				o.prefix = "";
				o.title = "";
				o.alias = "";
				o.firstName = "";
				o.lastName = "";
				o.middleName = "";
				o.gender = "unknown";
				o.birthDate = new Date();
				o.users = [];
				o.accounts = [];
				o.partners = [];
				o.dependents = [];
				o.notes = [];
				break;
			case "POLICY":
				o.logicalOrder = 0;
				o.score = 0;
				o.expiresDate = new Date();
				o.modifiedDate = new Date();
				o.createdDate = new Date();
				o.condition = "ALL";
				o.enabled = false;
				o.decisionAge = 0;
				o.description = "";
				o.rules = [];
				break;
			case "PROCESS":
				o.description = "";
				o.logicalOrder = 0;
				o.iterates = false;
				break;
			case "PROCESSSTEP":
				o.description = "";
				o.logicalOrder = 0;
				break;
			case "PROJECT":
				o.description = "";
				break;
			case "REQUIREMENT":
				o.description = "";
				o.requirementType = "UNKNOWN";
				o.requirementStatus = "UNKNOWN";
				o.priority = "UNKNOWN";
				o.requirementId = "";
				o.logicalOrder = 0;
				break;
			case "RESOURCE":
				o.resourceType = "UNKNOWN";
				o.description = "";
				o.utilization = 0;
				o.resourceDataId = 0;
				break;
			case "ROLE":
				o.roleType = "USER";
				break;
			case "RULE":
				o.score = 0;
				o.logicalOrder = 0;
				o.description = "";
				o.ruleType = "PERMIT";
				o.condition = "ALL";
				o.rules = [];
				o.patterns = [];
				break;
			case "SCHEDULE":
				o.startTime = new Date();
				o.endTime = o.startTime;
				break;
			case "STAGE":
				o.description = "";
				o.logicalOrder = 0;
				break;
			case "TAG":
				o.tagType = "UNKNOWN";
				break;
			case "TASK":
				o.description = "";
				o.taskStatus = "UNKNOWN";
				o.logicalOrder = 0;
				o.startDate = (new Date());
				o.dueDate = (new Date());
				o.completedDate = (new Date());
				o.childTasks = [];
				break;
			case "TICKET":
				o.description = "";
				o.ticketStatus = "UNKNOWN";
				o.priority = "NORMAL";
				o.severity = "LOW";
				o.dueDate = (new Date());
				o.closedDate = (new Date());
				o.reopenedDate = (new Date());
				o.createdDate = (new Date());
				o.modifiedDate = (new Date());
				break;
			case "TIME":
				o.value = 0;
				o.basisType = "DAY";
				o.nameType = "TIME";
				break;
			case "TRAIT":
				o.description = "";
				o.score = 0;
				o.alignmentType = "NEUTRAL";
				o.traitType = "UNKNOWN";
				break;
			case "USER":
				o.userStatus = "UNKNOWN";
				o.userType = "NORMAL";
				o.accountId = 0;
				break;
			case "VALIDATIONRULE":
				o.description = "";
				o.isReplacementRule = false;
				o.isRuleSet = false;
				o.comparison = false;
				o.expression = "";
				o.errorMessage = "";
				o.replacementValue = "";
				o.allowNull = false;
				o.validationType = "UNKNOWN";
				break;
			case "WORK":
				o.description = "";
				o.logicalOrder = 0;
				break;
			default:
				Hemi.logError("Unknown primitive type: " + sType);
				break;
		}
		
		return o;
	},
	object : function(sType){
		//if((v = this.getPolicyByName(s))) return v;
		var obj;
		if(typeof sType == "object" && sType.nameType){
			obj = sType;
			sType = obj.nameType;
		}
		v = Hemi.newObject("Type " + sType,"1.0",true,true,{
			object_create : function(){
				this.new(sType);
			},
			new : function(sType){
				var _o = this.getObjects(),o
				_o.object = (obj ? obj : model.primitive(sType));
				/// Hemi.object.addObjectAccessor(this,"rule");
				/// var g = AM6Client.make("GROUP", "DATA",uwm.getPathForType("Policy"));
				/// _o.policy = AM6Client.getByName("POLICY",g.objectId,s);
			},
			getType : function(){
				return this.getObjects().object;
			}
		});
		return v;
	}
});
}());