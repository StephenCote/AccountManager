package org.cote.accountmanager.beans;

import java.util.HashMap;

import org.cote.accountmanager.objects.VaultType;

public class VaultBean extends VaultType {
	private SecurityBean activeKeyBean = null;
	private SecurityBean vaultKeyBean = null;
	private HashMap<String, SecurityBean> symmetricKeyMap = new HashMap<>();
	
	
	
	
	public HashMap<String, SecurityBean> getSymmetricKeyMap() {
		return symmetricKeyMap;
	}
	public void setSymmetricKeyMap(HashMap<String, SecurityBean> symmetricKeyMap) {
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