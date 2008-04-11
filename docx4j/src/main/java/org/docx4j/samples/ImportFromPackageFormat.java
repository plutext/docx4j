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

package org.docx4j.samples;



import java.io.FileOutputStream;
import java.io.FileInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;

/* 
 * This is an example of loading a file from the pkg format Word 2007
 * can produce, and optionally, saving it as a docx zip file.
 * 
 * This is a convenient format for loading certain test cases.
 * 
 * TODO If this is extended to read the pkg properly (ie
 * follow the relationships, like open from zip file does), we
 * might consider moving it to org.docx4j.openpackaging.io
 * 
 * */
public class ImportFromPackageFormat {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/docProps-out.pkg";
				
		// Do we want to save output? 
		boolean save = true;
		// If so, where to?
		String outputfilepath = System.getProperty("user.dir") + "/sample-docs/just-created.docx";		
		
		
		// Create a package
		WordprocessingMLPackage wmlPackage = new WordprocessingMLPackage();
		
		try {
			JAXBContext jc = Context.jc;
			Unmarshaller u = jc.createUnmarshaller();
			u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

			org.docx4j.wml.Package wmlPackageEl = (org.docx4j.wml.Package)u.unmarshal(
					new javax.xml.transform.stream.StreamSource(new FileInputStream(inputfilepath))); 

			org.docx4j.wml.Document wmlDocument = null;
			org.docx4j.wml.Styles wmlStyles = null;
			for (org.docx4j.wml.Package.Part p : wmlPackageEl.getPart() ) {
				
				if (p.getXmlData().getDocument()!= null) {
					wmlDocument = p.getXmlData().getDocument();
				}				
				if (p.getXmlData().getStyles()!= null) {
					wmlStyles = p.getXmlData().getStyles();
				}				
			}
			
			// Create main document part
			MainDocumentPart wordDocumentPart = new MainDocumentPart();		
			// Put the content in the part				
			wordDocumentPart.setJaxbElement(wmlDocument);
			// Add the main document part to the package relationships
			// (creating it if necessary)
			wmlPackage.addTargetPart(wordDocumentPart);
			
			
			// That handled the Main Document Part; now set the Style part.
			StyleDefinitionsPart stylesPart = new StyleDefinitionsPart(); 
			stylesPart.setJaxbElement(wmlStyles);
			// Add the styles part to the main document part relationships
			// (creating it if necessary)
			wordDocumentPart.addTargetPart(stylesPart); // NB - add it to main doc part, not package!
			
			
			// Now that its loaded properly, lets just save it
			
			if (save) {
				SaveToZipFile saver = new SaveToZipFile(wmlPackage);
				saver.save(outputfilepath);
				System.out.println( "\n\n .. written to " + outputfilepath);
			} else {
				// Display its contents 
				System.out.println( "\n\n..done " );
			}
			
			
		} catch (Exception exc) {
			exc.printStackTrace();
			throw new RuntimeException(exc);
		}
				
	}
	
	

}
