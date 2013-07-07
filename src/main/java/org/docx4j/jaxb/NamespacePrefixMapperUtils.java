package org.docx4j.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamespacePrefixMapperUtils {
	
	private static Logger log = LoggerFactory.getLogger(NamespacePrefixMapperUtils.class);		
	
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
	
	private static JAXBContext testContext;
	
	private static Object prefixMapper;
	private static Object prefixMapperRels;
	
	
	public static Object getPrefixMapper() throws JAXBException {
		
		if (prefixMapper!=null) return prefixMapper;
		
		if (testContext==null) {
			
			// JBOSS might use a different class loader to load JAXBContext, which causes problems,
			// so explicitly specify our class loader.
			NamespacePrefixMapperUtils tmp = new NamespacePrefixMapperUtils();
			java.lang.ClassLoader classLoader = tmp.getClass().getClassLoader();
			
			testContext = JAXBContext.newInstance("org.docx4j.relationships",classLoader );
		}
		
		Marshaller m=testContext.createMarshaller();
		try {
			// Assume use of Java 6 implementation (ie not RI)
			m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", 
					new NamespacePrefixMapperSunInternal() );
			log.info("Using NamespacePrefixMapperSunInternal, which is suitable for Java 6");
			prefixMapper = new NamespacePrefixMapperSunInternal();
			return prefixMapper;
		} catch (java.lang.NoClassDefFoundError notJava6) {
			log.warn(notJava6.getMessage() + " .. trying RI.");
			return tryUsingRI(m);			
		} catch (javax.xml.bind.PropertyException notJava6) {
			// OpenJDK (1.6.0_23) does this
			log.warn(notJava6.getMessage() + " .. trying RI.");
			return tryUsingRI(m);			
		}
	}


	private static Object tryUsingRI(Marshaller m)
			throws JAXBException {
		try {
			// Try RI suitable one
			m.setProperty("com.sun.xml.bind.namespacePrefixMapper", 
					new NamespacePrefixMapper() );
			log.info("Using NamespacePrefixMapper, which is suitable for the JAXB RI");
			prefixMapper = new NamespacePrefixMapper();
			return prefixMapper;
		} catch (java.lang.NoClassDefFoundError notRIEither) {
			notRIEither.printStackTrace();
			log.error("JAXB: neither Reference Implementation nor Java 6 implementation present?", notRIEither);
			throw new JAXBException("JAXB: neither Reference Implementation nor Java 6 implementation present?");
		} catch (javax.xml.bind.PropertyException notRIEither) {
			notRIEither.printStackTrace();
			log.error("JAXB: neither Reference Implementation nor Java 6 implementation present?", notRIEither);
			throw new JAXBException("JAXB: neither Reference Implementation nor Java 6 implementation present?");
		}
	}

	
	public static Object getPrefixMapperRelationshipsPart() throws JAXBException {

		if (prefixMapperRels!=null) return prefixMapperRels;
		if (testContext==null) {
			
			// JBOSS might use a different class loader to load JAXBContext, which causes problems,
			// so explicitly specify our class loader.
			NamespacePrefixMapperUtils tmp = new NamespacePrefixMapperUtils();
			java.lang.ClassLoader classLoader = tmp.getClass().getClassLoader();
			
			testContext = JAXBContext.newInstance("org.docx4j.relationships",classLoader );
		}
		
		Marshaller m=testContext.createMarshaller();
		try {
			// Assume use of Java 6 implementation (ie not RI)
			m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", 
					new NamespacePrefixMapperRelationshipsPartSunInternal() );
			log.info("Using NamespacePrefixMapperSunInternal, which is suitable for Java 6");
			prefixMapperRels = new NamespacePrefixMapperRelationshipsPartSunInternal();
			return prefixMapperRels;
		} catch (java.lang.NoClassDefFoundError notJava6) {
			// javax.xml.bind.PropertyException
			log.warn(notJava6.getMessage() + " .. trying RI.");
			return tryRIforRelationshipsPart(m);
		} catch (javax.xml.bind.PropertyException notJava6) {
			log.warn(notJava6.getMessage() + " .. trying RI.");
			return tryRIforRelationshipsPart(m);
		}
	}


	private static Object tryRIforRelationshipsPart(Marshaller m)
			throws JAXBException {
		try {
			// Try RI suitable one
			m.setProperty("com.sun.xml.bind.namespacePrefixMapper", 
					new NamespacePrefixMapperRelationshipsPart() );
			log.info("Using NamespacePrefixMapperRelationshipsPart, which is suitable for the JAXB RI");
			prefixMapperRels = new NamespacePrefixMapperRelationshipsPart();
			return prefixMapperRels;
		} catch (java.lang.NoClassDefFoundError notRIEither) {
			notRIEither.printStackTrace();
			log.error("JAXB: neither Reference Implementation nor Java 6 implementation present?", notRIEither);
			throw new JAXBException("JAXB: neither Reference Implementation nor Java 6 implementation present?");
		} catch (javax.xml.bind.PropertyException notRIEither) {
			notRIEither.printStackTrace();
			log.error("JAXB: neither Reference Implementation nor Java 6 implementation present?", notRIEither);
			throw new JAXBException("JAXB: neither Reference Implementation nor Java 6 implementation present?");
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
			
		} catch (javax.xml.bind.PropertyException e) {
			
			log.error(e.getMessage(), e);
			throw e;
			
		}
		
	}
	
	public static String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) throws JAXBException {

		Object namespacePrefixMapper = getPrefixMapper();
		
		if ( namespacePrefixMapper.getClass().getName().equals("org.docx4j.jaxb.NamespacePrefixMapperSunInternal") ) {
			// Java 6
			return ((NamespacePrefixMapperSunInternal)namespacePrefixMapper).getPreferredPrefix(namespaceUri, suggestion, requirePrefix); 
			
		} else if (namespacePrefixMapper.getClass().getName().equals("org.docx4j.jaxb.NamespacePrefixMapper")) {
    		// JAXB Reference Implementation		
			return ((NamespacePrefixMapper)namespacePrefixMapper).getPreferredPrefix(namespaceUri, suggestion, requirePrefix); 
			
		} else {
			log.warn("Namespace prefix mapper not found!");
			return null;
		}
		
	}
	
}
