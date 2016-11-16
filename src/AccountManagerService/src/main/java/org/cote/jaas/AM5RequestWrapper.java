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
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.security.RolePrincipal;
import org.cote.accountmanager.data.security.UserPrincipal;
import org.cote.accountmanager.objects.UserType;

public class AM5RequestWrapper extends HttpServletRequestWrapper {
	public static final Logger logger = LogManager.getLogger(AM5RequestWrapper.class);
 
  private UserPrincipal principal = null;
  private List<String> roles = new ArrayList<>();
  private HttpServletRequest request = null;
   
  public AM5RequestWrapper(UserType user, HttpServletRequest inRequest) {
    super(inRequest);
    this.principal = new UserPrincipal(user.getId(), user.getName(), user.getOrganizationPath());
    try {
		List<RolePrincipal> rpl = AM5LoginModule.getRoleSets(principal);
		//logger.info("Roles: " + rpl.size());
		for(RolePrincipal r : rpl){
			//logger.info("Role: " + r.getName());
			roles.add(r.getName());
		}
	} catch (LoginException | FactoryException | ArgumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    //this.roles = inRoles;
    this.request = inRequest;


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