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
package org.cote.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.services.BaseService;
import org.cote.accountmanager.services.DataServiceImpl;
import org.cote.accountmanager.util.AMCodeUtil;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.GraphicsUtil;
import org.cote.accountmanager.util.ServiceUtil;
import org.cote.accountmanager.util.StreamUtil;
import org.cote.beans.MediaOptions;

public class ArticleUtil {

	public static String articleTemplate = null;
	public static String articleSectionTemplate = null;
	public static String articleNavBackTemplate = null;
	public static String articleNavForwardTemplate = null;
	public static String getArticleTemplate(ServletContext context){
		if(articleTemplate != null) return articleTemplate;
		articleTemplate = getResourceFromParam(context, "template.article");
		return articleTemplate;
	}
	public static String getArticleSectionTemplate(ServletContext context){
		if(articleSectionTemplate != null) return articleSectionTemplate;
		articleSectionTemplate = getResourceFromParam(context, "template.article.section");
		return articleSectionTemplate;
	}
	public static String getArticleNavBackTemplate(ServletContext context){
		if(articleNavBackTemplate != null) return articleNavBackTemplate;
		articleNavBackTemplate = getResourceFromParam(context, "template.article.navback");
		return articleNavBackTemplate;
	}
	public static String getArticleNavForwardTemplate(ServletContext context){
		if(articleNavForwardTemplate != null) return articleNavForwardTemplate;
		articleNavForwardTemplate = getResourceFromParam(context, "template.article.navforward");
		return articleNavForwardTemplate;
	}

	public static String getResourceFromParam(ServletContext context,String paramName){
		String out_str = null;
		try {
			BufferedInputStream bis = new BufferedInputStream(context.getResourceAsStream("/WEB-INF/" + context.getInitParameter(paramName)));
			out_str = StreamUtil.streamToString(bis);
			bis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out_str;
	}
	public static String[] ARTICLE_ROLES = new String[]{
		"BlogAuthor",
		"ArticleAuthor"
	};
	public static int MAX_RECORD_COUNT = 3;
	/// Note: The patterns are different between the article and media utilities
	/// The article patterns are simplified to reduce the URL length and make discovery simpler

	public static final Logger logger = Logger.getLogger(ArticleUtil.class.getName());
	/// public static UserRoleType blogRole = null;
	public static Map<String,UserRoleType> roles = new HashMap<String,UserRoleType>();
	
	///private static Pattern recPattern = Pattern.compile("^\\/([A-Za-z0-9\\.]+)\\/([\\w]+)\\/([%-_\\/\\s\\.A-Za-z0-9]+)$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private static Pattern articlePattern = Pattern.compile("^\\/([%-_\\/\\s\\.A-Za-z0-9]+)$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	public static UserRoleType getRoleByType(String type, OrganizationType org){
		return getRoleByName(type + "Author",org);
	}
	
