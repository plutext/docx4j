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
package org.docx4j.convert.out.common;

import org.docx4j.Docx4jProperties;
import org.docx4j.model.PropertyResolver;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hidden text: {@code w:vanish} on a run's effective run properties.
 *
 * <p>Word does not print hidden text (unless the user turns on the "print hidden
 * text" printing option, which lives in the application, not in the docx - hence
 * the docx4j property here), and it takes no space: a paragraph whose runs and
 * paragraph mark are all hidden produces no line at all.  Until 17.0.5 docx4j
 * rendered it, which moved everything after it down the page.</p>
 *
 * <p>Set {@code docx4j.convert.out.printHiddenText=true} to render it anyway.</p>
 *
 * @since 17.0.5
 */
public class HiddenText {

	private static final Logger log = LoggerFactory.getLogger(HiddenText.class);

	/** The docx4j property standing in for Word's "print hidden text" printing option. */
	public static final String PROPERTY_NAME = "docx4j.convert.out.printHiddenText";

	private HiddenText() {}

	/** Whether hidden text is to be rendered; false (as Word prints) unless the
	 *  {@value #PROPERTY_NAME} property says otherwise. */
	public static boolean isPrinted() {
		return Docx4jProperties.getProperty(PROPERTY_NAME, false);
	}

	/** Whether these <em>effective</em> run properties are hidden. */
	public static boolean isHidden(RPr effectiveRPr) {
		return effectiveRPr != null
				&& effectiveRPr.getVanish() != null
				&& effectiveRPr.getVanish().isVal();
	}

	/**
	 * Whether a run with these direct properties is hidden, and so is not to be
	 * rendered.  Resolves the effective run properties (the run style, the
	 * paragraph style and the document defaults can all carry w:vanish).
	 */
	public static boolean isHiddenRun(PropertyResolver propertyResolver, PPr pPrDirect, RPr rPrDirect) {
		if (propertyResolver == null || isPrinted()) return false;
		try {
			return isHidden(propertyResolver.getEffectiveRPr(rPrDirect, pPrDirect));
		} catch (Exception e) {
			log.warn("Couldn't resolve effective rPr for w:vanish: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Whether a paragraph whose content converted to nothing (every run hidden, or
	 * no runs at all) is itself to disappear: that is so when its paragraph mark is
	 * hidden too, since then Word leaves no line behind.  A paragraph with a visible
	 * mark still occupies a line, even with all of its text hidden.
	 *
	 * @param rPrParagraphMark the <em>effective</em> run properties of the paragraph mark
	 */
	public static boolean isHiddenParagraph(RPr rPrParagraphMark) {
		return !isPrinted() && isHidden(rPrParagraphMark);
	}
}
