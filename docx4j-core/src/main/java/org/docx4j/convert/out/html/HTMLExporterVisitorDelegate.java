/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
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

import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.common.AbstractVisitorExporterDelegate;
import org.docx4j.convert.out.common.ConversionSectionWrapper;
import org.docx4j.convert.out.common.XsltCommonFunctions;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CTFtnEdn;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HTMLExporterVisitorDelegate extends AbstractVisitorExporterDelegate<HTMLSettings, HTMLConversionContext> {

	protected HTMLExporterVisitorDelegate() {
		super(HTMLExporterVisitorGenerator.GENERATOR_FACTORY);
	}

	@Override
	protected Element createDocumentRoot(HTMLConversionContext conversionContext, Document document) throws Docx4JException {
		return document.createElement("html");
	}

	@Override
	protected void appendDocumentHeader(HTMLConversionContext conversionContext, Document document, Element documentRoot) throws Docx4JException {

		// the same implementation as the XSLT pathway (appendHeadElement); it
		// expects the section iterator on the first section, so the per-section
		// header/footer probe sees section 1
		org.w3c.dom.DocumentFragment head;
		try {
			conversionContext.getSections().next();
			head = XsltHTMLFunctions.appendHeadElement(conversionContext);
		} finally {
			conversionContext.getSections().start();
		}
		if (head != null) {
			documentRoot.appendChild(document.importNode(head, true));
		}
	}

	@Override
	protected Element createDocumentBody(HTMLConversionContext conversionContext, Document document, Element documentRoot) {
	Element body = document.createElement("body");
		appendUserMarkup(conversionContext, document, body, conversionContext.getUserBodyTop());
		return body;
	}

	/**
	 * userBodyTop/userBodyTail: the XSLT injects these raw (disable-output-escaping),
	 * which a DOM can't represent, so they must be well-formed here; parsed as a
	 * fragment, or dropped with a warning.
	 */
	protected void appendUserMarkup(HTMLConversionContext conversionContext,
			Document document, Element parent, String markup) {

		if (markup==null || markup.trim().length()==0) return;
		try {
			Document parsed = org.docx4j.XmlUtils.getNewDocumentBuilder().parse(
					new org.xml.sax.InputSource(new java.io.StringReader(
							"<x>" + markup + "</x>")));
			org.w3c.dom.Node wrapper = parsed.getDocumentElement();
			while (wrapper.getFirstChild()!=null) {
				parent.appendChild(document.importNode(wrapper.getFirstChild(), true));
				wrapper.removeChild(wrapper.getFirstChild());
			}
		} catch (Exception e) {
			conversionContext.getLog().warn("Dropping userBodyTop/userBodyTail: this pathway "
					+ "requires well-formed markup (" + e.getMessage() + ")");
		}
	}

	@Override
	protected Element createSectionRoot(
			HTMLConversionContext conversionContext, Document document,
			ConversionSectionWrapper sectionWrapper, Element currentParent)
			throws Docx4JException {
		// A div for each section might make sense, but docx2xhtml doesn't use it,
		// for this reason we don't do it here.
		return null;
	}

	@Override
	protected void appendSectionHeader(HTMLConversionContext conversionContext,
			Document document, ConversionSectionWrapper sectionWrapper,
			Element currentParent) throws Docx4JException {
		if (XsltCommonFunctions.hasDefaultHeader(conversionContext)) {
			appendPartContent(
				conversionContext, document,
				sectionWrapper, currentParent, 
				"header",
				sectionWrapper.getHeaderFooterPolicy().getDefaultHeader(),
				sectionWrapper.getHeaderFooterPolicy().getDefaultHeader().getJaxbElement().getContent());
		}
	}

	@Override
	protected Element createSectionBody(
			HTMLConversionContext conversionContext, Document document,
			ConversionSectionWrapper sectionWrapper, Element currentParent)
			throws Docx4JException {
    Element	div = document.createElement("div");
    	div.setAttribute("class", "document");
    	return div;
	}

	@Override
	protected void appendSectionFooter(HTMLConversionContext conversionContext,
			Document document, ConversionSectionWrapper sectionWrapper,
			Element currentParent) throws Docx4JException {
		if (XsltCommonFunctions.hasDefaultFooter(conversionContext)) {
			appendPartContent(
				conversionContext, document,
				sectionWrapper, currentParent, 
				"footer",
				sectionWrapper.getHeaderFooterPolicy().getDefaultFooter(),
				sectionWrapper.getHeaderFooterPolicy().getDefaultFooter().getJaxbElement().getContent());
		}
	}

	protected void appendPartContent(HTMLConversionContext conversionContext, 
								  	 Document document, 
								     ConversionSectionWrapper sectionWrapper, 
								     Element currentParent,
								     String className, Part part, List<Object> content) throws Docx4JException {
    Element	div = document.createElement("div");    	
    	currentParent.appendChild(div); 
    	div.setAttribute("class", className);
    	appendPartContent(conversionContext, document, part, content, div);
	}	

	@Override
	protected void appendDocumentFooter(
			HTMLConversionContext conversionContext, Document document,
			Element documentRoot) throws Docx4JException {
	MainDocumentPart mainDocumentPart = conversionContext.getWmlPackage().getMainDocumentPart();
		if (mainDocumentPart.hasFootnotesPart()) {
			appendFootnotesEndnotes(
				conversionContext, document,
				documentRoot, 
				"footnotes",
				mainDocumentPart.getFootnotesPart(),
				mainDocumentPart.getFootnotesPart().getJaxbElement().getFootnote());
		}
		if (mainDocumentPart.hasEndnotesPart()) {
			appendFootnotesEndnotes(
				conversionContext, document,
				documentRoot, 
				"endnotes",
				mainDocumentPart.getEndNotesPart(),
				mainDocumentPart.getEndNotesPart().getJaxbElement().getEndnote());
		}

		if (documentRoot.getLastChild() instanceof Element) {
			appendUserMarkup(conversionContext, document,
					(Element)documentRoot.getLastChild(), conversionContext.getUserBodyTail());
		}
	}

	@Override
	protected void writeDocument(HTMLConversionContext conversionContext, Document document,
			java.io.OutputStream outputStream) throws Docx4JException {

		// match the XSLT pathway's output: the XHTML doctype, and (as it does, per
		// the docx4j.Convert.Out.HTML.OutputMethodXML property) the xml or html
		// serialization method
		try {
			javax.xml.transform.Transformer t =
					org.docx4j.XmlUtils.getTransformerFactory().newTransformer();
			// set the method explicitly: with an html root element the serializer
			// would otherwise choose the html method by itself
			if (org.docx4j.Docx4jProperties.getProperty("docx4j.Convert.Out.HTML.OutputMethodXML", true)) {
				t.setOutputProperty(javax.xml.transform.OutputKeys.METHOD, "xml");
			} else {
				t.setOutputProperty(javax.xml.transform.OutputKeys.METHOD, "html");
			}
			// as the stylesheets say: indentation gives a worse result for things
			// like subscripts (a carriage return becomes a space)
			t.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "no");
			t.setOutputProperty(javax.xml.transform.OutputKeys.DOCTYPE_PUBLIC,
					"-//W3C//DTD XHTML 1.0 Transitional//EN");
			t.setOutputProperty(javax.xml.transform.OutputKeys.DOCTYPE_SYSTEM,
					"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
			t.setOutputProperty(javax.xml.transform.OutputKeys.ENCODING, "utf-8");
			t.transform(new javax.xml.transform.dom.DOMSource(document),
					new javax.xml.transform.stream.StreamResult(outputStream));
		} catch (javax.xml.transform.TransformerException e) {
			throw new Docx4JException("Exception writing the html", e);
		}
	}

	protected void appendFootnotesEndnotes(HTMLConversionContext conversionContext,
			Document document, Element documentRoot, String className,
			Part part, List<CTFtnEdn> ftnEdnList) {

		// documentRoot is the html element; the div belongs in the body
		org.w3c.dom.Node body = documentRoot.getLastChild();
		if (body==null) return;

		Element div = document.createElement("div");
		div.setAttribute("class", className);
		body.appendChild(div);

		for (int i=0; i<ftnEdnList.size(); i++) {
			CTFtnEdn note = ftnEdnList.get(i);
			if (note.getId()!=null && note.getId().signum()==0) {
				continue; // the separator; as in the XSLT, only w:id='0' is skipped
			}
			HTMLExporterVisitorGenerator generator = (HTMLExporterVisitorGenerator)
					generatorFactory.createInstance(conversionContext, document, div);
			// the number w:footnoteRef/w:endnoteRef renders, linked back to the
			// reference; cf the XSLT's count(preceding-sibling)-1
			generator.noteNumber = i - 1;
			new TraversalUtil(note.getContent(), generator);
		}
	}

	

}
