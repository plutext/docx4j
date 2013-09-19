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

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.common.AbstractWriterRegistry;
import org.docx4j.convert.out.common.Exporter;
import org.docx4j.convert.out.common.WmlXsltExporterDelegate;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Converts the document to HTML with a xsl transformation.
 * 
 * Uses Delegate: org.docx4j.convert.out.html.HTMLExporterXslt.HTMLExporterXsltDelegate
 * Uses Generator: None
 * 
 * @since 3.0
 */
public class HTMLExporterXslt extends AbstractHTMLExporter3 {
	
	private static final Logger log = LoggerFactory.getLogger(HTMLExporterXslt.class);

	/**
	 * Usual use case.  Ordinarily you'd use the Docx4J facade,
	 * rather than using this constructor directly.
	 */
	public HTMLExporterXslt() {
		super(new HTMLExporterXsltDelegate());
	}

	/**
	 * using a customised WriterRegistry
	 * 
	 * @param writerRegistry
	 */
	public HTMLExporterXslt(AbstractWriterRegistry writerRegistry) {
		super(new HTMLExporterXsltDelegate(), writerRegistry);
	}
	
	protected static final String PROPERTY_HTML_OUTPUT_TYPE = 
			"docx4j.Convert.Out.HTML.OutputMethodXML";
	
	protected static final String XHTML_TEMPLATE_RESOURCE = 
			"org/docx4j/convert/out/html/docx2xhtml.xslt";
	protected static final String HTML_TEMPLATE_RESOURCE = 
			"org/docx4j/convert/out/html/docx2html.xslt";
	
	protected static final String XSLT_RESOURCE_ROOT = 
			"org/docx4j/convert/out/html/";
	
	protected static final URIResolver RESOURCES_URI_RESOLVER = 
			new OutHtmlURIResolver();

	
	protected static class OutHtmlURIResolver implements URIResolver {
		@Override
		public Source resolve(String href, String base) throws TransformerException {
		  try {
			return new StreamSource(
						org.docx4j.utils.ResourceUtils.getResource(
								XSLT_RESOURCE_ROOT + href));
		} catch (IOException e) {
			throw new TransformerException(e);
		}  
		}
	}
	
	protected static class HTMLExporterXsltDelegate extends WmlXsltExporterDelegate<HTMLSettings, HTMLConversionContext> {
		public HTMLExporterXsltDelegate() {
			super(null);
		}

		@Override
		protected Templates loadDefaultTemplates() throws Docx4JException {
		Source xsltSource = null;
		Templates ret = null;
		URIResolver originalURIResolver = null;
			try {
				originalURIResolver = XmlUtils.getTransformerFactory().getURIResolver();
				
				// TODO FIXME - partially thread safe,
				// loading of Templates in the delegates is synchronized on the 
				// XmlUtils.getTransformerFactory() but other parts of the application
				// are not.
				XmlUtils.getTransformerFactory().setURIResolver(RESOURCES_URI_RESOLVER);
				if (Docx4jProperties.getProperty(PROPERTY_HTML_OUTPUT_TYPE, true)){
					log.info("Outputting well-formed XHTML..");
					defaultTemplatesResource = XHTML_TEMPLATE_RESOURCE;
				} else {
					log.info("Outputting HTML tag soup..");
					defaultTemplatesResource = HTML_TEMPLATE_RESOURCE;
				}
				xsltSource = new StreamSource(org.docx4j.utils.ResourceUtils.getResource(
						defaultTemplatesResource));				
				ret = XmlUtils.getTransformerTemplate(xsltSource);
			} catch (IOException e) {
				throw new Docx4JException("Exception loading template \"" + defaultTemplatesResource + "\", " + e.getMessage(), e);
			} catch (TransformerConfigurationException e) {
				throw new Docx4JException("Exception loading template \"" + defaultTemplatesResource + "\", " + e.getMessage(), e);
			}
			finally {
				XmlUtils.getTransformerFactory().setURIResolver(originalURIResolver);
			}
			return ret;
		}

		@Override
		protected Document getSourceDocument(HTMLSettings conversionSettings, HTMLConversionContext conversionContext) throws Docx4JException {
		WordprocessingMLPackage wmlPackage = conversionContext.getWmlPackage();
			//TODO: the docx2xhtml-core.xslt only knows about the MainDocumentPart, therefore it's 
			//unable to process any sections....
			return XmlUtils.marshaltoW3CDomDocument(wmlPackage.getMainDocumentPart().getJaxbElement());
		}

		@Override
		public void process(HTMLSettings conversionSettings, HTMLConversionContext conversionContext, OutputStream outputStream) throws Docx4JException {
			//TODO: The docx2fo takes care of moving to the MainDocumentPart,
			// docx2html-core doesn't, would make sense to have the same behaviour... 
			conversionContext.setCurrentPartMainDocument();
			super.process(conversionSettings, conversionContext, outputStream);
		}
		
		
		
	}
	
	protected static HTMLExporterXslt instance = null;

	public static Exporter<HTMLSettings> getInstance() {
		if (instance == null) {
			synchronized(HTMLExporterXslt.class) {
				if (instance == null) {
					instance = new HTMLExporterXslt();
				}
			}
		}
		return instance;
	}
}
