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

	/** The value the caller set, or null: an explicit setting applies to every document,
	 *  where the default is capped by the document's own docx4j:space-shrink. */
	public static Double configuredMaxSpaceShrink() {
		String v = System.getProperty(MAX_SPACE_SHRINK);
		if (v == null) v = Docx4jProperties.getProperty(MAX_SPACE_SHRINK);
		if (v == null) return null;
		try {
			return Double.valueOf(v.trim());
		} catch (NumberFormatException e) {
			return null;
		}
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
