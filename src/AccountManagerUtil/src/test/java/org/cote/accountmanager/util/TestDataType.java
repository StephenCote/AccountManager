/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.StringWriter;
import java.security.Security;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.types.CompressionEnumType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestDataType {
	public static final Logger logger = LogManager.getLogger(TestDataType.class);
	@Before
	public void setUp() throws Exception {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
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
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			error= true;
       	}
       	assertFalse("There as an error", error);
	}


}
