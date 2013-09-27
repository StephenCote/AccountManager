package org.cote.rest.services;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.util.DataUtil;
import org.cote.beans.DataBean;
import org.cote.beans.EntitySchema;
import org.cote.beans.MessageBean;
import org.cote.beans.SchemaBean;
import org.cote.rest.schema.ServiceSchemaBuilder;
import org.cote.rest.schema.ServiceSchemaMethod;
import org.cote.rest.schema.ServiceSchemaMethodParameter;
import java.util.UUID;

@Path("/message")
public class MessageService{
	private static SchemaBean schemaBean = null;
	
	public MessageService(){
		//JSONConfiguration.mapped().rootUnwrapping(false).build();
	}
	@POST @Path("/postMessage") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public MessageBean postMessage(MessageBean bean){
		MessageBean out_msg = new MessageBean();
		out_msg.setId(UUID.randomUUID().toString());
		out_msg.setName("Received " + (bean.getName() != null ? bean.getName() : "[no name]"));
		System.out.println("Received: (" + bean.getGuid() + ") " + bean.getName() + ":" + bean.getData());
		return out_msg;
	}
	
	@POST @Path("/postDataMessage") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public MessageBean postDataMessage(DataBean bean){
		MessageBean out_msg = new MessageBean();
		out_msg.setId(UUID.randomUUID().toString());
		out_msg.setName("Received " + (bean.getName() != null ? bean.getName() : "[no name]"));
		
		try {
			System.out.println("Received: " + bean.getName() + ":" + DataUtil.getValueString(bean));
		} catch (DataException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
			out_msg.setData(e.getMessage());
			
		}
		return out_msg;
	}
	
	@GET @Path("/getDataMessage") @Produces(MediaType.APPLICATION_JSON)
	public DataBean getDataMessage(){
		DataBean out_msg = new DataBean();
		out_msg.setId((long)0);
		out_msg.setName("Example data");
		try {
			DataUtil.setValue(out_msg, "abc".getBytes());
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return out_msg;
	}
	
	 @GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }



}