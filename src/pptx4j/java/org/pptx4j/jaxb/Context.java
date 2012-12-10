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
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.utils.Log4jConfigurator;

public class Context {
	
	/*
	 * Two reasons for having a separate class for this:
	 * 1. so that loading PML context does not slow
	 *    down docx4j operation on docx files
	 * 2. to try to maintain clean delineation between
	 *    docx4j and pptx4j
	 */
	
	public static JAXBContext jcPML;
	
	private static Logger log = Logger.getLogger(Context.class);
	
	static {

		Log4jConfigurator.configure();
		
		// Display diagnostic info about version of JAXB being used.
		Object namespacePrefixMapper;
		try {
			namespacePrefixMapper = NamespacePrefixMapperUtils.getPrefixMapper();
			if ( namespacePrefixMapper.getClass().getName().equals("org.docx4j.jaxb.NamespacePrefixMapperSunInternal") ) {
				// Java 6
				log.info("Using Java 6/7 JAXB implementation");
			} else {
				log.info("Using JAXB Reference Implementation");			
			}
		} catch (JAXBException e) {
			log.error("PANIC! No suitable JAXB implementation available");
			e.printStackTrace();
		}
		
		try {	
			
			// JBOSS might use a different class loader to load JAXBContext, which causes problems,
			// so explicitly specify our class loader.
			Context tmp = new Context();
			java.lang.ClassLoader classLoader = tmp.getClass().getClassLoader();
			//log.info("\n\nClassloader: " + classLoader.toString() );			
			
			log.info("loading Context jcPML");			
			jcPML = JAXBContext.newInstance("org.pptx4j.pml:" +
					"org.docx4j.dml:org.docx4j.dml.chart:org.docx4j.dml.chartDrawing:org.docx4j.dml.compatibility:org.docx4j.dml.diagram:org.docx4j.dml.lockedCanvas:org.docx4j.dml.picture:org.docx4j.dml.wordprocessingDrawing:org.docx4j.dml.spreadsheetdrawing:" +
					"org.docx4j.mce", 
					classLoader );
			log.info(".. loaded ..");
			
		} catch (Exception ex) {
			ex.printStackTrace();
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
