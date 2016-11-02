package org.cote.accountmanager.data.factory;

import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;

public interface IFactory {
	public void writeSpool(String tableName);
	public <T> void normalize(T object) throws ArgumentException, FactoryException;
	public <T> void denormalize(T object) throws ArgumentException, FactoryException;
	public <T> void populate(T object) throws FactoryException,ArgumentException;
	public <T> void depopulate(T object) throws FactoryException,ArgumentException;
	public void mapBulkIds(NameIdType map);
	public boolean update(NameIdType map) throws FactoryException;
	public boolean update(NameIdType map, ProcessingInstructionType instruction) throws FactoryException;
	public <T> boolean updateBulk(List<T> map) throws FactoryException;
	public <T> boolean updateBulk(List<T> map, ProcessingInstructionType instruction) throws FactoryException;
	public <T> boolean deleteBulk(List<T> map, ProcessingInstructionType instruction) throws FactoryException;
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction);
	public List<NameIdType> getByField(QueryField field, long organization_id) throws FactoryException, ArgumentException;
	public List<NameIdType> getByField(QueryField field, ProcessingInstructionType instruction, long organization_id) throws FactoryException, ArgumentException;
	public List<NameIdType> getByField(QueryField[] fields, long organization_id) throws FactoryException, ArgumentException;
	public List<NameIdType> getByField(QueryField[] fields, ProcessingInstructionType instruction, long organization_id) throws FactoryException, ArgumentException;
	public <T> T getByObjectId(String id, long organizationId) throws FactoryException, ArgumentException;
	public <T> List<T> getList(QueryField[] fields, ProcessingInstructionType pi, long organizationId) throws FactoryException, ArgumentException;	
	public <T> List<T> getList(QueryField[] fields, long organizationId) throws FactoryException, ArgumentException;
	public <T> List<T>  getPaginatedList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException;
	public <T> List<T>  getPaginatedList(QueryField[] fields, ProcessingInstructionType instruction, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException;
	public <T> List<T> getListByIds(long[] ids, long organizationId) throws FactoryException, ArgumentException;
	public <T> List<T> getListByIds(long[] ids, ProcessingInstructionType instruction, long organizationId) throws FactoryException, ArgumentException;
	public <T> T getById(long id, long organizationId) throws FactoryException, ArgumentException;
	public <T> T getByUrn(String urn);
	public void clearCache();
}
