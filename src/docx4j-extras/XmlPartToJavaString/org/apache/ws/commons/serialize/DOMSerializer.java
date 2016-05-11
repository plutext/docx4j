/*
 * Copyright 2003, 2004  The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ws.commons.serialize;

import javax.xml.XMLConstants;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/** Serializes a DOM node into a stream of SAX events.
 */
public class DOMSerializer {
	private boolean namespaceDeclarationAttribute;
	private boolean parentsNamespaceDeclarationDisabled;
	private boolean startingDocument = true;
	
	/** Sets whether XML namespace declarations are being serialized as
	 * attributes or as SAX events (default).
	 * @param pXmlDeclarationAttribute True, if a namespace declaration
	 * is being transmitted as an XML attribute. False otherwise.
	 */
	public void setNamespaceDeclarationAttribute(boolean pXmlDeclarationAttribute) {
		namespaceDeclarationAttribute = pXmlDeclarationAttribute;
	}
	
	/** Returns whether XML declarations are being serialized as
	 * attributes or as SAX events (default).
	 * @return True, if a namespace declaration
	 * is being transmitted as an XML attribute. False otherwise.
	 */
	public boolean isNamespaceDeclarationAttribute() {
		return namespaceDeclarationAttribute;
	}
	
	/** Returns whether XML declarations present in the parent nodes
	 * are being serialized (default) or not. This option takes effect
	 * only if the namespace declarations are sent as events. In other
	 * words, if the <code>namespaceDeclarationAttribute</code>
	 * properts is false.
	 * @param pParentsXmlDeclarationDisabled True, if namespace
	 * declarations of the parent nodes are disabled, false otherwise.
	 */
	public void setParentsNamespaceDeclarationDisabled(boolean pParentsXmlDeclarationDisabled) {
		parentsNamespaceDeclarationDisabled = pParentsXmlDeclarationDisabled;
	}
	
	/** Sets whether XML declarations present in the parent nodes
	 * are being serialized (default) or not. This option takes effect
	 * only if the namespace declarations are sent as events. In other
	 * words, if the <code>namespaceDeclarationAttribute</code>
	 * properts is false.
	 * @return True, if namespace declarations of the parent nodes are
	 * disabled, false otherwise.
	 */
	public boolean isParentsNamespaceDeclarationDisabled() {
		return parentsNamespaceDeclarationDisabled;
	}

	/** Returns, whether <code>startDocument</code> and
	 * <code>endDocument</code> events are generated for
	 * document nodes.
	 * @return True (default), if <code>startDocument</code> and
	 * <code>endDocument</code> events are being generated.
	 * False otherwise.
	 */
	public boolean isStartingDocument() {
		return startingDocument;
	}

	/** Sets, whether <code>startDocument</code> and
	 * <code>endDocument</code> events are generated for
	 * document nodes.
	 * @param pStartingDocument True (default), if
	 * <code>startDocument</code> and
	 * <code>endDocument</code> events are being generated.
	 * False otherwise.
	 */
	public void setStartingDocument(boolean pStartingDocument) {
		startingDocument = pStartingDocument;
	}

	/** Serializes the childs of <code>pNode</code>.
	 * @param pNode The parent node, whose childs are being serialized.
	 * @param pHandler The target handler.
	 * @throws SAXException The target handler reported an error.
	 */
	protected void doSerializeChilds(Node pNode, ContentHandler pHandler)
			throws SAXException {
		for (Node child = pNode.getFirstChild();  child != null;
		child = child.getNextSibling()) {
			doSerialize(child, pHandler);
		}
	}

	/** Initially creates startPrefixMapping events for the nodes parents. This
	 * is invoked only, if {@link #isNamespaceDeclarationAttribute()},
	 * and {@link #isParentsNamespaceDeclarationDisabled()} are false.
	 * @param pNode The node, for which namespace declarations are being
	 * created.
	 * @param pHandler The target handler.
	 * @throws SAXException The target handler reported an error.
	 */
	private void parentsStartPrefixMappingEvents(Node pNode, ContentHandler pHandler)
			throws SAXException {
		if (pNode != null) {
			parentsStartPrefixMappingEvents(pNode.getParentNode(), pHandler);
			if (pNode.getNodeType() == Node.ELEMENT_NODE) {
				startPrefixMappingEvents(pNode, pHandler);
			}
		}
	}

