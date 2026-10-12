/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.toc;

import static org.junit.Assert.assertEquals;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Style;
import org.junit.Test;

/**
 * The TOC's styles: a Hyperlink style the document has is used, not added again (until 17.3.3
 * every TOC update appended another, the styles part gaining a duplicate each time)
 */
public class TocStylesHyperlinkTest {

	@Test
	public void hyperlinkAddedOnce() throws Exception {
		MainDocumentPart mdp = WordprocessingMLPackage.createPackage().getMainDocumentPart();
		TocStyles.getTocStyles(mdp);
		assertEquals(1, hyperlinks(mdp));
		TocStyles.getTocStyles(mdp);
		TocStyles.getTocStyles(mdp);
		assertEquals(1, hyperlinks(mdp));
	}

	private static int hyperlinks(MainDocumentPart mdp) {
		int n = 0;
		for (Style s : mdp.getStyleDefinitionsPart().getJaxbElement().getStyle()) {
			if (s.getName() != null && TocStyles.HYPERLINK.equals(s.getName().getVal())) n++;
		}
		return n;
	}
}
