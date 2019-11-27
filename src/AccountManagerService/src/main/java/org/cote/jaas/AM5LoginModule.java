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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.RoleParticipationFactory;
import org.cote.accountmanager.data.security.RolePrincipal;
import org.cote.accountmanager.data.security.UserPrincipal;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
public class AM5LoginModule implements LoginModule {

	private static final Logger logger = LogManager.getLogger(AM5LoginModule.class);
	
    protected Subject subject;
    protected CallbackHandler callbackHandler;
    protected Map sharedState;
    protected Map options;
    protected boolean debug; 
    protected boolean succeeded;
    protected boolean commitSucceeded;
    protected String name;
    protected String orgPath;
    protected Principal[] authPrincipals;
    protected static Map<String, String> roleMap = null;
    protected static String authenticatedRole = null;

    public static void setAuthenticatedRole(String s){
    	authenticatedRole = s;
    }
    
    /// NOTE: RoleMap set in RestServiceConfig
    ///
    public static Map<String, String> getRoleMap(){
    	if(roleMap != null) return roleMap;
    	roleMap = new HashMap<>();
    	return roleMap;
    }
    
    public static void setRoleMap(Map<String,String> map){
    	roleMap = map;
    }

    public void initialize(Subject sub, CallbackHandler handler, Map state, Map opts) {
        this.subject = sub;
        this.callbackHandler = handler;
        this.sharedState = state;
        this.options = opts;

        debug = "true".equalsIgnoreCase((String) options.get("debug"));
 
        if (debug()) {
            printConfiguration(this);
        }
    }
 
 
    final public boolean debug() {
        return debug;
    }
 
 
    protected Principal[] getAuthPrincipals() {
        return authPrincipals;
    }
 
    public boolean login() throws LoginException {
        if (debug())
            logger.debug("Begin login");
 
        if (callbackHandler == null) {
            throw new LoginException("Error: no CallbackHandler available to garner authentication information from the user");
        }
        Callback[] callbacks = new Callback[] {
            new NameCallback("Username: "),
            new PasswordCallback("Password: ", false)
        };
 
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            succeeded = false;
            throw new LoginException(e.getMessage());
        }

        String username = ((NameCallback)callbacks[0]).getName();
        String password = new String(((PasswordCallback)callbacks[1]).getPassword());
        String orgPath = null;
        if(username.indexOf("/") > -1){
        	orgPath = username.substring(0, username.lastIndexOf("/"));
        	username = username.substring(username.lastIndexOf("/")+1,username.length());
        }
        else{
        	orgPath = "/Public";
        }

        if(orgPath == null || orgPath.length() == 0){
        	throw new LoginException("Null organization path");
        }
        
        OrganizationType orgType = null;
        UserType user = null;

        try {
			orgType = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(orgPath);
	        if(orgType == null){
	        	throw new LoginException("Organization is null for path: '" + orgPath);
	        }
			user = SessionSecurity.login(username, CredentialEnumType.HASHED_PASSWORD,password, orgType.getId());
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}

        if (debug())
        {
            logger.info("Username : " + username);
        }
        if(user != null){
        	succeeded = true;
        	name = username;
        	authPrincipals = new UserPrincipal[1];
        	authPrincipals[0] = new UserPrincipal(user.getId(), username, orgPath);
        }
 
        ((PasswordCallback)callbacks[1]).clearPassword();
        callbacks[0] = null;
        callbacks[1] = null;
 
        if (debug())
        {
            logger.info("Success : " + succeeded);
        }
 
        if (!succeeded)
            throw new LoginException("Authentication failed: Password does not match");
 
        return true; 
    }
 
    public boolean commit() throws LoginException {
        try {
 
            if (succeeded == false) {
                return false;
            }
 
            if (subject.isReadOnly()) {
                throw new LoginException("Subject is ReadOnly");
            }
            if (getAuthPrincipals() != null) {
                for (int i = 0; i < getAuthPrincipals().length; i++) {
                    if(!subject.getPrincipals().contains(getAuthPrincipals()[i])){
                    	logger.debug("Adding principle to subject: '" + getAuthPrincipals()[i].getName());
                        subject.getPrincipals().add(getAuthPrincipals()[i]);
                        subject.getPrincipals().addAll(getRoleSets((UserPrincipal)getAuthPrincipals()[0]));
                    }
                    else{
                    	logger.debug("Don't add principle to subject: '" + getAuthPrincipals()[i].getName());
                    }
                }
            }

            cleanup();
            if (debug()) {
                printSubject(subject);
            }
 
            commitSucceeded = true;
            return true;
 
        }
        catch (Throwable t) {
            if (debug()) {
                logger.error(FactoryException.LOGICAL_EXCEPTION,t);
            }
            throw new LoginException(t.toString());
        }
    }
 

    public static List<RolePrincipal> getRoleSets(UserPrincipal uprince) throws LoginException, FactoryException, ArgumentException
    {
		Map<String,String> map = getRoleMap();
		List<RolePrincipal> oroles = new ArrayList<>();
		List<UserRoleType> roles = ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getUserRoles(uprince);
		logger.debug("Retrieved " + roles.size() + " roles");
        for(BaseRoleType brole : roles) {
        	if(map.containsKey(brole.getName())){
        		RolePrincipal role = new RolePrincipal(map.get(brole.getName()));
        		oroles.add(role);
        	}
        }
        if(authenticatedRole != null && map.containsKey(authenticatedRole)){
        	oroles.add(new RolePrincipal(map.get(authenticatedRole)));
        }
        logger.debug("Returning " + oroles.size() + " roles mapped against " + roleMap.size());
        return oroles;
    }

     public boolean abort() throws LoginException {
        if (debug()) {
            logger.info("Aborted authentication attempt.");
        }
        if (succeeded == false) {
            cleanup();
            return false;
        } else if (succeeded && commitSucceeded == false) {
            // Login succeeded but authentication failed
            succeeded = false;
            cleanup();
        } else {
            // Authentication succeeded and commit succeeded,
            // but a commit failed
            logout();
        }
        return true;
    }
 
 
    protected void cleanup() {
        name = null;
    }
 
 
    protected void cleanupAll() {
        cleanup();
 
        if (getAuthPrincipals() != null) {
            for (int i = 0; i < getAuthPrincipals().length; i++) {
                subject.getPrincipals().remove(getAuthPrincipals()[i]);
            }
        }
    }
 
    public boolean logout() throws LoginException {
        succeeded = false;
        commitSucceeded = false;
        cleanupAll();
        return true;
    }
 
    protected static void printConfiguration(AM5LoginModule slm) {
        if (slm == null) {
            return;
        }
        logger.info("Configuration options:");
        if (slm.debug()) {
            logger.info("debug = " + slm.debug());
        }
    }
 
 
    protected static void printSet(Set s) {
        try {
            Iterator principalIterator = s.iterator();
            while (principalIterator.hasNext()) {
                Principal p = (Principal) principalIterator.next();
                logger.info("\t\t\t" + p.toString());
            }
        } catch (Throwable t) {
        }
    }
 
 
    protected static void printSubject(Subject subject) {
        try {
            if (subject == null) {
                return;
            }
            Set s = subject.getPrincipals();
            if ((s != null) && (s.size() != 0)) {
                logger.info("\t\t[AM5LoginModule] added the following Principals:");
                printSet(s);
            }
 
            s = subject.getPublicCredentials();
            if ((s != null) && (s.size() != 0)) {
                logger.info("\t\t[AM5LoginModule] added the following Public Credentials:");
                printSet(s);
            }
        } catch (Throwable t) {
        }
    }
}
