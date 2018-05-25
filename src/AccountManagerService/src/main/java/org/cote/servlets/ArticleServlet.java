package org.cote.servlets;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cote.accountmanager.service.util.MediaOptions;
import org.cote.accountmanager.service.util.MediaUtil;

public class ArticleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static int defCacheSeconds = 7200;
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
		MediaUtil.writeBinaryContent(request, response, options);
	}
	
	@Override
	public long getLastModified(HttpServletRequest req) {
		  return 0;
	}

}