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
package org.docx4j.model.properties.run;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.docx4j.model.color.ResolvedColor;
import org.docx4j.model.shapes.WordArtText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * What {@link TextFill}, {@link TextOutline} and {@link TextShadow} share: a resolved colour as CSS and FO write it, and
 * the report of an effect CSS can't carry (CR-083 phase 5, D23).
 *
 * @since 17.3.7
 */
final class TextEffectCss {

	private static final Logger log = LoggerFactory.getLogger(TextEffectCss.class);

	/** What has been reported at warn: each kind once a JVM, then at debug (a document can have thousands of runs) */
	private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();

	private TextEffectCss() {}

	/** A colour as CSS: "#RRGGBB" where opaque, else rgba() */
	static String css(ResolvedColor c) {
		if (c.getAlpha() >= ResolvedColor.OPAQUE) return "#" + c.toHex();
		return "rgba(" + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue() + ", "
				+ number(c.getAlpha() / (double) ResolvedColor.OPAQUE, 3) + ")";
	}

	/**
	 * A colour as XSL-FO: "#RRGGBB" where opaque, else "#RRGGBBAA", which FOP draws at that alpha. FOP writes no alpha for
	 * 00 (it draws such a colour opaque), so a colour with no opacity left is written 01, as near invisible as FOP draws.
	 */
	static String fo(ResolvedColor c) {
		String hex = "#" + c.toHex();
		if (c.getAlpha() >= ResolvedColor.OPAQUE) return hex;
		int a = (int) Math.round(255.0 * c.getAlpha() / ResolvedColor.OPAQUE);
		a = Math.max(1, Math.min(254, a));
		return hex + String.format(Locale.ROOT, "%02X", a);
	}

	/** A number with at most {@code places} decimals, trailing zeros dropped */
	static String number(double v, int places) {
		String s = String.format(Locale.ROOT, "%." + places + "f", v);
		if (s.indexOf('.') >= 0) {
			s = s.replaceAll("0+$", "");
			if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
		}
		return "-0".equals(s) ? "0" : s;
	}

	/** Reports a body run's effect that CSS doesn't carry as Word draws it ({@link WordArtText#BODY_EFFECT}) */
	static void report(String what) {
		if (REPORTED.add(what)) {
			log.warn(WordArtText.BODY_EFFECT + ": " + what + " (reported once)");
		} else if (log.isDebugEnabled()) {
			log.debug(WordArtText.BODY_EFFECT + ": " + what);
		}
	}
}
