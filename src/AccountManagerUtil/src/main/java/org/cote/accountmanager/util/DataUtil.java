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

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.types.CompressionEnumType;

public class DataUtil {
	public static final Logger logger = Logger.getLogger(DataUtil.class.getName());
	public static void clearCipher(DataType data){
		data.setEncipher(false);
		data.setCipherKey(null);
	}
	public static void setCipher(DataType data){
		SecurityBean bean = new SecurityBean();
		SecurityFactory.getSecurityFactory().generateSecretKey(bean);
		setCipher(data, bean);
		
	}
	public static void setCipher(DataType data, SecurityBean bean){
		if(bean == null){
			clearCipher(data);
		}
		else{
			SecurityBean useBean =  bean;
			if(bean.getEncryptCipherKey()){
				useBean = new SecurityBean();
				SecurityFactory.getSecurityFactory().setSecretKey(useBean, bean.getCipherKey(), bean.getCipherIV(), false);
			}
			data.setCipherKey(SecurityUtil.serializeToXml(useBean, false, false, true).getBytes());
			data.setEncipher(true);
		}
	}
	public static void clearPassword(DataType data){
		data.setPasswordProtect(false);
		data.setPassKey(null);
	}
	public static void setPassword(DataType data, String password){
		if(password == null){
			clearPassword(data);
		}
		else{
			SecurityBean bean = new SecurityBean();
			SecurityFactory.getSecurityFactory().setPassKey(bean, password, false);
			updatePassword(data, bean);
		}
	}
	private static void updatePassword(DataType data, SecurityBean bean){
		if(bean == null){
			clearPassword(data);
		}
		else{
			data.setPassKey(SecurityUtil.serializeToXml(bean, false, false, true).getBytes());
			data.setPasswordProtect(true);
		}
	}
	public static void setValueString(DataType d, String value) throws DataException
	{
		if (value != null && value.length() > 255)
		{
			
			try {
				setValue(d, value.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.error(e.getMessage());
				logger.error("Trace",e);
			}
			d.setShortData(null);
			if(d.getMimeType() == null || d.getMimeType().length() == 0){
				d.setMimeType("text/plain");
			}
		}
		else
		{
			d.setShortData(value);
		}
	}
	
	public static String getValueString(DataType d) throws DataException
	{
		if (d.getBlob())
		{
			try {
				return (new String(getValue(d),"UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.error(e.getMessage());
				logger.error("Trace",e);
			}
		}
		return d.getShortData();
		
	}

	public static void setValue(DataType d, byte[] value) throws DataException
	{
		d.setDataBytesStore(new byte[0]);
		d.setReadDataBytes(false);
		d.setShortData(null);
		if(d.getVaulted() == false){
			d.setDataHash(SecurityUtil.getDigestAsString(value));
			// don't override compression setting for vaulted data
			//
			d.setCompressed(false);
		}
		d.setCompressionType(CompressionEnumType.NONE);
		d.setBlob(true);

		if (d.getVaulted() == false && value.length > 512 && tryCompress(d))
		{
			value = ZipUtil.gzipBytes(value);
			d.setCompressed(true);
			d.setCompressionType(CompressionEnumType.GZIP);
		}
		if (d.getPasswordProtect())
		{
			if (d.getPassKey() == null || d.getPassKey().length == 0)
			{
				// If there is no cipher key, then that is an error
				// because the implementor specified to encipher it, and therefore
				// the data is thrown out as it can't be enciphered.
				// This is an error and up to the implementor to catch.
				// The error will show up as the data not being added.
				//
				throw new DataException("Pass key not specified for password protected data.");
			}
			else
			{
				SecurityBean bean = SecurityFactory.getSecurityFactory().createSecurityBean(d.getPassKey(), false);
				value = SecurityUtil.encipher(bean, value);
				d.setPasswordProtected(true);
				/// Zero out the pass key - it's a risk to keep it after this
				d.setPassKey(null);
			}
		}

		if (d.getEncipher() == true)
		{

			if(d.getCipherKey() == null || d.getCipherKey().length == 0){
				throw new DataException("Cipher key not specified for enciphered data");
			}
			else{
				
				SecurityBean bean = SecurityFactory.getSecurityFactory().createSecurityBean(d.getCipherKey(), false);
				value = SecurityUtil.encipher(bean, value);
				d.setEnciphered(true);
				/// Zero out the cipher key - it's a risk to keep it after this
				d.setCipherKey(null);
				
			}
		}

		d.setSize(value.length);
		d.setDataBytesStore(value);
		d.setBlob(true);
	}

	public static byte[] getValue(DataType d) throws DataException
	{
		if(d.getDetailsOnly() == true){
			throw new DataException("Cannot access data with a meta data object.");
		}
		if (!d.getBlob())
		{
			byte[] ret = new byte[0];
			if (d.getShortData() != null)
			{
				if (d.getPointer())
				{
					ret = FileUtil.getFile(d.getShortData());
				}
				else
				{
					ret = d.getShortData().getBytes();
				}
			}
			return ret;
		}
		else if (d.getReadDataBytes() == false && d.getDataBytesStore().length > 0)
		{
			try
			{
				byte[] ret = d.getDataBytesStore();
				if (d.getEnciphered())
				{
					if(d.getCipherKey() == null || d.getCipherKey().length == 0){
						throw new DataException("Cipher key was not specified for enciphered data.");
					}
					SecurityBean bean = SecurityFactory.getSecurityFactory().createSecurityBean(d.getCipherKey(), false);
					ret = SecurityUtil.decipher(bean, ret);
					d.setEnciphered(false);
					/// Zero out the cipher key
					d.setCipherKey(null);
				}

				if (d.getPasswordProtected())
				{
					if(d.getPassKey() == null || d.getPassKey().length == 0){
						throw new DataException("Pass key was not specified for password-protected data.");
					}
					SecurityBean bean = SecurityFactory.getSecurityFactory().createSecurityBean(d.getPassKey(), false);
					ret = SecurityUtil.decipher(bean, ret);
					d.setPasswordProtected(false);
					/// Zero out the pass key - it's a risk to keep it after this
					d.setPassKey(null);
				}
				if (d.getVaulted() == false && d.getCompressed() && ret.length > 0)
				{
					ret = ZipUtil.gunzipBytes(ret);
				}
				d.setDataBytesStore(ret);
				d.setReadDataBytes(true);
				ret = new byte[0];
			}
			catch (Exception e)
			{
				throw new DataException(e);
			}
		}
		if (d.getReadDataBytes())
		{
			if (d.getPointer() && d.getVaulted() == false)
			{
				return FileUtil.getFile(new String(d.getDataBytesStore()));
			}
			return d.getDataBytesStore();
		}

		return new byte[0];
	}

	public static boolean tryCompress(DataType d)
	{
		String mimeType = d.getMimeType();
		if (
			mimeType == null
			||
			(
				(
					mimeType.startsWith("image/") == false
					||
					mimeType.equals("image/svg+xml") == true)
				&&
				mimeType.startsWith("application/") == false
				&&
				mimeType.startsWith("audio/") == false
			)
		)
		{
			return true;
		}
		return false;

	}

}
