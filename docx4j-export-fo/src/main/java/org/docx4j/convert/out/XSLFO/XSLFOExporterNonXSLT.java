/*
 *  Copyright 2012, Plutext Pty Ltd.
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
package org.docx4j.convert.out.XSLFO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.configuration.Configuration;
import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FORenderer;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.fo.renderers.FORendererApacheFOP;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * 
 * Running Xalan extension functions on Android is problematic:
 * 
 *   http://stackoverflow.com/questions/10579339/is-it-possible-to-call-a-java-extension-function-from-xalan-on-android
 * 
 * and generating the XSL FO via Xalan + extension functions 
 * is a bit slow,   
 * so this uses TraversalUtils to generate XSL FO output
 * without any need for Xalan or XSLT.
 * 
 * The intent is that this mechanism will ultimately
 * completely replace the Xalan based approach, at which
 * point docx4j will no longer be dependent on Xalan
 * (the XSLT included in Oracle's java is sufficient for
 *  our mc pre processor)
 * 
 * We could use a simple JAXB model of XSL FO, 
 * or org.w3c.dom to construct the XSL FO document. 
 * 
 * This implementation uses org.w3c.dom, since
 * that is easiest based on existing code.
 * 
 * This class might be neater if it used CompoundTraversalUtilVisitorCallback,
 * but it would be less obvious what is going on.  
 * 
 * @author jharrop
 * @deprecated
 */
public class XSLFOExporterNonXSLT {
	private static Logger log = LoggerFactory.getLogger(XSLFOExporterNonXSLT.class);
	protected static final int DEFAULT_OUTPUT_SIZE = 102400;

	FOSettings foSettings = null;
	
	public XSLFOExporterNonXSLT(WordprocessingMLPackage wmlPackage, 
			FOSettings  pdfSettings) {

		foSettings = pdfSettings;
		if ((foSettings.getOpcPackage() == null) && (wmlPackage != null)) {
			foSettings.setOpcPackage(wmlPackage);
		}
	}
	
	/**
	 * Generate XSL FO for the entire MainDocumentPart.
	 * @return
	 */
	public org.w3c.dom.Document export() throws Docx4JException {
	ByteArrayOutputStream outStream = new ByteArrayOutputStream(DEFAULT_OUTPUT_SIZE);
	Document ret = null;
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		try {
			Docx4J.toFO(foSettings, outStream, Docx4J.FLAG_EXPORT_PREFER_NONXSL);
			ret = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(outStream.toByteArray()));
		} catch (Docx4JException e) {
			log.error("Exception exporting document: " + e.getMessage(), e);
		} catch (SAXException e) {
			log.error("Exception parsing document: " + e.getMessage(), e);
		} catch (IOException e) {
			log.error("Exception parsing document: " + e.getMessage(), e);
		}
		return ret;
	}
	

// ========================================================================	
	public void output(Document xslfo, OutputStream os, Configuration fopConfigZZZ) throws Docx4JException {
	FORenderer renderer = FORendererApacheFOP.getInstance();
	String foDocument = XmlUtils.w3CDomNodeToString(xslfo);
		renderer.render(foDocument, foSettings, false, null, os);
	}
}
