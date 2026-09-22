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
package org.docx4j.anon;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A legacy VML drawing part ({@code xl/drawings/vmlDrawing1.vml}: the shapes of
 * a workbook's comments, form controls and OLE previews) scrubbed as DOM. Its
 * root is {@code <xml>}, which docx4j's VML binding does not know, so
 * {@link Anonymize} reads the part's bytes and swaps it for a DOM part before
 * this runs. Scrubbed: a shape's {@code alt} and {@code title}, its links
 * ({@code href}, {@code o:href}, {@code o:althref} cleared), a text path's
 * {@code string}, the HTML text of a {@code v:textbox} ({@code div},
 * {@code font}, {@code span}...), and the {@code x:Fmla*} formulas of a control
 * (through the workbook's formula scrubber). The rest of {@code x:ClientData}
 * (anchors, alignment words, object types) is the schema's, and stays.
 *
 * @since 17.2.1
 */
public class VmlDomScrubber {

	private final ScrambleText scrambler;

	public VmlDomScrubber(ScrambleText scrambler) {
		this.scrambler = scrambler;
	}

	public void scrub(Node n) {
		if (n.getNodeType() == Node.ELEMENT_NODE) {
			Element el = (Element) n;
			NamedNodeMap attrs = el.getAttributes();
			for (int i = 0; attrs != null && i < attrs.getLength(); i++) {
				Attr a = (Attr) attrs.item(i);
				String ln = local(a);
				if (ln.equals("alt") || ln.equals("title") || ln.equals("string")) {
					a.setValue(scrambler.scramble(a.getValue()));
				} else if (ln.equals("href") || ln.equals("althref") || ln.equals("src")) {
					a.setValue("");
				}
			}
		}
		String eln = local(n);
		boolean html = eln.equals("div") || eln.equals("font") || eln.equals("span") || eln.equals("p")
				|| eln.equals("b") || eln.equals("i") || eln.equals("u") || eln.equals("strong") || eln.equals("em");
		NodeList children = n.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node c = children.item(i);
			if (c.getNodeType() == Node.TEXT_NODE || c.getNodeType() == Node.CDATA_SECTION_NODE) {
				if (html) {
					c.setNodeValue(scrambler.scramble(c.getNodeValue()));
				} else if (eln.startsWith("Fmla")) {
					c.setNodeValue(scrambler.formula(c.getNodeValue()));
				}
			} else {
				scrub(c);
			}
		}
	}

	private static String local(Node n) {
		return n.getLocalName() == null ? n.getNodeName() : n.getLocalName();
	}

}
