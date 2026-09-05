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
package org.docx4j.convert.out.common.wrappers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.convert.out.common.ConversionSectionWrapper;
import org.docx4j.convert.out.common.ConversionSectionWrappers;
import org.docx4j.convert.out.common.preprocess.PageNumberInformation;
import org.docx4j.convert.out.common.preprocess.PageNumberInformationCollector;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.STPageOrientation;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.SectPr.PgSz;
import org.jvnet.jaxb.lang.Child;

public class ConversionSectionWrapperFactory {
	
	protected static Logger log = LoggerFactory.getLogger(ConversionSectionWrapperFactory.class);
	
	protected static class SdtBlockFinder extends CallbackImpl {

		List<SdtBlock> sdtBlocks = new ArrayList<SdtBlock>();
		
		// Need a stack of these; only if we encounter a sectPr,
		// then copy contents of stack to list we remove.
		LinkedList<SdtBlock> ll = new LinkedList<SdtBlock>();
		
		@Override
		public List<Object> apply(Object o) {
			
			if (o instanceof org.docx4j.wml.P
				&& ((org.docx4j.wml.P)o).getPPr() != null 
				&& ((org.docx4j.wml.P)o).getPPr().getSectPr() != null ) {
				
				// this sdt contains a sectPr, so add it
				// and all its ancestor sdts to the ones we need to delete
				
				for (SdtBlock sdt : ll) {
					if (!sdtBlocks.contains(sdt) ) {
						sdtBlocks.add((SdtBlock)sdt);
					}
				}
				
			}

			return null;
		}
		
		@Override
		public void walkJAXBElements(Object parent) {
			
			List children = getChildren(parent);
			if (children != null) {

				for (Object o : children) {
					
					// if its wrapped in jakarta.xml.bind.JAXBElement, get its
					// value; this is ok, provided the results of the Callback
					// won't be marshalled
					o = XmlUtils.unwrap(o);
										
					this.apply(o);
					
					if (o instanceof SdtBlock) {
						ll.addLast((SdtBlock)o);
					}

					if (this.shouldTraverse(o)) {
						walkJAXBElements(o);
					}

					if (o instanceof SdtBlock) {
						ll.removeLast();
					}
					
				}
			}
		}
		
	}
	
	
	public static ConversionSectionWrappers process(WordprocessingMLPackage wmlPackage, boolean dummySections, boolean dummyPageNumbering) throws Docx4JException {
		
		List<ConversionSectionWrapper> conversionSections = null;
		Document document = wmlPackage.getMainDocumentPart().getContents();
		RelationshipsPart rels = wmlPackage.getMainDocumentPart().getRelationshipsPart();
		BooleanDefaultTrue evenAndOddHeaders = null;

		if ((wmlPackage.getMainDocumentPart().getDocumentSettingsPart() != null) &&
			(wmlPackage.getMainDocumentPart().getDocumentSettingsPart().getContents() != null)) {
			evenAndOddHeaders = wmlPackage.getMainDocumentPart().getDocumentSettingsPart().getContents().getEvenAndOddHeaders();
		}
		
		if (dummySections) {
			conversionSections = processDummy(wmlPackage, document, rels, evenAndOddHeaders, dummyPageNumbering);
		}
		else {
			conversionSections = processComplete(wmlPackage, document, rels, evenAndOddHeaders, dummyPageNumbering);
		}
		return new ConversionSectionWrappers(conversionSections);				
	}

