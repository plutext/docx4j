package org.docx4j.jaxb;

import org.apache.log4j.Logger;

public class NamespacePrefixMapperUtils {
	
	private static Logger log = Logger.getLogger(NamespacePrefixMapperUtils.class);				
	
	
	public static Object getPrefixMapper() throws ClassNotFoundException {
		
    	Class c;
    	try {
    		c = Class.forName("com.sun.xml.bind.marshaller.NamespacePrefixMapper");
    		return new NamespacePrefixMapper();
    	} catch (ClassNotFoundException cnfe) {
    		// JAXB Reference Implementation not present
    		log.info("JAXB RI (com.sun.xml.bind.marshaller.NamespacePrefixMapper) not present.  Trying Java 6 implementation.");
        	c = Class.forName("com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper");
        	// will throw ClassNotFoundException if not present,
        	// so if we get here ..
        	return new NamespacePrefixMapperSunInternal();
    	}
	}

	
	public static Object getPrefixMapperRelationshipsPart() throws ClassNotFoundException {
		
    	Class c;
    	try {
    		c = Class.forName("com.sun.xml.bind.marshaller.NamespacePrefixMapper");
    		return new NamespacePrefixMapperRelationshipsPart();
    	} catch (ClassNotFoundException cnfe) {
    		// JAXB Reference Implementation not present
    		log.info("JAXB RI (com.sun.xml.bind.marshaller.NamespacePrefixMapper) not present.  Trying Java 6 implementation.");
        	c = Class.forName("com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper");
        	// will throw ClassNotFoundException if not present,
        	// so if we get here ..
        	return new NamespacePrefixMapperRelationshipsPartSunInternal();
    	}
	}
	
}
