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

import org.docx4j.UnitsOfMeasurement;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.ConversionSectionWrapper;
import org.docx4j.convert.out.common.ConversionSectionWrappers;
import org.docx4j.convert.out.common.preprocess.PartialDeepCopy;
import org.docx4j.events.EventFinished;
import org.docx4j.events.StartEvent;
import org.docx4j.events.WellKnownProcessSteps;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.PageDimensions;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.plutext.jaxb.xslfo.ConditionalPageMasterReference;
import org.plutext.jaxb.xslfo.LayoutMasterSet;
import org.plutext.jaxb.xslfo.ObjectFactory;
import org.plutext.jaxb.xslfo.OddOrEvenType;
import org.plutext.jaxb.xslfo.PagePositionType;
import org.plutext.jaxb.xslfo.PageSequenceMaster;
import org.plutext.jaxb.xslfo.RegionAfter;
import org.plutext.jaxb.xslfo.RegionBefore;
import org.plutext.jaxb.xslfo.RegionBody;
import org.plutext.jaxb.xslfo.RepeatablePageMasterAlternatives;
import org.plutext.jaxb.xslfo.SimplePageMaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;



/**
 * A description of how this stuff works
 * can be found at /docs/headers_footers.docx
 * 
 * Its not possible to let FOP set the height (@extent) of the header and footer regions 
 * automatically: http://apache-fop.1065347.n5.nabble.com/Auto-size-header-to-fit-with-content-td4455.html 
 * 
 * So we need to set the height (@extent) of the header and footer regions explicitly.
 * 
 * We do that by creating a temp FO file which contains essentially just the headers/footers,
 * then interrogating FOP's area tree representation to find the height of each.
 * 
 * So:
 * 1. create LayoutMasterSet (with large extents)
 * 2. generate area tree from that
 * 3. return LayoutMasterSet (with required extents)
 * 
 * @author jharrop
 *
 */
public class LayoutMasterSetBuilder {

	protected static Logger log = LoggerFactory.getLogger(LayoutMasterSetBuilder.class);
	
	private static org.plutext.jaxb.xslfo.ObjectFactory factory;
		
	public static DocumentFragment getLayoutMasterSetFragment(AbstractWmlConversionContext context) {

		LayoutMasterSet lms = getFoLayoutMasterSet(context);	
		
		// Set suitable extents, for which we need area tree 
		FOSettings foSettings = (FOSettings)context.getConversionSettings();
		if ( !foSettings.lsLayoutMasterSetCalculationInProgress()) // Avoid infinite loop
			// Can't just do it where foSettings.getApacheFopMime() is not MimeConstants.MIME_FOP_AREA_TREE,
			// since TOC functionality uses that.
		{
			fixExtents( lms, context, true);
			addMirroredEvenMasters( lms, context );
		}
		
		org.w3c.dom.Document document = XmlUtils.marshaltoW3CDomDocument(lms, Context.getXslFoContext() );
		DocumentFragment docfrag = document.createDocumentFragment();
		docfrag.appendChild(document.getDocumentElement());
		
		
		return docfrag;		
	}
	
