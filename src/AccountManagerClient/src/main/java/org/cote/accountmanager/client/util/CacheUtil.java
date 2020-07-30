package org.cote.accountmanager.client.util;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.util.FileUtil;
import org.cote.accountmanager.util.JSONUtil;

public class CacheUtil {
	
	private static Pattern cleanPattern = Pattern.compile("[^A-Za-z0-9]");
	private static String HASH_PROVIDER = "SHA-512";
	private static MessageDigest hash_algorithm = null;
	public static final Logger logger = LogManager.getLogger(CacheUtil.class);
	
	@SuppressWarnings("unchecked")
	public static <T> T readCache(String name, Class objClass){
		T obj = null;
		String keyName =  cleanPattern.matcher(getDigestAsString(name.getBytes(),new byte[0])).replaceAll("");
		String path = ClientUtil.getCachePath() + "/" + keyName + ".json";
		File f = new File(path);
		if(f.exists()){
			obj = (T)JSONUtil.importObject(FileUtil.getFileAsString(f), objClass);
		}
		return obj;
	}
	public static <T> boolean cache(String name, T obj){
		String keyName =  cleanPattern.matcher(getDigestAsString(name.getBytes(),new byte[0])).replaceAll("");
		return FileUtil.emitFile(ClientUtil.getCachePath() + "/" + keyName + ".json", JSONUtil.exportObject(obj));
	}
	
	private static MessageDigest getMessageDigest(){
		return getMessageDigest(false);
	}
	private static MessageDigest getMessageDigest(boolean use_singleton){
		if(use_singleton && hash_algorithm != null) return hash_algorithm;
		MessageDigest digest = null;
		try{
			digest = MessageDigest.getInstance(HASH_PROVIDER);
		}
		catch(NoSuchAlgorithmException e){
			logger.error("Trace",e);
		}
		if(use_singleton && digest != null) hash_algorithm = digest;
		return digest;
	}
	public static String getDigestAsString(byte[] in_bytes, byte[] salt){
		byte[] hash = getDigest(in_bytes, salt);
		return Base64.getEncoder().encodeToString(hash);
		
	}
	public static byte[] getDigest(byte[] in_bytes, byte[] salt){
		MessageDigest digest = getMessageDigest();
		/// 2015/06/23 - Changed for CredentialType updated
		///
		digest.reset();
		//if(salt.length > 0){
			digest.update(salt);
		//}
		digest.digest(in_bytes);
		digest.update(in_bytes,0,in_bytes.length);
		return digest.digest();
	}
}
