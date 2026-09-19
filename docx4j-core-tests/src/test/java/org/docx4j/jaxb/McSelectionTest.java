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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.Docx4jProperties;
import org.docx4j.TextUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.mce.AlternateContent;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * CR-021 phase 1: one selection rule ({@link McSelection}) and a traversal that
 * knows whether it reads one branch or every branch ({@link McMode}).
 *
 * <p>The run under test is the shape Word 2010+ writes for a text box: an
 * mc:AlternateContent whose wps Choice holds one paragraph and whose VML Fallback
 * holds two, so a count of paragraphs says which branches a walk saw.</p>
 */
public class McSelectionTest {

	private static final String NS = " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\""
			+ " xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\""
			+ " xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\""
			+ " xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\""
			+ " xmlns:v=\"urn:schemas-microsoft-com:vml\"";

	private static final String CHOICE = "<mc:Choice Requires=\"wps\"><w:drawing>"
			+ "<wp:inline><wp:extent cx=\"1270000\" cy=\"635000\"/><wp:docPr id=\"1\" name=\"Text Box 1\"/>"
			+ "<a:graphic><a:graphicData uri=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\">"
			+ "<wps:wsp><wps:cNvSpPr txBox=\"1\"/><wps:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"1270000\" cy=\"635000\"/></a:xfrm>"
			+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></wps:spPr>"
			+ "<wps:txbx><w:txbxContent><w:p><w:r><w:t>choice para</w:t></w:r></w:p></w:txbxContent></wps:txbx>"
			+ "<wps:bodyPr/></wps:wsp></a:graphicData></a:graphic></wp:inline></w:drawing></mc:Choice>";

	private static final String FALLBACK = "<mc:Fallback><w:pict>"
			+ "<v:shape id=\"Text Box 1\" type=\"#_x0000_t202\" style=\"width:100pt;height:50pt\">"
			+ "<v:textbox><w:txbxContent>"
			+ "<w:p><w:r><w:t>fallback para one</w:t></w:r></w:p>"
			+ "<w:p><w:r><w:t>fallback para two</w:t></w:r></w:p>"
			+ "</w:txbxContent></v:textbox></v:shape></w:pict></mc:Fallback>";

	private static final String RUN = "<w:r" + NS + "><mc:AlternateContent>" + CHOICE + FALLBACK
			+ "</mc:AlternateContent></w:r>";

	private static final String RUN_NO_FALLBACK = "<w:r" + NS + "><mc:AlternateContent>" + CHOICE
			+ "</mc:AlternateContent></w:r>";

	private String savedPreference;

	@Before
	public void savePreference() {
		savedPreference = Docx4jProperties.getProperty(McSelection.PROPERTY, "");
		Docx4jProperties.setProperty(McSelection.PROPERTY, "");
	}

	@After
	public void restorePreference() {
		Docx4jProperties.setProperty(McSelection.PROPERTY, savedPreference);
	}

	private static R run(String xml) throws Exception {
		Object o = XmlUtils.unmarshalString(xml, Context.jc, R.class);
		return (R) XmlUtils.unwrap(o);
	}

