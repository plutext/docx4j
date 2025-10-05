/*
 *  Copyright 2007-2025, Plutext Pty Ltd.
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


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

import jakarta.xml.bind.JAXBContext;


/**
 * Example showing how to constrain the amount of time
 * available in which a docx package can be loaded. 
 * 
 * @author jharrop
 *
 */
public class OpenMainDocumentResourceLimited {

	public static JAXBContext context = org.docx4j.jaxb.Context.jc;

	public static void main(String[] args) throws Exception {

		String inputfilepath = System.getProperty("user.dir")
					+ "/sample-docs/sample-docx.docx";

		long start = System.currentTimeMillis();

		WordprocessingMLPackage wordMLPackage;
		ExecutorService es = Executors.newSingleThreadExecutor();
		Future<?> f = es.submit(() -> 
			Docx4J.load(new java.io.File(inputfilepath))
//			/* or */ WordprocessingMLPackage.load(new java.io.File(inputfilepath))
			);
		try {
			wordMLPackage = (WordprocessingMLPackage) f.get(200, TimeUnit.MILLISECONDS);
		} catch (TimeoutException te) {
		  f.cancel(true);
		  throw new Docx4JException("Loading timed out");
		}

		
		long end= System.currentTimeMillis();
		
		System.out.println( (end-start) + " ms");
		
//		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
//
//		// Uncomment to see the raw XML
//		System.out.println(XmlUtils.marshaltoString(documentPart.getJaxbElement(), true, true));
		
	}
}
