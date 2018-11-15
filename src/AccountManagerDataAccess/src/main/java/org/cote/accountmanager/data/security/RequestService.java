package org.cote.accountmanager.data.security;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.ControlFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.ControlActionEnumType;
import org.cote.accountmanager.objects.ControlEnumType;
import org.cote.accountmanager.objects.ControlType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;

public class RequestService {

	public static final Logger logger = LogManager.getLogger(RequestService.class);
	
	public static List<ControlType> getRequestControls(NameIdType object, boolean includeParent) throws FactoryException, ArgumentException{
		INameIdFactory factory = Factories.getFactory(FactoryEnumType.valueOf(object.getNameType().toString()));

		List<ControlType> ctls = ((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).getControlsForType(object, ControlEnumType.POLICY, ControlActionEnumType.ACCESS,true, false);
		if(includeParent) {
			NameIdType parentObj = null;
			if(factory.isClusterByGroup() || object.getNameType() == NameEnumType.DATA) {
				parentObj = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getById(((NameIdDirectoryGroupType)object).getGroupId(), object.getOrganizationId());
			}
			else if(factory.isClusterByParent() && object.getParentId().compareTo(0L) > 0) {
				parentObj = factory.getById(object.getParentId(), object.getOrganizationId());
			}
			
			if(parentObj != null) {
				ctls.addAll(((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).getControlsForType(parentObj, ControlEnumType.POLICY, ControlActionEnumType.ACCESS,true, false));
			}
		}
		return ctls;
	}
	
	public static boolean isRequestable(NameIdType object) throws FactoryException, ArgumentException {
		return (getRequestControls(object, true).size() > 0);
	}
	
}
