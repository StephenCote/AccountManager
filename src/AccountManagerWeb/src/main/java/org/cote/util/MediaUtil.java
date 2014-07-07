package org.cote.util;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.GraphicsUtil;
import org.cote.accountmanager.util.ServiceUtil;
import org.cote.beans.MediaOptions;

public class MediaUtil {
	public static final Logger logger = Logger.getLogger(MediaUtil.class.getName());
	private static Pattern recPattern = Pattern.compile("^\\/([A-Za-z0-9\\.]+)\\/([\\w]+)([%-_\\/\\s\\.A-Za-z0-9]+)$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private static Pattern dimPattern = Pattern.compile("(\\/\\d+x\\d+)$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	
	private static int maximumImageWidth = -1;
	private static int maximumImageHeight = -1;
	private static boolean restrictImageSize = false;
	private static boolean checkConfig = false;
	protected static boolean getRestrictImageSize(HttpServletRequest request){
		if(checkConfig) return restrictImageSize;
		restrictImageSize = getBoolParam(request,"image.restrict.size");
		checkConfig = true;
		return restrictImageSize;
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
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "MediaRead", AuditEnumType.SESSION, request.getSession(true).getId());
		String path = request.getPathInfo();
		AuditService.targetAudit(audit, AuditEnumType.UNKNOWN, path);
		if(path == null || path.length() == 0){
			AuditService.denyResult(audit, "Path is null or empty");
			response.sendError(404);
			return;
		}
		
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
			org = Factories.getOrganizationFactory().findOrganization(orgPath);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(org == null){
			AuditService.denyResult(audit, "Organization is invalid: '" + orgPath + "'");
			response.sendError(404);
			return;
		}
		
		UserType user = ServiceUtil.getUserFromSession(request);
		
		if(user == null) user = Factories.getDocumentControl(org);
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
			dir = (DirectoryGroupType)Factories.getGroupFactory().findGroup(user, GroupEnumType.DATA, objPath, org);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(dir == null){
			AuditService.denyResult(audit, "Path is invalid: '" + objPath + "'");
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
				synchronized(Factories.getGroupFactory()){
					thumbGroup = Factories.getGroupFactory().getDirectoryByName(".thumbnail", group, org);
					if(thumbGroup == null && AuthorizationService.canChangeGroup(user, group)){
						thumbGroup = Factories.getGroupFactory().getCreateDirectory(user, ".thumbnail", group, org);
					}
				}
				//DataType thumbData = 
				if(thumbGroup == null){
					AuditService.denyResult(audit, "The thumbnail group could not be found or created in " + group.getName() + (group.getPath() != null ? " (" + group.getPath() + ")" : ""));
					response.sendError(404);
					return;	
				}

				if(AuthorizationService.canViewGroup(user, thumbGroup) == false){
					AuditService.denyResult(audit, "User " + user.getName() + " is not authorized to view the thumbnail group in " + group.getName());
					response.sendError(404);
					return;	
				}

				/// Get the thumbnail data
				data = Factories.getDataFactory().getDataByName(thumbName, thumbGroup);
				/// If it doesn't exist, try to create it
				if(data == null){
						DataType chkData = Factories.getDataFactory().getDataByName(objName, true, group);
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
						if(AuthorizationService.canViewData(user, chkData)){
							chkData = Factories.getDataFactory().getDataByName(objName, false, group);
							if(chkData == null || chkData.getDataBytesStore() == null || chkData.getDataBytesStore().length == 0){
								AuditService.denyResult(audit, "Data '" + objName + " was not retrieved in a populated state");
								response.sendError(404);
								return;
							}
							byte[] imageBytes = DataUtil.getValue(chkData);
							byte[] thumbBytes = GraphicsUtil.createThumbnail(imageBytes, options.getThumbWidth(), options.getThumbHeight());
							if(thumbBytes.length == 0 && imageBytes.length > 0){
								logger.info("Thumbnail size exceeds source image dimensions.  Returning source bytearray.");
								thumbBytes = imageBytes;
							}
							/// 2014/03/13 - Thumbnail data is owned by the original image owner, not by the context user
							///
							UserType dataOwner = Factories.getUserFactory().getById(chkData.getOwnerId(), group.getOrganization());
							if(dataOwner == null){
								AuditService.denyResult(audit, "Deny '" + objName + "' owner #" + chkData.getOwnerId() + " was not found in Org #"  + group.getOrganization().getId());;
								response.sendError(404);
								return;
							}
							DataType thumbData = Factories.getDataFactory().newData(dataOwner, thumbGroup);
							thumbData.setMimeType("image/jpg");
							thumbData.setName(thumbName);
							DataUtil.setValue(thumbData, thumbBytes);
							if(Factories.getDataFactory().addData(thumbData) == false){
								AuditService.denyResult(audit, "Data " + thumbName + " was not added to group " + thumbGroup.getName());
								response.sendError(404);
								return;
							}
							data = Factories.getDataFactory().getDataByName(thumbName, thumbGroup);
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
				data = Factories.getDataFactory().getDataByName(objName, group);
				//logger.info("Restrict Size: " + restrictSize + " / " + data.getMimeType());
				if(data.getMimeType() != null && data.getMimeType().startsWith("image/") && restrictSize){
					logger.info("Redirecting to restricted image path");
					Factories.getGroupFactory().populate(group);
					
					AuditService.pendResult(audit, "Redirecting user " + user.getName() + " to " + request.getServletContext().getContextPath() + "/Thumbnail" + Factories.getOrganizationFactory().getOrganizationPath(org) + "/Data" + group.getPath() + "/" + objName + "/" + maxWidth + "x" + maxHeight + " with restricted dimensions");
					response.sendRedirect(request.getServletContext().getContextPath() + "/Thumbnail" + Factories.getOrganizationFactory().getOrganizationPath(org) + "/Data" + group.getPath() + "/" + objName + "/" + maxWidth + "x" + maxHeight);
					return;
				}
			}
			if(data != null && AuthorizationService.canViewData(user, data) == true){
				can_view = true;
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
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
		AuditService.permitResult(audit, "User " + user.getName() + " is authorized to view  " + data.getName() + " in " + data.getGroup());
		response.setContentType(data.getMimeType());
		byte[] value = new byte[0];
		try {
			value = DataUtil.getValue(data);
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		response.setContentLength(value.length);
		response.getOutputStream().write(value); 
		response.flushBuffer();
	}

	
}
