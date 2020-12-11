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

package org.xlsx4j.samples;


import javax.xml.bind.JAXBContext;

import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;


public class XlsxRoundTrip {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/pivot.xlsm";
		
		boolean save = true;
		String outputfilepath = System.getProperty("user.dir") + "/sample-docs/pivot-rtt.xlsm";
				
		// Open a document from the file system
		// 1. Load the Package
		SpreadsheetMLPackage pkg = (SpreadsheetMLPackage)OpcPackage.load(new java.io.File(inputfilepath));
		
		// Save it
		if (save) {		
			SaveToZipFile saver = new SaveToZipFile(pkg);
			saver.save(outputfilepath);
		}
	}
		

}
