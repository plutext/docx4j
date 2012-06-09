/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.convert.out.html;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Id;
import org.docx4j.wml.SdtPr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXException;

/**
 * If the content control has a tag containing @class=foo,
 * then create a div with that attribute and value.
 * 
 * If the value is the special case collapse, ie @class=collapse, 
 * then make this a collapsible div using javascript.
 * 
 * @author jason
 *
 */
public class TagClass extends SdtTagHandler {

	private static Logger log = Logger.getLogger(TagClass.class);

	/* replaces XSLT:
	 * 
  		<xsl:when test="contains(./w:sdtPr/w:tag/@w:val, '@class=collapse')">
  			<!-- Collapsible -->
  			<xsl:variable name="id" select="string(w:sdtPr/w:id/@w:val)"/>
			<p style="padding-left: 30px;">
			<a onmousedown="toggleDiv('t{$id}');" href="javascript:;">Toggle: 
			<xsl:value-of select="w:sdtPr/w:alias/@w:val"/></a></p>
			
			<!-- collapsed or expanded?  Default is collapsed.  -->
			<xsl:choose>
		  		<xsl:when test="contains(./w:sdtPr/w:tag/@w:val, 'display=block')">
					<div id="t{$id}" style="display: block;">     			
	  					<xsl:apply-templates select="w:sdtContent/*"/>
					</div> 
  				</xsl:when>
		  		<xsl:otherwise>
					<div id="t{$id}" style="display: none;">     			
	  					<xsl:apply-templates select="w:sdtContent/*"/>
					</div> 
  				</xsl:otherwise>  				
			</xsl:choose>
			
  		</xsl:when>
	 * 
	 */

	private Element createDiv(Document document, DocumentFragment docfrag, 
			SdtPr sdtPr,
			HashMap<String, String> tagMap) throws ParserConfigurationException, IOException, SAXException {
		
		// log.info("Document: " + document.getClass().getName() );

		String classVal = tagMap.get("@class");
		
		String sdtId="??";
		if (sdtPr.getId()!=null) sdtId=sdtPr.getId().getVal().toString();

		String sdtAlias = "??";
		SdtPr.Alias alias = this.getAlias(sdtPr);
		if (alias!=null) sdtAlias = alias.getVal();
		
		if (classVal.equals("collapse") ) {				
			XmlUtils.appendXmlFragment(document, docfrag, 
					"<p style=\"padding-left: 30px;\">"
					+ "<a onmousedown=\"toggleDiv('t" + sdtId +"');\" href=\"javascript:;\">Toggle:" + sdtAlias  
					+ "</a></p>"
					);				
		} 
		
		Element xhtmlDiv = document.createElement("div");
		docfrag.appendChild(xhtmlDiv);						
		xhtmlDiv.setAttribute("class", tagMap.get("@class"));
					
		if (classVal.equals("collapse") ) {
			xhtmlDiv.setAttribute("id", "t" + sdtId);
			if (tagMap.get("display")!=null
					&& tagMap.get("display").equals("block")) {
				xhtmlDiv.setAttribute("style", "display: block;");					
			} else {
				xhtmlDiv.setAttribute("style", "display: none;");										
			}
		}			
		
		return xhtmlDiv;
	}
	
	@Override
	public Node toNode(WordprocessingMLPackage wmlPackage, SdtPr sdtPr,
			HashMap<String, String> tagMap,
			NodeIterator childResults) throws TransformerException {

		try {
			// Create a DOM builder and parse the fragment
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			Document document = factory.newDocumentBuilder().newDocument();
			DocumentFragment docfrag = document.createDocumentFragment();
			
			Element xhtmlDiv = this.createDiv(document, docfrag, sdtPr, tagMap);
			
			return attachContents(docfrag, xhtmlDiv, childResults);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.toString());
			log.error(e);
			throw new TransformerException(e);
		}

	}

		@Override
		public Node toNode(WordprocessingMLPackage wmlPackage, SdtPr sdtPr,
				HashMap<String, String> tagMap,
				Node resultSoFar) throws TransformerException {
			try {
				// Create a DOM builder and parse the fragment
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				Document document = factory.newDocumentBuilder().newDocument();
				DocumentFragment docfrag = document.createDocumentFragment();
				
				Element xhtmlDiv = this.createDiv(document, docfrag, sdtPr, tagMap);
				
				return attachContents(docfrag, xhtmlDiv, resultSoFar);
				
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e.toString());
				log.error(e);
				throw new TransformerException(e);
			}
		}
		
	

}
