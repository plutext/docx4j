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
 * Registers Word-style line breaking with every FopFactory docx4j builds.
 * Found by docx4j-export-fo through ServiceLoader (META-INF/services) when
 * this jar is on the classpath; nothing else is needed.  Turn it off with
 * docx4j property docx4j.convert.out.fo.wordLineBreaking=false.
 */
public class WordLayoutCustomizer implements FopFactoryCustomizer {

	private static final Logger log = LoggerFactory.getLogger(WordLayoutCustomizer.class);

	public static final String PROPERTY = "docx4j.convert.out.fo.wordLineBreaking";

	/** How far the spaces of a justified line may be compressed to fit one more word,
	 *  as a fraction of their natural width; docx4j property or system property
	 *  docx4j.convert.out.fo.wordLineBreaking.maxSpaceShrink (default 0.24: measured against
	 *  Word 365, the value at which the justified probe breaks 98% of its lines as Word does;
	 *  0.20 gives 78%, 0.30 gives 74%). */
	public static final String MAX_SPACE_SHRINK = "docx4j.convert.out.fo.wordLineBreaking.maxSpaceShrink";

	public static final double DEFAULT_MAX_SPACE_SHRINK = 0.24;

	public static double maxSpaceShrink() {
		String v = System.getProperty(MAX_SPACE_SHRINK);
		if (v == null) v = Docx4jProperties.getProperty(MAX_SPACE_SHRINK);
		if (v == null) return DEFAULT_MAX_SPACE_SHRINK;
		try {
			return Double.parseDouble(v.trim());
		} catch (NumberFormatException e) {
			return DEFAULT_MAX_SPACE_SHRINK;
		}
	}

	@Override
	public void customize(FopFactoryBuilder builder, FOSettings settings) {
		if (!Docx4jProperties.getProperty(PROPERTY, true)) {
			log.debug("Word line breaking disabled by " + PROPERTY);
			return;
		}
		builder.setLayoutManagerMakerOverride(new WordLayoutManagerMaker());
		log.debug("Word line breaking enabled");
	}
}
