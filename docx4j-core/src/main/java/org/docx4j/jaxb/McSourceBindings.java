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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.namespace.NamespaceContext;

import org.docx4j.copy.CopyUtils;
import org.docx4j.mce.AlternateContent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The namespace bindings an element's {@code mc} attributes had in the source, kept beside the
 * object from unmarshal to marshal (CR-052).
 *
 * <p>An {@code mc:Ignorable}, a Choice's {@code Requires}, or a Choice's own {@code mc:Ignorable} or
 * {@code mc:MustUnderstand} names namespace prefixes, each identifying a namespace by the
 * declaration in scope where the attribute is.  JAXB keeps the attribute's string, not the
 * declarations, so at unmarshal docx4j records here, for each {@code mc:Choice} and for a part's
 * root, what each named prefix was bound to: the unmarshaller listener does it from the StAX
 * reader's namespace context at the element's end tag, the binder paths from the DOM.  At marshal
 * {@link McDeclarations} reads the record to declare each prefix where it is named (D4 (c)), the
 * source's binding first and docx4j's prefix table ({@link NamespacePrefixMappings}) only where the
 * source gave none (D3).</p>
 *
 * <p>The record is keyed by the object's identity (generated classes do not override
 * {@code equals}), weakly, so it lives as long as the Choice does and follows it when it is moved
 * into another part or package.  A copy gets the record too (D2): docx4j-core registers a
 * {@link CopyUtils.CopyListener} at start-up, which pairs a copied {@code mc:AlternateContent}'s
 * Choices with the original's, and {@code XmlUtils.deepCopy} reports the object it copies.  A copy
 * made by a generated class's typed single-valued {@code AlternateContent} field (a numbering
 * definition's, a chart space's, a spreadsheet drawing anchor's) bypasses that listener; the
 * Choices there carry Office's own prefixes, which the table gives, so nothing is lost.</p>
 *
 * <p>Before 17.3.4's CR-052 the record held a Choice's {@code Requires} bindings only, for text
 * extraction's branch selection (CR-046, {@link McSelection#requiresNamespaces}), and nothing read
 * it at marshal.</p>
 *
 * @since 17.3.4 (CR-052 phase 1)
 */
public final class McSourceBindings {

	private McSourceBindings() {}

	/** The Markup Compatibility namespace. */
	public static final String MCE_NS = "http://schemas.openxmlformats.org/markup-compatibility/2006";

	/** Each recorded element (a Choice or a root), by identity, to its prefix -> URI bindings; a null URI is a prefix the source left undeclared. */
	private static final Map<Object, Map<String, String>> RECORDS =
			Collections.synchronizedMap(new WeakHashMap<Object, Map<String, String>>());

	static {
		CopyUtils.setCopyListener(McSourceBindings::carry);
	}

	/** Makes sure the copy listener is registered; {@link Context} calls it at start-up. */
	static void install() {
		// the static initializer did the work
	}

	// ---- recording ----------------------------------------------------------------------------

	/**
	 * Records what the prefixes this Choice's {@code Requires}, {@code mc:Ignorable} and
	 * {@code mc:MustUnderstand} name resolve to, through the declarations in force at the element.
	 * The unmarshaller listener calls it while its reader stands at the element's end tag; the
	 * binder paths with the DOM element's context.  A null context, or a Choice naming no prefix,
	 * records nothing.
	 */
	public static void record(AlternateContent.Choice choice, NamespaceContext context) {
		if (choice == null || context == null) return;
		List<String> prefixes = prefixesNamedBy(choice);
		if (prefixes.isEmpty()) return;
		RECORDS.put(choice, resolve(prefixes, context));
	}

	/**
	 * Records from a StAX source, as the unmarshaller listeners call it for each object completed:
	 * a Choice's bindings where the reader stands at the {@code mc:Choice} end tag, and a root's
	 * (the object with no parent) from the declarations its start tag made, which the wrapped
	 * reader keeps ({@link McBranchSdt#rootNamespaces}).  Returns false for a Choice it could not
	 * record (the reader elsewhere: a Choice bound from a DOM element once the read was over), which
	 * the listener then keeps for {@link #recordFromDom}.
	 */
	public static boolean record(Object target, Object parent, javax.xml.stream.XMLStreamReader reader) {
		if (reader == null || target == null) return false;
		if (target instanceof AlternateContent.Choice) {
			if (reader.isEndElement() && "Choice".equals(reader.getLocalName()) && MCE_NS.equals(reader.getNamespaceURI())) {
				record((AlternateContent.Choice) target, reader.getNamespaceContext());
				return true;
			}
			return false; // a Choice bound from a DOM element after the read (a control in a branch): the caller records it from that element
		} else if (parent == null) {
			// the root: from the declarations its start tag made, which the wrapped reader kept (the
			// RI fires this at the root's end tag, MOXy at the end of the document, measured 2026-10-21)
			Map<String, String> declared = McBranchSdt.rootNamespaces(reader);
			if (declared != null) {
				recordRoot(target, new MapNamespaceContext(declared));
			} else if (reader.isEndElement()) {
				recordRoot(target, reader.getNamespaceContext());
			}
		}
		return true;
	}

