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
package org.docx4j.markdown;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Base64;

import org.docx4j.jaxb.Context;
import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextView;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.*;
import org.junit.Test;

/**
 * CR-056 (Jason, 2026-10-23: "should CR-056 be addressing any issues in markdown export?"): every Markdown sink
 * a document value reaches. Measured: body text, link text, alt text and a comment's text are escaped by the
 * renderer ({@code \<script\>}); a revision's or a comment's author (and date) in the CriticMarkup marker went
 * out raw ({@code {++<script>…</script>: inserted++}}), so a renderer that allows raw HTML had it as markup.
 * Fixed the same day: only the braces are raw, the detail is text.
 */
public class MarkdownHostileMarkupTest {
	private static final ObjectFactory F = Context.getWmlObjectFactory();
	static R run(String t) { R r = F.createR(); Text x = F.createText(); x.setValue(t); x.setSpace("preserve"); r.getContent().add(x); return r; }
	static P p(Object... content) { P p = F.createP(); for (Object o : content) p.getContent().add(o); return p; }

	static String export(TextOptions.MarkupDetail detail) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		mdp.getContent().add(p(run("T1 <script>alert('T1')</script> plain text")));
		mdp.getContent().add(p(run("T2 [x](javascript:alert('T2')) as text")));
		mdp.getContent().add(p(run("T3 <img src=x onerror=alert('T3')> as text")));
		// a benign link whose text tries to close the link syntax
		Relationship rel = new org.docx4j.relationships.ObjectFactory().createRelationship();
		rel.setType(Namespaces.HYPERLINK); rel.setTarget("https://example.org/ok"); rel.setTargetMode("External");
		mdp.getRelationshipsPart().addRelationship(rel);
		P.Hyperlink h = F.createPHyperlink(); h.setId(rel.getId()); h.getContent().add(run("T4 a](javascript:alert('T4')) [b"));
		mdp.getContent().add(p(h));
		// a picture with hostile alt text
		byte[] png = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");
		BinaryPartAbstractImage img = BinaryPartAbstractImage.createImagePart(pkg, mdp, png);
		org.docx4j.dml.wordprocessingDrawing.Inline inline = img.createImageInline("name", "alt", 1, 2, false);
		inline.getDocPr().setName("T5\"><img src=x onerror=alert('T5')>"); inline.getDocPr().setDescr("T5 ](x) <b>alt</b> <script>alert('T5')</script>");
		R ir = F.createR(); Drawing d = F.createDrawing(); d.getAnchorOrInline().add(inline); ir.getContent().add(d);
		mdp.getContent().add(p(ir));
		// a tracked insertion with a hostile author
		RunIns ins = F.createRunIns(); ins.setId(BigInteger.valueOf(7)); ins.setAuthor("T6 <script>alert('T6')</script>");
		ins.getCustomXmlOrSmartTagOrSdt().add(run("inserted")); mdp.getContent().add(p(ins));
		// a comment with a hostile author and text
		CommentsPart cp = new CommentsPart(); Comments comments = F.createComments(); cp.setJaxbElement(comments); mdp.addTargetPart(cp);
		Comments.Comment c = F.createCommentsComment(); c.setId(BigInteger.ONE); c.setAuthor("T7 <script>alert('T7')</script>");
		c.getContent().add(p(run("T8 <script>alert('T8')</script> comment text"))); comments.getComment().add(c);
		CommentRangeStart cs = F.createCommentRangeStart(); cs.setId(BigInteger.ONE); CommentRangeEnd ce = F.createCommentRangeEnd(); ce.setId(BigInteger.ONE);
		R cref = F.createR(); R.CommentReference cr = F.createRCommentReference(); cr.setId(BigInteger.ONE); cref.getContent().add(F.createRCommentReference(cr));
		mdp.getContent().add(p(cs, run("commented"), ce, cref));

		MarkdownExportOptions o = new MarkdownExportOptions();
		o.setTrackedChangesPolicy(MarkdownExportOptions.TrackedChangesPolicy.MARKUP);
		o.setTextOptions(new TextOptions().comments(true).markupDetail(detail));
		o.setView(TextView.MARKUP);
		return new MarkdownExporter(o).export(pkg);
	}

	/** Whether the markup occurs other than with its {@code <} backslash-escaped. */
	static boolean unescaped(String markup, String md) {
		for (int i = md.indexOf(markup); i >= 0; i = md.indexOf(markup, i + 1)) if (i == 0 || md.charAt(i - 1) != '\\') return true;
		return false;
	}

	@Test
	public void noRawMarkupFromAnyDocumentValue() throws Exception {
		for (TextOptions.MarkupDetail detail : TextOptions.MarkupDetail.values()) {
			String md = export(detail);
			assertFalse(detail + ": raw <script> in the Markdown\n" + md, unescaped("<script>", md));
			assertFalse(detail + ": raw <img in the Markdown\n" + md, unescaped("<img ", md));
			assertFalse(detail + ": a link destination from text\n" + md, unescaped("](javascript:", md));
			assertTrue(detail + ": the text kept, escaped\n" + md, md.contains("T1 \\<script\\>alert('T1')\\</script\\> plain text"));
			assertTrue(detail + ": the markers kept\n" + md, md.contains("{++") && md.contains("++}") && md.contains("{>>") && md.contains("<<}"));
			if (detail != TextOptions.MarkupDetail.TYPE) {
				assertTrue(detail + ": the insertion's author, escaped\n" + md, md.contains("{++T6 \\<script\\>alert('T6')\\</script\\>"));
				assertTrue(detail + ": the comment's author, escaped\n" + md, md.contains("{>>T7 \\<script\\>alert('T7')\\</script\\>"));
			}
		}
	}

	@Test
	public void benignMarkersUnchanged() throws Exception {
		String md = export(TextOptions.MarkupDetail.AUTHOR);
		assertTrue(md, md.contains(": inserted++}"));
		assertTrue(md, md.contains("comment text<<}"));
	}
}
