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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.jaxb.ResolvedAlternateContent;
import org.docx4j.wml.OnOffAdapter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The inspector's view of XML (CR-050): a part's DOM, each element's canonical path (WD Table 9.1),
 * and the walk's small helpers. A part docx4j holds as JAXB is marshalled once, and each element is
 * linked to the object it was marshalled from, so that the text model's selection of a branch and its
 * addresses are found for the stored element.
 */
final class InspectXml {

	static final String W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	static final String R = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
	static final String REL = "http://schemas.openxmlformats.org/package/2006/relationships";
	static final String WP = "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing";
	static final String A = "http://schemas.openxmlformats.org/drawingml/2006/main";
	static final String PIC = "http://schemas.openxmlformats.org/drawingml/2006/picture";
	static final String C = "http://schemas.openxmlformats.org/drawingml/2006/chart";
	static final String DGM = "http://schemas.openxmlformats.org/drawingml/2006/diagram";
	static final String DSP = "http://schemas.microsoft.com/office/drawing/2008/diagram";
	static final String WPS = "http://schemas.microsoft.com/office/word/2010/wordprocessingShape";
	static final String MC = "http://schemas.openxmlformats.org/markup-compatibility/2006";
	static final String V = "urn:schemas-microsoft-com:vml";
	static final String O = "urn:schemas-microsoft-com:office:office";
	static final String W14 = "http://schemas.microsoft.com/office/word/2010/wordml";
	static final String W15 = "http://schemas.microsoft.com/office/word/2012/wordml";
	static final String M = "http://schemas.openxmlformats.org/officeDocument/2006/math";
	static final String X = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
	static final String P = "http://schemas.openxmlformats.org/presentationml/2006/main";
	static final String DECORATIVE = "http://schemas.microsoft.com/office/drawing/2017/decorative";
	static final String CUSTOM_XML = "http://schemas.openxmlformats.org/officeDocument/2006/customXml";

	/** The canonical prefixes (WD Table 9.1), by namespace. */
	private static final Map<String, String> PREFIX = new HashMap<String, String>();
	static {
		String[][] ns = { { "w", W }, { "r", R }, { "rel", REL }, { "wp", WP }, { "a", A }, { "pic", PIC }, { "c", C },
				{ "dgm", DGM }, { "wps", WPS },
				{ "wpg", "http://schemas.microsoft.com/office/word/2010/wordprocessingGroup" }, { "mc", MC }, { "v", V },
				{ "o", O }, { "w10", "urn:schemas-microsoft-com:office:word" }, { "w14", W14 }, { "w15", W15 }, { "m", M },
				{ "x", X }, { "p", P }, { "cp", "http://schemas.openxmlformats.org/package/2006/metadata/core-properties" },
				{ "dc", "http://purl.org/dc/elements/1.1/" }, { "dcterms", "http://purl.org/dc/terms/" },
				{ "ep", "http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" },
				{ "op", "http://schemas.openxmlformats.org/officeDocument/2006/custom-properties" },
				{ "vt", "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes" },
				{ "pkg", "http://schemas.microsoft.com/office/2006/xmlPackage" } };
		for (String[] n : ns) PREFIX.put(n[1], n[0]);
	}

	private InspectXml() {}

	// --- names and paths --------------------------------------------------------------------------------

	/** An element's step name: its canonical prefix and local name, else the namespace name in place of a prefix. */
	static String qname(Element e) {
		String ns = e.getNamespaceURI();
		String local = e.getLocalName() != null ? e.getLocalName() : e.getTagName();
		if (ns == null) return local;
		String p = PREFIX.get(ns);
		return (p == null ? ns : p) + ":" + local;
	}

	static boolean is(Node n, String ns, String local) {
		return n instanceof Element && ns.equals(n.getNamespaceURI()) && local.equals(n.getLocalName());
	}

	/** The canonical paths of one document's elements, each worked out once. */
	static final class Paths {
		private final Map<Element, String> paths = new IdentityHashMap<Element, String>();

