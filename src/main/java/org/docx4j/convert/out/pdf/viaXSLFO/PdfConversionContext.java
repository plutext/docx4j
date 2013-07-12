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
package org.docx4j.convert.out.pdf.viaXSLFO;

import java.util.List;

import org.docx4j.convert.out.AbstractWmlConversionContext;
import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.convert.out.AbstractModelRegistry;
import org.docx4j.convert.out.ConversionSectionWrapper;
import org.docx4j.convert.out.ConversionSectionWrappers;
import org.docx4j.convert.out.common.writer.AbstractMessageWriter;
import org.docx4j.model.images.ConversionImageHandler;
import org.w3c.dom.traversal.NodeIterator;

/**
 * See /docs/developer/Convert_Out.docx for an overview of
 * the design.
 * 
 * @author Alberto Zerolo
 *
 */
public class PdfConversionContext extends AbstractWmlConversionContext {

	protected boolean requires2PassChecked = false;
	protected boolean requires2Pass = false;
	
	//The model registry is per output type a singleton
	protected static final AbstractModelRegistry PDF_MODEL_REGISTRY = 
		new AbstractModelRegistry() {
			@Override
			protected void registerDefaultConverterInstances() {
				registerConverter(new TableWriter());
				registerConverter(new SymbolWriter());
				registerConverter(new FldSimpleWriter());
				registerConverter(new BookmarkStartWriter());
				registerConverter(new HyperlinkWriter());
			}
		};
			
	//The message writer for pdf	
	protected static final AbstractMessageWriter PDF_MESSAGE_WRITER = new AbstractMessageWriter() {
		@Override
		protected String getOutputPrefix() {
			return "<fo:block xmlns:fo=\"http://www.w3.org/1999/XSL/Format\"  " 
					+ "font-size=\"12pt\" "
		        	+ "color=\"red\" "
		        	+ "font-family=\"sans-serif\" "
		        	+ "line-height=\"15pt\" "
		        	+ "space-after.optimum=\"3pt\" "
		        	+ "text-align=\"justify\"> ";
		}
		@Override
		protected String getOutputSuffix() {
			return "</fo:block>";
		}
	};

	public PdfConversionContext(PdfSettings settings, ConversionSectionWrappers conversionSectionWrappers) {
		super(PDF_MODEL_REGISTRY, PDF_MESSAGE_WRITER, settings, conversionSectionWrappers);
	}
	
	@Override
	protected ConversionImageHandler initializeImageHandler(AbstractConversionSettings settings, ConversionImageHandler handler) {
		if (handler == null) {
			//setup a private image handler if there is none in the configuration.
			handler = (settings.getImageDirPath() != null ? 
					new PDFConversionImageHandler(settings.getImageDirPath(), true) : 
					new PDFConversionImageHandler());
		}
		return handler;
	}
	
	
	/** If it is a 2 pass generation, the xslfo document can't be generated independently of 
	 *  a rendering step. Some APIs return a xslfo document without rendering it (XSLFOExporterNonXSLT,
	 *  Conversion.outputXSLFO), for this cases this method ensures, that the document doesn't require
	 *  the corresponding parameters but if a 2 pass generation was required the rendered document 
	 *  will show errors.      
	 */
	public void forceRequires1Pass() {
		requires2PassChecked = true;
		requires2Pass = false;
	}
	
	public boolean isRequires2Pass() {
		if (!requires2PassChecked) {
			requires2Pass = checkRequires2Pass();
			requires2PassChecked = true;
		}
		return requires2Pass;
	}

	/** A 2 pass pdf generation is required if the result of fo:page-number-citation-last does not correspond
	 *  to the field results of NUMPAGES or SECTIONPAGES. This is the case when one of those fields are used and:
	 * <ul>
	 * <li>There is an explicit start of page numbers (> 1 in the first section, anything in the following sections) or</li>
	 * <li>The document contains more than 1 section (and SECTIONPAGES is used).</li>
	 * </ul>
	 * In theory, a different page number formatting of NUMPAGES or SECTIONPAGES should also trigger a 2 pass 
	 * conversion. This case is ignored to have a consistent behavior with the page refs and reduce the amount 
	 * of 2 passes. 
	 * 
	 * @return
	 */
	protected boolean checkRequires2Pass() {
	boolean ret = false;
	boolean sectionPagesUsed = false;
	ConversionSectionWrapper wrapper = null;
	List<ConversionSectionWrapper> wrapperList = getSections().getList();
		for (int i=0; (!ret) && (i < wrapperList.size()); i++) {
			wrapper = wrapperList.get(i);
			if (wrapper.getPageNumberInformation().getPageStart() > -1) {
				ret = ((i == 0) && (wrapper.getPageNumberInformation().getPageStart() != 1)) ||
					  (i > 0);
			}
			if (wrapper.getPageNumberInformation().isSectionpagesPresent()) {
				sectionPagesUsed = true;
			}
		}
		return (ret || ((sectionPagesUsed) && (wrapperList.size() > 0)));
	}
	
}