	private static void fixExtents(LayoutMasterSet lms, AbstractWmlConversionContext context, boolean useXSLT) {
		
		WordprocessingMLPackage wordMLPackage = context.getWmlPackage();

		StartEvent startEvent = new StartEvent( wordMLPackage, WellKnownProcessSteps.FO_EXTENTS );
		startEvent.publish();
		
//		log.debug(wordMLPackage.getMainDocumentPart().getXML());

        if(log.isDebugEnabled()) {
            log.debug("incoming LMS: " + XmlUtils.marshaltoString(lms, Context.getXslFoContext()));
        }
		
		// Make a copy of it
		Set<String> relationshipTypes = new TreeSet<String>();
			relationshipTypes.add(Namespaces.DOCUMENT);
			relationshipTypes.add(Namespaces.HEADER);
			relationshipTypes.add(Namespaces.FOOTER);
			//those are probably not affected but get visited by the 
			//default TraversalUtil.
			relationshipTypes.add(Namespaces.ENDNOTES);
			relationshipTypes.add(Namespaces.FOOTNOTES);
			relationshipTypes.add(Namespaces.COMMENTS);
			
		WordprocessingMLPackage hfPkg;
		try {
			hfPkg = (WordprocessingMLPackage) PartialDeepCopy.process(wordMLPackage, relationshipTypes);
			
			FOPAreaTreeHelper.trimContent(hfPkg);
			FOPAreaTreeHelper.dropFloatingDrawingsFromHeadersFooters(hfPkg);
			
			FOSettings foSettings = (FOSettings)context.getConversionSettings();
			org.w3c.dom.Document areaTree = FOPAreaTreeHelper.getAreaTreeViaFOP( hfPkg, useXSLT, foSettings);
			
			log.debug(XmlUtils.w3CDomNodeToString(areaTree));
			
			Map<String, Integer> headerBpda = new HashMap<String, Integer>();
			Map<String, Integer> footerBpda = new HashMap<String, Integer>();
			
			FOPAreaTreeHelper.calculateHFExtents(areaTree,  headerBpda,  footerBpda);
			
			FOPAreaTreeHelper.adjustLayoutMasterSet(lms, context.getSections(), headerBpda, footerBpda);				
			
			/* Don't deleteEmbeddedFontTempFiles here, since they may be required by FOP later.
			 * And they should get deleted later anyway...
			 * 
				FontTablePart ftp = hfPkg.getMainDocumentPart().getFontTablePart();
				if (ftp!=null) {
					ftp.deleteEmbeddedFontTempFiles();
				}
			*/
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			/* The pre-pass could not measure the headers and footers, and until 17.0.6
			 * the dummy extents it started with survived: each region then took about
			 * half the page, leaving a body strip a couple of lines high.  One corpus
			 * document (whose docDefaults carry w:vanish, so the trimmed package
			 * produced an invalid empty fo:flow) came out as 35 pages for Word's 3.
			 * A header or footer we could not measure is much better treated as absent.
			 * @since 17.0.6 */
			try {
				zeroExtents(lms, context.getSections());
			} catch (Exception e2) {
				log.error(e2.getMessage(), e2);
			}
		}
        if(log.isDebugEnabled()) {
            log.debug("resulting LMS: " + XmlUtils.marshaltoString(lms, Context.getXslFoContext()));
        }
		
		new EventFinished(startEvent).publish();
		
	}
	
	/**
	 * Give every header and footer region a zero extent, and the body the margins
	 * w:pgMar asks for: what {@link FOPAreaTreeHelper#adjustLayoutMasterSet} would do
	 * had it measured every region as empty.  Used only when the extent pre-pass fails.
	 *
	 * @since 17.0.6
	 */
	private static void zeroExtents(LayoutMasterSet lms, ConversionSectionWrappers sections) {
		Map<String, Integer> zeroes = new HashMap<String, Integer>();
		for (Object o : lms.getSimplePageMasterOrPageSequenceMaster()) {
			if (o instanceof SimplePageMaster) {
				zeroes.put(((SimplePageMaster)o).getMasterName(), Integer.valueOf(0));
			}
		}
		FOPAreaTreeHelper.adjustLayoutMasterSet(lms, sections, zeroes, zeroes);
	}

