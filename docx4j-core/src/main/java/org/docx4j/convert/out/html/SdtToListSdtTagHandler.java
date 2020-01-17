package org.docx4j.convert.out.html;

import java.util.HashMap;

import javax.xml.transform.TransformerException;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.SdtPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

public class SdtToListSdtTagHandler extends SdtTagHandler {   
	
	private static Logger log = LoggerFactory.getLogger(SdtToListSdtTagHandler.class);
		
	@Override
	public Node toNode(WordprocessingMLPackage wmlPackage, SdtPr sdtPr,
			HashMap<String, String> tagMap,
			NodeIterator childResults) throws TransformerException {

		try {
			// Create a DOM builder and parse the fragment
			Document document = XmlUtils.getNewDocumentBuilder().newDocument();
			DocumentFragment docfrag = document.createDocumentFragment();

			if (tagMap.get("HTML_ELEMENT") == null) {

				// don't add a list
				return attachContents(docfrag, docfrag, childResults);
				
			} else if (tagMap.get("HTML_ELEMENT").equals("OL")) {

				Element xhtmlDiv = document.createElement("ol");
				docfrag.appendChild(xhtmlDiv);						
    			return attachContents(docfrag, xhtmlDiv, childResults);
				
			} else { // if (tagMap.get("HTML_ELEMENT").equals("UL")) {

				Element xhtmlDiv = document.createElement("ul");
				docfrag.appendChild(xhtmlDiv);						
    			return attachContents(docfrag, xhtmlDiv, childResults);
			}
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

			if (tagMap.get("HTML_ELEMENT") == null) {

				// don't add a list
				return attachContents(docfrag, docfrag, resultSoFar);
				
			} else if (tagMap.get("HTML_ELEMENT").equals("OL")) {

				Element xhtmlDiv = document.createElement("ol");
				docfrag.appendChild(xhtmlDiv);						
    			return attachContents(docfrag, xhtmlDiv, resultSoFar);
				
			} else { // if (tagMap.get("HTML_ELEMENT").equals("UL")) {

				Element xhtmlDiv = document.createElement("ul");
				docfrag.appendChild(xhtmlDiv);						
    			return attachContents(docfrag, xhtmlDiv, resultSoFar);
			}
		} catch (Exception e) {
			throw new TransformerException(e);
		}
	}
}
