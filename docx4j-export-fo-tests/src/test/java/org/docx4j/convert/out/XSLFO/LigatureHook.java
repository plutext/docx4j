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
package org.docx4j.convert.out.XSLFO;

import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.fo.FopCapabilities;
import org.docx4j.fonts.RunFontSelector;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * How docx4j turns ligatures off for these tests' FO: as {@code fox:gsub-features="-liga"} on
 * the font's own declaration where the FO renderer has the hook (the docx4j FO renderer,
 * since 17.3.0), else by sending the run to the font's single-byte {@code +noliga} twin
 * (Apache FOP).  The same decision as FOConversionContext.gsubFeatures().
 *
 * @since 17.3.1
 */
final class LigatureHook {

	private LigatureHook() {}

	/** Whether docx4j writes fox:gsub-features rather than taking the +noliga twin. */
	static boolean on() {
		String p = Docx4jProperties.getProperty("docx4j.convert.out.fo.gsubFeatures");
		if ("true".equalsIgnoreCase(p)) return true;
		if ("false".equalsIgnoreCase(p)) return false;
		return FopCapabilities.has(FopCapabilities.Capability.GSUB_FEATURES);
	}

	/** The fox:gsub-features delta on the element or an ancestor (it is inherited), or null. */
	static String delta(Element el) {
		for (Node n = el; n instanceof Element; n = n.getParentNode()) {
			Element e = (Element) n;
			if (e.hasAttributeNS(RunFontSelector.FOX_NS, RunFontSelector.GSUB_FEATURES)) {
				return e.getAttributeNS(RunFontSelector.FOX_NS, RunFontSelector.GSUB_FEATURES);
			}
		}
		return null;
	}
}
