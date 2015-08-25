package org.docx4j.utils;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;

import org.docx4j.openpackaging.exceptions.Docx4JException;

public class XmlSerializerUtil {
	
	public static void serialize(Source source, Result result,
			boolean omit_xml_declaration, boolean method_xml) throws Docx4JException {
		
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
			//serializer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
			
			serializer.transform( source, result );	
			
		} catch (Exception e) {
			throw new Docx4JException("Exception writing Document to OutputStream: " + e.getMessage(), e);
		}
	}

}
