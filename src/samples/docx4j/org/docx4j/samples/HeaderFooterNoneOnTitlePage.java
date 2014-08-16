/*
 *  Copyright 2007-2010, Plutext Pty Ltd.
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

import java.io.File;
import java.util.List;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.PPr;
import org.docx4j.wml.SectPr;

/**
 * Create a WordML Pkg and add a header to it.
 * Output is Flat OPC XML.
 * Notice:
 * 1. the Header part
 * 2. the contents of the sectPr element
 * 
 * @author jharrop
 *
 */
public class HeaderFooterNoneOnTitlePage  extends AbstractSample {

	private static ObjectFactory objectFactory = new ObjectFactory();

	public static void main(String[] args) throws Exception {
		
		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
			inputfilepath = System.getProperty("user.dir")
					+ "/simpleH.docx";
		}

		WordprocessingMLPackage output = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));

		List mainContent = output.getMainDocumentPart().getContent(); 
		
		// Add a sectPr		
		org.docx4j.wml.P  par = objectFactory.createP(); 
		mainContent.add(0,par); 
		SectPr sectPr = objectFactory.createSectPr();
		PPr ppr = objectFactory.createPPr();
		ppr.setSectPr(sectPr);
		par.setPPr(ppr);
		
		
		// From the spec, if this element is set to true and the
		// first page header type is omitted, then a blank header
		// shall be created as needed
		sectPr.setTitlePg(new BooleanDefaultTrue());
		
		// Set nextPage on the *next* (!) section
//		output.getDocumentModel().refresh();
//		List<SectionWrapper> sections = output.getDocumentModel().getSections();
//		SectPr nextSectPr = sections.get(1).getSectPr();
//		Type sectionType = objectFactory.createSectPrType();
//		sectionType.setVal("nextPage");
//		nextSectPr.setType(sectionType);
		

		// Now add before this sectPr, whatever content you
		// want at the beginning
		org.docx4j.wml.P  yourContent = objectFactory.createP(); 
		mainContent.add(0,yourContent); 		
	
		output.save(new File(System.getProperty("user.dir")
					+ "/simpleH_OUT.docx"));
	}
	

	
}
