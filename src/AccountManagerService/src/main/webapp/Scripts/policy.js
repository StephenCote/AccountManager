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
			define : function(){
				return AM6Client.define(this.getPolicy().objectId);
			},
			getPolicy : function(){ return this.getObjects().policy; },
			modify : function(v){
				var p = this.getPolicy();
				for(var i in v) p[i] = v[i];
				return AM6Client.update("POLICY",p);
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
				var r = azn.rule(s),p = this.getPolicy();
				if(!r || !r.getObjects().rule) return 0;
//				if(!p.populated) p = accountManagerRule.populatePolicy(p);
				if(!this.getChildRule(s)){
					if(p.rules == null || !p.rules) p.rules = [];
					p.rules.push(r.getRule());
					AM6Client.update("POLICY",p);
				}
				//r.getObjects().policy = this;
				return r;
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
			getRule : function(){ return this.getObjects().rule; },
			modify : function(v){
				var p = this.getRule();
				for(var i in v) p[i] = v[i];
				return AM6Client.update("RULE",p);
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
				var r = azn.pattern(s),p = this.getRule();
				if(!r || !r.getObjects().pattern) return 0;
//				if(!p.populated) p = accountManagerRule.populateRule(p);
				if(!this.getChildPattern(s)){
					if(p.patterns == null || !p.patterns) p.patterns = [];
					p.patterns.push(r.getPattern());
					AM6Client.update("RULE",p);
				}
				//r.getObjects().rule = this;
				return r;
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
			getPattern : function(){ return this.getObjects().pattern; },
			modify : function(v){
				var p = this.getPattern();
				for(var i in v) p[i] = v[i];
				return AM6Client.update("PATTERN", p);
			},
			fact : function(s){
				var r = azn.fact(s),p = this.getPattern();
				if(!r || !r.getObjects().fact) return 0;
				if(p.factUrn != r.getFact().urn){
					p.factUrn = r.getFact().urn;
					AM6Client.update("PATTERN", p);
				}

				return r;
			},
			match : function(s){
				var r = azn.fact(s),p = this.getPattern();
				if(!r || !r.getObjects().fact) return 0;
				if(p.matchUrn != r.getFact().urn){
					p.matchUrn = r.getFact().urn;
					AM6Client.update("PATTERN", p);
				}

				return r;
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
			modify : function(v){
				var p = this.getFact();
				for(var i in v) p[i] = v[i];
				return AM6Client.update("FACT", p);
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
	}

});
}());
