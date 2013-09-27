package org.cote.accountmanager.data;

import org.cote.accountmanager.objects.DataCellType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;


public class DataCell extends DataCellType {
	private Object cellData = null;
	private boolean dirty = false;
	
	public Object getCellData() {
		return cellData;
	}

	public void setCellData(Object cellData) {
		this.cellData = cellData;
		this.dirty = true;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	
	public <T> T getValue() throws DataAccessException{
		T out_val = (T)cellData;
		return out_val;
	}
	public <T> void setValue(T value) throws DataAccessException{
		this.setCellData(value);
		//System.out.println(this.dataType + ":" + value);

		if(this.dataType != SqlDataEnumType.NULL){
			SqlDataEnumType new_type = SqlTypeUtil.getType(value);
			if(new_type != SqlDataEnumType.NULL) this.setDataType(new_type);
		}

	}
	
}
