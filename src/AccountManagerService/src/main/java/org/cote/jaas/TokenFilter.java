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
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.security.AM5SigningKeyResolver;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
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
	    	String token = stringToken.substring(idx + 7, stringToken.length()).trim();
	    	String urn = Jwts.parser().setSigningKeyResolver(new AM5SigningKeyResolver()).parseClaimsJws(token).getBody().getId();
	    	UserType user = null;
	    	try{
		    	INameIdFactory iFact = Factories.getFactory(FactoryEnumType.USER);
		    	user = iFact.getByUrn(urn);

		    	if(user != null){
					iFact.denormalize(user);
		    		didChain = true;
		    		chain.doFilter(new AM5RequestWrapper(user, (HttpServletRequest)request), response);
		    	}
		    	else {
		    		logger.warn("Null user for urn " + urn);
		    	}
	    	}
	    	catch(FactoryException | ArgumentException f){
	    		logger.error(f);
	    	}	    	
	    }
	    else {
	    	/// logger.warn("No bearer");
	    }
	    if(didChain == false){
	    	chain.doFilter(request, response);
	    }
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
