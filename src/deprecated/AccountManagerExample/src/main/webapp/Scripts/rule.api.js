(function () {
	uwm.addApi("accountManagerRule", "/AccountManagerExample");
	uwm.addApiTypes("accountManagerRule", ["Policy","Rule","Pattern","Fact","Operation","Function"]);
	
	window.accountManagerRule = accountManagerRule = {
		define : function(o){
			return uwmServices.getService("Policy").define(typeof o == "number" ? o : o.id);
		},
		countPolicys : function(sPath){
			return uwmServices.getService("Policy").count(sPath);
		},
		listPolicys : function(sPath, iStartIndex, iRecordCount){
			return accountManager.serviceListInGroup(uwmServices.getService("Policy"),sPath, iStartIndex, iRecordCount);
		},
		addPolicy : function(sName, bE, sD, iS, iO, sC, iA, dC, dM, dE, aR,oGroup){
			var o = new org.cote.beans.policyType();
			o.enabled = bE;
			o.description = sD;
			o.score = iS;
			o.logicalOrder = iO;
			o.decisionAge = iA;
			o.condition = sC;
			o.name = sName;
			o.nameType = "POLICY";
			o.createdDate = dC;
			o.modifiedDate = dM;
			o.expiresDate = dE;
			o.rules = aR;
			if(oGroup){
				o.groupPath = oGroup;
			}
			return uwmServices.getService("Policy").add(o);
		},
		populatePolicy : function(o){
			return uwmServices.getService("Policy").populate(o);
		},

		deletePolicy : function(oRec){
			return uwmServices.getService("Policy").delete(oRec);
		},
		updatePolicy : function(oRec){
			return uwmServices.getService("Policy").update(oRec);
		},
		getPolicy : function(sName, oGroup){
			if(oGroup) return uwmServices.getService("Policy").readByGroupId(oGroup.id,sName);
			return uwmServices.getService("Policy").read(sName);
		},
		getPolicyById : function(iId){
			return uwmServices.getService("Policy").readById(iId);
		},
		countRules : function(sPath){
			return uwmServices.getService("Rule").count(sPath);
		},
		listRules : function(sPath, iStartIndex, iRecordCount){
			return accountManager.serviceListInGroup(uwmServices.getService("Rule"),sPath, iStartIndex, iRecordCount);
		},
		addRule : function(sName, sDesc,iS, iO, sType, sCond, aP, aR, oGroup){
			var o = new org.cote.beans.ruleType();
			
			o.name = sName;
			o.nameType = "RULE";
			o.description = sDesc;
			o.score = iS;
			o.logicalOrder = iO;
			o.ruleType = sType;
			o.condition =sCond;
			if(aP) o.patterns = aP;
			if(aR) o.rules = aR;
			
			if(oGroup){
				o.groupPath = oGroup;
			}
			return uwmServices.getService("Rule").add(o);
		},
		populateRule : function(o){
			return uwmServices.getService("Rule").populate(o);
		},

		deleteRule : function(oRec){
			return uwmServices.getService("Rule").delete(oRec);
		},
		updateRule : function(oRec){
			return uwmServices.getService("Rule").update(oRec);
		},
		getRule : function(sName, oGroup){
			if(oGroup) return uwmServices.getService("Rule").readByGroupId(oGroup.id,sName);
			return uwmServices.getService("Rule").read(sName);
		},
		getRuleById : function(iId){
			return uwmServices.getService("Rule").readById(iId);
		},
		countPatterns : function(sPath){
			return uwmServices.getService("Pattern").count(sPath);
		},
		listPatterns : function(sPath, iStartIndex, iRecordCount){
			return accountManager.serviceListInGroup(uwmServices.getService("Pattern"),sPath, iStartIndex, iRecordCount);
		},
		addPattern : function(sName, sDesc, iS, iO, sType, sComp, sFact, sMatch,oGroup){
			var o = new org.cote.beans.patternType();
			
			o.name = sName;
			o.description = sDesc;
			o.nameType = "PATTERN";
			o.score = iS;
			o.logicalOrder = iO;
			if(sFact) o.factUrn = sFact;
			if(sMatch) o.matchUrn = sMatch;
			o.patternType = (sType ? sType : "EXPRESSION");
			o.comparator = (sComp ? sComp : "EQUALS");
			if(oGroup){
				o.groupPath = oGroup;
			}
			return uwmServices.getService("Pattern").add(o);
		},
		populatePattern : function(o){
			return uwmServices.getService("Pattern").populate(o);
		},

		deletePattern : function(oRec){
			return uwmServices.getService("Pattern").delete(oRec);
		},
		updatePattern : function(oRec){
			return uwmServices.getService("Pattern").update(oRec);
		},
		getPattern : function(sName, oGroup){
			if(oGroup) return uwmServices.getService("Pattern").readByGroupId(oGroup.id,sName);
			return uwmServices.getService("Pattern").read(sName);
		},
		getPatternById : function(iId){
			return uwmServices.getService("Pattern").readById(iId);
		},
		countFacts : function(sPath){
			return uwmServices.getService("Fact").count(sPath);
		},
		listFacts : function(sPath, iStartIndex, iRecordCount){
			return accountManager.serviceListInGroup(uwmServices.getService("Fact"),sPath, iStartIndex, iRecordCount);
		},
		addFact : function(sName, sDesc,iS, sType, sFType, sDType, sData,sSUrn, sSUrl,sFData, oGroup){
			var o = new org.cote.beans.factType();
			
			o.name = sName;
			o.nameType = "FACT";
			o.description = sDesc;
			o.sourceDataType = sDType;
			o.sourceType = sData;
			o.score = iS;
			o.factoryType = sFType;
			o.factType = sType;
			o.sourceUrn = sSUrn;
			o.sourceUrl = sSUrl;
			o.factData = sFData;
			o.factReference = null;

			if(oGroup){
				o.groupPath = oGroup;
			}
			return uwmServices.getService("Fact").add(o);
		},
		deleteFact : function(oRec){
			return uwmServices.getService("Fact").delete(oRec);
		},
		updateFact : function(oRec){
			return uwmServices.getService("Fact").update(oRec);
		},
		getFact : function(sName, oGroup){
			if(oGroup) return uwmServices.getService("Fact").readByGroupId(oGroup.id,sName);
			return uwmServices.getService("Fact").read(sName);
		},
		getFactById : function(iId){
			return uwmServices.getService("Fact").readById(iId);
		},
		countOperations : function(sPath){
			return uwmServices.getService("Operation").count(sPath);
		},
		listOperations : function(sPath, iStartIndex, iRecordCount){
			return accountManager.serviceListInGroup(uwmServices.getService("Operation"),sPath, iStartIndex, iRecordCount);
		},

		addOperation : function(sName, sDesc, iScore, sType, sOp, oGroup){
			var o = new org.cote.beans.operationType();
			o.name = sName;
			o.nameType = "OPERATION";
			o.description = sDesc;
			o.operationType = sType;
			o.score = iScore;
			o.operation = sOp;
			if(oGroup){
				o.groupPath = oGroup;
			}
			return uwmServices.getService("Operation").add(o);
		},
		deleteOperation : function(oRec){
			return uwmServices.getService("Operation").delete(oRec);
		},
		updateOperation : function(oRec){
			return uwmServices.getService("Operation").update(oRec);
		},
		getOperation : function(sName, oGroup){
			if(oGroup) return uwmServices.getService("Operation").readByGroupId(oGroup.id,sName);
			return uwmServices.getService("Operation").read(sName);
		},
		getOperationById : function(iId){
			return uwmServices.getService("Operation").readById(iId);
		},
		countFunctions : function(sPath){
			return uwmServices.getService("Function").count(sPath);
		},
		listFunctions : function(sPath, iStartIndex, iRecordCount){
			return accountManager.serviceListInGroup(uwmServices.getService("Function"),sPath, iStartIndex, iRecordCount);
		},
		addFunction : function(sName, sDesc, sType, iLog, iSco, sUrl,sUrn, oGroup){
			var o = new org.cote.beans.operationType();
			o.name = sName;
			o.nameType = "FUNCTION";
			o.description = sDesc;
			o.functionType = sType;
			o.logicalOrder = iLog;
			o.score = iSco;
			o.sourceUrl = sUrl;
			o.sourceUrn = sUrn;
			if(oGroup){
				o.groupPath = oGroup;
			}
			return uwmServices.getService("Function").add(o);
		},
		deleteFunction : function(oRec){
			return uwmServices.getService("Function").delete(oRec);
		},
		updateFunction : function(oRec){
			return uwmServices.getService("Function").update(oRec);
		},
		getFunction : function(sName, oGroup){
			if(oGroup) return uwmServices.getService("Function").readByGroupId(oGroup.id,sName);
			return uwmServices.getService("Function").read(sName);
		},
		getFunctionById : function(iId){
			return uwmServices.getService("Function").readById(iId);
		}

	};
}());