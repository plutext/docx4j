package org.docx4j;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.docx4j.utils.ResourceUtils;

public class Docx4jProperties {
	
	protected static Logger log = Logger.getLogger(Docx4jProperties.class);
	
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
	
}