	/** A NamespaceContext over a prefix-to-URI map. */
	private static final class MapNamespaceContext implements NamespaceContext {
		private final Map<String, String> declared;

		MapNamespaceContext(Map<String, String> declared) {
			this.declared = declared;
		}

		@Override
		public String getNamespaceURI(String prefix) {
			String uri = declared.get(prefix == null ? "" : prefix);
			return uri == null ? javax.xml.XMLConstants.NULL_NS_URI : uri;
		}

		@Override
		public String getPrefix(String namespaceURI) {
			for (Map.Entry<String, String> e : declared.entrySet()) {
				if (namespaceURI.equals(e.getValue())) return e.getKey();
			}
			return null;
		}

		@Override
		public java.util.Iterator<String> getPrefixes(String namespaceURI) {
			List<String> out = new ArrayList<String>();
			for (Map.Entry<String, String> e : declared.entrySet()) {
				if (namespaceURI.equals(e.getValue())) out.add(e.getKey());
			}
			return out.iterator();
		}
	}

	/**
	 * Records what the prefixes a root's {@code mc:Ignorable} names resolve to, through the
	 * declarations in force at the root element (a {@code JAXBElement} is unwrapped).  A root
	 * without the attribute, or a null context, records nothing.
	 */
	public static void recordRoot(Object root, NamespaceContext context) {
		root = org.docx4j.XmlUtils.unwrap(root);
		if (root == null || context == null) return;
		List<String> prefixes = prefixes(NamespacePrefixMapperUtils.ignorableOf(root));
		if (prefixes.isEmpty()) return;
		RECORDS.put(root, resolve(prefixes, context));
	}

	/**
	 * Records, for a tree unmarshalled from a DOM, what the DOM declares: the root's
	 * {@code mc:Ignorable} prefixes from the document element, and each Choice's prefixes from
	 * its {@code mc:Choice} element.  The Choices are paired with the DOM's elements in the order
	 * the unmarshaller completed them (document order of end tags); the pairing is checked by each
	 * pair's {@code Requires}, and abandoned, recording no Choice, where it does not hold (a
	 * {@code mc:Choice} inside content JAXB kept as a DOM element has no object).
	 *
	 * @param root the unmarshalled root (a {@code JAXBElement} is unwrapped)
	 * @param element the DOM element it was unmarshalled from
	 * @param choices the Choices the listener saw, in order; may be empty
	 */
	public static void recordFromDom(Object root, Element element, List<AlternateContent.Choice> choices) {
		if (element == null) return;
		recordRoot(root, new DomNamespaceContext(element));
		if (choices == null || choices.isEmpty()) return;
		List<Element> elements = new ArrayList<Element>();
		choiceElementsInEndOrder(element, elements);
		if (elements.size() != choices.size()) return;
		for (int i = 0; i < choices.size(); i++) {
			String requires = choices.get(i).getRequires();
			String domRequires = elements.get(i).hasAttribute("Requires") ? elements.get(i).getAttribute("Requires") : null;
			if (requires == null ? domRequires != null : !requires.equals(domRequires)) return;
		}
		for (int i = 0; i < choices.size(); i++) {
			record(choices.get(i), new DomNamespaceContext(elements.get(i)));
		}
	}