    /**
     * For XSLFOExporterNonXSLT
     * @since 3.0
     *
     */
	public static void appendLayoutMasterSetFragment(AbstractWmlConversionContext context, Node foRoot) {

		LayoutMasterSet lms = getFoLayoutMasterSet(context);	
		
		// Set suitable extents, for which we need area tree 
		FOSettings foSettings = (FOSettings)context.getConversionSettings();
		if ( !foSettings.lsLayoutMasterSetCalculationInProgress()) // Avoid infinite loop
			// Can't just do it where foSettings.getApacheFopMime() is not MimeConstants.MIME_FOP_AREA_TREE,
			// since TOC functionality uses that.
		{
			fixExtents( lms, context, false);
			addMirroredEvenMasters( lms, context );
		}
		
		org.w3c.dom.Document document = XmlUtils.marshaltoW3CDomDocument(lms, Context.getXslFoContext() );
		XmlUtils.treeCopy(document.getDocumentElement(), foRoot);
	}

	
	private static LayoutMasterSet getFoLayoutMasterSet(AbstractWmlConversionContext context) {
		
		
		LayoutMasterSet lms = getFactory().createLayoutMasterSet();
		List<ConversionSectionWrapper> sections = context.getSections().getList();
		ConversionSectionWrapper section = null;

		/* w:gutterAtTop and w:mirrorMargins are document settings, not section ones;
		 * they decide which edge w:pgMar/@w:gutter is added to.  @since 17.0.6 */
		boolean gutterAtTop = settingIsOn(context, "gutterAtTop");
		boolean mirrorMargins = settingIsOn(context, "mirrorMargins");

		/* Whether FOP's folio number and Word's page number have opposite parity from
		 * this section on - see foliosInverted.  @since 17.0.6 */
		boolean foliosInverted = false;

		for(int i=0; i<sections.size(); i++) {
			
			section = sections.get(i);
			HeaderFooterPolicy hf = section.getHeaderFooterPolicy();
			String sectionName = "s" + Integer.toString(i + 1);
			
			// FIRST, create simple-page-masters
			// has first header or footer?
			if (hf.getFirstHeader()!=null || hf.getFirstFooter()!=null) {
				// per spec, HeaderFooterPolicy checks the titlePg elememt
				
				lms.getSimplePageMasterOrPageSequenceMaster().add(
					createSimplePageMaster(sectionName + "-firstpage", 
							section.getPageDimensions(), 
							"firstpage",
						(hf.getFirstHeader()!=null),
						(hf.getFirstFooter()!=null) , gutterAtTop, mirrorMargins));
			}
			
			// has even or odd header or footer?
    		/*
    		 *       <w:headerReference w:type="even" r:id="rId12"/>
    		 *       <w:headerReference w:type="default" r:id="rId13"/>
    		 *       
    		 *       the default one is treated as odd.
    		 */
			if (hf.getEvenHeader()!=null || hf.getEvenFooter()!=null) {
				
				lms.getSimplePageMasterOrPageSequenceMaster().add(
					createSimplePageMaster(sectionName + "-evenpage",  
							section.getPageDimensions(), 
							"evenpage",
						(hf.getEvenHeader()!=null),
						(hf.getEvenFooter()!=null) , gutterAtTop, mirrorMargins));
				
				// the xslt outputs a "-default" page as the odd-page
			}
			
			if (hf.getDefaultHeader()!=null 
					|| hf.getDefaultFooter()!=null) {
				
				lms.getSimplePageMasterOrPageSequenceMaster().add(
					createSimplePageMaster(sectionName + "-default",  
							section.getPageDimensions(), 
							"default",
						(hf.getDefaultHeader()!=null),
						(hf.getDefaultFooter()!=null) , gutterAtTop, mirrorMargins));				
			}

			// simple: no headers and footers - after the first page anyway/
			// We still need this where there is just a first page header/footer,
			// since otherwise there'd be no page sequence for any content 
			// after the first page, and you'd get: 
			//    org.apache.fop.fo.pagination.PageProductionException: 
			//    Subsequences exhausted in page-sequence-master ..., cannot recover.
			//
			// <w:sectPr>
			//   <w:headerReference w:type="first" r:id="rId7"/>
			// </w:sectPr>			
			if (
				(hf.getDefaultHeader() == null) && (hf.getDefaultFooter() == null)) {
				lms.getSimplePageMasterOrPageSequenceMaster().add(
						createSimplePageMaster(sectionName + "-simple",  
								section.getPageDimensions(), 
								"simple",
							true, true, gutterAtTop, mirrorMargins));
			}
			
			// SECOND, create page-sequence-masters
			foliosInverted = foliosInverted(section, foliosInverted);
			lms.getSimplePageMasterOrPageSequenceMaster().add(
					createPageSequenceMaster(hf, sectionName, foliosInverted )  );
		}
		
		// 
		
		return lms;
	}
	
