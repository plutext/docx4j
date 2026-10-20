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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;

import org.docx4j.mce.AlternateContent;
import org.docx4j.wml.CTSdtCell;
import org.docx4j.wml.CTSdtRow;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtRun;
import org.jvnet.jaxb.lang.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A {@code w:sdt} directly inside {@code mc:Choice} or {@code mc:Fallback} (CR-051 section 11.3).
 * A branch's content is a lax any-element list, so JAXB binds a child by its root element name
 * alone, and four classes carry {@code @XmlRootElement(name = "sdt")}: the one the context answers
 * with ({@code CTSdtRow}) admits rows only, so a block, run or cell control there lost its content
 * or failed the unmarshal.  Now, on the way in, such an element is put in a private namespace
 * ({@link #NAMESPACE}), so the list delivers it as a DOM element, as it does a {@code w:customXml};
 * and once the tree stands, it is bound by the kind its container asks for ({@link #bind}):
 * {@code SdtBlock} in block content, {@code SdtRun} in a paragraph's, {@code CTSdtRow} in a
 * table's, {@code CTSdtCell} in a row's.  The private namespace never reaches output: a bound
 * control is an object again, and one that cannot be bound is put back in the {@code w} namespace.
 *
 * <p>The way in: {@link #wrap(XMLStreamReader)} on a StAX source, {@link #rename(Node)} on a DOM
 * before it is unmarshalled.  The binding: the part's unmarshal ({@link #rebind}), with its own
 * listener's records; {@code XmlUtils}' unmarshal methods, with a {@link Collector}; and the
 * splice, for an element promoted out of a branch ({@code McSplice.bind}).</p>
 *
 * @since 17.3.4 (CR-051 phase 1)
 */
public final class McBranchSdt {

	private static final Logger log = LoggerFactory.getLogger(McBranchSdt.class);

	/**
	 * The private namespace a {@code w:sdt} directly in a branch carries between the parse and its
	 * binding.  A URN in a name nobody else writes: no producer declares it, no schema docx4j binds
	 * names it, and it is never written out ({@link #restore}), so it cannot collide with a
	 * document's own namespaces.
	 */
	public static final String NAMESPACE = "urn:docx4j:mc-branch-sdt";

	/** The prefix the renamed element carries while it is a DOM element. */
	public static final String PREFIX = "mcsdt";

	static final String W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
	static final String MC_NS = "http://schemas.openxmlformats.org/markup-compatibility/2006";

	private McBranchSdt() {}

	// --- the way in: StAX -------------------------------------------------------------------------

	/**
	 * The reader with each {@code w:sdt} that is a direct child of {@code mc:Choice} or
	 * {@code mc:Fallback} answering with {@link #NAMESPACE}.  Elsewhere the reader is passed through
	 * untouched; the cost is a name test per element.
	 */
	public static XMLStreamReader wrap(XMLStreamReader reader) {
		return reader instanceof Delegate ? reader : new Delegate(reader);
	}

	private static final class Delegate extends StreamReaderDelegate {
		/** For each open element: whether it is a Choice or a Fallback. */
		private boolean[] branch = new boolean[64];
		private int depth; // the number of open elements
		/** Whether the current START_ELEMENT or END_ELEMENT is a renamed one. */
		private boolean renamed;

		Delegate(XMLStreamReader reader) {
			super(reader);
		}

		@Override
		public int next() throws XMLStreamException {
			int event = super.next();
			track(event);
			return event;
		}

		@Override
		public int nextTag() throws XMLStreamException {
			int event = super.nextTag();
			track(event);
			return event;
		}

		private void track(int event) {
			if (event == START_ELEMENT) {
				String ns = super.getNamespaceURI();
				String local = super.getLocalName();
				boolean parentIsBranch = depth > 0 && branch[depth - 1];
				renamed = parentIsBranch && "sdt".equals(local) && W_NS.equals(ns);
				boolean isBranch = !renamed && MC_NS.equals(ns) && ("Choice".equals(local) || "Fallback".equals(local));
				if (depth == branch.length) branch = java.util.Arrays.copyOf(branch, depth * 2);
				branch[depth++] = isBranch;
			} else if (event == END_ELEMENT) {
				depth = Math.max(0, depth - 1);
				renamed = depth > 0 && branch[depth - 1] && "sdt".equals(super.getLocalName()) && W_NS.equals(super.getNamespaceURI());
			} else {
				renamed = false;
			}
		}

		@Override
		public String getNamespaceURI() {
			return renamed ? McBranchSdt.NAMESPACE : super.getNamespaceURI();
		}

		@Override
		public String getPrefix() {
			return renamed ? McBranchSdt.PREFIX : super.getPrefix();
		}

		@Override
		public QName getName() {
			return renamed ? new QName(McBranchSdt.NAMESPACE, "sdt", McBranchSdt.PREFIX) : super.getName();
		}
	}

	// --- the way in: SAX (the preprocessor's result straight into JAXB) ----------------------------------

	/**
	 * A content handler in front of this one, with each {@code w:sdt} that is a direct child of
	 * {@code mc:Choice} or {@code mc:Fallback} renamed into {@link #NAMESPACE}: for an XSLT result
	 * that goes straight into an unmarshaller handler ({@code JAXBResult}), where neither a reader
	 * nor a DOM can be touched.
	 */
	public static org.xml.sax.ContentHandler wrap(org.xml.sax.ContentHandler handler) {
		return new SaxRenamer(handler);
	}

	private static final class SaxRenamer extends org.xml.sax.helpers.XMLFilterImpl {
		private boolean[] branch = new boolean[64];
		private int depth;

		SaxRenamer(org.xml.sax.ContentHandler handler) {
			setContentHandler(handler);
		}

		@Override
		public void startElement(String uri, String local, String qName, org.xml.sax.Attributes atts) throws org.xml.sax.SAXException {
			boolean parentIsBranch = depth > 0 && branch[depth - 1];
			boolean renamed = parentIsBranch && "sdt".equals(local) && W_NS.equals(uri);
			boolean isBranch = !renamed && MC_NS.equals(uri) && ("Choice".equals(local) || "Fallback".equals(local));
			if (depth == branch.length) branch = java.util.Arrays.copyOf(branch, depth * 2);
			branch[depth++] = isBranch;
			if (renamed) {
				super.startPrefixMapping(PREFIX, NAMESPACE);
				super.startElement(NAMESPACE, local, PREFIX + ":sdt", atts);
			} else {
				super.startElement(uri, local, qName, atts);
			}
		}

		@Override
		public void endElement(String uri, String local, String qName) throws org.xml.sax.SAXException {
			depth = Math.max(0, depth - 1);
			boolean renamed = depth > 0 && branch[depth - 1] && "sdt".equals(local) && W_NS.equals(uri);
			if (renamed) {
				super.endElement(NAMESPACE, local, PREFIX + ":sdt");
				super.endPrefixMapping(PREFIX);
			} else {
				super.endElement(uri, local, qName);
			}
		}
	}

	// --- the way in: DOM ----------------------------------------------------------------------------

	/**
	 * Renames, in place, each {@code w:sdt} that is a direct child of {@code mc:Choice} or
	 * {@code mc:Fallback} under this node into {@link #NAMESPACE}; returns the elements renamed,
	 * for {@link #restore(Collection)} where the DOM is a caller's.
	 */
	public static List<Element> rename(Node node) {
		List<Element> renamed = new ArrayList<Element>();
		if (node == null) return renamed;
		Document doc = node instanceof Document ? (Document) node : node.getOwnerDocument();
		Element root = node instanceof Document ? ((Document) node).getDocumentElement() : node instanceof Element ? (Element) node : null;
		if (doc == null || root == null) return renamed;
		for (String name : new String[] { "Choice", "Fallback" }) {
			NodeList branches = root.getElementsByTagNameNS(MC_NS, name);
			for (int i = 0; i < branches.getLength(); i++) {
				for (Node c = branches.item(i).getFirstChild(); c != null; c = c.getNextSibling()) {
					if (c instanceof Element && W_NS.equals(c.getNamespaceURI()) && "sdt".equals(c.getLocalName())) {
						renamed.add((Element) doc.renameNode(c, NAMESPACE, PREFIX + ":sdt"));
					}
				}
			}
		}
		if (isBranch(root)) {
			// the node itself a branch: its own children too
			for (Node c = root.getFirstChild(); c != null; c = c.getNextSibling()) {
				if (c instanceof Element && W_NS.equals(c.getNamespaceURI()) && "sdt".equals(c.getLocalName())) {
					renamed.add((Element) doc.renameNode(c, NAMESPACE, PREFIX + ":sdt"));
				}
			}
		}
		return renamed;
	}

	private static boolean isBranch(Element e) {
		return MC_NS.equals(e.getNamespaceURI()) && ("Choice".equals(e.getLocalName()) || "Fallback".equals(e.getLocalName()));
	}

	/** Puts each element back in the {@code w} namespace, as {@code w:sdt}. */
	public static void restore(Collection<Element> renamed) {
		for (Element e : renamed) restore(e);
	}

	/** Puts the element back in the {@code w} namespace, as {@code w:sdt}; returns it. */
	public static Element restore(Element e) {
		if (e == null || !NAMESPACE.equals(e.getNamespaceURI())) return e;
		return (Element) e.getOwnerDocument().renameNode(e, W_NS, "w:sdt");
	}

	/** Whether the object is a renamed element, waiting to be bound. */
	public static boolean isBranchSdt(Object o) {
		return o instanceof Element && NAMESPACE.equals(((Element) o).getNamespaceURI()) && "sdt".equals(((Element) o).getLocalName());
	}

	/** Whether any branch of the element holds a renamed element, waiting to be bound. */
	public static boolean holdsBranchSdt(AlternateContent ac) {
		for (Object branch : McSelection.branches(ac, McMode.ALL)) {
			for (Object o : McSplice.contentOf(branch)) if (isBranchSdt(o)) return true;
		}
		return false;
	}

	// --- the binding --------------------------------------------------------------------------------

	/** The kind of control the container holds: a table's rows, a row's cells, block content's blocks, else a paragraph's runs. */
	public static Class<? extends org.docx4j.wml.SdtElement> kindFor(Object container) {
		if (McSplice.isRowContainer(container)) return CTSdtRow.class;
		if (McSplice.isCellContainer(container)) return CTSdtCell.class;
		if (McSplice.isBlockContainer(container)) return SdtBlock.class;
		return SdtRun.class;
	}

	/**
	 * The element bound as the kind its container asks for, bare (a branch list and the block
	 * containers hold the object itself; {@link #wrapFor} gives a container's wrapper).  Nested
	 * branches inside it are renamed first and bound in their turn through the unmarshaller's
	 * {@link Collector}, where it has one.  Where the binding fails the element is put back in the
	 * {@code w} namespace and returned as it is, so a save still writes {@code w:sdt}.
	 */
	public static Object bind(Element el, Object container, Unmarshaller u) {
		Class<? extends org.docx4j.wml.SdtElement> kind = kindFor(container);
		try {
			rename(el); // nested branches inside the control
			JAXBElement<? extends org.docx4j.wml.SdtElement> bound = u.unmarshal(el, kind);
			return bound.getValue();
		} catch (Exception e) {
			log.warn("w:sdt directly in an mc:AlternateContent branch could not be bound as " + kind.getSimpleName()
					+ "; left as a DOM element: " + e.getMessage());
			return restore(el);
		}
	}

	/** The bound control as the container's content list holds one: bare for block content, wrapped as {@code w:sdt} elsewhere. */
	public static Object wrapFor(Object bound, Object container) {
		if (bound instanceof SdtBlock || bound instanceof Element) return bound;
		return new JAXBElement(new QName(W_NS, "sdt"), bound.getClass(), bound);
	}

	/**
	 * Binds, in place, each renamed element in these elements' branches by the kind the element's
	 * effective container asks for ({@link McSplice#effectiveContainer}), with this unmarshaller;
	 * where it carries a {@link Collector}, the elements nested in what was bound are bound in
	 * their turn.  Returns the number bound.
	 */
	public static int rebind(Collection<AlternateContent> elements, Unmarshaller u) {
		if (elements == null || elements.isEmpty()) return 0;
		int n = 0;
		List<AlternateContent> pending = new ArrayList<AlternateContent>(elements);
		Collector collector = u.getListener() instanceof Collector ? (Collector) u.getListener() : null;
		for (int round = 0; !pending.isEmpty() && round < 64; round++) {
			for (AlternateContent ac : pending) {
				Object container = McSplice.effectiveContainer(ac);
				for (Object branch : McSelection.branches(ac, McMode.ALL)) {
					List<Object> any = McSplice.contentOf(branch);
					for (int i = 0; i < any.size(); i++) {
						if (!isBranchSdt(any.get(i))) continue;
						Object bound = bind((Element) any.get(i), container, u);
						any.set(i, bound);
						if (bound instanceof Child) ((Child) bound).setParent(branch);
						if (!(bound instanceof Element)) n++;
					}
				}
			}
			pending = collector == null ? new ArrayList<AlternateContent>() : collector.take();
		}
		return n;
	}

	/** An unmarshaller of this context with a {@link Collector}, for binding outside a part. */
	public static Unmarshaller unmarshaller(jakarta.xml.bind.JAXBContext jc) throws jakarta.xml.bind.JAXBException {
		Unmarshaller u = jc.createUnmarshaller();
		u.setEventHandler(new JaxbValidationEventHandler());
		u.setListener(new Collector());
		return u;
	}

	/** Binds, in place, every renamed element under this tree's elements that the collector saw. */
	public static int rebindAll(Collector collector, Unmarshaller u) {
		return rebind(collector.take(), u);
	}

	/**
	 * An unmarshal listener that notes each {@code mc:AlternateContent} holding a renamed element,
	 * for {@link #rebind} once the tree stands (the branch's parent is not yet set when the
	 * callback fires).  {@code Docx4jUnmarshallerListener} does the same for a part.
	 */
	public static class Collector extends Unmarshaller.Listener {
		private final List<AlternateContent> found = new ArrayList<AlternateContent>();

		@Override
		public void afterUnmarshal(Object target, Object parent) {
			if (target instanceof AlternateContent && holdsBranchSdt((AlternateContent) target)) found.add((AlternateContent) target);
		}

		/** The elements noted so far; the list is cleared. */
		public List<AlternateContent> take() {
			List<AlternateContent> taken = new ArrayList<AlternateContent>(found);
			found.clear();
			return taken;
		}
	}
}
