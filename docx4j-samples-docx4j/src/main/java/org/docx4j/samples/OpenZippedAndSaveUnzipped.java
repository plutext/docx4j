/*
 *  Copyright 2012, Plutext Pty Ltd.
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

import javax.xml.bind.JAXBContext;

import org.docx4j.openpackaging.io3.Save;
import org.docx4j.openpackaging.io3.stores.UnzippedPartStore;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;


/**
 * Example of saving a docx unzipped.
 * 
 * @author jharrop
 * @since 3.0
 */
public class OpenZippedAndSaveUnzipped extends AbstractSample {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 

	public static void main(String[] args) throws Exception {

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
	    	inputfilepath = System.getProperty("user.dir") + "/sample-docs/sample-docx.docx";
		}
		System.out.println(inputfilepath);	    	
		
		
		// Load the docx
		WordprocessingMLPackage wordMLPackage = (WordprocessingMLPackage)OpcPackage.load(new java.io.File(inputfilepath));
				
		// Save it unzipped		
		File baseDir = new File(System.getProperty("user.dir") + "/OUT"); 
		baseDir.mkdir();
		
		UnzippedPartStore ups = new UnzippedPartStore(baseDir);
		ups.setSourcePartStore(wordMLPackage.getSourcePartStore());
		Save saver = new Save(wordMLPackage, ups);
		
		saver.save(null);
	}
		

}
