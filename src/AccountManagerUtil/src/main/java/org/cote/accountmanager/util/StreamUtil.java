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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StreamUtil {
	public static final Logger logger = LogManager.getLogger(StreamUtil.class);
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
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		synchronized(in){
			int mark = 8192;
			byte[] read_buffer = new byte[mark];
			int bytesRead=0;

			while (bytesRead != -1) {
				bytesRead = in.read(read_buffer);
				if (bytesRead == -1) break;
				baos.write(read_buffer,0,bytesRead);
			}
		}
		return baos.toByteArray();

	}

	
	public static String streamToString(BufferedInputStream in) throws IOException{
		int offset=2048;
		int bytesRead=0;
		StringBuilder sb=new StringBuilder();

		int max=in.available();
		boolean breakOut=false;

		while(bytesRead != -1) {
			byte[] buffer = new byte[offset];
			bytesRead = in.read(buffer,0,offset);
			if(bytesRead == -1) break;
			if(bytesRead > 0 && buffer[bytesRead -1] == -1){
				bytesRead--;
				breakOut=true;
			}
			String sStr=new String(buffer, 0, bytesRead);
			sb.append(sStr);

			if(bytesRead >= max && max > 0) break;
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
			logger.error("StreamUtil:: fileHandleToBytes: " + e.toString());
		}
		return baos.toByteArray();
	}
	public static byte[] fileToBytes(String fileName){
		File f = new File(fileName);
		
		return fileHandleToBytes(f);
		
	}
}
