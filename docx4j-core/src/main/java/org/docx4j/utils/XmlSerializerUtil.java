package org.docx4j.utils;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlSerializerUtil {

	private static Logger log = LoggerFactory.getLogger(XmlSerializerUtil.class);	
	
	public static void serialize(Source source, Result result,
			boolean omit_xml_declaration, boolean method_xml) throws Docx4JException {
		
		serialize( source, result, omit_xml_declaration,  method_xml, false);
	}

	/**
	 * @param source
	 * @param result
	 * @param omit_xml_declaration
	 * @param method_xml
	 * @param indent
	 * @throws Docx4JException
	 * 
	 * @since 3.3.1
	 */
	public static void serialize(Source source, Result result,
			boolean omit_xml_declaration, boolean method_xml, boolean indent) throws Docx4JException {
		
		// Xalan <= 2.7.2 can't handle astral characters: https://issues.apache.org/jira/browse/XALANJ-2419
		// but org.docx4j.org.apache.xalan repackaging is patched to fix this
		
		try {
			Transformer serializer = new org.docx4j.org.apache.xalan.transformer.TransformerIdentityImpl();
			
			if (omit_xml_declaration) {
				serializer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
			}
			if (method_xml) {
				serializer.setOutputProperty(javax.xml.transform.OutputKeys.METHOD, "xml");	// probably the default anyway?			
			}
			if (indent) {
				serializer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
				// actual indent amount is set in org/docx4j/org/apache/xml/serializer/docx4j_xalan_output_xml.properties 
			}
						
			serializer.transform( source, result );	
			
		} catch (Exception e) {
			
			log.error(e.getMessage(),e);
			throw new Docx4JException("Exception writing Document to OutputStream: " + e.getMessage(), e);
		}
	}
	
}