	/** The dummy section wrappers only contains one section with all the document. Therefore
	 *  any sections within the document are ignored in the conversion process. As it doesn't 
	 *  need to check for sections it is faster and the html-Output only uses one section.<br>
	 *  It will use the Header/Footer of the body sectPr. This isn't correct, if there are 
	 *  several Sections in the document, but to find the correct SectPr it would need to check
	 *  the document content - and the aim of this method is a low overhead.  
	 * 
	 * @param wmlPackage
	 * @param dummyPageNumbering
	 * @return
	 */
	protected static List<ConversionSectionWrapper> processDummy(WordprocessingMLPackage wmlPackage, Document document, RelationshipsPart rels, BooleanDefaultTrue evenAndOddHeaders, boolean dummyPageNumbering) {
		
		log.debug("starting");
		
		List<ConversionSectionWrapper> conversionSections = new ArrayList<ConversionSectionWrapper>();
		ConversionSectionWrapper currentSectionWrapper = null;
		HeaderFooterPolicy previousHF =
				new HeaderFooterPolicy(document.getBody().getSectPr(), null, rels, evenAndOddHeaders);
	
		// SectionWrapper does work where sectPr is null (ie document has no body level sectPr),
		// so document.getBody().getSectPr() is ok
	
		currentSectionWrapper = createSectionWrapper(
				document.getBody().getSectPr(), previousHF, rels, evenAndOddHeaders,
				1, document.getBody().getContent(), dummyPageNumbering); 		
				conversionSections.add(currentSectionWrapper);
		return conversionSections;
	}
	
	/**
	 * @param sectPr
	 * @param headerFooterPolicy
	 * @param rels
	 * @param evenAndOddHeaders  from the document settings part
	 * @param conversionSectionIndex
	 * @param content
	 * @param dummyPageNumbering
	 * @return
	 */
	protected static ConversionSectionWrapper createSectionWrapper(
			SectPr sectPr, HeaderFooterPolicy headerFooterPolicy, RelationshipsPart rels, BooleanDefaultTrue evenAndOddHeaders, 
			int conversionSectionIndex, List<Object> content,
			boolean dummyPageNumbering) {
		
		ConversionSectionWrapper csw = 
					new ConversionSectionWrapper(sectPr, headerFooterPolicy, rels, evenAndOddHeaders,
					"s" + Integer.toString(conversionSectionIndex), content);
		
		PageNumberInformation pageNumberInformation = 
				PageNumberInformationCollector.process(csw, dummyPageNumbering);
		csw.setPageNumberInformation(pageNumberInformation);
		
		return csw;
	}
	
