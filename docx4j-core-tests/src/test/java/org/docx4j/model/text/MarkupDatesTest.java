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
package org.docx4j.model.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.docx4j.markdown.MarkdownExporter;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

/**
 * WD 0.46 REQ-702 and REQ-706 under {@code markup_detail: author_date}: a revision's and a comment's
 * date is the author's local time from {@code w:date}, its {@code Z} not written, with the UTC offset
 * where {@code dateUtc} gives it; {@code dateUtc} with {@code Z} where it is alone; the author alone
 * where there is neither.
 */
public class MarkupDatesTest {

	private static final TextOptions AUTHOR_DATE = TextOptions.of(TextView.MARKUP).markupDetail(TextOptions.MarkupDetail.AUTHOR_DATE);

	/** Word's four measured cases (CR-068 phase 0), in plain text, in Markdown and in the block model. */
	@Test
	public void measuredCases() throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.load(
				ViewAgreementTest.resources().resolve("revisions/cr068/manual-redline-dates.docx").toFile());
		TextResult r = Texts.of(pkg, AUTHOR_DATE);
		String text = PlainText.of(r);
		String[] expected = { "{--manual redline, 2026-10-31T00:00:00: deleted D1--}", "{--manual redline, 2026-10-31T00:00:00Z: deleted D2--}",
				"{--manual redline, 2026-10-31T00:00:00+00:00: deleted D3--}", "{--manual redline, 2026-10-31T11:00:00+11:00: deleted D4--}" };
		for (String e : expected) assertTrue(text, text.contains(e));
		String md = new MarkdownExporter().export(r, pkg);
		for (String e : expected) assertTrue(md, md.contains(e));
		List<String> dates = new ArrayList<String>();
		for (TextBlock b : r.blocks()) for (TextRun run : b.getRuns()) if (run.getRevision() != null) dates.add(run.getRevision().getDate());
		assertEquals(Arrays.asList("2026-10-31T00:00:00", "2026-10-31T00:00:00Z", "2026-10-31T00:00:00+00:00", "2026-10-31T11:00:00+11:00"), dates);
	}

	/** A mark with no date writes its author alone; a comment's dateUtc is read through the comments ids and extensible parts. */
	@Test
	public void noDateAndComments() throws Exception {
		WordprocessingMLPackage pkg = MiniDocx.body("<w:p><w:r><w:t>Kept </w:t></w:r><w:del w:id=\"5\" w:author=\"A\"><w:r><w:delText>gone</w:delText></w:r></w:del></w:p>"
				+ "<w:p><w:commentRangeStart w:id=\"0\"/><w:r><w:t>One</w:t></w:r><w:commentRangeEnd w:id=\"0\"/><w:r><w:commentReference w:id=\"0\"/></w:r></w:p>"
				+ "<w:p><w:commentRangeStart w:id=\"1\"/><w:r><w:t>Two</w:t></w:r><w:commentRangeEnd w:id=\"1\"/><w:r><w:commentReference w:id=\"1\"/></w:r></w:p>")
				.part("comments.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.comments+xml",
						"http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments", "rIdC",
						"<w:comments " + MiniDocx.NS + ">"
						+ "<w:comment w:id=\"0\" w:author=\"R\" w:date=\"2026-10-31T11:00:00Z\"><w:p w14:paraId=\"1A2B3C4D\"><w:r><w:t>with utc</w:t></w:r></w:p></w:comment>"
						+ "<w:comment w:id=\"1\" w:author=\"R\" w:date=\"2026-10-31T11:00:00Z\"><w:p w14:paraId=\"5E6F7A8B\"><w:r><w:t>without</w:t></w:r></w:p></w:comment>"
						+ "</w:comments>")
				.part("commentsIds.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.commentsIds+xml",
						"http://schemas.microsoft.com/office/2016/09/relationships/commentsIds", "rIdI",
						"<w16cid:commentsIds xmlns:w16cid=\"http://schemas.microsoft.com/office/word/2016/wordml/cid\">"
						+ "<w16cid:commentId w16cid:paraId=\"1a2b3c4d\" w16cid:durableId=\"0ABC1234\"/></w16cid:commentsIds>")
				.part("commentsExtensible.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.commentsExtensible+xml",
						"http://schemas.microsoft.com/office/2018/08/relationships/commentsExtensible", "rIdE",
						"<w16cex:commentsExtensible xmlns:w16cex=\"http://schemas.microsoft.com/office/word/2018/wordml/cex\">"
						+ "<w16cex:commentExtensible w16cex:durableId=\"0abc1234\" w16cex:dateUtc=\"2026-10-31T00:00:00Z\"/></w16cex:commentsExtensible>")
				.load();
		TextResult r = Texts.of(pkg, AUTHOR_DATE);
		String text = PlainText.of(r);
		assertTrue(text, text.contains("Kept {--A: gone--}"));
		assertTrue(text, text.contains("{>>R, 2026-10-31T11:00:00+11:00: with utc<<}"));
		assertTrue(text, text.contains("{>>R, 2026-10-31T11:00:00: without<<}"));
		assertEquals("2026-10-31T11:00:00+11:00", r.comments().get("0").getDate());
		assertEquals("2026-10-31T11:00:00", r.comments().get("1").getDate());
		String md = new MarkdownExporter().export(r, pkg);
		assertTrue(md, md.contains("{--A: gone--}"));
	}
}
