/*
 *  Copyright 2010, Plutext Pty Ltd.
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
package org.docx4j.convert.out.html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jakarta.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.preprocess.Containerization;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.SdtPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXException;

/**
 * 
 * In Word, adjacent paragraphs with the same borders are enclosed in a single border
 * (unless bullets/numbering apply).
 * 
 * Similarly with shading.  (If the 2 paragraphs are shaded different colors, then the
 * color of the first extends to the start of the second, so there is no white strip
 * between them).
 * 
 * To do the same in HTML, Containerization puts matching paragraphs into a content
 * control.
 * 
 * It is the job of this class to make a div out of that content control, and 
 * set the border/shading on that as a style, copying the style value from
 * the first paragraph enclosed.
 *
 * <p>Borders ({@link Containerization#TAG_BORDERS}) since 17.3.3: each paragraph writes its
 * own borders, its left and right standing at its own indents as Word stands them (CR-003),
 * and of the top and bottom only the group's first paragraph keeps its top and its last its
 * bottom, so that the group is one box; the div carries no style.  This handler is registered
 * for that tag by default ({@link SdtWriter}).  Until then the div copied the first
 * paragraph's whole style (its margins and indents too, applied twice) while the paragraphs
 * left their top and bottom borders to it, so no top or bottom border was drawn at all, and
 * none by default, with no handler registered.</p>
 * 
 * TODO: Note that there will typically be 2 content controls and therefore divs,
 * one for borders and one for shading.  But the style will be the same on
 * each, so one is redundant. 
 * 
 * @author jason
 *
 */
public class TagSingleBox extends SdtTagHandler {

	private static Logger log = LoggerFactory.getLogger(TagSingleBox.class);

	private Element createDiv(Document document, DocumentFragment docfrag, 
			Node n) throws ParserConfigurationException, IOException, SAXException, JAXBException {
		
//		log.debug("Node is: " + n.getNodeName() );

		Element xhtmlDiv = document.createElement("div");
		if (n instanceof Element) {
			xhtmlDiv.setAttribute("style", ((Element)n).getAttribute("style") );
		}

		docfrag.appendChild(xhtmlDiv);

		return xhtmlDiv;
	}
	
	private Node getNodeByName(String name, Node n) {
		
		if (n.getNodeType()!=Node.ELEMENT_NODE
				&& n.getNodeType()!=Node.DOCUMENT_NODE
				&& n.getNodeType()!=Node.DOCUMENT_FRAGMENT_NODE) {
			return null;
		}
		
		if (n.getNodeName().equals(name)) {
			return n;
		}
		
		NodeList list = n.getChildNodes();
		for (int i=0; i<list.getLength(); i++) {
			Node c = getNodeByName(name, list.item(i));
			if (c!=null) return c;
		}
		return null;
	}
	
	@Override
	public Node toNode(WordprocessingMLPackage wmlPackage, SdtPr sdtPr,
			HashMap<String, String> tagMap,
			NodeIterator childResults) throws TransformerException {

		try {
			// Create a DOM builder and parse the fragment
			Document document = XmlUtils.getNewDocumentBuilder().newDocument();
			DocumentFragment docfrag = document.createDocumentFragment();
			
			Node contents = childResults.nextNode();

			// We want to copy the direct/adhoc formatting from the first p;
			// but that's not a w:p anymore; its an HTML p so ..			
			Node firstP = getNodeByName("p", contents );
			
			if (isBorders(tagMap)) {
				Element xhtmlDiv = document.createElement("div");
				docfrag.appendChild(xhtmlDiv);
				Node result = attachContents(docfrag, xhtmlDiv, contents);
				oneBox(xhtmlDiv);
				if (!xhtmlDiv.hasChildNodes()) docfrag.removeChild(xhtmlDiv); // an empty group
				return result;
			}

			Element xhtmlDiv = this.createDiv(document, docfrag, firstP );
									
			return attachContents(docfrag, xhtmlDiv, contents);
			
		} catch (Exception e) {
			throw new TransformerException(e);
		}

	}

		@Override
		public Node toNode(WordprocessingMLPackage wmlPackage, SdtPr sdtPr,
				HashMap<String, String> tagMap,
				Node resultSoFar) throws TransformerException {
			
			
			try {
				// Create a DOM builder and parse the fragment
				Document document = XmlUtils.getNewDocumentBuilder().newDocument();
				DocumentFragment docfrag = document.createDocumentFragment();
				
				Node contents = resultSoFar;
				Node firstP = getNodeByName("p", contents );
				// NB, this first p could be one which another Handler inserted;
				// caveat emptor.
				
				if (isBorders(tagMap)) {
					Element xhtmlDiv = document.createElement("div");
					docfrag.appendChild(xhtmlDiv);
					Node result = attachContents(docfrag, xhtmlDiv, contents);
					oneBox(xhtmlDiv);
					if (!xhtmlDiv.hasChildNodes()) docfrag.removeChild(xhtmlDiv); // an empty group
					return result;
				}

				Element xhtmlDiv = this.createDiv(document, docfrag, firstP );
				
				return attachContents(docfrag, xhtmlDiv, contents);
				
			} catch (Exception e) {
				throw new TransformerException(e);
			}
		}
		
	

	private static boolean isBorders(HashMap<String, String> tagMap) {
		return tagMap != null && tagMap.containsKey(Containerization.TAG_BORDERS);
	}

	/** The group's paragraphs as one box: the first keeps its top border, the last its bottom */
	private static void oneBox(Element div) {
		List<Element> paragraphs = new ArrayList<Element>();
		collectParagraphs(div, paragraphs);
		for (int i = 0; i < paragraphs.size(); i++) {
			Element p = paragraphs.get(i);
			String style = p.getAttribute("style");
			if (style == null || style.isEmpty()) continue;
			String kept = withoutSides(style, i > 0, i < paragraphs.size() - 1);
			if (kept.isEmpty()) {
				p.removeAttribute("style");
			} else if (!kept.equals(style)) {
				p.setAttribute("style", kept);
			}
		}
	}

	private static void collectParagraphs(Node n, List<Element> paragraphs) {
		for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c.getNodeType() != Node.ELEMENT_NODE) continue;
			String name = c.getLocalName() != null ? c.getLocalName() : c.getNodeName();
			if ("p".equals(name) || "li".equals(name)) {
				paragraphs.add((Element) c);
			} else if (!"table".equals(name)) {
				collectParagraphs(c, paragraphs);
			}
		}
	}

	/** A style's declarations less the top border and padding (top) and the bottom ones (bottom) */
	static String withoutSides(String style, boolean top, boolean bottom) {
		StringBuilder sb = new StringBuilder();
		for (String declaration : style.split(";")) {
			if (declaration.trim().isEmpty()) continue;
			String name = declaration.substring(0, Math.max(0, declaration.indexOf(':'))).trim().toLowerCase();
			if (top && (name.startsWith("border-top-") || name.equals("padding-top"))) continue;
			if (bottom && (name.startsWith("border-bottom-") || name.equals("padding-bottom"))) continue;
			sb.append(declaration).append(';');
		}
		return sb.toString();
	}

}
