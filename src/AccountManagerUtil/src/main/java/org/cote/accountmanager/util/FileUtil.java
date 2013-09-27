package org.cote.accountmanager.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class FileUtil {
	public static String getFileAsString(String path){
		
		String out_str = null;
		byte[] data = getFile(path);
		try {
			out_str = new String(data,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out_str;
	}
	public static byte[] getFile(String path){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		File f = new File(path);
		if(f.exists() == false) return new byte[0];
		
		try{
			FileInputStream fis = new FileInputStream(path);
			copyStream(fis,baos);
			fis.close();
		}
		catch(IOException ie){
			ie.printStackTrace();
		}
		return baos.toByteArray();
	}
	public static long copyStream(InputStream in, OutputStream out) throws IOException{
		long copied=0;
		synchronized(in){
			synchronized(out){
				byte[] buffer = new byte[8192];
				int bytesRead=0;
				while (bytesRead != -1) {
					bytesRead = in.read(buffer);
					if (bytesRead == -1) break;
					copied+=(long)bytesRead;
					out.write(buffer, 0, bytesRead);
				}
			}
		}
		return copied;
	}
	public static boolean emitFile(String path, String contents){
		byte[] cont = new byte[0];
		boolean out_bool = false;
		try {
			cont = contents.getBytes("UTF-8");
			out_bool = emitFile(path, cont);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return out_bool;
		
	}
	public static boolean makePath(String path){
		boolean out_bool = false;
		File f = new File(path);
		if(f.exists() == false) out_bool = f.mkdirs();
		else out_bool = true;
		
		return out_bool;
	}
	public static boolean emitFile(String path, byte[] contents){
		boolean out_bool = false;
		File f = new File(path);
		File p = f.getParentFile();
		if(p.exists() == false) p.mkdirs();
		if(f.exists() == true) f.delete();
		try{
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(contents);
			fos.flush();
			fos.close();
			out_bool = true;
		}
		catch(IOException ie){
			ie.printStackTrace();
		}
		return out_bool;
		
	}
}
