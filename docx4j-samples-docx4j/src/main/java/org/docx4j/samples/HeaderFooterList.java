/*
 *  Copyright 2010, Plutext Pty Ltd.
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

package org.docx4j.samples;


import java.util.List;

import javax.xml.bind.JAXBContext;

import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;


/**
 * This sample tells you how docx4j is interpreting headers/footers
 * in your docx.
 * 
 * It should match what you see when you look at the docx in Word
 * (as opposed to the raw document.xml//sectPr content).
 * 
 * @author jharrop
 *
 */
public class HeaderFooterList extends AbstractSample {

	public static JAXBContext context = org.docx4j.jaxb.Context.jc;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
			inputfilepath = System.getProperty("user.dir")
					+ "/sample-docs/test-docs/header-footer/header_first.xml";
			
//			inputfilepath = System.getProperty("user.dir")
//			+ "/tmp/toc.docx";
		}

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));

		List<SectionWrapper> sectionWrappers = wordMLPackage.getDocumentModel().getSections();
		
		for (SectionWrapper sw : sectionWrappers) {
			HeaderFooterPolicy hfp = sw.getHeaderFooterPolicy();
			
			System.out.println("\n\nSECTION  \n");
			
			System.out.println("Headers:");
			if (hfp.getFirstHeader()!=null) System.out.println("-first"); 
			if (hfp.getDefaultHeader()!=null) System.out.println("-default"); 
			if (hfp.getEvenHeader()!=null) System.out.println("-even"); 
			
			System.out.println("\nFooters:");
			if (hfp.getFirstFooter()!=null) System.out.println("-first"); 
			if (hfp.getDefaultFooter()!=null) System.out.println("-default"); 
			if (hfp.getEvenFooter()!=null) System.out.println("-even"); 
			
		}
		
	}

}