	protected static List<ConversionSectionWrapper> processComplete(WordprocessingMLPackage wmlPackage, Document document, 
			RelationshipsPart rels, BooleanDefaultTrue evenAndOddHeaders, boolean dummyPageNumbering) {
		
		log.debug("starting");

		// Get a list of all the sectPrs
		removeContentControls( document);
		List<SectPr> sectPrs =  getSectPrs(document);

		// Local vars
		List<ConversionSectionWrapper> conversionSections = new ArrayList<ConversionSectionWrapper>();
		ConversionSectionWrapper currentSectionWrapper = null;
		HeaderFooterPolicy previousHF = null;
		int conversionSectionIndex = 0;
				List<Object> sectionContent = new ArrayList<Object>();
		// the continuous sections merged into the wrapper being built
		List<MergedPart> columnParts = new ArrayList<MergedPart>();
		// Now go through the document content,
		
		int sectPrIndex = 0; // includes continuous ones
		for (Object o : document.getBody().getContent() ) {
			
			if (o instanceof org.docx4j.wml.P) {
				
				if (((org.docx4j.wml.P)o).getPPr() != null ) {
					
					org.docx4j.wml.PPr ppr = ((org.docx4j.wml.P)o).getPPr();
					if (ppr.getSectPr()!=null) {

						/* If the *following* section is continuous, 
						 * don't add *this* section, because we want our
						 * sectionWrapper to surround the content preceding
						 * both sectPr.
						 * 
						 * When we do eventually create that section wrapper,
						 * it must use the header/footer settings from 
						 * *this* section.
						 * 
						 * There is a special case to consider:
						 *  
						        <w:sectPr>
						          <w:type w:val="continuous"/>
						          <w:pgSz <---- different values from previous sectPr
						 *
						 * In this case, Word will render a page break, 
						 * but:
						 * 1. still show it as continuous
						 * 2. still use the headers/footers from this section
						 *  
						 */
						
						boolean ignoreThisSection = false;
						SectPr followingSectPr = sectPrs.get(++sectPrIndex);
						if ( followingSectPr.getType()!=null
								     && followingSectPr.getType().getVal().equals("continuous")) {

							log.info("following sectPr is continuous; this section wrapper must include its contents ");
							ignoreThisSection = true;
							
						} 
						
						
						if (ignoreThisSection) {
							// In case there are some headers/footers that apply to both this content and the 
							// content before the continuous sectPr,
							// or that need to get inherited by the section after the continuous sectPr 
							previousHF = new HeaderFooterPolicy(ppr.getSectPr(), previousHF, rels, evenAndOddHeaders);

							
							PgSz pgSzThis = ppr.getSectPr().getPgSz();
							PgSz pgSzNext = followingSectPr.getPgSz();
							boolean pageBreak = insertPageBreak( pgSzThis,  pgSzNext);
							if (pageBreak) {
								ppr.setPageBreakBefore(new BooleanDefaultTrue());
							}
							// Word gives the section break mark no line of its own where the
							// paragraph carrying it is otherwise empty; keep the paragraph
							// only where it has content, where it is all this section has,
							// or where it carries the page break inserted just above.
							boolean drop = !pageBreak && !sectionContent.isEmpty()
									&& rendersNothing((org.docx4j.wml.P)o);
							// this section's content (up to and including this paragraph) is
							// one part of the merged page-sequence, with its own column
							// count and page margins
							columnParts.add(new MergedPart(ppr.getSectPr(), sectionContent.size() + (drop ? 0 : 1)));
							//ppr.setSectPr(null); // Don't do this, since we have to process the docx (inc sectPrs) multiple times for a single PDF output
							if (drop) continue;

												} else {
							// The paragraph carrying the sectPr is the last paragraph of the
							// section it ends (in Word its mark is where the section break
							// shows), so it belongs in this section's content.  Until 17.0.5
							// it was added to the next section's, where it rendered as an
							// empty first line at the top of the new section's first page.
							// Word gives it no line at all when it is empty (the break mark
							// is all it is), and the empty block otherwise ends the flow -
							// and where it does not fit, starts a page of its own carrying
							// only the running header.
							if (sectionContent.isEmpty() || !rendersNothing((org.docx4j.wml.P)o)) {
								sectionContent.add(o);
							}
							Merged merged = spanColumnParts(sectionContent, columnParts, ppr.getSectPr());
							currentSectionWrapper = createSectionWrapper(
									ppr.getSectPr(), previousHF, rels, evenAndOddHeaders,
									++conversionSectionIndex, sectionContent, dummyPageNumbering);
							useWinningPartCols(currentSectionWrapper, columnParts, ppr.getSectPr(), merged.cols);
							if (merged.cols != colsNum(ppr.getSectPr())) {
								currentSectionWrapper.getPageDimensions().setColsNum(merged.cols);
							}
							usePartMargins(currentSectionWrapper, merged.marginRef);
							conversionSections.add(currentSectionWrapper);
							previousHF = currentSectionWrapper.getHeaderFooterPolicy();
							sectionContent = new ArrayList<Object>();
							columnParts = new ArrayList<MergedPart>();
							continue;
						}
					}
				}				
			} 
			sectionContent.add(o);
//			System.out.println(XmlUtils.marshaltoString(o, true));
		}
		
		/* Section wrapper for the document level sectPr, containing remaining content.
		 *
		 * Since 17.0.5 the paragraph carrying a sectPr belongs to the section it ends,
		 * so where the last paragraph of the body carries one, this last section has no
		 * content of its own.  Word renders nothing for it; an fo:flow with no block in
		 * it is invalid FO, and the export failed.
		 */
		if (sectionContent.isEmpty() && !conversionSections.isEmpty()) {
			log.debug("the document level sectPr has no content; nothing to render for it");
			return conversionSections;
		}

		Merged merged = spanColumnParts(sectionContent, columnParts, document.getBody().getSectPr());
		currentSectionWrapper = createSectionWrapper(
				document.getBody().getSectPr(), previousHF, rels, evenAndOddHeaders,
				++conversionSectionIndex, sectionContent, dummyPageNumbering);
		useWinningPartCols(currentSectionWrapper, columnParts, document.getBody().getSectPr(), merged.cols);
		if (merged.cols != colsNum(document.getBody().getSectPr())) {
			currentSectionWrapper.getPageDimensions().setColsNum(merged.cols);
		}
		usePartMargins(currentSectionWrapper, merged.marginRef);
		conversionSections.add(currentSectionWrapper);
		return conversionSections;
	}

