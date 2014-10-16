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


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.docx4j.utils.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Context {
	
	public static final JAXBContext jc;
	
	// TEMP/Experimental
//	public static void setJc(JAXBContext jc) {
//		Context.jc = jc;
//	}

	@Deprecated
	public static JAXBContext jcThemePart;
	
	public static JAXBContext jcDocPropsCore;
	public static JAXBContext jcDocPropsCustom;
	public static JAXBContext jcDocPropsExtended;
	public static JAXBContext jcRelationships;
	public static JAXBContext jcCustomXmlProperties;
	public static JAXBContext jcContentTypes;

	public static JAXBContext jcXmlPackage;
	
	private static JAXBContext jcXslFo;
	public static JAXBContext jcSectionModel;

	public static JAXBContext jcXmlDSig;

	/** @since 3.0.1 */
	public static JAXBContext jcMCE;
	
	private static Logger log = LoggerFactory.getLogger(Context.class);
		
	static {
		JAXBContext tempContext = null;

		// Display diagnostic info about version of JAXB being used.
		log.info("java.vendor="+System.getProperty("java.vendor"));
		log.info("java.version="+System.getProperty("java.version"));
		
		// This stuff is just debugging diagnostics
		try {
			searchManifestsForJAXBImplementationInfo( ClassLoader.getSystemClassLoader());
			if (Thread.currentThread().getContextClassLoader()==null) {
				log.warn("ContextClassLoader is null for current thread");
				// Happens with IKVM 
			} else if (ClassLoader.getSystemClassLoader()!=Thread.currentThread().getContextClassLoader()) {
				searchManifestsForJAXBImplementationInfo(Thread.currentThread().getContextClassLoader());
			}
		} catch ( java.security.AccessControlException e) {
			// Google AppEngine throws this, at com.google.apphosting.runtime.security.CustomSecurityManager.checkPermission
			log.warn("Caught/ignored " + e.getMessage());
		}
		
		// Diagnostics regarding JAXB implementation
		InputStream jaxbPropsIS=null;
		try {
			// Is MOXy configured?
			jaxbPropsIS = ResourceUtils.getResource("org/docx4j/wml/jaxb.properties");
			log.info("MOXy JAXB implementation intended..");
		} catch (Exception e3) {
			log.info("No MOXy JAXB config found; assume not intended..");
			log.debug(e3.getMessage());
		}
		if (jaxbPropsIS==null) {
			// Only probe for other implementations if no MOXy
			try {
				Object namespacePrefixMapper = NamespacePrefixMapperUtils.getPrefixMapper();
				if ( namespacePrefixMapper.getClass().getName().equals("org.docx4j.jaxb.NamespacePrefixMapperSunInternal") ) {
					// Java 6
					log.info("Using Java 6/7 JAXB implementation");
				} else {
					log.info("Using JAXB Reference Implementation");			
				}
				
			} catch (JAXBException e) {
				log.error("PANIC! No suitable JAXB implementation available");
				log.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}
      
      try { 
			// JAXBContext.newInstance uses the context class loader of the current thread. 
			// To specify the use of a different class loader, 
			// either set it via the Thread.setContextClassLoader() api 
			// or use the newInstance method.
			// JBOSS (older versions only?) might use a different class loader to load JAXBContext, 
    	  	// which caused problems, so in docx4j we explicitly specify our class loader.  
    	  	// IKVM 7.3.4830 also needs this to be done
    	  	// (java.lang.Thread.currentThread().setContextClassLoader doesn't seem to help!)
    	  	// and there are no environments in which this approach is known to be problematic
			
			java.lang.ClassLoader classLoader = Context.class.getClassLoader();

			tempContext = JAXBContext.newInstance("org.docx4j.wml:org.docx4j.w14:org.docx4j.w15:" +
					"org.docx4j.schemas.microsoft.com.office.word_2006.wordml:" +
					"org.docx4j.dml:org.docx4j.dml.chart:org.docx4j.dml.chartDrawing:org.docx4j.dml.compatibility:org.docx4j.dml.diagram:org.docx4j.dml.lockedCanvas:org.docx4j.dml.picture:org.docx4j.dml.wordprocessingDrawing:org.docx4j.dml.spreadsheetdrawing:org.docx4j.dml.diagram2008:" +
					// All VML stuff is here, since compiling it requires WML and DML (and MathML), but not PML or SML
					"org.docx4j.vml:org.docx4j.vml.officedrawing:org.docx4j.vml.wordprocessingDrawing:org.docx4j.vml.presentationDrawing:org.docx4j.vml.spreadsheetDrawing:org.docx4j.vml.root:" +
					"org.docx4j.docProps.coverPageProps:" +
					"org.opendope.xpaths:org.opendope.conditions:org.opendope.questions:org.opendope.answers:org.opendope.components:org.opendope.SmartArt.dataHierarchy:" +
					"org.docx4j.math:" +
					"org.docx4j.sharedtypes:org.docx4j.bibliography",classLoader );
			
			if (tempContext.getClass().getName().equals("org.eclipse.persistence.jaxb.JAXBContext")) {
				log.info("MOXy JAXB implementation is in use!");
			} else {
				log.info("Not using MOXy; using " + tempContext.getClass().getName());				
			}
			
			jcThemePart = tempContext; //JAXBContext.newInstance("org.docx4j.dml",classLoader );
			jcDocPropsCore = JAXBContext.newInstance("org.docx4j.docProps.core:org.docx4j.docProps.core.dc.elements:org.docx4j.docProps.core.dc.terms",classLoader );
			jcDocPropsCustom = JAXBContext.newInstance("org.docx4j.docProps.custom",classLoader );
			jcDocPropsExtended = JAXBContext.newInstance("org.docx4j.docProps.extended",classLoader );
			jcXmlPackage = JAXBContext.newInstance("org.docx4j.xmlPackage",classLoader );
			jcRelationships = JAXBContext.newInstance("org.docx4j.relationships",classLoader );
			jcCustomXmlProperties = JAXBContext.newInstance("org.docx4j.customXmlProperties",classLoader );
			jcContentTypes = JAXBContext.newInstance("org.docx4j.openpackaging.contenttype",classLoader );
			
			jcSectionModel = JAXBContext.newInstance("org.docx4j.model.structure.jaxb",classLoader );
			
			jcXmlDSig = JAXBContext.newInstance("org.plutext.jaxb.xmldsig",classLoader );

			jcMCE = JAXBContext.newInstance("org.docx4j.mce",classLoader );
			
			log.debug(".. other contexts loaded ..");
										
			
		} catch (Exception ex) {
			log.error("Cannot initialize context", ex);
		}				
      jc = tempContext;
	}
	
	private static org.docx4j.wml.ObjectFactory wmlObjectFactory;
	
	public static org.docx4j.wml.ObjectFactory getWmlObjectFactory() {
		
		if (wmlObjectFactory==null) {
			wmlObjectFactory = new org.docx4j.wml.ObjectFactory();
		}
		return wmlObjectFactory;
		
	}

	public static JAXBContext getXslFoContext() {
		if (jcXslFo==null) {
			try {	
				Context tmp = new Context();
				java.lang.ClassLoader classLoader = tmp.getClass().getClassLoader();

				jcXslFo = JAXBContext.newInstance("org.plutext.jaxb.xslfo",classLoader );
				
			} catch (JAXBException ex) {
	      log.error("Cannot determine XSL-FO context", ex);
			}						
		}
		return jcXslFo;		
	}
	
	public static void searchManifestsForJAXBImplementationInfo(ClassLoader loader) {
	    Enumeration resEnum;
	    try {
	        resEnum = loader.getResources(JarFile.MANIFEST_NAME);
	        while (resEnum.hasMoreElements()) {
	        	InputStream is = null;
	            try {
	                URL url = (URL)resEnum.nextElement();
//	                System.out.println("\n\n" + url);
	                is = url.openStream();
	                if (is != null) {
	                    Manifest manifest = new Manifest(is);

                    	Attributes mainAttribs = manifest.getMainAttributes();
                    	String impTitle = mainAttribs.getValue("Implementation-Title");
                    	if (impTitle!=null
                    			&& impTitle.contains("JAXB Reference Implementation")
                    					|| impTitle.contains("org.eclipse.persistence") ) {
	                    
        	                log.info("\n" + url);
		                    for(Object key2  : mainAttribs.keySet() ) {
		                    	
		                    	log.info(key2 + " : " + mainAttribs.getValue((java.util.jar.Attributes.Name)key2));
		                    }
                    	}
                    	
	                    // In 2.1.3, it is in here
	                    for(String key  :  manifest.getEntries().keySet() ) {
	    	                //System.out.println(key);	                    
	    	                if (key.equals("com.sun.xml.bind.v2.runtime")) {
		    	                log.info("Found JAXB reference implementation in " + url);
		                    	mainAttribs = manifest.getAttributes((String)key);
		                    
			                    for(Object key2  : mainAttribs.keySet() ) {
			                    	log.info(key2 + " : " + mainAttribs.getValue((java.util.jar.Attributes.Name)key2));
			                    }
		                    }
	                    }
	                    
	                }
	            }
	            catch (Exception e) {
	                // Silently ignore 
//	            	log.error(e.getMessage(), e);
	            } finally {
	            	IOUtils.closeQuietly(is);
	            }
	        }
	    } catch (IOException e1) {
	        // Silently ignore 
//        	log.error(e1);
	    }
	     
	}	
}
