(function(){
var policyPath = "~/Policies";
var factPath = "~/Facts";
var patternPath = "~/Patterns";
var functionPath = "~/Functions";
var functionFactPath = "~/FunctionFacts";
var operationPath = "~/Operations";
var rulePath = "~/Rules";

function getUrn(o,s){
	if(!o.populated) o = accountManager.getGroupById(o.id);
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
				var _o = this.getObjects();
				Hemi.object.addObjectAccessor(this,"rule");
				var g = accountManager.getCreatePath(uwm.getPathForType("Policy"), policyPath);
				_o.policy = accountManagerRule.getPolicy(s,g);
				if(_o.policy == null){

					if(accountManagerRule.addPolicy(s,true,"",0,0,"ALL",10,new Date(),new Date(),new Date(),[],g)){
						_o.policy = accountManagerRule.getPolicy(s,g);
						if(!_o.policy.populated) _o.policy = accountManagerRule.populate(_o.policy);
					}
					else{
						delete _o.policy;
					}
					
				}
			},
			getPolicy : function(){ return this.getObjects().policy; },
			modify : function(v){
				var p = this.getPolicy();
				for(var i in v) p[i] = v[i];
				return accountManagerRule.updatePolicy(p);
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
				if(!p.populated) p = accountManagerRule.populatePolicy(p);
				if(!this.getChildRule(s)){
					if(p.rules == null || !p.rules) p.rules = [];
					p.rules.push(r.getRule());
					accountManagerRule.updatePolicy(p);
				}
				//r.getObjects().policy = this;
				return r;
			},
			delete : function(){
				var _o = this.getObjects(), b=0;
				if(_o.policy){
					b = accountManagerRule.deletePolicy(this.getObjects().policy);
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
				var _o = this.getObjects();
				var g = accountManager.getCreatePath(uwm.getPathForType("Rule"), rulePath);
				_o.rule = accountManagerRule.getRule(s,g);
				if(_o.rule == null){
					if(accountManagerRule.addRule(s,"",0,0,"PERMIT","UNKNOWN",0,0,g)){
						_o.rule = accountManagerRule.getRule(s,g);
					}
				}
			},
			getRule : function(){ return this.getObjects().rule; },
			modify : function(v){
				var p = this.getRule();
				for(var i in v) p[i] = v[i];
				return accountManagerRule.updateRule(p);
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
					b = accountManagerRule.deleteRule(this.getObjects().rule);
					delete _o.rule;
				}
				this.destroy();
				return b;
			},
			pattern : function(s){
				var r = azn.pattern(s),p = this.getRule();
				if(!r || !r.getObjects().pattern) return 0;
				if(!p.populated) p = accountManagerRule.populateRule(p);
				if(!this.getChildPattern(s)){
					if(p.patterns == null || !p.patterns) p.patterns = [];
					p.patterns.push(r.getPattern());
					accountManagerRule.updateRule(p);
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
				var _o = this.getObjects();
				var g = accountManager.getCreatePath(uwm.getPathForType("Pattern"), patternPath);
				_o.pattern = accountManagerRule.getPattern(s,g);
				if(_o.pattern == null){
					if(accountManagerRule.addPattern(s,0,0,0,"EXPRESSION","EQUALS",0,0,g)){
						_o.pattern = accountManagerRule.getPattern(s,g);
					}
				}
			},
			getPattern : function(){ return this.getObjects().pattern; },
			modify : function(v){
				var p = this.getPattern();
				for(var i in v) p[i] = v[i];
				return accountManagerRule.updatePattern(p);
			},
			fact : function(s){
				var r = azn.fact(s),p = this.getPattern();
				if(!r || !r.getObjects().fact) return 0;
				if(p.factUrn != r.getFact().urn){
					p.factUrn = r.getFact().urn;
					accountManagerRule.updatePattern(p);
				}

				return r;
			},
			match : function(s){
				var r = azn.fact(s),p = this.getPattern();
				if(!r || !r.getObjects().fact) return 0;
				if(p.matchUrn != r.getFact().urn){
					p.matchUrn = r.getFact().urn;
					accountManagerRule.updatePattern(p);
				}

				return r;
			},
			delete : function(){
				var _o = this.getObjects(), b=0;
				if(_o.pattern){
					b = accountManagerRule.deletePattern(this.getObjects().pattern);
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
				var _o = this.getObjects();
				var g = accountManager.getCreatePath(uwm.getPathForType("Fact"), factPath);
				_o.fact = accountManagerRule.getFact(s,g);
				if(_o.fact == null){
					if(accountManagerRule.addFact(s,"",0,"STATIC","UNKNOWN","UNKNOWN",null,null,null,null,g)){
						_o.fact = accountManagerRule.getFact(s,g);
					}
				}
			},
			getFact : function(){ return this.getObjects().fact; },
			modify : function(v){
				var p = this.getFact();
				for(var i in v) p[i] = v[i];
				return accountManagerRule.updateFact(p);
			},
			delete : function(){
				var _o = this.getObjects(), b=0;
				if(_o.fact){
					b = accountManagerRule.deleteFact(this.getObjects().fact);
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
		var r = new org.cote.beans.policyRequestType();
		r.urn = d.urn;
		r.requestType = (s ? s : "DECIDE");
		r.organizationPath = accountManager.getOrganizationPath();
		r.facts = [];
		if(d.parameters && d.parameters.length){
			for(var i = 0; i < d.parameters.length;i++){
				var f = new org.cote.beans.factType();
				for(var v in d.parameters[i]) f[v] = d.parameters[i][v];
				r.facts.push(f);
			}
		}
		return r;
	}

});
}());