		String of(Element e) {
			String p = paths.get(e);
			if (p != null) return p;
			Node parent = e.getParentNode();
			int n = 1;
			if (parent instanceof Element) {
				for (Node s = parent.getFirstChild(); s != null && s != e; s = s.getNextSibling()) {
					if (s instanceof Element && same((Element) s, e)) n++;
				}
				p = of((Element) parent) + "/" + qname(e) + "[" + n + "]";
			} else {
				p = "/" + qname(e) + "[1]";
			}
			paths.put(e, p);
			return p;
		}

		private static boolean same(Element a, Element b) {
			String na = a.getNamespaceURI(), nb = b.getNamespaceURI();
			return (na == null ? nb == null : na.equals(nb)) && a.getLocalName().equals(b.getLocalName());
		}
	}

	// --- navigation (lxml's find, findall, iter, iterancestors) -------------------------------------------

	static Element parent(Element e) {
		Node p = e.getParentNode();
		return p instanceof Element ? (Element) p : null;
	}

	/** The first child element of this name, or null. */
	static Element child(Element e, String ns, String local) {
		if (e == null) return null;
		for (Node c = e.getFirstChild(); c != null; c = c.getNextSibling()) if (is(c, ns, local)) return (Element) c;
		return null;
	}

	/** The child elements of this name. */
	static List<Element> children(Element e, String ns, String local) {
		List<Element> out = new ArrayList<Element>();
		if (e == null) return out;
		for (Node c = e.getFirstChild(); c != null; c = c.getNextSibling()) if (is(c, ns, local)) out.add((Element) c);
		return out;
	}

	/** Every child element. */
	static List<Element> children(Element e) {
		List<Element> out = new ArrayList<Element>();
		if (e == null) return out;
		for (Node c = e.getFirstChild(); c != null; c = c.getNextSibling()) if (c instanceof Element) out.add((Element) c);
		return out;
	}

	/** The element and its descendants of any of these names ({@code ns, local} pairs), in document order. */
	static List<Element> iter(Element root, String... names) {
		List<Element> out = new ArrayList<Element>();
		if (root == null) return out;
		Deque<Node> stack = new ArrayDeque<Node>();
		stack.push(root);
		while (!stack.isEmpty()) {
			Node n = stack.pop();
			if (!(n instanceof Element)) continue;
			Element e = (Element) n;
			if (names.length == 0) out.add(e);
			else for (int i = 0; i < names.length; i += 2) if (is(e, names[i], names[i + 1])) { out.add(e); break; }
			for (Node c = e.getLastChild(); c != null; c = c.getPreviousSibling()) if (c instanceof Element) stack.push(c);
		}
		return out;
	}

	/** The first element of these names at or below {@code root}, in document order, or null. */
	static Element first(Element root, String... names) {
		List<Element> l = iter(root, names);
		return l.isEmpty() ? null : l.get(0);
	}

	/** The nearest ancestor (not the element itself) of any of these names, or null. */
	static Element ancestor(Element e, String... names) {
		for (Element a = parent(e); a != null; a = parent(a)) {
			for (int i = 0; i < names.length; i += 2) if (is(a, names[i], names[i + 1])) return a;
		}
		return null;
	}

	/** An attribute's value, or null where it is absent (DOM gives an empty string). */
	static String attr(Element e, String ns, String local) {
		if (e == null) return null;
		if (ns == null) return e.hasAttribute(local) ? e.getAttribute(local) : null;
		return e.hasAttributeNS(ns, local) ? e.getAttributeNS(ns, local) : null;
	}

	/** {@code w:}'s attribute. */
	static String w(Element e, String local) {
		return attr(e, W, local);
	}

	/**
	 * An ST_OnOff element's value: null where the element is absent; on unless {@code w:val} is exactly 0, false or
	 * off, as Word reads it ({@link OnOffAdapter}, CR-060).
	 */
	static Boolean on(Element e) {
		return on(e, W, "val");
	}

	static Boolean on(Element e, String ns, String local) {
		if (e == null) return null;
		String v = attr(e, ns, local);
		return v == null || OnOffAdapter.read(v);
	}

	static boolean isOn(Element e) {
		return Boolean.TRUE.equals(on(e));
	}

	/** An element's text content (a {@code w:t}'s text). */
	static String text(Element e) {
		return e == null ? "" : e.getTextContent();
	}

