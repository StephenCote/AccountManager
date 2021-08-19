/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cote.accountmanager.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileUtil {
	public static final Logger logger = LogManager.getLogger(FileUtil.class);
	public static String getFileAsString(String path){
		return getFileAsString(new File(path));
	}
	private FileUtil(){
		
	}
	public static String getFileAsString(File f){		
		return new String(getFile(f), StandardCharsets.UTF_8);
	}
	public static byte[] getFile(String path){
		return getFile(new File(path));
	}
	public static byte[] getFile(File f){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		if(!f.exists())
			return new byte[0];
		
		try{
			FileInputStream fis = new FileInputStream(f);
			copyStream(fis,baos);
			fis.close();
		}
		catch(IOException ie){
			logger.error(ie.getMessage());
			logger.error(ie);
		}
		return baos.toByteArray();
	}
	public static long copyStream(InputStream in, OutputStream out) throws IOException{
		long copied=0;
		Map<String, Object> locks = new HashMap<>();
		locks.put("input", in);
		locks.put("output", out);
		synchronized(locks.get("input")){
			synchronized(locks.get("output")){
				byte[] buffer = new byte[8192];
				int bytesRead=0;
				while (bytesRead != -1) {
					bytesRead = in.read(buffer);
					if (bytesRead == -1)
						break;
					copied+=(long)bytesRead;
					out.write(buffer, 0, bytesRead);
				}
			}
		}
		return copied;
	}
	public static boolean emitFile(String path, String contents)
	{
		return emitFile(path, contents, StandardCharsets.UTF_8);
	}
	public static boolean emitFile(String path, String contents, Charset encoding){
		return emitFile(path, contents.getBytes(encoding));
	}
	public static boolean makePath(String path){
		boolean outBool = false;
		File f = new File(path);
		if(!f.exists())
			outBool = f.mkdirs();
		else outBool = true;
		return outBool;
	}
	public static boolean emitFile(String path, byte[] contents){
		boolean outBool = false;
		File f = new File(path);
		File p = f.getParentFile();
		if(!p.exists())
			p.mkdirs();
		if(f.exists()){
			if(!f.delete()){
				logger.error(String.format("Failed to delete %s",path));
			}
		}
		FileOutputStream fos = null;
		try{
			fos = new FileOutputStream(f);
			fos.write(contents);
			fos.flush();
			fos.close();
			outBool = true;
		}
		catch(IOException ie){
			logger.error(ie.getMessage());
			logger.error(ie);
		}
		finally{

			try {
				if(fos != null) fos.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}
		return outBool;
		
	}
}
