/*
 *  Copyright 2009, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */
package org.docx4j.model.datastorage;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class CustomXmlDataStorageImpl implements CustomXmlDataStorage {

	private static Logger log = Logger.getLogger(CustomXmlDataStorageImpl.class);	
	
	private Document doc;
	private static XPathFactory xPathFactory;
	private static XPath xPath;
	
	private static DocumentBuilderFactory documentFactory;
	private static DocumentBuilder documentBuilder;
	
	static {		
		xPathFactory = XPathFactory.newInstance();
		xPath = xPathFactory.newXPath();
		
		documentFactory = DocumentBuilderFactory.newInstance();
		documentFactory.setNamespaceAware(true);
		try {
			documentBuilder = documentFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} 
	}
	
	private XmlNamespaceContext nsContext;
	private XmlNamespaceContext getNamespaceContext() {
		if (nsContext==null) {
			nsContext = new XmlNamespaceContext();
			xPath.setNamespaceContext(nsContext);
		}
		return nsContext;
	}

	/* (non-Javadoc)
	 * @see org.docx4j.model.datastorage.CustomXmlDataStorage#factory()
	 */
	public CustomXmlDataStorage factory() {
		return new CustomXmlDataStorageImpl();
	}
	
	/* (non-Javadoc)
	 * @see org.docx4j.model.datastorage.CustomXmlDataStorage#getXPath(java.lang.String)
	 */
	public String getXPath(String xpathString, String prefixMappings)  throws Docx4JException {
		try {
			getNamespaceContext().registerPrefixMappings(prefixMappings);
			
			String result = xPath.evaluate(xpathString, doc );
			log.debug(xpathString + " ---> " + result);
			return result;
		} catch (Exception e) {
			throw new Docx4JException("Problems evaluating xpath '" + xpathString + "'", e);
		}
	}
	
	/**
	 * Set the value of the node referenced in the xpath expression.
	 * 
	 * @param xpath
	 * @param value
	 * @param prefixMappings a string such as "xmlns:ns0='http://schemas.medchart'"
	 * @return
	 * @throws Docx4JException
	 */
	public boolean setNodeValueAtXPath(String xpath, String value, String prefixMappings) throws Docx4JException {

		try {
			getNamespaceContext().registerPrefixMappings(prefixMappings);

			Node n = (Node)xPath.evaluate(xpath, doc, XPathConstants.NODE );
			if (n==null) {
				log.debug("xpath returned null");
				return false;
			}
			n.setTextContent(value);
			
			// cache is now invalid
			return true;
		} catch (Exception e) {
			throw new Docx4JException("Problem setting value at xpath " + xpath);
		} 
		
	}

	/* (non-Javadoc)
	 * @see org.docx4j.model.datastorage.CustomXmlDataStorage#marshal(java.io.OutputStream)
	 */
	public void writeDocument(OutputStream os) throws Docx4JException {
		// Used when saving to zip file
		 try {
			DOMSource source = new DOMSource(doc);
			 TransformerFactory.newInstance().newTransformer().transform(source, 
					 new StreamResult(os) );
		} catch (Exception e) {
			throw new Docx4JException("Problems saving to OutputStream", e);
		} 
		
	}
	
	/* (non-Javadoc)
	 * @see org.docx4j.model.datastorage.CustomXmlDataStorage#marshal(org.w3c.dom.Document)
	 */
	public org.w3c.dom.Document getDocument() throws Docx4JException {
		// Used when saving to JCR
		return doc;
	}

	/* (non-Javadoc)
	 * @see org.docx4j.model.datastorage.CustomXmlDataStorage#unmarshal(java.io.InputStream)
	 */
	public void setDocument(InputStream is) throws Docx4JException {
		try {
			doc = documentBuilder.parse(is);
		} catch (Exception e) {
			throw new Docx4JException("Problems parsing InputStream", e);
		} 
	}	

	public void setDocument( org.w3c.dom.Document doc ) throws Docx4JException {
		this.doc = doc;
	}
	
//	public void setNamespaceContext(String prefixMappings) throws Docx4JException {		
//		xPath.setNamespaceContext(new XmlNamespaceContext(prefixMappings) );		
//	}
	
	public class XmlNamespaceContext implements NamespaceContext {

		Map<String, String> namespaces = new HashMap<String, String>();
		public XmlNamespaceContext() {
			//namespaces.put("ns0", "http://schemas.medchart");			
		}
		
		/* (non-Javadoc)
		 * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
		 */
		public String getNamespaceURI(String prefix) {
			if (prefix==null) throw new NullPointerException("Null prefix");
			String result = namespaces.get(prefix);
			if (result==null) {
				return XMLConstants.NULL_NS_URI;
			} else {
				return result;
			}
		}

		/* (non-Javadoc)
		 * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
		 */
		public String getPrefix(String uri) {
			// This method isn't necessary for xpath processing
			throw new UnsupportedOperationException();
		}

		/* (non-Javadoc)
		 * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
		 */
		public Iterator getPrefixes(String uri) {
			// This method isn't necessary for xpath processing
			throw new UnsupportedOperationException();
		}
		
		public void registerPrefixMappings(String prefixMappings) {
			// eg  w:prefixMappings="xmlns:ns0='http://schemas.medchart'"
			// according to the spec, whitespace is the delimiter
			
			// we get one of these each time we encounter a w:dataBinding
			// element in a content control; pity it is not done just
			// once!
			
			// first tokenise on space
			StrTokenizer tokens = new StrTokenizer(prefixMappings);
			while (tokens.hasNext() ) {
				String token = tokens.nextToken();
				//log.debug("Got: " + token);
				int pos = token.indexOf("=");
				String prefix = token.substring(6, pos); // drop xmlns:
				//log.debug("Got: " + prefix);
				String uri = token.substring(pos+2, token.lastIndexOf("'"));
				//log.debug("Got: " + uri);
				namespaces.put(prefix, uri);
			}
			
		}
		
	}
	
}
