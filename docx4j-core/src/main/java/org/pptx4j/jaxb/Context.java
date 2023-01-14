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


import jakarta.xml.bind.JAXBContext;

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
					"org.docx4j.dml:org.docx4j.dml.chart:org.docx4j.dml.chart.x2007:org.docx4j.dml.chartDrawing:org.docx4j.dml.compatibility:org.docx4j.dml.diagram:org.docx4j.dml.lockedCanvas:org.docx4j.dml.picture:org.docx4j.dml.wordprocessingDrawing:org.docx4j.dml.spreadsheetdrawing:org.docx4j.dml.diagram2008:" +
					"org.docx4j.mce:" + // note that's not in the docx4j Context.jc
					// VML, requires WML and DML (and MathML), but not PML or SML
					// Is it ever used in a pptx?
					// "org.docx4j.vml:org.docx4j.vml.officedrawing:org.docx4j.vml.wordprocessingDrawing:org.docx4j.vml.presentationDrawing:org.docx4j.vml.spreadsheetDrawing:org.docx4j.vml.root:" +					
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
					// The following copied from docx4j Context.jc 2023 01 14; since docx4j 11.4.9
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
					"org.docx4j.com.microsoft.schemas.office.powerpoint.x2014.inkAction:" + 
					"org.docx4j.com.microsoft.schemas.office.thememl.x2012.main:" +
//					"org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingDrawing:" +
//					"org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape:" +
//					"org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingCanvas:" +
//					"org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup:" +
//					"org.docx4j.com.microsoft.schemas.office.word.x2012.wordprocessingDrawing:" +
//					"org.docx4j.w15symex:org.docx4j.w16cid:" +
					"org.docx4j.com.microsoft.schemas.office.webextensions.taskpanes_2010_11:" +
					"org.docx4j.com.microsoft.schemas.office.webextensions.webextension_2010_11",
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


