package org.cote.util;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountManagerGroup implements Group {
	private Map<String,Principal> principals = null;
	private String name = null;
	public AccountManagerGroup(String n){
		principals = new HashMap<String,Principal>();
		name = n;
	}
	
	public String getName() {
		return name;
	}

	public boolean removeMember(Principal p){
		principals.remove(p.getName());
		return true;
	}
	public boolean isMember(Principal p){
		return principals.containsKey(p.getName());
	}
	public boolean addMember(Principal p){
		principals.put(p.getName(), p);
		return true;
	}
	public Enumeration<Principal> members(){
		
		return Collections.enumeration(principals.values());
	}
}