	/** Tag of the container the FO exporter renders as a block spanning all columns. @since 17.0.5 */
	public static final String TAG_SPAN_ALL = "XSLT_Cols";

	/** Tag of the container the FO exporter renders with start-indent/end-indent, so
	 *  that a merged continuous section keeps its own page margins.  The value is
	 *  <code>XSLT_Ind=start,end</code> in twips, relative to the page-sequence's own
	 *  margins.  @since 17.0.5 */
	public static final String TAG_INDENT = "XSLT_Ind";

	/** One of the continuous sections merged into a single page-sequence. @since 17.0.5 */
	private static class MergedPart {
		final SectPr sectPr;
		/** end index (exclusive) of this part's content */
		final int end;
		MergedPart(SectPr sectPr, int end) {
			this.sectPr = sectPr;
			this.end = end;
		}
	}

	private static int colsNum(SectPr sectPr) {
		if (sectPr == null || sectPr.getCols() == null || sectPr.getCols().getNum() == null) return 1;
		return Math.max(1, sectPr.getCols().getNum().intValue());
	}

	private static int marginLeft(SectPr sectPr) {
		if (sectPr == null || sectPr.getPgMar() == null || sectPr.getPgMar().getLeft() == null) return -1;
		return sectPr.getPgMar().getLeft().intValue();
	}

	private static int marginRight(SectPr sectPr) {
		if (sectPr == null || sectPr.getPgMar() == null || sectPr.getPgMar().getRight() == null) return -1;
		return sectPr.getPgMar().getRight().intValue();
	}

	/**
	 * True where Word gives the paragraph no line of its own: it carries a
	 * section break mark and nothing else.  Bookmarks, comment/permission range
	 * markers, proofing marks and runs with no content render nothing, so they
	 * do not make the paragraph non-empty.
	 *
	 * @since 17.0.5
	 */
	private static boolean rendersNothing(org.docx4j.wml.P p) {
		if (p.getContent() == null) return true;
		for (Object child : p.getContent()) {
			Object o = XmlUtils.unwrap(child);
			if (o instanceof org.docx4j.wml.ProofErr
					|| o instanceof org.docx4j.wml.CTBookmark
					|| o instanceof org.docx4j.wml.CTMarkupRange
					|| o instanceof org.docx4j.wml.CTPerm) {
				continue;
			}
			if (o instanceof org.docx4j.wml.R) {
				for (Object rChild : ((org.docx4j.wml.R)o).getContent()) {
					Object r = XmlUtils.unwrap(rChild);
					if (r instanceof org.docx4j.wml.RPr
							|| r instanceof org.docx4j.wml.R.LastRenderedPageBreak) {
						continue;
					}
					return false;
				}
				continue;
			}
			return false;
		}
		return true;
	}

