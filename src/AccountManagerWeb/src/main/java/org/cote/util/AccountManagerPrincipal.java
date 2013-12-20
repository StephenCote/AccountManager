package org.cote.util;

import java.io.Serializable;
import java.security.Principal;

public class AccountManagerPrincipal implements Principal,Serializable {
		private static final long serialVersionUID = 11110L;  
		private long _id = 0L;
	    private String _name = null;
	    private String _organizationPath = null;
	 
	    public AccountManagerPrincipal(String name){
	    	_name = name;
	    	_organizationPath = "/Public";
	    }
	    public AccountManagerPrincipal(String name, String organizationPath) {
	        _name = name;
	        _organizationPath = organizationPath;
	    }
	    public AccountManagerPrincipal(long id, String name, String organizationPath) {
	        _name = name;
	        _organizationPath = organizationPath;
	        _id = id;
	    }
	 
	    /*
	    public boolean equals(Object another) {
	        return ((AccountManagerPrincipal)another).getName().equals(_name);
	    }
	    */
	    public boolean equals(Object o) {
	        if (o == null)
	            return false;

	        if (this == o)
	            return true;

	        if (!(o instanceof AccountManagerPrincipal))
	            return false;
	        AccountManagerPrincipal that = (AccountManagerPrincipal)o;

	        if (this.getName().equals(that.getName()))
	            return true;
	        return false;
	    }
	 
	 
	    public long get_id() {
			return _id;
		}

		public String getName() {
	        return _name;
	    }
	 
	    public String getOrganizationPath(){
	    	return _organizationPath;
	    }
	 
	    public int hashCode() {
	        return _name.hashCode();
	    }
	 
	 
	    public String toString() {
	        return "[AccountManagerPrincipal] : " + _name;
	    }
	 
	}

