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
	
	public static String getProperty(String name) {
		
		if (properties==null) {init();}
		return properties.getProperty(name);		
	}

	public static Properties getProperties() {
		
		if (properties==null) {init();}
		return properties;		
	}
	
}
