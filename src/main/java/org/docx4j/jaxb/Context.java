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
package org.docx4j.jaxb;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

public class Context {
	
	public static JAXBContext jc;
	public static JAXBContext jcThemePart;
	public static JAXBContext jcDocPropsCore;
	public static JAXBContext jcDocPropsCustom;
	public static JAXBContext jcDocPropsExtended;
	public static JAXBContext jcXmlPackage;
	public static JAXBContext jcRelationships;
	public static JAXBContext jcCustomXmlProperties;
	public static JAXBContext jcContentTypes;
	public static JAXBContext jcPML;
	
	private static Logger log = Logger.getLogger(Context.class);
	
	static {

		// Display diagnostic info about version of JAXB being used.
    	Class c;
    	try {
    		c = Class.forName("com.sun.xml.bind.marshaller.NamespacePrefixMapper");
    		System.out.println("JAXB: Using RI");
    	} catch (ClassNotFoundException cnfe) {
    		// JAXB Reference Implementation not present
    		System.out.println("JAXB: RI not present.  Trying Java 6 implementation.");
        	try {
				c = Class.forName("com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper");
	    		System.out.println("JAXB: Using Java 6 implementation.");
			} catch (ClassNotFoundException e) {
				System.out.println("JAXB: neither Reference Implementation nor Java 6 implementation present?");
			}
    	}
		
		try {	
			
			// JBOSS might use a different class loader to load JAXBContext, which causes problems,
			// so explicitly specify our class loader.
			Context tmp = new Context();
			java.lang.ClassLoader classLoader = tmp.getClass().getClassLoader();
			//log.info("\n\nClassloader: " + classLoader.toString() );			
			
			jc = JAXBContext.newInstance("org.docx4j.wml:org.docx4j.dml:org.docx4j.dml.picture:org.docx4j.dml.wordprocessingDrawing:org.docx4j.vml:org.docx4j.vml.officedrawing:org.docx4j.math",classLoader );
			jcThemePart = JAXBContext.newInstance("org.docx4j.dml",classLoader );
			jcDocPropsCore = JAXBContext.newInstance("org.docx4j.docProps.core:org.docx4j.docProps.core.dc.elements:org.docx4j.docProps.core.dc.terms",classLoader );
			jcDocPropsCustom = JAXBContext.newInstance("org.docx4j.docProps.custom",classLoader );
			jcDocPropsExtended = JAXBContext.newInstance("org.docx4j.docProps.extended",classLoader );
			jcXmlPackage = JAXBContext.newInstance("org.docx4j.xmlPackage",classLoader );
			jcRelationships = JAXBContext.newInstance("org.docx4j.relationships",classLoader );
			jcCustomXmlProperties = JAXBContext.newInstance("org.docx4j.customXmlProperties",classLoader );
			jcContentTypes = JAXBContext.newInstance("org.docx4j.openpackaging.contenttype",classLoader );
			jcPML = JAXBContext.newInstance("org.pptx4j.pml:org.docx4j.dml:org.docx4j.dml.picture",classLoader );
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}				
	}
	
	public static org.docx4j.wml.ObjectFactory wmlObjectFactory;
	
	public static org.docx4j.wml.ObjectFactory getWmlObjectFactory() {
		
		if (wmlObjectFactory==null) {
			wmlObjectFactory = new org.docx4j.wml.ObjectFactory();
		}
		return wmlObjectFactory;
		
	}
	
	public static org.pptx4j.pml.ObjectFactory pmlObjectFactory;
	public static org.pptx4j.pml.ObjectFactory getpmlObjectFactory() {
		
		if (pmlObjectFactory==null) {
			pmlObjectFactory = new org.pptx4j.pml.ObjectFactory();
		}
		return pmlObjectFactory;
		
	}
	
		
}
