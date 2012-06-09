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

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

/**
 * Create a docx containing an XHTML AltChunk,
 * and then convert that to normal docx content.
 * @author jharrop
 *
 */
public class AltChunkXHTMLRoundTrip {

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
		mdp.addParagraphOfText("Paragraph 1");

		// Add the XHTML altChunk
		String xhtml = "<html><head><title>Import me</title></head><body><p>Hello World!</p></body></html>"; 
		mdp.addAltChunk(AltChunkType.Xhtml, xhtml.getBytes());
		
		mdp.addParagraphOfText("Paragraph 3");
		
		// Round trip
		WordprocessingMLPackage pkgOut = mdp.convertAltChunks();
		
		// Display result
		System.out.println(
				XmlUtils.marshaltoString(pkgOut.getMainDocumentPart().getJaxbElement(), true, true));
		
	}

}
