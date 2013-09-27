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