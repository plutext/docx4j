package org.docx4j.samples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.text.StrTokenizer;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.XmlPart.XmlNamespaceContext;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XPathNavigation {

	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 

//	private static XPathFactory xPathFactory;
//	private static XPath xPath;
//		
//	static {
//		
//		// Crimson doesn't support setTextContent; this.writeDocument also fails.
//		// We've already worked around the problem with setTextContent,
//		// but rather than do the same for writeDocument,
//		// let's just stop using it.
//		System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
//			"com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
//		
//		xPathFactory = XPathFactory.newInstance();
//		xPath = xPathFactory.newXPath();				
//	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		//String inputfilepath = System.getProperty("user.dir") + "/sample-docs/Images.docx";
		String inputfilepath = System.getProperty("user.dir") + "//tmp//modelo.docx";
//    	String inputfilepath = "/home/dev/workspace/docx4j/sample-docs/fo-200912.xml";
				
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

		// Setup the binder
		//Binder<Node> binder = documentPart.getBinder();

//		javax.xml.parsers.DocumentBuilderFactory dbf 
//		= DocumentBuilderFactory.newInstance();
//	dbf.setNamespaceAware(true);
//	org.w3c.dom.Document doc = dbf.newDocumentBuilder().newDocument();
//	binder.marshal(documentPart.getJaxbElement(), doc);
//	System.out.println(XmlUtils.w3CDomNodeToString(doc) );
	
		System.out.println( "Binder established" );
		
		/* updateXML is not enough to establish the initial association.
		 * 
		 * There are 2 ways to establish it.
		 * 
		 * Something like the following should work, but didn't quite
		 * 
			javax.xml.parsers.DocumentBuilderFactory dbf 
				= DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			org.w3c.dom.Document doc = dbf.newDocumentBuilder().newDocument();
			binder.marshal(documentPart.getJaxbElement(), doc);
		 *
		 * this does populate doc, but doesn't seem to establish the association??
		 * 
			
		 * The way which does work is the other way around ie 
		 * unmarshall from the XML document, so that's what we do.
		 * 
		 * You can do:
		 * 
				Node node = binder.updateXML(documentPart.getJaxbElement());
				node = binder.getXMLNode(documentPart.getJaxbElement());

		 * OR
		 * 
				Node node = binder.getXMLNode(documentPart.getJaxbElement());

		 * BUT NOT

				Node node = binder.updateXML(documentPart.getJaxbElement());

		 * 
		 */
				
		List<Object> list = documentPart.getJAXBNodesViaXPath("//w:p", true);
		
		System.out.println("got " + list.size() );
				
		/*
		 * Note https://jaxb.dev.java.net/issues/show_bug.cgi?id=459
		 * (Binder.updateXML() fails if called twice)
		 * 
		 * So we can't use XPath after modifying the document
		 * via JAXB, unless we get the marshall stuff working.
		 * 
		 */
		
	}
	
		
}
