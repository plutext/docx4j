/*
 *  Copyright 2020, Plutext Pty Ltd.
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

package org.pptx4j.samples;


import java.io.File;
import java.io.FileOutputStream;

import org.docx4j.openpackaging.io3.SaveSlides;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Save identified slides (0-based index) only. 
 * 
 * @author jharrop
 * @since 8.1.6
 */
public class SaveSelectedSlides {
	
	protected static Logger log = LoggerFactory.getLogger(SaveSelectedSlides.class);
			
	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir") + "/your_input.pptx";
		
		PresentationMLPackage presentationMLPackage = 
			(PresentationMLPackage)OpcPackage.load(new java.io.File(inputfilepath));
		
		System.out.println("\n\n saving .. \n\n");
		String outputfilepath = System.getProperty("user.dir") + "/OUT_SaveSelectedSlides.pptx";
		FileOutputStream fos = new FileOutputStream(new File(outputfilepath)); 
		
		int[] slides = {2,6,4};
		SaveSlides saver = new SaveSlides(presentationMLPackage, slides );
		saver.save(fos);
		
		System.out.println("\n\n done .. \n\n");
		
	}	
}
