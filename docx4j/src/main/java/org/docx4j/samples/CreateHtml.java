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
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.docx4j.convert.out.html.AbstractHtmlExporter;
import org.docx4j.convert.out.html.HtmlExporter;
import org.docx4j.convert.out.html.HtmlExporterNG;
import org.docx4j.convert.out.html.HtmlExporterNG2;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

public class CreateHtml {
	    
	    public static void main(String[] args) 
	            throws Exception {
	    	
	    	boolean save = true;	    	
	    	boolean useHtmlExporterNG = true;

	    	String inputfilepath = System.getProperty("user.dir") + "/sample-docs/sample-docx.xml";	    	
	    	
			System.out.println(inputfilepath);
			WordprocessingMLPackage wordMLPackage;
			if (inputfilepath.endsWith(".xml")) {
				
				JAXBContext jc = Context.jcXmlPackage;
				Unmarshaller u = jc.createUnmarshaller();
				u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());

				org.docx4j.xmlPackage.Package wmlPackageEl = (org.docx4j.xmlPackage.Package)((JAXBElement)u.unmarshal(
						new javax.xml.transform.stream.StreamSource(new FileInputStream(inputfilepath)))).getValue(); 

				org.docx4j.convert.in.FlatOpcXmlImporter xmlPackage = new org.docx4j.convert.in.FlatOpcXmlImporter( wmlPackageEl); 

				wordMLPackage = (WordprocessingMLPackage)xmlPackage.get(); 
			
			} else {
				wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
			}
	    	
			AbstractHtmlExporter exporter;
			if (useHtmlExporterNG) {
				// note the *2* here
				exporter = new HtmlExporterNG2(); 			
			} else {
				exporter = new HtmlExporter();
			}
			
			OutputStream os; 
			if (save) {
				os = new java.io.FileOutputStream(inputfilepath + ".html");
			} else {
				os = System.out;

			}
			
			javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(os);
			exporter.html(wordMLPackage, result, 
   					inputfilepath + "_files");
			if (save) {
				System.out.println("Saved: " + inputfilepath + ".html using " +  exporter.getClass().getName() );
			}
	        	        
	    }
	}