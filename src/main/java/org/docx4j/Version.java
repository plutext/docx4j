package org.docx4j;

import java.util.Properties;

import org.docx4j.utils.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Version {

	protected static Logger log = LoggerFactory.getLogger(Version.class);
	
	private static Properties properties;
	
	private static void init() {
		
		properties = new Properties();
		try {
			properties.load(
					ResourceUtils.getResource("docx4j_version.properties"));
		} catch (Exception e) {
			log.warn("Couldn't find/read docx4j_version.properties; " + e.getMessage());
		}
	}
	
	private static String version;
	
	public static String getDocx4jVersion() {
		
		if (version==null) {
		
			if (properties==null) {init();}
					
			version = properties.getProperty("version");
		}
		
		return version;
	}
	
	
	
}
