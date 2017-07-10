package org.cote.accountmanager.beans;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cote.accountmanager.objects.VaultType;

public class VaultBean extends VaultType {
	private SecurityBean activeKeyBean = null;
	private SecurityBean vaultKeyBean = null;
	private Map<String, SecurityBean> symmetricKeyMap = null;
	
	public VaultBean(){
		symmetricKeyMap =  Collections.synchronizedMap(new HashMap<>());
	}
	
	
	
	
	public Map<String, SecurityBean> getSymmetricKeyMap() {
		return symmetricKeyMap;
	}
	public void setSymmetricKeyMap(Map<String, SecurityBean> symmetricKeyMap) {
		this.symmetricKeyMap = symmetricKeyMap;
	}
	public SecurityBean getActiveKeyBean() {
		return activeKeyBean;
	}
	public void setActiveKeyBean(SecurityBean activeKeyBean) {
		this.activeKey = activeKeyBean;
		this.activeKeyBean = activeKeyBean;
	}
	public SecurityBean getVaultKeyBean() {
		return vaultKeyBean;
	}
	public void setVaultKeyBean(SecurityBean vaultKeyBean) {
		this.vaultKeyBean = vaultKeyBean;
		this.vaultKey = vaultKeyBean;
	}
	
	
	
}