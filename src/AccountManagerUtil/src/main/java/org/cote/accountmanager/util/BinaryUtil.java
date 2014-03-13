package org.cote.accountmanager.util;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

public class BinaryUtil {
	public static String fromBase64Str(String in_base64){
		return fromBase64Str(in_base64.getBytes());
	}
	public static String fromBase64Str(byte[] in_base64){
		String out_str = null;
		try {
			out_str = (new String(fromBase64(in_base64),"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out_str;

	}
	public static byte[] fromBase64(byte[] in_base64){
		return Base64.decodeBase64(in_base64);
	}
	public static String toBase64Str(String in_str){
		byte[] b = new byte[0];
		try {
			b = in_str.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return toBase64Str(b);
	}
	public static String toBase64Str(byte[] in_str){
		String out_str = null;
		try {
			out_str = new String(toBase64(in_str),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out_str;
		//return (new String(toBase64(in_str)));
	}
	public static byte[] toBase64(byte[] in_str){
		return Base64.encodeBase64(in_str);
		
	}
}
