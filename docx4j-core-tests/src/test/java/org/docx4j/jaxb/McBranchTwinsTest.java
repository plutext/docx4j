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
package org.docx4j.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.McSelection.BranchStep;
import org.docx4j.mce.AlternateContent;
import org.docx4j.model.bookmarks.BookmarksIntegrity;
import org.docx4j.model.bookmarks.BookmarksIntegrity.BookmarksStatus;
import org.docx4j.model.datastorage.OpenDoPEReverter;
import org.docx4j.model.fields.seq.FieldUpdaterSEQ;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.Text;
import org.junit.Test;
import org.jvnet.jaxb.lang.Child;

/**
 * Defects of today at every level, which CR-051's phase 0 audit found (CR-051 section 10.5): code
 * that walks every branch of an mc:AlternateContent and met a text box's twins - its copy in a
 * wps Choice and in a VML Fallback - as two of everything.
 */
public class McBranchTwinsTest {

	private static final String NS = " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\""
			+ " xmlns:v=\"urn:schemas-microsoft-com:vml\""
			+ " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\""
			+ " xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\""
			+ " xmlns:w99=\"urn:example:not-understood\"";

	/** A paragraph holding a text box, its content once in a wps Choice and once in a VML Fallback. */
	private static P textBoxParagraph(String choiceRequires, String inChoice, String inFallback) throws Exception {
		return (P) XmlUtils.unmarshalString("<w:p" + NS + "><w:r><mc:AlternateContent>"
				+ "<mc:Choice Requires=\"" + choiceRequires + "\">" + box(inChoice) + "</mc:Choice>"
				+ "<mc:Fallback>" + box(inFallback) + "</mc:Fallback>"
				+ "</mc:AlternateContent></w:r></w:p>");
	}

	private static String box(String content) {
		return "<w:pict><v:shape style=\"width:50pt;height:20pt\"><v:textbox><w:txbxContent>" + content
				+ "</w:txbxContent></v:textbox></v:shape></w:pict>";
	}

	private static P paragraph(String content) throws Exception {
		return (P) XmlUtils.unmarshalString("<w:p" + NS + ">" + content + "</w:p>");
	}

	private static AlternateContent alternateContent(P p) {
		R r = (R) p.getContent().get(0);
		return (AlternateContent) XmlUtils.unwrap(r.getContent().get(0));
	}

	// --- McSelection.branchPaths and twins ---------------------------------------------------------

	@Test
	public void branchPathsAndTwins() throws Exception {
		P p = textBoxParagraph("wps", "<w:p><w:r><w:t>c</w:t></w:r></w:p>", "<w:p><w:r><w:t>f</w:t></w:r></w:p>");
		Map<Object, List<BranchStep>> paths = McSelection.branchPaths(p, Text.class);
		assertEquals(2, paths.size());
		List<Text> texts = new ArrayList<Text>();
		for (Object t : paths.keySet()) texts.add((Text) t);
		Text c = "c".equals(texts.get(0).getValue()) ? texts.get(0) : texts.get(1);
		Text f = c == texts.get(0) ? texts.get(1) : texts.get(0);
		AlternateContent ac = alternateContent(p);
		assertSame(ac, paths.get(c).get(0).getElement());
		assertSame(ac.getChoice().get(0), paths.get(c).get(0).getBranch());
		assertEquals(0, paths.get(c).get(0).getIndex());
		assertSame(ac.getFallback(), paths.get(f).get(0).getBranch());
		assertEquals(1, paths.get(f).get(0).getIndex());
		assertTrue(McSelection.twins(paths.get(c), paths.get(f)));
		assertFalse("the same branch", McSelection.twins(paths.get(c), paths.get(c)));
		assertFalse("outside any element", McSelection.twins(paths.get(c), null));
	}

	// --- TraversalUtil.replaceChildren -------------------------------------------------------------

