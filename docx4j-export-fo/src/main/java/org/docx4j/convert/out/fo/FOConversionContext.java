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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.convert.out.ConversionFeatures;
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
import org.docx4j.model.pagination.PaginationAreaTreeHandler;
import org.docx4j.openpackaging.exceptions.CyclicStylesException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
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
				registerWriter(useFloats() ? new FOPictWriterFloatUsed() : new FOPictWriterFloatAvoided());
			}
		};

	/**
	 * docx4j.convert.out.fo.pictures.float (default true): whether a picture or
	 * text box Word wraps text around may be rendered as an fo:float.
	 *
	 * <p>FOP's side floats are unreliable: a float of any height followed by
	 * content that overflows the page (a table row taller than the space left, say)
	 * makes it throw NoSuchElementException from LMiter.next, under
	 * PageBreaker.handleFloatLayout, and the whole export fails.  Set this false to
	 * lay such objects out in the flow instead (no text beside them, but the export
	 * completes).  Since 17.0.5 a text box is never a float in any case; this
	 * governs anchored pictures with square/tight/through wrapping, and which
	 * picture writer is registered here.</p>
	 *
	 * @since 17.0.5
	 */
	public static final String FLOAT_PROPERTY = "docx4j.convert.out.fo.pictures.float";

	/** @since 17.0.5 */
	public static boolean useFloats() {
		return org.docx4j.Docx4jProperties.getProperty(FLOAT_PROPERTY, true);
	}
			
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

	public FOConversionContext(FOSettings settings, WordprocessingMLPackage wmlPackage, ConversionSectionWrappers conversionSectionWrappers) throws CyclicStylesException {
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
	
	/** paraId to the number of blocks written for it (CR-012); null until the first. */
	private Map<String, Integer> paragraphFoIds;

	/** The paragraph being converted: its block id (null: no ids for it) and how much of
	 *  its text the runs met so far stand for (CR-012). */
	private static final class ParagraphFrame {
		final String foId;
		int offset = 0;
		ParagraphFrame(String foId) { this.foId = foId; }
	}
	private final java.util.ArrayDeque<ParagraphFrame> paragraphFrames = new java.util.ArrayDeque<ParagraphFrame>();

	/**
	 * The generator is about to convert this paragraph's content: returns the id for its
	 * block (see {@link #paragraphFoId(String)}), and opens the count its runs' ids are
	 * taken from.  Pair with {@link #endParagraph()}.  Nested paragraphs (a note's, a
	 * text box's) keep their own count.
	 *
	 * @since 17.1.1
	 */
	public String beginParagraph(String paraId) {
		String foId = paragraphFoId(paraId);
		paragraphFrames.push(new ParagraphFrame(foId));
		return foId;
	}

	public void endParagraph() {
		if (!paragraphFrames.isEmpty()) paragraphFrames.pop();
	}

	/**
	 * The id for this run's inline: {@code r-<paragraph id>-<offset>}, the offset being
	 * how many characters of the paragraph's text ({@link org.docx4j.model.pagination.RunText})
	 * the runs before it stand for; null where the paragraph has no id.  Counts the run
	 * off either way.
	 *
	 * @since 17.1.1
	 */
	public String runFoId(org.docx4j.wml.R r) {
		ParagraphFrame frame = paragraphFrames.peek();
		if (frame == null || frame.foId == null) return null; // no ids: nothing to count
		String id = PaginationAreaTreeHandler.FO_RUN_ID_PREFIX
				+ frame.foId.substring(PaginationAreaTreeHandler.FO_ID_PREFIX.length()) + "-" + frame.offset;
		frame.offset += org.docx4j.model.pagination.RunText.length(r);
		return id;
	}

	@Override
	public void enterTextBox() {
		super.enterTextBox();
		paragraphFrames.push(new ParagraphFrame(null)); // its runs are not the paragraph's
	}

	@Override
	public void exitTextBox() {
		super.exitTextBox();
		if (!paragraphFrames.isEmpty()) paragraphFrames.pop();
	}

	/**
	 * The {@code id} for this paragraph's fo:block, or null for none: the paragraph's
	 * {@code w14:paraId} prefixed, when {@link ConversionFeatures#PP_FO_PARAGRAPH_IDS} is
	 * on and the paragraph is the main document part's (a header or footer paragraph
	 * would recur on every page, and a note's paragraphs are not keyed) and not in a text
	 * box (laid out where the box is, not in the flow; not keyed either).  A paragraph
	 * written more than once (the preprocessing splits one at a page break inside it)
	 * gets a counter on its later ids, since ids must be unique in the FO document.
	 *
	 * @see PaginationAreaTreeHandler#foId(String, int)
	 * @since 17.1.1
	 */
	public String paragraphFoId(String paraId) {

		if (paraId == null || paraId.isEmpty()
				|| getConversionSettings() == null
				|| !getConversionSettings().getFeatures().contains(ConversionFeatures.PP_FO_PARAGRAPH_IDS)
				|| !(getCurrentPart() instanceof MainDocumentPart)
				|| isInTextBox()) {
			return null;
		}
		if (paragraphFoIds == null) paragraphFoIds = new HashMap<String, Integer>();
		Integer n = paragraphFoIds.get(paraId);
		int occurrence = (n == null ? 0 : n.intValue());
		paragraphFoIds.put(paraId, Integer.valueOf(occurrence + 1));
		return PaginationAreaTreeHandler.foId(paraId, occurrence);
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
	boolean numPagesUsed = false;
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
			/* A NUMPAGES in any section but the last becomes a
			 * fo:page-number-citation-last naming a *later* page-sequence, which FOP
			 * cannot resolve while it lays that section out - so it painted nothing at
			 * all where Word prints the total on every page.  The 2-pass path resolves
			 * the count from the area tree first, which is what it exists for.
			 * @since 17.1.0 */
			if (wrapper.getPageNumberInformation().isNumpagesPresent()
					&& (i < wrapperList.size()-1 || twoPassForNumpages())) {
				numPagesUsed = true;
			}
		}
		return (ret || numPagesUsed || ((sectionPagesUsed) && (wrapperList.size() > 1)));
	}

	/**
	 * Whether a NUMPAGES anywhere makes the conversion two-pass, so that the count is
	 * written as a literal the way Word prints it, rather than as an
	 * {@code fo:page-number-citation-last} FOP resolves after it has broken the line.
	 *
	 * <p>FOP reserves the width of "MMM" for an unresolved citation.  Measured on a
	 * corpus document whose footer is an image, "Trang ", the page number, "/" and the
	 * count: the line no longer fitted and wrapped, Word's "Trang 1/ 2" at y=775.7
	 * x=525.3..572.0 becoming "Trang 1/" at 761.9 and "2" at 775.6 x=40.5, and the
	 * footer region grew to 53.5pt, costing the body that much on every page.  A
	 * document of several sections was already two-pass for the same field.</p>
	 *
	 * <p>Set {@code docx4j.convert.out.fo.twoPassForNumpages} to false to keep the
	 * single pass (and the citation) where the second render costs too much.</p>
	 *
	 * @since 17.1.0
	 */
	private static boolean twoPassForNumpages() {
		return org.docx4j.Docx4jProperties.getProperty(
				"docx4j.convert.out.fo.twoPassForNumpages", true);
	}

}
