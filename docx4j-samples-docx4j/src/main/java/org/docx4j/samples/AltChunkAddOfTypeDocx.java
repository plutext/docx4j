/*
 *  Copyright 2007-2019, Plutext Pty Ltd.
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
import java.io.FileInputStream;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;


/**
 * This adds an AlternativeFormatInputPart
 * containing a docx.
 * 
 * Word will convert it to normal docx content
 * when it is first opened.  Or you could 
 * use Plutext's MergeDocx to do this.
 * 
 * @author jharrop
 *
 */
public class AltChunkAddOfTypeDocx {
	
	public static void main(String[] args) throws Exception {
		
		// Configure:
		
		boolean CREATE_HOST_DOCX = true;
		/* if not, then use */ String inputfilepath = System.getProperty("user.dir") + "/sample-docs/sample-docx.xml";
		
		boolean ADD_TO_HEADER = false;
		
		String chunkPath = System.getProperty("user.dir") + "/wide.docx";
		
		boolean save = true;
		String outputfilepath = System.getProperty("user.dir") + "/OUT_AltChunkAddOfTypeDocx.docx";
		
		
		// Now do it...
		
		WordprocessingMLPackage hostDocxPkg = null;
		if (CREATE_HOST_DOCX) {
			hostDocxPkg = WordprocessingMLPackage.createPackage();			
			hostDocxPkg.getMainDocumentPart().addParagraphOfText("before");
		} else {
			// Open a document from the file system
			hostDocxPkg = WordprocessingMLPackage.load(new File(inputfilepath));
		}
				
		
		if (ADD_TO_HEADER) {
			HeaderPart hp = hostDocxPkg.getDocumentModel().getSections().get(0).getHeaderFooterPolicy().getDefaultHeader();
			hp.addAltChunk(AltChunkType.WordprocessingML, new FileInputStream(chunkPath) );
		} else {
			MainDocumentPart main = hostDocxPkg.getMainDocumentPart();
			main.addAltChunk(AltChunkType.WordprocessingML, new FileInputStream(chunkPath) );
		}

		if (CREATE_HOST_DOCX) {
			hostDocxPkg.getMainDocumentPart().addParagraphOfText("after");
		}
		
		if (save) {		
			Docx4J.save(hostDocxPkg, new File(outputfilepath));
			System.out.println("Saved " + outputfilepath);
		}
	}
		

}
