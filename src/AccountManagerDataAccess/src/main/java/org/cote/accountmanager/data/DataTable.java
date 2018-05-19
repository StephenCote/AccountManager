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
package org.cote.accountmanager.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cote.accountmanager.objects.DataColumnType;
import org.cote.accountmanager.objects.DataTableType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;

public class DataTable extends DataTableType {
	private Map<String,Integer> columnMap = Collections.synchronizedMap(new HashMap<>());
	private static Map<String,Boolean> restrictSelectMap = new HashMap<>();
	private static Map<String, Boolean> restrictUpdateMap = new HashMap<>();

	public void setRestrictUpdateColumn(String columnName, boolean restricted){
		restrictUpdateMap.put(columnName,  restricted);
	}
	public void setRestrictSelectColumn(String columnName, boolean restricted){
		restrictSelectMap.put(columnName,  restricted);
	}
	public boolean getCanSelectColumn(String columnName)
	{
		return (!(restrictSelectMap.containsKey(columnName) && restrictSelectMap.get(columnName).booleanValue()));
	}
	
	public boolean getCanUpdateColumn(String columnName)
	{
		return (!(restrictUpdateMap.containsKey(columnName) && restrictUpdateMap.get(columnName).booleanValue()));
	}
	
	public int getColumnIndex(String columnName) throws DataAccessException{
		if(!columnMap.containsKey(columnName)) throw new DataAccessException("Invalid column '" + columnName + "'");
		return getColumnMap().get(columnName).intValue();
	}
	public Map<String, Integer> getColumnMap() {
		return columnMap;
	}
	public DataColumnType addColumn(String name, int index, int size, SqlDataEnumType type){
		DataColumnType col = new DataColumnType();
		col.setColumnIndex(index);
		col.setColumnName(name);
		col.setColumnSize(size);
		col.setDataType(type);
		columnMap.put(name, index);
		this.getColumns().add(col);
		this.setColumnSize(this.getColumns().size());
		return col;
	}
	public DataRow addNewRow(){
		DataRow row = newRow();
		this.getRows().add(row);
		return row;
	}
	public DataRow newRow(){
		DataRow row = new DataRow();
		row.setTable(this);
		int colLen = this.getColumns().size();
		for(int i = 0; i < colLen; i++){
			DataCell cell = new DataCell();
			DataColumnType col = this.getColumns().get(i);
			cell.setColumnIndex(col.getColumnIndex());
			cell.setColumnName(col.getColumnName());
			cell.setDataType(col.getDataType());
			row.getCells().add(cell);
		}
		return row;
	}
}
