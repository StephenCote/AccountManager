package org.cote.accountmanager.data.io;

import java.util.ArrayList;
import java.util.List;

import org.cote.accountmanager.data.DataCell;

public class BulkInsertMeta {
	private int parameterCount = 0;
	private String insertTemplate = null;
	private String deleteTemplate = null;
	private String updateTemplate = null;
	List<DataCell> queryCells = new ArrayList<DataCell>();
	public BulkInsertMeta(){
		
	}
	public int getParameterCount() {
		return parameterCount;
	}
	public void setParameterCount(int parameterCount) {
		this.parameterCount = parameterCount;
	}
	public String getInsertTemplate() {
		return insertTemplate;
	}
	public void setInsertTemplate(String insertTemplate) {
		this.insertTemplate = insertTemplate;
	}
	
	public String getDeleteTemplate() {
		return deleteTemplate;
	}
	public void setDeleteTemplate(String deleteTemplate) {
		this.deleteTemplate = deleteTemplate;
	}
	public String getUpdateTemplate() {
		return updateTemplate;
	}
	public void setUpdateTemplate(String updateTemplate) {
		this.updateTemplate = updateTemplate;
	}
	public List<DataCell> getQueryCells() {
		return queryCells;
	}
	public void setQueryCells(List<DataCell> queryCells) {
		this.queryCells = queryCells;
	}
	
}
