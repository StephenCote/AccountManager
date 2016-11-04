package org.cote.accountmanager.data.factory;

import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.DirectoryGroupType;

public interface INameIdGroupFactory extends INameIdFactory {
	public int countInGroup(BaseGroupType group) throws FactoryException;
	public <T> List<T>  listInGroup(BaseGroupType group, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException;
	public <T> T getByNameInGroup(String name, DirectoryGroupType parentGroup) throws FactoryException, ArgumentException;
	public <T> T getByNameInGroup(String name, long parentId, DirectoryGroupType parentGroup) throws FactoryException, ArgumentException;
	public <T> T getByNameInGroup(String name, long parentGroupId, long organizationId) throws FactoryException, ArgumentException;
	public <T> T getByNameInGroup(String name, long parentId, long parentGroupId, long organizationId) throws FactoryException, ArgumentException;
	public <T> List<T> search(String searchValue, long startRecord, int recordCount, DirectoryGroupType dir) throws FactoryException, ArgumentException;
}
