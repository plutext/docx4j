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
package org.docx4j.convert.out.fo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The two-pass page-number filter keeps character data where the document has it.
 *
 * <p>{@link PlaceholderReplacementHandler} buffers character data so that a placeholder
 * split over several {@code characters()} calls can still be found, and flushed the
 * buffer only at {@code endElement} - so text which came <b>before</b> a child element was
 * handed on after that child had started, that is, inside it.  A table-of-contents entry
 * written as {@code <inline>text <leader/> <page-number-citation/></inline>} - which every
 * entry of a resolved TOC is - lost both of its spaces that way: each was flushed as the
 * content of the element that followed it, where FO draws nothing for it.  A document
 * without page-number citations never went through this filter at all, which is why the
 * same FO rendered correctly there.</p>
 *
 * @since 17.2.0
 */
public class PlaceholderCharacterOrderTest {

	/** A handler which records the SAX events it is given, as a readable string. */
	private static final class Recorder extends DefaultHandler {
		private final StringBuilder events = new StringBuilder();

		@Override
		public void startElement(String uri, String local, String qName, Attributes atts) {
			events.append('<').append(qName).append('>');
		}

		@Override
		public void endElement(String uri, String local, String qName) {
			events.append("</").append(qName).append('>');
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			events.append('[').append(new String(ch, start, length)).append(']');
		}
	}

	/** A lookup which replaces "MMM" with the page number, as the real one does. */
	private static final class Lookup implements PlaceholderReplacementHandler.PlaceholderLookup {
		@Override
		public boolean hasPlaceholders(StringBuilder buffer) {
			return buffer.indexOf("MMM") >= 0;
		}

		@Override
		public void replaceValues(StringBuilder buffer) {
			int at;
			while ((at = buffer.indexOf("MMM")) >= 0) buffer.replace(at, at + 3, "7");
		}
	}

	@Test
	public void textBeforeAChildElementStaysBeforeIt() throws Exception {
		Recorder out = new Recorder();
		PlaceholderReplacementHandler h = new PlaceholderReplacementHandler(out, new Lookup());
		AttributesImpl none = new AttributesImpl();

		// <inline>entry <leader/> <page-number/></inline>
		h.startElement("", "inline", "inline", none);
		text(h, "entry ");
		h.startElement("", "leader", "leader", none);
		h.endElement("", "leader", "leader");
		text(h, " ");
		h.startElement("", "page-number", "page-number", none);
		h.endElement("", "page-number", "page-number");
		h.endElement("", "inline", "inline");

		assertEquals("<inline>[entry ]<leader></leader>[ ]<page-number></page-number></inline>",
				out.events.toString());
	}

	/** and a placeholder split over two characters() calls is still found */
	@Test
	public void aSplitPlaceholderIsStillResolved() throws Exception {
		Recorder out = new Recorder();
		PlaceholderReplacementHandler h = new PlaceholderReplacementHandler(out, new Lookup());
		AttributesImpl none = new AttributesImpl();

		h.startElement("", "inline", "inline", none);
		text(h, "page M");
		text(h, "MM.");
		h.endElement("", "inline", "inline");

		assertEquals("<inline>[page 7.]</inline>", out.events.toString());
	}

	private static void text(PlaceholderReplacementHandler h, String s) throws Exception {
		h.characters(s.toCharArray(), 0, s.length());
	}
}
