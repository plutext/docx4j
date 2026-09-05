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
package org.docx4j.fop.wordlayout;

import org.apache.fop.apps.FopFactoryBuilder;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.fo.renderers.FopFactoryCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers Word-style layout (line breaking and line placement) with every
 * FopFactory docx4j builds.  Part of docx4j-export-fo, found through
 * ServiceLoader (META-INF/services), and on by default; nothing has to be
 * added to the classpath.  Turn it off with the docx4j property
 * docx4j.convert.out.fo.wordLayout=false, which restores plain FOP layout.
 */
public class WordLayoutCustomizer implements FopFactoryCustomizer {

	private static final Logger log = LoggerFactory.getLogger(WordLayoutCustomizer.class);

	public static final String PROPERTY = "docx4j.convert.out.fo.wordLayout";

	/** How far the spaces of a justified line may be compressed to fit one more word,
	 *  as a fraction of their natural width; docx4j property or system property
	 *  docx4j.convert.out.fo.wordLayout.maxSpaceShrink (default 0.24: measured against
	 *  Word 365, the value at which the justified probe breaks 98% of its lines as Word does;
	 *  0.20 gives 78%, 0.30 gives 74%).
	 *
	 *  This is the limit for a document Word lays out with its 2013 engine
	 *  (w:compatSetting compatibilityMode 15).  Older documents get 0: docx4j writes
	 *  docx4j:space-shrink="0" on fo:root for them and the line manager caps this
	 *  value with it. */
	public static final String MAX_SPACE_SHRINK = "docx4j.convert.out.fo.wordLayout.maxSpaceShrink";

	public static final double DEFAULT_MAX_SPACE_SHRINK = 0.24;

	public static double maxSpaceShrink() {
		Double v = configuredMaxSpaceShrink();
		return v == null ? DEFAULT_MAX_SPACE_SHRINK : v.doubleValue();
	}

	/** How far the spaces of a justified line may be compressed to take a longer
	 *  <em>hyphenation fragment</em> - the part of a hyphenated word which stays on the
	 *  line.  Measured against Word 365 (the hyphenation probes' goldens): Word accepted
	 *  a fragment costing 1.2% and 6.0% of the line's spaces and rejected 13.5%, 14.5%,
	 *  22.0% and 25.6%, where it will compress by up to 20.5% to pull a whole word on
	 *  (see {@link #MAX_SPACE_SHRINK}).  docx4j property or system property
	 *  docx4j.convert.out.fo.wordLayout.maxHyphenSpaceShrink; default 0.10.
	 *  @since 17.0.6 */
	public static final String MAX_HYPHEN_SPACE_SHRINK
			= "docx4j.convert.out.fo.wordLayout.maxHyphenSpaceShrink";

	public static final double DEFAULT_MAX_HYPHEN_SPACE_SHRINK = 0.10;

	public static double maxHyphenSpaceShrink() {
		Double v = doubleProperty(MAX_HYPHEN_SPACE_SHRINK);
		return v == null ? DEFAULT_MAX_HYPHEN_SPACE_SHRINK : v.doubleValue();
	}

	/** Whether <code>w:hyphenationZone</code> is enforced as the largest gap Word will
	 *  leave at a line end before hyphenating.  Measured on the two hyphenation probes,
	 *  whose zones are 18pt and 36pt: Word's line breaks are the same in both, and it
	 *  hyphenated lines whose gap without the hyphen was 16.71pt to 34.09pt - inside the
	 *  36pt zone - so the zone never fired.  Off by default; set the docx4j property or
	 *  system property docx4j.convert.out.fo.wordLayout.hyphenationZone to true to
	 *  restore the behaviour docx4j had in 17.0.5.
	 *  @since 17.0.6 */
	public static final String ENFORCE_HYPHENATION_ZONE
			= "docx4j.convert.out.fo.wordLayout.hyphenationZone";

	public static boolean enforceHyphenationZone() {
		String v = System.getProperty(ENFORCE_HYPHENATION_ZONE);
		if (v == null) {
			return Docx4jProperties.getProperty(ENFORCE_HYPHENATION_ZONE, false);
		}
		return Boolean.parseBoolean(v.trim());
	}

	private static Double doubleProperty(String name) {
		String v = System.getProperty(name);
		if (v == null) v = Docx4jProperties.getProperty(name);
		if (v == null) return null;
		try {
			return Double.valueOf(v.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/** The value the caller set, or null: an explicit setting applies to every document,
	 *  where the default is capped by the document's own docx4j:space-shrink. */
	public static Double configuredMaxSpaceShrink() {
		return doubleProperty(MAX_SPACE_SHRINK);
	}

	/** The namespace of the line-box attributes WordLineLayoutManager reads, when
	 *  Word layout is on (otherwise docx4j leaves them out). */
	@Override
	public String extensionNamespace() {
		return Docx4jProperties.getProperty(PROPERTY, true) ? WordLayoutElementMapping.URI : null;
	}

	@Override
	public void customize(FopFactoryBuilder builder, FOSettings settings) {
		if (!Docx4jProperties.getProperty(PROPERTY, true)) {
			log.debug("Word layout disabled by " + PROPERTY);
			return;
		}
		builder.setLayoutManagerMakerOverride(new WordLayoutManagerMaker());
		log.debug("Word layout enabled");
	}
}
