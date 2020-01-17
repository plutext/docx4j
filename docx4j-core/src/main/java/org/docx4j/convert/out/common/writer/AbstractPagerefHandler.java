/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
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
package org.docx4j.convert.out.common.writer;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.AbstractWmlConversionContext;
import org.docx4j.model.fields.FldSimpleModel;
import org.docx4j.model.fields.FormattingSwitchHelper;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//In HTML there is only one page - therefore the result is always a (more or less formatted) "1"
public abstract class AbstractPagerefHandler implements AbstractFldSimpleWriter.FldSimpleNodeWriterHandler {
	protected int outputType = -1;
	
	protected AbstractPagerefHandler(int outputType) {
		this.outputType = outputType;
	}
	
	@Override
	public String getName() { return "PAGEREF"; }

	@Override
	public int getProcessType() {
		return PROCESS_NONE;
	}

	@Override
	public Node toNode(AbstractWmlConversionContext context, FldSimpleModel model, Document doc) throws TransformerException {
	String bookmarkId = model.getFldParameters().get(0);
	Node content = model.getContent();
	Node literalNode = null;
	AbstractHyperlinkWriterModel hyperlinkModel = null;
	DocumentFragment docFrag = null;
	String textcontent = null;
	List<String> textcontentitems = null;
	String textcontentitem = null;
		if (FormattingSwitchHelper.hasSwitch("\\p", model.getFldParameters())) {
			textcontent = getTextcontent(content);
			textcontentitems = splitTextcontent(textcontent);
			//textcontentitems == null -> no page number (above/below)
			//textcontentitems != null -> split [literal,] # marker [, literal]
			if (textcontentitems != null) {
				docFrag = doc.createDocumentFragment();
				for (int i=0; i<textcontentitems.size(); i++) {
					textcontentitem = textcontentitems.get(i);
					if ("#".equals(textcontentitem)) {
						docFrag.appendChild(createPageref(context, doc, bookmarkId));
					}
					else {
						
						literalNode = doc.createDocumentFragment();
						XmlUtils.treeCopy(content, literalNode);
						literalNode = literalNode.getFirstChild();
						setTextcontent(literalNode, textcontentitem);
						docFrag.appendChild(literalNode);
					}
				}
				content = docFrag;
			}
		}
		else {
			content = doc.createDocumentFragment();
			content.appendChild(createPageref(context, doc, bookmarkId));
		}
		if (FormattingSwitchHelper.hasSwitch("\\h", model.getFldParameters())) {
			hyperlinkModel = new AbstractHyperlinkWriterModel();
			hyperlinkModel.build(context, model, content); //the bookmark is the target, \h gets ignored
			content = HyperlinkUtil.toNode(outputType, context, hyperlinkModel, content, doc);
		}
		return content;
	}

	protected List<String> splitTextcontent(String textcontent) {
	List<String> ret = null;
	int startPos = 0;
	int endPos = -1;
		if ((textcontent != null) && (textcontent.length() > 0)) {
			while ((startPos < textcontent.length()) && 
				   (!Character.isDigit(textcontent.charAt(startPos))))
					startPos++;
			endPos = startPos + 1;
			while ((endPos < textcontent.length()) && 
				   (Character.isDigit(textcontent.charAt(endPos))))
						endPos++;
			if ((startPos < endPos) && (endPos <= textcontent.length())) {
				ret = new ArrayList<String>();
				if (startPos > 0) ret.add(textcontent.substring(0, startPos));
				ret.add("#"); // marker for the position of the pagenumber
				if (endPos < textcontent.length()) ret.add(textcontent.substring(endPos));
			}
		}
		return ret;
	}

	protected String getTextcontent(Node root) {
	Node textNode = findTextNode(root);
		return (textNode != null ? textNode.getNodeValue() : null);
	}

	protected void setTextcontent(Node root, String text) {
	Node textNode = findTextNode(root);
		if (textNode != null) {
			//Apache FOP may remove any leading/trailing spaces if 
			//a page-number-citation is at the end of a paragraph
			//for this reson change the first/last one to a non breaking space.
			if (text.charAt(text.length() - 1) == ' ') {
				text = text.substring(0, text.length() - 1) + (char)160;
			}
			if (text.charAt(0) == ' ') {
				if (text.length() > 1) {
					text = (char)160 + (text.substring(1));
				}
				else {
					text = Character.toString((char)160);
				}
			}
			textNode.setNodeValue(text);
		}
	}

	/** Find a textnode with a text content
	 * 
	 * @return textnode or null
	 */
	protected Node findTextNode(Node root) {
	Node ret = null;
	NodeList childNodes = null;
	Node childNode = null;
		switch (root.getNodeType()) {
			case Node.TEXT_NODE:
				ret = ((root.getNodeValue() != null) && (root.getNodeValue().length() > 0) ?
						root : null);
				break;
			case Node.DOCUMENT_NODE:
			case Node.DOCUMENT_FRAGMENT_NODE:
			case Node.ELEMENT_NODE:
				childNodes = root.getChildNodes();
				for (int i=0; (ret == null) && (i<childNodes.getLength()); i++) 
					ret = findTextNode(childNodes.item(i));
				break;
		}
		return ret;
	}

	protected abstract Node createPageref(AbstractWmlConversionContext context, Document doc, String bookmarkId);
}