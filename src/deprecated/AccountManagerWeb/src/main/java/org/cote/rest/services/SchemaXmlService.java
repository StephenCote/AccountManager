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

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import org.cote.beans.MessageBean;

import com.sun.jersey.api.json.JSONConfiguration;

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