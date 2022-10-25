package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.cote.accountmanager.factory.FieldMap;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PatchSetType;
import org.cote.accountmanager.objects.PatchType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.JSONUtil;
import org.junit.Test;

public class TestFactoryPatch extends BaseDataAccessTest {

	@Test
	public void TestDataFactoryPatch() {
		DataFactory dfact = null;
		DataType data = null;
		PatchBundle bundle = getPatchBundle();
		boolean updated = false;
		try {
			dfact = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA));
			updated = dfact.patch(bundle.getPatchSet());
			logger.info("Updated: " + updated);
		} catch (FactoryException e) {
			logger.error(e);
		}
	}
	
	
	@Test
	public void TestPatchAuthorization() {
		PatchBundle bundle = getPatchBundle();
		
		/*
		 * 		BasePermissionType permission = getPermission(actor, object, permissionBase);
		
		return isAuthorized(actor, object, permissionBase, (permission == null ? new BasePermissionType[]{} : new BasePermissionType[]{permission}));

		 */
		
	}
	
	private PatchBundle getPatchBundle() {
		DirectoryGroupType dir = null;
		GroupFactory gfact = null;
		DataType data = null;
		String dataName = UUID.randomUUID().toString();
		try {
			gfact = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP));
			dir = gfact.getCreateDirectory(testUser, "PatchTest", testUser.getHomeDirectory(), testUser.getOrganizationId());
			data = this.newTextData(dataName, "Example data", testUser, dir);
		} catch (FactoryException | ArgumentException e) {
			logger.error(e);
		}
		assertNotNull("Example data is null", data);
		
		PatchSetType pst = Factories.newPatchSet(ColumnEnumType.OBJECTID, data.getObjectId());
		pst.getPatches().add(Factories.newPatch(ColumnEnumType.DESCRIPTION, "New Description"));
		pst.getPatches().add(Factories.newPatch(ColumnEnumType.DIMENSIONS, "DinkyDo!"));
		
		return new PatchBundle(data, pst);
	}

	
	
	
}
class PatchBundle{
	private PatchSetType patchSet = null;
	private NameIdType object = null;
	public PatchBundle(NameIdType obj, PatchSetType pst) {
		patchSet = pst;
		object = obj;
	}
	public PatchSetType getPatchSet() {
		return patchSet;
	}
	public NameIdType getObject() {
		return object;
	}
	
}