	/**
	 * A page-sequence made of several merged continuous sections takes its column
	 * count from the part that has the most columns (spanColumnParts), but the
	 * wrapper itself was built from the sectPr that ends the sequence - so until
	 * 17.0.5 the column gap came from that last section, which in the common case
	 * (columns, then one column again) is the one with no columns at all.  The gap
	 * and the count must come from the same section, so copy that part's whole
	 * w:cols across.
	 *
	 * @param lastSectPr the sectPr that ends the page-sequence
	 * @param cols the column count the wrapper will use
	 * @since 17.0.5
	 */
	private static void useWinningPartCols(ConversionSectionWrapper wrapper, List<MergedPart> columnParts,
			SectPr lastSectPr, int cols) {
		if (wrapper == null || columnParts.isEmpty() || cols < 2) return;
		if (colsNum(lastSectPr) == cols) return; // the wrapper's own w:cols already won
		for (MergedPart part : columnParts) {
			if (colsNum(part.sectPr) == cols && part.sectPr.getCols() != null) {
				wrapper.getPageDimensions().setCols(part.sectPr.getCols());
				return;
			}
		}
	}

	/**
	 * The page masters of a page-sequence made of several continuous sections can
	 * only carry one set of page margins; until 17.0.5 that was the last section's,
	 * applied to all the content before it as well.  Word starts the page with the
	 * first of them, so that is normally what the sequence uses (see
	 * {@link #marginReference}); the other parts carry the difference as indents
	 * (spanColumnParts).
	 *
	 * @param part the section whose w:pgMar the masters take; null leaves the wrapper's own
	 * @since 17.0.5
	 */
	private static void usePartMargins(ConversionSectionWrapper wrapper, SectPr part) {
		if (wrapper == null || part == null || part.getPgMar() == null) return;
		if (marginLeft(part) < 0 || marginRight(part) < 0) return;
		// on a copy of the wrapper's own (which has the values the last section's
		// sectPr, or the defaults, gave it, so nothing that is unset here goes missing)
		SectPr.PgMar pgMar = XmlUtils.deepCopy(wrapper.getPageDimensions().getPgMar());
		pgMar.setLeft(part.getPgMar().getLeft());
		pgMar.setRight(part.getPgMar().getRight());
		if (part.getPgMar().getTop() != null) pgMar.setTop(part.getPgMar().getTop());
		if (part.getPgMar().getBottom() != null) pgMar.setBottom(part.getPgMar().getBottom());
		if (part.getPgMar().getHeader() != null) pgMar.setHeader(part.getPgMar().getHeader());
		if (part.getPgMar().getFooter() != null) pgMar.setFooter(part.getPgMar().getFooter());
		wrapper.getPageDimensions().setPgMar(pgMar);
	}

	/** What merging a run of continuous sections into one page-sequence came to.
	 *  @since 17.0.6 */
	private static class Merged {
		/** the column count the page-sequence must use */
		final int cols;
		/** the section whose w:pgMar the page masters take, or null for the wrapper's own */
		final SectPr marginRef;
		Merged(int cols, SectPr marginRef) {
			this.cols = cols;
			this.marginRef = marginRef;
		}
	}

	/**
	 * Which of the merged sections' w:pgMar the page-sequence is built on.
	 *
	 * <p>Word starts the page with the <b>first</b> of them, and 17.0.5 used that, the
	 * other parts carrying the difference as indents - which puts every line where Word
	 * puts it, because a part's measure is the region body less its own indents whatever
	 * the region body is.  The region body still bounds one thing the indents cannot
	 * move: the <b>columns</b>. So where the sequence has a multi-column part whose own
	 * text column is wider than the first part's, the page masters are built on that
	 * part instead.  Measured: a certificate whose section 1 has 152/186pt margins and
	 * one column, and whose continuous section 2 has 51/45pt margins and two columns of
	 * 157 + 24 + 318pt, needs a 499pt region body for those columns; built on section 1
	 * it was 257pt and the two columns came out 116.5pt wide, with the blocks' negative
	 * end-indents letting the text overflow them.</p>
	 *
	 * @since 17.0.6
	 */
	private static SectPr marginReference(List<SectPr> sectPrs, int[] cols, int max) {
		SectPr first = sectPrs.get(0);
		if (max < 2 || marginLeft(first) < 0 || marginRight(first) < 0) return first;
		SectPr widest = null;
		for (int i = 0; i < sectPrs.size(); i++) {
			SectPr sp = sectPrs.get(i);
			if (cols[i] != max || marginLeft(sp) < 0 || marginRight(sp) < 0) continue;
			if (widest == null
					|| marginLeft(sp) + marginRight(sp) < marginLeft(widest) + marginRight(widest)) {
				widest = sp;
			}
		}
		if (widest == null) return first;
		return (marginLeft(widest) + marginRight(widest) < marginLeft(first) + marginRight(first))
				? widest : first;
	}

