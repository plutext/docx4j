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
package org.docx4j.convert.out.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.ConversionFeatures;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.jaxb.Context;
import org.docx4j.model.text.TextView;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * The HTML exporter's revision view (CR-046 phase 3, D11): {@code markup} by default, insertions
 * {@code <ins>} and deletions {@code <del>} with their author and date (WD REQ-705); the
 * {@code accepted} and {@code original} views are the resolved documents rendered, on the
 * exporter's working copy - the document is not changed.  Both pathways.
 */
public class HtmlViewTest {

	private static final String W = "xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"";

	private static final int[] FLAGS = { Docx4J.FLAG_EXPORT_PREFER_XSL, Docx4J.FLAG_EXPORT_PREFER_NONXSL };

	private static WordprocessingMLPackage tracked() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getContent().add((P) XmlUtils.unmarshalString("<w:p " + W + ">"
				+ "<w:r><w:t xml:space=\"preserve\">keep </w:t></w:r>"
				+ "<w:ins w:id=\"1\" w:author=\"Ann\" w:date=\"2026-01-02T03:04:05Z\"><w:r><w:t>added</w:t></w:r></w:ins>"
				+ "<w:del w:id=\"2\" w:author=\"Bob\" w:date=\"2026-01-03T00:00:00Z\"><w:r><w:delText xml:space=\"preserve\"> removed</w:delText></w:r></w:del>"
				+ "</w:p>", Context.jc, P.class));
		return pkg;
	}

	private static String html(WordprocessingMLPackage pkg, TextView view, int flag) throws Exception {
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setOpcPackage(pkg);
		if (view != null) settings.setView(view);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Docx4J.toHTML(settings, out, flag);
		return out.toString("UTF-8");
	}

	private static String text(String html) {
		String body = html.substring(html.indexOf("<body"));
		return body.replaceAll("<[^>]*>", "");
	}

	@Test
	public void markupByDefault() throws Exception {
		for (int flag : FLAGS) {
			String html = html(tracked(), null, flag);
			assertTrue(html, html.matches("(?s).*<ins [^>]*data-author=\"Ann\"[^>]*>(<span[^>]*>)*added.*"));
			assertTrue(html, html.matches("(?s).*<ins [^>]*datetime=\"2026-01-02T03:04:05Z\".*"));
			assertTrue(html, html.matches("(?s).*<del [^>]*data-author=\"Bob\"[^>]*>(<span[^>]*>)* removed.*"));
			assertTrue(html, html.matches("(?s).*<del [^>]*class=\"del\".*"));
			assertFalse(html, html.contains("<span class=\"del\">"));
		}
		HTMLSettings settings = Docx4J.createHTMLSettings();
		assertEquals(TextView.MARKUP, settings.getView());
	}

	@Test
	public void acceptedAndOriginal() throws Exception {
		for (int flag : FLAGS) {
			WordprocessingMLPackage pkg = tracked();
			String before = pkg.getMainDocumentPart().getXML();
			String accepted = html(pkg, TextView.ACCEPTED, flag);
			assertTrue(accepted, text(accepted).contains("keep added"));
			assertFalse(accepted, text(accepted).contains("removed"));
			assertFalse(accepted, accepted.contains("<ins") || accepted.contains("<del"));
			String original = html(pkg, TextView.ORIGINAL, flag);
			assertTrue(original, text(original).contains("keep  removed"));
			assertFalse(original, text(original).contains("added"));
			assertFalse(original, original.contains("<ins") || original.contains("<del"));
			// the views were the working copy's
			assertEquals(before, pkg.getMainDocumentPart().getXML());
		}
	}

	@Test
	public void viewIsItsFeature() throws Exception {
		HTMLSettings settings = Docx4J.createHTMLSettings();
		settings.setView(TextView.ORIGINAL);
		assertTrue(settings.getFeatures().contains(ConversionFeatures.PP_COMMON_REJECT_TRACKED_CHANGES));
		settings.setView(TextView.ACCEPTED);
		assertTrue(settings.getFeatures().contains(ConversionFeatures.PP_COMMON_ACCEPT_TRACKED_CHANGES));
		assertFalse(settings.getFeatures().contains(ConversionFeatures.PP_COMMON_REJECT_TRACKED_CHANGES));
		assertEquals(TextView.ACCEPTED, settings.getView());
		settings.setView(TextView.MARKUP);
		assertEquals(TextView.MARKUP, settings.getView());
	}
}
