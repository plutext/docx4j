/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */
package org.xlsx4j.jaxb;


import javax.xml.bind.JAXBContext;

import org.docx4j.jaxb.ProviderProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Context {
	
	/*
	 * Two reasons for having a separate class for this:
	 * 1. so that loading SML context does not slow
	 *    down docx4j operation on docx files
	 * 2. to try to maintain clean delineation between
	 *    docx4j and xlsx4j
	 */
	
	public static JAXBContext jcSML;
	
	private static Logger log = LoggerFactory.getLogger(Context.class);
		
	static {
		
		// Display diagnostic info about version of JAXB being used.
		log.info("java.vendor="+System.getProperty("java.vendor"));
		log.info("java.version="+System.getProperty("java.version"));
		
		org.docx4j.jaxb.Context.searchManifestsForJAXBImplementationInfo( ClassLoader.getSystemClassLoader());
		if (Thread.currentThread().getContextClassLoader()==null) {
			log.warn("ContextClassLoader is null for current thread");
			// Happens with IKVM 
		} else if (ClassLoader.getSystemClassLoader()!=Thread.currentThread().getContextClassLoader()) {
			org.docx4j.jaxb.Context.searchManifestsForJAXBImplementationInfo(Thread.currentThread().getContextClassLoader());
		}
		
		try {	
			
			java.lang.ClassLoader classLoader = Context.class.getClassLoader();
				
			jcSML = JAXBContext.newInstance("org.xlsx4j.sml:" +
					"org.xlsx4j.schemas.microsoft.com.office.excel.x2010.spreadsheetDrawing:" +	
					"org.xlsx4j.schemas.microsoft.com.office.excel_2006.main:" +
					"org.xlsx4j.schemas.microsoft.com.office.excel_2008_2.main",classLoader, ProviderProperties.getProviderProperties() );
				
			
			
			if (jcSML.getClass().getName().equals("org.eclipse.persistence.jaxb.JAXBContext")) {
				log.info("MOXy JAXB implementation is in use!");
			} else {
				log.info("Not using MOXy.");				
			}
			
		} catch (Exception ex) {
			log.error("Cannot initialize context", ex);
		}				
	}
	
	
	public static org.xlsx4j.sml.ObjectFactory smlObjectFactory;
	public static org.xlsx4j.sml.ObjectFactory getsmlObjectFactory() {
		
		if (smlObjectFactory==null) {
			smlObjectFactory = new org.xlsx4j.sml.ObjectFactory();
		}
		return smlObjectFactory;
		
	}
}
