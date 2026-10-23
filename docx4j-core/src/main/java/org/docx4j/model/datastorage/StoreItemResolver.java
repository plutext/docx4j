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
package org.docx4j.model.datastorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import org.docx4j.Docx4jProperties;
import org.docx4j.jaxb.NamespacePrefixMappings;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * The custom XML part a binding is evaluated against: the one its {@code w:storeItemID} names, or,
 * where that names no part, the one its XPath fits, as Word repairs such a binding (CR-058 D4,
 * measured in Word 365 on docx4j-portfolio's bind-plain probe and its two-parts and no-part
 * variants, 22 and 23 October 2026: Word showed the value and its save rewrote the control's
 * {@code w:storeItemID} to the part's). The rule: the parts whose root element's namespace and local
 * name match the XPath's first step, read with the binding's own prefix mappings; one such part is
 * taken; of several, the first in part-name order whose XPath selects a node, else the first that
 * fits (Word took the first on the two-parts probe); none, and the binding is unresolved, which the
 * generator leaves as it is and the text model reads as its surface. A stale ID is resolved once per
 * document, and logged once. Until 17.3.5 one stale ID failed the whole bind on the NonXSLT
 * traverser and silently emptied the control on the XSLT one; {@link #STRICT_PROPERTY} restores the
 * failure for a deployment that wants a bad template rejected.
 *
 * @since 17.3.5
 */
public final class StoreItemResolver {

	private static final Logger log = LoggerFactory.getLogger(StoreItemResolver.class);

	/** {@code true}: a store item ID naming no part fails the bind with an {@link InputIntegrityException}, on both traversers. */
	public static final String STRICT_PROPERTY = "docx4j.model.datastorage.BindingHandler.strictStoreItemIds";

	private StoreItemResolver() {}

	/** Per document (its parts map): the stale IDs seen, lower-cased, with their XPath, to the key of the part resolved, or null for none. */
	private static final Map<Map<?, ?>, Map<String, String>> RESOLVED =
			Collections.synchronizedMap(new WeakHashMap<Map<?, ?>, Map<String, String>>());

	/** The strict failure raised inside an XSLT extension function, which Xalan's error listener would otherwise swallow. */
	private static final ThreadLocal<InputIntegrityException> STRICT_FAILURE = new ThreadLocal<InputIntegrityException>();

	public static boolean isStrict() {
		return Docx4jProperties.getProperty(STRICT_PROPERTY, false);
	}

	/**
	 * The part for a binding: named by its store item ID, else the one its XPath fits.
	 *
	 * @return the part, or null where none is named and none fits
	 * @throws InputIntegrityException where none is named and {@link #STRICT_PROPERTY} is true, whether or not one fits
	 */
	public static CustomXmlPart resolve(Map<String, ? extends CustomXmlPart> parts, String storeItemId, String xpath, String prefixMappings) {
		return resolve(parts, storeItemId, xpath, prefixMappings, isStrict());
	}

	/**
	 * As {@link #resolve(Map, String, String, String)}, strictness given (the text model passes false:
	 * it reads and never fails a document).
	 */
	public static CustomXmlPart resolve(Map<String, ? extends CustomXmlPart> parts, String storeItemId, String xpath, String prefixMappings, boolean strict) {
		if (parts == null || storeItemId == null) return null;
		String key = storeItemId.toLowerCase(Locale.ROOT);
		CustomXmlPart named = parts.get(key);
		if (named != null) return named;
		if (strict) {
			// the failure of old, whether or not a part fits: a bad template is to be rejected
			InputIntegrityException e = new InputIntegrityException("Couldn't locate part by storeItemId " + storeItemId
					+ " (" + STRICT_PROPERTY + "=true)");
			STRICT_FAILURE.set(e);
			throw e;
		}
		String resolvedKey = resolvedKey(parts, key, storeItemId, xpath, prefixMappings);
		return resolvedKey == null ? null : parts.get(resolvedKey);
	}

	/** True where the binding resolves to a part (named, or repaired); the XSLT's guard, which leaves an unresolved control as it is. */
	public static boolean resolvable(Map<String, ? extends CustomXmlPart> parts, String storeItemId, String xpath, String prefixMappings) {
		if (storeItemId == null || storeItemId.isEmpty()) return true; // not this resolver's business (od:xpath bindings, properties)
		String upper = storeItemId.toUpperCase(Locale.ROOT);
		if (upper.equals(BindingHandler.CORE_PROPERTIES_STOREITEMID) || upper.equals(BindingHandler.EXTENDED_PROPERTIES_STOREITEMID)) return true;
		return resolve(parts, storeItemId, xpath, prefixMappings) != null;
	}

	/**
	 * The store item ID the binding should carry: the part's own where the binding's names no part and
	 * one fits (what Word's save writes back, D5), else the binding's as it is. Null and empty in,
	 * the same out.
	 */
	public static String resolvedStoreItemId(Map<String, ? extends CustomXmlPart> parts, String storeItemId, String xpath, String prefixMappings) {
		if (parts == null || storeItemId == null || storeItemId.isEmpty()) return storeItemId;
		String key = storeItemId.toLowerCase(Locale.ROOT);
		if (parts.containsKey(key)) return storeItemId;
		String resolvedKey = resolvedKey(parts, key, storeItemId, xpath, prefixMappings);
		if (resolvedKey == null) return storeItemId;
		CustomXmlPart part = parts.get(resolvedKey);
		String id = part == null ? null : part.getItemId();
		if (id == null || id.isEmpty()) id = resolvedKey;
		return id.toUpperCase(Locale.ROOT);
	}

	/** Whether the strict failure was raised on this thread since the last {@link #clearStrictFailure()}; the XSLT traverser rethrows it after its transform. */
	public static InputIntegrityException strictFailure() {
		return STRICT_FAILURE.get();
	}

	public static void clearStrictFailure() {
		STRICT_FAILURE.remove();
	}

	// --- the repair ---------------------------------------------------------------------------------

	private static String resolvedKey(Map<String, ? extends CustomXmlPart> parts, String key, String storeItemId, String xpath, String prefixMappings) {
		Map<String, String> memo;
		synchronized (RESOLVED) {
			memo = RESOLVED.get(parts);
			if (memo == null) {
				memo = Collections.synchronizedMap(new HashMap<String, String>());
				RESOLVED.put(parts, memo);
			}
		}
		String memoKey = key + "|" + xpath; // one stale ID can fit different parts for different XPaths (the two-parts probe)
		if (memo.containsKey(memoKey)) return memo.get(memoKey);
		String resolved = repair(parts, storeItemId, xpath, prefixMappings);
		memo.put(memoKey, resolved);
		return resolved;
	}

	private static String repair(Map<String, ? extends CustomXmlPart> parts, String storeItemId, String xpath, String prefixMappings) {
		String[] step = firstStep(xpath, prefixMappings);
		List<Map.Entry<String, ? extends CustomXmlPart>> fitting = new ArrayList<Map.Entry<String, ? extends CustomXmlPart>>();
		for (Map.Entry<String, ? extends CustomXmlPart> e : parts.entrySet()) {
			if (!(e.getValue() instanceof CustomXmlDataStoragePart)) continue;
			CustomXmlDataStoragePart part = (CustomXmlDataStoragePart) e.getValue();
			Element root;
			try {
				if (part.getData() == null || part.getData().getDocument() == null) continue;
				root = part.getData().getDocument().getDocumentElement();
			} catch (Exception unreadable) {
				log.debug(partName(part) + ": " + unreadable.getMessage());
				continue;
			}
			if (root == null) continue;
			if (step != null) {
				String ns = root.getNamespaceURI() == null ? "" : root.getNamespaceURI();
				String local = root.getLocalName() == null ? root.getNodeName() : root.getLocalName();
				if (!step[0].equals(ns) || !step[1].equals(local)) continue;
			}
			fitting.add(e);
		}
		if (fitting.isEmpty()) {
			log.warn("Store item " + storeItemId + " names no custom XML part, and none fits " + xpath + ": the binding is unresolved");
			return null;
		}
		Collections.sort(fitting, (a, b) -> partName(a.getValue()).compareTo(partName(b.getValue())));
		if (fitting.size() == 1) {
			log.info("Store item " + storeItemId + " names no custom XML part; " + partName(fitting.get(0).getValue())
					+ " fits " + xpath + " and is used, as Word repairs it");
			return fitting.get(0).getKey();
		}
		for (Map.Entry<String, ? extends CustomXmlPart> e : fitting) {
			if (selectsANode((CustomXmlDataStoragePart) e.getValue(), xpath, prefixMappings)) {
				log.warn("Store item " + storeItemId + " names no custom XML part; " + fitting.size() + " fit " + xpath
						+ ", and " + partName(e.getValue()) + ", the first whose XPath selects a node, is used, as Word repairs it");
				return e.getKey();
			}
		}
		log.warn("Store item " + storeItemId + " names no custom XML part; " + fitting.size() + " fit " + xpath
				+ " and none selects a node; " + partName(fitting.get(0).getValue()) + ", the first, is used");
		return fitting.get(0).getKey();
	}

	private static String partName(CustomXmlPart part) {
		try {
			return ((org.docx4j.openpackaging.parts.Part) part).getPartName().getName();
		} catch (Exception e) {
			return String.valueOf(part);
		}
	}

	private static boolean selectsANode(CustomXmlDataStoragePart part, String xpath, String prefixMappings) {
		try {
			List<org.w3c.dom.Node> nodes = part.xpathGetNodes(xpath, prefixMappings);
			return nodes != null && !nodes.isEmpty();
		} catch (Exception e) {
			log.debug(xpath + " on " + partName(part) + ": " + e.getMessage());
			return false;
		}
	}

	/**
	 * The XPath's first step as {namespace URI, local name}, with the binding's prefix mappings
	 * ({@code /p:root[1]/p:b7[1]} with {@code xmlns:p='urn:x'} is {@code {"urn:x", "root"}}); null where
	 * the XPath does not start with a named step, in which case every part fits.
	 */
	static String[] firstStep(String xpath, String prefixMappings) {
		if (xpath == null) return null;
		String s = xpath.trim();
		if (s.startsWith("//")) return null;
		if (s.startsWith("/")) s = s.substring(1);
		int end = s.length();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '/' || c == '[' || c == '(' || Character.isWhitespace(c)) {
				end = i;
				break;
			}
		}
		String step = s.substring(0, end);
		if (step.isEmpty() || step.equals("*") || step.equals(".") || step.equals("..") || step.contains("::") || step.startsWith("@")) return null;
		String prefix = "";
		String local = step;
		int colon = step.indexOf(':');
		if (colon >= 0) {
			prefix = step.substring(0, colon);
			local = step.substring(colon + 1);
		}
		if (local.isEmpty() || local.equals("*")) return null;
		String ns = "";
		if (!prefix.isEmpty()) {
			ns = NamespacePrefixMappings.parse(prefixMappings).get(prefix);
			if (ns == null) ns = new NamespacePrefixMappings().getNamespaceURI(prefix); // docx4j's table, as the XPath would be read
			if (ns == null) ns = "";
		}
		return new String[] { ns, local };
	}
}
