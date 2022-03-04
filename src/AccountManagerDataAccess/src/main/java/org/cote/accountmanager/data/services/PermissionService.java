package org.cote.accountmanager.data.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.data.factory.IParticipationFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;

public class PermissionService {

	public static final Logger logger = LogManager.getLogger(PermissionService.class);
	
	protected static final Map<Long,List<BasePermissionType>> SYSTEM_PERMISSION_OBJECTS = new HashMap<>();
	
	public static List<BasePermissionType> getSystemPermissions(long organizationId){
		List<BasePermissionType> perms = new ArrayList<>();
		
		if(!SYSTEM_PERMISSION_OBJECTS.containsKey(organizationId)) {
			Map<FactoryEnumType, FactoryEnumType> factories = AuthorizationService.getAuthorizationFactories();
			if(factories.keySet().isEmpty()){
				logger.error("No factories registered with authorization service");
			}
			try {
				for(FactoryEnumType factType : factories.keySet()){
					IParticipationFactory fact = Factories.getFactory(factories.get(factType));
					String[] permissionNames = fact.getDefaultPermissions();
					
					for (String permName : permissionNames)
					{
						BasePermissionType perm = AuthorizationService.getRootPermission(permName, fact.getDefaultPermissionType(), organizationId);
						if(perm == null) {
							logger.error("Failed to find permission " + permName);
						}
						else perms.add(perm);
					}
				}
				
				for(String permName : FactoryDefaults.DEFAULT_ACCOUNT_PERMISSIONS) {
					BasePermissionType perm = AuthorizationService.getRootPermission(permName, PermissionEnumType.ACCOUNT, organizationId);
					if(perm == null) {
						logger.error("Failed to find permission " + permName);
					}
					else perms.add(perm);
				}
				
				for(String permName : FactoryDefaults.DEFAULT_OBJECT_PERMISSIONS) {
					BasePermissionType perm = AuthorizationService.getRootPermission(permName, PermissionEnumType.OBJECT, organizationId);
					if(perm == null) {
						logger.error("Failed to find permission " + permName);
					}
					else perms.add(perm);
				}
				
				for(String permName : FactoryDefaults.DEFAULT_APPLICATION_PERMISSIONS) {
					BasePermissionType perm = AuthorizationService.getRootPermission(permName, PermissionEnumType.APPLICATION, organizationId);
					if(perm == null) {
						logger.error("Failed to find permission " + permName);
					}
					else perms.add(perm);
				}
				
				SYSTEM_PERMISSION_OBJECTS.put(organizationId, perms);
			}
			catch(FactoryException | ArgumentException e) {
				logger.error(e);
			}
		}
		else {
			perms = SYSTEM_PERMISSION_OBJECTS.get(organizationId);
		}
		return perms;
	}
	
}