	private static AlternateContent alternateContent(R r) {
		for (Object o : r.getContent()) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof AlternateContent) return (AlternateContent) u;
		}
		throw new AssertionError("no mc:AlternateContent in the run");
	}

	private static List<String> paragraphs(Object parent, McMode mode) {
		final List<String> texts = new ArrayList<String>();
		TraversalUtil.CallbackImpl counter = new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof P) {
					StringWriter sw = new StringWriter();
					try {
						TextUtils.extractText(o, sw);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					texts.add(sw.toString());
				}
				return null;
			}
		};
		if (mode == null) {
			new TraversalUtil(parent, counter);
		} else {
			new TraversalUtil(parent, counter, mode);
		}
		return texts;
	}

	@Test
	public void prefersChoiceNeedsEveryPrefixListed() {
		assertFalse("empty property prefers nothing", McSelection.prefersChoice("wps"));
		Docx4jProperties.setProperty(McSelection.PROPERTY, "wps");
		assertTrue(McSelection.prefersChoice("wps"));
		assertFalse("wps w14 needs both", McSelection.prefersChoice("wps w14"));
		assertFalse(McSelection.prefersChoice(null));
		assertFalse(McSelection.prefersChoice(""));
		Docx4jProperties.setProperty(McSelection.PROPERTY, "wps w14 wpg");
		assertTrue(McSelection.prefersChoice("wps w14"));
		assertTrue(McSelection.prefersChoice("wpg"));
		assertFalse(McSelection.prefersChoice("v"));
	}

	@Test
	public void selectedBranchIsTheFallbackByDefaultAndTheChoiceWhenPreferred() throws Exception {
		AlternateContent ac = alternateContent(run(RUN));
		assertSame(ac.getFallback(), McSelection.selectedBranch(ac));
		assertEquals(1, McSelection.select(ac).size()); // the w:pict
		Docx4jProperties.setProperty(McSelection.PROPERTY, "wps");
		assertSame(ac.getChoice().get(0), McSelection.selectedBranch(ac));
	}

	@Test
	public void noFallbackTakesTheFirstChoice() throws Exception {
		AlternateContent ac = alternateContent(run(RUN_NO_FALLBACK));
		assertSame(ac.getChoice().get(0), McSelection.selectedBranch(ac));
		AlternateContent empty = new AlternateContent();
		assertNull(McSelection.selectedBranch(empty));
		assertTrue(McSelection.select(empty).isEmpty());
	}

	@Test
	public void branchesByMode() throws Exception {
		AlternateContent ac = alternateContent(run(RUN));
		assertEquals(1, McSelection.branches(ac, McMode.READ).size());
		assertEquals(2, McSelection.branches(ac, McMode.ALL).size());
		assertSame(ac.getChoice().get(0), McSelection.branches(ac, McMode.ALL).get(0));
		assertSame(ac.getFallback(), McSelection.branches(ac, McMode.ALL).get(1));
	}

	@Test
	public void traversalReadsOneBranchByDefault() throws Exception {
		R r = run(RUN);
		List<String> texts = paragraphs(r, null);
		assertEquals("the Fallback's two paragraphs, once: " + texts, 2, texts.size());
		assertEquals("fallback para one", texts.get(0));
		Docx4jProperties.setProperty(McSelection.PROPERTY, "wps");
		texts = paragraphs(r, null);
		assertEquals("the Choice's one paragraph: " + texts, 1, texts.size());
		assertEquals("choice para", texts.get(0));
	}

	@Test
	public void traversalAllModeSeesEveryBranch() throws Exception {
		R r = run(RUN);
		List<String> texts = paragraphs(r, McMode.ALL);
		assertEquals("Choice then Fallback: " + texts, 3, texts.size());
		assertEquals("choice para", texts.get(0));
		assertEquals("fallback para two", texts.get(2));
		assertEquals("explicit READ is the default", 2, paragraphs(r, McMode.READ).size());
	}

	@Test
	public void visitOverloadsCarryTheMode() throws Exception {
		R r = run(RUN);
		final int[] count = { 0 };
		TraversalUtilVisitor<P> visitor = new TraversalUtilVisitor<P>() {
			@Override
			public void apply(P element) {
				count[0]++;
			}
		};
		TraversalUtil.visit(r, visitor);
		assertEquals(2, count[0]);
		count[0] = 0;
		TraversalUtil.visit(r, visitor, McMode.ALL);
		assertEquals(3, count[0]);
		count[0] = 0;
		TraversalUtil.CallbackImpl cb = new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof P) count[0]++;
				return null;
			}
		};
		assertEquals(McMode.READ, cb.getMcMode());
		cb.setMcMode(McMode.ALL);
		TraversalUtil.visit(r, cb);
		assertEquals("setMcMode before a plain visit", 3, count[0]);
	}

	@Test
	public void extractTextWritesOneBranch() throws Exception {
		R r = run(RUN);
		StringWriter sw = new StringWriter();
		TextUtils.extractText(r, sw);
		assertEquals("fallback para onefallback para two", sw.toString());
		Docx4jProperties.setProperty(McSelection.PROPERTY, "wps");
		sw = new StringWriter();
		TextUtils.extractText(r, sw);
		assertEquals("choice para", sw.toString());
	}

	@Test
	public void extractTextAppliesTheLastResortToAnElementWithNoFallback() throws Exception {
		// Choices but no Fallback: McSelection takes the first Choice; the stream holds its
		// text back until the element ends and then writes it
		R r = run(RUN_NO_FALLBACK);
		StringWriter sw = new StringWriter();
		TextUtils.extractText(r, sw);
		assertEquals("choice para", sw.toString());
		// and with a Fallback present the held-back Choice is discarded, not written twice
		sw = new StringWriter();
		TextUtils.extractText(run(RUN), sw);
		assertEquals("fallback para onefallback para two", sw.toString());
	}
}
