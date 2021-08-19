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


/// <summary>
///  (Class Adapted from http://www.whitefrost.com/documents/html/api/accountManager4/class_core_1_1_util_1_1_xml_1_1_x_m_l_document.html )
///  The original Java and .NET implementations used DiffieHellman and Triple-DES, and later RSA and AES.
///  Due to cross-platform interface references, the property names were never updated from 'DES'.
/// </summary>

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.exceptions.FactoryException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlUtil {
	public static final Logger logger = LogManager.getLogger(XmlUtil.class);
	private static TransformerFactory transFactory = null;
	private XmlUtil() {
		
	}
	private static TransformerFactory getTransformerFactory(){
		if(transFactory == null){
			transFactory = TransformerFactory.newInstance();
		}
		return transFactory;
		
	}

	public static Transformer loadTransformer(byte[] data){
		
		Transformer transformer = null;
		try{
			ByteArrayInputStream bais=new ByteArrayInputStream(data);
			InputStreamReader isr=new InputStreamReader(bais);
			StreamSource source=new StreamSource(isr);
			transformer=getTransformerFactory().newTransformer(source);
			transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION,"yes");
			bais.close();
			isr.close();
		}
		catch(TransformerConfigurationException tce){
			logger.error(tce.getMessage());
			logger.error(FactoryException.TRACE_EXCEPTION,tce);
	
		}
		catch(IOException e){
			logger.error(e.getMessage());
			logger.error(FactoryException.TRACE_EXCEPTION,e);
		}
		return transformer;
	}
	public static Transformer loadTransformer(String templateFile){
		return loadTransformer(FileUtil.getFile(templateFile));
	}
	public static byte[] transform(Transformer transformer, Document useDocument){
		byte[] returnData=new byte[0];
		try{
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			DOMSource domSource=new DOMSource(useDocument.getDocumentElement());
			transformer.transform(domSource,new StreamResult(baos));

			returnData=baos.toByteArray();
		
			baos.close();
		}
		catch(TransformerException | IOException e){
			logger.error(e.getMessage());
			returnData=e.toString().getBytes();
			logger.error(FactoryException.TRACE_EXCEPTION,e);
		}
		return returnData;
	}
	
	public static Node selectSingleNode(Node refNode, String expression){
		XPath xPath =  XPathFactory.newInstance().newXPath();
		Node outNode = null;
		try {
			outNode = (Node)xPath.compile(expression).evaluate(refNode, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			logger.error(e.getMessage());
			logger.error(FactoryException.TRACE_EXCEPTION,e);
		}
		return outNode;
	}	
	public static Node selectSingleNode(Document refNode, String expression){
		XPath xPath =  XPathFactory.newInstance().newXPath();
		Node outNode = null;
		try {
			outNode = (Node)xPath.compile(expression).evaluate(refNode, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			logger.error(e.getMessage());
			logger.error(FactoryException.TRACE_EXCEPTION,e);
		}
		return outNode;
	}

	public static NodeList selectNodes(Node refNode, String expression){
		XPath xPath =  XPathFactory.newInstance().newXPath();
		NodeList outNodes = null;
		try {
			outNodes = (NodeList)xPath.compile(expression).evaluate(refNode, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			logger.error(e.getMessage());
			logger.error(FactoryException.TRACE_EXCEPTION,e);
		}
		return outNodes;
	}	
	public static NodeList selectNodes(Document refNode, String expression){
		XPath xPath =  XPathFactory.newInstance().newXPath();
		NodeList outNodes = null;
		try {
			outNodes = (NodeList)xPath.compile(expression).evaluate(refNode, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			logger.error(e.getMessage());
			logger.error(FactoryException.TRACE_EXCEPTION,e);
		}
		return outNodes;
	}
	
	/// TODO: Cleanup all these legacy cross-conversation names
	///

	public static String getStringFromDoc(Document d)    {
	    StringWriter output = new StringWriter();

	    
		try {
			Transformer transformer = getTransformerFactory().newTransformer();

			transformer.transform(new DOMSource(d.getDocumentElement()), new StreamResult(output));
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			logger.error(e.getMessage());
			logger.error(FactoryException.TRACE_EXCEPTION,e);
		}
	    

	    return output.toString();
	}
	
	public static Document getDocumentFromBytes(byte[] data){

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		Document doc = null;
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		try {
			dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
			db = dbf.newDocumentBuilder();

			doc = db.parse(bais);
		}
		catch (ParserConfigurationException | SAXException | IOException e) {
			logger.error(e.getMessage());
			logger.error(FactoryException.TRACE_EXCEPTION,e);
		}
		finally{
			try {
				bais.close();
			} catch (IOException e) {
				
				logger.error(FactoryException.TRACE_EXCEPTION,e);
			}
		}
		return doc;
	}
	public static Element FindElement(Element parent_element, String element_name){
		return FindElement(parent_element, element_name, null);
	}
	public static Element FindElement(Element parent_element, String element_name, String child_element_name){
		NodeList match= parent_element.getElementsByTagName(element_name);
		if(match.getLength() == 0)
			return null;
		Element m = (Element)match.item(0);
		if(child_element_name != null)
			m = FindElement(m, child_element_name, null);
		return m;
	}
	public static String FindElementText(Element parent_element, String element_name, String node_name){
		Element match = FindElement(parent_element, element_name);
		if(match == null)
			return null;
		return GetElementText(match, node_name);
	}
	public static String GetElementText(Element parent, String node_name)
	{
		NodeList match = parent.getElementsByTagName(node_name);
		if (match.getLength() == 0)
			return null;
		Node m = match.item(0);
		return getNodeText(m);
	}
	public static String getNodeText(Node node){
		if(node == null)
			return null;
		StringBuilder buff = new StringBuilder();
		for(int c = 0; c < node.getChildNodes().getLength();c++){
			Node cn = node.getChildNodes().item(c);
			if(cn.getNodeType() == Node.TEXT_NODE || cn.getNodeType() == Node.CDATA_SECTION_NODE){
				buff.append(cn.getNodeValue());
			}
		}
		return buff.toString().trim();

	}
}