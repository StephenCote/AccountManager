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
package org.cote.rest.servlets;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.service.util.ServiceUtil;
import org.cote.accountmanager.services.DataServiceImpl;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.MimeUtil;
import org.cote.accountmanager.util.StreamUtil;

/**
 * Servlet implementation class MediaFormServlet
 */
public class MediaFormServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final Logger logger = LogManager.getLogger(MediaFormServlet.class);
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MediaFormServlet() {
        super();
        
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		boolean bBit = false;
		
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload();
		String responseId = null;
		String name = null;
		String description = null;
		String mimeType = null;
		long groupId = 0;
		String groupPath = null;
		String orgPath = null;
		long id = 0;
		byte[] data = new byte[0];
		
		
		// Parse the request
		try{
			FileItemIterator iter = upload.getItemIterator(request);
			
			while (iter.hasNext()) {
			    FileItemStream item = iter.next();

			    String fname = item.getFieldName();
			    InputStream stream = item.openStream();
			    if (item.isFormField()) {
			    	if(fname.equals("responseId")){
			    		responseId = Streams.asString(stream);
			    	}
			    	else if(fname.equals("description")){
			    		description = Streams.asString(stream);
			    	}
			    	else if(fname.equals("groupId")){
			    		groupId = Long.parseLong(Streams.asString(stream));
			    	}
			    	else if(fname.equals("groupPath")){
			    		groupPath = Streams.asString(stream);
			    	}
			    	else if(fname.equals("organizationPath")){
			    		orgPath = Streams.asString(stream);
			    	}
			    	else if(fname.equals("name")){
			    		name = Streams.asString(stream);
			    	}
			    	else if(fname.equals("id")){
			    		id = Long.parseLong(Streams.asString(stream));
			    	}
			    } else {
			        data = StreamUtil.getStreamBytes(stream);
			        mimeType = MimeUtil.getType(item.getName());
			    }
			}
		}
		catch(Exception e){
			logger.error("Error: " + e.getMessage());
			logger.error("Error",e);
		}

		/*
		logger.error("Media info:");
		logger.error(name);
		logger.error(description);
		logger.error(mimeType);
		logger.error("Group id = " + groupId);
		logger.error("Data size = " + data.length);
		*/
		System.out.println("Media info:");
		System.out.println(name);
		System.out.println(description);
		System.out.println(mimeType);
		System.out.println("Group id = " + groupId);
		System.out.println("Group path = " + groupPath);
		System.out.println("Data size = " + data.length);
		UserType user = ServiceUtil.getUserFromSession(request);
		if(user != null){
			try{
				DataType newData = new org.cote.accountmanager.objects.DataType();
				if(groupId > 0L) newData.setGroupId(groupId);
				newData.setGroupPath(groupPath);
				newData.setOrganizationPath(orgPath);
				newData.setNameType(NameEnumType.DATA);
				newData.setName(name);
				newData.setDescription(description);
				newData.setMimeType(mimeType);
				DataUtil.setValue(newData,  data);
				bBit = DataServiceImpl.add(newData,request);
			}
			 catch (DataException e) {
				
				logger.error(e.getMessage());
				logger.error("Error",e);
			} 
		}
		
		response.setContentType("text/html");
		response.getWriter().write("<html><head><title>Media Form</title><script type = \"text/javascript\">" + getResponseScript(responseId, bBit) + "</script></head>");
		response.getWriter().write("</html>");
		response.flushBuffer();
	}
	private static String getResponseScript(String responseId, boolean success){
		StringBuffer buff = new StringBuffer();
		buff.append("window.onload = Init;");
		buff.append("function Init(){");
		buff.append("if(window != window.parent && typeof window.parent.Hemi == \"object\"){");
        buff.append("window.parent.Hemi.message.service.publish(\"frame_response\",{id:\"" + responseId + "\",status:" + (success ? "true":"false") + "});");
		buff.append("}");
		buff.append("}");
		buff.append("</script>");
		return buff.toString();
	}

}
