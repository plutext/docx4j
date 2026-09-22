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
 * The 2018 comments docx4j does not bind, scrubbed as DOM in place:
 * <ul>
 * <li>PowerPoint's ([MS-PPTX] 2.3, namespace
 * {@code http://schemas.microsoft.com/office/powerpoint/2018/8/main}, prefix
 * p188): {@code /ppt/authors.xml} ({@code p188:authorLst}) and
 * {@code /ppt/comments/modernComment_*.xml} ({@code p188:cmLst}) - an author's
 * {@code name} and {@code initials} through {@link Names} (the same
 * {@code Author n} as everywhere else), its {@code userId} (a directory id or
 * email) replaced by that name and its {@code providerId} by {@code None}; every
 * {@code created}, {@code startDate} and {@code dueDate} fixed; the text of every
 * {@code a:t} scrambled. Comment ids, author ids, positions and the anchors
 * ({@code ac:txMkLst}) are structure and stay.</li>
 * <li>Excel's threaded comments ([MS-XLSX], namespace
 * {@code http://schemas.microsoft.com/office/spreadsheetml/2018/threadedcomments}):
 * {@code /xl/persons/person.xml} ({@code personList/person}: {@code displayName}
 * through {@link Names}, {@code userId} → the name, {@code providerId} → None)
 * and {@code /xl/threadedComments/threadedComment*.xml}
 * ({@code threadedComment}: {@code dT} fixed, the {@code text} scrambled; the
 * cell reference, ids, parent ids, mentions and done flags stay).</li>
 * </ul>
 * Anything else with text is left for {@link Verify} to find.
 *
 * @since 17.2.1
 */
public class ModernCommentsScrubber {

	private final Names names;
	private final ScrambleText scrambler;

	public ModernCommentsScrubber(Names names, ScrambleText scrambler) {
		this.names = names;
		this.scrambler = scrambler;
	}

	public void scrub(Node n) {
		if (n.getNodeType() == Node.ELEMENT_NODE) {
			Element el = (Element) n;
			String mappedAuthor = null;
			NamedNodeMap attrs = el.getAttributes();
			// the name first, so the user id can take the mapped name
			Attr name = attribute(attrs, local(el).equals("person") ? "displayName" : "name");
			if (name != null && (local(el).equals("author") || local(el).equals("person"))) {
				mappedAuthor = names.author(name.getValue());
				name.setValue(mappedAuthor);
			}
			for (int i = 0; attrs != null && i < attrs.getLength(); i++) {
				Attr a = (Attr) attrs.item(i);
				String ln = local(a);
				if (ln.equals("initials")) {
					a.setValue(names.initials(a.getValue()));
				} else if (ln.equals("userId")) {
					a.setValue(mappedAuthor != null ? mappedAuthor : "Author");
				} else if (ln.equals("providerId")) {
					a.setValue("None");
				} else if (ln.equals("created") || ln.equals("startDate") || ln.equals("dueDate") || ln.equals("dT")) {
					a.setValue(Names.FIXED_DATE.toXMLFormat());
				}
			}
		}
		NodeList children = n.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node c = children.item(i);
			if (c.getNodeType() == Node.TEXT_NODE || c.getNodeType() == Node.CDATA_SECTION_NODE) {
				if (local(n).equals("t") || local(n).equals("text")) {
					c.setNodeValue(scrambler.scramble(c.getNodeValue()));
				}
			} else {
				scrub(c);
			}
		}
	}

	private static Attr attribute(NamedNodeMap attrs, String localName) {
		for (int i = 0; attrs != null && i < attrs.getLength(); i++) {
			Attr a = (Attr) attrs.item(i);
			if (local(a).equals(localName)) return a;
		}
		return null;
	}

	private static String local(Node n) {
		return n.getLocalName() == null ? n.getNodeName() : n.getLocalName();
	}

}
