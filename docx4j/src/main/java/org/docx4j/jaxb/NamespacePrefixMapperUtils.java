package org.docx4j.jaxb;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

public class NamespacePrefixMapperUtils {
	
	private static Logger log = Logger.getLogger(NamespacePrefixMapperUtils.class);		
	
	/*
	 * As from 2010 08 26,  
	 * both com.sun.xml.bind.marshaller.NamespacePrefixMapper
	 * and  com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper
	 * are provided in the jar JAXB-NamespacePrefixMapperInterfaces.jar
	 * so that people can build docx4j without needing both JAXB
	 * implementations.
	 * 
	 * But if that jar is on their classpath, testing for either
	 * of these classes will always succeed.
	 * 
	 * So, we have to test for something else.  The following will do:
	 * 
	 * com.sun.xml.bind.marshaller.MinimumEscapeHandler
	 * com.sun.xml.internal.bind.marshaller.MinimumEscapeHandler
	 */
	
	
	public static Object getPrefixMapper() throws JAXBException {
		
    	Class c;
    	try {
    		c = Class.forName("com.sun.xml.bind.marshaller.MinimumEscapeHandler");
    		return new NamespacePrefixMapper();  // JAXB Reference Implementation 
    	} catch (ClassNotFoundException cnfe) {
    		// JAXB Reference Implementation not present
    		// Use Java 6 implementation
    		log.debug("JAXB RI (com.sun.xml.bind.marshaller.MinimumEscapeHandler) not present.  Trying Java 6 implementation.");
        	try {
				c = Class.forName("com.sun.xml.internal.bind.marshaller.MinimumEscapeHandler");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				log.error("JAXB: neither Reference Implementation nor Java 6 implementation present?", e);
				throw new JAXBException("JAXB: neither Reference Implementation nor Java 6 implementation present?");
			}
        	return new NamespacePrefixMapperSunInternal();
    	}
	}

	
	public static Object getPrefixMapperRelationshipsPart() throws JAXBException {
		
    	Class c;
    	try {
    		c = Class.forName("com.sun.xml.bind.marshaller.MinimumEscapeHandler");
    		return new NamespacePrefixMapperRelationshipsPart();
    	} catch (ClassNotFoundException cnfe) {
    		// JAXB Reference Implementation not present
    		log.debug("JAXB RI (com.sun.xml.bind.marshaller.MinimumEscapeHandler) not present.  Trying Java 6 implementation.");
        	try {
				c = Class.forName("com.sun.xml.internal.bind.marshaller.MinimumEscapeHandler");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				log.error("JAXB: neither Reference Implementation nor Java 6 implementation present?", e);
				throw new JAXBException("JAXB: neither Reference Implementation nor Java 6 implementation present?");
			}
        	return new NamespacePrefixMapperRelationshipsPartSunInternal();
    	}
	}
	
	/**
	 * setProperty on 'com.sun.xml.bind.namespacePrefixMapper' or
	 * 'com.sun.xml.internal.bind.namespacePrefixMapper', as appropriate,
	 * depending on whether JAXB reference implementation, or Java 6 
	 * implementation is being used.
	 * 
	 * @param marshaller
	 * @param namespacePrefixMapper
	 * @throws JAXBException
	 */
	public static void setProperty(Marshaller marshaller, Object namespacePrefixMapper) throws JAXBException {
		
		try {
			if ( namespacePrefixMapper.getClass().getName().equals(
						"org.docx4j.jaxb.NamespacePrefixMapper")
					|| namespacePrefixMapper.getClass().getName().equals(
							"org.docx4j.jaxb.NamespacePrefixMapperRelationshipsPart") ) {
			
				marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", 
						namespacePrefixMapper ); 
			
				// Reference implementation appears to be present (in endorsed dir?)
				log.debug("setProperty: com.sun.xml.bind.namespacePrefixMapper");
//				System.out.println("setProperty: com.sun.xml.bind.namespacePrefixMapper");
				
			} else {
				
				// Use JAXB distributed in Java 6 - note 'internal' 
				// Switch to other mapper
				log.debug("attempting to setProperty: com.sun.xml.INTERNAL.bind.namespacePrefixMapper");
				marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", namespacePrefixMapper);
//				System.out.println("setProperty: com.sun.xml.INTERNAL.bind.namespacePrefixMapper");
			}
			
		} catch (javax.xml.bind.PropertyException cnfe) {
			
//			cnfe.printStackTrace();
			log.error(cnfe);
			throw cnfe;
			
		}
		
	}
	
	public static String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) throws JAXBException {

		Object namespacePrefixMapper = getPrefixMapper();
		
		if ( namespacePrefixMapper instanceof NamespacePrefixMapperSunInternal) {
			// Java 6
			return ((NamespacePrefixMapperSunInternal)namespacePrefixMapper).getPreferredPrefix(namespaceUri, suggestion, requirePrefix); 
			
		} else if (namespacePrefixMapper instanceof NamespacePrefixMapper) {
    		// JAXB Reference Implementation		
			return ((NamespacePrefixMapper)namespacePrefixMapper).getPreferredPrefix(namespaceUri, suggestion, requirePrefix); 
			
		} else {
			log.warn("Namespace prefix mapper not found!");
			return null;
		}
		
	}
	
}
