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
import javax.security.auth.login.FailedLoginException;
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
	
    // initial state
    protected Subject _subject;
    protected CallbackHandler _callbackHandler;
    protected Map _sharedState;
    protected Map _options;
 
    // configuration options
    protected boolean _debug; 
 
    // the authentication status
    protected boolean _succeeded;
    protected boolean _commitSucceeded;
 
    // username
    protected String _name;
    protected String orgPath;
 
    protected Principal[] _authPrincipals;
 
    protected static Map<String, String> _roleMap = null;
    protected static String authenticatedRole = null;
    public static void setAuthenticatedRole(String s){
    	authenticatedRole = s;
    }
    
    /// NOTE: RoleMap set in RestServiceConfig
    ///
    public static Map<String, String> getRoleMap(){
    	if(_roleMap != null) return _roleMap;
    	_roleMap = new HashMap<>();
    	return _roleMap;
    }
    
    public static void setRoleMap(Map<String,String> map){
    	_roleMap = map;
    }
    /**
     * Initialize this <code>LoginModule</code>.
     * <p/>
     * <p/>
     *
     * @param subject         the <code>Subject</code> to be authenticated. <p>
     * @param callbackHandler a <code>CallbackHandler</code> for communicating
     *                        with the end user (prompting for usernames and
     *                        passwords, for example). <p>
     * @param sharedState     shared <code>LoginModule</code> state. <p>
     * @param options         options specified in the login
     *                        <code>Configuration</code> for this particular
     *                        <code>LoginModule</code>.
     */
    public void initialize(Subject subject,
                           CallbackHandler callbackHandler,
                           Map sharedState,
                           Map options) {
        this._subject = subject;
        this._callbackHandler = callbackHandler;
        this._sharedState = sharedState;
        this._options = options;
 
        // initialize any configured options
        _debug = "true".equalsIgnoreCase((String) _options.get("debug"));
 
        if (debug()) {
            printConfiguration(this);
        }
    }
 
 
    final public boolean debug() {
        return _debug;
    }
 
 
    protected Principal[] getAuthPrincipals() {
        return _authPrincipals;
    }
 
 
    /**
     * Authenticate the user by prompting for a username and password.
     * <p/>
     * <p/>
     *
     * @return true if the authentication succeeded, or false if this
     *         <code>LoginModule</code> should be ignored.
     * @throws FailedLoginException if the authentication fails. <p>
     * @throws LoginException       if this <code>LoginModule</code>
     *                              is unable to perform the authentication.
     */
    public boolean login() throws LoginException {
        if (debug())
            logger.info("\t\t[AM5LoginModule] login");
 
        if (_callbackHandler == null)
            throw new LoginException("Error: no CallbackHandler available " +
                    "to garner authentication information from the user");

        // Setup default callback handlers.
        Callback[] callbacks = new Callback[] {
        	//new TextInputCallback("Organization: ","/Public"),
            new NameCallback("Username: "),
            new PasswordCallback("Password: ", false)
            
        };
 
        try {
            _callbackHandler.handle(callbacks);
        } catch (Exception e) {
            _succeeded = false;
            throw new LoginException(e.getMessage());
        }
 
        //HttpServletRequest request = null;
        /*
        //HttpServletResponse response = null;
        try {
			request = (HttpServletRequest) PolicyContext.getContext("javax.servlet.http.HttpServletRequest");
		} catch (PolicyContextException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			throw new LoginException("Unable to obtain request context");
		}
        */
        
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
        //request.getParameter("j_organizationpath");
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

					//SessionSecurity.login(ServiceUtil.getSessionId(request),username, CredentialEnumType.HASHED_PASSWORD,password, orgType.getId());

		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}

        if (debug())
        {
            logger.info("\t\t[AM5LoginModule] username : " + username);
        }
        if(user != null){
        	_succeeded = true;
        	_name = username;
        	_authPrincipals = new UserPrincipal[1];
        	_authPrincipals[0] = new UserPrincipal(user.getId(), username, orgPath);
        }
 
        ((PasswordCallback)callbacks[1]).clearPassword();
        callbacks[0] = null;
        callbacks[1] = null;
 
        if (debug())
        {
            logger.info("\t\t[AM5LoginModule] success : " + _succeeded);
        }
 
        if (!_succeeded)
            throw new LoginException
                            ("Authentication failed: Password does not match");
 
        return true; 
    }
 
 
    /**
     * <p> This method is called if the LoginContext's
     * overall authentication succeeded
     * (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules
     * succeeded).
     * <p/>
     * <p> If this LoginModule's own authentication attempt
     * succeeded (checked by retrieving the private state saved by the
     * <code>login</code> method), then this method associates a
     * <code>Principal</code>
     * with the <code>Subject</code> located in the
     * <code>LoginModule</code>.  If this LoginModule's own
     * authentication attempted failed, then this method removes
     * any state that was originally saved.
     * <p/>
     * <p/>
     *
     * @return true if this LoginModule's own login and commit
     *         attempts succeeded, or false otherwise.
     * @throws LoginException if the commit fails.
     */
    public boolean commit()
            throws LoginException {
        try {
 
            if (_succeeded == false) {
                return false;
            }
 
            if (_subject.isReadOnly()) {
                throw new LoginException("Subject is ReadOnly");
            }
 
            // add authenticated principals to the Subject

            if (getAuthPrincipals() != null) {
                for (int i = 0; i < getAuthPrincipals().length; i++) {
                    if(!_subject.getPrincipals().contains(getAuthPrincipals()[i])){
                    	logger.info("Adding principle to subject: '" + getAuthPrincipals()[i].getName());
                  
                        _subject.getPrincipals().add(getAuthPrincipals()[i]);
                        /// debug test
                        _subject.getPrincipals().addAll(getRoleSets((UserPrincipal)getAuthPrincipals()[0]));
                    }
                    else{
                    	logger.info("Don't add principle to subject: '" + getAuthPrincipals()[i].getName());
                    }
                }
            }
 
            // in any case, clean out state
            cleanup();
            if (debug()) {
                printSubject(_subject);
            }
 
            _commitSucceeded = true;
            return true;
 
        } catch (Throwable t) {
            if (debug()) {
                logger.info(t.getMessage());
                logger.error(FactoryException.LOGICAL_EXCEPTION,t);
            }
            throw new LoginException(t.toString());
        }
    }
 

    public static List<RolePrincipal> getRoleSets(UserPrincipal uprince) throws LoginException, FactoryException, ArgumentException
    {
            //String[] roles = new String[]{"admin"};
            //Group[] groups = {new AccountManagerGroup("Roles")};
    		Map<String,String> map = getRoleMap();
    		List<RolePrincipal> oroles = new ArrayList<>();
    		List<UserRoleType> roles = ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getUserRoles(uprince);
    		logger.info("Retrieved " + roles.size() + " roles");
            for(BaseRoleType brole : roles) {
            	if(map.containsKey(brole.getName())){
            		RolePrincipal role = new RolePrincipal(map.get(brole.getName()));
            		oroles.add(role);
            	}
                //groups[0].addMember(role);
            }
            if(authenticatedRole != null && map.containsKey(authenticatedRole)){
            	oroles.add(new RolePrincipal(map.get(authenticatedRole)));
            }
            //if(true) throw new LoginException("Whatever");
            logger.info("Returning " + oroles.size() + " roles mapped against " + _roleMap.size());
            return oroles;
            //.toArray(new RolePrincipal[0]);
    }

    /**
     * <p> This method is called if the LoginContext's
     * overall authentication failed.
     * (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules
     * did not succeed).
     * <p/>
     * <p> If this LoginModule's own authentication attempt
     * succeeded (checked by retrieving the private state saved by the
     * <code>login</code> and <code>commit</code> methods),
     * then this method cleans up any state that was originally saved.
     * <p/>
     * <p/>
     *
     * @return false if this LoginModule's own login and/or commit attempts
     *         failed, and true otherwise.
     * @throws LoginException if the abort fails.
     */
    public boolean abort() throws LoginException {
        if (debug()) {
            logger.info
                     ("\t\t[AM5LoginModule] aborted authentication attempt.");
        }
 
        if (_succeeded == false) {
            cleanup();
            return false;
        } else if (_succeeded && _commitSucceeded == false) {
            // login succeeded but overall authentication failed
            _succeeded = false;
            cleanup();
        } else {
            // overall authentication succeeded and commit succeeded,
            // but someone else's commit failed
            logout();
        }
        return true;
    }
 
 
    protected void cleanup() {
        _name = null;
        /*
        if (_password != null) {
            for (int i = 0; i < _password.length; i++) {
                _password[i] = ' ';
            }
            _password = null;
        }
        */
    }
 
 
    protected void cleanupAll() {
        cleanup();
 
        if (getAuthPrincipals() != null) {
            for (int i = 0; i < getAuthPrincipals().length; i++) {
                _subject.getPrincipals().remove(getAuthPrincipals()[i]);
            }
        }
    }
 
 
    /**
     * Logout the user.
     * <p/>
     * <p> This method removes the <code>Principal</code>
     * that was added by the <code>commit</code> method.
     * <p/>
     * <p/>
     *
     * @return true in all cases since this <code>LoginModule</code>
     *         should not be ignored.
     * @throws LoginException if the logout fails.
     */
    public boolean logout() throws LoginException {
        _succeeded = false;
        _commitSucceeded = false;
        cleanupAll();
        return true;
    }
 
    // helper methods //
 
    protected static void printConfiguration(AM5LoginModule slm) {
        if (slm == null) {
            return;
        }
        logger.info("\t\t[AM5LoginModule] configuration options:");
        if (slm.debug()) {
            logger.info("\t\t\tdebug = " + slm.debug());
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
