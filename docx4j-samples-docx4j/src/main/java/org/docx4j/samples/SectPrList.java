/*
 *  Copyright 2007-2025, Plutext Pty Ltd.
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


import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.com.microsoft.schemas.office.word.x2006.wordml.CTRel;
import org.docx4j.finders.SectPrFinder;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.SectPr;

import jakarta.xml.bind.JAXBContext;


public class SectPrList  {

	public static JAXBContext context = org.docx4j.jaxb.Context.jc;

	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir")
					+ "/fb.docx";
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));

		SectPrList sectPrList = new SectPrList();
		sectPrList.listSectPr(wordMLPackage);
		
	}
	
	void listSectPr(WordprocessingMLPackage wordMLPackage) throws Docx4JException {
		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

    	// Find the sectPrs
    	SectPrFinder sf = new SectPrFinder(wordMLPackage.getMainDocumentPart());
		new TraversalUtil(wordMLPackage.getMainDocumentPart().getContents(), sf);
		
		int i = 1;
		
		for (SectPr sectPr : sf.getOrderedSectPrList() ) {

			// remove the stuff we're not interested in seeing
			sectPr.getEGHdrFtrReferences().clear();
			sectPr.setPgSz(null);
//			sectPr.setPgMar(null);
			sectPr.setCols(null);
			sectPr.setDocGrid(null);
			
//			sectPr.setType(null);
//			sectPr.setPgNumType(null);
						
			System.out.println(i + " " + removeNamespacesFromPrintout(XmlUtils.marshaltoString(sectPr)));
			i++;
		}
		
		System.out.println("Count: " + --i );
		

	}
	
	String removeNamespacesFromPrintout(String in) {
		
		if (in.endsWith("/>")) {
			return "<w:sectPr/>";
		}
		
		int gt = in.indexOf(">");
		
		return  "<w:sectPr>" + in.substring(gt+1); 
	}

}
