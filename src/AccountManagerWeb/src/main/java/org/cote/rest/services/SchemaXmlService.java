package org.cote.rest.services;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

import org.cote.accountmanager.objects.types.*;
import org.cote.accountmanager.objects.MessageType;
import org.cote.accountmanager.objects.SchemaType;
import org.cote.beans.MessageBean;
import org.cote.beans.SchemaBean;

import com.sun.jersey.api.json.JSONConfiguration;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.UUID;

@Path("/schemaxml")
public class SchemaXmlService{
	public SchemaXmlService(){
		JSONConfiguration.mapped().rootUnwrapping(false).build();
	}
	 @GET 
	 @Produces(MediaType.APPLICATION_XML) 
	 //@Context UriInfo ui
	 public MessageBean get(){
		 MessageBean msg = new MessageBean();
		 msg.setId("111");
		 msg.setName("222");
		 msg.setData("333");
		 return msg;
	 }
	 /*
	 public SchemaBean get() {
		 SchemaBean st = new SchemaBean();
		 MessageType msg = new MessageType();
		 msg.setId("Demo Message");
		 st.setMessageType(msg);
		 st.setId("Example");
		 st.setEncodingType(MediaType.APPLICATION_JSON);
		 return st;
	 }
	 */

	 @POST
	 @Consumes(MediaType.APPLICATION_XML)
	 @Produces(MediaType.APPLICATION_XML) 
	 public MessageBean post(JAXBElement<MessageBean> in_bean) {
	 //public MessageBean post(MessageBean msg){
		 MessageBean msg = in_bean.getValue();
		 MessageBean out_msg = new MessageBean();
		 out_msg.setId(UUID.randomUUID().toString());
		 out_msg.setName("Confirmation");
		 out_msg.setData("Received message: " + (msg == null ? "Null" : msg.getId()));
		 if(msg == null){
			 System.out.println("MessageBean is null");
		 }
		 else{
			 System.out.println("Message: " + msg.getId() + ":" + msg.getName() + ":" + msg.getData());
		 }
		 return out_msg;
	 }  
 
 


}