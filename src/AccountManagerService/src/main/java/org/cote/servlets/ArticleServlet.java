package org.cote.servlets;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.service.util.MediaOptions;
import org.cote.accountmanager.service.util.MediaUtil;

public class ArticleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static int defCacheSeconds = 7200;
	public static final Logger logger = LogManager.getLogger(ArticleServlet.class);
    public ArticleServlet() {
        super();
        
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long expiry = new Date().getTime() + (defCacheSeconds*1000);
		
		response.setHeader("Cache-Control", "public,max-age="+ defCacheSeconds);
		response.setDateHeader("Expires", expiry);
		response.setCharacterEncoding("UTF-8");
	    
		MediaOptions options = new MediaOptions();
		options.setUseTemplate(true);
		options.setEncodeData(true);
		options.setTemplatePath("WEB-INF/resource/dwacTemplate.html");
		options.setTemplateContentType("text/html");
		try{
			MediaUtil.writeBinaryContent(request, response, options);
		}
		catch(IOException e){
			logger.error(e.getMessage());
			response.sendError(404);
		}
	}
	
	@Override
	public long getLastModified(HttpServletRequest req) {
		  return 0;
	}

}