package org.docx4j.utils;


import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.ws.commons.serialize.DOMSerializer;
import org.docx4j.convert.out.common.preprocess.ParagraphStylesInTableFix;
import org.docx4j.jaxb.JAXBAssociation;
import org.docx4j.jaxb.XPathBinderAssociationIsPartialException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.JaxbXmlPartXPathAware;
import org.docx4j.openpackaging.parts.PresentationML.JaxbPmlPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.samples.SampleDocument;
import org.docx4j.utils.sax.SAXHandlerToCodeString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/**
 * Converts a part to a String, suitable for use in Java code.
 * 
 * @author jharrop
 *
 */
public class XmlPartToString {
	
	protected static Logger log = LoggerFactory.getLogger(ParagraphStylesInTableFix.class);	
	
	private XmlPartToString() {}

	public static String get(JaxbXmlPartXPathAware jaxbXmlPart) throws XPathBinderAssociationIsPartialException, JAXBException {
	
		List<JAXBAssociation> results = ((JaxbXmlPartXPathAware)jaxbXmlPart).getJAXBAssociationsForXPath("/", false);
		
		return (new XmlPartToString()).nodeToString(results.get(0));
		
	}
	

	private String nodeToString(JAXBAssociation jaxbAssociation //, JaxbXmlPart jaxbXmlPart
			)  {
	
		Node n = jaxbAssociation.getDomNode();
		
		StringWriter out = new StringWriter();		
		SAXHandlerToCodeString saxHandlerToCodeString = new SAXHandlerToCodeString(out);

		DOMSerializer domSerializer = new DOMSerializer();
		try {
			domSerializer.serialize(n, saxHandlerToCodeString);
		} catch (SAXException e) {
			return "Code generation not available for this object";
		}
		
		String result=  out.toString();	
		result = result.substring(0, result.length()-1) +";"; 
		
		// Now add the namespace declarations to the string
		String nsStuff = saxHandlerToCodeString.getNamespaceDecs();		
		String completeXml = insertNamespaceDecs(result, nsStuff );	
		
		return completeXml;
	}
	

	private String insertNamespaceDecs(String xml, String namespaceDecs) {
		int pos = xml.indexOf(">");
		return xml.substring(0, pos) + namespaceDecs + xml.substring(pos); 
	}	
	

	public static void main(String[] args) 
	        throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/todd_p_spacing_direct.docx";
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		JaxbXmlPartXPathAware jaxbXmlPart = (JaxbXmlPartXPathAware)wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart(); 
		
		System.out.println(XmlPartToString.get(jaxbXmlPart));

		}
	
}
