/*
 *  Copyright 2016, Plutext Pty Ltd.
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


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.services.client.ConversionException;
import org.docx4j.services.client.Converter;
import org.docx4j.services.client.ConverterHttp;
import org.docx4j.services.client.Format;
import org.docx4j.toc.TocException;
import org.docx4j.toc.TocGenerator;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Use Plutext's commercial PDF Converter to count the number of 
 * pages in the input docx, then write that value into the document properties
 * 
 * @author jharrop
 *
 */
public class PageCounter extends AbstractSample {
	
	private static Logger log = LoggerFactory.getLogger(PageCounter.class);		
	
	public static void main(String[] args) throws Exception {

		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
	    	inputfilepath = System.getProperty("user.dir") + "/sample-docs/sample-docx.docx";
		}
		System.out.println(inputfilepath);	    	
		
		
		// Load the docx
		WordprocessingMLPackage wordMLPackage = Docx4J.load(new java.io.File(inputfilepath));
		
		// Add a bookmark at the end
		P p = new P();
		R r = new R();
		p.getContent().add(r);
		wordMLPackage.getMainDocumentPart().getContent().add(p);
		String bookmarkName = "d4jEnd"; 
		bookmarkRun(p,r, bookmarkName, 123); // need to do a better job of creating a unique ID!
		
		// Get the corresponding number
		Map<String, Integer> map = getPageNumbersMapViaService(wordMLPackage);
		
		// OPtionally write it
		wordMLPackage.addDocPropsExtendedPart();
		org.docx4j.openpackaging.parts.DocPropsExtendedPart docPropsExtendedPart = wordMLPackage.getDocPropsExtendedPart();
		org.docx4j.docProps.extended.Properties extendedProps = (org.docx4j.docProps.extended.Properties)docPropsExtendedPart.getJaxbElement(); 
		extendedProps.setPages(map.get(bookmarkName));
		
		// Save it
		String outputfilepath = System.getProperty("user.dir") + "/OUT_PageCounter.docx";
		Docx4J.save(wordMLPackage, new File(outputfilepath), Docx4J.FLAG_NONE); //(FLAG_NONE == default == zipped docx)
		
		System.out.println("Saved: " + outputfilepath);
	}
	
    private static Map<String, Integer> getPageNumbersMapViaService(WordprocessingMLPackage wordMLPackage) throws TocException {
    	
		ByteArrayOutputStream tmpDocxFile = new ByteArrayOutputStream(); 
		try {
			Docx4J.save(wordMLPackage, tmpDocxFile, Docx4J.FLAG_SAVE_ZIP_FILE);
		} catch (Exception e) {
			throw new TocException("Error saving pkg to stream; " + e.getMessage(),e);
		}  
		
		// Not working?  Check your input
//		try {
//			File save_ToCEntries = new File( System.getProperty("java.io.tmpdir") 
//					+ "/foo_tocEntries.docx"); 
//			Docx4J.save(wordMLPackage, save_ToCEntries, Docx4J.FLAG_SAVE_ZIP_FILE);
//			log.debug("Saved: " + save_ToCEntries);
//		} catch (Exception e) {
//			log.error(e.getMessage(), e);
//		}
    	    	
    	// 
		String documentServicesEndpoint 
			= Docx4jProperties.getProperty("com.plutext.converter.URL", 
					"http://localhost:9016/v1/00000000-0000-0000-0000-000000000000/convert");    	
//			"https://converter-eval.plutext.com:443/v1/00000000-0000-0000-0000-000000000000/convert");
		
		Converter converter = new ConverterHttp(documentServicesEndpoint); 

		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
			
		try {
			converter.convert(tmpDocxFile.toByteArray(), Format.DOCX, Format.TOC, baos);
		} catch (ConversionException e) {
			
			if (e.getResponse()!=null) {

				throw new TocException("Error in toc web service at " 
						+ documentServicesEndpoint + "\n HTTP response: " 
						+ e.getResponse().getStatusLine().getStatusCode()
						+ " " + e.getResponse().getStatusLine().getReasonPhrase() ,e);
				
			} else if (e.getMessage()==null){			
				throw new TocException("Error in toc web service at " 
						+ documentServicesEndpoint + "\n",e);
			} else {
				throw new TocException("Error in toc web service at " 
						+ documentServicesEndpoint + "\n" + e.getMessage(),e);				
			}
			
		} catch (Exception e) {
			throw new TocException("Error in toc web service at " 
						+ documentServicesEndpoint + "\n" + e.getMessage(),e);
		}
		
		/* NEW DocumentServices 2.0-x
		 * {"bookmarks":{"_Toc299924107":"68","_Toc318272803":"1"}}
		 */
		
		byte[] json = baos.toByteArray();
//		System.out.println(json);
		
		Map<String,Integer> map = new HashMap<String,Integer>();
		ObjectMapper m = new ObjectMapper();
		
		JsonNode rootNode;
		try {
			rootNode = m.readTree(json);
		} catch (Exception e) {
//			System.err.println(json);
			throw new TocException("Error reading toc json; \n" + json + "\n"+ e.getMessage(),e);
		}
		JsonNode bookmarksNode = rootNode.path("bookmarks");
		
		Iterator<Map.Entry<String, JsonNode>> bookmarksValueObj = bookmarksNode.fields();
		while (bookmarksValueObj.hasNext()) {
			Map.Entry<String, JsonNode> entry = bookmarksValueObj.next();
//			System.out.println(entry.getKey());
			if (entry.getValue()==null) {
				log.warn("Null page number computed for bookmark " + entry.getKey());
			}
			map.put(entry.getKey(), new Integer(entry.getValue().asInt()));
		}		
		
//		System.out.println("map size " + map.size());
//		System.out.println(map);
			
		return map;

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
