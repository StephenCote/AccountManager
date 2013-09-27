package org.cote.rest.servlets;

import java.io.ByteArrayInputStream;
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
import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.services.DataServiceImpl;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.MimeUtil;
import org.cote.accountmanager.util.ServiceUtil;
import org.cote.accountmanager.util.StreamUtil;

/**
 * Servlet implementation class MediaFormServlet
 */
public class MediaFormServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final Logger logger = Logger.getLogger(MediaFormServlet.class.getName());
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MediaFormServlet() {
        super();
        // TODO Auto-generated constructor stub
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
			e.printStackTrace();
		}

		logger.error("Media info:");
		logger.error(name);
		logger.error(description);
		logger.error(mimeType);
		logger.error("Group id = " + groupId);
		logger.error("Data size = " + data.length);


		UserType user = ServiceUtil.getUserFromSession(request);
		if(user != null){
			try{
				DataType newData = new org.cote.accountmanager.objects.DataType();
				if(groupId > 0) newData.setGroup((DirectoryGroupType)Factories.getGroupFactory().getById(groupId,user.getOrganization()));
				newData.setName(name);
				newData.setDescription(description);
				newData.setMimeType(mimeType);
				DataUtil.setValue(newData,  data);
				bBit = DataServiceImpl.add(newData,request);
			}
			catch(FactoryException fe){
				logger.error(fe.getMessage());
				fe.printStackTrace();
			} catch (DataException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
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