	/** Every text node's value at or below an element, in document order (lxml's {@code itertext}). */
	static List<String> texts(Element e) {
		List<String> out = new ArrayList<String>();
		Deque<Node> stack = new ArrayDeque<Node>();
		stack.push(e);
		while (!stack.isEmpty()) {
			Node n = stack.pop();
			if (n.getNodeType() == Node.TEXT_NODE || n.getNodeType() == Node.CDATA_SECTION_NODE) out.add(n.getNodeValue());
			for (Node c = n.getLastChild(); c != null; c = c.getPreviousSibling()) stack.push(c);
		}
		return out;
	}

	// --- a part's DOM -----------------------------------------------------------------------------------

	/** A part's DOM, the number of its elements, and the JAXB object each element was marshalled from. */
	static final class Dom {
		final Document document;
		final Map<Element, Object> jaxb;
		long elements;
		final Paths paths = new Paths();

		Dom(Document document, Map<Element, Object> jaxb, long elements) {
			this.document = document;
			this.jaxb = jaxb;
			this.elements = elements;
		}

		Element root() {
			return document == null ? null : document.getDocumentElement();
		}
	}

	/**
	 * A JAXB tree marshalled, as docx4j writes its parts (its prefix mapper), into a DOM, the elements the
	 * inspector needs linked to the objects they were marshalled from: each element of a {@link #LINKED}
	 * name to the first object of its class whose {@code beforeMarshal} has come and not been taken, which
	 * is taken with those before it. The reference implementation calls a bean's {@code beforeMarshal}
	 * just before its element starts; MOXy starts an element lazily, so its first child's may come first,
	 * and calls it for some objects that make no element of their own: matching by class serves both.
	 */
	static Dom marshal(Object jaxbElement, JAXBContext jc, InspectRun run) throws Exception {
		Marshaller m = jc.createMarshaller();
		try {
			NamespacePrefixMapperUtils.setProperty(m, NamespacePrefixMapperUtils.getPrefixMapper());
		} catch (Exception e) {
			// another JAXB implementation's mapper: the prefixes don't matter here
		}
		final Builder b = new Builder(run);
		m.setListener(new Marshaller.Listener() {
			@Override public void beforeMarshal(Object source) {
				b.pending.add(source instanceof JAXBElement ? ((JAXBElement<?>) source).getValue() : source);
			}
		});
		try {
			m.marshal(jaxbElement, b);
		} catch (Exception e) {
			// the element budget, spent while the part was marshalled: the inspection stops
			for (Throwable t = e; t != null; t = t.getCause()) if (t instanceof Budget) throw new InspectRun.Stop(Inspection.LIMIT_ELEMENTS);
			throw e;
		}
		return new Dom(b.doc, b.jaxb, b.count);
	}

	/**
	 * The record of {@code word} mode put back (CR-051 section 3.2): each {@code mc:AlternateContent}
	 * a load took out of the part, marshalled into this DOM at the promoted content's place, the
	 * promoted elements moved into the branch they came from (so they keep their links to the live
	 * objects the text model recorded), so that the walk finds the element and its unselected
	 * branches where {@code preserve} mode would, and their {@code mce.branch} findings carry the same
	 * addresses.  Applied last record first, since each record's address and index describe the tree
	 * with the records before it applied.  A record whose container the address no longer locates
	 * (the tree was edited since the load) is skipped.
	 */
	static void restore(Dom dom, List<ResolvedAlternateContent> records, JAXBContext jc, InspectRun run) throws Exception {
		for (int k = records.size() - 1; k >= 0; k--) {
			ResolvedAlternateContent r = records.get(k);
			if (r.getBranch() == null || r.getPromotedCount() <= 0) continue; // nothing promoted: no branch to inspect
			Element container = locate(dom.root(), r.getAddress());
			if (container == null) continue;
			List<Element> content = contentChildren(container);
			int from = r.getIndex(), n = r.getPromotedCount();
			if (from < 0 || from + n > content.size()) continue;
			Builder b = new Builder(run, dom.document);
			Marshaller m = jc.createMarshaller();
			try {
				NamespacePrefixMapperUtils.setProperty(m, NamespacePrefixMapperUtils.getPrefixMapper());
			} catch (Exception e) {
				// another JAXB implementation's mapper: the prefixes don't matter here
			}
			m.setListener(new Marshaller.Listener() {
				@Override public void beforeMarshal(Object source) {
					b.pending.add(source instanceof JAXBElement ? ((JAXBElement<?>) source).getValue() : source);
				}
			});
			try {
				m.marshal(r.getElement(), b);
			} catch (Exception e) {
				for (Throwable t = e; t != null; t = t.getCause()) if (t instanceof Budget) throw new InspectRun.Stop(Inspection.LIMIT_ELEMENTS);
				throw e;
			}
			Element ac = null;
			for (Node c = b.fragment.getFirstChild(); c != null; c = c.getNextSibling()) if (c instanceof Element) { ac = (Element) c; break; }
			if (ac == null) continue;
			dom.jaxb.putAll(b.jaxb);
			dom.elements += b.count;
			Element branch = null;
			for (Element c : children(ac)) if (b.jaxb.get(c) == r.getBranch()) { branch = c; break; }
			Node after = content.get(from + n - 1).getNextSibling();
			if (branch != null) {
				while (branch.getFirstChild() != null) branch.removeChild(branch.getFirstChild());
				for (int i = from; i < from + n; i++) branch.appendChild(content.get(i));
			}
			container.insertBefore(ac, after);
		}
	}