	/**
	 * Whether FOP's folio number is of the opposite parity to Word's page number, from
	 * this section on.
	 *
	 * <p>{@code fo:page-sequence/@initial-page-number} must be at least 1 (XSL 1.1
	 * &#xa7;6.4.5: "a positive integer"), and FOP clamps anything smaller.  A section
	 * with {@code <w:pgNumType w:start="0"/>} - Word's usual way of writing a cover page
	 * that is "page 0" so that the first numbered page is 1 - therefore lays out as
	 * folio 1 where Word calls it page 0, and every {@code odd-or-even} page master
	 * alternative after it selects the wrong one.  Measured on a document whose
	 * {@code sectPr} has {@code w:evenAndOddHeaders}, an even header with no
	 * {@code w:jc} and a right-aligned default (odd) header: Word's page 2 header is
	 * right-aligned at x=430.3..524.7 and ours was the even one at x=70.9..162.9, page 3
	 * the mirror image - the whole document's headers on the wrong side.</p>
	 *
	 * <p>Only the parity can be repaired here, by swapping the ODD and EVEN
	 * alternatives.  The printed number cannot: {@code fo:page-number} is formatted by
	 * FOP from the folio it clamped, and nothing in XSL-FO offsets it, so a
	 * {@code PAGE} field in such a section still prints one too high.  (The two-pass
	 * literal that carries NUMPAGES is one value for the whole section, and PAGE
	 * differs on every page of it, so it cannot carry PAGE.)  Recorded as an FOP
	 * limitation in word-layout-rules.md &#xa7;10.</p>
	 *
	 * @param inheritedInversion whether the sections before this one already inverted it
	 * @since 17.0.6
	 */
	private static boolean foliosInverted(ConversionSectionWrapper section, boolean inheritedInversion) {
		if (!oddEvenParityFix()) return false;
		int start = section.getPageNumberInformation().getPageStart();
		if (start < 0) return inheritedInversion;  // numbering continues from the section before
		// FOP renders this section's first page as folio max(1, start)
		return ((Math.max(1, start) - start) % 2) != 0;
	}

	/**
	 * With {@code w:settings/w:mirrorMargins}, every page master that is not already
	 * chosen by page parity gains a mirrored twin for the even (left-hand) pages, whose
	 * left and right margins are the other way round.
	 *
	 * <p>The twin is a copy of the finished master - taken <em>after</em> the extent
	 * pre-pass, so it carries the measured header and footer extents and, keeping the
	 * region names, is served by the same {@code fo:static-content}.  (Building it
	 * before the pre-pass would have left it with no measurement of its own: the
	 * pre-pass renders one page per section, so a master only an even page uses is
	 * never exercised and keeps the dummy half-page extent.)</p>
	 *
	 * <p>Where the section already has an even master - {@code w:evenAndOddHeaders} -
	 * the alternatives select on parity already and
	 * {@link #createSimplePageMaster} has mirrored that master's margins itself.</p>
	 *
	 * <p>Measured against Word 365 on a 42-page document whose three {@code sectPr} all
	 * say {@code w:left="2268" w:right="1418"}: Word's even pages start at x=70.8 and
	 * ours at 113.4 - 42.6pt out on half the document.</p>
	 *
	 * @since 17.0.6
	 */
	private static void addMirroredEvenMasters(LayoutMasterSet lms, AbstractWmlConversionContext context) {

		if (!settingIsOn(context, "mirrorMargins")) return;
		if (!org.docx4j.Docx4jProperties.getProperty("docx4j.convert.out.fo.mirrorMargins", true)) return;

		List<Object> items = lms.getSimplePageMasterOrPageSequenceMaster();
		Map<String, SimplePageMaster> byName = new HashMap<String, SimplePageMaster>();
		for (Object o : items) {
			if (o instanceof SimplePageMaster) byName.put(((SimplePageMaster)o).getMasterName(), (SimplePageMaster)o);
		}

		List<ConversionSectionWrapper> sections = context.getSections().getList();
		List<Object> added = new java.util.ArrayList<Object>();
		boolean foliosInverted = false;

		for (int i=0; i<sections.size(); i++) {
			String sectionName = "s" + Integer.toString(i + 1);
			boolean inverted = foliosInverted(sections.get(i), foliosInverted);
			foliosInverted = inverted;
			if (byName.containsKey(sectionName + "-evenpage")) continue; // parity already decides

			for (Object o : items) {
				if (!(o instanceof PageSequenceMaster)) continue;
				PageSequenceMaster psm = (PageSequenceMaster)o;
				if (!sectionName.equals(psm.getMasterName())) continue;
				for (Object alt : psm.getSinglePageMasterReferenceOrRepeatablePageMasterReferenceOrRepeatablePageMasterAlternatives()) {
					if (!(alt instanceof RepeatablePageMasterAlternatives)) continue;
					List<ConditionalPageMasterReference> refs =
							((RepeatablePageMasterAlternatives)alt).getConditionalPageMasterReference();
					List<ConditionalPageMasterReference> rebuilt =
							new java.util.ArrayList<ConditionalPageMasterReference>(refs.size() + 1);
					for (ConditionalPageMasterReference ref : refs) {
						// the first-page master and anything already chosen by parity stand
						SimplePageMaster src = byName.get(ref.getMasterReference());
						if (src == null || ref.getPagePosition() != null || ref.getOddOrEven() != null) {
							rebuilt.add(ref);
							continue;
						}
						SimplePageMaster mirror = mirrorOf(src);
						added.add(mirror);

						ConditionalPageMasterReference even = getFactory().createConditionalPageMasterReference();
						even.setMasterReference(mirror.getMasterName());
						even.setOddOrEven(inverted ? OddOrEvenType.ODD : OddOrEvenType.EVEN);
						rebuilt.add(even);

						ref.setOddOrEven(inverted ? OddOrEvenType.EVEN : OddOrEvenType.ODD);
						rebuilt.add(ref);
					}
					refs.clear();
					refs.addAll(rebuilt);
				}
			}
		}
		items.addAll(added);
	}