	@Test
	public void replaceChildrenEditsABranch() throws Exception {
		P p = paragraph("<mc:AlternateContent><mc:Choice Requires=\"w14\"><w:r><w:t>a</w:t></w:r></mc:Choice>"
				+ "<mc:Fallback><w:r><w:t>b</w:t></w:r></mc:Fallback></mc:AlternateContent>");
		AlternateContent ac = (AlternateContent) XmlUtils.unwrap(p.getContent().get(0));

		List<Object> choiceContent = new ArrayList<Object>();
		R x = Context.getWmlObjectFactory().createR();
		choiceContent.add(x);
		TraversalUtil.replaceChildren(ac.getChoice().get(0), choiceContent);
		assertEquals(1, ac.getChoice().get(0).getAny().size());
		assertSame(x, ac.getChoice().get(0).getAny().get(0));
		assertSame(ac.getChoice().get(0), x.getParent());

		List<Object> fallbackContent = new ArrayList<Object>();
		R y = Context.getWmlObjectFactory().createR();
		R z = Context.getWmlObjectFactory().createR();
		fallbackContent.add(y);
		fallbackContent.add(z);
		TraversalUtil.replaceChildren(ac.getFallback(), fallbackContent);
		assertEquals(2, ac.getFallback().getAny().size());
		assertSame(z, ac.getFallback().getAny().get(1));
		assertSame(ac.getFallback(), z.getParent());
	}

	// --- FieldUpdaterSEQ ----------------------------------------------------------------------------

	private static String simpleSeq(String label) {
		return "<w:fldSimple w:instr=\" SEQ Figure \\* ARABIC \"><w:r><w:t>" + label + "</w:t></w:r></w:fldSimple>";
	}

	private static String complexSeq(String label) {
		return "<w:r><w:fldChar w:fldCharType=\"begin\"/></w:r><w:r><w:instrText xml:space=\"preserve\"> SEQ Figure \\* ARABIC </w:instrText></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"separate\"/></w:r><w:r><w:t>" + label + "</w:t></w:r>"
				+ "<w:r><w:fldChar w:fldCharType=\"end\"/></w:r>";
	}

