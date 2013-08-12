package org.docx4j;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.utils.ResourceUtils;

public class Docx4jProperties {
	
	protected static Logger log = LoggerFactory.getLogger(Docx4jProperties.class);
	
	private static Properties properties;
	
	private static void init() {
		
		properties = new Properties();
		try {
			properties.load(
					ResourceUtils.getResource("docx4j.properties"));
		} catch (Exception e) {
			log.error("Error reading docx4j.properties", e);
		}
	}
	
	public static String getProperty(String key) {
		
		if (properties==null) {init();}
				
		return properties.getProperty(key);		
	}

	
	/**
	 * @since 2.7.2
	 */
	public static String getProperty(String key, String defaultValue) {
		
		if (properties==null) {init();}
				
		return properties.getProperty(key, defaultValue);		
	}
	
	/**
	 * @since 2.8.1
	 */
	public static boolean getProperty(String key, boolean defaultValue) {
		
		if (properties==null) {init();}
		String result = properties.getProperty(key, Boolean.toString(defaultValue));
		return Boolean.parseBoolean(result);
	}
	
	public static Properties getProperties() {
		
		if (properties==null) {init();}
		return properties;		
	}
	
	/**
	 * Useful if a unit test requires a certain property value.
	 * 
	 * @since 3.0.0
	 */
	public static void setProperty(String key, Boolean value) {
		if (properties==null) {init();}
		properties.setProperty(key, value.toString());		
	}	

	/**
	 * Useful if a unit test requires a certain property value.
	 * 
	 * @since 3.0.0
	 */
	public static void setProperty(String key, String value) {
		if (properties==null) {init();}
		properties.setProperty(key, value);		
	}	
	
}
