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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZipUtil {
	public static final Logger logger = LogManager.getLogger(ZipUtil.class);
	public static byte[] gzipStream(InputStream in){
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try{
			GZIPOutputStream gzout = new GZIPOutputStream(baos);
			StreamUtil.copyStream(in, gzout);
			gzout.close();
		}
		catch(IOException e){
			logger.error(e);
		}
		return baos.toByteArray();
	}
	
	public static byte[] gzipBytes(byte[] inBytes){
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try{
//			ByteArrayInputStream bais=new ByteArrayInputStream(inBytes);
			GZIPOutputStream gzout = new GZIPOutputStream(baos);
			gzout.write(inBytes,0,inBytes.length);
			gzout.close();

		}
		catch(IOException e){
			logger.error("ZipUtil:: gzipBytes: " + e);
		}
		return baos.toByteArray();

	}
	public static byte[] gunzipBytes(byte[] inBytes){
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try{
			ByteArrayInputStream bais=new ByteArrayInputStream(inBytes);
			GZIPInputStream gzin = new GZIPInputStream(bais);
			StreamUtil.copyStream(gzin, baos);
			gzin.close();
			bais.close();
		}
		catch(IOException e){
			logger.error("ZipUtil:: gunzipBytes: " + e);
		}
		return baos.toByteArray();

	}
}