	/**
	 * A page-sequence cannot change its column count or its page margins mid-page,
	 * but a block in it can span all its columns, and can be indented.  So when
	 * continuous sections were merged into one wrapper, the page-sequence takes the
	 * largest column count and the first section's margins, and each part that
	 * differs is wrapped in a container the FO exporter renders accordingly:
	 * <ul>
	 * <li>fewer columns (tag XSLT_Cols=n): fo:block span="all"; FOP balances the
	 * columns before it, as Word does at a continuous break.  Word's common case
	 * (1 column, then a 2-column stretch, then 1 column again) comes out right; a
	 * 2-column part under a 3-column count is spanned too (an approximation).</li>
	 * <li>other page margins (tag XSLT_Ind=start,end in twips): an
	 * fo:block-container indented by the difference, which is a new reference area,
	 * so the indents of the paragraphs and tables inside it still work.</li>
	 * </ul>
	 * Sections that agree in both are left alone.
	 *
	 * <p>Since 17.0.6 a part whose w:cols declares columns of different widths becomes a
	 * one-row table instead ({@link UnequalColumns}), and so counts as one column here;
	 * and the margins the masters take are {@link #marginReference}'s rather than always
	 * the first part's.</p>
	 *
	 * @param columnParts the merged parts, in order
	 * @param lastSectPr the sectPr that ends the page-sequence (the last part)
	 * @return the column count the wrapper should use
	 * @since 17.0.5
	 */
	private static Merged spanColumnParts(List<Object> content, List<MergedPart> columnParts, SectPr lastSectPr) {

		// the parts of the page-sequence, in order, each with its own sectPr
		List<List<Object>> parts = new ArrayList<List<Object>>();
		List<SectPr> sectPrs = new ArrayList<SectPr>();
		int start = 0;
		for (MergedPart part : columnParts) {
			int end = Math.min(part.end, content.size());
			parts.add(new ArrayList<Object>(content.subList(start, end)));
			sectPrs.add(part.sectPr);
			start = end;
		}
		parts.add(new ArrayList<Object>(content.subList(start, content.size())));
		sectPrs.add(lastSectPr);

		// a stretch of unequal columns becomes a one-row table, and so one column
		int[] cols = new int[parts.size()];
		boolean asTables = false;
		for (int i = 0; i < parts.size(); i++) {
			cols[i] = colsNum(sectPrs.get(i));
			List<Object> table = UnequalColumns.asOneRowTable(parts.get(i), sectPrs.get(i));
			if (table != null) {
				parts.set(i, table);
				cols[i] = 1;
				asTables = true;
			}
		}
		int max = 1;
		for (int c : cols) max = Math.max(max, c);

		if (columnParts.isEmpty()) {
			if (asTables) {
				content.clear();
				content.addAll(parts.get(0));
			}
			return new Merged(max, null);
		}

		SectPr ref = marginReference(sectPrs, cols, max);
		int refLeft = marginLeft(ref), refRight = marginRight(ref);
		boolean sameMargins = true;
		for (SectPr sectPr : sectPrs) {
			if (marginLeft(sectPr) != refLeft || marginRight(sectPr) != refRight) sameMargins = false;
		}
		if (refLeft < 0 || refRight < 0) sameMargins = true; // nothing to compare against

		boolean uniformCols = true;
		for (int c : cols) if (c != max) uniformCols = false;
		if (uniformCols && sameMargins && !asTables) return new Merged(max, ref);

		List<Object> result = new ArrayList<Object>();
		for (int i = 0; i < parts.size(); i++) {
			addPart(result, parts.get(i), sectPrs.get(i), cols[i], max, sameMargins, refLeft, refRight);
		}
		content.clear();
		content.addAll(result);
		return new Merged(max, ref);
	}

