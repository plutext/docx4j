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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.docx4j.convert.out.FORenderer;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.FORenderer.SectionPageInformation;
import org.docx4j.convert.out.common.AbstractConversionContext;
import org.docx4j.convert.out.common.AbstractExporterDelegate;
import org.docx4j.convert.out.common.AbstractWmlExporter;
import org.docx4j.convert.out.common.ConversionSectionWrapper;
import org.docx4j.convert.out.common.ConversionSectionWrappers;
import org.docx4j.model.fields.FormattingSwitchHelper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.FoNumberFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFOExporter extends AbstractWmlExporter<FOSettings, FOConversionContext>{

	private static Logger log = LoggerFactory.getLogger(AbstractFOExporter.class);
	
	protected AbstractFOExporter(AbstractExporterDelegate<FOSettings, FOConversionContext> exporterDelegate) {
		super(exporterDelegate);
	}

	protected static final int DEFAULT_START_SIZE = 10240;
	
	protected static class FoSectionPageInformation implements FORenderer.SectionPageInformation {
		
		protected String documentPageCountID = null;
		protected String documentPageCountFoFormat = null;
		protected String sectionPageCountID = null;
		protected String sectionPageCountFoFormat = null;

		
		public FoSectionPageInformation(String documentPageCountID,
				String documentPageCountFoFormat, String sectionPageCountID,
				String sectionPageCountFoFormat) {
			super();
			this.documentPageCountID = documentPageCountID;
			this.documentPageCountFoFormat = documentPageCountFoFormat;
			this.sectionPageCountID = sectionPageCountID;
			this.sectionPageCountFoFormat = sectionPageCountFoFormat;
		}

		@Override
		public String getDocumentPageCountID() {
			return documentPageCountID;
		}

		@Override
		public String getDocumentPageCountFoFormat() {
			return documentPageCountFoFormat;
		}

		@Override
		public String getSectionPageCountID() {
			return sectionPageCountID;
		}

		@Override
		public String getSectionPageCountFoFormat() {
			return sectionPageCountFoFormat;
		}
	}

	@Override
	protected FOConversionContext createContext(
			FOSettings conversionSettings, 
			WordprocessingMLPackage preprocessedPackage,
			ConversionSectionWrappers sectionWrappers) {
		return new FOConversionContext(conversionSettings, preprocessedPackage, sectionWrappers);
	}

	@Override
	protected OutputStream createIntermediateOutputStream(OutputStream outputStream) throws Docx4JException {
		return new ByteArrayOutputStream(DEFAULT_START_SIZE);
	}

	@Override
	protected void postprocess(FOSettings conversionSettings,
			AbstractConversionContext conversionContext,
			OutputStream intermediateOutputStream, OutputStream outputStream)
			throws Docx4JException {
		
			//intermediateOutputStream is a ByteArrayOutputStream, as we have created it above
		
		String	foDocument = null;
		File dumpFoFile = conversionSettings.getFoDumpFile();
		FOConversionContext foConversionContext = (FOConversionContext)conversionContext;

		try {
			foDocument = ((ByteArrayOutputStream)intermediateOutputStream).toString("UTF-8");
			
			// DEBuGGING for https://issues.apache.org/jira/browse/XALANJ-2419 (astral character serialization issue)
			if (log.isDebugEnabled()) {
				
				for (int i = 0; i < foDocument.length(); i=foDocument.offsetByCodePoints(i, 1)){
	
		    	    char c1 = foDocument.charAt(i);
	//	    		int cp = text.codePointAt(i);
		    	    
		    	    if (Character.isHighSurrogate(c1)) {
		    	    	// Process this and the next
						log.debug("added as code point OK");
						
	//				   	char c2 = foDocument.charAt(i+1);
	//				   	
	//				   	System.out.println(
	//				   			String.format("%04x", (int) c1));
	//				   	System.out.println(
	//				   			String.format("%04x", (int) c2));					
						
		    	    } else if (c1=='#') {
		    	    	log.debug("Unintended " + foDocument.substring(i-1, i+7) + "?"); 
		    	    }
				}
			}
			
			
		} catch (UnsupportedEncodingException e) {
			//if UTF-8 is unsupported, then anything will do... (java without utf-8??)
			log.error("No UTF-8!? " + e.getMessage());
			foDocument = ((ByteArrayOutputStream)intermediateOutputStream).toString();
		}
		if (log.isDebugEnabled()) {
			log.debug(foDocument);
		}
		if (dumpFoFile != null) {
			try {
				FileUtils.writeStringToFile(dumpFoFile, foDocument, "UTF-8");
				log.info("Saved " + dumpFoFile.getPath());
			} catch (IOException e) {
				log.warn("fo file couldn't be dumped to " + dumpFoFile.getPath() + ": " + e, e);
			}
		}

		foConversionContext.getFORenderer().
			render(foDocument, conversionSettings, 
				   foConversionContext.isRequires2Pass(), 
				   createPageNumberInformation(foConversionContext), 
				   outputStream);
	}


	protected List<SectionPageInformation> createPageNumberInformation(FOConversionContext conversionContext) {
		
		List<ConversionSectionWrapper> wrapperList = null;
		List<FORenderer.SectionPageInformation> ret = Collections.EMPTY_LIST;
		ConversionSectionWrapper section = null;
		String sectionId = null;
		String pageFoFormat = null;
		String pageWordFormat = null;
		FORenderer.SectionPageInformation pageNumberInformation = null;
	
		if (conversionContext.isRequires2Pass()) {
			wrapperList = conversionContext.getSections().getList();
			ret = new ArrayList<FORenderer.SectionPageInformation>(wrapperList.size());
			for (int i=0; i<wrapperList.size(); i++) {
				section = wrapperList.get(i);
				pageWordFormat = section.getPageNumberInformation().getPageFormat();
				pageFoFormat = FormattingSwitchHelper.getFoPageNumberFormat(pageWordFormat);
				/* The format used for the document and section page count corresponds to the 
				 * one defined for the page number format. If some other format has been defined
				 * in the NUMPAGES (section.getPageNumberInformation().getNumpagesFormat()) or 
				 * in the SECTIONPAGES (section.getPageNumberInformation().getSectionpagesFormat())
				 * this format is ignored here. This is done, so that the look is consistent with 
				 * the 1 pass conversion, where fo formats the fo:page-number-citation-last according
				 * the page number format.
				 */
				sectionId = section.getId();
				if (pageFoFormat == null) {
					pageFoFormat = FoNumberFormatUtil.FO_PAGENUMBER_DECIMAL;
				}
				pageNumberInformation = 
						new FoSectionPageInformation(
								"field_numpages_" + sectionId +  "_value", 
								pageFoFormat, 
								"field_sectionpages_" + sectionId +  "_value", 
								pageFoFormat);
				ret.add(pageNumberInformation);
			}
		}
		return ret;
	}
	
}