	/** The names of the property children a container's content list leaves out. */
	private static final Set<String> PROPERTIES = new HashSet<String>(Arrays.asList("pPr", "rPr", "tcPr", "trPr", "tblPrEx",
			"tblPr", "tblGrid", "sectPr", "customXmlPr", "sdtPr", "sdtEndPr"));

	/** A container's child elements in content-list order: its property children left out. */
	private static List<Element> contentChildren(Element container) {
		List<Element> out = new ArrayList<Element>();
		for (Element c : children(container)) {
			if (W.equals(c.getNamespaceURI()) && PROPERTIES.contains(c.getLocalName())) continue;
			out.add(c);
		}
		return out;
	}

	/**
	 * The element a {@link org.docx4j.jaxb.McSplice#address} names, from the root: a step with an
	 * index is that content child of the element before it; one without is its first child of that
	 * name.  Null where a step finds nothing.
	 */
	static Element locate(Element root, String address) {
		if (root == null || address == null) return null;
		String[] steps = address.split("/");
		Element e = root;
		for (int i = 1; i < steps.length && e != null; i++) {
			String step = steps[i];
			int bracket = step.indexOf('[');
			if (bracket >= 0) {
				int index = Integer.parseInt(step.substring(bracket + 1, step.length() - 1));
				List<Element> content = contentChildren(e);
				e = index < content.size() ? content.get(index) : null;
				if (e != null && !sameName(e, step.substring(0, bracket))) e = null;
			} else {
				Element found = null;
				for (Element c : children(e)) if (sameName(c, step)) { found = c; break; }
				e = found;
			}
		}
		return e;
	}

	/** Whether the element has this {@code prefix:local} name: the local name, and the namespace the conventional prefix names where it is known. */
	private static boolean sameName(Element e, String name) {
		int colon = name.indexOf(':');
		String local = colon < 0 ? name : name.substring(colon + 1);
		if (!local.equals(e.getLocalName())) return false;
		if (colon < 0) return true;
		String ns = new org.docx4j.jaxb.NamespacePrefixMappings().getNamespaceURI(name.substring(0, colon));
		return ns == null || ns.isEmpty() || ns.equals(e.getNamespaceURI());
	}

	/** The element budget spent while marshalling, raised through JAXB as a SAX exception. */
	private static final class Budget extends org.xml.sax.SAXException {
		private static final long serialVersionUID = 1L;
	}

	/**
	 * The elements linked to their objects, by namespace and local name, and the class of each: the text
	 * model's selection reads a branch's object, and its addresses are recorded by paragraph, table,
	 * control, WordArt shape, workbook cell and slide shape.
	 */
	private static final Map<String, Class<?>> LINKED = new HashMap<String, Class<?>>();
	static {
		LINKED.put(W + " p", org.docx4j.wml.P.class);
		LINKED.put(W + " r", org.docx4j.wml.R.class);
		LINKED.put(W + " tbl", org.docx4j.wml.Tbl.class);
		LINKED.put(W + " sdt", org.docx4j.wml.SdtElement.class);
		LINKED.put(MC + " AlternateContent", org.docx4j.mce.AlternateContent.class);
		LINKED.put(MC + " Choice", org.docx4j.mce.AlternateContent.Choice.class);
		LINKED.put(MC + " Fallback", org.docx4j.mce.AlternateContent.Fallback.class);
		LINKED.put(V + " shape", org.docx4j.vml.CTShape.class);
		LINKED.put(X + " c", org.xlsx4j.sml.Cell.class);
		LINKED.put(P + " sp", org.pptx4j.pml.Shape.class);
		LINKED.put(DGM + " pt", org.docx4j.dml.diagram.CTPt.class);
	}