	private static void addPart(List<Object> result, List<Object> part, SectPr sectPr, int partCols,
			int max, boolean sameMargins, int refLeft, int refRight) {
		if (part.isEmpty()) return;
		List<Object> content = new ArrayList<Object>(part);
		if (!sameMargins
				&& (marginLeft(sectPr) != refLeft || marginRight(sectPr) != refRight)
				&& marginLeft(sectPr) >= 0 && marginRight(sectPr) >= 0) {
			content = wrap(content,
					TAG_INDENT + "=" + (marginLeft(sectPr) - refLeft) + "," + (marginRight(sectPr) - refRight));
		}
		if (partCols < max) {
			content = wrap(content, TAG_SPAN_ALL + "=" + partCols);
		}
		result.addAll(content);
	}

	/** The content in an SdtBlock carrying this tag (the containers the FO exporter renders). */
	private static List<Object> wrap(List<Object> content, String tagVal) {
		SdtBlock sdt = Context.getWmlObjectFactory().createSdtBlock();
		org.docx4j.wml.SdtPr sdtPr = Context.getWmlObjectFactory().createSdtPr();
		org.docx4j.wml.Tag tag = Context.getWmlObjectFactory().createTag();
		tag.setVal(tagVal);
		sdtPr.setTag(tag);
		sdt.setSdtPr(sdtPr);
		org.docx4j.wml.SdtContentBlock sdtContent = Context.getWmlObjectFactory().createSdtContentBlock();
		sdtContent.getContent().addAll(content);
		sdt.setSdtContent(sdtContent);
		List<Object> wrapped = new ArrayList<Object>();
		wrapped.add(sdt);
		return wrapped;
	}

