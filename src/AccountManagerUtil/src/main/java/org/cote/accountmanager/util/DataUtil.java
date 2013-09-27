package org.cote.accountmanager.util;

import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.types.CompressionEnumType;

public class DataUtil {
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
				System.out.println("KEY LENGTH: " + bean.getCipherKey().length + "::" + bean.getCipherIV().length);
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
			byte[] passKey = SecurityUtil.getPassphraseBytes(password);
			SecurityFactory.getSecurityFactory().setPassKey(bean, passKey, false);
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
			
			setValue(d, value.getBytes());
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
			return (new String(getValue(d)));
		}
		else
		{
			return d.getShortData();
		}
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
			if (d.getPassKey().length == 0)
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
			}
		}

		if (d.getEncipher() == true)
		{
			if(d.getCipherKey().length == 0){
				throw new DataException("Cipher key not specified for enciphered data");
			}
			else{
				SecurityBean bean = SecurityFactory.getSecurityFactory().createSecurityBean(d.getCipherKey(), false);
				value = SecurityUtil.encipher(bean, value);
				d.setEnciphered(true);
			}
		}

		d.setSize(value.length);
		/// this.size = data_bytes.Length;
		//d.Size = value.Length;
		d.setDataBytesStore(value);
		d.setBlob(true);
		///d.blob_data = new System.IO.MemoryStream(value);
	}

	public static byte[] getValue(DataType d) throws DataException
	{
		if (!d.getBlob())
		{
			byte[] ret = new byte[0];
			if (d.getShortData() == null)
			{
				if (d.getPointer())
				{
					throw new DataException("IO Read not implemented");
					///ret = Core.Util.IO.FileUtil.GetFileBytes(d.short_data);
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
					if(d.getCipherKey().length == 0){
						throw new DataException("Cipher key was not specified for enciphered data.");
					}
					SecurityBean bean = SecurityFactory.getSecurityFactory().createSecurityBean(d.getCipherKey(), false);
					ret = SecurityUtil.decipher(bean, ret);
					d.setEnciphered(false);
				}

				if (d.getPasswordProtected())
				{
					if(d.getPassKey().length == 0){
						throw new DataException("Pass key was not specified for password-protected data.");
					}
					SecurityBean bean = SecurityFactory.getSecurityFactory().createSecurityBean(d.getPassKey(), false);
					ret = SecurityUtil.decipher(bean, ret);
					d.setPasswordProtected(false);
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
				throw new DataException("Pointer derference not implemented");
				//return Core.Util.IO.FileUtil.GetFileBytes(Core.Util.Lang.LangUtil.ByteArrayToString(d.data_bytes_store));
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
