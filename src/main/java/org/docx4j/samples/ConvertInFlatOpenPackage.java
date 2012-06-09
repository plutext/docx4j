/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.samples;



import java.io.FileInputStream;

import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/* 
 * This is an example of loading a file from the pkg format Word 2007
 * can produce, and optionally, saving it as a docx zip file.
 * 
 * This is a convenient format for loading certain test cases.
 * 
 * */
public class ConvertInFlatOpenPackage extends AbstractSample {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
			inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/sample-docx.xml";
		}
							
		String outputfilepath = System.getProperty("user.dir") + "/OUT_ConvertInFlatOpenPackage.docx";
		
		try {
			// First, load Flat OPC Package from file
			org.docx4j.convert.in.FlatOpcXmlImporter importer 
				= new org.docx4j.convert.in.FlatOpcXmlImporter( new FileInputStream(inputfilepath) ); 

			WordprocessingMLPackage wmlPackage = (WordprocessingMLPackage)importer.get(); 
			
			// All done ..  save the WordprocessingMLPackage
			SaveToZipFile saver = new SaveToZipFile(wmlPackage);
			saver.save(outputfilepath);
			System.out.println( "\n\n .. written to " + outputfilepath);
			
			
		} catch (Exception exc) {
			exc.printStackTrace();
			throw new RuntimeException(exc);
		}
				
	}
	
	

}
