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

import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMResult;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchema;
import jakarta.xml.bind.annotation.XmlType;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.mce.AlternateContent;
import org.docx4j.utils.XMLStreamWriterWrapper;
import org.docx4j.utils.XMLStreamWriterWrapperIndenting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Where a marshal declares each namespace prefix the tree's {@code mc} attributes name
 * (CR-052 section 4): the root's {@code mc:Ignorable}, each {@code mc:Choice}'s
 * {@code Requires}, and a Choice's own {@code mc:Ignorable} and {@code mc:MustUnderstand}.
 *
 * <p>{@link #plan} reads the tree once and places every prefix, as Word 365 does (D4 (c)):</p>
 * <ul>
 * <li>a prefix bound as docx4j's table binds it ({@link NamespacePrefixMappings}) goes on the root,
 *     as Word puts its fixed set, through the prefix mapper's Ignorable value as before
 *     ({@link Plan#tablePrefixes()});</li>
 * <li>a prefix the root's {@code mc:Ignorable} names with a binding the table does not give (a
 *     producer's private prefix, recorded at unmarshal: {@link McSourceBindings}) goes on the root
 *     too, as a (prefix, URI) pair the mapper pre-declares ({@link Plan#rootPairs()});</li>
 * <li>any other goes on the {@code mc:Choice} that names it, with the source's binding, ahead of
 *     {@code Requires} as Word writes it ({@link Plan#onChoice}): a private prefix, an alias for a
 *     namespace the table binds to another prefix, or a table prefix the source bound to another
 *     namespace.  Where the root keeps that same binding anyway it is declared once, on the root.
 *     Nothing goes on {@code mc:Fallback};</li>
 * <li>a prefix with no recorded binding and none in the table stays undeclared, with a warning, as
 *     before.</li>
 * </ul>
 *
 * <p>The source's binding comes first, then the table (D3).  Where a Choice's own content uses the
 * namespace the table binds the prefix to, a declaration on the Choice would rebind that content,
 * so the content's binding stands and a warning is logged.</p>
 *
 * <p>JAXB's prefix mapper can pre-declare on the root only, so a marshal with a declaration to put
 * on a Choice goes through an {@link XMLStreamWriter} ({@link #marshal}): the marshaller's
 * listener is told of each Choice before its start tag, and the writer wrapper puts the
 * declarations on that element.  A marshal with none keeps its usual path.  Both JAXB runtimes
 * (the RI and MOXy) fire the listener before the element is written.</p>
 *
 * @since 17.3.4 (CR-052 phase 1)
 */
public final class McDeclarations {

	private static final Logger log = LoggerFactory.getLogger(McDeclarations.class);

	private McDeclarations() {}

	/** The placements for one marshal of one tree. */
	public static final class Plan {

		/** Every declaration on the root: prefix to URI, in the order found. */
		private final Map<String, String> root = new LinkedHashMap<String, String>();
		/** The root prefixes bound as the table binds them: the mapper declares these from its Ignorable value. */
		private final Set<String> fromTable = new LinkedHashSet<String>();
		/** The declarations on each Choice that gets its own. */
		private final IdentityHashMap<AlternateContent.Choice, Map<String, String>> onChoice =
				new IdentityHashMap<AlternateContent.Choice, Map<String, String>>();
		/** Prefixes named somewhere and resolved nowhere. */
		private final Set<String> undeclared = new TreeSet<String>();

		Plan() {}

		/** Whether any Choice gets a declaration of its own, which needs the XMLStreamWriter route. */
		public boolean declaresOnChoices() {
			return !onChoice.isEmpty();
		}

		/** The declarations this Choice gets on its own element (prefix to URI), or null. */
		public Map<String, String> onChoice(AlternateContent.Choice choice) {
			return onChoice.get(choice);
		}

		/** Every declaration the root gets, prefix to URI. */
		public Map<String, String> onRoot() {
			return Collections.unmodifiableMap(root);
		}

		/**
		 * The root prefixes the table binds as the source did, space-separated, for the prefix
		 * mapper's Ignorable value ({@link McIgnorableNamespaceDeclarator#setMcIgnorable}), which
		 * declares them through the table as it always has; null where there are none.
		 */
		public String tablePrefixes() {
			if (fromTable.isEmpty()) return null;
			StringBuilder sb = new StringBuilder();
			for (String p : fromTable) sb.append(sb.length() == 0 ? "" : " ").append(p);
			return sb.toString();
		}

		/**
		 * The root declarations the table does not give: prefix to the source's URI, for the
		 * mapper to pre-declare as pairs ({@link NamespacePrefixMapperUtils#getPreDeclaredNamespaceUris2}).
		 */
		public Map<String, String> rootPairs() {
			Map<String, String> pairs = new LinkedHashMap<String, String>();
			for (Map.Entry<String, String> e : root.entrySet()) {
				if (!fromTable.contains(e.getKey())) pairs.put(e.getKey(), e.getValue());
			}
			return pairs;
		}

		/** Every prefix declared anywhere, space-separated (a canonicalizer's inclusive list); null where none. */
		public String allPrefixes() {
			LinkedHashSet<String> all = new LinkedHashSet<String>(root.keySet());
			for (Map<String, String> m : onChoice.values()) all.addAll(m.keySet());
			if (all.isEmpty()) return null;
			StringBuilder sb = new StringBuilder();
			for (String p : all) sb.append(sb.length() == 0 ? "" : " ").append(p);
			return sb.toString();
		}

		/** The prefixes named somewhere and declared nowhere (warned about). */
		public Set<String> undeclared() {
			return Collections.unmodifiableSet(undeclared);
		}

		/** Puts the root's declarations on a DOM element (the canonicalizing path marshals to a DOM first). */
		public void declareOnRoot(Element rootElement) {
			for (Map.Entry<String, String> e : root.entrySet()) {
				rootElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:" + e.getKey(), e.getValue());
			}
		}

		private boolean putRoot(String prefix, String uri, boolean asTable) {
			String have = root.get(prefix);
			if (have != null) {
				if (!have.equals(uri)) {
					log.warn("Prefix '" + prefix + "' is bound to " + have + " on the root; not also to " + uri);
					return false;
				}
				return true;
			}
			root.put(prefix, uri);
			if (asTable) fromTable.add(prefix);
			return true;
		}

		@Override
		public String toString() {
			return "root " + root + ", on " + onChoice.size() + " Choice(s) " + new ArrayList<Map<String, String>>(onChoice.values())
					+ (undeclared.isEmpty() ? "" : ", undeclared " + undeclared);
		}
	}

	/**
	 * Plans the declarations for a marshal of {@code root}.
	 *
	 * @param root the object to be marshalled (a {@code JAXBElement} is unwrapped)
	 * @param prefixes further prefixes to declare on the root, space-separated, or null: the
	 *     root's {@code mc:Ignorable} as the part reports it, a part's registered Choice prefixes
	 *     ({@code JaxbXmlPart.getMcChoiceNamespaces()}), a caller's {@code mcChoiceNamespace}.
	 *     The root's own {@code mc:Ignorable} is read from the object whether or not it is passed.
	 */
	public static Plan plan(Object root, String prefixes) {
		Plan plan = new Plan();
		Object r = XmlUtils.unwrap(root);
		if (r == null) return plan;
		Map<String, String> rootRecord = McSourceBindings.of(r);
		Set<String> unresolved = new LinkedHashSet<String>();

		// the root: its mc:Ignorable, then what the caller asks for
		LinkedHashSet<String> rootPrefixes = new LinkedHashSet<String>();
		rootPrefixes.addAll(McSourceBindings.prefixes(NamespacePrefixMapperUtils.ignorableOf(r)));
		rootPrefixes.addAll(McSourceBindings.prefixes(prefixes));
		for (String p : rootPrefixes) {
			String src = rootRecord == null ? null : rootRecord.get(p);
			String tbl = table(p);
			String uri = src != null ? src : tbl;
			if (uri == null) {
				unresolved.add(p);
				continue;
			}
			plan.putRoot(p, uri, uri.equals(tbl));
		}

		// each Choice
		for (AlternateContent.Choice choice : choices(r)) {
			Map<String, String> record = McSourceBindings.of(choice);
			for (String p : McSourceBindings.prefixesNamedBy(choice)) {
				String src = record == null ? null : record.get(p);
				String tbl = table(p);
				String uri = src != null ? src : tbl;
				if (uri == null) {
					unresolved.add(p);
					continue;
				}
				String onRoot = plan.root.get(p);
				if (onRoot != null) {
					if (onRoot.equals(uri)) continue; // declared once, on the root
				} else if (uri.equals(tbl)) {
					plan.putRoot(p, uri, true); // Word's fixed set goes on the root
					continue;
				}
				// on the Choice, with the source's binding, unless that would rebind the Choice's own
				// content, which uses the namespace the prefix otherwise has (the table's, or the root's)
				String clash = null;
				if (tbl != null && !tbl.equals(uri) && usesNamespace(choice, tbl)) clash = tbl;
				else if (onRoot != null && !onRoot.equals(tbl) && usesNamespace(choice, onRoot)) clash = onRoot;
				if (clash != null) {
					log.warn("mc:Choice Requires='" + choice.getRequires() + "': its source bound '" + p + "' to " + uri
							+ ", but its content uses " + clash + " (the prefix's namespace in scope); the content's binding stands");
					continue;
				}
				Map<String, String> decls = plan.onChoice.get(choice);
				if (decls == null) {
					decls = new LinkedHashMap<String, String>();
					plan.onChoice.put(choice, decls);
				}
				decls.put(p, uri);
			}
		}

		for (String p : unresolved) {
			if (plan.root.containsKey(p)) continue;
			boolean declared = false;
			for (Map<String, String> m : plan.onChoice.values()) {
				if (m.containsKey(p)) {
					declared = true;
					break;
				}
			}
			if (!declared) {
				plan.undeclared.add(p);
				log.warn("No mapping for prefix '" + p + "'");
			}
		}
		if (log.isDebugEnabled()) log.debug("mc declarations: " + plan);
		return plan;
	}

	/** The table's binding of a prefix, or null. */
	static String table(String prefix) {
		String uri = NamespacePrefixMappings.getNamespaceURIStatic(prefix);
		return uri == null || uri.isEmpty() ? null : uri;
	}

	/** Every mc:Choice in the tree, in document order, the root included if it is one. */
	private static List<AlternateContent.Choice> choices(Object root) {
		final List<AlternateContent.Choice> out = new ArrayList<AlternateContent.Choice>();
		if (root instanceof AlternateContent.Choice) out.add((AlternateContent.Choice) root);
		new TraversalUtil(root, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				if (o instanceof AlternateContent.Choice) out.add((AlternateContent.Choice) o);
				return null;
			}
		}, McMode.ALL);
		return out;
	}

	// ---- the clash check (D3) ------------------------------------------------------------------

	private static final ConcurrentHashMap<Class<?>, String> ELEMENT_NAMESPACES = new ConcurrentHashMap<Class<?>, String>();

	/** Whether the Choice's content has an element in this namespace (a DOM element's attributes count too). */
	static boolean usesNamespace(AlternateContent.Choice choice, String uri) {
		for (Object o : choice.getAny()) {
			if (usesNamespace(o, uri)) return true;
		}
		return false;
	}

	private static boolean usesNamespace(Object o, String uri) {
		if (o == null) return false;
		if (o instanceof JAXBElement) {
			if (uri.equals(((JAXBElement<?>) o).getName().getNamespaceURI())) return true;
			o = ((JAXBElement<?>) o).getValue();
			if (o == null) return false;
		}
		if (o instanceof Node) return domUsesNamespace((Node) o, uri);
		if (uri.equals(elementNamespace(o.getClass()))) return true;
		List<Object> children = TraversalUtil.getChildrenImpl(o, McMode.ALL);
		if (children != null) {
			for (Object child : children) {
				if (usesNamespace(child, uri)) return true;
			}
		}
		return false;
	}

	private static boolean domUsesNamespace(Node n, String uri) {
		if (n instanceof Element) {
			if (uri.equals(n.getNamespaceURI())) return true;
			NamedNodeMap atts = n.getAttributes();
			for (int i = 0; atts != null && i < atts.getLength(); i++) {
				if (uri.equals(atts.item(i).getNamespaceURI())) return true;
			}
		}
		for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (domUsesNamespace(c, uri)) return true;
		}
		return false;
	}

	/** The namespace a JAXB class's element is in: its @XmlRootElement's, else its @XmlType's, else its package's. */
	static String elementNamespace(Class<?> c) {
		String ns = ELEMENT_NAMESPACES.get(c);
		if (ns != null) return ns;
		ns = "";
		XmlRootElement re = c.getAnnotation(XmlRootElement.class);
		XmlType t = c.getAnnotation(XmlType.class);
		if (re != null && !"##default".equals(re.namespace())) {
			ns = re.namespace();
		} else if (t != null && !"##default".equals(t.namespace())) {
			ns = t.namespace();
		} else if (c.getPackage() != null) {
			XmlSchema s = c.getPackage().getAnnotation(XmlSchema.class);
			if (s != null) ns = s.namespace();
		}
		ELEMENT_NAMESPACES.put(c, ns);
		return ns;
	}

	// ---- the root pairs the mapper pre-declares ------------------------------------------------

	private static final ThreadLocal<Map<String, String>> ROOT_PAIRS = new ThreadLocal<Map<String, String>>();

	/**
	 * Sets the (prefix, URI) pairs the prefix mapper pre-declares on the root for the marshal about
	 * to run on this thread ({@link Plan#rootPairs()}), returning what was there, which the caller
	 * puts back with {@link #restoreRootPairs} when its marshal is done (CR-049's discipline).
	 */
	public static Map<String, String> installRootPairs(Map<String, String> pairs) {
		Map<String, String> previous = ROOT_PAIRS.get();
		if (pairs == null || pairs.isEmpty()) ROOT_PAIRS.remove(); else ROOT_PAIRS.set(pairs);
		return previous;
	}

	/** Puts back what {@link #installRootPairs} returned. */
	public static void restoreRootPairs(Map<String, String> previous) {
		if (previous == null) ROOT_PAIRS.remove(); else ROOT_PAIRS.set(previous);
	}

	/** The pairs installed on this thread; empty where none. */
	static Map<String, String> currentRootPairs() {
		Map<String, String> pairs = ROOT_PAIRS.get();
		return pairs == null ? Collections.<String, String>emptyMap() : pairs;
	}

	// ---- the XMLStreamWriter route -------------------------------------------------------------

	/** One factory for the writers: the JDK's creates a fresh writer per call and is not reconfigured. */
	private static final XMLOutputFactory OUTPUT_FACTORY = XMLOutputFactory.newFactory();

	/** An XMLStreamWriter on an output stream, UTF-8. */
	public static XMLStreamWriter writer(OutputStream os) throws XMLStreamException {
		return OUTPUT_FACTORY.createXMLStreamWriter(os, "UTF-8");
	}

	/** An XMLStreamWriter on a character writer. */
	public static XMLStreamWriter writer(Writer w) throws XMLStreamException {
		return OUTPUT_FACTORY.createXMLStreamWriter(w);
	}

	/** An XMLStreamWriter building into a DOM node (a Document, DocumentFragment or Element). */
	public static XMLStreamWriter writer(Node node) throws XMLStreamException {
		return OUTPUT_FACTORY.createXMLStreamWriter(new DOMResult(node));
	}

	/** The XML declaration JAXB's own stream output writes, which the stream route writes itself. */
	private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

	/**
	 * Marshals {@code o} to the stream through an XMLStreamWriter so that the plan's Choice
	 * declarations are written on their elements ({@link #marshal(Marshaller, Object, Plan, XMLStreamWriter, boolean, boolean, boolean)}).
	 * Unless the marshaller is in fragment mode it writes JAXB's own XML declaration first
	 * (with {@code standalone="yes"}, as the default path's output has) and marshals the rest as a
	 * fragment.  The stream is flushed, not closed.
	 */
	public static void marshal(Marshaller m, Object o, Plan plan, OutputStream os, boolean indent,
			boolean versionComment, boolean isNewPkg) throws JAXBException, XMLStreamException, java.io.IOException {
		if (declareFirst(m)) os.write(XML_DECLARATION.getBytes(java.nio.charset.StandardCharsets.UTF_8));
		XMLStreamWriter xsw = writer(os);
		marshal(m, o, plan, xsw, indent, versionComment, isNewPkg);
		xsw.close();
		os.flush();
	}

	/** As {@link #marshal(Marshaller, Object, Plan, OutputStream, boolean, boolean, boolean)}, to a character writer. */
	public static void marshal(Marshaller m, Object o, Plan plan, Writer w, boolean indent,
			boolean versionComment, boolean isNewPkg) throws JAXBException, XMLStreamException, java.io.IOException {
		if (declareFirst(m)) w.write(XML_DECLARATION);
		XMLStreamWriter xsw = writer(w);
		marshal(m, o, plan, xsw, indent, versionComment, isNewPkg);
		xsw.close();
		w.flush();
	}

	/** As {@link #marshal(Marshaller, Object, Plan, OutputStream, boolean, boolean, boolean)}, into a DOM node (no declaration). */
	public static void marshal(Marshaller m, Object o, Plan plan, Node node, boolean indent,
			boolean versionComment, boolean isNewPkg) throws JAXBException, XMLStreamException {
		XMLStreamWriter xsw = writer(node);
		marshal(m, o, plan, xsw, indent, versionComment, isNewPkg);
		xsw.close();
	}

	/** Whether the marshal wants an XML declaration, which the caller then writes, putting the marshaller in fragment mode. */
	private static boolean declareFirst(Marshaller m) throws JAXBException {
		Object fragment = m.getProperty(Marshaller.JAXB_FRAGMENT);
		if (Boolean.TRUE.equals(fragment)) return false;
		m.setProperty(Marshaller.JAXB_FRAGMENT, true);
		return true;
	}

	/**
	 * Marshals {@code o} through the writer so that the plan's Choice declarations are written on
	 * their elements: the marshaller gets a {@link Docx4jMarshallerListener} that, told of each
	 * Choice before its start tag, hands the wrapper the declarations to put on it.  The wrapper's
	 * queue is written out and the underlying writer flushed; the caller closes the underlying
	 * writer (the overloads on a stream, a character writer and a DOM node do).
	 *
	 * @param indent whether to indent (JAXB's formatted output does not reach an XMLStreamWriter)
	 * @param versionComment whether to write docx4j's version comment at the body, as a part's save does
	 * @param isNewPkg for that comment
	 */
	public static void marshal(Marshaller m, Object o, Plan plan, XMLStreamWriter underlying, boolean indent,
			boolean versionComment, boolean isNewPkg) throws JAXBException, XMLStreamException {
		XMLStreamWriterWrapper xsww = indent ? new XMLStreamWriterWrapperIndenting(null, underlying)
				: new XMLStreamWriterWrapper(null, underlying);
		m.setListener(new Docx4jMarshallerListener(xsww, isNewPkg, versionComment, plan));
		m.marshal(o, xsww);
		xsww.flushQueue();
		underlying.flush();
	}
}
