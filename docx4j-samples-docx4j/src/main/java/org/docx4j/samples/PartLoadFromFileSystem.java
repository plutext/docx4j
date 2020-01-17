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

import java.io.FileInputStream;
import java.io.InputStream;

import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.io.Load;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;

/**
 * Import foreign parts, relying on docx4j to work out which part
 * to create.
 * 
 * If you know which part you want, it is simpler / more comprehensible
 * to unmarshall an input stream, then set the content of a new part
 * to that.
 * 
 * @author Jason Harrop
 */
public class PartLoadFromFileSystem {

	public static void main(String[] args) throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		String path_in = System.getProperty("user.dir") + "/src/test/resources/parts/";
		
		// Need to know how what type of part to map to		
		InputStream in = new FileInputStream(path_in + "[Content_Types].xml");
		ContentTypeManager externalCtm = new ContentTypeManager();
		externalCtm.parseContentTypesFile(in);
		
		// Example of a part which become a rel of the word document
		in = new FileInputStream(path_in + "settings.xml");
		attachForeignPart(wordMLPackage, wordMLPackage.getMainDocumentPart(),
				externalCtm, "word/settings.xml", in);

		// Example of a part which become a rel of the package
		in = new FileInputStream(path_in + "app.xml");
		attachForeignPart(wordMLPackage, wordMLPackage,
				externalCtm, "docProps/app.xml", in);
		

		wordMLPackage.save(new java.io.File(
				System.getProperty("user.dir") + "/OUT_PartLoadFromFileSystem.docx") );
				
	}
	
	
	public static void attachForeignPart( WordprocessingMLPackage wordMLPackage, 
			Base attachmentPoint,
			ContentTypeManager foreignCtm, 
			String resolvedPartUri, InputStream is) throws Exception{
		
		
		Part foreignPart = Load.getRawPart(is, foreignCtm,  resolvedPartUri, null);
			// the null means this won't work for an AlternativeFormatInputPart 
		attachmentPoint.addTargetPart(foreignPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS);
		// Add content type
		ContentTypeManager packageCtm = wordMLPackage.getContentTypeManager();
		packageCtm.addOverrideContentType(foreignPart.getPartName().getURI(), foreignPart.getContentType());
		
		System.out.println("Attached foreign part: " + resolvedPartUri);
		
	}
	
}
