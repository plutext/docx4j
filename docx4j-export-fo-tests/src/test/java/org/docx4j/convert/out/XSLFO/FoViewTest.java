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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.model.text.TextView;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Document;
import org.junit.Test;

/**
 * The FO exporter's revision view (CR-046 phase 3, D11): {@code markup} by default, deletions
 * drawn struck through; {@code accepted} and {@code original}, the documents Word's Accept All
 * and Reject All leave, laid out from the exporter's working copy - the document is not changed.
 * Both pathways.
 */
public class FoViewTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final int[] FLAGS = { Docx4J.FLAG_EXPORT_PREFER_XSL, Docx4J.FLAG_EXPORT_PREFER_NONXSL };

	private static WordprocessingMLPackage tracked() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().setJaxbElement((Document) XmlUtils.unmarshalString("<w:document " + W + "><w:body>"
				+ "<w:p><w:r><w:t xml:space=\"preserve\">keep </w:t></w:r>"
				+ "<w:ins w:id=\"1\" w:author=\"Ann\" w:date=\"2026-01-02T03:04:05Z\"><w:r><w:t>INSERTED</w:t></w:r></w:ins>"
				+ "<w:del w:id=\"2\" w:author=\"Bob\" w:date=\"2026-01-03T00:00:00Z\"><w:r><w:delText xml:space=\"preserve\"> DELETED</w:delText></w:r></w:del>"
				+ "</w:p></w:body></w:document>"));
		return pkg;
	}

	private static String fo(WordprocessingMLPackage pkg, TextView view, int flags) throws Exception {
		FOSettings foSettings = Docx4J.createFOSettings();
		foSettings.setOpcPackage(pkg);
		foSettings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
		if (view != null) foSettings.setView(view);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Docx4J.toFO(foSettings, baos, flags);
		return new String(baos.toByteArray(), StandardCharsets.UTF_8);
	}

	@Test
	public void views() throws Exception {
		for (int flags : FLAGS) {
			WordprocessingMLPackage pkg = tracked();
			String before = pkg.getMainDocumentPart().getXML();

			String markup = fo(pkg, null, flags);
			assertTrue(markup, markup.contains("INSERTED") && markup.contains("DELETED"));
			assertTrue(markup, markup.contains("line-through"));

			String accepted = fo(pkg, TextView.ACCEPTED, flags);
			assertTrue(accepted, accepted.contains("INSERTED"));
			assertFalse(accepted, accepted.contains("DELETED"));

			String original = fo(pkg, TextView.ORIGINAL, flags);
			assertTrue(original, original.contains("DELETED"));
			assertFalse(original, original.contains("INSERTED"));
			assertFalse(original, original.contains("line-through"));

			assertEquals(before, pkg.getMainDocumentPart().getXML());
		}
		assertEquals(TextView.MARKUP, Docx4J.createFOSettings().getView());
	}
}
