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
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.GraphicsUtil;
import org.cote.accountmanager.util.ServiceUtil;
import org.cote.beans.MediaOptions;

public class MediaUtil {
	public static final Logger logger = Logger.getLogger(MediaUtil.class.getName());
	private static Pattern recPattern = Pattern.compile("^\\/([A-Za-z0-9\\.]+)\\/([\\w]+)([%-_\\/\\s\\.A-Za-z0-9]+)$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	private static Pattern dimPattern = Pattern.compile("(\\/\\d+x\\d+)$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
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
				logger.error("No alternate dimensions specified: " + d.groupCount());
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
			dir = Factories.getGroupFactory().findGroup(user, objPath, org);
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
	
		/// If this is a thumbnail request, then:
		/// 1) get the details only data and confirm it's an image
		/// 

		
		try{
			if(options.isThumbnail()){
				String thumbName = objName + " " + options.getThumbWidth() + "x" + options.getThumbHeight();
				DirectoryGroupType thumbGroup = Factories.getGroupFactory().getDirectoryByName(".thumbnail", group, org);
				if(thumbGroup == null && AuthorizationService.canChangeGroup(user, group)){
					thumbGroup = Factories.getGroupFactory().getCreateDirectory(user, ".thumbnail", group, org);
				}
				//DataType thumbData = 
				if(thumbGroup == null){
					AuditService.denyResult(audit, "The thumbnail group could not be found or created in " + group.getName());
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
							DataType thumbData = Factories.getDataFactory().newData(user, thumbGroup);
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
			} /// End if thumbnail
			else{
				data = Factories.getDataFactory().getDataByName(objName, group);
			}
			if(data != null && AuthorizationService.canViewData(user, data) == true){
				can_view = true;
			}
		}
		catch(FactoryException fe){
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(data == null){
			AuditService.denyResult(audit, "Data is invalid: '" + objName + "'");
			response.sendError(404);
			return;
		}
		if(can_view == false){
			AuditService.denyResult(audit, "User '" + user.getName() + "' is not authorized to view data '" + data.getName() + "' in organization '" + org.getName() + "'");
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