	/** Each run's text (not its field instructions), in the order of a walk of every branch. */
	private static List<String> runTexts(List<Object> content) {
		final List<String> out = new ArrayList<String>();
		new TraversalUtil(content, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof R) {
					StringBuilder sb = new StringBuilder();
					for (Object item : ((R) o).getContent()) {
						// a field result FieldUpdaterSEQ writes is a bare Text
						if (item instanceof Text) {
							sb.append(((Text) item).getValue());
						} else if (item instanceof JAXBElement && "t".equals(((JAXBElement<?>) item).getName().getLocalPart())) {
							sb.append(((Text) XmlUtils.unwrap(item)).getValue());
						}
					}
					if (sb.length() > 0) out.add(sb.toString());
				}
				return null;
			}
		}, McMode.ALL);
		return out;
	}

	private static List<String> seq(String choiceRequires, String inChoice, String inFallback, boolean complex) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		List<Object> body = pkg.getMainDocumentPart().getContent();
		String before = complex ? complexSeq("x") : simpleSeq("x");
		String after = complex ? complexSeq("y") : simpleSeq("y");
		body.add(paragraph(before));
		body.add(textBoxParagraph(choiceRequires,
				inChoice == null ? "<w:p/>" : "<w:p>" + (complex ? complexSeq(inChoice) : simpleSeq(inChoice)) + "</w:p>",
				inFallback == null ? "<w:p/>" : "<w:p>" + (complex ? complexSeq(inFallback) : simpleSeq(inFallback)) + "</w:p>"));
		body.add(paragraph(after));
		new FieldUpdaterSEQ(pkg).update();
		return runTexts(body);
	}

	@Test
	public void seqCountsATextBoxOnce() throws Exception {
		// body, Choice, Fallback, body
		assertEquals("[1, 2, 2, 3]", seq("wps", "c", "f", false).toString());
		assertEquals("[1, 2, 2, 3]", seq("wps", "c", "f", true).toString());
	}

	@Test
	public void seqCountsTheBranchWordReads() throws Exception {
		// a Choice nothing understands: Word reads the Fallback
		assertEquals("[1, 2, 2, 3]", seq("w99", "c", "f", false).toString());
		// a caption in the Fallback only, while Word reads the Choice: numbered, but not counted
		assertEquals("[1, 2, 2]", seq("wps", null, "f", false).toString());
	}

	// --- BookmarksIntegrity -------------------------------------------------------------------------

	private static String bookmarked(String name, int id, String text) {
		return "<w:p><w:bookmarkStart w:id=\"" + id + "\" w:name=\"" + name + "\"/><w:r><w:t>" + text
				+ "</w:t></w:r><w:bookmarkEnd w:id=\"" + id + "\"/></w:p>";
	}

	@Test
	public void bookmarkTwinsAreNotDuplicates() throws Exception {
		List<Object> body = new ArrayList<Object>();
		body.add(textBoxParagraph("wps", bookmarked("inBox", 5, "c"), bookmarked("inBox", 5, "f")));
		assertEquals(BookmarksStatus.OK, new BookmarksIntegrity().check(body, false));

		// two text boxes with the one bookmark are still duplicates
		body.add(textBoxParagraph("wps", bookmarked("inBox", 5, "c"), bookmarked("inBox", 5, "f")));
		assertEquals(BookmarksStatus.BROKEN, new BookmarksIntegrity().check(body, false));
	}

	@Test
	public void bookmarkDuplicatesInTheBodyStillFound() throws Exception {
		List<Object> body = new ArrayList<Object>();
		body.add(XmlUtils.unmarshalString(bookmarked("same", 1, "a").replace("<w:p>", "<w:p" + NS + ">")));
		body.add(XmlUtils.unmarshalString(bookmarked("same", 2, "b").replace("<w:p>", "<w:p" + NS + ">")));
		assertEquals(BookmarksStatus.BROKEN, new BookmarksIntegrity().check(body, false));
	}

	// --- OpenDoPEReverter ---------------------------------------------------------------------------

	private static String condition(String tag, String text) {
		return "<w:sdt><w:sdtPr><w:id w:val=\"7\"/><w:tag w:val=\"" + tag + "\"/></w:sdtPr><w:sdtContent>"
				+ "<w:p><w:r><w:t>" + text + "</w:t></w:r></w:p></w:sdtContent></w:sdt>";
	}

	private static WordprocessingMLPackage conditionInTextBox(String tag, String inChoice, String inFallback) throws Exception {
		WordprocessingMLPackage pkg = WordprocessingMLPackage.createPackage();
		pkg.getMainDocumentPart().getContent().add(
				textBoxParagraph("wps", condition(tag, inChoice), condition(tag, inFallback)));
		XPathsPart xpaths = new XPathsPart(new PartName("/customXml/item1.xml"));
		xpaths.setJaxbElement(new org.opendope.xpaths.Xpaths());
		pkg.getMainDocumentPart().addTargetPart(xpaths);
		return pkg;
	}

	private static List<SdtBlock> sdts(WordprocessingMLPackage pkg) {
		final List<SdtBlock> out = new ArrayList<SdtBlock>();
		new TraversalUtil(pkg.getMainDocumentPart().getContent(), new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof SdtBlock) out.add((SdtBlock) o);
				return null;
			}
		}, McMode.ALL);
		return out;
	}

	@Test
	public void reverterRestoresEachTwinFromItsOwnBranch() throws Exception {
		WordprocessingMLPackage template = conditionInTextBox("od:condition=c1", "TemplateChoice", "TemplateFallback");
		WordprocessingMLPackage instance = conditionInTextBox("od:resultConditionFalse=c1", "", "");

		new OpenDoPEReverter(template, instance).revert();

		List<SdtBlock> restored = sdts(instance);
		assertEquals(2, restored.size());
		assertNotSame("each branch its own control", restored.get(0), restored.get(1));
		assertEquals("[TemplateChoice, TemplateFallback]", runTexts(instance.getMainDocumentPart().getContent()).toString());
		for (SdtBlock sdt : restored) {
			assertTrue(((Child) sdt).getParent() instanceof org.docx4j.wml.CTTxbxContent);
		}
	}
}
