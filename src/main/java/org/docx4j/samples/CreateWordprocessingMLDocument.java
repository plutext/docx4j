/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */


package org.docx4j.samples;

import java.io.File;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.SaveToZipFile;

/**
 * Creates a WordprocessingML document from scratch. 
 * 
 * @author Jason Harrop
 * @version 1.0
 */
public class CreateWordprocessingMLDocument {

	public static void main(String[] args) throws Exception {
		
		System.out.println( "Creating package..");
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createTestPackage();
		System.out.println( ".. done!");
		
		//injectDocPropsCustomPart(wordMLPackage);
		
		// Now save it 
		wordMLPackage.save(new java.io.File("/tmp/result.docx") );
		
		System.out.println("Done.");
				
	}
	
	public static void injectDocPropsCustomPart(WordprocessingMLPackage wordMLPackage) {
		
		try {
			org.docx4j.openpackaging.parts.DocPropsCustomPart docPropsCustomPart = new org.docx4j.openpackaging.parts.DocPropsCustomPart();
			
			java.io.InputStream is = new java.io.FileInputStream("/tmp/custompart.xml" );
			
			docPropsCustomPart.unmarshal(is);
			
			wordMLPackage.addTargetPart(docPropsCustomPart);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}
