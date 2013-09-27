package org.cote.accountmanager.util;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.types.CompressionEnumType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;

public class TestDataType {
	public static final Logger logger = Logger.getLogger(TestDataType.class.getName());
	@Before
	public void setUp() throws Exception {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		String log4jPropertiesPath = System.getProperty("log4j.configuration");
		if(log4jPropertiesPath != null){
			System.out.println("Properties=" + log4jPropertiesPath);
			PropertyConfigurator.configure(log4jPropertiesPath);
		}
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDataType(){
		logger.info("Testing default DataType values");
		DataType data = new DataType();
		assertNotNull("Blob boolean is null", data.getBlob());
		logger.info(data.getCompressionType());
	}
	
	@Test
	public void testSerialization(){
       	DataType data = new DataType();
       	data.setName("Example");
       	boolean error = false;
		try{
	        JAXBContext contextA = JAXBContext.newInstance(DataType.class);
	        JAXBElement<DataType> jaxbElementA = new JAXBElement(new QName("DataType"), DataType.class, data);
	        JAXBSource sourceA = new JAXBSource(contextA, jaxbElementA);
	
	        JAXBContext contextB = JAXBContext.newInstance(DataType.class);
	        Unmarshaller unmarshallerB = contextB.createUnmarshaller();
	        JAXBElement<DataType> jaxbElementB = unmarshallerB.unmarshal(sourceA, DataType.class);
	
	        DataType data2 = jaxbElementB.getValue();
		
		}
		catch(Exception e){
			logger.error(e.getMessage());
			error= true;
		}
		assertFalse("There was an error", error);

	}

	@Test
	public void testSerialWriter(){
       	DataBean data = new DataBean();
       	data.setName("Example");
       	data.setCompressionType(CompressionEnumType.NONE);
       	boolean error = false;
       	try{
	       	StringWriter writer = new StringWriter();
	       	JAXBContext context = JAXBContext.newInstance(DataBean.class);
	       	Marshaller m = context.createMarshaller();
	       	m.marshal(data, writer);
	       	logger.info(writer.toString());
       	}
       	catch(Exception e){
			logger.error(e.getMessage());
			logger.error(e.getStackTrace());
			error= true;
       	}
       	assertFalse("There as an error", error);
	}


}
