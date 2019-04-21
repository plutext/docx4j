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


import org.docx4j.XmlUtils;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;


/**
 * Simple example showing how to see the
 * document's XML (MainDocumentPart).
 * 
 * If you want to see all the parts
 * as a Flat OPC XML document, see 
 * the ExportInPackageFormat sample.
 * 
 * @author jharrop
 *
 */
public class DisplayMainDocumentPartXml extends AbstractSample {

		public static void main(String[] args) throws Exception {

			/*
			 * You can invoke this from an OS command line with something like:
			 * 
			 *   java -cp docx4j-6.1.0-shaded.jar org.docx4j.samples.DisplayMainDocumentPartXml sample-docs/word/sample-docx.docx 
			 * 
			 */

			try {
				getInputFilePath(args);
			} catch (IllegalArgumentException e) {
				inputfilepath = System.getProperty("user.dir")
						+ "/sample-docs/sample-docx.xml";
			}
			
			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
			MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();

			// Remove gumpf so it is easier to read
			VariablePrepare.prepare(wordMLPackage);
			
		   	// Pretty print the main document part
			System.out.println(
					XmlUtils.marshaltoString(documentPart.getJaxbElement(), true, true) );
			
		}
		
		

	}


