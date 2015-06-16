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
package org.cote.rest.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

import org.cote.accountmanager.objects.ContactInformationType;

import org.cote.beans.CryptoBean;
import org.cote.beans.DataBean;
import org.cote.beans.DirectoryBean;
import org.cote.beans.EntitySchema;
import org.cote.beans.GroupBean;
import org.cote.beans.MessageBean;
import org.cote.beans.SchemaBean;
import org.cote.beans.SessionBean;
import org.cote.beans.SessionDataBean;



import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;

@Provider
//@Produces("application/json")
public class JAXBJSONContextResolver 
implements ContextResolver<JAXBContext>
{

    private JAXBContext context;
    private Class[] types = {SessionDataBean.class, ContactInformationType.class, CryptoBean.class,SessionBean.class,EntitySchema.class, DataBean.class, DirectoryBean.class, MessageBean.class, SchemaBean.class};

    public JAXBJSONContextResolver() throws Exception {
    	/*
    	System.out.println("JSon BeanResolver");
        Map props = new HashMap<String, Object>();
        props.put(JSONJAXBContext.JSON_NOTATION, JSONJAXBContext.JSONNotation.MAPPED);
        props.put(JSONJAXBContext.JSON_ROOT_UNWRAPPING, Boolean.TRUE);

        this.context = new JSONJAXBContext(types, props);
		*/
        JSONConfiguration.MappedBuilder b = JSONConfiguration.mapped();
    	//JSONConfiguration.MappedJettisonBuilder b = JSONConfiguration.mappedJettison();
        //b.nonStrings("id");
        b.rootUnwrapping(false);
    	
    	//JSONConfiguration.NaturalBuilder b = JSONConfiguration.natural();
    	
    	//JSONConfiguration.Builder b = JSONConfiguration.
        //JSONConfiguration.
        //b.arrays("distribution");
        context = new JSONJAXBContext(b.build(), types);
    }

    public JAXBContext getContext(Class<?> objectType) {
        return (types[0].equals(objectType)) ? context : null;
    	//return context;
    }
}