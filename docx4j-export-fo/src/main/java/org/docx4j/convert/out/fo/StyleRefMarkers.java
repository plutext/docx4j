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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.docx4j.XmlUtils;
import org.docx4j.model.fields.FormattingSwitchHelper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.CTSimpleField;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.Style;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@code STYLEREF} in a header or footer, which Word evaluates page by page: the text of
 * the first paragraph of the named style on the page, or the nearest one before it
 * where the page has none (the last on the page with {@code \l}; the paragraph's number
 * with {@code \n}, {@code \r} or {@code \w}).  docx4j painted the stored result, which is
 * whatever page the author last saved on, on every page.
 *
 * <p>XSL-FO has exactly this: an {@code fo:marker} on each paragraph of the style, and an
 * {@code fo:retrieve-marker} in the static content, whose
 * {@code retrieve-position="first-starting-within-page"} takes the page's first marker
 * and, where the page has none, the last one before it -
 * {@code retrieve-boundary="document"} lets that reach back across page-sequences, which
 * FOP honours (probed: a page with no heading, and a second page-sequence, both retrieve
 * the heading before them).  The marker's text inherits its formatting from the retrieve
 * point, so the field's own run formatting applies, as {@code \* MERGEFORMAT} intends.</p>
 *
 * <p>Measured on a 179-page mode-12 document whose running header carries
 * {@code STYLEREF "Unnumbered Heading"} and {@code STYLEREF "Heading 1"}: the stored
 * result "Introduction" was painted on some 167 pages where Word shows the chapter the
 * page is in.  Five corpus documents carry the shape.</p>
 *
 * <p>{@link FldSimpleWriter} writes the {@code fo:retrieve-marker}, whose class name
 * carries the style's id; {@link #apply} puts the {@code fo:marker} on every block of that
 * style in the flow (it reads the paragraph style hint, so it is one of the
 * {@link WordLayoutFixups} and needs them on); and {@link #paintStoredResults} keeps the
 * stored result in the copy the header/footer extent pre-pass measures, whose body holds
 * no such paragraphs.  Body-level {@code STYLEREF}, which Word resolves once, is still
 * painted from its stored result.</p>
 *
 * @since 17.1.1
 */
public final class StyleRefMarkers {

	private static final String FO_NS = "http://www.w3.org/1999/XSL/Format";

	/** The class name prefix; the rest is the style id, {@link #className}. */
	static final String CLASS_PREFIX = "docx4j-styleref-";

	private StyleRefMarkers() {}

	/** The marker class for paragraphs of this style: their text, or with {@code number}
	 *  their list label. */
	static String className(String styleId, boolean number) {
		return CLASS_PREFIX + styleId.replaceAll("[^A-Za-z0-9_-]", "_") + (number ? "-n" : "");
	}

	/**
	 * The id of the paragraph style a {@code STYLEREF} argument names: by the style's
	 * name (Word writes the name with its aliases, "Heading 1,h1,Level 1 Topic Heading",
	 * or quoted), by an alias, by the id, case-insensitively; a bare digit is that outline
	 * level's built-in heading style ({@code STYLEREF 1} is "heading 1").
	 *
	 * @return the style id, or null where no paragraph style matches
	 */
	static String resolveStyleId(WordprocessingMLPackage pkg, String argument) {
		if (pkg == null || argument == null) return null;
		String name = argument.trim();
		if (name.length() >= 2 && name.startsWith("\"") && name.endsWith("\"")) {
			name = name.substring(1, name.length() - 1).trim();
		}
		if (name.isEmpty()) return null;
		if (name.matches("[1-9]")) name = "heading " + name;
		StyleDefinitionsPart sdp = pkg.getMainDocumentPart().getStyleDefinitionsPart(false);
		if (sdp == null || sdp.getJaxbElement() == null) return null;
		List<String> wanted = new ArrayList<String>();
		wanted.add(name);
		for (String part : name.split(",")) {
			if (part.trim().length() > 0) wanted.add(part.trim());
		}
		for (Style style : sdp.getJaxbElement().getStyle()) {
			if (style.getType() != null && !"paragraph".equals(style.getType())) continue;
			if (matches(style, wanted)) return style.getStyleId();
		}
		return null;
	}

	private static boolean matches(Style style, List<String> wanted) {
		List<String> names = new ArrayList<String>();
		if (style.getName() != null && style.getName().getVal() != null) {
			names.add(style.getName().getVal());
			for (String part : style.getName().getVal().split(",")) names.add(part.trim());
		}
		if (style.getAliases() != null && style.getAliases().getVal() != null) {
			for (String alias : style.getAliases().getVal().split(",")) names.add(alias.trim());
		}
		if (style.getStyleId() != null) names.add(style.getStyleId());
		for (String w : wanted) {
			for (String n : names) {
				if (n.equalsIgnoreCase(w)) return true;
			}
		}
		return false;
	}

	/**
	 * Whether this field is a {@code STYLEREF} whose result is to be retrieved per page:
	 * one in a header or footer, naming a paragraph style the document has.
	 *
	 * @return the style id, or null
	 */
	static String retrievedStyleId(WordprocessingMLPackage pkg, boolean inHeaderOrFooter, String fldName,
			List<String> parameters) {
		if (!inHeaderOrFooter || !"STYLEREF".equals(fldName) || parameters == null || parameters.isEmpty()) {
			return null;
		}
		if (!WordLayoutFixups.isEnabled()) return null; // the markers are one of the fixups
		return resolveStyleId(pkg, parameters.get(0));
	}

	/** Whether the field asks for the paragraph's number rather than its text
	 *  ({@code \n}, {@code \r}, {@code \w}). */
	static boolean wantsNumber(List<String> parameters) {
		for (String p : parameters) {
			if ("\\n".equals(p) || "\\r".equals(p) || "\\w".equals(p)) return true;
		}
		return false;
	}

	/** Whether the field asks for the last paragraph on the page ({@code \l}). */
	static boolean wantsLast(List<String> parameters) {
		for (String p : parameters) {
			if ("\\l".equals(p)) return true;
		}
		return false;
	}

	// ------------------------------------------------------------------ the markers

	/**
	 * Put an {@code fo:marker} on every paragraph block whose style a retrieve-marker in
	 * this document asks for.  The block's paragraph-style hint says which style it is;
	 * the marker is its initial child, as XSL-FO requires, and only in the flow: never in
	 * static content, where XSL-FO forbids it, nor in a footnote, which Word does not
	 * search.  A text box or a floating table on the page counts, as it does for Word:
	 * measured, a cover title in a table cell's text box is what a document's running
	 * header shows on every later page.
	 */
	static void apply(Document doc) {
		Set<String> wanted = new HashSet<String>();
		for (Element rm : elements(doc, "retrieve-marker")) {
			String name = rm.getAttribute("retrieve-class-name");
			if (name.startsWith(CLASS_PREFIX)) wanted.add(name);
		}
		if (wanted.isEmpty()) return;
		for (Element block : elements(doc, "block")) {
			if (!block.hasAttribute(WordLayoutFixups.HINT_PSTYLE)) continue;
			String styleId = block.getAttribute(WordLayoutFixups.HINT_PSTYLE);
			if (styleId.length() == 0) continue;
			String text = className(styleId, false), number = className(styleId, true);
			boolean wantText = wanted.contains(text), wantNumber = wanted.contains(number);
			if (!wantText && !wantNumber) continue;
			if (!inFlow(block)) continue;
			if (wantNumber) insertMarker(doc, block, number, labelText(block));
			if (wantText) insertMarker(doc, block, text, markerText(block));
		}
	}

	private static void insertMarker(Document doc, Element block, String className, String text) {
		Element marker = doc.createElementNS(FO_NS, "fo:marker");
		marker.setAttribute("marker-class-name", className);
		marker.appendChild(doc.createTextNode(text));
		block.insertBefore(marker, block.getFirstChild());
	}

	/** Whether the block is in the page-sequence's flow, and not in static content (where
	 *  an fo:marker is not allowed) or a footnote (which Word does not search). */
	private static boolean inFlow(Element block) {
		for (Node n = block.getParentNode(); n instanceof Element; n = n.getParentNode()) {
			Element e = (Element) n;
			if (!FO_NS.equals(e.getNamespaceURI())) continue;
			String name = e.getLocalName();
			if ("flow".equals(name)) return true;
			if ("static-content".equals(name) || "footnote-body".equals(name)) return false;
		}
		return false;
	}

	/** The paragraph's text: what a reader sees, without its footnotes (reference mark
	 *  and all), leaders, page numbers or pictures, white space collapsed. */
	static String markerText(Element block) {
		StringBuilder sb = new StringBuilder();
		collectText(block, sb);
		return sb.toString().replaceAll("\\s+", " ").trim();
	}

	private static void collectText(Node n, StringBuilder sb) {
		for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c.getNodeType() == Node.TEXT_NODE || c.getNodeType() == Node.CDATA_SECTION_NODE) {
				sb.append(c.getNodeValue());
			} else if (c instanceof Element) {
				Element e = (Element) c;
				String name = e.getLocalName();
				if ("footnote".equals(name) || "float".equals(name) || "block-container".equals(name)
						|| "leader".equals(name) || "page-number".equals(name)
						|| "page-number-citation".equals(name) || "page-number-citation-last".equals(name)
						|| "external-graphic".equals(name) || "instream-foreign-object".equals(name)
						|| "marker".equals(name) || "retrieve-marker".equals(name)) {
					continue;
				}
				if ("block".equals(name)) sb.append(' '); // a nested block (a w:br) ends a line
				collectText(e, sb);
			}
		}
	}

	/** The list label of the item the block is the body of, or "" where it is not one. */
	static String labelText(Element block) {
		for (Node n = block.getParentNode(); n instanceof Element; n = n.getParentNode()) {
			Element e = (Element) n;
			if (!FO_NS.equals(e.getNamespaceURI())) continue;
			if ("list-item".equals(e.getLocalName())) {
				for (Node c = e.getFirstChild(); c != null; c = c.getNextSibling()) {
					if (c instanceof Element && "list-item-label".equals(((Element) c).getLocalName())) {
						return markerText((Element) c);
					}
				}
				return "";
			}
			if ("flow".equals(e.getLocalName()) || "static-content".equals(e.getLocalName())) break;
		}
		return "";
	}

	private static List<Element> elements(Document doc, String localName) {
		NodeList nl = doc.getElementsByTagNameNS(FO_NS, localName);
		List<Element> out = new ArrayList<Element>(nl.getLength());
		for (int i = 0; i < nl.getLength(); i++) out.add((Element) nl.item(i));
		return out;
	}

	// ------------------------------------------------------ the extent pre-pass copy

	/**
	 * Replace every {@code STYLEREF} field in the package's headers and footers by its
	 * stored result.  For the copy the header/footer extent pre-pass measures: its body is
	 * filler, so a retrieve-marker there finds nothing and a header line holding only the
	 * field would measure as no line at all, where the real header has one.
	 */
	static void paintStoredResults(WordprocessingMLPackage pkg) {
		org.docx4j.openpackaging.parts.relationships.RelationshipsPart relPart
			= pkg.getMainDocumentPart().getRelationshipsPart();
		if (relPart == null) return;
		for (org.docx4j.relationships.Relationship rs : relPart.getRelationships().getRelationship()) {
			Object hdrFtr = null;
			try {
				if (org.docx4j.openpackaging.parts.relationships.Namespaces.HEADER.equals(rs.getType())) {
					hdrFtr = ((org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart) relPart.getPart(rs)).getJaxbElement();
				} else if (org.docx4j.openpackaging.parts.relationships.Namespaces.FOOTER.equals(rs.getType())) {
					hdrFtr = ((org.docx4j.openpackaging.parts.WordprocessingML.FooterPart) relPart.getPart(rs)).getJaxbElement();
				}
			} catch (RuntimeException e) {
				continue;
			}
			if (hdrFtr instanceof ContentAccessor) paintStoredResults(((ContentAccessor) hdrFtr).getContent());
		}
	}

	private static void paintStoredResults(List<Object> content) {
		if (content == null) return;
		for (int i = 0; i < content.size(); i++) {
			Object o = XmlUtils.unwrap(content.get(i));
			if (o instanceof CTSimpleField
					&& "STYLEREF".equals(FormattingSwitchHelper.getFldSimpleName(((CTSimpleField) o).getInstr()))) {
				List<Object> result = new ArrayList<Object>(((CTSimpleField) o).getContent());
				content.remove(i);
				content.addAll(i, result);
				i += result.size() - 1;
			} else if (o instanceof SdtElement) {
				SdtElement sdt = (SdtElement) o;
				if (sdt.getSdtContent() != null) paintStoredResults(sdt.getSdtContent().getContent());
			} else if (o instanceof ContentAccessor) {
				paintStoredResults(((ContentAccessor) o).getContent());
			}
		}
	}
}
