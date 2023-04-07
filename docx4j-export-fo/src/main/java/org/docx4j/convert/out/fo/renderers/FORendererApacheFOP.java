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
package org.docx4j.convert.out.fo.renderers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.PageSequenceResults;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FORenderer;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.fo.AbstractPlaceholderLookup;
import org.docx4j.convert.out.fo.PlaceholderReplacementHandler;
import org.docx4j.events.EventFinished;
import org.docx4j.events.StartEvent;
import org.docx4j.events.WellKnownProcessSteps;
import org.docx4j.fonts.fop.util.FopConfigUtil;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.XmlSerializerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The Apache FO Renderer uses Apache FOP to render the fo document
 *  and is the default FO Renderer
 */
public class FORendererApacheFOP extends AbstractFORenderer { //implements FORenderer {
	
	protected static Logger log = LoggerFactory.getLogger(FORendererApacheFOP.class);
	protected static FORendererApacheFOP instance = null;

	private static String XSL_FO = "http://www.w3.org/1999/XSL/Format";
	
	private static final String FO_USER_AGENT = "foUserAgent";
	public static final String FOP_FACTORY = "fopFactory";
	
	
	protected static class FopPlaceholderLookup extends AbstractPlaceholderLookup {
		
		public FopPlaceholderLookup(List<SectionPageInformation> pageNumberInformation) {
			super(pageNumberInformation);
		}
		
		public void setResults(FormattingResults formattingResults) throws Docx4JException {
			
			List<PageSequenceResults> resultList = null;
			PageSequenceResults pageSequenceResults = null;
			
			if (formattingResults == null) {
				throw new Docx4JException("Apache fop returned no FormattingResults (null)");
			}
			else {
				resultList = formattingResults.getPageSequences();
				if (resultList == null) {
					throw new Docx4JException("Apache fop returned null pageSequences");
				}
				else if (resultList.size() != pageNumberInformation.size()) {
					throw new Docx4JException("Apache fop returned different count of sections than expected, returned: " + resultList.size() + ", expected: " + pageNumberInformation.size());
				}
			}
			
			putDocumentPageCount(formattingResults.getPageCount());
			for (int i=0; i<formattingResults.getPageSequences().size(); i++) {
				pageSequenceResults = resultList.get(i);
				putSectionPageCount(i, pageSequenceResults.getPageCount());
			}
		}
	}

	//@Override
	public void render(String foDocument, FOSettings settings, 
			boolean twoPass,
			List<SectionPageInformation> pageNumberInformation,
			OutputStream outputStream) throws Docx4JException {
		
		
		String apacheFopMime = setupApacheFopMime(settings);
		StreamSource foDocumentSrc = new StreamSource(new StringReader(foDocument));
		FopPlaceholderLookup placeholderLookup = null;
		FormattingResults formattingResults = null;
		FopFactory fopFactory = null;
		if (settings.getSettings().get(FOP_FACTORY)==null) {
			
			try {
				FopFactoryBuilder fopFactoryBuilder = FORendererApacheFOP.getFopFactoryBuilder(settings);
				fopFactory = fopFactoryBuilder.build();
				// Put FOP_FACTORY and FO_USER_AGENT in settings
			    FOUserAgent foUserAgent = FORendererApacheFOP.getFOUserAgent(settings, fopFactory);			
			} catch (FOPException e) {
				throw new Docx4JException("FOUserAgent issue", e);
			}
			
		} else {
			fopFactory = (FopFactory)settings.getSettings().get(FOP_FACTORY);
		}
		if (twoPass) {
			//1st pass in 2 pass
			log.debug("1st pass in 2 pass");
			
			StartEvent startEvent = new StartEvent( settings.getOpcPackage(), WellKnownProcessSteps.FOP_RENDER_PASS1 );
			startEvent.publish();
			
			placeholderLookup = new FopPlaceholderLookup(pageNumberInformation);
			formattingResults = calcResults(fopFactory, apacheFopMime, foDocumentSrc, placeholderLookup);
			placeholderLookup.setResults(formattingResults);
			foDocumentSrc = new StreamSource(new StringReader(foDocument));
			
			new EventFinished(startEvent).publish();
			
		}
		
		//1st pass in 1 pass or 2nd pass in 2 pass
		
		if (TEXTBOX_POSTPROCESSING_REQUIRED) {
			
//			System.out.println("\n\n performing TEXTBOX_POSTPROCESSING \n\n");
			
			DOMResult result = new DOMResult(); 
			org.docx4j.XmlUtils.transform(foDocumentSrc, xslt_POSTPROCESSING, null, result);		

			org.w3c.dom.Document docResult = ((org.w3c.dom.Document)result.getNode());
			String modifiedFO = XmlUtils.w3CDomNodeToString(docResult); 
			log.debug("After moving! \n" + modifiedFO);	
			
			foDocumentSrc = new StreamSource(new StringReader(modifiedFO));
			
		}

		StartEvent startEvent = new StartEvent( settings.getOpcPackage(), WellKnownProcessSteps.FOP_RENDER_PASS1 );
		startEvent.publish();
		
	    FOUserAgent foUserAgent = (FOUserAgent)settings.getSettings().get(FO_USER_AGENT);
		render(fopFactory, foUserAgent, apacheFopMime, foDocumentSrc, placeholderLookup, outputStream);
		
		new EventFinished(startEvent).publish();
	}


	private String setupApacheFopMime(FOSettings settings) {
	String ret = settings.getApacheFopMime();
		if (ret == null) {
			ret = MimeConstants.MIME_PDF;
		}
		return ret;
	}

