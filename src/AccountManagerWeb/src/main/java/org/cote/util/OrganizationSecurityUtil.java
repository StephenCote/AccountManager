package org.cote.util;

import java.io.UnsupportedEncodingException;

import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.security.OrganizationSecurity;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.util.BinaryUtil;
import org.cote.accountmanager.util.SecurityUtil;

public class OrganizationSecurityUtil {
	public static String encipherString(String inStr, OrganizationType org){
		return BinaryUtil.toBase64Str(encipherBytes(inStr.getBytes(), org));
	}
	public static String decipherString(String inStr, OrganizationType org){
		String out_str = null;
		try{
			out_str = new String(decipherBytes(BinaryUtil.fromBase64(inStr.getBytes()), org),"UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out_str;
	}
	public static byte[] encipherBytes(byte[] inBytes, OrganizationType org){
		SecurityBean secBean = OrganizationSecurity.getSecurityBean(org);
		return SecurityUtil.encipher(secBean, inBytes);
	}
	public static byte[] decipherBytes(byte[] inBytes, OrganizationType org){
		SecurityBean secBean = OrganizationSecurity.getSecurityBean(org);
		return SecurityUtil.decipher(secBean, inBytes);
	}
}
