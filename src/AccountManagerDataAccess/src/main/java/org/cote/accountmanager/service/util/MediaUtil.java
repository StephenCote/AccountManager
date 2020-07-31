/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.VaultBean;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.VaultService;
import org.cote.accountmanager.data.util.UrnUtil;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.util.BinaryUtil;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.GraphicsUtil;
import org.cote.accountmanager.util.StreamUtil;

public class MediaUtil {
	public static final Logger logger = LogManager.getLogger(MediaUtil.class);
	private static Pattern recPattern = Pattern.compile("^\\/([\\sA-Za-z0-9\\.]+)\\/([\\w]+)([%-_\\/\\s\\.A-Za-z0-9]+)$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private static Pattern dimPattern = Pattern.compile("(\\/\\d+x\\d+)$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private static final VaultService vaultService = new VaultService();
	private static int maximumImageWidth = -1;
	private static int maximumImageHeight = -1;
	private static boolean restrictImageSize = false;
	private static boolean allowDataPointers = false;
	private static boolean checkConfig = false;
	private static boolean checkConfigDataPoint = false;
	private static Map<String,String> templateContents = new HashMap<>();
	protected static boolean getRestrictImageSize(HttpServletRequest request){
		if(checkConfig) return restrictImageSize;
		restrictImageSize = getBoolParam(request,"image.restrict.size");
		checkConfig = true;
		return restrictImageSize;
	}
	protected static boolean isAllowDataPointers(HttpServletRequest request){
		if(checkConfigDataPoint) return allowDataPointers;
		checkConfigDataPoint = true;
		allowDataPointers = getBoolParam(request,"data.pointers.enabled");
		return allowDataPointers;
	}
	protected static int getMaximumImageWidth(HttpServletRequest request){ 
		if(maximumImageWidth >= 0) return maximumImageWidth;
		maximumImageWidth = getIntParam(request, "image.maximum.width");
		return maximumImageWidth;
	}
	protected static int getMaximumImageHeight(HttpServletRequest request){ 
		if(maximumImageHeight >= 0) return maximumImageHeight;
		maximumImageHeight = getIntParam(request, "image.maximum.height");
		return maximumImageHeight;
	}
	protected static boolean getBoolParam(HttpServletRequest request, String name){
		boolean ret = false;
		String iV = request.getServletContext().getInitParameter(name);
		if(iV != null && iV.length() > 0){
			ret = Boolean.parseBoolean(iV);
		}
		return ret;
	}
	protected static int getIntParam(HttpServletRequest request, String name){
		int ret = 0;
		String iV = request.getServletContext().getInitParameter(name);
		if(iV != null && iV.length() > 0){
			ret = Integer.parseInt(iV);
		}
		return ret;
	}
	public static void writeBinaryContent(HttpServletRequest request, HttpServletResponse response) throws IOException{
		writeBinaryContent(request, response, new MediaOptions());
	}
	public static void writeBinaryContent(HttpServletRequest request, HttpServletResponse response, MediaOptions options) throws IOException{
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "MediaRead", AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		String path = request.getPathInfo();
		AuditService.targetAudit(audit, AuditEnumType.UNKNOWN, path);
		if(path == null || path.length() == 0){
			AuditService.denyResult(audit, "Path is null or empty");
			response.sendError(404);
			return;
		}
		logger.debug("Media path: " + path);
		Matcher m = recPattern.matcher(path);
		if(!m.find() || m.groupCount() != 3){
			AuditService.denyResult(audit, "Unexpected path construct");
			response.sendError(404);
			return;
		}
		
		String orgPath = "/" + m.group(1).trim().replace('.', '/');
		String type = m.group(2).trim();
		String subPath = m.group(3).trim();
		String name = null;
		int index = 0;
		
		if((index = subPath.lastIndexOf('/')) > -1){
			logger.debug("Testing '" + subPath + "' for dimensions");
			Matcher d = dimPattern.matcher(subPath);
			if(d.find() && d.groupCount() == 1){
				String[] dimPair = d.group(1).trim().replace("/", "").split("x");
				options.setThumbWidth(Integer.parseInt(dimPair[0]));
				options.setThumbHeight(Integer.parseInt(dimPair[1]));
				subPath = d.replaceAll("");
				index = subPath.lastIndexOf('/');
				logger.debug("Adjust path for dimenion information");
				logger.debug("New Path: " + subPath);
			}
			else{
				logger.debug("No alternate dimensions discovered: " + d.groupCount());
			}
			name = subPath.substring(index+1,subPath.length()).trim();
			subPath = subPath.substring(0,index);
		}
		
		if(orgPath.length() == 0 || type.length() == 0 || subPath.length() == 0 || name == null || name.length() == 0){
			AuditService.denyResult(audit, "Type, path, or name did not contain a value");
			response.sendError(404);
			return;
		}
		AuditEnumType aType = AuditEnumType.fromValue(type.toUpperCase());
		AuditService.targetAudit(audit, aType, path);
		writeBinaryContent(request, response, options, audit, aType, orgPath, subPath, name);
	}
	public static void writeBinaryContent(
			HttpServletRequest request,
			HttpServletResponse response,
			MediaOptions options,
			AuditType audit,
			AuditEnumType type,
			String orgPath,
			String objPath,
			String objName
	) throws IOException{
		OrganizationType org = null;
		try{
			org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(orgPath);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		if(org == null){
			AuditService.denyResult(audit, "Organization is invalid: '" + orgPath + "'");
			response.sendError(404);
			return;
		}
		
		UserType user = ServiceUtil.getUserFromSession(request);
		
		if(user == null) user = Factories.getDocumentControl(org.getId());
		writeBinaryContent(request, response, options, audit, type, org, user, objPath, objName);
	}
	public static void writeBinaryContent(
			HttpServletRequest request,
			HttpServletResponse response,
			MediaOptions options,
			AuditType audit,
			AuditEnumType type,
			OrganizationType org,
			UserType user,
			String objPath,
			String objName
	) throws IOException{
		
		DirectoryGroupType dir = null;
		try{
			dir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, objPath, org.getId());
		}
		catch(FactoryException | ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		if(dir == null){
			AuditService.denyResult(audit, "Path '" + objPath + "' is invalid for " + (user == null ? "null user":user.getName()) + " in organization " + org.getName());
			response.sendError(404);
			return;
		}
		writeBinaryContent(request, response, options, audit, type, org, user, dir, objName);
		
	}
	public static void writeBinaryContent(
			HttpServletRequest request,
			HttpServletResponse response,
			MediaOptions options,
			AuditType audit,
			AuditEnumType type,
			OrganizationType org,
			UserType user,
			DirectoryGroupType group,
			String objName
	) throws IOException{
		switch(type){
			case DATA:
				writeBinaryData(request, response, options,audit, type, org, user, group, objName);
				break;
			default:
				AuditService.denyResult(audit, "Unexpected target type");
				response.sendError(404);
				break;
		}
	}
	public static void writeBinaryData(
			HttpServletRequest request,
			HttpServletResponse response,
			MediaOptions options,
			AuditType audit,
			AuditEnumType type,
			OrganizationType org,
			UserType user,
			DirectoryGroupType group,
			String objName
	) throws IOException{
		DataType data = null;
		boolean can_view = false;
		
		/// If the config stipulates a maximum width and height, then force thumbnail size to be no larger
		/// If the optional force image bit is set, force all image requests through the thumbnail mechanism to prevent delivery of the full original resolution
		///
		int maxWidth = getMaximumImageWidth(request);
		int maxHeight = getMaximumImageHeight(request);
		boolean restrictSize = getRestrictImageSize(request);
		if(maxWidth > 0 && maxHeight > 0 && options.isThumbnail()){
			boolean bLim = false;
			if(options.getThumbHeight() > maxHeight){
				bLim = true;
				options.setThumbHeight(maximumImageHeight);
			}
			if(options.getThumbWidth() > maxWidth){
				bLim = true;
				options.setThumbWidth(maximumImageWidth);
			}
			if(bLim){
				logger.info("Limiting width and height to " + maxWidth + "," + maxHeight);
			}
		}
	
		/// If this is a thumbnail request, then:
		/// 1) get the details only data and confirm it's an image
		/// 
	
		
		try{
			if(options.isThumbnail()){
				String thumbName = objName + " " + options.getThumbWidth() + "x" + options.getThumbHeight();
				DirectoryGroupType thumbGroup = null;
				synchronized(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP))){
					thumbGroup = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName(".thumbnail", group, org.getId());
					if(thumbGroup == null && AuthorizationService.canChange(user, group)){
						thumbGroup = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(user, ".thumbnail", group, org.getId());
					}
				}
				if(thumbGroup == null){
					AuditService.denyResult(audit, "The thumbnail group could not be found or created in " + group.getName() + (group.getPath() != null ? " (" + group.getPath() + ")" : ""));
					response.sendError(404);
					return;	
				}

				if(AuthorizationService.canView(user, thumbGroup) == false){
					AuditService.denyResult(audit, "User " + user.getName() + " is not authorized to view the thumbnail group in " + group.getName());
					response.sendError(404);
					return;	
				}

				/// Get the thumbnail data
				data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(thumbName, thumbGroup);
				/// If it doesn't exist, try to create it
				if(data == null){
						DataType chkData = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(objName, true, group);
						if(chkData == null){
							AuditService.denyResult(audit, "Data is invalid: '" + objName + "'");
							response.sendError(404);
							return;
						}
						 if(chkData.getMimeType() == null || chkData.getMimeType().startsWith("image/") == false){
								AuditService.denyResult(audit, "Data type '" + chkData.getMimeType() + " is not supported for thumbnails for " + objName);
								response.sendError(404);
								return;
						 }
						if(AuthorizationService.canView(user, chkData)){
							chkData = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(objName, false, group);
							if(chkData == null || chkData.getDataBytesStore() == null || chkData.getDataBytesStore().length == 0){
								AuditService.denyResult(audit, "Data '" + objName + " was not retrieved in a populated state");
								response.sendError(404);
								return;
							}
							VaultBean vaultBean = null;
							byte[] imageBytes = new byte[0];
							if(chkData.getPointer() && !isAllowDataPointers(request)){
								logger.error("Access to data pointer for thumbnail data is forbidden.");
							}
							else{
								vaultBean = (chkData.getVaulted() ? vaultService.getVaultByUrn(user, chkData.getVaultId()) : null);
								if(vaultBean != null && vaultBean.getActiveKeyId() == null) vaultService.newActiveKey(vaultBean);

								if(vaultBean != null) imageBytes = vaultService.extractVaultData(vaultBean, chkData);
								else imageBytes = DataUtil.getValue(chkData);
							}
							byte[] thumbBytes = GraphicsUtil.createThumbnail(imageBytes, options.getThumbWidth(), options.getThumbHeight());
							if(thumbBytes.length == 0 && imageBytes.length > 0){
								logger.info("Thumbnail size exceeds source image dimensions.  Returning source bytearray.");
								thumbBytes = imageBytes;
							}
							/// 2014/03/13 - Thumbnail data is owned by the original image owner, not by the context user
							///
							UserType dataOwner = Factories.getNameIdFactory(FactoryEnumType.USER).getById(chkData.getOwnerId(), group.getOrganizationId());
							if(dataOwner == null){
								AuditService.denyResult(audit, "Deny '" + objName + "' owner #" + chkData.getOwnerId() + " was not found in Org #"  + group.getOrganizationId());;
								response.sendError(404);
								return;
							}
							DataType thumbData = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(dataOwner, thumbGroup.getId());
							thumbData.setMimeType("image/png");
							thumbData.setName(thumbName);
							if(vaultBean != null) vaultService.setVaultBytes(vaultBean, thumbData, thumbBytes);
							else DataUtil.setValue(thumbData, thumbBytes);
							if(((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).add(thumbData) == false){
								AuditService.denyResult(audit, "Data " + thumbName + " was not added to group " + thumbGroup.getName());
								response.sendError(404);
								return;
							}
							data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(thumbName, thumbGroup);
						}
						else{
							AuditService.denyResult(audit, "User " + user.getName() + " is not authorized to view  " + objName);
							response.sendError(404);
							return;
						}
					
				}
				if(data == null){
					logger.warn("Thumbnail data is null for data name " + objName + " and user " + user.getName());
				}
			} /// End if thumbnail
			else{
				data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(objName, group);
				if(data != null && data.getMimeType() != null && data.getMimeType().startsWith("image/") && restrictSize){
					logger.info("Redirecting to restricted image path");
					((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(group);
					((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(group);
					String dotPath = UrnUtil.getDotOrganizationPath(org.getId());
					AuditService.pendResult(audit, "Redirecting user " + user.getName() + " to " + request.getServletContext().getContextPath() + "/thumbnail/" + dotPath + "/Data" + group.getPath() + "/" + objName + "/" + maxWidth + "x" + maxHeight + " with restricted dimensions");
					response.sendRedirect(request.getServletContext().getContextPath() + "/thumbnail/" + dotPath + "/Data" + group.getPath() + "/" + objName + "/" + maxWidth + "x" + maxHeight);
					return;
				}
			}
			if(data != null && AuthorizationService.canView(user, data)){
				can_view = true;
			}
		}
		catch(FactoryException| ArgumentException | DataException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		if(data == null){
			AuditService.denyResult(audit, "Data is invalid: '" + objName + "'");
			response.sendError(404);
			return;
		}
		if(can_view == false){
			AuditService.denyResult(audit, "User '" + user.getName() + "' is not authorized to view data '" + data.getName() + "' in organization '" + org.getName() + "' because the view bit is set to false.");
			response.sendError(404);
			return;	
		}
		AuditService.permitResult(audit, "User " + user.getName() + " is authorized to view  " + data.getName() + " in " + data.getGroupId());
		response.setContentType(data.getMimeType());

		byte[] value = new byte[0];
		try {
			if(data.getPointer() && isAllowDataPointers(request) == false){
				logger.error("Access to data pointer data is forbidden.");
			}
			else{
				VaultBean vaultBean = (data.getVaulted() ? vaultService.getVaultByUrn(user, data.getVaultId()) : null);
				if(data.getVaulted()){
				if(vaultBean != null && vaultBean.getActiveKeyId() == null) vaultService.newActiveKey(vaultBean);
					value = vaultService.extractVaultData(vaultBean, data);
				
				}
				else{
					value = DataUtil.getValue(data);
				}
			}
		} catch (DataException | FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		if(options.isEncodeData()){
			value = BinaryUtil.toBase64(value);
		}
		if(options.isUseTemplate() && options.getTemplatePath() != null){
			
			InputStream resourceContent = null;
			String template = null;
			if(templateContents.containsKey(options.getTemplatePath())) template = templateContents.get(options.getTemplatePath());
			else{
				try {

					resourceContent = request.getServletContext().getResourceAsStream(options.getTemplatePath());
					template = StreamUtil.streamToString(new BufferedInputStream(resourceContent));
					if(template != null && template.length() > 0){
						templateContents.put(options.getTemplatePath(), template);
					}
				} catch (IOException e) {
					
					logger.error(FactoryException.LOGICAL_EXCEPTION,e);
				}
				finally{
					if(resourceContent != null)
						try {
							resourceContent.close();
						} catch (IOException e) {
							
							logger.error(FactoryException.LOGICAL_EXCEPTION,e);
						}
				}

			}
			if(template != null){
				template = template.replaceAll("%TITLE%", data.getName() + " (" + data.getObjectId() + ") - Distributed Web Application Component");
				template = template.replaceAll("%CONTENT%", request.getRequestURI().replaceAll("/dwac/", "/media/"));
				value = template.getBytes();
				if(options.getTemplateContentType() != null) response.setContentType(options.getTemplateContentType());
			}
			else{
				response.sendError(500);
				AuditService.denyResult(audit, "Template is invalid: '" + options.getTemplatePath() + "'");
			}
		}
		response.setContentLength(value.length);
		response.getOutputStream().write(value); 
		response.flushBuffer();
	}


	
}