	/**
	 * A copy of this page master with its left and right margins the other way round,
	 * named "&lt;name&gt;-mirrored".  Built field by field rather than by marshalling:
	 * the XSL-FO model's page masters are not root elements, so a JAXB round-trip copy
	 * of one fails (measured: three corpus documents came out as an export exception).
	 * The regions are new objects too, so the two masters share no JAXB node, but they
	 * keep the <em>same</em> region names - which is the point, since that is what makes
	 * one {@code fo:static-content} serve both.
	 *
	 * @since 17.0.6
	 */
	private static SimplePageMaster mirrorOf(SimplePageMaster src) {

		SimplePageMaster mirror = getFactory().createSimplePageMaster();
		mirror.setMasterName(src.getMasterName() + "-mirrored");
		mirror.setPageHeight(src.getPageHeight());
		mirror.setPageWidth(src.getPageWidth());
		mirror.setMarginTop(src.getMarginTop());
		mirror.setMarginBottom(src.getMarginBottom());
		mirror.setMarginLeft(src.getMarginRight());
		mirror.setMarginRight(src.getMarginLeft());

		RegionBody srcBody = src.getRegionBody();
		if (srcBody!=null) {
			RegionBody rb = getFactory().createRegionBody();
			rb.setMarginTop(srcBody.getMarginTop());
			rb.setMarginBottom(srcBody.getMarginBottom());
			rb.setMarginLeft(srcBody.getMarginLeft());
			rb.setMarginRight(srcBody.getMarginRight());
			rb.setColumnCount(srcBody.getColumnCount());
			rb.setColumnGap(srcBody.getColumnGap());
			rb.setDisplayAlign(srcBody.getDisplayAlign());
			rb.setRegionName(srcBody.getRegionName());
			mirror.setRegionBody(rb);
		}
		if (src.getRegionBefore()!=null) {
			RegionBefore rBefore = getFactory().createRegionBefore();
			rBefore.setRegionName(src.getRegionBefore().getRegionName());
			rBefore.setExtent(src.getRegionBefore().getExtent());
			mirror.setRegionBefore(rBefore);
		}
		if (src.getRegionAfter()!=null) {
			RegionAfter rAfter = getFactory().createRegionAfter();
			rAfter.setRegionName(src.getRegionAfter().getRegionName());
			rAfter.setExtent(src.getRegionAfter().getExtent());
			mirror.setRegionAfter(rAfter);
		}
		return mirror;
	}

	/** @since 17.0.6 */
	private static boolean oddEvenParityFix() {
		return org.docx4j.Docx4jProperties.getProperty(
				"docx4j.convert.out.fo.pgNumType.oddEvenParityFix", true);
	}

