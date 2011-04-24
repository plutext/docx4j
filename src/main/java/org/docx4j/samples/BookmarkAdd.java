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
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.io.LoadFromZipFile;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;


public class BookmarkAdd  extends AbstractSample {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
			inputfilepath = System.getProperty("user.dir")
			+ "/simple.docx";
		}
				
		boolean save = false;
		String outputfilepath = "/home/dev/simple-out-rtt2.docx";		
		
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		P p = (P)wordMLPackage.getMainDocumentPart().getJaxbElement().getBody().getEGBlockLevelElts().get(0);
		R r = (R)p.getParagraphContent().get(1);
		
		bookmarkRun(p,r, "abcd", 123);
		
		System.out.println( XmlUtils.marshaltoString(p, true)  );
		
		// Save it		
		if (save) {		
			SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
			saver.save(outputfilepath);
		}
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
		int index = p.getParagraphContent().indexOf(r);
	
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
		p.getParagraphContent().add(index+1, bmEnd); // from 2.7.0, use getContent()
		
		// Next, bookmark start
		CTBookmark bm = factory.createCTBookmark();
		bm.setId(ID);
		bm.setName(name);		
		JAXBElement<CTBookmark> bmStart =  factory.createBodyBookmarkStart(bm);
		p.getParagraphContent().add(index, bmStart);
		
	}

}
