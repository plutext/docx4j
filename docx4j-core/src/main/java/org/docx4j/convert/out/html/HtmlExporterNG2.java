/*
 *  Copyright 2007-2010, Plutext Pty Ltd.
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
package org.docx4j.convert.out.html;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.XmlSerializerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * HtmlExporterNG (now removed - see 
 * http://dev.plutext.org/trac/docx4j/browser/trunk/docx4j/src/main/java/org/docx4j/convert/out/html/HtmlExporterNG.java?rev=1238
 * ) had 2 salient features:
 * 
 * 1.  CSS .xx_pPr and .xx_rPr are created / split out for 
 *     each docx paragraph style
 *     
 * 2.  Each docx style is resolved into its effective
 *     style, by the PropertyResolver
 *     
 * When roundtripping HTML content, this had adverse
 * consequences (neither too bad, but adding to our work):
 * 
 * (a) It is harder to tell what has been added to the
 *     style definition in an editor (if/when that is 
 *     possible)
 *     
 * (b) Have to convert .xx_pPr and .xx_rPr back to xx
 * 
 * Also, we're not really taking advantage of the 
 * ability of CSS to cascade.
 * 
 * So this HtmlExporterNG2 takes a different approach.
 * It creates a css definition for each style in use
 * (either directly or indirectly), and in the
 * document, on the element's class attribute, puts the
 * set of style names in the hierarchy into . 
 *  
 * The CSS rules are written in a depth or breadth
 * first traversal of the style graph, so that  
 * conflicts are handled correctly.
 * 
 * TODO: handle document defaults.
 * 
 * 
 * @author jason
 * @deprecated 
 */
public class HtmlExporterNG2 extends  AbstractHtmlExporter {
	protected static final int DEFAULT_OUTPUT_SIZE = 102400;
	protected static Logger log = LoggerFactory.getLogger(HtmlExporterNG2.class);
	
	WordprocessingMLPackage wmlPackage;
	public void setWmlPackage(WordprocessingMLPackage wmlPackage) {
		this.wmlPackage = wmlPackage;
	}

	HTMLSettings htmlSettings;
	public void setHtmlSettings(HTMLSettings htmlSettings) {
		this.htmlSettings = htmlSettings;
	}
	
	static Templates xslt;		
	/**
	 * org/docx4j/convert/out/html/docx2xhtmlNG2.xslt will be used by default
	 * to transform the docx to html.
	 * This method allows you to use your own xslt instead.
	 * @param xslt
	 */		
	public static void setXslt(Templates xslt) {
		
		HtmlExporterNG2.xslt = xslt;
	}	
	
	// Implement the interface.  	
	
	@Override
	public void output(javax.xml.transform.Result result) throws Docx4JException {
		
		if (wmlPackage==null) {
			throw new Docx4JException("Must setWmlPackage");
		}
		
		if (htmlSettings==null) {
			log.debug("Using empty HtmlSettings");
			htmlSettings = new HTMLSettings();			
		}		
		
		try {
			html(wmlPackage, result, htmlSettings);
		} catch (Exception e) {
			throw new Docx4JException("Failed to create HTML output", e);
		}		
		
	}
	
	// End interface
    
	/** Create an html version of the document. 
	 * 
	 * @param result
	 *            The javax.xml.transform.Result object to transform into 
	 * 
	 * */ 
	public void html(WordprocessingMLPackage wmlPackage,
			javax.xml.transform.Result result, HTMLSettings htmlSettings)
			throws Exception {
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(DEFAULT_OUTPUT_SIZE);
		if ((xslt != null) && (htmlSettings.getCustomXsltTemplates() == null)) {
			htmlSettings.setCustomXsltTemplates(xslt);
		}
		if ((wmlPackage != null) && (htmlSettings.getOpcPackage() == null)) {
			htmlSettings.setOpcPackage(wmlPackage);
		}
		Docx4J.toHTML(htmlSettings, outStream, Docx4J.FLAG_EXPORT_PREFER_XSL);

		// Resolution of doctype eg
		// <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
		// may be extremely slow, so instead of
		// transformer.transform(new StreamSource(new ByteArrayInputStream(bytes)), result);
		// use
		
	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance(); // as opposed to XmlUtils.getNewDocumentBuilder()
	    dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);	
	    dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
	    dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
	    
	    DocumentBuilder db = dbFactory.newDocumentBuilder();
	    // that feature is enough; alternatively, the below also works.  Use both for good measure.
	    db.setEntityResolver(new EntityResolver() {

	        @Override
	        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
	            return new InputSource(new StringReader("")); // Returns a valid dummy source
	            // returning null here is no good; it seems to cause:
	        	//    XMLEntityManager.reset(XMLComponentManager) 
	        	//    XIncludeAwareParserConfiguration(XML11Configuration).resetCommon() 
	            // with the result that this entity resolver is not used!
	        }
	    });	
	    
	    org.w3c.dom.Document doc = null;
	    if (log.isDebugEnabled()) {
		    byte[] bytes = outStream.toByteArray();
		    log.debug(new String(bytes));
		    doc = db.parse(new ByteArrayInputStream(bytes));
	    } else {
	    	doc = db.parse(new ByteArrayInputStream(outStream.toByteArray()));
	    }
		XmlSerializerUtil.serialize(new DOMSource(doc.getDocumentElement()), result, false, false);
			
	}
}
