/**
 * 
 */
package org.docx4j.jaxb;

import javax.xml.bind.JAXBContext;

/**
 * @author jharrop
 *
 */
public class WmlSchema {

	public static javax.xml.validation.Schema schema;
	
	static {
		
		try {		
			javax.xml.validation.SchemaFactory sf = 
				javax.xml.validation.SchemaFactory.newInstance(
				      javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);

			javax.xml.validation.Schema schema = sf.newSchema(new java.io.File("/home/jharrop/workspace200711/docx4j-001/src/main/resources/wml-local-subset.xsd"));			
		} catch (Exception ex) {
			ex.printStackTrace();
		}				
	}
	
	
	
}