	/** A DOM docx4j holds already (an XML part, a custom XML part): its elements counted, nothing linked. */
	static Dom of(Document doc) {
		long n = doc == null || doc.getDocumentElement() == null ? 0 : iter(doc.getDocumentElement()).size();
		return new Dom(doc, new IdentityHashMap<Element, Object>(), n);
	}

	/** Bytes parsed as XML, without a DTD or external entity. */
	static Dom parse(byte[] bytes) throws Exception {
		Document doc = XmlUtils.getNewDocumentBuilder().parse(new java.io.ByteArrayInputStream(bytes));
		return of(doc);
	}

	/** SAX to DOM, linking each element to the bean marshalled just before it. */
	private static final class Builder extends DefaultHandler {
		final Document doc;
		/** Where a marshal into an existing document builds: its elements, under this fragment. */
		final org.w3c.dom.DocumentFragment fragment;
		final Map<Element, Object> jaxb = new IdentityHashMap<Element, Object>();
		Node current;
		/** The objects whose beforeMarshal has come and that no element has taken, in order. */
		final List<Object> pending = new ArrayList<Object>();
		long count;
		private final List<String[]> prefixes = new ArrayList<String[]>();
		private final StringBuilder text = new StringBuilder();

		private final long allowed;

		Builder(InspectRun run) throws Exception {
			doc = XmlUtils.getNewDocumentBuilder().newDocument();
			fragment = null;
			current = doc;
			allowed = run == null ? Long.MAX_VALUE : run.maxElements - run.elements;
		}

		/** Builds into this document, under a fragment of it, so the elements can be put among the document's own. */
		Builder(InspectRun run, Document into) {
			doc = into;
			fragment = into.createDocumentFragment();
			current = fragment;
			allowed = run == null ? Long.MAX_VALUE : run.maxElements - run.elements;
		}

		@Override public void startPrefixMapping(String prefix, String uri) {
			prefixes.add(new String[] { prefix, uri });
		}

		@Override public void startElement(String uri, String local, String qName, Attributes atts) throws Budget {
			if (count >= allowed) throw new Budget();
			flush();
			Element e = doc.createElementNS(uri == null || uri.isEmpty() ? null : uri, qName == null || qName.isEmpty() ? local : qName);
			for (String[] p : prefixes) {
				e.setAttributeNS("http://www.w3.org/2000/xmlns/", p[0].isEmpty() ? "xmlns" : "xmlns:" + p[0], p[1]);
			}
			prefixes.clear();
			for (int i = 0; i < atts.getLength(); i++) {
				String q = atts.getQName(i);
				if (q != null && (q.equals("xmlns") || q.startsWith("xmlns:"))) continue;
				String ns = atts.getURI(i);
				e.setAttributeNS(ns == null || ns.isEmpty() ? null : ns, q == null || q.isEmpty() ? atts.getLocalName(i) : q, atts.getValue(i));
			}
			current.appendChild(e);
			current = e;
			count++;
			Class<?> expected = uri == null ? null : LINKED.get(uri + " " + local);
			if (expected != null) {
				for (int i = 0; i < pending.size(); i++) {
					if (!expected.isInstance(pending.get(i))) continue;
					jaxb.put(e, pending.get(i));
					pending.subList(0, i + 1).clear();
					break;
				}
			}
		}

		@Override public void endElement(String uri, String local, String qName) {
			flush();
			current = current.getParentNode();
		}

		@Override public void characters(char[] ch, int start, int length) {
			text.append(ch, start, length);
		}

		private void flush() {
			if (text.length() == 0) return;
			current.appendChild(doc.createTextNode(text.toString()));
			text.setLength(0);
		}
	}
}
