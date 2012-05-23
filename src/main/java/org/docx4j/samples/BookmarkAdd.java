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


import java.math.BigInteger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.P.Hyperlink;
import org.docx4j.wml.R;


public class BookmarkAdd  extends AbstractSample {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();

		String outputfilepath = System.getProperty("user.dir")
				+ "/OUT_bookmarkAdd.docx";;		
						
		wordMLPackage.getMainDocumentPart().addParagraphOfText("x");
		wordMLPackage.getMainDocumentPart().addParagraphOfText("x");
		wordMLPackage.getMainDocumentPart().addParagraphOfText("hello world");
		P p = (P)wordMLPackage.getMainDocumentPart().getContent().get(2);
		R r = (R)p.getContent().get(0);
		
		String bookmarkName = "abcd"; 
		bookmarkRun(p,r, bookmarkName, 123);

		wordMLPackage.getMainDocumentPart().addParagraphOfText("x");
		wordMLPackage.getMainDocumentPart().addParagraphOfText("x");
		
		// Now add an internal hyperlink to it
		Hyperlink h = MainDocumentPart.hyperlinkToBookmark(bookmarkName, "link to bookmark");
		wordMLPackage.getMainDocumentPart().addParagraphOfText("some text").getContent().add(h);
		
		System.out.println( XmlUtils.marshaltoString(p, true)  );
		
		SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
		saver.save(outputfilepath);
	}
	
	
	/**
	 * Surround the specified r in the specified p
	 * with a bookmark (with specified name and id)
	 * @param p
	 * @param r
	 * @param name
	 * @param id
	 */
	public static void bookmarkRun(P p, R r, String name, int id) {
		
		// Find the index
		int index = p.getContent().indexOf(r);
	
		if (index<0) {
			System.out.println("P does not contain R!");
			return;
		}
		
		ObjectFactory factory = Context.getWmlObjectFactory();
		BigInteger ID = BigInteger.valueOf(id);
		
		
		// Add bookmark end first
		CTMarkupRange mr = factory.createCTMarkupRange();
		mr.setId(ID);
		JAXBElement<CTMarkupRange> bmEnd = factory.createBodyBookmarkEnd(mr);
		p.getContent().add(index+1, bmEnd); 
		
		// Next, bookmark start
		CTBookmark bm = factory.createCTBookmark();
		bm.setId(ID);
		bm.setName(name);		
		JAXBElement<CTBookmark> bmStart =  factory.createBodyBookmarkStart(bm);
		p.getContent().add(index, bmStart);
		
	}

}
