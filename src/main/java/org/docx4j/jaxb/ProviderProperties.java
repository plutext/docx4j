package org.docx4j.jaxb;

import java.util.HashMap;
import java.util.Map;

/**
 * Allows you to set provider-specific properties.
 * 
 * This should generally not be necessary.
 *  
 * For JAXB Reference Implementation specific properties, see 
 * https://github.com/gf-metro/jaxb/blob/master/jaxb-ri/runtime/impl/src/main/java/com/sun/xml/bind/api/JAXBRIContext.java#L437
 * 
 * For MOXy specific properties, see 
 * https://github.com/eclipse/eclipselink.runtime/blob/master/moxy/org.eclipse.persistence.moxy/src/org/eclipse/persistence/jaxb/JAXBContextProperties.java
 * 
 * @author jharrop
 * @since 3.3.3
 */
public class ProviderProperties {

	private static Map<String, Object> providerProperties = new HashMap<String, Object>(); 	

	/** 
	 * Return the Map in which you can set provider-specific properties. 
	 * 
	 * @since 3.3.3 
	 */
	public static Map<String, Object> getProviderProperties() {
		
		return providerProperties;
	}
	
}
