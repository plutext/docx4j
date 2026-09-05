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
package org.docx4j.model;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.wml.PPr;

/**
 * A document's automatic hyphenation settings, as Word applies them
 * (w:settings, ECMA-376 17.15.1).
 *
 * <ul>
 * <li><b>w:autoHyphenation</b> (17.15.1.10) - hyphenation is off unless this is
 * present and true. Word additionally needs a hyphenation dictionary for the
 * text's language: measured on four corpus documents which set
 * w:autoHyphenation (de-AT, de-DE, sl-SI, pt-BR), Word 365 on an English
 * installation hyphenated not one word of them.</li>
 * <li><b>w:hyphenationZone</b> (17.15.1.44) - the largest gap Word tolerates at
 * the end of a line before it hyphenates the next word. When the next whole
 * word does not fit, Word hyphenates it only where the space left on the line
 * is greater than the zone; otherwise it breaks before the word. Word's UI
 * default is 0.25 inch (360 twips) in US measurements and 0.75 cm (425 twips)
 * in metric ones, and all four corpus documents carry 425 explicitly. ECMA-376
 * gives no default for the element, so 360 is used where it is absent.</li>
 * <li><b>w:consecutiveHyphenLimit</b> (17.15.1.22) - how many lines in a row may
 * end in a hyphen; 0, and the absent case, mean no limit.</li>
 * <li><b>w:doNotHyphenateCaps</b> (17.15.1.37) - words in all capitals are not
 * hyphenated.</li>
 * <li><b>w:suppressAutoHyphens</b> (17.3.1.34), on the paragraph rather than the
 * document: this paragraph is never hyphenated. It is part of w:pPr, so it is
 * inherited through the style hierarchy; {@link #hyphenates(PPr)} takes the
 * effective pPr.</li>
 * </ul>
 *
 * @since 17.0.6
 */
public class HyphenationSettings {

	/** Word's US default hyphenation zone, 0.25 inch in twips. */
	public static final int DEFAULT_ZONE_TWIPS = 360;

	private final boolean autoHyphenation;
	private final int zoneTwips;
	private final int consecutiveLimit;
	private final boolean doNotHyphenateCaps;

	public HyphenationSettings(boolean autoHyphenation, int zoneTwips,
			int consecutiveLimit, boolean doNotHyphenateCaps) {
		this.autoHyphenation = autoHyphenation;
		this.zoneTwips = zoneTwips;
		this.consecutiveLimit = consecutiveLimit;
		this.doNotHyphenateCaps = doNotHyphenateCaps;
	}

	/** The settings of this package; hyphenation off where it has no settings part. */
	public static HyphenationSettings of(WordprocessingMLPackage pkg) {
		return new HyphenationSettings(
				DocumentSettingsPart.isAutoHyphenation(pkg),
				DocumentSettingsPart.getHyphenationZone(pkg),
				DocumentSettingsPart.getConsecutiveHyphenLimit(pkg),
				DocumentSettingsPart.isDoNotHyphenateCaps(pkg));
	}

	/** w:autoHyphenation. */
	public boolean isAutoHyphenation() {
		return autoHyphenation;
	}

	/** w:hyphenationZone in twips (360 where the document gives none). */
	public int getZoneTwips() {
		return zoneTwips;
	}

	/** w:consecutiveHyphenLimit; 0 means no limit. */
	public int getConsecutiveLimit() {
		return consecutiveLimit;
	}

	/** w:doNotHyphenateCaps. */
	public boolean isDoNotHyphenateCaps() {
		return doNotHyphenateCaps;
	}

	/**
	 * Whether a paragraph with this effective pPr is hyphenated: the document
	 * asks for automatic hyphenation and the paragraph does not suppress it.
	 *
	 * @param effectivePPr the paragraph's style-resolved properties (may be null)
	 */
	public boolean hyphenates(PPr effectivePPr) {
		return autoHyphenation && !isSuppressed(effectivePPr);
	}

	/** w:pPr/w:suppressAutoHyphens on the (effective) paragraph properties. */
	public static boolean isSuppressed(PPr effectivePPr) {
		return effectivePPr != null
				&& effectivePPr.getSuppressAutoHyphens() != null
				&& effectivePPr.getSuppressAutoHyphens().isVal();
	}

	@Override
	public String toString() {
		return "autoHyphenation=" + autoHyphenation + " zone=" + zoneTwips
				+ " consecutiveLimit=" + consecutiveLimit
				+ " doNotHyphenateCaps=" + doNotHyphenateCaps;
	}
}
