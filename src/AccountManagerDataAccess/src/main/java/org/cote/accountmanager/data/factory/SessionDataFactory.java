package org.cote.accountmanager.data.factory;

import org.cote.accountmanager.objects.UserSessionDataType;
import org.cote.accountmanager.objects.UserSessionType;

public class SessionDataFactory {
	public void setValue(UserSessionType session, String name, String data){
		UserSessionDataType newData = new UserSessionDataType();
		newData.setName(name);
		newData.setValue(data);
		session.getChangeSessionData().add(newData);
		int index = getIndex(session, name);
		if(index >= 0){
			session.getSessionData().remove(index);
		}
		if(data != null){
			//session.setDataSize(session.getDataSize() + 1);
			session.getSessionData().add(newData);
		}
		//else session.setDataSize(session.getDataSize() - 1);
		session.setDataSize(session.getSessionData().size());
	}
	public String getValue(UserSessionType session, String name){
		int index = getIndex(session, name);
		if(index <= -1) return null;
		return session.getSessionData().get(index).getValue();
	}
	
	private boolean hasKey(UserSessionType session, String name){
		return (getIndex(session, name) >= 0);
	}
	
	private int getIndex(UserSessionType session, String name){
		int out_index = -1;
		for(int i = session.getSessionData().size() - 1; i >= 0; i--){
			if(session.getSessionData().get(i).getName().equals(name)){
				out_index = i;
				break;
			}
		}
		return out_index;
	}
}
