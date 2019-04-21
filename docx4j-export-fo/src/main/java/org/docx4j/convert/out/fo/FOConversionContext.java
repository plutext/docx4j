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

import java.util.List;

import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.convert.out.FORenderer;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.AbstractWriterRegistry;
import org.docx4j.convert.out.common.ConversionSectionWrapper;
import org.docx4j.convert.out.common.ConversionSectionWrappers;
import org.docx4j.convert.out.common.writer.AbstractMessageWriter;
import org.docx4j.convert.out.fo.renderers.FORendererApacheFOP;
import org.docx4j.convert.out.fo.renderers.FORendererDummy;
import org.docx4j.fonts.RunFontSelector;
import org.docx4j.fonts.RunFontSelector.RunFontActionType;
import org.docx4j.fonts.RunFontSelector.RunFontCharacterVisitor;
import org.docx4j.model.images.ConversionImageHandler;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * See /docs/developer/Convert_Out.docx for an overview of
 * the design.
 * 
 * @author Alberto Zerolo
 *
 */
public class FOConversionContext extends AbstractWmlConversionContext {
	
	private static Logger log = LoggerFactory.getLogger(FOConversionContext.class);

	protected boolean requires2PassChecked = false;
	protected boolean requires2Pass = false;
	protected FORenderer foRenderer;
	
	//The model registry is per output type a singleton
	protected static final AbstractWriterRegistry FO_WRITER_REGISTRY = 
		new AbstractWriterRegistry() {
			@Override
			protected void registerDefaultWriterInstances() {
				registerWriter(new TableWriter());
				registerWriter(new SymbolWriter());
				registerWriter(new BrWriter());
				registerWriter(new FldSimpleWriter());
				registerWriter(new BookmarkStartWriter());
				registerWriter(new HyperlinkWriter());
				registerWriter(new FOPictWriterFloatUsed()); 
			}
		};
			
	//The message writer for pdf	
	protected static final AbstractMessageWriter FO_MESSAGE_WRITER = new AbstractMessageWriter() {
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

	public FOConversionContext(FOSettings settings, WordprocessingMLPackage wmlPackage, ConversionSectionWrappers conversionSectionWrappers) {
		super(FO_WRITER_REGISTRY, FO_MESSAGE_WRITER, settings, wmlPackage, conversionSectionWrappers, createRunFontSelector(wmlPackage));
		this.foRenderer = initializeFoRenderer(settings);
	}
	
	protected static RunFontSelector createRunFontSelector(WordprocessingMLPackage wmlPackage) {
		
		return new RunFontSelector(wmlPackage, 
				
			new RunFontCharacterVisitor() {
			
	    		DocumentFragment df;			
				StringBuilder sb = new StringBuilder(1024); 
				Element span;
				
				String lastFont;
				String fallbackFontName; 
				
				private Document document;
				@Override
				public void setDocument(Document document) {
					this.document = document;
					 df = document.createDocumentFragment();
				}
				
				private boolean spanReusable = true;
				public boolean isReusable() {
					return spanReusable;
				}
	
				public void addCharacterToCurrent(char c) {
			    	sb.append(c);		
				}

				@Override
				public void addCodePointToCurrent(int cp) {
					
					sb.append(new String(Character.toChars(cp)));
				}
				
				public void finishPrevious() {
					
			    	if (sb.length()>0) {
			    		if (span==null) { // init
			    			span = runFontSelector.createElement(document);	
			    			// so that spaces have correct font set
			    			if (lastFont!=null) {
								runFontSelector.setAttribute(span, lastFont); 			    				
			    			}
			    		}
				    	df.appendChild(span);   
				    	span.setTextContent(sb.toString()); 
//				    	log.info("span: " + sb.toString()); 
				    	sb.setLength(0);
			    	}		
				}
	
				public void createNew() {
					span = runFontSelector.createElement(document);			
				}
	
				public void setMustCreateNewFlag(boolean val) {
					spanReusable = !val;
				}
	
				public void fontAction(String fontname) {
					
//					System.out.println("FO RFS fontname: " + fontname);
//    				Throwable t = new Throwable();
//    				t.printStackTrace();
					
					
					if (fontname==null) {
						runFontSelector.setAttribute(span, fallbackFontName); 						
					} else {
						runFontSelector.setAttribute(span, fontname); 
						lastFont = fontname;
					}
				}

				@Override
				public Object getResult() {
					span=null; // ready for next time
					return df;
				}

				private RunFontSelector runFontSelector;
				@Override
				public void setRunFontSelector(RunFontSelector runFontSelector) {
					this.runFontSelector = runFontSelector;
				}

				@Override
				public void setFallbackFont(String fontname) {
					fallbackFontName = fontname;
					
				}


				
			}, RunFontActionType.XSL_FO);

	}
	
	
	protected FORenderer initializeFoRenderer(FOSettings settings) {
		
		FORenderer ret = settings.getCustomFoRenderer();
		if (ret == null) {
			if (FOSettings.INTERNAL_FO_MIME.equals(settings.getApacheFopMime())) {
				ret = FORendererDummy.getInstance();
				forceRequires1Pass();
			}
			else {
				ret = FORendererApacheFOP.getInstance();
			}
			settings.setCustomFoRenderer(ret); // make sure this is always set
		}
		return ret;
	}

	@Override
	protected ConversionImageHandler initializeImageHandler(AbstractConversionSettings settings, ConversionImageHandler handler) {
		if (handler == null) {
			//setup a private image handler if there is none in the configuration.
			handler = (settings.getImageDirPath() != null ? 
					new FOConversionImageHandler(settings.getImageDirPath(), true) : 
					new FOConversionImageHandler());
		}
		return handler;
	}
	
	public FORenderer getFORenderer() {
		return foRenderer;
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
		return (ret || ((sectionPagesUsed) && (wrapperList.size() > 1)));
	}
	
}