	/** Finally creates endPrefixMapping events for the nodes parents. This
	 * is invoked only, if {@link #isNamespaceDeclarationAttribute()},
	 * and {@link #isParentsNamespaceDeclarationDisabled()} are false.
	 * @param pNode The node, for which namespace declarations are being
	 * created.
	 * @param pHandler The target handler.
	 * @throws SAXException The target handler reported an error.
	 */
	private void parentsEndPrefixMappingEvents(Node pNode, ContentHandler pHandler)
			throws SAXException {
		if (pNode != null) {
			if (pNode.getNodeType() == Node.ELEMENT_NODE) {
				endPrefixMappingEvents(pNode, pHandler);
			}
			parentsEndPrefixMappingEvents(pNode.getParentNode(), pHandler);
		}
	}

	/** Creates startPrefixMapping events for the node <code>pNode</code>.
	 * @param pNode The node being serialized.
	 * @param pHandler The target handler.
	 * @throws SAXException The target handler reported an error.
	 */
	private void startPrefixMappingEvents(Node pNode, ContentHandler pHandler)
			throws SAXException {
		NamedNodeMap nnm = pNode.getAttributes();
		if (nnm != null) {
			for (int i = 0;  i < nnm.getLength();  i++) {
				Node attr = nnm.item(i);
				if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attr.getNamespaceURI())) {
					String prefix;
					if (XMLConstants.XMLNS_ATTRIBUTE.equals(attr.getPrefix())) {
						prefix = attr.getLocalName();
					} else if (XMLConstants.XMLNS_ATTRIBUTE.equals(attr.getNodeName())) {
						prefix = "";
					} else {
						throw new IllegalStateException("Unable to parse namespace declaration: " + attr.getNodeName());
					}
					String uri = attr.getNodeValue();
					if (uri == null) {
						uri = "";
					}
					pHandler.startPrefixMapping(prefix, uri);
				}
			}
		}
	}
	
	/** Creates endPrefixMapping events for the node <code>pNode</code>.
	 * @param pNode The node being serialized.
	 * @param pHandler The target handler.
	 * @throws SAXException The target handler reported an error.
	 */
	private void endPrefixMappingEvents(Node pNode, ContentHandler pHandler)
			throws SAXException {
		NamedNodeMap nnm = pNode.getAttributes();
		if (nnm != null) {
			for (int i = nnm.getLength()-1;  i >= 0;  i--) {
				Node attr = nnm.item(i);
				if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attr.getNamespaceURI())) {
					String prefix = attr.getLocalName();
					pHandler.endPrefixMapping(prefix);
				}
			}
		}
	}

	private void characters(ContentHandler pHandler, String pValue, boolean pCdata)
			throws SAXException {
		LexicalHandler lh;
		if (pCdata) {
			lh = (pHandler instanceof LexicalHandler) ? (LexicalHandler) pHandler : null;
		} else {
			lh = null;
		}
		if (lh != null) {
			lh.startCDATA();
		}
		pHandler.characters(pValue.toCharArray(), 0, pValue.length());
		if (lh != null) {
			lh.endCDATA();
		}
	}

	/** Converts the given node <code>pNode</code> into a
	 * stream of SAX events, which are fired into the
	 * content handler <code>pHandler</code>.
	 * @param pNode The node being serialized.
	 * @param pHandler The target handler.
	 * @throws SAXException The target handler reported an error.
	 */
	public void serialize(Node pNode, ContentHandler pHandler)
			throws SAXException {
		if (!isNamespaceDeclarationAttribute()  &&
				!isParentsNamespaceDeclarationDisabled()) {
			parentsStartPrefixMappingEvents(pNode.getParentNode(), pHandler);
		}
		doSerialize(pNode, pHandler);
		if (!isNamespaceDeclarationAttribute()  &&
				!isParentsNamespaceDeclarationDisabled()) {
			parentsEndPrefixMappingEvents(pNode.getParentNode(), pHandler);
		}
	}

	/** Converts the given node <code>pNode</code> into a
	 * stream of SAX events, which are fired into the
	 * content handler <code>pHandler</code>. Unlike
	 * {@link #serialize(Node, ContentHandler)}, this method
	 * doesn't call
	 * {@link #parentsStartPrefixMappingEvents(Node, ContentHandler)},
	 * and
	 * {@link #parentsEndPrefixMappingEvents(Node, ContentHandler)}.
	 * @param pNode The node being serialized.
	 * @param pHandler The target handler.
	 * @throws SAXException The target handler reported an error.
	 */
	protected void doSerialize(Node pNode, ContentHandler pHandler)
			throws SAXException {
		switch (pNode.getNodeType()) {
			case Node.DOCUMENT_NODE:
				boolean startDocumentEvent = isStartingDocument();
				if (startDocumentEvent) {
					pHandler.startDocument();
				}
				doSerializeChilds(pNode, pHandler);
				if (startDocumentEvent) {
					pHandler.endDocument();
				}
				break;
			case Node.DOCUMENT_FRAGMENT_NODE:
				doSerializeChilds(pNode, pHandler);
				break;
			case Node.ELEMENT_NODE:
				AttributesImpl attr = new AttributesImpl();
				boolean isNamespaceDeclarationAttribute = isNamespaceDeclarationAttribute();
				if (!isNamespaceDeclarationAttribute) {
					startPrefixMappingEvents(pNode, pHandler);
				}
				NamedNodeMap nnm = pNode.getAttributes();
				if (nnm != null) {
					for (int i = 0;  i < nnm.getLength();  i++) {
						Node a = nnm.item(i);
						if (isNamespaceDeclarationAttribute  ||
								!XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(a.getNamespaceURI())) {
							String aUri = a.getNamespaceURI();
							String aLocalName = a.getLocalName();
							String aNodeName = a.getNodeName();
							if (aLocalName == null) {
								if (aUri == null  ||  aUri.length() == 0) {
									aLocalName = aNodeName;
								} else {
									throw new IllegalStateException("aLocalName is null");
								}
							}
							//	uri The Namespace URI, or the empty string if none is available or Namespace processing is not being performed.
							//	localName The local name, or the empty string if Namespace processing is not being performed.
							//	qName The qualified (prefixed) name, or the empty string if qualified names are not available.
							//	type The attribute type as a string.
							//	value The attribute value							
							attr.addAttribute(aUri == null ? "" : aUri, 
									aLocalName, aNodeName,  // JBH 2013 05 17 these args were the wrong way around!
									 "CDATA", a.getNodeValue());
//							attr.addAttribute(aUri == null ? "" : aUri, 
//									aNodeName,
//									aLocalName, "CDATA", a.getNodeValue());
						}
					}
				}
				String nUri = pNode.getNamespaceURI();
				if (nUri == null) {
					nUri = "";
				}
				pHandler.startElement(nUri, pNode.getLocalName(),
						pNode.getNodeName(), attr);
				doSerializeChilds(pNode, pHandler);
				pHandler.endElement(nUri, pNode.getLocalName(),
						pNode.getNodeName());
				if (!isNamespaceDeclarationAttribute) {
					endPrefixMappingEvents(pNode, pHandler);
				}
				break;
			case Node.TEXT_NODE:
				characters(pHandler, pNode.getNodeValue(), false);
				break;
			case Node.CDATA_SECTION_NODE:
				characters(pHandler, pNode.getNodeValue(), true);
				break;
			case Node.PROCESSING_INSTRUCTION_NODE:
				pHandler.processingInstruction(pNode.getNodeName(), pNode.getNodeValue());
				break;
			case Node.ENTITY_REFERENCE_NODE:
				pHandler.skippedEntity(pNode.getNodeName());
				break;
			case Node.COMMENT_NODE:
				if (pHandler instanceof LexicalHandler) {
					String s = pNode.getNodeValue();
					((LexicalHandler) pHandler).comment(s.toCharArray(), 0, s.length());
				}
				break;
			default:
				throw new IllegalStateException("Unknown node type: " + pNode.getNodeType());
		}
	}
}