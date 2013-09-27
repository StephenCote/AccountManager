package org.cote.rest.servlets;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cote.beans.MediaOptions;
import org.cote.util.MediaUtil;

/**
 * Servlet implementation class ThumbnailServlet
 */
public class ThumbnailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static int defCacheSeconds = 7200;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ThumbnailServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		long expiry = new Date().getTime() + (defCacheSeconds*1000);
		
	    response.setDateHeader("Expires", expiry);
	    response.setHeader("Cache-Control", "max-age="+ defCacheSeconds);
	    
		MediaOptions options = new MediaOptions();
		options.setThumbHeight(128);
		options.setThumbWidth(128);
		options.setThumbnail(true);
		MediaUtil.writeBinaryContent(request, response, options);
	}
	public long getLastModified(HttpServletRequest req) {
		  return 0;
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
