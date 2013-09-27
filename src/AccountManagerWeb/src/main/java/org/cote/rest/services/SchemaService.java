package org.cote.rest.services;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.cote.beans.EntitySchema;
import org.cote.beans.SchemaBean;
import org.cote.rest.schema.ServiceSchemaBuilder;
import org.cote.rest.schema.ServiceSchemaMethod;

@Path("/schema")
public class SchemaService{
	private static SchemaBean schemaBean = null;
	private static EntitySchema ent = null;
	public SchemaService(){
		//JSONConfiguration.mapped().rootUnwrapping(false).build();
	}

	 @GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
	 
	 @GET @Path("/entity") @Produces(MediaType.APPLICATION_JSON)
	 public EntitySchema getEntitySchema(){
		 if(ent == null){
			 ent = new EntitySchema();
			 //populateDataSchema(ent.getDataBeanSchema());
		 }
		 return ent;
	 }

	 @GET 
	 @Produces(MediaType.APPLICATION_JSON) 
	 //@Context UriInfo ui
	 public SchemaBean get(){

		 return new SchemaBean();
		 
	 }

 
 


}