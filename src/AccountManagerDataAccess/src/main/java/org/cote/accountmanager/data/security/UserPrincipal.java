/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.data.security;

import java.io.Serializable;
import java.security.Principal;

import org.cote.accountmanager.objects.UserType;

public class UserPrincipal extends UserType implements Principal,Serializable {
		private static final long serialVersionUID = 11110L;  
	 
	    public UserPrincipal(String name){
	    	this.name = name;
	    	this.organizationPath = "/Public";
	    }
	    public UserPrincipal(String name, String organizationPath) {
	        this.name = name;
	        this.organizationPath = organizationPath;
	    }
	    public UserPrincipal(long id, String name, String organizationPath) {
	        this.name = name;
	        this.organizationPath = organizationPath;
	        this.id = id;
	    }
	 

	    public boolean equals(Object o) {
	        if (o == null)
	            return false;

	        if (this == o)
	            return true;

	        if (!(o instanceof UserPrincipal))
	            return false;
	        UserPrincipal that = (UserPrincipal)o;

	        if (this.getName().equals(that.getName()))
	            return true;
	        return false;
	    }
	 
	    @Override
	    public int hashCode() {
	        return name.hashCode();
	    }
	 
	    @Override
	    public String toString() {
	        return "[UserPrincipal] : " + organizationPath + "/" + name + " (#" + id + ")";
	    }
	 
	}

