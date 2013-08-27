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
package org.docx4j.convert.out.fo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.PageSequenceResults;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FORenderer;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.fonts.fop.util.FopConfigUtil;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The Apache FO Renderer uses Apache FOP to render the fo document
 *  and is the default FO Renderer
 */
public class ApacheFORenderer implements FORenderer {
	
	protected static Logger log = LoggerFactory.getLogger(ApacheFORenderer.class);
	protected static ApacheFORenderer instance = null;
	
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

	@Override
	public void render(String foDocument, FOSettings settings, 
			boolean twoPass,
			List<SectionPageInformation> pageNumberInformation,
			OutputStream outputStream) throws Docx4JException {
		
		String apacheFopConfiguration = setupApacheFopConfiguration(settings);
		String apacheFopMime = setupApacheFopMime(settings);
		StreamSource foDocumentSrc = new StreamSource(new StringReader(foDocument));
		FopPlaceholderLookup placeholderLookup = null;
		FormattingResults formattingResults = null;
		FopFactory fopFactory = null;
		try {
			fopFactory = getFopFactory(apacheFopConfiguration);
		} catch (FOPException e) {
			throw new Docx4JException("Exception creating fop factory for rendering: " + e.getMessage(), e);
		}
		if (twoPass) {
			//1st pass in 2 pass
			log.debug("1st pass in 2 pass");
			placeholderLookup = new FopPlaceholderLookup(pageNumberInformation);
			formattingResults = calcResults(fopFactory, apacheFopMime, foDocumentSrc, placeholderLookup);
			placeholderLookup.setResults(formattingResults);
			foDocumentSrc = new StreamSource(new StringReader(foDocument));
		}
		//1st pass in 1 pass or 2nd pass in 2 pass
		render(fopFactory, apacheFopMime, foDocumentSrc, placeholderLookup, outputStream);
	}

