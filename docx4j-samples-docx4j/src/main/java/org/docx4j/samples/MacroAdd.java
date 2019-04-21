/*
 *  Copyright 2014, Plutext Pty Ltd.
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

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

/**
 * How to add a macro 
 * 
 * @author Jason Harrop
 */
public class MacroAdd  {
	
	static org.docx4j.wml.ObjectFactory wmlObjectFactory = new org.docx4j.wml.ObjectFactory();

	public static void main(String[] args) throws Exception {
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart wordDocumentPart = wordMLPackage.getMainDocumentPart();
		
	      // Get vbaProject.bin, and attach it to wordDocumentPart
	      java.io.InputStream is = new java.io.FileInputStream(System.getProperty("user.dir") + "/vbaProject.bin" );

	      org.docx4j.openpackaging.parts.WordprocessingML.VbaProjectBinaryPart vbaProject 
	         = new org.docx4j.openpackaging.parts.WordprocessingML.VbaProjectBinaryPart();
	      vbaProject.setBinaryData(is);
	      wordDocumentPart.addTargetPart(vbaProject);
	      
	      // Get /word/vbaData.xml, and attach it to vbaProject
	      org.docx4j.openpackaging.parts.WordprocessingML.VbaDataPart vbaData 
	         = new org.docx4j.openpackaging.parts.WordprocessingML.VbaDataPart();
	      java.io.InputStream is2 = new java.io.FileInputStream(System.getProperty("user.dir") + "/vbaData.xml" );
	      vbaData.unmarshal(is2);
	      //vbaData.setDocument( is2 );
	      
	      vbaProject.addTargetPart( vbaData);
	         
	      // Change the Word document's content type!
	      wordDocumentPart.setContentType( new org.docx4j.openpackaging.contenttype.ContentType(
	            org.docx4j.openpackaging.contenttype.ContentTypes.WORDPROCESSINGML_DOCUMENT_MACROENABLED ) );
	      // .. but that's a dumb setter right now :(
	      org.docx4j.openpackaging.contenttype.ContentTypeManager ctm 
	         = wordMLPackage.getContentTypeManager();
	      org.docx4j.openpackaging.parts.PartName partName 
	         = wordDocumentPart.getPartName();
	      
	      ctm.removeContentType( partName  );
	      ctm.addOverrideContentType( new java.net.URI("/word/document.xml"), 
	            org.docx4j.openpackaging.contenttype.ContentTypes.WORDPROCESSINGML_DOCUMENT_MACROENABLED);
	      
		// Save it
		String filename = System.getProperty("user.dir") + "/OUT_MacroAdd.docm";
		wordMLPackage.save(new java.io.File(filename) );
		System.out.println("Saved " + filename);
						
	}
	
	
	
}
