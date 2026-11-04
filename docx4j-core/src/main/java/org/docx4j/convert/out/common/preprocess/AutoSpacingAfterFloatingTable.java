/*
 *  Copyright 2026, Plutext Pty Ltd.
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

import java.math.BigInteger;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.model.PropertyResolver;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.Tbl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Word applies no {@code w:beforeAutospacing} to the paragraph straight after a floating
 * table, so neither do the converters: that paragraph's space before becomes 0.  Measured
 * on probe spacing-autospacing-after-table (Word's goldens, 5 November 2026, the same at
 * modes 12 and 15): the space goes after one floating table or two, at any tblpY, whether
 * or not the paragraph opens with a {@code w:br}, and whatever {@code w:before} stands
 * beside the flag.  It stays after an inline table, after a framed paragraph, where the
 * paragraph itself anchors a picture, and where another paragraph comes between; an
 * explicit {@code w:before} stays; the paragraph above keeps its own auto space after.
 * Corpus document 6293 ran 13.9pt low from that paragraph on.
 *
 * <p>Only the main document's body is treated, through block-level content controls: a
 * cell, a header or a footer is not measured, nor is a document that sets
 * {@code doNotUseHTMLParagraphAutoSpacing}, where the writers use the explicit values.
 * Runs before {@link Containerization}, which would hide the paragraph in a container.</p>
 *
 * @since 17.3.6
 */
public class AutoSpacingAfterFloatingTable {

	private static Logger log = LoggerFactory.getLogger(AutoSpacingAfterFloatingTable.class);

	public static void process(WordprocessingMLPackage wmlPackage) {
		if (DocumentSettingsPart.isDoNotUseHTMLParagraphAutoSpacing(wmlPackage)) return;
		PropertyResolver resolver = null;
		try {
			resolver = wmlPackage.getMainDocumentPart().getPropertyResolver();
		} catch (Exception e) {
			log.warn("No property resolver; direct spacing only: " + e.getMessage());
		}
		process(wmlPackage.getMainDocumentPart().getJaxbElement().getBody().getContent(), resolver);
	}

	private static void process(List<Object> content, PropertyResolver resolver) {
		boolean afterFloatingTable = false;
		for (Object o : content) {
			Object block = XmlUtils.unwrap(o);
			if (block instanceof SdtBlock) {
				SdtBlock sdt = (SdtBlock) block;
				if (sdt.getSdtContent() != null) process(sdt.getSdtContent().getContent(), resolver);
				afterFloatingTable = false;
			} else if (block instanceof Tbl) {
				Tbl table = (Tbl) block;
				afterFloatingTable = table.getTblPr() != null && table.getTblPr().getTblpPr() != null;
			} else if (block instanceof P) {
				if (afterFloatingTable && autoSpaceBefore((P) block, resolver)) dropSpaceBefore((P) block);
				afterFloatingTable = false;
			} // a bookmark between them is not a block
		}
	}

	/** Whether the paragraph's effective spacing, its style's under its own, is auto before. */
	private static boolean autoSpaceBefore(P paragraph, PropertyResolver resolver) {
		PPr pPr = paragraph.getPPr();
		if (resolver != null) {
			try {
				pPr = resolver.getEffectivePPr(paragraph.getPPr());
			} catch (Exception e) {
				log.warn("Effective spacing: " + e.getMessage());
			}
		}
		return pPr != null && pPr.getSpacing() != null && Boolean.TRUE.equals(pPr.getSpacing().isBeforeAutospacing());
	}

	private static void dropSpaceBefore(P paragraph) {
		PPr pPr = paragraph.getPPr();
		if (pPr == null) {
			pPr = new PPr();
			paragraph.setPPr(pPr);
		}
		PPrBase.Spacing spacing = pPr.getSpacing();
		if (spacing == null) {
			spacing = new PPrBase.Spacing();
			pPr.setSpacing(spacing);
		}
		spacing.setBeforeAutospacing(Boolean.FALSE);
		spacing.setBefore(BigInteger.ZERO);
	}
}
