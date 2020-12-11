package org.docx4j;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.utils.ResourceUtils;

public class Docx4jProperties {
	
	protected static Logger log = LoggerFactory.getLogger(Docx4jProperties.class);
	
	private static Properties properties;
	
	private static void init() {
		
		properties = new Properties();
		try (
				InputStream is = ResourceUtils.getResource("docx4j.properties");
			) {
			properties.load(is);
		} catch (Exception e) {
			log.warn("Couldn't find/read docx4j.properties; " + e.getMessage());
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
	 * @since 3.3.0
	 */
	public static boolean getProperty(String key, boolean defaultValue) {
		
		if (properties==null) {init();}
		String result = properties.getProperty(key, Boolean.toString(defaultValue));
		return Boolean.parseBoolean(result);
	}

	public static int getProperty(String key, int defaultValue) {
		
		if (properties==null) {init();}
		String val = properties.getProperty(key);
		if (val==null) return defaultValue;
		
		try {
			return (Integer.parseInt(val));
		} catch (NumberFormatException e) {
			log.info(e.getMessage(),e);
			return defaultValue;
		}
	}
	/**
	 * @since 6.0.0
	 */
	public static long getPropertyLong(String key, long defaultValue) {
		
		if (properties==null) {init();}
		String val = properties.getProperty(key);
		if (val==null) return defaultValue;
		
		try {
			return (Long.parseLong(val));
		} catch (NumberFormatException e) {
			log.info(e.getMessage(),e);
			return defaultValue;
		}
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
	/**
	 * @since 6.0.0
	 */
	public static void setPropertyLong(String key, long value) {
		
		if (properties==null) {init();}
		properties.setProperty(key, Long.toString(value));		
	}
	
}
