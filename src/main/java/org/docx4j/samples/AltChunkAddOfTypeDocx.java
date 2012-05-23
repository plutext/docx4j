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

import org.docx4j.openpackaging.io.SaveToZipFile;
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
		
		boolean ADD_TO_HEADER = false;
		
		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/sample-docx.xml";
		String chunkPath = System.getProperty("user.dir") + "/sample-docs/word/chunk.docx";
		
		boolean save = true;
		String outputfilepath = System.getProperty("user.dir") + "/OUT_AltChunkAddOfTypeDocx.docx";
		
		
		// Open a document from the file system
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
				
		if (ADD_TO_HEADER) {
			HeaderPart hp = wordMLPackage.getDocumentModel().getSections().get(0).getHeaderFooterPolicy().getDefaultHeader();
			hp.addAltChunk(AltChunkType.WordprocessingML, new FileInputStream(chunkPath) );
		} else {
			MainDocumentPart main = wordMLPackage.getMainDocumentPart();
			main.addAltChunk(AltChunkType.WordprocessingML, new FileInputStream(chunkPath) );
		}
		
		if (save) {		
			SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
			saver.save(outputfilepath);
			System.out.println("Saved " + outputfilepath);
		}
	}
		

}
