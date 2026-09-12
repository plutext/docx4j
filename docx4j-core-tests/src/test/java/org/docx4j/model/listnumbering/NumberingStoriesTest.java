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
package org.docx4j.model.listnumbering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.CTFtnEdn;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.P;
import org.junit.Test;

/**
 * Each story numbers from its own counters, as Word does (CR-014 probe P7,
 * golden 2026-09-12): the body 1 2 3 and then 4 5 6 after the notes; a
 * section's header 1 2 3 and its footer 4 5 6 (one counter for the two); the
 * footnotes part one counter across its notes (1 2 3, 4 5 6); the endnotes
 * part 1 2 3; each text box 1 2 3; the comment 1 2 3 (read in Word's window).
 * Every numbered paragraph of {@code numbering-stories.docx} uses w:num 70.
 */
public class NumberingStoriesTest {

	static final String DOC = "src/test/java/org/docx4j/model/listnumbering/numbering-stories.docx";

	static WordprocessingMLPackage load() throws Exception {
		return WordprocessingMLPackage.load(new File(DOC));
	}

	/** The numbered paragraphs directly in the content (not inside a text box). */
	static List<P> numbered(List<Object> content) {
		List<P> out = new ArrayList<P>();
		for (Object o : content) {
			o = XmlUtils.unwrap(o);
			if (o instanceof P && ((P) o).getPPr() != null && ((P) o).getPPr().getNumPr() != null) out.add((P) o);
		}
		return out;
	}

	static String labels(WordprocessingMLPackage pkg, List<P> ps, NumberingState state) {
		StringBuilder sb = new StringBuilder();
		for (P p : ps) {
			if (sb.length() > 0) sb.append(' ');
			Emulator.NumberingResult t = Emulator.getNumber(pkg, p.getPPr(), state);
			sb.append(t == null ? "null" : t.getNumString());
		}
		return sb.toString();
	}

	static <T extends Part> T part(WordprocessingMLPackage pkg, Class<T> type) {
		for (Part p : pkg.getParts().getParts().values()) {
			if (type.isInstance(p)) return type.cast(p);
		}
		return null;
	}

	/** The first text box's content (the wps one, in the mc:Choice; the VML
	 *  fallback holds a copy the exporters never traverse). */
	static CTTxbxContent textBox(MainDocumentPart mdp) throws Exception {
		List<Object> nodes = mdp.getJAXBNodesViaXPath("//w:txbxContent", false);
		return nodes.isEmpty() ? null : (CTTxbxContent) XmlUtils.unwrap(nodes.get(0));
	}

	@Test
	public void eachStoryCountsOnItsOwn() throws Exception {
		WordprocessingMLPackage pkg = load();
		MainDocumentPart mdp = pkg.getMainDocumentPart();
		NumberingStates states = new NumberingStates();

		List<P> body = numbered(mdp.getContent());
		assertEquals(6, body.size());
		assertEquals("1. 2. 3.", labels(pkg, body.subList(0, 3), states.forPart(mdp)));

		// the other stories, converted between the body's two halves
		HeaderPart header = part(pkg, HeaderPart.class);
		FooterPart footer = part(pkg, FooterPart.class);
		assertSame(states.forPart(header), states.forPart(footer));
		assertEquals("1. 2. 3.", labels(pkg, numbered(header.getContent()), states.forPart(header)));
		assertEquals("4. 5. 6.", labels(pkg, numbered(footer.getContent()), states.forPart(footer)));

		FootnotesPart footnotes = part(pkg, FootnotesPart.class);
		StringBuilder fn = new StringBuilder();
		for (CTFtnEdn note : footnotes.getJaxbElement().getFootnote()) {
			if (note.getType() != null) continue; // separator, continuationSeparator
			if (fn.length() > 0) fn.append(" | ");
			fn.append(labels(pkg, numbered(note.getContent()), states.forPart(footnotes)));
		}
		assertEquals("1. 2. 3. | 4. 5. 6.", fn.toString());

		EndnotesPart endnotes = part(pkg, EndnotesPart.class);
		StringBuilder en = new StringBuilder();
		for (CTFtnEdn note : endnotes.getJaxbElement().getEndnote()) {
			if (note.getType() != null) continue;
			en.append(labels(pkg, numbered(note.getContent()), states.forPart(endnotes)));
		}
		assertEquals("1. 2. 3.", en.toString());

		CommentsPart comments = part(pkg, CommentsPart.class);
		StringBuilder cm = new StringBuilder();
		for (org.docx4j.wml.Comments.Comment c : comments.getJaxbElement().getComment()) {
			cm.append(labels(pkg, numbered(c.getContent()), states.forPart(comments)));
		}
		assertEquals("1. 2. 3.", cm.toString());

		CTTxbxContent txbx = textBox(mdp);
		assertEquals("1. 2. 3.", labels(pkg, numbered(txbx.getContent()), states.newStory()));

		// and the body carries on where it left off
		assertEquals("4. 5. 6.", labels(pkg, body.subList(3, 6), states.forPart(mdp)));

		// none of that touched the numbering part's own default state
		assertEquals("1. 2. 3. 4. 5. 6.", labels(pkg, body, null));
		assertNotSame(states.main(), mdp.getNumberingDefinitionsPart().getNumberingState());
	}

	@Test
	public void peekTakesNothing() throws Exception {
		WordprocessingMLPackage pkg = load();
		List<P> body = numbered(pkg.getMainDocumentPart().getContent());
		NumberingState state = new NumberingState();
		assertEquals("1.", Emulator.peek(pkg, body.get(0).getPPr(), state).getNumString());
		assertEquals("1.", Emulator.peek(pkg, body.get(0).getPPr(), state).getNumString());
		assertEquals("1.", Emulator.getNumber(pkg, body.get(0).getPPr(), state).getNumString());
		assertEquals("2.", Emulator.peek(pkg, body.get(1).getPPr(), state).getNumString());
		assertEquals("2.", Emulator.getNumber(pkg, body.get(1).getPPr(), state).getNumString());
		// against the part's default state too
		assertEquals("1.", Emulator.peek(pkg, body.get(0).getPPr(), null).getNumString());
		assertEquals("1.", Emulator.peek(pkg, body.get(0).getPPr(), null).getNumString());
		assertEquals("1.", Emulator.getNumber(pkg, body.get(0).getPPr()).getNumString());
		assertEquals("2.", Emulator.peek(pkg, body.get(0).getPPr(), null).getNumString());
	}

	@Test
	public void resetStartsAgain() throws Exception {
		WordprocessingMLPackage pkg = load();
		List<P> body = numbered(pkg.getMainDocumentPart().getContent());
		NumberingState state = new NumberingState();
		assertEquals("1. 2. 3. 4. 5. 6.", labels(pkg, body, state));
		state.reset();
		assertEquals("1. 2. 3. 4. 5. 6.", labels(pkg, body, state));
		// getEmulator(true) does the same for the part's default state
		assertEquals("1. 2. 3.", labels(pkg, body.subList(0, 3), null));
		pkg.getMainDocumentPart().getNumberingDefinitionsPart().getEmulator(true);
		assertEquals("1. 2. 3.", labels(pkg, body.subList(0, 3), null));
	}
}
