package org.cote.rocket.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.INameIdGroupFactory;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.propellant.objects.FormElementType;
import org.cote.propellant.objects.types.ElementEnumType;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.FormElementFactory;
import org.cote.rocket.factory.LifecycleFactory;
import org.cote.rocket.service.rest.BaseService;
import org.junit.Test;

public class TestCapabilities extends BaseAccelerantTest {
	
	
	
	@Test
	public void TestInterfaceCast(){
		LifecycleFactory lf = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE));
		INameIdFactory ilf = (INameIdFactory)lf;
		INameIdGroupFactory iglf = (INameIdGroupFactory)lf;
	}
	
	@Test
	public void TestForms(){
		boolean add = false;
		try {
			DirectoryGroupType formsElDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/FormElements", testUser.getOrganizationId());
			FormElementType el = ((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).newFormElement(testUser, formsElDir.getId());
			String name = UUID.randomUUID().toString();
			el.setName(name);
			el.setElementName("el-" + name);
			el.setElementType(ElementEnumType.STRING);
			BaseService.denormalize(el);
			add = BaseService.add(AuditEnumType.FORMELEMENT, el, getRequestMock(testUser));
		
		} catch (FactoryException | ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertTrue("Failed to add elements", add);
	}
	
	@Test
	public void TestRequestMock(){
		BaseGroupType group = null;
		try{
		group = org.cote.accountmanager.service.rest.BaseService.findGroup(GroupEnumType.DATA, "~/", getRequestMock(testUser));
		}
		catch(Exception e){
			logger.error("Error",e);
		}
		assertNotNull("Group is null",group);
	}
}