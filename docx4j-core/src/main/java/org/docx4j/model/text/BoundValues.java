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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.docx4j.XmlUtils;
import org.docx4j.model.datastorage.BindingHandler;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.utils.XPathFactoryUtil;
import org.docx4j.wml.CTDataBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A data-bound control's bound value (WD REQ-509): {@code BindingTraverser}'s rule, read-only. The
 * binding's XPath is evaluated, with its own prefix mappings, against the custom XML part its
 * {@code w:storeItemID} names (or the core or application properties, by their well-known store item
 * ids); the value is the string value of the first node selected. A binding resolves when the part
 * exists and the XPath selects a node; one that does not resolve gives null, and the control's
 * surface is read instead.
 */
final class BoundValues {

	private static final Logger log = LoggerFactory.getLogger(BoundValues.class);

	private final WordprocessingMLPackage pkg;
	/** Each part's document, by store item id; a properties part marshalled once. */
	private final Map<String, org.w3c.dom.Document> documents = new HashMap<String, org.w3c.dom.Document>();

	BoundValues(WordprocessingMLPackage pkg) {
		this.pkg = pkg;
	}

	/** The bound value, or null where the binding doesn't resolve. */
	String value(CTDataBinding binding) {
		if (pkg == null || binding == null || binding.getXpath() == null || binding.getStoreItemID() == null) return null;
		org.w3c.dom.Document doc = document(binding.getStoreItemID());
		if (doc == null) return null;
		try {
			XPath xpath = XPathFactoryUtil.newXPath();
			xpath.setNamespaceContext(new Mappings(binding.getPrefixMappings()));
			NodeList nodes;
			synchronized (doc) {
				nodes = (NodeList) xpath.evaluate(binding.getXpath(), doc, XPathConstants.NODESET);
			}
			if (nodes == null || nodes.getLength() == 0) return null;
			Node n = nodes.item(0);
			return n instanceof Attr ? ((Attr) n).getValue() : n.getTextContent();
		} catch (Exception e) {
			log.debug("The binding " + binding.getXpath() + " doesn't resolve: " + e.getMessage());
			return null;
		}
	}

	private org.w3c.dom.Document document(String storeItemId) {
		String key = storeItemId.toUpperCase(java.util.Locale.ROOT);
		if (documents.containsKey(key)) return documents.get(key);
		org.w3c.dom.Document doc = null;
		try {
			if (key.equals(BindingHandler.CORE_PROPERTIES_STOREITEMID)) {
				doc = marshal(pkg.getDocPropsCorePart());
			} else if (key.equals(BindingHandler.EXTENDED_PROPERTIES_STOREITEMID)) {
				doc = marshal(pkg.getDocPropsExtendedPart());
			} else if (pkg.getCustomXmlDataStorageParts() != null) {
				CustomXmlPart part = pkg.getCustomXmlDataStorageParts().get(storeItemId.toLowerCase(java.util.Locale.ROOT));
				if (part instanceof CustomXmlDataStoragePart && ((CustomXmlDataStoragePart) part).getData() != null) {
					doc = ((CustomXmlDataStoragePart) part).getData().getDocument();
				} else if (part instanceof JaxbXmlPart) {
					// a part docx4j binds (the cover-page properties, DocPropsCoverPagePart; OpenDoPE's own parts):
					// marshalled, as the core and extended properties are. Until 17.3.4 it gave null, so a
					// control bound to a cover-page property showed its stored surface where Word shows the
					// value (docx4j-portfolio's finding on its bind-props probe, 2026-10-22).
					doc = marshal((JaxbXmlPart<?>) part);
				}
			}
		} catch (Exception e) {
			log.debug("No document for the store item " + storeItemId + ": " + e.getMessage());
		}
		documents.put(key, doc);
		return doc;
	}

	private static org.w3c.dom.Document marshal(JaxbXmlPart<?> part) {
		if (part == null || part.getJaxbElement() == null) return null;
		return XmlUtils.marshaltoW3CDomDocument(part.getJaxbElement(), part.getJAXBContext());
	}

	/** A binding's {@code w:prefixMappings}: {@code xmlns:p='uri'}, whitespace apart, for its XPath only. */
	static final class Mappings implements NamespaceContext {
		private final Map<String, String> uris = new HashMap<String, String>();

		Mappings(String prefixMappings) {
			if (prefixMappings == null) return;
			java.util.regex.Matcher m = java.util.regex.Pattern.compile("xmlns:([^=\\s]+)\\s*=\\s*(['\"])(.*?)\\2").matcher(prefixMappings);
			while (m.find()) uris.put(m.group(1), m.group(3));
		}

		@Override
		public String getNamespaceURI(String prefix) {
			if (prefix == null) throw new IllegalArgumentException();
			if (XMLConstants.XML_NS_PREFIX.equals(prefix)) return XMLConstants.XML_NS_URI;
			String uri = uris.get(prefix);
			return uri == null ? XMLConstants.NULL_NS_URI : uri;
		}

		@Override
		public String getPrefix(String namespaceURI) {
			for (Map.Entry<String, String> e : uris.entrySet()) if (e.getValue().equals(namespaceURI)) return e.getKey();
			return null;
		}

		@Override
		public Iterator<String> getPrefixes(String namespaceURI) {
			String p = getPrefix(namespaceURI);
			return p == null ? java.util.Collections.<String>emptyIterator() : java.util.Collections.singleton(p).iterator();
		}
	}
}