	private static PageSequenceMaster createPageSequenceMaster(HeaderFooterPolicy hf,
			String sectionName, boolean foliosInverted ) {

		boolean noHeadersFootersAfterFirstPage = true;
		
		PageSequenceMaster psm = getFactory().createPageSequenceMaster();
		psm.setMasterName(sectionName);
		
		RepeatablePageMasterAlternatives rpma = getFactory().createRepeatablePageMasterAlternatives();
		
		psm.getSinglePageMasterReferenceOrRepeatablePageMasterReferenceOrRepeatablePageMasterAlternatives().add(rpma);
		
		// has first header or footer?
		if (hf.getFirstHeader()!=null || hf.getFirstFooter()!=null) {			
			ConditionalPageMasterReference cpmr1 = getFactory().createConditionalPageMasterReference();
			cpmr1.setMasterReference(sectionName+"-firstpage");
			cpmr1.setPagePosition(PagePositionType.FIRST);
			rpma.getConditionalPageMasterReference().add(cpmr1);
		}

		if (hf.getEvenHeader()!=null || hf.getEvenFooter()!=null) {

			ConditionalPageMasterReference cpmr2 = getFactory().createConditionalPageMasterReference();
			cpmr2.setMasterReference(sectionName+"-evenpage");
			//cpmr2.setPagePosition(PagePositionType.FIRST);
			cpmr2.setOddOrEven(foliosInverted ? OddOrEvenType.ODD : OddOrEvenType.EVEN);
			rpma.getConditionalPageMasterReference().add(cpmr2);

			// the xslt outputs a "-default" page as the odd-page
			ConditionalPageMasterReference cpmr3 = getFactory().createConditionalPageMasterReference();
			cpmr3.setMasterReference(sectionName+"-default");
			//cpmr3.setPagePosition(PagePositionType.FIRST);
			cpmr3.setOddOrEven(foliosInverted ? OddOrEvenType.EVEN : OddOrEvenType.ODD);
			rpma.getConditionalPageMasterReference().add(cpmr3);
			
			noHeadersFootersAfterFirstPage = false;
		} else if (hf.getDefaultHeader()!=null || hf.getDefaultFooter()!=null) {
			
			ConditionalPageMasterReference cpmr4 = getFactory().createConditionalPageMasterReference();
			cpmr4.setMasterReference(sectionName+"-default");
			//cpmr4.setPagePosition(PagePositionType.FIRST);
			rpma.getConditionalPageMasterReference().add(cpmr4);			
			
			noHeadersFootersAfterFirstPage = false;
		}
		
		if (noHeadersFootersAfterFirstPage) {
			ConditionalPageMasterReference cpmr5 = getFactory().createConditionalPageMasterReference();
			cpmr5.setMasterReference(sectionName+"-simple");
			//cpmr5.setPagePosition(PagePositionType.FIRST);
			rpma.getConditionalPageMasterReference().add(cpmr5);						
		}
		
		return psm;
	}
	
	
	/** @since 17.0.6 */
	private static boolean settingIsOn(AbstractWmlConversionContext context, String name) {
		try {
			org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart dsp =
					context.getWmlPackage().getMainDocumentPart().getDocumentSettingsPart();
			if (dsp==null || dsp.getJaxbElement()==null) return false;
			org.docx4j.wml.CTSettings settings = dsp.getJaxbElement();
			org.docx4j.wml.BooleanDefaultTrue b = "gutterAtTop".equals(name)
					? settings.getGutterAtTop() : settings.getMirrorMargins();
			return b!=null && b.isVal();
		} catch (Exception e) {
			log.debug("Couldn't read w:" + name + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * @param gutterAtTop w:settings/w:gutterAtTop
	 * @param mirrorMargins w:settings/w:mirrorMargins - together these decide which edge
	 *        w:pgMar/@w:gutter is added to (@since 17.0.6)
	 */
	private static SimplePageMaster createSimplePageMaster( 
			String masterName, PageDimensions page, String appendRegionName, 
			boolean needBefore, boolean needAfter,
			boolean gutterAtTop, boolean mirrorMargins) {
		return createSimplePageMaster(masterName, page, appendRegionName, needBefore, needAfter,
				gutterAtTop, mirrorMargins, masterName!=null && masterName.endsWith("-evenpage"));
	}

	/**
	 * @param evenPage whether this master lays out Word's even (left-hand) pages, which
	 *        decides which edge the gutter goes on and, with mirrored margins, which of
	 *        w:pgMar's left and right is the inside one (@since 17.0.6)
	 */
	private static SimplePageMaster createSimplePageMaster( 
			String masterName, PageDimensions page, String appendRegionName, 
			boolean needBefore, boolean needAfter,
			boolean gutterAtTop, boolean mirrorMargins, boolean evenPage) {
		
		// This method uses dummy large extents
		// A later step fixes them.
		
		SimplePageMaster spm = getFactory().createSimplePageMaster();
		spm.setMasterName(masterName);
		
		// dimensions.  
		//   <w:pgSz w:w="12240" w:h="15840"/>
        //   <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="708" w:footer="708" w:gutter="0"/>

		spm.setPageHeight( UnitsOfMeasurement.twipToBest(page.getPgSz().getH().intValue() ));
		spm.setPageWidth(  UnitsOfMeasurement.twipToBest(page.getPgSz().getW().intValue() ));
		
		/* w:pgMar/@w:gutter is extra binding margin, added to the left margin (or to the
		 * top with w:gutterAtTop; with mirrored margins it goes to the right on even
		 * pages).  PageDimensions.getWritableWidthTwips() already subtracts it, so the
		 * text column was the right width and started in the wrong place: measured on a
		 * document with w:left="851" w:gutter="567", Word puts every portrait line at
		 * x=70.9 (851 + 567 twips) where ours was at 42.5, -28.35pt on 222 pages.
		 * @since 17.0.6 */
		int gutterTwips = page.getGutter();
		boolean gutterOnRight = !gutterAtTop && mirrorMargins && evenPage;

		/* w:settings/w:mirrorMargins: Word calls w:pgMar/@w:left the inside margin and
		 * @w:right the outside one, so on an even (left-hand) page they swap - and the
		 * binding edge, which is what @w:gutter widens, swaps with them.  Measured
		 * against Word 365 on a document whose three sectPr all say w:left="2268"
		 * w:right="1418": Word's even pages start at x=70.8 (the 1418-twip margin) and
		 * ours at 113.4 - 42.6pt out, on half the document's 42 pages.  @since 17.0.6 */
		boolean mirrored = mirrorMargins && evenPage;
		int insideTwips = page.getPgMar().getLeft().intValue();
		int outsideTwips = page.getPgMar().getRight().intValue();

		int marginLeftTwips = (mirrored ? outsideTwips : insideTwips)
				+ ((gutterAtTop || gutterOnRight) ? 0 : gutterTwips);
		int marginRightTwips = (mirrored ? insideTwips : outsideTwips)
				+ (gutterOnRight ? gutterTwips : 0);

		/* A section whose w:cols declares a single w:col narrower than the margin box
		 * uses that width for its text: measured on a document whose margin box is
		 * 451.45pt and whose w:cols says <w:col w:w="8640"/> (432pt), Word centres on
		 * 288 where ours was 297.7 (+9.7pt on every centred line) and puts the right
		 * edge at 504 where ours was 523.45 (+19.45pt).  §7's unequal-columns table is
		 * only built for several w:col children, so a single narrow one fell through.
		 * @since 17.0.6 */
		int narrowing = page.getSingleColumnNarrowing();
		if (narrowing > 0) marginRightTwips += narrowing;

		spm.setMarginLeft( UnitsOfMeasurement.twipToBest(marginLeftTwips) );
		spm.setMarginRight( UnitsOfMeasurement.twipToBest(marginRightTwips) );
		
		/* 
		 * Region before & after live in region body margins:
		 * 
		 * Per http://www.w3.org/TR/xsl/#fo_region-body
		 * 
		 * The body region should be sized and positioned within the fo:simple-page-master 
		 * so that there is room for the areas returned by the flow that is assigned to the 
		 * fo:region-body and for any desired side regions, that is, fo:region-before, 
		 * fo:region-after, fo:region-start and fo:region-end's that are to be placed on the same page. 
		 * 
		 * These side regions are positioned within the content-rectangle of the page-reference-area. 
		 * The margins on the fo:region-body are used to position the region-viewport-area for the 
		 * fo:region-body and to leave space for the other regions that surround the fo:region-body.
		 *                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		 *                    
		 * The spacing between the last four regions and the fo:region-body is determined by subtracting 
		 * the relevant extent trait on the side regions from the trait that corresponds to the "margin-x" 
		 * property on the fo:region-body.
		 */
		RegionBody rb = getFactory().createRegionBody();		
		rb.setMarginLeft("0mm");
		rb.setMarginRight("0mm");

		rb.setColumnCount(String.valueOf(page.getColsNum())); //Number of Equal Width Columns
		rb.setColumnGap(UnitsOfMeasurement.twipToBest(page.getColsSpacing())); //Spacing Between Equal Width Columns

		/* w:sectPr/w:vAlign: Word aligns the section's content vertically in the text
		 * area (Page Setup > Layout > Vertical alignment).  display-align on
		 * fo:region-body is XSL 1.1's equivalent and FOP applies it, so a title page
		 * whose section says "center" is centred rather than sitting at the top:
		 * measured, every line of one such page was 112.5pt above Word's.  "both"
		 * (justified) has no FO equivalent - the closest is "center".  @since 17.0.6 */
		String vAlign = page.getVerticalAlign();
		if (vAlign!=null) {
			if ("center".equals(vAlign) || "both".equals(vAlign)) {
				rb.setDisplayAlign(org.plutext.jaxb.xslfo.DisplayAlignType.CENTER);
			} else if ("bottom".equals(vAlign)) {
				rb.setDisplayAlign(org.plutext.jaxb.xslfo.DisplayAlignType.AFTER);
			}
		}

		float halfPageHeight = page.getPgSz().getH().intValue()/40; // convert from twips, then * 0.5
		String halfPageHeightPts = halfPageHeight + "pt";  
		
		spm.setRegionBody(rb);
		
		if (needBefore) {
			//Header
			RegionBefore rBefore = getFactory().createRegionBefore();
			rBefore.setRegionName("xsl-region-before-"+appendRegionName);
			spm.setRegionBefore(rBefore);
			
			// Margin top on SPM is space between the page edge and the start of the header			
			int marginTopTwips 
				=  page.getHeaderMargin() + (gutterAtTop ? gutterTwips : 0);
			spm.setMarginTop( UnitsOfMeasurement.twipToBest(marginTopTwips ) );
			
			// Size header manually
			rBefore.setExtent( halfPageHeightPts); // A4 portrait is 297mm high
			
			
			// Leave room for this region in body margin
			rb.setMarginTop(halfPageHeightPts);
			
			
		} else {
			// No header.  A negative w:pgMar/@w:top means the body starts |top| from the
			// page edge (§7, FOPAreaTreeHelper); a negative FO margin would put it off
			// the page.  @since 17.0.6
			spm.setMarginTop( UnitsOfMeasurement.twipToBest(
					Math.abs(page.getPgMar().getTop().intValue()) + (gutterAtTop ? gutterTwips : 0) ) );
		}

		if (needAfter) {
			// Footer
			RegionAfter rAfter = getFactory().createRegionAfter();
			rAfter.setRegionName("xsl-region-after-"+appendRegionName);
			spm.setRegionAfter(rAfter);
			
			int marginBottomTwips= page.getFooterMargin();
			spm.setMarginBottom( UnitsOfMeasurement.twipToBest(marginBottomTwips) );
			
			// Size footer manually
			rAfter.setExtent( halfPageHeightPts); // A4 portrait is 297mm high
					
			// Leave room for this region in body margin
			rb.setMarginBottom(halfPageHeightPts );
			
		} else {
			// No footer; a negative w:pgMar/@w:bottom as above
			spm.setMarginBottom( UnitsOfMeasurement.twipToBest(
					Math.abs(page.getPgMar().getBottom().intValue())) );
		}
		
		return spm;
	}


	private static ObjectFactory getFactory() {
		if (factory == null) factory = new ObjectFactory();
		return factory;
	}

}
