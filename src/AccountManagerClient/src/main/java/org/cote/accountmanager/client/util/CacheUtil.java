package org.cote.accountmanager.client.util;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.client.ClientContext;
import org.cote.accountmanager.util.FileUtil;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.accountmanager.util.SecurityUtil;

public class CacheUtil {
	
	public static Pattern cleanPattern = Pattern.compile("[^A-Za-z0-9]");

	public static final Logger logger = LogManager.getLogger(CacheUtil.class);
	
	@SuppressWarnings("unchecked")
	public static <T> T readCache(ClientContext context, String name, Class objClass){
		T obj = null;
		String keyName =  getCacheKeyName(name);
		String path = ClientUtil.getCachePath() + "/" + context.getContextId() + "/" + keyName + ".json";
		File f = new File(path);
		if(f.exists()){
			obj = (T)JSONUtil.importObject(FileUtil.getFileAsString(f), objClass);
		}
		return obj;
	}
	public static String getCacheKeyName(String name) {
		return cleanPattern.matcher(SecurityUtil.getDigestAsString(name.getBytes(),new byte[0])).replaceAll("");
	}
	public static <T> boolean cache(ClientContext context, String name, T obj){
		String keyName =  getCacheKeyName(name);
		return FileUtil.emitFile(ClientUtil.getCachePath() + "/" + context.getContextId() + "/" + keyName + ".json", JSONUtil.exportObject(obj));
	}
	

}
