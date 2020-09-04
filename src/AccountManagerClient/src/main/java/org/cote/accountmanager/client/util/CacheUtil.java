package org.cote.accountmanager.client.util;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.client.ClientContext;
import org.cote.accountmanager.util.DirectoryUtil;
import org.cote.accountmanager.util.FileUtil;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.accountmanager.util.SecurityUtil;

public class CacheUtil {
	
	public static Pattern cleanPattern = Pattern.compile("[^A-Za-z0-9\\.\\-]");

	public static final Logger logger = LogManager.getLogger(CacheUtil.class);
	
	public static boolean clearCache(ClientContext context) {
		String cachePath = ClientUtil.getCachePath() + "/" + context.getContextId();
		boolean outBool = false;
		if(context.getContextId() == null) {
			logger.error("Invalid context id");
			return outBool;
		}
		if(context.getContextId().equals(context.getDefaultContextId())) {
			logger.warn("Clearing default cache");
		}
		else {
			logger.info("Clearing " + context.getContextId());
		}
		DirectoryUtil du = new DirectoryUtil(cachePath);
		List<File> files = du.dir();
		for(File f : files){
			f.delete();
		}
		outBool = true;
		return outBool;
	}
	
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
		String cName = name;
		/// SecurityUtil.getDigestAsString(name.getBytes(),new byte[0])
		return cleanPattern.matcher(cName).replaceAll("");
	}
	public static <T> boolean cache(ClientContext context, String name, T obj){
		String keyName =  getCacheKeyName(name);
		return FileUtil.emitFile(ClientUtil.getCachePath() + "/" + context.getContextId() + "/" + keyName + ".json", JSONUtil.exportObject(obj));
	}
	

}