	public static FORenderer getInstance() {
		if (instance == null) {
			synchronized (FORendererApacheFOP.class) {
				if (instance == null) {
					instance = new FORendererApacheFOP();
				}
			}
		}
		return instance;
	}

	
	protected void render(FopFactory fopFactory, FOUserAgent foUserAgent, String outputFormat, Source foDocumentSrc, PlaceholderReplacementHandler.PlaceholderLookup placeholderLookup, OutputStream outputStream) throws Docx4JException {
		Fop fop = null;
		Result result = null;
		try {
			if (foUserAgent==null) {
				fop = fopFactory.newFop(outputFormat, outputStream);
			} else {
				fop = fopFactory.newFop(outputFormat, foUserAgent, outputStream);				
			}
			result = (placeholderLookup == null ?
					//1 Pass
					new SAXResult(fop.getDefaultHandler()) :
					//2 Pass
					new SAXResult(new PlaceholderReplacementHandler(fop.getDefaultHandler(), placeholderLookup)));
		} catch (FOPException e) {
			throw new Docx4JException("Exception setting up result for fo transformation: " + e.getMessage(), e);
		}
		
		XmlSerializerUtil.serialize(foDocumentSrc, result, false, false);
	}
	
	
	/**
	 * For first pass of two pass process, invoke org.apache.fop.apps.FormattingResults which can tell us the number of pages 
	 * in each page sequence, and in the document as a whole.
	 * 
	 * @param fopFactory
	 * @param outputFormat
	 * @param foDocumentSrc
	 * @param placeholderLookup
	 * @return
	 * @throws Docx4JException
	 */
	protected FormattingResults calcResults(FopFactory fopFactory, String outputFormat, Source foDocumentSrc, PlaceholderReplacementHandler.PlaceholderLookup placeholderLookup) throws Docx4JException {
		Fop fop = null;
		Result result = null;
		try {
			fop = fopFactory.newFop(outputFormat, new NullOutputStream());
			result = new SAXResult(new PlaceholderReplacementHandler(fop.getDefaultHandler(), placeholderLookup));
		} catch (FOPException e) {
			throw new Docx4JException("Exception setting up result for fo transformation: " + e.getMessage(), e);
		}

		XmlSerializerUtil.serialize(foDocumentSrc, result, false, false);
		
		return fop.getResults();
	}
	
	
	/**
	 * Allow user access to FOUserAgent, so they can setAccessibility(true).  Access to other settings
	 * is possible but unsupported.
	 * 
	 * @param wmlPackage
	 * @return
	 * @throws FOPException
	 */
	public static FOUserAgent getFOUserAgent(FOSettings settings, FopFactory fopFactory) throws Docx4JException, FOPException {
		
		if (fopFactory==null) {
			throw new Docx4JException("FopFactory is null");
		}
		settings.getSettings().put(FOP_FACTORY, fopFactory);
		
	    FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
		settings.getSettings().put(FO_USER_AGENT, foUserAgent);
		
		return foUserAgent;
	}

	/**
	 * Get a FopFactoryBuilder, automagically preconfigured with font info.
	 * Uses default ResourceResolver.
	 * defaultBaseURI can be set at property "docx4j.Convert.Out.fop.FopConfParser.defaultBaseURI"
	 * 
	 * @param settings
	 * @return
	 * @throws FOPException
	 */
	public static FopFactoryBuilder getFopFactoryBuilder(FOSettings settings) throws FOPException {
		return getFopFactoryBuilder(settings, null); 
	}	
	/**
	 * Get a FopFactoryBuilder, automagically preconfigured with font info.
	 * Uses the specified ResourceResolver.
	 * defaultBaseURI can be set at property "docx4j.Convert.Out.fop.FopConfParser.defaultBaseURI"
	 * 
	 * @param settings
	 * @param resourceResolver  your custom resourceResolver 
	 * @return
	 * @throws FOPException
	 */
	public static FopFactoryBuilder getFopFactoryBuilder(FOSettings settings, ResourceResolver resourceResolver) throws FOPException {

		org.docx4j.convert.out.fopconf.Fop fopConfig = settings.getFopConfig();
		String userConfig = XmlUtils.marshaltoString(fopConfig, Context.getFopConfigContext());
		if (log.isDebugEnabled()) {
			log.debug(userConfig);
		}
		
		InputStream is = IOUtils.toInputStream(userConfig, "UTF-8");		
		
		try {
			
			// Default base URI must not be null
			String baseUriProperty=Docx4jProperties.getProperty("docx4j.convert.out.fop.FopConfParser.defaultBaseURI");
			
			URI defaultBaseURI = null;
			if (baseUriProperty==null) {
				log.debug("Using defaultBaseURI");
				defaultBaseURI = new java.io.File(".").toURI();
			} else {
				log.debug("Using baseURI " + baseUriProperty);
				defaultBaseURI = new URI(baseUriProperty);
			}

			FopConfParser fopConfParser = null;
			if (resourceResolver==null) {
				fopConfParser = new FopConfParser(is, defaultBaseURI);
			} else {
				fopConfParser = new FopConfParser(is, defaultBaseURI, resourceResolver);
			}
			return fopConfParser.getFopFactoryBuilder();

//			log.debug("FOP configured OK." );
//			return fopFactoryBuilder.build();
			
		} catch (Exception e) {
			log.error("Can't set up FOP 2.x; " + e.getMessage() );
			log.error(e.getMessage(), e);
			throw new FOPException(e.getMessage(), e);
		}
	}


}