	/**
	 * The {@code mc:Choice} elements under (not including) {@code el}, in document order of their
	 * end tags, which is the order the unmarshaller completes them.  A control renamed into
	 * {@link McBranchSdt#NAMESPACE}, which JAXB keeps as a DOM element to bind afterwards, is not
	 * entered: its Choices are bound, and recorded, when it is ({@link McBranchSdt#bind}).
	 */
	private static void choiceElementsInEndOrder(Element el, List<Element> out) {
		for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (!(n instanceof Element) || McBranchSdt.NAMESPACE.equals(n.getNamespaceURI())) continue;
			choiceElementsInEndOrder((Element) n, out);
			if (MCE_NS.equals(n.getNamespaceURI()) && "Choice".equals(n.getLocalName())) out.add((Element) n);
		}
	}

	/**
	 * Sets an element's bindings outright: for code that builds an {@code mc:Choice} naming a
	 * prefix docx4j's table does not know, so that a save declares it ({@code prefix -> URI}).
	 * Null or empty removes the record.
	 */
	public static void set(Object element, Map<String, String> bindings) {
		if (element == null) return;
		if (bindings == null || bindings.isEmpty()) {
			RECORDS.remove(element);
		} else {
			RECORDS.put(element, Collections.unmodifiableMap(new LinkedHashMap<String, String>(bindings)));
		}
	}

	// ---- reading -------------------------------------------------------------------------------

	/**
	 * The recorded bindings of a Choice or a root: each prefix its {@code mc} attributes name to
	 * the URI it resolved to in the source (null for one the source left undeclared), in attribute
	 * order; unmodifiable.  Null where nothing was recorded (an element built in code, or
	 * unmarshalled where no declarations were to hand).
	 */
	public static Map<String, String> of(Object element) {
		return element == null ? null : RECORDS.get(element);
	}

	/** The prefixes a Choice's {@code Requires}, {@code mc:Ignorable} and {@code mc:MustUnderstand} name, in that order, each once. */
	public static List<String> prefixesNamedBy(AlternateContent.Choice choice) {
		LinkedHashSet<String> all = new LinkedHashSet<String>();
		all.addAll(prefixes(choice.getRequires()));
		all.addAll(prefixes(choice.getIgnorable()));
		all.addAll(prefixes(choice.getMustUnderstand()));
		return new ArrayList<String>(all);
	}

	/** The whitespace-separated prefixes of an {@code mc} attribute value; empty for null or blank. */
	public static List<String> prefixes(String attributeValue) {
		if (attributeValue == null || attributeValue.trim().isEmpty()) return Collections.emptyList();
		List<String> out = new ArrayList<String>();
		for (String p : attributeValue.trim().split("\\s+")) {
			if (!p.isEmpty() && !out.contains(p)) out.add(p);
		}
		return out;
	}

	// ---- copies (D2) ---------------------------------------------------------------------------

	/**
	 * Gives a copied {@code mc:AlternateContent} or Choice its original's record: the
	 * {@link CopyUtils.CopyListener}, told of every member of an untyped list the generated code
	 * copies, so it touches the record for those two kinds only.  An {@code mc:AlternateContent}'s
	 * Choices are paired by position, since the generated copy makes them directly.
	 */
	public static void carry(Object source, Object copy) {
		if (source instanceof AlternateContent) {
			if (!(copy instanceof AlternateContent) || source == copy) return;
			List<AlternateContent.Choice> from = ((AlternateContent) source).getChoice();
			List<AlternateContent.Choice> to = ((AlternateContent) copy).getChoice();
			int n = Math.min(from.size(), to.size());
			for (int i = 0; i < n; i++) carryRecord(from.get(i), to.get(i));
		} else if (source instanceof AlternateContent.Choice) {
			if (copy != null && source != copy) carryRecord(source, copy);
		}
	}

	/**
	 * As {@link #carry}, for the one object {@code XmlUtils.deepCopy} is given, which may also be a
	 * part's root (its {@code mc:Ignorable} bindings follow it).
	 */
	public static void carryTopLevel(Object source, Object copy) {
		if (source == null || copy == null || source == copy) return;
		if (source instanceof AlternateContent || source instanceof AlternateContent.Choice) {
			carry(source, copy);
		} else {
			carryRecord(source, copy);
		}
	}

	private static void carryRecord(Object source, Object copy) {
		Map<String, String> record = RECORDS.get(source);
		if (record != null) RECORDS.put(copy, record); // unmodifiable: shared
	}

	// ---- helpers -------------------------------------------------------------------------------

	private static Map<String, String> resolve(List<String> prefixes, NamespaceContext context) {
		LinkedHashMap<String, String> bindings = new LinkedHashMap<String, String>();
		for (String prefix : prefixes) {
			String uri = context.getNamespaceURI(prefix);
			bindings.put(prefix, uri == null || uri.isEmpty() ? null : uri);
		}
		return Collections.unmodifiableMap(bindings);
	}

	/** The namespace declarations in scope at a DOM node, as a NamespaceContext. */
	public static final class DomNamespaceContext implements NamespaceContext {
		private final Node node;

		public DomNamespaceContext(Node node) {
			this.node = node;
		}

		@Override
		public String getNamespaceURI(String prefix) {
			String uri = node.lookupNamespaceURI(prefix == null || prefix.isEmpty() ? null : prefix);
			return uri == null ? javax.xml.XMLConstants.NULL_NS_URI : uri;
		}

		@Override
		public String getPrefix(String namespaceURI) {
			return node.lookupPrefix(namespaceURI);
		}

		@Override
		public java.util.Iterator<String> getPrefixes(String namespaceURI) {
			String prefix = node.lookupPrefix(namespaceURI);
			return (prefix == null ? Collections.<String>emptyList() : Collections.singletonList(prefix)).iterator();
		}
	}

}
