package org.cote.jaas;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.rocket.Factories;

import io.jsonwebtoken.Jwts;

public class TokenFilter implements Filter{
	private static final Logger logger = LogManager.getLogger(TokenFilter.class);
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException{
	    HttpServletRequest req = (HttpServletRequest) request;
	    String stringToken = req.getHeader("Authorization");
	    boolean didChain = false;
	    
	    int idx = -1;
	    if (stringToken != null && (idx = stringToken.indexOf("Bearer")) > -1) {
	            //throw new ServletException("Authorization header not found");
	    	String token = stringToken.substring(idx + 7, stringToken.length()).trim();
	    	logger.info("Filtering: '" + token + "'");
	    	String urn = Jwts.parser().setSigningKeyResolver(new AM5SigningKeyResolver()).parseClaimsJws(token).getBody().getId();
	    	logger.info("Processing: " + urn);
	    	INameIdFactory iFact = Factories.getFactory(FactoryEnumType.USER);
	    	UserType user = iFact.getByUrn(urn);
	    	if(user != null){
	    		try {
					iFact.denormalize(user);
				} catch (ArgumentException | FactoryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		didChain = true;
	    		chain.doFilter(new AM5RequestWrapper(user, (HttpServletRequest)request), response);
	    	}
	    	
	    }
	    if(didChain == false){
	    	chain.doFilter(request, response);
	    }
	    //stringToken = stringToken.substring(authorizationSchema.length()).trim();
	    /*
	    <YourLibraryJWT> jwtToken = <YourLibraryJWTParser>.parse(stringToken);
	    if (!<YourLibraryJWTVerifier.verify(jwtToken)){
	      throw new Exception("JWT corrupt");
	    }
	    */
	    //chain.doFilter(request,response);
	  }

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}
}
