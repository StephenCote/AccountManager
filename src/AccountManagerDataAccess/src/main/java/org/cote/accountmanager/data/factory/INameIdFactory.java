/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cote.accountmanager.data.factory;

import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;

public interface INameIdFactory {
	//public String getDataTable();
	//public void writeSpool(String tableName);
	
	public <T> T find(String path) throws FactoryException,ArgumentException;
	public <T> T find(String type, String path, long organizationId) throws FactoryException, ArgumentException;
	public <T> T find(UserType user, String type, String path, long organizationId) throws FactoryException, ArgumentException;
	public <T> T makePath(String type, String pathBase, long organizationId) throws FactoryException, ArgumentException, DataAccessException;
	public <T> T makePath(UserType user, String type, String pathBase, long organizationId) throws FactoryException, ArgumentException, DataAccessException;
	public void setBatchSize(int batchSize);
	public boolean getBulkMode();
	public void registerProvider();
	public boolean isClusterByGroup();
	public boolean isClusterByParent();
	public boolean isParticipation();
	public FactoryEnumType getFactoryType();
	public String getCacheReport();
	public void writeSpool();
	public <T> void normalize(T object) throws ArgumentException, FactoryException;
	public <T> void denormalize(T object) throws ArgumentException, FactoryException;
	public <T> void populate(T object) throws FactoryException,ArgumentException;
	public <T> void depopulate(T object) throws FactoryException,ArgumentException;
	public void mapBulkIds(NameIdType map);
	
	public int deleteByOrganization(long organizationId) throws FactoryException, ArgumentException;
	public int deleteByOwner(UserType owner) throws FactoryException, ArgumentException;
	public <T> boolean delete(T object) throws ArgumentException, FactoryException;
	public <T> boolean deleteBulk(List<T> map, ProcessingInstructionType instruction) throws FactoryException;

	public <T> boolean add(T object) throws ArgumentException,FactoryException;
	
	public <T> boolean update(T map) throws FactoryException;
	public <T> boolean update(T map, ProcessingInstructionType instruction) throws FactoryException;
	public <T> boolean updateBulk(List<T> map) throws FactoryException;
	public <T> boolean updateBulk(List<T> map, ProcessingInstructionType instruction) throws FactoryException;

	//public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction);
	public <T> T getByNameInParent(String name, String type, long parent_id, long organization_id) throws FactoryException, ArgumentException;
	public <T> T getByNameInParent(String name, long parent_id, long organization_id) throws FactoryException, ArgumentException;
	public <T> T getByName(String name, long organizationId) throws FactoryException, ArgumentException;
	public List<NameIdType> getByField(QueryField field, long organization_id) throws FactoryException, ArgumentException;
	public List<NameIdType> getByField(QueryField field, ProcessingInstructionType instruction, long organization_id) throws FactoryException, ArgumentException;
	public List<NameIdType> getByField(QueryField[] fields, long organization_id) throws FactoryException, ArgumentException;
	public List<NameIdType> getByField(QueryField[] fields, ProcessingInstructionType instruction, long organization_id) throws FactoryException, ArgumentException;

	public <T> List<T> listByOwner(UserType user) throws FactoryException, ArgumentException;
	public <T> List<T> listByName(String name, long organization_id) throws FactoryException, ArgumentException;
	public <T> List<T> list(QueryField[] fields, ProcessingInstructionType pi, long organizationId) throws FactoryException, ArgumentException;	
	public <T> List<T> list(QueryField[] fields, long organizationId) throws FactoryException, ArgumentException;
	public <T> List<T>  paginateList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException;
	public <T> List<T>  paginateList(QueryField[] fields, ProcessingInstructionType instruction, long startRecord, int recordCount, long organizationId)  throws FactoryException, ArgumentException;
	public <T> List<T> listByIds(long[] ids, long organizationId) throws FactoryException, ArgumentException;
	public <T> List<T> listByIds(long[] ids, ProcessingInstructionType instruction, long organizationId) throws FactoryException, ArgumentException;

	public <T> T getByObjectId(String id, long organizationId) throws FactoryException, ArgumentException;
	public <T> T getById(long id, long organizationId) throws FactoryException, ArgumentException;
	public <T> T getByUrn(String urn);
	public <T> List<T> search(String searchValue, long startRecord, int recordCount, long org) throws FactoryException, ArgumentException;
	public <T> List<T> search(QueryField[] fields, ProcessingInstructionType pi, long organizationId) throws FactoryException, ArgumentException;
	public int countInOrganization(long organization_id) throws FactoryException;
	public <T> int countInParent(T parent) throws FactoryException;
	public <T> List<T> listInParent(String type, long parentId, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException;
	public <T> List<T> listInParent(long parentId, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException;
	public void clearCache();
	public String reportCacheSize();
	
	public void removeBranchFromCache(NameIdType obj);
	public void removeFromCache(NameIdType obj);
	public void removeFromCache(NameIdType obj, String keyName);
	public <T> T readCache(String name);
	public <T> T readCache(long id);
	public boolean updateToCache(NameIdType obj) throws ArgumentException;
	public boolean updateToCache(NameIdType obj,String keyName) throws ArgumentException;
	public boolean addToCache(NameIdType map) throws ArgumentException;
	public boolean addToCache(NameIdType map, String keyName) throws ArgumentException;
	
}