	private static boolean insertPageBreak(PgSz pgSzThis, PgSz pgSzNext) {

		boolean insertPageBreak = false;
		
		// If the w:pgSz on the two sections differs, 
		// then Word inserts a page break (ie doesn't treat it as continuous).
		// If no w:pgSz element is present, then Word defaults
		// (presumably to Legal? TODO CHECK. There is no default setting in the docx).
		// Word always inserts a w:pgSz element?

		
		if (pgSzThis!=null && pgSzNext!=null) {
			
			if (pgSzThis.getH().compareTo(pgSzNext.getH())!=0) {
				insertPageBreak = true;
			}
			if (pgSzThis.getW().compareTo(pgSzNext.getW())!=0) {
				insertPageBreak = true;
			}
			
			// Orientation:default is portrait
			boolean portraitThis = true;
			if (pgSzThis.getOrient()!=null) {
				portraitThis=pgSzThis.getOrient().equals(STPageOrientation.PORTRAIT);
			}
			boolean portraitNext = true;
			if (pgSzNext.getOrient()!=null) {
				portraitNext=pgSzNext.getOrient().equals(STPageOrientation.PORTRAIT);
			}
			if (portraitThis!=portraitNext) {
				insertPageBreak = true;									
			}
			
		}
		// TODO: handle cases where one or both pgSz elements are missing,
		// or H or W is missing.
		// Treat pgSz element missing as Legal size?
		return insertPageBreak;
	}
	
	
	private static void removeContentControls(Document document) {

		// First, remove content controls, 
		// since the P could be in a content control.
		// (It is easier to remove content controls, than
		//  to make the code below TraversalUtil based)
		// RemovalHandler is an XSLT-based way of doing this,
		// but here we avoid introducing a dependency on
		// XSLT (Xalan) for PDF output.
		SdtBlockFinder sbr = new SdtBlockFinder();
		new TraversalUtil(document.getContent(), sbr);
		for( int i=sbr.sdtBlocks.size()-1 ; i>=0; i--) {
			// Have to process in reverse order
			// so that parentList is correct for nested sdt
			
			SdtBlock sdtBlock = sbr.sdtBlocks.get(i);
			Object parent = sdtBlock.getParent();
			List<Object> parentList = null;
			if (parent instanceof List) {
				parentList = (List<Object>)parent;
			} else if (parent instanceof ContentAccessor) {
				// eg a w:sdt which is a child of w:body: the parent pointer is the
				// object, not its content list.  Until 17.0.5 this aborted the export
				// with a NullPointerException.
				parentList = ((ContentAccessor)parent).getContent();
			} else {
				log.error("Can't unwrap w:sdt: unexpected parent "
						+ (parent==null ? "null" : parent.getClass().getName()));
				continue;
			}
			int index = parentList.indexOf(sdtBlock);
			if (index<0) {
				log.error("Can't unwrap w:sdt: not found in its parent's content");
				continue;
			}
			parentList.remove(index);
			parentList.addAll(index, sdtBlock.getSdtContent().getContent());				
		}
		
//		if (log.isDebugEnabled()) {
//			log.debug(XmlUtils.marshaltoString(document, true, true));
//		}
		
	}
	
	private static List<SectPr> getSectPrs(Document document) {

		// According to the ECMA-376 2ed, if type is not specified, read it as next page
		// However Word 2007 sometimes treats it as continuous, and sometimes doesn't??	
		// 20130216 Review above comment: !  In the Word UI, the Word "continuous" is shown where it is effective
		// (except 20140517 where the page sizes differ, so that it says continuous but inserts a break!)
		// In the XML, it is stored in the next following sectPr.

		
		// Make a list, so it is easy to look at the following sectPr,
		// which we need to do to handle continuous sections properly
		List<SectPr> sectPrs = new ArrayList<SectPr>();
		for (Object o : document.getBody().getContent() ) {
			
			if (o instanceof org.docx4j.wml.P) {
				if (((org.docx4j.wml.P)o).getPPr() != null ) {
					org.docx4j.wml.PPr ppr = ((org.docx4j.wml.P)o).getPPr();
					if (ppr.getSectPr()!=null) {
						sectPrs.add(ppr.getSectPr());
					}
				}				
			} 
		}
		
		if (document.getBody().getSectPr()!=null) {
			// usual case
			sectPrs.add(document.getBody().getSectPr()); 
			
		} else {
			log.debug("No body level sectPr in document");
			
			// OK if the last object is w:p and it contains a sectPr.
			List<Object> all = document.getBody().getContent();
	    	Object last = null;
	    	if (all.size()>0) {
	    		last = all.get( all.size()-1 );
	    	}
	    	if (last !=null
	    			&& last instanceof P 
	    			&& ((P) last).getPPr()!=null 
	    				&& ((P) last).getPPr().getSectPr() !=null) {
	    			// ok
				log.debug(".. but last p contains sectPr .. move it"); // so our assumption later about there being a following section is correct

				SectPr thisSectPr = ((P) last).getPPr().getSectPr();
				document.getBody().setSectPr(thisSectPr);
				((P) last).getPPr().setSectPr(null);
				sectPrs.remove(thisSectPr);
	    	
	    	} else {			
				document.getBody().setSectPr(Context.getWmlObjectFactory().createSectPr());
				sectPrs.add(document.getBody().getSectPr()); 
	    	}
		}
		
		return sectPrs;
	}
	

}
