/*
 *  Copyright 2026, Plutext Pty Ltd.
 *
 *  This file is part of docx4j.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.docx4j.convert.out.common.preprocess;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.Style;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Where Word leaves the Hyperlink character style off: the runs of a cross-reference's result,
 * and a table of contents' hyperlinks.  Their runs carry {@code w:rStyle} Hyperlink (Word
 * writes it, and so does docx4j's {@code TocGenerator}), and Word draws them in their
 * paragraph's formatting instead: not the style's colour, underline, weight or size.
 *
 * <p>Measured with Word 365 on a probe of 21 cases (docx4j-layout-fidelity's
 * {@code HyperlinkStyleProbes}, 2026-10-12; the probe and Word's readings are in
 * docx4j-core-tests, {@code hyperlink-style/}):</p>
 * <ul>
 * <li>in a REF or PAGEREF field's result the style is left off, with or without {@code \h};</li>
 * <li>in a TOC field's result it is left off a run inside a hyperlink (a {@code w:hyperlink} or a
 *     HYPERLINK field), with or without the TOC's {@code \h}, and kept on a run outside one;</li>
 * <li>a HYPERLINK field's result, or a {@code w:hyperlink}, anywhere else keeps it;</li>
 * <li>the FollowedHyperlink style goes as Hyperlink does; any other character style (one based on
 *     Hyperlink too) and direct formatting stay.</li>
 * </ul>
 * And on the four real-document corpora's Word PDFs: of the Hyperlink-styled runs in a hyperlink
 * in a TOC result whose text could be found, 1,447 drawn black, 288 grey and 45 in the TOC
 * styles' own colours, none in the Hyperlink colour; in a {@code w:hyperlink} outside a field,
 * 486 blue of 535 runs without a colour of their own.
 *
 * <p>This step removes such a {@code w:rStyle} (the styles found by their {@code w:name}, which
 * Word does not translate) in the main document part, on the output's copy of the document
 * ({@link org.docx4j.convert.out.ConversionFeatures#PP_COMMON_FIELD_HYPERLINK_STYLE}).</p>
 *
 * @since 17.3.3
 */
public class FieldHyperlinkStyle {

	private static Logger log = LoggerFactory.getLogger(FieldHyperlinkStyle.class);

	public static void process(WordprocessingMLPackage wmlPackage) {
		Set<String> ids = hyperlinkStyleIds(wmlPackage.getMainDocumentPart().getStyleDefinitionsPart());
		if (ids.isEmpty()) return;
		int n = walk(wmlPackage.getMainDocumentPart().getContent(), new ArrayDeque<Field>(), false, ids);
		if (n > 0) log.debug("Hyperlink style left off " + n + " runs in fields");
	}

	/** The ids of the character styles named Hyperlink or FollowedHyperlink */
	static Set<String> hyperlinkStyleIds(StyleDefinitionsPart sdp) {
		Set<String> ids = new HashSet<String>();
		if (sdp == null || sdp.getJaxbElement() == null) return ids;
		for (Style s : sdp.getJaxbElement().getStyle()) {
			String name = s.getName() == null ? null : s.getName().getVal();
			if ("character".equals(s.getType()) && s.getStyleId() != null
					&& ("hyperlink".equalsIgnoreCase(name) || "followedhyperlink".equalsIgnoreCase(name))) {
				ids.add(s.getStyleId());
			}
		}
		return ids;
	}

	/** A field being read: its instruction, and whether its result has begun */
	private static class Field {
		final StringBuilder instr = new StringBuilder();
		boolean inResult;

		Field(String instr, boolean inResult) {
			this.instr.append(instr);
			this.inResult = inResult;
		}

		/** the field's name, upper case: TOC, REF, PAGEREF, HYPERLINK ... */
		String name() {
			String s = instr.toString().trim();
			int sp = s.indexOf(' ');
			return (sp < 0 ? s : s.substring(0, sp)).toUpperCase();
		}
	}

	/** Whether Word leaves the Hyperlink style off a run here (see the class description) */
	private static boolean leftOff(Deque<Field> fields, boolean inLink) {
		boolean toc = false, hyperlinkField = false;
		for (Field f : fields) {
			if (!f.inResult) continue;
			String name = f.name();
			if (name.equals("REF") || name.equals("PAGEREF")) return true;
			if (name.equals("TOC")) toc = true;
			if (name.equals("HYPERLINK")) hyperlinkField = true;
		}
		return toc && (inLink || hyperlinkField);
	}

	private static int walk(List<Object> content, Deque<Field> fields, boolean inLink, Set<String> ids) {
		if (content == null) return 0;
		int n = 0;
		for (Object o : content) {
			Object u = XmlUtils.unwrap(o);
			if (u instanceof R) {
				R r = (R) u;
				for (Object c : r.getContent()) {
					Object cu = XmlUtils.unwrap(c);
					if (cu instanceof FldChar) {
						STFldCharType t = ((FldChar) cu).getFldCharType();
						if (t == STFldCharType.BEGIN) {
							fields.push(new Field("", false));
						} else if (t == STFldCharType.SEPARATE && !fields.isEmpty()) {
							fields.peek().inResult = true;
						} else if (t == STFldCharType.END && !fields.isEmpty()) {
							fields.pop();
						}
					} else if (c instanceof JAXBElement && cu instanceof Text
							&& "instrText".equals(((JAXBElement<?>) c).getName().getLocalPart())
							&& !fields.isEmpty() && !fields.peek().inResult) {
						String v = ((Text) cu).getValue();
						if (v != null) fields.peek().instr.append(v);
					}
				}
				if (r.getRPr() != null && r.getRPr().getRStyle() != null
						&& ids.contains(r.getRPr().getRStyle().getVal()) && leftOff(fields, inLink)) {
					r.getRPr().setRStyle(null);
					n++;
				}
			} else if (u instanceof CTSimpleField) {
				CTSimpleField f = (CTSimpleField) u;
				fields.push(new Field(f.getInstr() == null ? "" : f.getInstr(), true));
				n += walk(f.getContent(), fields, inLink, ids);
				fields.pop();
			} else if (u instanceof P.Hyperlink) {
				n += walk(((P.Hyperlink) u).getContent(), fields, true, ids);
			} else {
				List<Object> children = TraversalUtil.getChildrenImpl(u);
				if (children != null) n += walk(children, fields, inLink, ids);
			}
		}
		return n;
	}
}