	public static UserRoleType getRoleByName(String name, OrganizationType org){
		String key = org.getId() + "-" + name;
		if(roles.containsKey(key)) return roles.get(key);
		UserRoleType role = null;
		try {
			UserType adminUser = Factories.getUserFactory().getUserByName("Admin", org);
			role = Factories.getRoleFactory().getCreateUserRole(adminUser, name, null);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(role != null){
			roles.put(key, role);
		}
		return role;

	}
	
	public static void writeBinaryContent(HttpServletRequest request, HttpServletResponse response, MediaOptions options) throws IOException{
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "ArticleRead", AuditEnumType.SESSION, request.getSession(true).getId());
		String path = request.getPathInfo();
		AuditService.targetAudit(audit, AuditEnumType.UNKNOWN, path);
		if(path == null || path.length() == 0){
			AuditService.denyResult(audit, "Path is null or empty");
			response.sendError(404);
			return;
		}
		
		
		logger.info("Article Path: " + path);
		Matcher m = articlePattern.matcher(path);
		if(!m.find() || m.groupCount() != 1){
			AuditService.denyResult(audit, "Unexpected path construct");
			response.sendError(404);
			return;
		}
		
		/// Supported prefix patterns are:
		///		/OrgPath/[ArticleType]/[User/SubPath]
		///			EG: Article/Public/Blog/Steve
		///		[DefaultOrgPath]/[ArticleType]/[User/SubPath]
		///			EG: /Blog/Steve
		
		
		String orgPath = request.getServletContext().getInitParameter("organization.default");
		String type = options.getMediaBase();
		/// SubPath ==
		///   0 : UserName
		///	  1 : Article Name
		///	If 1 is empty, then it's a list
		///
		String[] subPath = m.group(1).split("/");
		
		if(orgPath.length() == 0 || type.length() == 0 || subPath.length == 0){
			AuditService.denyResult(audit, "Type, path, or name did not contain a value");
			response.sendError(404);
			return;
		}

		OrganizationType org = null;
		UserType user = null;
		UserType targUser = null;
		UserRoleType role = null;
		DirectoryGroupType dir = null;
		try{
			org = Factories.getOrganizationFactory().findOrganization(orgPath);
			if(org == null){
				AuditService.denyResult(audit, "Organization is invalid: '" + orgPath + "'");
				response.sendError(404);
				return;
			}

			user = ServiceUtil.getUserFromSession(request);
			if(user == null) user = Factories.getDocumentControl(org);
			
			targUser = Factories.getUserFactory().getUserByName(subPath[0], org);
			if(targUser == null){
				AuditService.denyResult(audit, "User is invalid: '" + subPath[0] + "'");
				response.sendError(404);
				return;
			}
			Factories.getUserFactory().populate(targUser);
			dir = Factories.getGroupFactory().getDirectoryByName(type, targUser.getHomeDirectory(), targUser.getOrganization());
			if(dir == null){
				AuditService.denyResult(audit, "Content directory is null for " + targUser.getName() + ": '~/" + type + "'");
				response.sendError(404);
				return;
			}
			
			/// This role check is in here more to stop people from driving random tests into the system
			/// So if a user isn't in this role, they obviously don't want to share anything this way, so stop checking
			///
			role = getRoleByType(type,org);
			if(RoleService.getIsUserInEffectiveRole(role, targUser) == false){
				AuditService.denyResult(audit, "User " + subPath[0] + " is not an authorized author in : '" + type + "Author' role");
				response.sendError(404);
				return;
			}
			
			/// Finally, make sure the requesting user has read access to the directory
			///
			if(AuthorizationService.canViewGroup(user, dir) == false){
				AuditService.denyResult(audit, "User " + user.getName() + " is not authorized to view '" + dir.getName() + ".  NOTE: This may stem from an authenticated user other than the owner not having explicit rights, where the anonymous case does through Document Control.  Need to make sure the directory has rights for both public users as well as document control.");
				response.sendError(404);
				return;
				
			}

		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		String name = null;
		if(subPath.length > 1) name = subPath[1].trim();
		List<DataType> articleData = new ArrayList<DataType>();
		
		long startIndex = 0;
		int recordCount = MAX_RECORD_COUNT;
		int totalCount = 0;
		StringBuffer navBuff = new StringBuffer();
		/// List Mode
		String navBack = "";
		String navForward = "";

		if(name == null || name.length() == 0){
			String pageStr = request.getParameter("page");
			int page = 0;
			if(pageStr != null && pageStr.matches("^\\d+$")){
				page = (Integer.parseInt(pageStr)-1);
				startIndex = page * recordCount;
			}
			AuditType caudit = AuditService.beginAudit(ActionEnumType.READ, "Count " + type + " items", AuditEnumType.USER, user.getName());
			AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getPath());
			totalCount = BaseService.count(caudit, AuditEnumType.DATA, user, dir, request);
			logger.info("Page = " + pageStr + " / " + startIndex + " / " + recordCount);
			if(startIndex < 0) startIndex = 0;
			if(startIndex >= totalCount) startIndex = totalCount - recordCount;
			
			String urlBase = "/AccountManager/" + type + "/" + targUser.getName();
			
			if((startIndex + recordCount) < totalCount){
				navForward = getArticleNavForwardTemplate(request.getServletContext());
				navForward = navForward.replaceAll("%FORWARD_URL%", urlBase + "?page=" + (page+2));
			}
			if(page > 0){
				navBack = getArticleNavBackTemplate(request.getServletContext());
				navBack = navBack.replaceAll("%BACK_URL%", urlBase + (page > 1 ? "?page=" + (page) : ""));
			}
			

			
			ProcessingInstructionType instruction = new ProcessingInstructionType();
			instruction.setOrderClause("createddate DESC");
			try {
				articleData = DataServiceImpl.getListByGroup(dir, instruction, false, startIndex, recordCount);
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				AuditService.denyResult(audit, "Error: " + e.getMessage());
				response.sendError(404);
				e.printStackTrace();
				return;
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				AuditService.denyResult(audit, "Error: " + e.getMessage());
				response.sendError(404);
				e.printStackTrace();
				return;
			}
		}
		/// Single mode
		else{
			DataType data = (DataType)BaseService.readByName(audit,AuditEnumType.DATA,user,dir,name,request);
			if(data == null){
				AuditService.denyResult(audit, "Null data returned for " + name);
				response.sendError(404);
				return;
			}
			articleData.add(data);
		}
		if(articleData == null){
			AuditService.denyResult(audit, "Null data list returned - this is an internal error");
			response.sendError(404);
			return;
		}
		
		String template = getArticleTemplate(request.getServletContext());
		if(template == null || template.length() == 0){
			AuditService.denyResult(audit, "Failed to load template");
			response.sendError(404);
			return;
		}
		DataType profile = DataServiceImpl.getProfile(targUser);
		String blogTitle = Factories.getAttributeFactory().getAttributeValueByName(profile, "blog.title");
		String blogSubtitle = Factories.getAttributeFactory().getAttributeValueByName(profile, "blog.subtitle");
		String author = Factories.getAttributeFactory().getAttributeValueByName(profile, "blog.signature");
		if(blogTitle == null || blogTitle.length() == 0) blogTitle = targUser.getName() + "'s Blog";
		if(blogSubtitle == null) blogSubtitle = "";
		
		//template = template.replaceAll("%TITLE%", (name != null && name.length() > 0 ? name : blogTitle));
		template = template.replaceAll("%TITLE%",blogTitle);
		template = template.replaceAll("%SUBTITLE%", blogSubtitle);
		template = template.replaceAll("%AUTHOR_USERNAME%",targUser.getName());
		StringBuffer buff = new StringBuffer();

		for(int i = 0; i < articleData.size();i++){
			
			String section = getArticleSectionTemplate(request.getServletContext());
			if(section == null || section.length() == 0){
				AuditService.denyResult(audit, "Failed to load section template");
				response.sendError(404);
				return;
			}

			DataType data = articleData.get(i);
			try {
				/// For lists, inject [h1] and [h2] if they don't already exist based on the data name and description
				///
				StringBuffer preface = new StringBuffer();
				String contentDataStr = DataUtil.getValueString(data);
				if(contentDataStr.indexOf("[h1]") == -1) preface.append("[h1]" + data.getName() + "[/h1]");
				if(contentDataStr.indexOf("[h2]") == -1 && data.getDescription() != null && data.getDescription().length() > 0) preface.append("[h2]" + data.getDescription() + "[/h2]");
				String contentStr = AMCodeUtil.decodeAMCodeToHtml(preface.toString() + contentDataStr);
				section = section.replace("%CONTENT%", contentStr);
				buff.append(section + "\n");
			} catch (DataException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			}
			catch(Exception e){
				logger.error(e.getMessage());
				e.printStackTrace();
				
			}
		}
		
		template = template.replace("%NAVIGATION%",navBack + navForward);
		template = template.replace("%CONTENT%", buff.toString());
		
		response.setContentType("text/html; charset=UTF-8");
		response.setContentLength(template.length());
		response.getWriter().write(template);
		response.flushBuffer();

	}
}
