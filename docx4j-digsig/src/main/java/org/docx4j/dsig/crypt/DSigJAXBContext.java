/*
 *  Copyright 2016, Plutext Pty Ltd.
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
package org.docx4j.dsig.crypt;


import jakarta.xml.bind.JAXBContext;

import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.JAXBImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DSigJAXBContext {
	

	public static JAXBContext jcXmlDSig;
	
	private static Logger log = LoggerFactory.getLogger(DSigJAXBContext.class);
		
	static {
		JAXBContext tempContext = null;
      
      try { 
			java.lang.ClassLoader classLoader = DSigJAXBContext.class.getClassLoader();

			tempContext = JAXBContext.newInstance("org.apache.xml.security.binding.xmldsig:" // or 1.1?
					+ "org.docx4j.com.microsoft.schemas.office.x2006.digsig:"
					+ "org.docx4j.org.etsi.uri.x01903.v13:"
					+ "org.docx4j.org.etsi.uri.x01903.v141:"
					+ "org.docx4j.org.openxmlformats.schemas.xpackage.x2006.digitalSignature",classLoader );
			
			if (tempContext.getClass().getName().equals("org.eclipse.persistence.jaxb.JAXBContext")) {
				log.info("MOXy JAXB implementation is in use!");
			} else if (Context.getJaxbImplementation() !=null
						&& Context.getJaxbImplementation()==JAXBImplementation.ECLIPSELINK_MOXy) {
				log.error("Not using MOXy; using " + tempContext.getClass().getName() + "; check your docx4j-MOXy-JAXBContext is 3.3.0 or later!");				
			} else {
				log.info("Not using MOXy; using " + tempContext.getClass().getName());								
			}
			
			jcXmlDSig = tempContext;
										
			
		} catch (Exception ex) {
			log.error("Cannot initialize context", ex);
		}				

	}
	
	
}
