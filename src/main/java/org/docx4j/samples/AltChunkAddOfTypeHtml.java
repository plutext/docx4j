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

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;

/**
 * Add HTML, in a way that leaves it up to downstream 
 * application (eg Word) to convert the content to docx content.
 * 
 * Unless you really want the HTML converted by Word,
 * consider converting your HTML to well-formed XHTML,
 * then using docx4j's XHTMLImporter instead.
 * 
 * @author jharrop
 *
 */
public class AltChunkAddOfTypeHtml {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();

		String html = "<html><head><title>Import me</title></head><body><p>Hello World!</p></body></html>"; 

		wordMLPackage.getMainDocumentPart().addAltChunk(AltChunkType.Html, html.getBytes()); 

		wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/test.docx"));		
	}

}
