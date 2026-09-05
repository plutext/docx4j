/*
 *  Copyright 2010, Plutext Pty Ltd.
 *
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
package org.docx4j.convert.out.common.preprocess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Body;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.ProofErr;
import org.docx4j.wml.R;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.Text;

/**
 * A hard page break inside a paragraph.
 *
 * <p>A {@code w:br w:type="page"} which reaches the FO inside an {@code fo:inline} is
 * ignored by FOP, so the break has to be expressed on a block.  Until 17.0.6 this class
 * simply removed the break and set {@code w:pageBreakBefore} on the paragraph holding it,
 * which is right only where the break comes <em>first</em>: where anything precedes it,
 * the text before the break moved to the next page with the rest of the paragraph.</p>
 *
 * <p>Word breaks the page <em>at</em> the break: what precedes it stays on the page it is
 * on, and what follows opens the next.  So a paragraph whose break has content before it
 * is split in two - the second half carrying the break, the paragraph's properties, and
 * whatever followed the break.  Measured on a real document whose cover picture and page
 * break share a paragraph: Word puts the 270x225pt picture on page 1 (mutool
 * {@code transform="270 0 0 225 162.65 347.59"}), where docx4j put it on page 2 at
 * y=80.58, and every line of page 2 then sat 268.4pt low.  21 of 99 documents of a corpus
 * of long real documents hold such a paragraph.</p>
 *
 * <p>The two halves are one paragraph, so nothing is doubled between them: the first half
 * keeps the space-before and loses the space-after, the second half the other way about,
 * and the second takes neither the numbering label (Word numbers the paragraph once) nor
 * the first-line indent.  A {@code w:sectPr} belongs to the paragraph's end, so it goes
 * with the second half.</p>
 *
 * @author alberto
 */
public class PageBreak {

	private static Logger log = LoggerFactory.getLogger(PageBreak.class);

	/**
	 * If a page-break w:br w:type="page" is found within a run with some formatting applied to it
	 * then it will be generated into an fo:inline tag. This page break will be ignored by fop. This class
	 * moves the page-breaks to the enclosing block.
	 */
	public static void process(WordprocessingMLPackage wmlPackage) {
	Body body = wmlPackage.getMainDocumentPart().getJaxbElement().getBody();
		//TODO: Convert to visitor
		movePageBreaks(body);
	}

	private static void movePageBreaks(Body body) {

		List<Object> elts = body.getContent();
		for (int i=0; i<elts.size(); i++) {
			Object o = elts.get(i);
			if (o instanceof P) {
				updateParagraph((P)o, elts, i);
				// where the paragraph was split, the continuation is now at i+1 and is
				// visited by the loop in its turn, so a paragraph with several breaks
				// is split at each of them
			}
		}
	}

	/**
	 * @param siblings the list the paragraph is in, so that a continuation can be
	 *        inserted after it; null where there is nowhere to put one (the paragraph is
	 *        then treated as it was before 17.0.6)
	 * @param index the paragraph's position in that list
	 */
	static void updateParagraph(P paragraph, List<Object> siblings, int index) {

		List<Object> content = paragraph.getContent();
		int[] at = firstPageBreak(content);
		if (at==null) return;

		if (siblings!=null && contentPrecedes(content, at)) {
			split(paragraph, at, siblings, index);
			return;
		}

		// nothing before the break: it is the paragraph's own break-before, which is
		// what docx4j has always made of it.  A paragraph which already breaks before
		// itself keeps this one as a break of its own - two consecutive breaks are an
		// empty page in Word, which one break-before would not give.
		if (paragraph.getPPr()!=null
				&& paragraph.getPPr().getPageBreakBefore()!=null
				&& paragraph.getPPr().getPageBreakBefore().isVal()) {
			return;
		}
		removeBreak(content, at);
		if (paragraph.getPPr() == null) {
			paragraph.setPPr(new PPr());
		}
		paragraph.getPPr().setPageBreakBefore(new BooleanDefaultTrue());
	}

	/** Where the paragraph's first {@code w:br w:type="page"} is: {@code {i}} for one
	 *  which is a direct child, {@code {i,j}} for one in the i-th item's run content;
	 *  null where the paragraph has none. */
	private static int[] firstPageBreak(List<Object> content) {

		if (content==null) return null;
		for (int i=0; i<content.size(); i++) {
			Object ce = content.get(i);
			if (isPageBreak(ce)) return new int[] { i };
			if (ce instanceof R) {
				List<Object> rc = ((R)ce).getContent();
				for (int j=0; rc!=null && j<rc.size(); j++) {
					if (isPageBreak(rc.get(j))) return new int[] { i, j };
				}
			}
		}
		return null;
	}

	private static boolean isPageBreak(Object o) {
		return (o instanceof Br) && STBrType.PAGE.equals(((Br)o).getType());
	}

	/** Whether anything which draws precedes the break. */
	private static boolean contentPrecedes(List<Object> content, int[] at) {

		for (int i=0; i<at[0]; i++) {
			if (draws(content.get(i))) return true;
		}
		if (at.length>1) {
			List<Object> rc = ((R)content.get(at[0])).getContent();
			for (int j=0; j<at[1]; j++) {
				if (drawsInRun(rc.get(j))) return true;
			}
		}
		return false;
	}

