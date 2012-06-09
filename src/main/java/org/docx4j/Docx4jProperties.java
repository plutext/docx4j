/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
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
	
	public static Properties getProperties() {
		
		if (properties==null) {init();}
		return properties;		
	}
	
}
