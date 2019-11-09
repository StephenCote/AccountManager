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
 * Repackaged from accountManagerAzn.js
 */

(function(){
var policyPath = "~/Policies";
var factPath = "~/Facts";
var patternPath = "~/Patterns";
var functionPath = "~/Functions";
var functionFactPath = "~/FunctionFacts";
var operationPath = "~/Operations";
var rulePath = "~/Rules";

function getUrn(o,s){
	if(!o.populated) o = AM6Client.get("GROUP",o.objectId);
	return "urn:am5." + o.path.replace(/^\/home\//gi,"").replace(/\//gi,".").toLowerCase() + "." + s.replace(/[^A-Za-z0-9]+/g,"").toLowerCase();
}

window.azn = azn = Hemi.newObject("AZN","1.0",true,true,{
	object_create : function(){
		Hemi.object.addObjectAccessor(this,"policy");
		Hemi.object.addObjectAccessor(this,"rule");
		Hemi.object.addObjectAccessor(this,"pattern");
		Hemi.object.addObjectAccessor(this,"fact");
		Hemi.object.addObjectAccessor(this,"operation");
	},
	policy : function(s){
		if((v = this.getPolicyByName(s))) return v;
		v = Hemi.newObject("Policy " + s,"1.0",true,true,{
			object_create : function(){
				var _o = this.getObjects(),o
				Hemi.object.addObjectAccessor(this,"rule");
				var g = AM6Client.make("GROUP", "DATA",uwm.getPathForType("Policy"));
				_o.policy = AM6Client.getByName("POLICY",g.objectId,s);
				if(_o.policy == null){
					o = new org.cote.objects.policyType();
					o.enabled = true;
					o.description = "";
					o.score = 0;
					o.logicalOrder = 0;
					o.decisionAge = 0;
					o.condition = "ALL";
					o.name = s;
					o.nameType = "POLICY";
					o.createdDate = new Date();
					o.modifiedDate = new Date();
					o.expiresDate = new Date();
					o.rules = [];
					o.groupPath = g.path;
					if(AM6Client.update("POLICY",o)){
						_o.policy = AM6Client.getByName("POLICY",g.objectId,s);
//						if(!_o.policy.populated) _o.policy = accountManagerRule.populate(_o.policy);
					}
					else{
						delete _o.policy;
					}
					
				}
			},
			promise : function(){
				return this.getObjects().promise;
			},
			define : function(){
				var oPolicy = this;
				return new Promise((res,rej)=>{
					oPolicy.promise().then(()=>{
						var oPolicyObj = oPolicy.getPolicy();
						AM6Client.define(oPolicyObj.objectId,function(s,v){
							if(v && v.json) res(v.json);
							rej(null);
						});
					});
				});
			},
			view : function(){
				return azn.view("Policy", this);
			},
			getPolicy : function(){ return this.getObjects().policy; },
			object : function(){ return this.getPolicy(); },
			modify : function(v){
				return azn.modify("POLICY", this, v);
			},

			getChildRule : function(s){
				var p = this.getPolicy(),o,i=0;
				for(;i < p.rules.length;i++){
					if(p.rules[i].name == s){
						o = p.rules[i];
						break;
					}
				}
				return o;
				
			},
			rule : function(s){
				var sRuleName = s;
				var oR = azn.rule(s);
				var oP = this;
				var oRP = oR.promise();
				var oCP = this.promise();
				this.getObjects().promise = new Promise((res,rej)=>{
					/// Policy create / lookup
					oCP.then(()=>{
						var p = oP.getPolicy();
						/// Rule create / lookup
						oRP.then(() => {
							/// Add rule association
							if(!oP.getChildRule(sRuleName)){
								if(p.rules == null || !p.rules) p.rules = [];
								p.rules.push(oR.getRule());
								AM6Client.update("POLICY",p, function(xs,xv){
									if(xv && xv.json) res(oR);
									else rej(oR);
								});
							}
							else{
								res(oR);
							}
						});
					})
				});
				return oR;
			},

			delete : function(){
				var _o = this.getObjects(), b=0;
				if(_o.policy){
					b = AM6Client.delete("POLICY",this.getObjects().policy.objectId);
					delete _o.policy;
				}
				this.destroy();
				return b;
			}
		});
		this.addNewPolicy(v,s);
		return v;
		
	},
	rule : function(s){

		if((v = this.getRuleByName(s))) return v;
		v = Hemi.newObject("Rule " + s,"1.0",true,true,{
			object_create : function(){
				var _o = this.getObjects(),o;
				var g = AM6Client.make("GROUP", "DATA",uwm.getPathForType("Rule"));
				_o.rule = AM6Client.getByName("RULE",g.objectId,s);
				if(_o.rule == null){

					o = new org.cote.objects.ruleType();
					
					o.name = s;
					o.nameType = "RULE";
					o.description = "";
					o.score = 0;
					o.logicalOrder = 0;
					o.ruleType = "PERMIT";
					o.condition ="ALL";
					o.patterns = [];
					o.rules = [];
					o.groupPath = g.path;
					
					
					if(AM6Client.update("RULE",o)){
						_o.rule = AM6Client.getByName("RULE",g.objectId,s);
					}
				}
			},
			promise : function(){
				return this.getObjects().promise;
			},
			view : function(){
				return azn.view("Rule", this);
			},
			getRule : function(){ return this.getObjects().rule; },
			object : function(){ return this.getRule(); },
			modify : function(v){
				return azn.modify("RULE", this, v);
			},

			getChildPattern : function(s){
				var p = this.getRule(),o,i=0;
				for(;i < p.patterns.length;i++){
					if(p.patterns[i].name == s){
						o = p.patterns[i];
						break;
					}
				}
				return o;
				
			},
			delete : function(){
				var _o = this.getObjects(), b=0;
				if(_o.rule){
					b = AM6Client.delete("RULE",this.getObjects().rule.objectId);
					delete _o.rule;
				}
				this.destroy();
				return b;
			},

			pattern : function(s){
				var sPatternName = s;
				var oP = azn.pattern(s);
				var oR = this;
				var oPP = oP.promise();
				var oRP = this.promise();
				this.getObjects().promise = new Promise((res,rej)=>{
					/// Rule create / lookup
					oRP.then(()=>{
						var p = oR.getRule();
						/// Pattern create / lookup
						oPP.then(() => {
							/// Add pattern association
							if(!oR.getChildPattern(sPatternName)){
								if(p.patterns == null || !p.patterns) p.patterns = [];
								p.patterns.push(oP.getPattern());
								AM6Client.update("RULE",p, function(xs,xv){
									if(xv && xv.json) res(oP);
									else rej(oP);
								});
							}
							else{
								res(oP);
							}
						});
					})
				});
				return oP;
			}
		});
		this.addNewRule(v,s);
		return v;
	},
	pattern : function(s){

		if((v = this.getPatternByName(s))) return v;
		v = Hemi.newObject("Pattern " + s,"1.0",true,true,{
			object_create : function(){
				var _o = this.getObjects(), o;
				var g = AM6Client.make("GROUP", "DATA",uwm.getPathForType("Pattern"));
				_o.pattern = AM6Client.getByName("PATTERN",g.objectId,s);
				if(_o.pattern == null){

					o = new org.cote.objects.patternType();
					
					o.name = s;
					o.description = "";
					o.nameType = "PATTERN";
					o.score = 0;
					o.logicalOrder = 0;
					o.patternType ="EXPRESSION";
					o.comparator = "EQUALS";
					o.groupPath = g.path;

					if(AM6Client.update("PATTERN",o)){
						_o.pattern = AM6Client.getByName("PATTERN",g.objectId,s);
					}
				}
			},
			promise : function(){
				return this.getObjects().promise;
			},
			view : function(){
				return azn.view("Pattern", this);
			},

			getPattern : function(){ return this.getObjects().pattern; },
			object : function(){ return this.getPattern(); },
			modify : function(v){
				return azn.modify("PATTERN", this, v);
			},
			citeUrn : function(s, sP){
				var oF = azn.fact(s),oP = this;
				var oFP = oF.promise();
				var oPP = oP.promise();
				this.getObjects().promise = new Promise((res,rej)=>{
					oPP.then(()=>{
						var p = oP.getPattern();
						oFP.then(()=>{
							var r = oF.getFact();
							if(!r) rej(oF);
							if(p[sP] != r.urn){
								p[sP] = r.urn;
								AM6Client.update("PATTERN", p, function(hs, hv){
									if(hv && hv.json) res(oF);
									else rej(oF);
								});;
							}
							else{
								res(oF);
							}
						});
					});
				});

				return oF;
			},
			fact : function(s){
				return this.citeUrn(s, "factUrn");
			},
			match : function(s){
				return this.citeUrn(s, "matchUrn");
			},

			delete : function(){
				var _o = this.getObjects(), b=0;
				if(_o.pattern){
					b = AM6Client.delete("PATTERN",this.getObjects().pattern.objectId);
					delete _o.pattern;
				}
				this.destroy();
				return b;
			}
		});
		this.addNewPattern(v,s);
		return v;
	},
	fact : function(s){

		if((v = this.getFactByName(s))) return v;
		v = Hemi.newObject("Fact " + s,"1.0",true,true,{
			object_create : function(){
				var _o = this.getObjects(),o;
				var g = AM6Client.make("GROUP","DATA",uwm.getPathForType("Fact"));
				_o.fact = AM6Client.getByName("FACT",g.objectId,s);
				if(_o.fact == null){
					o = new org.cote.objects.factType();
					
					o.name = s;
					o.nameType = "FACT";
					o.description = "";
					o.sourceDataType = null;
					o.sourceType = null;
					o.score = 0;
					o.factoryType = "UNKNOWN";
					o.factType = "STATIC";
					o.sourceUrn = null;
					o.sourceUrl = null;
					o.factData = null;
					o.factReference = null;
					o.groupPath = g.path;
					if(AM6Client.update("FACT",o)){
						_o.fact = AM6Client.getByName("FACT",g.objectId,s);
					}
				}
			},
			
			getFact : function(){ return this.getObjects().fact; },
			view : function(){
				return azn.view("Fact", this);
			},

			promise : function(){
				return this.getObjects().promise;
			},
			object : function(){ return this.getFact(); },
			modify : function(v){
				return azn.modify("FACT", this, v);
			},

			delete : function(){
				var _o = this.getObjects(), b=0;
				if(_o.fact){
					b = AM6Client.delete("FACT",this.getObjects().fact.objectId);
					delete _o.fact;
				}
				this.destroy();
				return b;
			}
		});
		this.addNewFact(v,s);
		return v;
	},
	define : function(s){
		var oPolicy = azn.policy(s);
		return oPolicy.define();
	},
	createPolicyRequest : function(d, s){
		if(!d){
			Hemi.logError("Missing policy definition");
			return;
		}
		var r = new org.cote.objects.policyRequestType();
		r.urn = d.urn;
		r.requestType = (s ? s : "DECIDE");
		r.organizationPath = AM6Client.currentOrganization;
		r.facts = [];
		if(d.parameters && d.parameters.length){
			for(var i = 0; i < d.parameters.length;i++){
				var f = new org.cote.objects.factType();
				for(var v in d.parameters[i]) f[v] = d.parameters[i][v];
				r.facts.push(f);
			}
		}
		return r;
	},
	modify : function(t, o, v){
		var oCP = o.getObjects().promise;
		o.getObjects().promise = new Promise((res,rej)=>{
			oCP.then(()=>{
				var p = o.object();
				for(var i in v) p[i] = v[i];
				AM6Client.update(t, p, function(hs, hv){
					if(hv && hv.json) res(o);
					else{
						rej(o);
					}
				});
				return o;
			});
		});
		return o;
	},
	view : function(sType, oObj){
		return new Promise((res,rej)=>{
			oObj.promise().then(()=>{
				var o = oObj.object();
				var oProps = {parentRef:0,defaultPath:0,openerId:0,listType:sType,picker:0,viewType:o,listId:0};
				Hemi.app.createWindow(o.name , uwm.getApiTypeView(sType) + "/Forms/" + sType  + ".xml", "View-" + o.id, 0, 0, oProps)
				.then((oW)=>{
		            if (oW) {
		            	oW.resizeTo((v ? 700 : 475), (v ? 500 : 400));
		            	Hemi.app.getWindowManager().then((oM)=>{oM.CenterWindow(oW);});;
		            	oW.setHideOnClose(0);
		            	res(oW);
		            }
		        });
			});
		});
	}


});
}());
