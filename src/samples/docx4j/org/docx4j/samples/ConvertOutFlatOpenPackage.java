/*
 *  Copyright 2007-2012, Plutext Pty Ltd.
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

import org.docx4j.convert.out.flatOpcXml.FlatOpcXmlCreator;
import org.docx4j.openpackaging.packages.OpcPackage;


/**
 * Convert a docx/pptx/xlsx to 'Flat OPC XML' format,
 * which Word/Powerpoint can happily read, and which 
 * is convenient for editing in an XML editor.
 * 
 * @author jharrop
 *
 */
public class ConvertOutFlatOpenPackage extends AbstractSample {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
			inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/sample-docx.docx";
		}
		
		// Do we want to save output? 
		boolean save = true;
		// If so, whereto?
		outputfilepath = System.getProperty("user.dir") + "/OUT_ConvertOutFlatOpenPackage.xml";
		
		// Open a document from the file system
		OpcPackage wmlPackage = OpcPackage.load(new java.io.File(inputfilepath));
		
		if (save) {
			wmlPackage.save(new File(outputfilepath));
			System.out.println( "\n\n .. written to " + outputfilepath);
		} else {
		   	// Create a org.docx4j.wml.Package object
			FlatOpcXmlCreator worker = new FlatOpcXmlCreator(wmlPackage);

			// Display its contents 
			worker.marshal(System.out);				
		}
		
	}

}
