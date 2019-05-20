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
package org.pptx4j.jaxb;


import javax.xml.bind.JAXBContext;

import org.docx4j.jaxb.ProviderProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Context {
	
	/*
	 * Two reasons for having a separate class for this:
	 * 1. so that loading PML context does not slow
	 *    down docx4j operation on docx files
	 * 2. to try to maintain clean delineation between
	 *    docx4j and pptx4j
	 */
	
	public static JAXBContext jcPML;
	
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

			jcPML = JAXBContext.newInstance("org.pptx4j.pml:" +
					"org.docx4j.dml:org.docx4j.dml.chart:org.docx4j.dml.chartDrawing:org.docx4j.dml.compatibility:org.docx4j.dml.diagram:org.docx4j.dml.lockedCanvas:org.docx4j.dml.picture:org.docx4j.dml.wordprocessingDrawing:org.docx4j.dml.spreadsheetdrawing:" +
					"org.pptx4j.com.microsoft.schemas.office.powerpoint.x2010.main:" +
					"org.pptx4j.com.microsoft.schemas.office.powerpoint.x2012.main:" +
					"org.pptx4j.com.microsoft.schemas.office.powerpoint.x201606.main:" +
					"org.pptx4j.com.microsoft.schemas.office.powerpoint.x2013.main.command:" +
					"org.pptx4j.com.microsoft.schemas.office.powerpoint.x201509.main:" +
					"org.pptx4j.com.microsoft.schemas.office.powerpoint.x201510.main:" +
					"org.pptx4j.com.microsoft.schemas.office.powerpoint.x2015.main:" +
					"org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.sectionzoom:" +
					"org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.slidezoom:" +
					"org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.summaryzoom:" +
					"org.pptx4j.com.microsoft.schemas.office.powerpoint.x201710.main:" +
					"org.pptx4j.com.microsoft.schemas.office.powerpoint.x201703.main:" +
					"org.pptx4j.com.microsoft.schemas.office.powerpoint.x201804.main:" +
					"org.docx4j.mce", 
					classLoader, ProviderProperties.getProviderProperties() );
			
			if (jcPML.getClass().getName().equals("org.eclipse.persistence.jaxb.JAXBContext")) {
				log.info("MOXy JAXB implementation is in use!");
			} else {
				log.info("Not using MOXy.");				
			}
			
			
		} catch (Exception ex) {
			log.error("Cannot initialize context", ex);
		}				
	}
	
	
	public static org.pptx4j.pml.ObjectFactory pmlObjectFactory;
	public static org.pptx4j.pml.ObjectFactory getpmlObjectFactory() {
		
		if (pmlObjectFactory==null) {
			pmlObjectFactory = new org.pptx4j.pml.ObjectFactory();
		}
		return pmlObjectFactory;
		
	}
}


