package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.cote.accountmanager.factory.FieldMap;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PatchSetType;
import org.cote.accountmanager.objects.PatchType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.util.JSONUtil;
import org.junit.Test;

public class TestFactoryPatch extends BaseDataAccessTest {

	@Test
	public void TestDataFactoryPatch() {
		DataFactory dfact = null;
		DataType data = null;
		PatchBundle bundle = getDataPatchBundle();
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
		PatchBundle bundle = getDataPatchBundle();
		boolean patched = BaseService.patch(AuditEnumType.DATA, bundle.getPatchSet(), testUser);
		assertTrue("Expected data to be patched", patched);
		PatchBundle invalidGroup = getInvalidDataBundle(ColumnEnumType.GROUPID, "0");
		boolean notPatched = BaseService.patch(AuditEnumType.DATA, invalidGroup.getPatchSet(), testUser);
		assertFalse("Expected data to not be patched", notPatched);
		
		PatchBundle roleBundle = getRolePatchBundle();
		boolean patched2 = BaseService.patch(AuditEnumType.ROLE, roleBundle.getPatchSet(), testUser);
		assertTrue("Expected role to be patched", patched2);
		PatchBundle invalidRole = getInvalidRolePatchBundle();
		logger.info(JSONUtil.exportObject(invalidRole));
		boolean notPatched2 = BaseService.patch(AuditEnumType.ROLE, invalidRole.getPatchSet(), testUser);
		assertFalse("Expected role to not be patched", notPatched2);

		
		/*
		 * 		BasePermissionType permission = getPermission(actor, object, permissionBase);
		
		return isAuthorized(actor, object, permissionBase, (permission == null ? new BasePermissionType[]{} : new BasePermissionType[]{permission}));

		 */
		
	}
	
	private PatchBundle getDataPatchBundle() {
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
	private PatchBundle getInvalidRolePatchBundle() {
		PatchBundle bundle = getRolePatchBundle();
		BaseRoleType otherRole = this.getUserRole(testUser2);
		PatchSetType pst = Factories.newPatchSet(ColumnEnumType.OBJECTID, bundle.getObject().getObjectId());
		pst.getPatches().add(Factories.newPatch(ColumnEnumType.PARENTID, otherRole.getId() + ""));
		
		return new PatchBundle(bundle.getObject(), pst);
	}	
	private PatchBundle getRolePatchBundle() {
		String roleName = UUID.randomUUID().toString();
		BaseRoleType parent = this.getUserRole(testUser);
		BaseRoleType role = this.getRole(testUser, roleName, RoleEnumType.PERSON, parent);
		assertNotNull("Role is null", role);
		
		PatchSetType pst = Factories.newPatchSet(ColumnEnumType.OBJECTID, role.getObjectId());
		pst.getPatches().add(Factories.newPatch(ColumnEnumType.NAME, roleName + " NEW"));
		
		return new PatchBundle(role, pst);
	}
	
	private PatchBundle getInvalidDataBundle(ColumnEnumType valueType, String value) {
		PatchBundle bundle = getDataPatchBundle();
		DataType data = (DataType)bundle.getObject();
		PatchSetType pst = Factories.newPatchSet(ColumnEnumType.OBJECTID, data.getObjectId());
		pst.getPatches().add(Factories.newPatch(valueType, value));
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