	private String setupApacheFopConfiguration(FOSettings settings) throws Docx4JException {
	String ret = settings.getApacheFopConfiguration();
	WordprocessingMLPackage wmlPackage = (WordprocessingMLPackage)settings.getWmlPackage();
		if (ret == null) {
			ret = FopConfigUtil.createDefaultConfiguration(wmlPackage.getFontMapper(), 
					wmlPackage.getMainDocumentPart().fontsInUse());
		}
		return ret;
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
			synchronized (ApacheFORenderer.class) {
				if (instance == null) {
					instance = new ApacheFORenderer();
				}
			}
		}
		return instance;
	}

	
	protected void render(FopFactory fopFactory, String outputFormat, Source foDocumentSrc, PlaceholderReplacementHandler.PlaceholderLookup placeholderLookup, OutputStream outputStream) throws Docx4JException {
		Fop fop = null;
		Result result = null;
		try {
			fop = fopFactory.newFop(outputFormat, outputStream);
			result = (placeholderLookup == null ?
					//1 Pass
					new SAXResult(fop.getDefaultHandler()) :
					//2 Pass
					new SAXResult(new PlaceholderReplacementHandler(fop.getDefaultHandler(), placeholderLookup)));
		} catch (FOPException e) {
			throw new Docx4JException("Exception setting up result for fo transformation: " + e.getMessage(), e);
		}

		Transformer transformer;
		try {
			transformer = XmlUtils.getTransformerFactory().newTransformer();
			transformer.transform(foDocumentSrc, result);
		} catch (TransformerConfigurationException e) {
			throw new Docx4JException("Exception setting up transformer: " + e.getMessage(), e);
		} catch (TransformerException e) {
			throw new Docx4JException("Exception executing transformer: " + e.getMessage(), e);
		}
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

		Transformer transformer;
		try {
			transformer = XmlUtils.getTransformerFactory().newTransformer();
			transformer.transform(foDocumentSrc, result);
		} catch (TransformerConfigurationException e) {
			throw new Docx4JException("Exception setting up transformer: " + e.getMessage(), e);
		} catch (TransformerException e) {
			throw new Docx4JException("Exception executing transformer: " + e.getMessage(), e);
		}
		return fop.getResults();
	}
	
	protected Fop createFop(String userConfiguration, String outputFormat, OutputStream outputStream) throws FOPException {
		
		FopFactory fopFactory = getFopFactory(userConfiguration);
		return fopFactory.newFop(outputFormat != null ? outputFormat : MimeConstants.MIME_PDF, outputStream);
	}

	
	protected FopFactory getFopFactory(String userConfig) throws FOPException {

		//The current implementation doesn't pass the user config to the fop factory 
		//if there is only a factory per Thread, all documents rendered in that  
		//Thread would use the configuration done for the first document.
		//For this reason disable the reuse of the FopFactories until this issue 
		//gets resolved.
		return createFopFactory(userConfig);
//		FopFactory fopFactory = fopFactories.get(Thread.currentThread().getId());
//		if (fopFactory == null) {
//			synchronized(fopFactories) {
//				fopFactory = createFopFactory(userConfig);
//				fopFactories.put(Thread.currentThread().getId(), fopFactory);
//			}
//		}
//		fopFactory.setUserConfig(userConfig);
//		return fopFactory;
	}
	
	/**
	 * Create and configure FopFactory.
	 * 
	 * @param userConfig
	 * @param defaultBaseURI
	 * @return
	 * @throws FOPException
	 */
	protected FopFactory createFopFactory(String userConfig) throws FOPException {
		// This class won't compile unless you have some version of FOP on your path. 

		/* 
		 * Unfortunately, <=1.1 requires Configuration object,
		 * and >1.1 requires InputStream.
		 * 
		 * So either we pass Configuration object into this method, 
		 * and use DefaultConfigurationSerializer to get an input stream from it,
		 * or we pass a string, and make it into a Configuration object in the case
		 * where it is required. 
		 * 
		 * I've submitted a patch to FOP allowing configuration
		 * using a Configuration object.  Let's see whether it is applied.
		 * 
		 * In the absence of that, passing a String (or an InputStream) seems better going forward.
		 * So that's what the code accepts for now.
		 */
		
		InputStream is=null;
		try {
			is = IOUtils.toInputStream(userConfig, "UTF-8");
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		FopFactory fopFactory = null;
		
		// If FopConfParser is on path, it is post 1.1
		try {
			Class fopConfParserClass = Class.forName("org.apache.fop.apps.FopConfParser");
			
			URI defaultBaseURI = new URI("http://dummy.domain");
				// Default base URI must not be null, but we don't need it.
				// at org.apache.fop.apps.EnvironmentalProfileFactory$Profile (EnvironmentalProfileFactory.java:92)

			Object o = fopConfParserClass.getConstructor(InputStream.class, URI.class).newInstance(is, defaultBaseURI);
			
			Method method = fopConfParserClass.getDeclaredMethod("getFopFactoryBuilder", new Class[0] );
			Object fopFactoryBuilder = method.invoke(o);
			
			Class fopFactoryBuilderClass = Class.forName("org.apache.fop.apps.FopFactoryBuilder");
			method = fopFactoryBuilderClass.getDeclaredMethod("build", new Class[0] );
			
			fopFactory = (FopFactory)method.invoke(fopFactoryBuilder);
			
		} catch (Exception e) {
			log.warn("Can't set up FOP svn; " + e.getMessage() );
			log.debug(e.getMessage(), e);
			// eg java.lang.ClassNotFoundException: org.apache.fop.apps.FopConfParser
			
			// legacy FOP 1.0 or 1.1 config.
			try {
				Method method;
				Class[] params = new Class[1];

				// FopFactory fopFactory = FopFactory.newInstance();
				method = FopFactory.class.getDeclaredMethod("newInstance", new Class[0] );
				fopFactory = (FopFactory)method.invoke(null);
				
				// fopFactory.setUserConfig(userConfig);
				params[0] = Configuration.class;				
				method = FopFactory.class.getDeclaredMethod("setUserConfig", params );
				
				// There isn't a method which takes it as a string :-(
				DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
				
				method.invoke(fopFactory, cfgBuilder.build(is) );
				
			} catch (Exception e1) {
				e1.printStackTrace();
				
				// java.lang.IllegalAccessException: Class org.docx4j.fonts.fop.util.FopFactoryUtil 
				// can not access a member of class org.apache.fop.apps.FopFactory 
				// with modifiers "protected"
				
			} 
		}
		return fopFactory;
	}
}
