/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
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
