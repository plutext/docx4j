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

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

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
	
	public static JAXBContext jcSectionModel;

	public static JAXBContext jcEncryption;

	/** @since 3.0.1 */
	public static JAXBContext jcMCE;
	
	private static Logger log = LoggerFactory.getLogger(Context.class);
	
	
	public static JAXBImplementation jaxbImplementation = null;
		
	/** 
	 * Return the JAXB implementation detected to be in use.
	 * 
	 * @since 3.3.0 */
	public static JAXBImplementation getJaxbImplementation() {
		return jaxbImplementation;
	}
	
	
	static {
		JAXBContext tempContext = null;

		// Display diagnostic info about version of JAXB being used.
		log.info("java.vendor="+System.getProperty("java.vendor"));
		log.info("java.version="+System.getProperty("java.version"));
		log.info("java.vm.name="+System.getProperty("java.vm.name"));
		
//		// This stuff is just debugging diagnostics
//		try {
//			searchManifestsForJAXBImplementationInfo( ClassLoader.getSystemClassLoader());
//			if (Thread.currentThread().getContextClassLoader()==null) {
//				log.warn("ContextClassLoader is null for current thread");
//				// Happens with IKVM 
//			} else if (ClassLoader.getSystemClassLoader()!=Thread.currentThread().getContextClassLoader()) {
//				searchManifestsForJAXBImplementationInfo(Thread.currentThread().getContextClassLoader());
//			}
//		} catch ( java.security.AccessControlException e) {
//			// Google AppEngine throws this, at com.google.apphosting.runtime.security.CustomSecurityManager.checkPermission
//			log.warn("Caught/ignored " + e.getMessage());
//		}
		
      
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
			
//    	  System.setProperty("jakarta.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
    	  
			java.lang.ClassLoader classLoader = Context.class.getClassLoader();

			tempContext = JAXBContext.newInstance("org.docx4j.wml:org.docx4j.w14:org.docx4j.w15:" +
					"org.docx4j.com.microsoft.schemas.office.word.x2006.wordml:" +
					"org.docx4j.dml:org.docx4j.dml.chart:org.docx4j.dml.chart.x2007:org.docx4j.dml.chartDrawing:org.docx4j.dml.compatibility:org.docx4j.dml.diagram:org.docx4j.dml.lockedCanvas:org.docx4j.dml.picture:org.docx4j.dml.wordprocessingDrawing:org.docx4j.dml.spreadsheetdrawing:org.docx4j.dml.diagram2008:" +
					// All VML stuff is here, since compiling it requires WML and DML (and MathML), but not PML or SML
					"org.docx4j.vml:org.docx4j.vml.officedrawing:org.docx4j.vml.wordprocessingDrawing:org.docx4j.vml.presentationDrawing:org.docx4j.vml.spreadsheetDrawing:org.docx4j.vml.root:" +
					"org.docx4j.docProps.coverPageProps:" +
					"org.opendope.xpaths:org.opendope.conditions:org.opendope.questions:org.opendope.answers:org.opendope.components:org.opendope.SmartArt.dataHierarchy:" +
					"org.docx4j.math:" +
					"org.docx4j.sharedtypes:org.docx4j.bibliography:" +
					"org.docx4j.com.microsoft.schemas.ink.x2010.main:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x2010.chartDrawing:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x2010.main:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x2012.chart:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle:" +
//					"org.docx4j.com.microsoft.schemas.office.drawing.x2008.diagram:" // see instead existing org.docx4j.dml.diagram2008
					"org.docx4j.com.microsoft.schemas.office.drawing.x2010.diagram:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x2012.main:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x2010.picture:" +
					"org.docx4j.org.w3.x1998.math.mathML:" +
					"org.docx4j.org.w3.x2003.inkML:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x2013.main.command:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x2014.chart.ac:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x2014.chart:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x2014.main:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x201611.diagram:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x201611.main:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x201612.diagram:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x2016.ink:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x2016.SVG.main:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x201703.chart:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x2017.decorative:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x2017.model3d:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x2018.animation.model3d:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x2018.animation:" +
					"org.docx4j.com.microsoft.schemas.office.drawing.x2018.hyperlinkcolor:" +
					"org.docx4j.com.microsoft.schemas.office.powerpoint.x2014.inkAction:" + /* nb that is generated from drawing, but maybe we need to copy this and ink to pptx4j context */
					"org.docx4j.com.microsoft.schemas.office.thememl.x2012.main:" +
					"org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingDrawing:" +
					"org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape:" +
					"org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingCanvas:" +
					"org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup:" +
					"org.docx4j.com.microsoft.schemas.office.word.x2012.wordprocessingDrawing:" +
					"org.docx4j.w15symex:org.docx4j.w16cid:" +
					"org.docx4j.com.microsoft.schemas.office.webextensions.taskpanes_2010_11:" +
					"org.docx4j.com.microsoft.schemas.office.webextensions.webextension_2010_11", classLoader,
					ProviderProperties.getProviderProperties() );
			
			log.debug("JAXB Context: " + tempContext.getClass().getName());

			if (tempContext.getClass().getName().equals("org.glassfish.jaxb.runtime.v2.runtime.JAXBContextImpl")) {
				jaxbImplementation = JAXBImplementation.REFERENCE;
				log.info("JAXB Reference Implementation is in use.");
				
			} else if (tempContext.getClass().getName().equals("org.eclipse.persistence.jaxb.JAXBContext")) {
				jaxbImplementation = JAXBImplementation.ECLIPSELINK_MOXy;
				log.info("MOXy JAXB implementation is in use!");
				
			} else if (tempContext.getClass().getName().startsWith("com.ibm.xml.xlxp2.jaxb")) {
				/*
				 * Per Michael Glavassevich at https://stackoverflow.com/a/35443723/1031689
				 * 
				 * WebSphere Application Server v7+ contains an optimized JAXB implementation 
				 * that will usually be used instead of the JAXB implementation that's built 
				 * into the Sun/Oracle Java SDK. 
				 * 
				 * See https://stackoverflow.com/questions/48700004/does-webspheres-jaxb-marshallerproxy-use-the-reference-implementation/48718329#48718329
				 *  
				 * com.ibm.xml.xlxp2.jaxb.JAXBContextImpl is in jar plugins\com.ibm.ws.prereq.xlxp.jar
				 * 
				 * This can be simulated outside websphere by using IBM Java, eg IBM J9 VM (build 2.8, JRE 1.8.0 Linux amd64-64
				 * with lib/endorsed contents:
				 * 
				 * 		com.ibm.jaxb.tools.jar
				 * 		com.ibm.ws.prereq.xlxp.jar
				 *   
				 */
				jaxbImplementation = JAXBImplementation.IBM_WEBSPHERE_XLXP; // unless proven otherwise
				String xlxpContextUsage = tempContext.toString();
				log.info(xlxpContextUsage);
				if (xlxpContextUsage!=null && xlxpContextUsage.contains("Fallback JAXBContext will be used to process any requests")) {
					int pos = xlxpContextUsage.indexOf("Fallback JAXBContext");
					if (pos>=0) {
						String fallbackContext = xlxpContextUsage.substring(pos);
						if (fallbackContext.contains("com/sun/xml/internal/bind")) {
							jaxbImplementation=JAXBImplementation.ORACLE_JRE;
						} else if (fallbackContext.contains("com/sun/xml/bind")) {
							jaxbImplementation=JAXBImplementation.REFERENCE;
						} else {
							log.warn("TODO: identify context from " + fallbackContext);
						}
					} else {
						log.warn("No fallback context specified?");						
					}
				} else {
					log.info("Using IBM JAXB implementation; see system property com.ibm.xml.xlxp.jaxb.opti.level in WebSphere v7+ ");					
				}
				log.info("Using " + jaxbImplementation);
				
			} else {
				log.warn("Using unexpected JAXB: " + tempContext.getClass().getName());
			}
			
			jcThemePart = tempContext; //JAXBContext.newInstance("org.docx4j.dml",classLoader );
			jcDocPropsCore = JAXBContext.newInstance("org.docx4j.docProps.core:org.docx4j.docProps.core.dc.elements:org.docx4j.docProps.core.dc.terms",classLoader, ProviderProperties.getProviderProperties() );
			jcDocPropsCustom = JAXBContext.newInstance("org.docx4j.docProps.custom",classLoader, ProviderProperties.getProviderProperties() );
			jcDocPropsExtended = JAXBContext.newInstance("org.docx4j.docProps.extended",classLoader, ProviderProperties.getProviderProperties() );
			jcXmlPackage = JAXBContext.newInstance("org.docx4j.xmlPackage",classLoader, ProviderProperties.getProviderProperties() );
			jcRelationships = JAXBContext.newInstance("org.docx4j.relationships",classLoader, ProviderProperties.getProviderProperties() );
			jcCustomXmlProperties = JAXBContext.newInstance("org.docx4j.customXmlProperties",classLoader, ProviderProperties.getProviderProperties() );
			jcContentTypes = JAXBContext.newInstance("org.docx4j.openpackaging.contenttype",classLoader, ProviderProperties.getProviderProperties() );
			
			jcSectionModel = JAXBContext.newInstance("org.docx4j.model.structure.jaxb",classLoader, ProviderProperties.getProviderProperties() );
			
			try {
				//jcXmlDSig = JAXBContext.newInstance("org.plutext.jaxb.xmldsig",classLoader );
				jcEncryption = JAXBContext.newInstance(
						 "org.docx4j.com.microsoft.schemas.office.x2006.encryption:"
						+ "org.docx4j.com.microsoft.schemas.office.x2006.keyEncryptor.certificate:"
						+ "org.docx4j.com.microsoft.schemas.office.x2006.keyEncryptor.password:"
						,classLoader, ProviderProperties.getProviderProperties() );
			} catch (jakarta.xml.bind.JAXBException e) {
				log.error(e.getMessage());
			}

			jcMCE = JAXBContext.newInstance("org.docx4j.mce",classLoader, ProviderProperties.getProviderProperties() );
			
			log.debug(".. other contexts loaded ..");
										
			
		} catch (Exception ex) {
			ex.printStackTrace();
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
                    			&& impTitle.contains("Jakarta XML Binding Implementation")
                    				) {
                    		
                    		/*
				                 <groupId>com.sun.xml.bind</groupId>
				                 <artifactId>jaxb-impl</artifactId>
				                 <version>3.0.0</version>
                    		 * 
									Implementation-Title: Jakarta XML Binding Implementation
									Implementation-Vendor: Eclipse Foundation
									Implementation-Vendor-Id: org.eclipse
									Implementation-Version: 3.0.0
									Major-Version: 3.0.0
									Specification-Title: Jakarta XML Binding
									Specification-Version: 3.0
									Bundle-Description: Old JAXB Runtime module. Contains sources required f
									 or runtime processing.
									Bundle-ManifestVersion: 2
									Bundle-Name: Old JAXB Runtime
									Bundle-SymbolicName: com.sun.xml.bind.jaxb-impl
									Bundle-Vendor: Eclipse Foundation
									Bundle-Version: 3.0.0
								
								
							  <groupId>org.glassfish.jaxb</groupId>
							  <artifactId>jaxb-runtime</artifactId>
							  <version>3.0.2</version>

								Build-Version: JAXB RI 3.0.2
								Git-Url: scm:git:ssh://git@github.com/eclipse-ee4j/jaxb-ri/jaxb-runtime-
								 parent/jaxb-runtime
								Implementation-Title: Jakarta XML Binding Implementation
								Implementation-Vendor: Eclipse Foundation
								Implementation-Vendor-Id: org.glassfish.jaxb
								Implementation-Version: 3.0.2
								Major-Version: 3.0.2
								Specification-Title: Jakarta XML Binding
								Specification-Version: 3.0
								Bundle-Description: JAXB (JSR 222) Reference Implementation
								Bundle-Name: JAXB Runtime
								Bundle-SymbolicName: org.glassfish.jaxb.runtime								
								
                    		 */
	                    
        	                log.info("\n" + url);
		                    for(Object key2  : mainAttribs.keySet() ) {
		                    	
		                    	log.info(key2 + " : " + mainAttribs.getValue((java.util.jar.Attributes.Name)key2));
		                    }
                    	}
                    	// Bundle-Name: EclipseLink MOXy
                    	impTitle = mainAttribs.getValue("Bundle-Name");
                    	if (impTitle!=null
                    			&& impTitle.contains("EclipseLink MOXy")
                        				) {
	                    
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
	
    public static void main(String[] args)
            throws Exception {

    	System.out.println(Context.getWmlObjectFactory().getClass().getName()); 
    	
    }
    
}
