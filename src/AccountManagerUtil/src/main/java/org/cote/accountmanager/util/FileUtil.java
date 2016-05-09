/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

public class FileUtil {
	public static final Logger logger = Logger.getLogger(FileUtil.class.getName());
	public static String getFileAsString(String path){
		return getFileAsString(new File(path));
	}
	public static String getFileAsString(File f){		
		String out_str = null;
		byte[] data = getFile(f);
		try {
			out_str = new String(data,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return out_str;
	}
	public static byte[] getFile(String path){
		return getFile(new File(path));
	}
	public static byte[] getFile(File f){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		if(f.exists() == false)
			return new byte[0];
		
		try{
			FileInputStream fis = new FileInputStream(f);
			copyStream(fis,baos);
			fis.close();
		}
		catch(IOException ie){
			logger.error(ie.getMessage());
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
		return emitFile(path, contents, "UTF-8");
	}
	public static boolean emitFile(String path, String contents, String encoding){
		byte[] cont = new byte[0];
		boolean out_bool = false;
		try {
			cont = contents.getBytes(encoding);
			out_bool = emitFile(path, cont);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return out_bool;
		
	}
	public static boolean makePath(String path){
		boolean out_bool = false;
		File f = new File(path);
		if(f.exists() == false)
			out_bool = f.mkdirs();
		else out_bool = true;
		
		return out_bool;
	}
	public static boolean emitFile(String path, byte[] contents){
		boolean out_bool = false;
		File f = new File(path);
		File p = f.getParentFile();
		if(p.exists() == false)
			p.mkdirs();
		if(f.exists() == true)
			f.delete();
		try{
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(contents);
			fos.flush();
			fos.close();
			out_bool = true;
		}
		catch(IOException ie){
			logger.error(ie.getMessage());
			ie.printStackTrace();
		}
		return out_bool;
		
	}
}
