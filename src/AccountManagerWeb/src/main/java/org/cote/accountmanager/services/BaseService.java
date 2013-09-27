package org.cote.accountmanager.services;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
//import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.FactoryService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserRoleType;
//import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;


import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.cote.accountmanager.util.ServiceUtil;
import org.cote.util.BeanUtil;

public class BaseService{
	public static final Logger logger = Logger.getLogger(BaseService.class.getName());
	
	public static String getDefaultGroupName(AuditEnumType type){
		String out_path = "~";
		switch(type){
			case DATA:
				out_path = "Data";
				break;
			case GROUP:
				out_path = "";
				break;
		}
		return out_path;
	}
	public static String getDefaultPath(AuditEnumType type){
		return "~/" + getDefaultGroupName(type);

	}
	
	private static void cloneNameIdDirectoryType(NameIdType src, NameIdType targ){
		targ.setName(src.getName());
		targ.setParentId(src.getParentId());
		if(src.getNameType() == null) targ.setNameType(NameEnumType.APPLICATION);
		else targ.setNameType(src.getNameType());
	}
	/// don't blindly accept values 
	///
	private static <T> boolean sanitizeAddNewObject(AuditEnumType type, UserType user, T in_obj) throws ArgumentException, FactoryException, DataException, DataAccessException{
		boolean out_bool = false;
		switch(type){
			case ROLE:
				BaseRoleType rlbean = (BaseRoleType)in_obj;
				BaseRoleType parentRole = null;
				if(rlbean.getParentId() > 0){
					parentRole = Factories.getRoleFactory().getById(rlbean.getParentId(), rlbean.getOrganization());
					if(parentRole == null) throw new ArgumentException("Role parent #" + rlbean.getParentId() + " is invalid");
				}
				BaseRoleType new_role = Factories.getRoleFactory().newUserRole(user, rlbean.getName(), parentRole);
				out_bool = Factories.getRoleFactory().addRole(new_role);
				break;
			case USER:
				UserType ubean = (UserType)in_obj;
				UserType new_user = Factories.getUserFactory().newUser(ubean.getName(), SecurityUtil.getSaltedDigest(ubean.getPassword()), UserEnumType.NORMAL, UserStatusEnumType.NORMAL, ubean.getOrganization());
				new_user.setContactInformation(ubean.getContactInformation());
				out_bool = Factories.getUserFactory().addUser(new_user);
				break;
			case DATA:
				DataType rbean = (DataType)in_obj;
				DataType new_rec = Factories.getDataFactory().newData(user, rbean.getGroup());
				cloneNameIdDirectoryType(rbean, new_rec);
				new_rec.setDescription(rbean.getDescription());
				new_rec.setDimensions(rbean.getDimensions());
				if(rbean.getExpiryDate() != null) new_rec.setExpiryDate(rbean.getExpiryDate());
				new_rec.setMimeType(rbean.getMimeType());
				new_rec.setRating(rbean.getRating());

				DataUtil.setValue(new_rec, DataUtil.getValue(rbean));
				out_bool = Factories.getDataFactory().addData(new_rec);
				break;				
		}
		return out_bool;
	}
	private static <T> boolean updateObject(AuditEnumType type, T in_obj) throws ArgumentException, FactoryException, DataAccessException {
		boolean out_bool = false;
		switch(type){
			case ROLE:
				out_bool = Factories.getRoleFactory().updateRole((BaseRoleType)in_obj);
				break;
			case USER:
				out_bool = Factories.getUserFactory().updateUser((UserType)in_obj);
				break;
			case DATA:
				out_bool = Factories.getDataFactory().updateData((DataType)in_obj);
				break;	
		}
		return out_bool;		
	}
	private static <T> boolean deleteObject(AuditEnumType type, T in_obj) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		switch(type){
			case ROLE:
				out_bool = Factories.getRoleFactory().deleteRole((BaseRoleType)in_obj);
				break;
			case USER:
				out_bool = Factories.getUserFactory().deleteUser((UserType)in_obj);
				break;
			case DATA:
				out_bool = Factories.getDataFactory().deleteData((DataType)in_obj);
				break;

		}
		return out_bool;
	}
	private static <T> T getFactory(AuditEnumType type){
		T out_obj = null;
		switch(type){
			case ROLE:
				out_obj = (T)Factories.getRoleFactory();
				break;
			case USER:
				out_obj = (T)Factories.getUserFactory();
				break;
			case DATA:
				out_obj = (T)Factories.getDataFactory();
				break;
			
		}
		return out_obj;
	}
	private static <T> T getById(AuditEnumType type, long id, OrganizationType org) throws ArgumentException, FactoryException {
		NameIdFactory factory = getFactory(type);
		T out_obj = factory.getById(id, org);
		populate(type, out_obj);
		delink(type, out_obj);
		switch(type){
		case DATA:
			DataType d = (DataType)out_obj;
			if(d.getCompressed()){
				d = BeanUtil.getBean(DataType.class, d);
				try {
					byte[] data = DataUtil.getValue(d);
					d.setCompressed(false);
					d.setDataBytesStore(data);
					d.setReadDataBytes(false);
					out_obj = (T)d;
				} catch (DataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			break;
		}
		return out_obj;		
	}
	private static <T> T getByNameInParent(AuditEnumType type, String name, NameIdType parent) throws ArgumentException, FactoryException {
		
		T out_obj = null;
		switch(type){
			case ROLE:
				out_obj = (T)Factories.getRoleFactory().getUserRoleByName(name, (BaseRoleType)parent, parent.getOrganization());
				break;
		}		
		populate(type, out_obj);
		delink(type, out_obj);
		return out_obj;		
	}
	private static <T> T getByName(AuditEnumType type, String name, DirectoryGroupType group) throws ArgumentException, FactoryException {
		
		T out_obj = null;
		switch(type){
			case DATA:
				out_obj = (T)Factories.getDataFactory().getDataByName(name, group);
				DataType d = (DataType)out_obj;
				if(d.getCompressed()){
					d = BeanUtil.getBean(DataType.class, d);
					try {
						byte[] data = DataUtil.getValue(d);
						d.setCompressed(false);
						d.setDataBytesStore(data);
						d.setReadDataBytes(false);
						out_obj = (T)d;
					} catch (DataException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				break;
		}		
		populate(type, out_obj);
		delink(type, out_obj);
		return out_obj;		
	}
	private static <T> T getByName(AuditEnumType type, String name, OrganizationType org) throws ArgumentException, FactoryException {
		
		T out_obj = null;
		switch(type){
			case ROLE:
				out_obj = (T)Factories.getRoleFactory().getRoleByName(name, org);
				break;
			case USER:
				out_obj = (T)Factories.getUserFactory().getUserByName(name, org);
				break;
		}		
		populate(type, out_obj);
		delink(type, out_obj);
		return out_obj;		
	}
	private static <T> void delink(AuditEnumType type, T obj){
		DirectoryGroupType dir = null;
		switch(type){
			case DATA:
				dir = ((DataType)obj).getGroup();
				break;
		}
		if(dir != null){
			dir.setParentGroup(null);
			dir.getSubDirectories().clear();
			dir.setPopulated(false);
		}
	}
	private static <T> void populate(AuditEnumType type,T object){
		try{
		switch(type){

			case GROUP:
				Factories.getGroupFactory().populate((BaseGroupType)object);
				break;
			case USER:
				Factories.getUserFactory().populate((UserType)object);
				break;
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	
	}
	private static boolean canViewType(AuditEnumType type, UserType user, NameIdType obj) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		switch(type){
			case ROLE:
				out_bool = AuthorizationService.canViewRole(user, (BaseRoleType)obj);
				break;
			case DATA:
				out_bool = AuthorizationService.canViewData(user, (DataType)obj);
				break;
			case USER:
				// allow for user requesting self
				// this does not register true for 'isMapOwner' for the user object as a user does not own itslef
				//
				out_bool = (user.getId().compareTo(obj.getId())==0 && user.getOrganization().getId().compareTo(obj.getOrganization().getId())==0);
				if(!out_bool)  out_bool = AuthorizationService.isMapOwner(user, obj);
				if(!out_bool) out_bool = AuthorizationService.isAccountAdministratorInMapOrganization(user, (UserType)obj);
				if(!out_bool) out_bool = AuthorizationService.isAccountReaderInMapOrganization(user, (UserType)obj);
				break;
			case GROUP:
				out_bool = AuthorizationService.canCreateGroup(user, (BaseGroupType)obj);
				break;
		}
		return out_bool;
	}
	private static boolean canCreateType(AuditEnumType type, UserType user, NameIdType obj) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		switch(type){
			case ROLE:

				if(obj.getParentId() > 0){
					BaseRoleType parent = Factories.getRoleFactory().getById(obj.getParentId(),obj.getOrganization());
					out_bool = AuthorizationService.canChangeRole(user, parent);
				}
				if(!out_bool){
					out_bool = AuthorizationService.isAccountAdministratorInMapOrganization(user, obj);
				}
				break;
			case DATA:
				out_bool = AuthorizationService.canChangeGroup(user, ((DataType)obj).getGroup());
				break;
			case USER:
				out_bool = AuthorizationService.isAccountAdministratorInMapOrganization(user, obj);
				break;
			case GROUP:
				out_bool = AuthorizationService.canCreateGroup(user, (BaseGroupType)obj);
				break;
		}
		return out_bool;
	}
	private static boolean canChangeType(AuditEnumType type, UserType user, NameIdType obj) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		switch(type){
			case ROLE:
				out_bool = AuthorizationService.canChangeRole(user, (BaseRoleType)obj);
	
				break;
			case DATA:
				out_bool = AuthorizationService.canChangeData(user, (DataType)obj);
				/// If 
				if(!out_bool) out_bool = AuthorizationService.canChangeGroup(user, ((DataType)obj).getGroup());
				break;
			case USER:
				// allow for user requesting self
				// this does not register true for 'isMapOwner' for the user object as a user does not own itslef
				//
				out_bool = (user.getId().compareTo(obj.getId())==0 && user.getOrganization().getId().compareTo(obj.getOrganization().getId())==0);
				if(!out_bool)  out_bool = AuthorizationService.isMapOwner(user, obj);
				if(!out_bool) out_bool = AuthorizationService.isAccountAdministratorInMapOrganization(user, (UserType)obj);
				break;
			case GROUP:
				out_bool = AuthorizationService.canChangeGroup(user, (BaseGroupType)obj);
				break;
		}
		return out_bool;
	}
	private static boolean canDeleteType(AuditEnumType type, UserType user, NameIdType obj) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		switch(type){
			case ROLE:
				out_bool = AuthorizationService.canDeleteRole(user, (BaseRoleType)obj);
				break;
			case DATA:
				out_bool = AuthorizationService.canDeleteData(user, (DataType)obj);
				//if(out_bool) out_bool = AuthorizationService.canChangeGroup(user, ((DataType)obj).getGroup());
				break;
			case USER:
				// allow for user deleting self
				// this does not register true for 'isMapOwner' for the user object as a user does not own itslef
				//
				out_bool = (user.getId().compareTo(obj.getId())==0 && user.getOrganization().getId().compareTo(obj.getOrganization().getId())==0);
				if(out_bool) throw new FactoryException("Self deletion not supported via Web interface");
				if(!out_bool)  out_bool = AuthorizationService.isMapOwner(user, obj);
				if(!out_bool) out_bool = AuthorizationService.isAccountAdministratorInMapOrganization(user, (UserType)obj);
				break;				
		}
		return out_bool;
	}
	
	/// Duped in AuthorizationService, except the type is taken from the object instead of from the AuditEnumType
	private static <T> boolean authorizeRoleType(AuditEnumType type, UserType adminUser, BaseRoleType targetRole, T bucket, boolean view, boolean edit, boolean delete, boolean create) throws FactoryException, DataAccessException, ArgumentException{
		boolean out_bool = false;
		switch(type){
			case DATA:
				DataType data = (DataType)bucket;
				AuthorizationService.switchData(adminUser, targetRole, data, AuthorizationService.getViewDataPermission(data.getGroup().getOrganization()), view);
				AuthorizationService.switchData(adminUser, targetRole, data, AuthorizationService.getEditDataPermission(data.getGroup().getOrganization()), edit);
				AuthorizationService.switchData(adminUser, targetRole, data, AuthorizationService.getDeleteDataPermission(data.getGroup().getOrganization()), delete);
				AuthorizationService.switchData(adminUser, targetRole, data, AuthorizationService.getCreateDataPermission(data.getGroup().getOrganization()), create);
				out_bool = true;
				break;
			case GROUP:
				BaseGroupType group = (BaseGroupType)bucket;
				AuthorizationService.switchGroup(adminUser, targetRole, group, AuthorizationService.getViewGroupPermission(group.getOrganization()), view);
				AuthorizationService.switchGroup(adminUser, targetRole, group, AuthorizationService.getEditGroupPermission(group.getOrganization()), edit);
				AuthorizationService.switchGroup(adminUser, targetRole, group, AuthorizationService.getDeleteGroupPermission(group.getOrganization()), delete);
				AuthorizationService.switchGroup(adminUser, targetRole, group, AuthorizationService.getCreateGroupPermission(group.getOrganization()), create);
				out_bool = true;
				break;
		}
		
		return out_bool;
	}
	/// Duped in AuthorizationService, except the type is taken from the object instead of from the AuditEnumType
	private static <T> boolean authorizeUserType(AuditEnumType type, UserType adminUser, UserType targetUser, T bucket, boolean view, boolean edit, boolean delete, boolean create) throws FactoryException, DataAccessException, ArgumentException{
		boolean out_bool = false;
		switch(type){
			case DATA:
				DataType Data = (DataType)bucket;
				AuthorizationService.switchData(adminUser, targetUser, Data, AuthorizationService.getViewDataPermission(Data.getOrganization()), view);
				AuthorizationService.switchData(adminUser, targetUser, Data, AuthorizationService.getEditDataPermission(Data.getOrganization()), edit);
				AuthorizationService.switchData(adminUser, targetUser, Data, AuthorizationService.getDeleteDataPermission(Data.getOrganization()), delete);
				AuthorizationService.switchData(adminUser, targetUser, Data, AuthorizationService.getCreateDataPermission(Data.getOrganization()), create);
				out_bool = true;
				break;
			case ROLE:
				BaseRoleType role = (BaseRoleType)bucket;
				AuthorizationService.switchRole(adminUser, targetUser, role, AuthorizationService.getViewRolePermission(role.getOrganization()), view);
				AuthorizationService.switchRole(adminUser, targetUser, role, AuthorizationService.getEditRolePermission(role.getOrganization()), edit);
				AuthorizationService.switchRole(adminUser, targetUser, role, AuthorizationService.getDeleteRolePermission(role.getOrganization()), delete);
				AuthorizationService.switchRole(adminUser, targetUser, role, AuthorizationService.getCreateRolePermission(role.getOrganization()), create);
				out_bool = true;
				break;
			case GROUP:
				BaseGroupType group = (BaseGroupType)bucket;
				AuthorizationService.switchGroup(adminUser, targetUser, group, AuthorizationService.getViewGroupPermission(group.getOrganization()), view);
				AuthorizationService.switchGroup(adminUser, targetUser, group, AuthorizationService.getEditGroupPermission(group.getOrganization()), edit);
				AuthorizationService.switchGroup(adminUser, targetUser, group, AuthorizationService.getDeleteGroupPermission(group.getOrganization()), delete);
				AuthorizationService.switchGroup(adminUser, targetUser, group, AuthorizationService.getCreateGroupPermission(group.getOrganization()), create);
				out_bool = true;
				break;
		}
		
		return out_bool;
	}
	public static <T> boolean authorizeRole(AuditEnumType type, OrganizationType org, long targetRoleId, T bucket, boolean view, boolean edit, boolean delete, boolean create, HttpServletRequest request){
		boolean out_bool = false;

		BaseRoleType targetRole = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHORIZE, "authorizeRole", AuditEnumType.SESSION, request.getSession(true).getId());
		NameIdType typeBean = (NameIdType)bucket;
		AuditService.targetAudit(audit, type, (typeBean == null ? "null" : typeBean.getName()));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return false;

		try {
			if(canChangeType(type, user, typeBean)){
				targetRole = Factories.getRoleFactory().getById(targetRoleId, org);
				if(targetRole != null){
					if(authorizeRoleType(type, user, targetRole, bucket, view, edit, delete, create)){
						AuditService.permitResult(audit, "Applied authorization policy updates for role #" + targetRoleId);
						out_bool = true;
					}
				}
				else{
					AuditService.denyResult(audit, "Target user #" + targetRoleId + " in organization #" + org.getId() + " does not exist");
				}
			}
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AuditService.denyResult(audit, e.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AuditService.denyResult(audit, e.getMessage());
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AuditService.denyResult(audit, e.getMessage());
		}
		return out_bool;
	}
	public static <T> boolean authorizeUser(AuditEnumType type, OrganizationType org, long targetUserId, T bucket, boolean view, boolean edit, boolean delete, boolean create, HttpServletRequest request){
		boolean out_bool = false;

		UserType targetUser = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHORIZE, "authorizeUser", AuditEnumType.SESSION, request.getSession(true).getId());
		NameIdType typeBean = (NameIdType)bucket;
		AuditService.targetAudit(audit, type, (typeBean == null ? "null" : typeBean.getName()));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return false;

		try {
			if(canChangeType(type, user, typeBean)){
				targetUser = Factories.getUserFactory().getById(targetUserId, org);
				if(targetUser != null){
					if(authorizeUserType(type, user, targetUser, bucket, view, edit, delete, create)){
						AuditService.permitResult(audit, "Applied authorization policy updates");
						out_bool = true;
					}
				}
				else{
					AuditService.denyResult(audit, "Target user #" + targetUserId + " in organization #" + org.getId() + " does not exist");
				}
			}
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AuditService.denyResult(audit, e.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AuditService.denyResult(audit, e.getMessage());
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AuditService.denyResult(audit, e.getMessage());
		}
		return out_bool;
	}
	
	public static <T> boolean delete(AuditEnumType type, T bean, HttpServletRequest request){
		
		boolean out_bool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.DELETE, "delete", AuditEnumType.SESSION, request.getSession(true).getId());
		NameIdType typeBean = (NameIdType)bean;
		AuditService.targetAudit(audit, type, (typeBean == null ? "null" : typeBean.getName()));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return false;

		try {
			if(typeBean.getId() <= 0){
				AuditService.denyResult(audit,"Bean contains invalid data");
				return out_bool;
			}
			if(canDeleteType(type, user, typeBean)){
				out_bool = deleteObject(type, bean);
				if(out_bool) AuditService.permitResult(audit, "Deleted " + typeBean.getName());
				else AuditService.denyResult(audit, "Unable to delete " + typeBean.getName());
				
			}
			else{
				AuditService.denyResult(audit, "User is not authorized");
				System.out.println("User is not authorized to delete the object object '" + typeBean.getName() + "' #" + typeBean.getId());
			}
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			AuditService.denyResult(audit, e1.getMessage());
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			AuditService.denyResult(audit, e1.getMessage());
		}

		return out_bool;
	}
	public static <T> boolean add(AuditEnumType addType, T bean, HttpServletRequest request){
		
		boolean out_bool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "add", AuditEnumType.SESSION, request.getSession(true).getId());
		NameIdType dirBean = (NameIdType)bean;
		AuditService.targetAudit(audit, addType, (dirBean == null ? "null" : dirBean.getName()));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return false;

		try {

			if(canCreateType(addType, user, dirBean) == true){

				out_bool = sanitizeAddNewObject(addType, user, bean);
				if(out_bool) AuditService.permitResult(audit, "Added " + dirBean.getName());
				else AuditService.denyResult(audit, "Unable to add " + dirBean.getName());
				
			}
			else{
				AuditService.denyResult(audit, "User is not authorized");
				System.out.println("User is not authorized to add the object  '" + dirBean.getName());
			}
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			AuditService.denyResult(audit, e1.getMessage());
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			AuditService.denyResult(audit, e1.getMessage());
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AuditService.denyResult(audit, e.getMessage());
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AuditService.denyResult(audit, e.getMessage());
		}

		return out_bool;
	}
	
	public static <T> boolean update(AuditEnumType type, T bean,HttpServletRequest request){
		boolean out_bool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "update",AuditEnumType.SESSION, request.getSession(true).getId());
		NameIdType dirBean = (NameIdType)bean;
		AuditService.targetAudit(audit, type, (dirBean == null ? "null" : dirBean.getName()));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return false;
		if(dirBean == null){
			AuditService.denyResult(audit, "Null value");
			return false;
		}

		try {
			if(canChangeType(type, user, dirBean) == true){
				out_bool = updateObject(type, bean); 	
				if(out_bool) AuditService.permitResult(audit, "Updated " + dirBean.getName() + " (#" + dirBean.getId() + ")");
				else AuditService.denyResult(audit, "Unable to update " + dirBean.getName() + " (#" + dirBean.getId() + ")");
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to change object '" + dirBean.getName() + "' #" + dirBean.getId());
			}
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			AuditService.denyResult(audit, e1.getMessage());
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			AuditService.denyResult(audit, e1.getMessage());
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AuditService.denyResult(audit, e.getMessage());
		}

		return out_bool;
	}
	public static <T> T readById(AuditEnumType type, long id,HttpServletRequest request){
		T out_obj = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readById",AuditEnumType.SESSION, request.getSession(true).getId());
		AuditService.targetAudit(audit, type, Long.toString(id));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return out_obj;
		
		try {
			
			NameIdType dirType = getById(type,id, user.getOrganization());
			if(dirType == null){
				AuditService.denyResult(audit, "#" + id + " doesn't exist in organization " + user.getOrganization().getName());
				return null;
			}			
			if(canViewType(type, user, dirType) == true){
				out_obj = (T)dirType;
				AuditService.permitResult(audit, "Read " + dirType.getName() + " (#" + dirType.getId() + ")");
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view object '" + dirType.getName() + "' #" + dirType.getId());
			}

		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return out_obj;
	}	
	public static <T> T readByName(AuditEnumType type, String name,HttpServletRequest request){
		DirectoryGroupType dir = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.SESSION, request.getSession(true).getId());
		AuditService.targetAudit(audit, type, name);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return null;

		try{
			dir = Factories.getGroupFactory().getCreateUserDirectory(user, getDefaultGroupName(type));
		}
		 catch (FactoryException e1) {
			 logger.error(e1.getMessage());
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} 
		return readByName(audit,type, user, dir, name, request);
	}
	public static <T> T readByName(AuditEnumType type, long groupId, String name,HttpServletRequest request){
		DirectoryGroupType dir = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.SESSION, request.getSession(true).getId());
		AuditService.targetAudit(audit, type, name);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return null;

		try{
			dir = Factories.getGroupFactory().getById(groupId, user.getOrganization());
		}
		 catch (FactoryException e1) {
			// TODO Auto-generated catch block
			 logger.error(e1.getMessage());
			e1.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} 
		return readByName(audit,type, user, dir, name, request);
	}
	public static <T> T readByName(AuditEnumType type, DirectoryGroupType dir, String name,HttpServletRequest request){
		T out_obj = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.SESSION, request.getSession(true).getId());
		AuditService.targetAudit(audit, type, name);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return out_obj;
		return readByName(audit,type, user, dir, name, request);
	}
	public static <T> T readByName(AuditType audit,AuditEnumType type, UserType user, DirectoryGroupType dir, String name,HttpServletRequest request){
		T out_obj = null;
		try {
			//DirectoryGroupType group = Factories.getGroupFactory().getCreateUserDirectory(user, getDefaultGroupName(type));
			Factories.getGroupFactory().populate(dir);
			out_obj = getByName(type, name, dir);
			if(out_obj == null){
				AuditService.denyResult(audit, "'" + name + "' doesn't exist");
				return null;
			}
			if(canViewType(type, user, (NameIdType)out_obj)){
				AuditService.permitResult(audit, "Read " + name + " (#" + ((NameIdType)out_obj).getId() + ")");

			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view object '" + dir.getName() + "' #" + dir.getId());
			}
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return out_obj;
	}
	
	public static <T> T readByNameInParent(AuditEnumType type, NameIdType parent, String name,HttpServletRequest request){
		T out_obj = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByNameInParent",AuditEnumType.SESSION, request.getSession(true).getId());
		AuditService.targetAudit(audit, type, name);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return out_obj;
		return readByNameInParent(audit,type, user, parent, name, request);
	}
	public static <T> T readByNameInParent(AuditType audit,AuditEnumType type, UserType user, NameIdType parent, String name,HttpServletRequest request){
		T out_obj = null;
		try {

			out_obj = getByNameInParent(type, name, parent);
			if(out_obj == null){
				AuditService.denyResult(audit, "'" + name + "' doesn't exist");
				return null;
			}
			if(canViewType(type, user, (NameIdType)out_obj)){
				AuditService.permitResult(audit, "Read " + name + " (#" + ((NameIdType)out_obj).getId() + ")");

			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view object '" + parent.getName() + "' #" + parent.getId());
			}
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return out_obj;
	}
	
	public static <T> T readByNameInOrganization(AuditEnumType type, long orgId, String name,HttpServletRequest request){
		OrganizationType org = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.SESSION, request.getSession(true).getId());
		AuditService.targetAudit(audit, type, name);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return null;

		try{
			org = Factories.getOrganizationFactory().getOrganizationById(orgId);
		}
		 catch (FactoryException e1) {
			// TODO Auto-generated catch block
			 logger.error(e1.getMessage());
			e1.printStackTrace();
		} catch (ArgumentException e) {
			logger.error(e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return readByName(audit,type, user, org, name, request);
	}
	public static <T> T readByName(AuditEnumType type, OrganizationType org, String name,HttpServletRequest request){
		T out_obj = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.SESSION, request.getSession(true).getId());
		AuditService.targetAudit(audit, type, name);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return out_obj;
		return readByName(audit,type, user, org, name, request);
	}
	public static <T> T readByName(AuditType audit,AuditEnumType type, UserType user, OrganizationType org, String name,HttpServletRequest request){
		T out_obj = null;
		try {
			//DirectoryGroupType group = Factories.getGroupFactory().getCreateUserDirectory(user, getDefaultGroupName(type));

			out_obj = getByName(type, name, org);
			if(out_obj == null){
				AuditService.denyResult(audit, "'" + name + "' doesn't exist");
				return null;
			}
			if(canViewType(type, user, (NameIdType)out_obj)){
				AuditService.permitResult(audit, "Read " + name + " (#" + ((NameIdType)out_obj).getId() + ")");
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view object '" + ((NameIdType)out_obj).getName() + "' #" + ((NameIdType)out_obj).getId());
				out_obj = null;
			}
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			out_obj = null;
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			out_obj = null;
		} 

		return out_obj;
	}
	
	public static int countByGroup(AuditEnumType type, String path, HttpServletRequest request){
		DirectoryGroupType dir = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "count",AuditEnumType.SESSION, request.getSession(true).getId());
		AuditService.targetAudit(audit, type, path);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return 0;

		try{
			dir = Factories.getGroupFactory().findGroup(user, path, user.getOrganization());
			//dir = Factories.getGroupFactory().getById(groupId, user.getOrganization());
		}
		 catch (FactoryException e1) {
			// TODO Auto-generated catch block
			 logger.error(e1.getMessage());
			e1.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} 
		if(dir == null){
			AuditService.denyResult(audit, "Path '" + path + "' does not exist");
			return 0;
		}
		return count(audit,type, user, dir, request);
	}
	public static int count(AuditType audit,AuditEnumType type, UserType user, DirectoryGroupType dir, HttpServletRequest request){
		int out_count = 0;
		try {
			if(canViewType(AuditEnumType.GROUP, user, dir) == true){
				out_count = count(type, dir);
				AuditService.permitResult(audit, "Count " + out_count + " of " + type.toString() + " in '" + dir.getName() + "' #" + dir.getId());
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view objects in the specified group '" + dir.getName() + "' #" + dir.getId());
			}
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return out_count;
	}
	private static int count(AuditEnumType type, DirectoryGroupType group) throws ArgumentException, FactoryException {
		
		NameIdFactory factory = getFactory(type);
		if(type == AuditEnumType.DATA) return ((DataFactory)factory).getCount(group);
		return ((NameIdGroupFactory)factory).getCount(group);		
	}
	
	public static int countByOrganization(AuditEnumType type, long orgId, HttpServletRequest request){
		OrganizationType org = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "count",AuditEnumType.SESSION, request.getSession(true).getId());
		AuditService.targetAudit(audit, type, Long.toString(orgId));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return 0;

		try{
			org = Factories.getOrganizationFactory().getOrganizationById(orgId);
		}
		 catch (FactoryException e1) {
			// TODO Auto-generated catch block
			 logger.error(e1.getMessage());
			e1.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} 
		return countByOrganization(audit,type, user, org, request);
	}
	public static int countInParent(AuditEnumType type, NameIdType parent, HttpServletRequest request){
		OrganizationType org = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "countByParent",AuditEnumType.SESSION, request.getSession(true).getId());
		AuditService.targetAudit(audit, type, parent.getName() + " #" + parent.getId());
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return 0;

		return countInParent(audit,type, user, org, request);
	}
	public static int countByOrganization(AuditType audit,AuditEnumType type, UserType user, OrganizationType org, HttpServletRequest request){
		int out_count = 0;
		try {
			if(AuthorizationService.isDataAdministratorInOrganization(user, org) || AuthorizationService.isAccountAdministratorInOrganization(user, org)){
				out_count = count(type, org.getId());
				AuditService.permitResult(audit, "Count " + out_count + " of " + type.toString());
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to count directly in organization '" + org.getName() + "' #" + org.getId());
			}
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return out_count;
	}
	public static int countInParent(AuditType audit,AuditEnumType type, UserType user, NameIdType parent, HttpServletRequest request){
		int out_count = 0;
		try {
			if(canViewType(type,user, parent) == true){
				out_count = countInParent(type, parent);
				AuditService.permitResult(audit, "Count " + out_count + " of " + type.toString());
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to count in parent '" + parent.getName() + "' #" + parent.getId());
			}
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return out_count;
	}
	public static int count(AuditType audit,AuditEnumType type, UserType user, OrganizationType org, HttpServletRequest request){
		int out_count = 0;
		try {
			if(canViewType(type,user, org) == true){
				out_count = count(type, org.getId());
				AuditService.permitResult(audit, "Count " + out_count + " of " + type.toString());
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view user lists in the specified organization '" + org.getName() + "' #" + org.getId());
			}
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return out_count;
	}
	private static int count(AuditEnumType type, long organization_id) throws ArgumentException, FactoryException {
		NameIdFactory factory = getFactory(type);
		return factory.getCount(organization_id);
	}
	private static int countInParent(AuditEnumType type, NameIdType parent) throws ArgumentException, FactoryException {
		NameIdFactory factory = getFactory(type);
		return factory.getCountInParent(parent);
	}	
}