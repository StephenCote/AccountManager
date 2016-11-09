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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.util.DataUtil;
import org.cote.beans.DataBean;
import org.cote.beans.MessageBean;

@Path("/message")
public class MessageService{
	private static SchemaBean schemaBean = null;
	public static final Logger logger = LogManager.getLogger(MessageService.class);
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
			
			System.out.println(e.getMessage());
			logger.error("Error",e);
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
			
			logger.error("Error",e);
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