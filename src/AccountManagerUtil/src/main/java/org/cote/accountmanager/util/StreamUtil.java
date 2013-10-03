package org.cote.accountmanager.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtil {
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

	public static byte[] getStreamBytes(InputStream in) throws IOException{
		
		int copied = 0;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		synchronized(in){
			int mark = 8192;
			byte[] read_buffer = new byte[mark];
			int bytesRead=0;

			while (bytesRead != -1) {
				bytesRead = in.read(read_buffer);
				if (bytesRead == -1) break;
				baos.write(read_buffer,0,bytesRead);
				copied += bytesRead;
			}
		}
		return baos.toByteArray();

	}

	
	public static String streamToString(BufferedInputStream in) throws IOException{
		int offset=2048;
		int bytes_read=0;
		String ret="";
		int totalBytes=0;
		StringBuffer sb=new StringBuffer();

		int max=in.available();
		boolean breakOut=false;

		while(bytes_read != -1) {
			byte[] buffer = new byte[offset];
			bytes_read = in.read(buffer,0,offset);
			if(bytes_read == -1) break;
			if(bytes_read > 0 && buffer[bytes_read -1] == -1){
				bytes_read--;
				breakOut=true;
			}
			totalBytes+=bytes_read;
			String sStr=new String(buffer, 0, bytes_read);
			sb.append(sStr);

			if(bytes_read >= max && max > 0) break;
			if(breakOut) break;
		}
		return sb.toString();
	}
	public static byte[] fileHandleToBytes(File file){
		if(file == null || file.exists() == false){
			return new byte[0];
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try{
			if(file.exists()){
				FileInputStream fis=new FileInputStream(file);
				copyStream(fis,baos);
				fis.close();
			}
		}
		catch(IOException e){
			System.out.println("StreamUtil:: fileHandleToBytes: " + e.toString());
		}
		return baos.toByteArray();
	}
	public static byte[] fileToBytes(String fileName){
		File f = new File(fileName);
		
		return fileHandleToBytes(f);
		
	}
}
