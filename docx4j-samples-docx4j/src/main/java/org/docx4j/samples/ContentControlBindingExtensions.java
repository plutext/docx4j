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

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;

import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.model.datastorage.BindingHandler;
import org.docx4j.model.datastorage.BindingHyperlinkResolverForOpenAPI3;
import org.docx4j.model.datastorage.DocxFetcher;
import org.docx4j.model.datastorage.ValueInserterPlainTextForOpenAPI3;
import org.docx4j.model.datastorage.ValueInserterPlainTextImpl;
import org.docx4j.model.datastorage.XsltProvider;
import org.docx4j.model.datastorage.XsltProviderImpl;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.utils.XPathFactoryUtil;
import org.w3c.dom.Document;


/**
 * This sample demonstrates populating content controls
 * from a custom xml part (based on the xpaths given
 * in the content controls), via FLAG_BIND_BIND_XML
 * 
 * Word does this itself automatically, for if there is
 * a w:databinding element in the sdtPr.
 * 
 * However, out of the box, Word doesn't allow for
 * repeating things (table rows, paragraphs etc), nor
 * conditional inclusion/exclusion.
 * 
 * The OpenDoPE conventions support that; and this sample 
 * demonstrates docx4j's implementation of that.
 * 
 * In practice you'll want the XML part to be injected at runtime.  
 * For that, use FLAG_NONE, or include FLAG_BIND_INSERT_XML.
 * 
 * See description of flags below.
*/
public class ContentControlBindingExtensions {
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc; 

	static String filepathprefix;
	
	public static void main(String[] args) throws Exception {

		// Without Saxon, you are restricted to XPath 1.0
		boolean USE_SAXON = true; // set this to true; add Saxon to your classpath, and uncomment below 
		
		String input_DOCX = System.getProperty("user.dir") + "/sample-docs/databinding/invoice.docx";
//		String input_DOCX = System.getProperty("user.dir") + "/sample-docs/databinding/invoice_Saxon_XPath2.docx";
		
		String input_XML = System.getProperty("user.dir") + "/sample-docs/databinding/invoice-data.xml";

		
		// resulting docx
		String OUTPUT_DOCX = System.getProperty("user.dir") + "/OUT_ContentControlsMergeXML.docx";
		
//		if (USE_SAXON) XPathFactoryUtil.setxPathFactory(
//		        new net.sf.saxon.xpath.XPathFactoryImpl());
		

		// Load input_template.docx
		WordprocessingMLPackage wordMLPackage = Docx4J.load(new File(input_DOCX));

		// Open the xml stream
		FileInputStream xmlStream = new FileInputStream(new File(input_XML));

		// Do the binding:
		// FLAG_NONE means that all the steps of the binding will be done,
		// otherwise you could pass a combination of the following flags:
		// FLAG_BIND_INSERT_XML: inject the passed XML into the document
		// FLAG_BIND_BIND_XML: bind the document and the xml (including any OpenDope handling)
		// FLAG_BIND_REMOVE_SDT: remove the content controls from the document (only the content remains)
		// FLAG_BIND_REMOVE_XML: remove the custom xml parts from the document 
		
		// BindingHyperlinkResolver is used by default
		// BindingHandler.setHyperlinkResolver(new BindingHyperlinkResolverForOpenAPI3());
		BindingHandler.getHyperlinkResolver().setHyperlinkStyle("Hyperlink");
		
		// Defaults to ValueInserterPlainTextImpl()
		// BindingHandler.setValueInserterPlainText(new ValueInserterPlainTextForOpenAPI3());
			
		//Docx4J.bind(wordMLPackage, xmlStream, Docx4J.FLAG_NONE);
		//If a document doesn't include the Opendope definitions, eg. the XPathPart,
		//then the only thing you can do is insert the xml
		//the example document binding-simple.docx doesn't have an XPathPart....
		Docx4J.bind(wordMLPackage, xmlStream, Docx4J.FLAG_BIND_INSERT_XML | Docx4J.FLAG_BIND_BIND_XML);// | Docx4J.FLAG_BIND_REMOVE_SDT);
		
		// That won't invoke any XSLT Finisher (available in 6.1.0).  For that, you need to use signature:
		//  bind(WordprocessingMLPackage wmlPackage, Document xmlDocument, int flags, DocxFetcher docxFetcher,
		//  		XsltProvider xsltProvider, String xsltFinisherfilename, Map<String, Map<String, Object>> finisherParams)
		// Here is an example of using it on invoice.docx to shade a table row:
/*	    // Signature requires Document, not stream
  		Document xmlDoc = XmlUtils.getNewDocumentBuilder().parse(xmlStream);
  		// Specify the finished XSLT we want to us
	    Docx4jProperties.setProperty("docx4j.model.datastorage.XsltFinisher.xslt",  "XsltFinisherInvoice.xslt");
	    // Configure properties
	    Map<String, Map<String, Object>> finisherParams = new HashMap<String, Map<String, Object>>(); 
		Map<String, Object> tcPrParams = new HashMap<String, Object>();
		tcPrParams.put("fillColor", "5250D0"); // color to use for shading
		finisherParams.put("t_r",  tcPrParams);
		// Now perform the binding	    
		Docx4J.bind(wordMLPackage, xmlDoc, Docx4J.FLAG_BIND_INSERT_XML | Docx4J.FLAG_BIND_BIND_XML,
				null, new XsltProviderImpl(), null, finisherParams);
*/		
		//Save the document 
		Docx4J.save(wordMLPackage, new File(OUTPUT_DOCX), Docx4J.FLAG_NONE);
		System.out.println("Saved: " + OUTPUT_DOCX);
	}	
	
				

}
