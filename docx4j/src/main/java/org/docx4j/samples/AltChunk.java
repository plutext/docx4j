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

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTAltChunk;


public class AltChunk {
	
	public static void main(String[] args) throws Exception {
		
		boolean ADD_TO_HEADER = true;
		HeaderPart hp = null;
		
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/sample-docx.xml";
		
		String chunkPath = System.getProperty("user.dir") + "/sample-docs/word/chunk.docx";
		
		boolean save = true;
		String outputfilepath = System.getProperty("user.dir") + "/altChunk_out.docx";
		
		
		// Open a document from the file system
		// 1. Load the Package
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		MainDocumentPart main = wordMLPackage.getMainDocumentPart();
		
		if (ADD_TO_HEADER) {
			hp = wordMLPackage.getDocumentModel().getSections().get(0).getHeaderFooterPolicy().getDefaultHeader();
		}
		
		AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(new PartName("/chunk.docx") );
		afiPart.setBinaryData(
				new FileInputStream(chunkPath) );
		
		afiPart.setContentType(new ContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml")); //docx
		//afiPart.setContentType(new ContentType("application/xhtml+xml")); //xhtml

		Relationship altChunkRel = null;
		if (ADD_TO_HEADER) {
			altChunkRel = hp.addTargetPart(afiPart);
		} else {
			altChunkRel = main.addTargetPart(afiPart);			
		}
		
		CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk();
		ac.setId(altChunkRel.getId());

		if (ADD_TO_HEADER) {
			hp.getJaxbElement().getEGBlockLevelElts().add(ac);
		} else {
			main.addObject(ac);
		}
		
		// Save it
		
		if (save) {		
			SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
			saver.save(outputfilepath);
			System.out.println("Saved " + outputfilepath);
		}
	}
		

}
