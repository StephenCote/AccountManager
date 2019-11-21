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
package org.cote.jaas;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.security.RolePrincipal;
import org.cote.accountmanager.data.security.UserPrincipal;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.UserType;

public class AM5RequestWrapper extends HttpServletRequestWrapper {
	public static final Logger logger = LogManager.getLogger(AM5RequestWrapper.class);
 
  private UserPrincipal principal = null;
  private List<String> roles = new ArrayList<>();
   
  public AM5RequestWrapper(UserType user, HttpServletRequest inRequest) {
    super(inRequest);
    this.principal = new UserPrincipal(user.getId(), user.getName(), user.getOrganizationPath());
    try {
		List<RolePrincipal> rpl = AM5LoginModule.getRoleSets(principal);
		for(RolePrincipal r : rpl){
			roles.add(r.getName());
		}
	} catch (LoginException | FactoryException | ArgumentException e) {
		logger.error(e);
	}



  }
 
  @Override
  public boolean isUserInRole(String role) {
    return roles.contains(role);
  }
 
  @Override
  public Principal getUserPrincipal() {
	 return principal;
  }
}