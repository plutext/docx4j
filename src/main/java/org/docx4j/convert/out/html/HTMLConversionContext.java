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

import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.convert.out.ConversionHTMLScriptElementHandler;
import org.docx4j.convert.out.ConversionHTMLStyleElementHandler;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.AbstractWriterRegistry;
import org.docx4j.convert.out.common.ConversionSectionWrappers;
import org.docx4j.convert.out.common.writer.AbstractMessageWriter;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.RunFontSelector;
import org.docx4j.fonts.RunFontSelector.RunFontActionType;
import org.docx4j.fonts.RunFontSelector.RunFontCharacterVisitor;
import org.docx4j.model.images.ConversionImageHandler;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CTBookmark;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * See /docs/developer/Convert_Out.docx for an overview of
 * the design.
 * 
 * @author Alberto
 *
 */
public class HTMLConversionContext extends AbstractWmlConversionContext {
	protected Mapper fontMapper;
	protected ConversionHTMLStyleElementHandler styleElementHandler;
	protected ConversionHTMLScriptElementHandler scriptElementHandler;
	protected String userCSS;
	protected String userScript;
	protected String userBodyTop;
	protected String userBodyTail;
	
	protected static final ConversionHTMLStyleElementHandler DEFAULT_STYLE_ELEMENT_HANDLER = new ConversionHTMLStyleElementHandler() {
		@Override
		public Element createStyleElement(OpcPackage opcPackage, Document document, String styleDefinition) {
			
			// See XsltHTMLFunctions, which typically generates the String styleDefinition.
			// In practice, the styles are coupled to the document content, so you're
			// less likely to override their content; just whether they are linked or inline.
			
			Element ret = null;
			if ((styleDefinition != null) && (styleDefinition.length() > 0)) {
				ret = document.createElement("style");
				ret.appendChild(document.createComment(styleDefinition));
			}
			return ret;
		}
	};

	protected static final ConversionHTMLScriptElementHandler DEFAULT_SCRIPT_ELEMENT_HANDLER = new ConversionHTMLScriptElementHandler() {
		
		@Override
		public Element createScriptElement(OpcPackage opcPackage, Document document, String scriptDefinition) {
			Element ret = null;
			if ((scriptDefinition != null) && (scriptDefinition.length() > 0)) {
				ret = document.createElement("script");
				ret.setAttribute("type", "text/javascript");
				ret.appendChild(document.createComment(scriptDefinition));
			}
			return ret;
		}
	};
	
	//The model registry is per output type a singleton
	protected static final AbstractWriterRegistry HTML_WRITER_REGISTRY = 
		new AbstractWriterRegistry() {
			@Override
			protected void registerDefaultWriterInstances() {
				registerWriter(new TableWriter());
				registerWriter(new SymbolWriter());
				registerWriter(new BrWriter());
				registerWriter(new FldSimpleWriter());
				registerWriter(new BookmarkStartWriter());
				registerWriter(new HyperlinkWriter());
			}
		};

	//The message writer for html	
	protected static final AbstractMessageWriter HTML_MESSAGE_WRITER = 
		new AbstractMessageWriter() {
		
			@Override
			protected String getOutputPrefix() {
				return "<div style=\"color:red\" >";
			}
			@Override
			protected String getOutputSuffix() {
				return "</div>";
			}
		};		
	
	/**
	 * HTMLConversionContext used by default
	 * 
	 * @param settings
	 * @param preprocessedPackage
	 * @param conversionSectionWrappers
	 */
	public HTMLConversionContext(HTMLSettings settings, WordprocessingMLPackage preprocessedPackage, ConversionSectionWrappers conversionSectionWrappers) {
		super(HTML_WRITER_REGISTRY, HTML_MESSAGE_WRITER, settings, preprocessedPackage, conversionSectionWrappers, createRunFontSelector(preprocessedPackage));
	}

	private static RunFontSelector createRunFontSelector(WordprocessingMLPackage wmlPackage) {
		
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
					sb.append(
							new String(Character.toChars(cp)));
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
				
				
		}, RunFontActionType.XHTML);

	}
	
	/**
	 * HTMLConversionContext using a customised WriterRegistry
	 * 
	 * @param writerRegistry
	 * @param settings
	 * @param preprocessedPackage
	 * @param conversionSectionWrappers
	 */
	public HTMLConversionContext(AbstractWriterRegistry writerRegistry, HTMLSettings settings, WordprocessingMLPackage preprocessedPackage, ConversionSectionWrappers conversionSectionWrappers) {
		super(writerRegistry, HTML_MESSAGE_WRITER, settings, preprocessedPackage, conversionSectionWrappers, createRunFontSelector(preprocessedPackage));
	}
	
	@Override
	protected void initializeSettings(AbstractConversionSettings settings, OpcPackage localOpcPackage) {
	HTMLSettings htmlSettings = null;
		if (settings == null) {
			settings = new HTMLSettings();
		}
		else if (!(settings instanceof HTMLSettings)) {
			throw new IllegalArgumentException("The class of the settings isn't HtmlSettings, it is " + settings.getClass().getName());
		}
		htmlSettings = (HTMLSettings)settings;
		super.initializeSettings(htmlSettings, localOpcPackage);
		fontMapper = htmlSettings.getFontMapper();
		if (fontMapper == null) {
			fontMapper = getWmlPackage().getFontMapper();
		}
		userCSS = htmlSettings.getUserCSS();
		userScript = htmlSettings.getUserScript();
		userBodyTop = htmlSettings.getUserBodyTop();
		userBodyTail = htmlSettings.getUserBodyTail();
		styleElementHandler = htmlSettings.getStyleElementHandler();
		if (styleElementHandler == null) {
			styleElementHandler = DEFAULT_STYLE_ELEMENT_HANDLER;
		}
		scriptElementHandler = htmlSettings.getScriptElementHandler();
		if (scriptElementHandler == null) {
			scriptElementHandler = DEFAULT_SCRIPT_ELEMENT_HANDLER;
		}
	}

	@Override
	protected ConversionImageHandler initializeImageHandler(AbstractConversionSettings settings, ConversionImageHandler handler) {
	HTMLSettings htmlSettings = (HTMLSettings)settings;
		if (handler == null) {
			handler = new HTMLConversionImageHandler(
						htmlSettings.getImageDirPath(), 
						htmlSettings.getImageTargetUri(), 
						htmlSettings.isImageIncludeUUID());
		}
		return handler;
	}

	public Mapper getFontMapper() {
		return fontMapper;
	}
	
	public Element createStyleElement(Document document, String styleDefinition) {
		return styleElementHandler.createStyleElement(getWmlPackage(), document, styleDefinition);
	}
	
	public Element createScriptElement(Document document, String scriptDefinition) {
		return scriptElementHandler.createScriptElement(getWmlPackage(), document, scriptDefinition);
	}
	
	public String getUserCSS() {
		return userCSS;
	}
	
	public String getUserScript() {
		return userScript;
	}
	
	public String getUserBodyTop() {
		return userBodyTop;
	}
	
	public String getUserBodyTail() {
		return userBodyTail;
	}
	
	/**
	 * If property docx4j.Convert.Out.HTML.BookmarkStartWriter.mapTo=id,
	 * bookmarks as encountered will be stored here.
	 */
	private CTBookmark bookmarkStart;

	public CTBookmark getBookmarkStart() {
		return bookmarkStart;
	}

	public void setBookmarkStart(CTBookmark bookmarkStart) {
		this.bookmarkStart = bookmarkStart;
	}
	
}
