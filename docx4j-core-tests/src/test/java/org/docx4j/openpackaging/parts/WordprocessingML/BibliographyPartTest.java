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
package org.docx4j.openpackaging.parts.WordprocessingML;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.docx4j.XmlUtils;
import org.docx4j.bibliography.CTSources;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.ResourceUtils;
import org.junit.Test;

/**
 * CR-027 phase 1: Word writes Version="6" on b:Sources, an attribute
 * ECMA-376's bibliography schema does not declare, so the typed part dropped
 * it on a round trip (loadAndSave.docx: one in, none out). Declared from
 * measurement; this pins the round trip.
 */
public class BibliographyPartTest {

	@Test
	public void versionSurvivesARoundTrip() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(ResourceUtils.getResource("loadAndSave.docx"));
		BibliographyPart part = pkg.getMainDocumentPart().getBibliographyPart();
		assertNotNull("loadAndSave.docx has a bibliography part", part);
		CTSources sources = (CTSources) XmlUtils.unwrap(part.getJaxbElement());
		assertEquals("6", sources.getVersion());
		assertEquals("APA", sources.getStyleName());

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pkg.save(out);
		WordprocessingMLPackage again = WordprocessingMLPackage.load(new ByteArrayInputStream(out.toByteArray()));
		CTSources reread = (CTSources) XmlUtils.unwrap(again.getMainDocumentPart().getBibliographyPart().getJaxbElement());
		assertEquals("6", reread.getVersion());
	}
}
