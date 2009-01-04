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

import java.io.File;
import java.io.IOException;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.SaveToZipFile;

import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.relationships.Relationship;
import org.docx4j.openpackaging.parts.relationships.Namespaces;

/**
 * Creates a WordprocessingML document from scratch. 
 * 
 * @author Jason Harrop
 * @version 1.0
 */
public class AddImage {

	public static void main(String[] args) throws Exception {
		
		System.out.println( "Creating package..");
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		
		File file = new File("/home/dev/lanl/testing/fig1.pdf" );
		
		// Our utility method wants that as a byte array
		
		java.io.InputStream is = new java.io.FileInputStream(file );
        long length = file.length();    
        // You cannot create an array using a long type.
        // It needs to be an int type.
        if (length > Integer.MAX_VALUE) {
        	System.out.println("File too large!!");
        }
        byte[] bytes = new byte[(int)length];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            System.out.println("Could not completely read file "+file.getName());
        }
        is.close();
        
        
		// TODO - an algorithm for generating the partname
		
		org.docx4j.utils.ImageUtils.createImagePart(wordMLPackage, bytes, "/word/media/image.png");
		
		// Now save it 
		wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/result.docx") );
		
		System.out.println("Done.");
				
	}
	
		
}
