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
package org.cote.accountmanager.service.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.data.services.UserService;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.util.AMCodeUtil;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.StreamUtil;

public class ArticleUtil {
	public static final Logger logger = LogManager.getLogger(ArticleUtil.class);
	private static final Pattern articlePattern = Pattern.compile("^\\/([\\sA-Za-z0-9\\.]+)\\/([%-_\\/\\s\\.A-Za-z0-9]+)$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
			//Pattern.compile("^\\/([%-_\\/\\s\\.A-Za-z0-9]+)$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private static final Pattern headerLinkPattern = Pattern.compile("\\<h1(?:\\s*)\\>((.|\\n|\\r)*?)\\</h1(?:\\s*)\\>");
	private static String articleTemplate = null;
	private static String articleSectionTemplate = null;
	private static String articleMetaDataTemplate = null;
	private static String articleNavBackTemplate = null;
	private static String articleNavForwardTemplate = null;
	protected static final String[] ARTICLE_ROLES = new String[]{
			"BlogAuthor",
			"ArticleAuthor"
		};
		
	protected static int MAX_RECORD_COUNT = 3;
		
	/// Note: The patterns are different between the article and media utilities
	/// The article patterns are simplified to reduce the URL length and make discovery simpler
	protected static final Map<String,UserRoleType> roles = new HashMap<>();
		


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
	
	public static String getArticleMetaDataTemplate(ServletContext context){
		if(articleMetaDataTemplate != null) return articleMetaDataTemplate;
		articleMetaDataTemplate = getResourceFromParam(context, "template.article.meta");
		return articleMetaDataTemplate;
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
		String outStr = null;
		try {
			BufferedInputStream bis = new BufferedInputStream(context.getResourceAsStream(context.getInitParameter(paramName)));
			outStr = StreamUtil.streamToString(bis);
			bis.close();
		} catch (IOException e) {
			
			logger.error("Error",e);
		}
		return outStr;
	}
	
	public static void writeBinaryContent(HttpServletRequest request, HttpServletResponse response, MediaOptions options) throws IOException{
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "ArticleRead", AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		String path = request.getPathInfo();
		AuditService.targetAudit(audit, AuditEnumType.UNKNOWN, path);
		if(path == null || path.length() == 0){
			AuditService.denyResult(audit, "Path is null or empty");
			response.sendError(404);
			return;
		}
		
		
		Matcher m = articlePattern.matcher(path);
		if(!m.find() || m.groupCount() != 2){
			AuditService.denyResult(audit, "Unexpected path construct");
			response.sendError(404);
			return;
		}
		
		/// Supported prefix patterns are:
		///		/OrgPath/[ArticleType]/[User/SubPath]
		///			EG: Article/Public/Blog/Steve
		///		[DefaultOrgPath]/[ArticleType]/[User/SubPath]
		///			EG: /Blog/Steve
		
		
		String orgPath = "/" + m.group(1).trim().replace('.', '/');
		String sBaseDir = options.getMediaBase();
		/// SubPath ==
		///   0 : UserName
		///	  1 : Article Name
		///	If 1 is empty, then it's a list
		///

		String[] subPath = m.group(2).split("/");
		
		if(orgPath.length() == 0 || sBaseDir.length() == 0 || subPath.length == 0){
			AuditService.denyResult(audit, "Type, path, or name did not contain a value");
			response.sendError(404);
			return;
		}

		long organizationId = 0L;
		UserType user = null;
		UserType targUser = null;
		UserRoleType role = null;
		DirectoryGroupType dir = null;
		try{
			OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(orgPath);
			if(org == null){
				AuditService.denyResult(audit, "Organization is invalid: '" + orgPath + "'");
				response.sendError(404);
				return;
			}
			organizationId = org.getId();

			user = ServiceUtil.getUserFromSession(request);
			if(user == null){
				user = Factories.getDocumentControl(organizationId);
				if(user == null){
					AuditService.denyResult(audit, "Null user identified");
					response.sendError(404);
					return;
				}
			}
			
			targUser = Factories.getNameIdFactory(FactoryEnumType.USER).getByName(subPath[0], organizationId);
			if(targUser == null){
				AuditService.denyResult(audit, "User is invalid: '" + subPath[0] + "'");
				response.sendError(404);
				return;
			}
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(targUser);
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName(sBaseDir, targUser.getHomeDirectory(), targUser.getOrganizationId());
			if(dir == null){
				AuditService.denyResult(audit, "Content directory is null for " + targUser.getName() + ": '~/" + sBaseDir + "'");
				response.sendError(404);
				return;
			}
			
			/// This role check is in here more to stop people from driving random tests into the system
			/// So if a user isn't in this role, they obviously don't want to share anything this way, so stop checking
			///
			role = RoleService.getArticleAuthorUserRole(org.getId());
			if(RoleService.getIsUserInEffectiveRole(role, targUser) == false){
				AuditService.denyResult(audit, "User " + subPath[0] + " is not an authorized author in : '" + sBaseDir + "Author' role");
				response.sendError(404);
				return;
			}
			
			/// Finally, make sure the requesting user has read access to the directory
			///
			if(!AuthorizationService.canView(user, dir)){
				AuditService.denyResult(audit, "User " + user.getName() + " is not authorized to view '" + dir.getName() + ".  NOTE: This may stem from an authenticated user other than the owner not having explicit rights, where the anonymous case does through Document Control.  Need to make sure the directory has rights for both public users as well as document control.");
				response.sendError(404);
				return;
				
			}

		}
		catch(FactoryException | ArgumentException e) {
			logger.error(e.getMessage());
			logger.error("Error",e);
		}
		
		String name = null;
		if(subPath.length > 1) name = subPath[1].trim();
		List<DataType> articleData = new ArrayList<DataType>();
		
		long startIndex = 0;
		int recordCount = MAX_RECORD_COUNT;
		int totalCount = 0;
		String navBack = "";
		String navForward = "";
		boolean singleMode = false;
		if(name == null || name.length() == 0){
			String pageStr = request.getParameter("page");
			long page = 0;
			if(pageStr != null && pageStr.matches("^\\d+$")){
				page = (Long.parseLong(pageStr)-1);
				startIndex = page * recordCount;
			}
			AuditType caudit = AuditService.beginAudit(ActionEnumType.READ, "Count " + sBaseDir + " items", AuditEnumType.USER, user.getName());
			AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getUrn());
			totalCount = BaseService.count(caudit, AuditEnumType.DATA, user, dir);
			logger.info("Page = " + pageStr + " / " + startIndex + " / " + recordCount);
			if(startIndex < 0) startIndex = 0;
			if(startIndex >= totalCount) startIndex = totalCount - recordCount;
			
			String urlBase = "/AccountManagerService/article" + orgPath + "/" + targUser.getName();
			
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
			
			articleData = BaseService.listByGroup(AuditEnumType.DATA, "DATA", dir.getObjectId(), startIndex, recordCount, user);
		
		}
		/// Single mode
		else{
			singleMode = true;
			DataType data = BaseService.readByName(AuditEnumType.DATA,dir,name,user);
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
		DataType profile = UserService.getProfile(targUser);
		String blogTitle = Factories.getAttributeFactory().getAttributeValueByName(profile, "blog.title");
		String blogSubtitle = Factories.getAttributeFactory().getAttributeValueByName(profile, "blog.subtitle");
		String author = Factories.getAttributeFactory().getAttributeValueByName(profile, "blog.signature");
		if(blogTitle == null || blogTitle.length() == 0) blogTitle = targUser.getName() + "'s Blog";
		if(blogSubtitle == null) blogSubtitle = "";
		
		if(singleMode == false) template = template.replaceAll("%PAGETITLE%",blogTitle);
		template = template.replaceAll("%TITLE%",blogTitle);
		template = template.replaceAll("%SUBTITLE%", blogSubtitle);
		template = template.replaceAll("%AUTHOR_USERNAME%",targUser.getName());
		StringBuilder buff = new StringBuilder();

		for(int i = 0; i < articleData.size();i++){
			String section = getArticleSectionTemplate(request.getServletContext());
			String meta = getArticleMetaDataTemplate(request.getServletContext());
			if(section == null || section.length() == 0){
				AuditService.denyResult(audit, "Failed to load section template");
				response.sendError(404);
				return;
			}
			if(meta == null || meta.length() == 0){
				AuditService.denyResult(audit, "Failed to load metadata template");
				response.sendError(404);
				return;
			}

			DataType data = articleData.get(i);
			if(data.getDetailsOnly()){
				data = BaseService.readByObjectId(AuditEnumType.DATA, data.getObjectId(), user);
			}
			try {
				/// For lists, inject [h1] and [h2] if they don't already exist based on the data name and description
				///
				StringBuilder preface = new StringBuilder();
				String contentDataStr = DataUtil.getValueString(data);
				if(contentDataStr.indexOf("[h1]") == -1) preface.append("[h1]" + data.getName() + "[/h1]");
				if(contentDataStr.indexOf("[h2]") == -1 && data.getDescription() != null && data.getDescription().length() > 0) preface.append("[h2]" + data.getDescription() + "[/h2]");
				String contentStr = AMCodeUtil.decodeAMCodeToHtml(preface.toString() + contentDataStr);
				String linkUrl = "/AccountManagerService/article" + orgPath + "/" + targUser.getName() + "/" + data.getName();
				Matcher headerM = headerLinkPattern.matcher(contentStr);
				/// this is an error if it doesn't find because it was just added when missing
				///
				if(headerM.find()){
					/// If single mode, change the page title to be that of the article
					///
					if(singleMode == true) template = template.replaceAll("%PAGETITLE%",headerM.group(1));
					/// otherwise, add a link to the single instance
					///
					else contentStr = headerM.replaceFirst("<h1><a class = \"uwm-content-title-link\" href = \"" + linkUrl + "\">$1</a></h1>");
				}
				String metaStr = "Written by " + author + " on " + CalendarUtil.exportDateAsString(CalendarUtil.getDate(data.getCreatedDate()), "yyyy/MM/dd");
				meta = meta.replace("%META%", metaStr);

				section = section.replace("%CONTENT%", contentStr);
				section = section.replace("%META%", meta);
				buff.append(section + "\n");
			} catch (DataException e) {
				
				logger.error(e.getMessage());
				logger.error("Error",e);
			}
			catch(Exception e){
				logger.error(e.getMessage());
				logger.error("Error",e);
				
			}

		} /// end for
		
		template = template.replace("%NAVIGATION%",navBack + navForward);
		template = template.replace("%CONTENT%", buff.toString());
		
		response.setContentType("text/html; charset=UTF-8");
		response.setContentLength(template.length());
		response.getWriter().write(template);
		response.flushBuffer();

	}
}
