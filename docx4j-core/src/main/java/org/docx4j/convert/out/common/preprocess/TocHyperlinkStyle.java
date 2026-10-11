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
import org.docx4j.wml.R;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.Style;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Word draws a hyperlinked table of contents' entries without the Hyperlink character style.
 * The runs of a TOC field with the {@code \h} switch carry {@code w:rStyle} Hyperlink (Word
 * writes it, and so does docx4j's {@code TocGenerator}), but Word draws them in their
 * paragraph's formatting, neither in the Hyperlink colour nor underlined; the same entries in a
 * TOC which is no longer a field keep the style.  Measured on Word's PDFs of the four
 * real-document corpora (2026-10-12): of the Hyperlink-styled runs in a TOC {@code \h} result
 * whose text could be found, 1,447 drawn black, 288 grey and 45 in the TOC styles' own
 * colours, none in the Hyperlink colour; in a {@code w:hyperlink} outside a TOC the style
 * applies (486 blue of 535 runs without a colour of their own).
 *
 * <p>This step removes a {@code w:rStyle} naming the Hyperlink style (found by its
 * {@code w:name}, which Word does not translate) from the runs of a TOC {@code \h} field's
 * result, in the main document part, on the output's copy of the document
 * ({@link org.docx4j.convert.out.ConversionFeatures#PP_COMMON_TOC_HYPERLINK_STYLE}).
 * Hyperlinked cross-references are said to behave alike; none in the corpora carries the
 * style, so they are left as they are.</p>
 *
 * @since 17.3.3
 */
public class TocHyperlinkStyle {

	private static Logger log = LoggerFactory.getLogger(TocHyperlinkStyle.class);

	public static void process(WordprocessingMLPackage wmlPackage) {
		Set<String> ids = hyperlinkStyleIds(wmlPackage.getMainDocumentPart().getStyleDefinitionsPart());
		if (ids.isEmpty()) return;
		int n = walk(wmlPackage.getMainDocumentPart().getContent(), new ArrayDeque<Field>(), ids);
		if (n > 0) log.debug("Hyperlink style left off " + n + " TOC entry runs");
	}

	/** The ids of the character styles named Hyperlink */
	static Set<String> hyperlinkStyleIds(StyleDefinitionsPart sdp) {
		Set<String> ids = new HashSet<String>();
		if (sdp == null || sdp.getJaxbElement() == null) return ids;
		for (Style s : sdp.getJaxbElement().getStyle()) {
			if ("character".equals(s.getType()) && s.getName() != null
					&& "hyperlink".equalsIgnoreCase(s.getName().getVal()) && s.getStyleId() != null) {
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

		boolean isHyperlinkedToc() {
			String s = instr.toString().trim();
			return (s.startsWith("TOC ") || s.equals("TOC")) && s.contains("\\h");
		}
	}

	private static boolean inHyperlinkedToc(Deque<Field> fields) {
		for (Field f : fields) {
			if (f.inResult && f.isHyperlinkedToc()) return true;
		}
		return false;
	}

	private static int walk(List<Object> content, Deque<Field> fields, Set<String> ids) {
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
						&& ids.contains(r.getRPr().getRStyle().getVal()) && inHyperlinkedToc(fields)) {
					r.getRPr().setRStyle(null);
					n++;
				}
			} else if (u instanceof CTSimpleField) {
				CTSimpleField f = (CTSimpleField) u;
				fields.push(new Field(f.getInstr() == null ? "" : f.getInstr(), true));
				n += walk(f.getContent(), fields, ids);
				fields.pop();
			} else {
				List<Object> children = TraversalUtil.getChildrenImpl(u);
				if (children != null) n += walk(children, fields, ids);
			}
		}
		return n;
	}
}
