/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.util;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Iterator;
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
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.service.util.ServiceUtil;

public class AccountManagerLoginModule implements LoginModule {
	public static final Logger logger = Logger.getLogger(AccountManagerLoginModule.class.getName());
	//private static String EAI_PASSWORD = "debugdebug";
	
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
 
    // username and password
    protected String _name;
    //protected char[] _password;
    protected String orgPath;
 
    protected Principal[] _authPrincipals;
 
 
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
            System.out.println("\t\t[SampleLoginModule] login");
 
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
 
        HttpServletRequest request = null;
        //HttpServletResponse response = null;
        try {
			request = (HttpServletRequest) PolicyContext.getContext("javax.servlet.http.HttpServletRequest");
		} catch (PolicyContextException e) {
			
			logger.error(e.getStackTrace());
			throw new LoginException("Unable to obtain request context");
		}
        
        String username = ((NameCallback)callbacks[0]).getName();
        String password = new String(((PasswordCallback)callbacks[1]).getPassword());
        String orgPath = request.getParameter("j_organizationpath");
        if(orgPath == null || orgPath.length() == 0){
        	throw new LoginException("Null organization path");
        }
        
        OrganizationType orgType = null;
       // String password_hash = SecurityUtil.getSaltedDigest(password);
        UserType user = null;

        try {
			orgType = Factories.getOrganizationFactory().findOrganization(orgPath);
	        if(orgType == null){
	        	throw new LoginException("Organization is null for path: '" + orgPath);
	        }
			user = SessionSecurity.login(ServiceUtil.getSessionId(request),username, CredentialEnumType.HASHED_PASSWORD,password, orgType.getId());

		} catch (FactoryException e) {
			
			logger.error(e.getStackTrace());
		} catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
		}

        if (debug())
        {
            System.out.println("\t\t[SampleLoginModule] username : " + username);
        }
        if(user != null){
        	_succeeded = true;
        	_name = username;
        	//_password = password.toCharArray();
        	_authPrincipals = new AccountManagerPrincipal[1];
        	_authPrincipals[0] = new AccountManagerPrincipal(user.getId(), username, orgPath);
        	//_authPrincipals[1] = new AccountManagerPrincipal("AuthorizedUser");
        	//ServiceUtil.addCookie(response,"OrganizationId",Long.toString(orgType.getId()));
        }
 
        ((PasswordCallback)callbacks[1]).clearPassword();
        callbacks[0] = null;
        callbacks[1] = null;
 
        if (debug())
        {
            System.out.println("\t\t[SampleLoginModule] success : " + _succeeded);
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
                    if(!_subject.getPrincipals().contains(getAuthPrincipals()[i]))
{
                    	System.out.println("Adding principle to subject: '" + getAuthPrincipals()[i].getName());
                  
                        _subject.getPrincipals().add(getAuthPrincipals()[i]);
                        /// debug test
                        _subject.getPrincipals().add(getRoleSets()[0]);
                    }
                    else{
                    	System.out.println("Don't add principle to subject: '" + getAuthPrincipals()[i].getName());
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
                System.out.println(t.getMessage());
                logger.error(t.getStackTrace());
            }
            throw new LoginException(t.toString());
        }
    }
 

    protected Group[] getRoleSets() throws LoginException
    {
            String[] roles = new String[]{"AuthorizedUser"};
            Group[] groups = {new AccountManagerGroup("Roles")};
            
            for(int r = 0; r < roles.length; r ++) {
                AccountManagerPrincipal role = new AccountManagerPrincipal(roles[r]);
                groups[0].addMember(role);
            }
            //if(true) throw new LoginException("Whatever");
            System.out.println("Returning " + groups.length + " groups");
            return groups;
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
            System.out.println
                     ("\t\t[SampleLoginModule] aborted authentication attempt.");
        }
 
        if (_succeeded == false) {
            cleanup();
            return false;
        } else if (_succeeded == true && _commitSucceeded == false) {
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
 
    protected static void printConfiguration(AccountManagerLoginModule slm) {
        if (slm == null) {
            return;
        }
        System.out.println("\t\t[SampleLoginModule] configuration options:");
        if (slm.debug()) {
            System.out.println("\t\t\tdebug = " + slm.debug());
        }
    }
 
 
    protected static void printSet(Set s) {
        try {
            Iterator principalIterator = s.iterator();
            while (principalIterator.hasNext()) {
                Principal p = (Principal) principalIterator.next();
                System.out.println("\t\t\t" + p.toString());
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
                System.out.println
                     ("\t\t[SampleLoginModule] added the following Principals:");
                printSet(s);
            }
 
            s = subject.getPublicCredentials();
            if ((s != null) && (s.size() != 0)) {
                System.out.println
              ("\t\t[SampleLoginModule] added the following Public Credentials:");
                printSet(s);
            }
        } catch (Throwable t) {
        }
    }
}
