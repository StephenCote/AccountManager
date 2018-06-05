package org.cote.rocket.util;

import java.util.HashMap;
import java.util.Map;

import org.cote.accountmanager.objects.NameIdType;
import org.cote.propellant.objects.TaskType;

public class ImportMap{
	///
	/// Given some list of volatile artifact types, stash new/pre-saved dependent tasks against a custom urn value that is oldName-parentId-groupId
	/// 
	public Map<String,NameIdType> urnMap = new HashMap<>();
	public ImportMap(){
	
	}
	public String getKey(TaskType task){
		return task.getName() + "-" + task.getParentId() + "-" + task.getGroupId();
	}

	
}