	/** Paragraph-level children which mark a position rather than drawing anything. */
	private static final Set<String> INVISIBLE = new HashSet<String>(Arrays.asList(
			"bookmarkStart", "bookmarkEnd", "permStart", "permEnd",
			"commentRangeStart", "commentRangeEnd", "proofErr",
			"moveFromRangeStart", "moveFromRangeEnd", "moveToRangeStart", "moveToRangeEnd",
			"customXmlInsRangeStart", "customXmlInsRangeEnd",
			"customXmlDelRangeStart", "customXmlDelRangeEnd",
			"customXmlMoveFromRangeStart", "customXmlMoveFromRangeEnd",
			"customXmlMoveToRangeStart", "customXmlMoveToRangeEnd",
			"lastRenderedPageBreak", "annotationRef"));

	private static boolean draws(Object o) {

		if (o instanceof PPr || o instanceof ProofErr
				|| o instanceof CommentRangeStart || o instanceof CommentRangeEnd
				|| o instanceof CTMarkupRange) return false;
		if (o instanceof R) {
			List<Object> rc = ((R)o).getContent();
			for (int j=0; rc!=null && j<rc.size(); j++) {
				if (drawsInRun(rc.get(j))) return true;
			}
			return false;
		}
		if (o instanceof JAXBElement) {
			JAXBElement<?> je = (JAXBElement<?>)o;
			if (je.getName()!=null && INVISIBLE.contains(je.getName().getLocalPart())) return false;
		}
		return true; // hyperlink, sdt, smartTag, ins, del, ...
	}

	private static boolean drawsInRun(Object o) {

		if (o instanceof Br) return false; // a break draws nothing of its own
		if (o instanceof JAXBElement) {
			JAXBElement<?> je = (JAXBElement<?>)o;
			String local = je.getName()==null ? "" : je.getName().getLocalPart();
			if (INVISIBLE.contains(local)) return false;
			if (je.getValue() instanceof Text) {
				String v = ((Text)je.getValue()).getValue();
				return v!=null && v.length()>0;
			}
		}
		if (o instanceof Text) {
			String v = ((Text)o).getValue();
			return v!=null && v.length()>0;
		}
		return true;
	}

	/** Remove the break itself, and the run it was alone in. */
	private static void removeBreak(List<Object> content, int[] at) {

		if (at.length==1) {
			content.remove(at[0]);
			return;
		}
		R r = (R)content.get(at[0]);
		r.getContent().remove(at[1]);
		if (r.getContent().isEmpty()) content.remove(at[0]);
	}

	/**
	 * Split the paragraph at the break: this paragraph keeps what precedes it, and a new
	 * one carrying w:pageBreakBefore takes what follows.
	 */
	private static void split(P paragraph, int[] at, List<Object> siblings, int index) {

		List<Object> content = paragraph.getContent();
		List<Object> tail = new ArrayList<Object>();
		int from;

		if (at.length>1) {
			// the run holding the break: its remainder opens the continuation
			R r = (R)content.get(at[0]);
			List<Object> rc = r.getContent();
			List<Object> rest = new ArrayList<Object>(rc.subList(at[1]+1, rc.size()));
			while (rc.size()>at[1]) rc.remove(rc.size()-1); // the break and everything after it
			if (rc.isEmpty()) {
				content.remove(at[0]);
				from = at[0];
			} else {
				from = at[0]+1;
			}
			if (!rest.isEmpty()) {
				R continuation = new R();
				if (r.getRPr()!=null) continuation.setRPr(XmlUtils.deepCopy(r.getRPr()));
				continuation.getContent().addAll(rest);
				tail.add(continuation);
			}
		} else {
			content.remove(at[0]);
			from = at[0];
		}

		tail.addAll(new ArrayList<Object>(content.subList(from, content.size())));
		while (content.size()>from) content.remove(content.size()-1);

		if (paragraph.getPPr()==null) paragraph.setPPr(new PPr());
		P second = new P();
		second.setPPr(continuationProperties(paragraph.getPPr()));
		second.getContent().addAll(tail);
		siblings.add(index+1, second);

		if (log.isDebugEnabled()) {
			log.debug("Paragraph split at a w:br w:type=\"page\"");
		}
	}

	/**
	 * The properties of the half which opens the new page: the paragraph's own, with
	 * w:pageBreakBefore added, no space-before (the paragraph's space-before was applied
	 * where it started), no numbering label and no first-line indent - one paragraph gets
	 * one of each - and the section break, which belongs to the paragraph's end.  The
	 * first half loses its space-after for the same reason.
	 */
	private static PPr continuationProperties(PPr first) {

		PPr second = XmlUtils.deepCopy(first);
		second.setPageBreakBefore(new BooleanDefaultTrue());
		second.setNumPr(null);
		first.setSectPr(null);

		PPrBase.Spacing spacing = second.getSpacing();
		if (spacing==null) {
			spacing = new PPrBase.Spacing();
			second.setSpacing(spacing);
		}
		spacing.setBefore(java.math.BigInteger.ZERO);
		spacing.setBeforeAutospacing(Boolean.FALSE);

		PPrBase.Spacing firstSpacing = first.getSpacing();
		if (firstSpacing==null) {
			firstSpacing = new PPrBase.Spacing();
			first.setSpacing(firstSpacing);
		}
		firstSpacing.setAfter(java.math.BigInteger.ZERO);
		firstSpacing.setAfterAutospacing(Boolean.FALSE);

		PPrBase.Ind ind = second.getInd();
		if (ind!=null) {
			ind.setFirstLine(null);
			ind.setFirstLineChars(null);
			if (ind.getHanging()!=null) {
				ind.setHanging(null);
				ind.setHangingChars(null);
			}
		}
		return second;
	}

}
