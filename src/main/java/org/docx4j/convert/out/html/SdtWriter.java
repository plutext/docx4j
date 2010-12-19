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
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.sdt.QueryString;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.SdtPr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXException;

public class SdtWriter {
	
	private static Logger log = Logger.getLogger(SdtWriter.class);		
	
	private static Map<String, SdtTagHandler> handlers = new HashMap<String, SdtTagHandler>();
	
	public static void registerTagHandler(String key, SdtTagHandler handler) {
		
		handlers.put(key, handler);
	}
	
	static IdentityHandler identity = new IdentityHandler();
	
//	public static Node toNode(String sdtId, String sdtTag, String sdtAlias,
//			NodeIterator childResults) throws TransformerException {

	public static Node toNode(WordprocessingMLPackage wmlPackage,
    		NodeIterator sdtPrNodeIt,
			NodeIterator childResults) throws TransformerException {
	
		Node result = null;
		SdtTagHandler handler;

		/* We avoid unmarshalling the sdt itself, since 
		 * it could be one of a number of docx4j classes.
		 * 
		 * If we did need to unmarshall it, we could either
		 * use its parent element as a hint, or try 
		 * unmarshalling as SdtBlock, then SdtRow, then SdtCell
		 * etc.
		 * 
		 * Still, you have access to all of sdtPr, and the
		 * transformed w:sdtContent/*. What more could you want ;-)
		 */
		
		SdtPr sdtPr;
		try {
    		Node n = sdtPrNodeIt.nextNode();
    		sdtPr = (SdtPr)XmlUtils.unmarshal(n, Context.jc, SdtPr.class);
		} catch (JAXBException e1) {
			throw new TransformerException("Missing or broken w:sdtPr", e1);
		}
		
		if (sdtPr.getTag()==null) {
			
			if (handlers.get("*")!=null) {
				// handler '*' only gets applied if no other one has been				
				handler = handlers.get("*");			
				result = handler.toNode(wmlPackage, null, null, childResults);
			} else {
				// Just return the contents!
				result = identity.toNode(wmlPackage, null, null, childResults);
			}
			return result;
		}
		
		
		HashMap<String, String> map 
			= QueryString.parseQueryString(sdtPr.getTag().getVal(), true);
		
		
		/*
		 * This is intended to handle more than one matching key
		 * in the tag, though each is likely to result in an
		 * extra nested div.  ie no attempt is made to consolidate
		 * everything on a single div tag.
		 */
		
		for( String key : map.keySet() ) {
			
			handler = handlers.get(key);
			if (handler == null) {
				log.error("No model registered for sdt tag key " + key + "; ignoring ..");
				continue;
			} else {
				log.debug("Using model " + handler.getClass().getName() + " for sdt tag key "
						+ key);
			}
			
			if (result==null) {
				result = handler.toNode(wmlPackage, sdtPr, map, childResults);
			} else {
				result = handler.toNode(wmlPackage, sdtPr, map, result);
			}			
		}
		
		// Always apply handler called '**'
		if (handlers.get("**")!=null) {
			handler = handlers.get("**");			
			if (result==null) {
				result = handler.toNode(wmlPackage, sdtPr, map, childResults);
			} else {
				result = handler.toNode(wmlPackage, sdtPr, map, result);
			}			
		}
		
		// If nothing matched, make sure we still return something.
		// If you want to ignore the sdt contents, you need use the null handler
		if (result==null) {
			if (handlers.get("*")!=null) {
				// handler '*' only gets applied if no other one has been				
				handler = handlers.get("*");			
				result = handler.toNode(wmlPackage, sdtPr, map, childResults);
			} else {
				// Just return the contents!
				result = identity.toNode(wmlPackage, sdtPr, map, childResults);
			}
		}
		
		return result;
	}

	/**
	 * Include sdt contents as-is in output.
	 */	
	static class IdentityHandler extends SdtTagHandler {
		
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
				
				return attachContents(docfrag, docfrag, childResults);
				
			} catch (Exception e) {
				throw new TransformerException(e);
			}
		}

		@Override
		public Node toNode(WordprocessingMLPackage wmlPackage, SdtPr sdtPr,
				HashMap<String, String> tagMap,
				Node resultSoFar) throws TransformerException {
			// Implemented just in case user explicitly invokes IdentityHandler..			
			try {
				// Create a DOM builder and parse the fragment
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				Document document = factory.newDocumentBuilder().newDocument();
				DocumentFragment docfrag = document.createDocumentFragment();
				
				return attachContents(docfrag, docfrag, resultSoFar);
				
			} catch (Exception e) {
				throw new TransformerException(e);
			}
		}
	}
	
	/**
	 * Omit sdt contents from output.
	 */
	static class NullHandler extends SdtTagHandler {
		
		@Override
		public Node toNode(WordprocessingMLPackage wmlPackage, SdtPr sdtPr,
				HashMap<String, String> tagMap,
				NodeIterator childResults) throws TransformerException {

			return emptyFragment();
		}

		@Override
		public Node toNode(WordprocessingMLPackage wmlPackage, SdtPr sdtPr,
				HashMap<String, String> tagMap,
				Node resultSoFar) throws TransformerException {
			
			return emptyFragment();
		}
		
		private DocumentFragment emptyFragment() throws TransformerException {
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			Document document;
			try {
				document = factory.newDocumentBuilder().newDocument();
			} catch (ParserConfigurationException e) {
				throw new TransformerException(e);
			}
			return document.createDocumentFragment();
			
		}
	}
	
}

/*
 * 
  	<xsl:choose>
  		<xsl:when test="contains(./w:sdtPr/w:tag/@w:val, 'XSLT_')">
  			<!-- An SDT we've inserted to handle adjacent borders/shading nodes -->

			<xsl:variable name="childResults">
	  			<xsl:apply-templates select="w:sdtContent/*"/>
			</xsl:variable>
		
		
		  	<xsl:choose>
  				<xsl:when test="./w:sdtContent/w:p[1]/w:pPr">
  				
					<xsl:variable name="pPrNode" select="./w:sdtContent/w:p[1]/w:pPr" />
				  	
					<xsl:variable name="pStyleVal" select="string( w:pPr/w:pStyle/@w:val )" />  	

			  		<xsl:copy-of select="java:org.docx4j.convert.out.html.HtmlExporterNG2.createBlockForSdt( 
	  					$wmlPackage, $pPrNode, $pStyleVal, $childResults, string(./w:sdtPr/w:tag/@w:val))" />
  				
  				</xsl:when>
  				<xsl:when test="./w:sdtContent/w:sdt/w:sdtContent/w:p[1]/w:pPr">
  				
					<xsl:variable name="pPrNode" select="./w:sdtContent/w:sdt/w:sdtContent/w:p[1]/w:pPr" />
				  	
					<xsl:variable name="pStyleVal" select="string( w:pPr/w:pStyle/@w:val )" />  	

			  		<xsl:copy-of select="java:org.docx4j.convert.out.html.HtmlExporterNG2.createBlockForSdt( 
	  					$wmlPackage, $pPrNode, $pStyleVal, $childResults, string(./w:sdtPr/w:tag/@w:val))" />
  				
  				</xsl:when>
		  		<xsl:otherwise>
		  			<!-- Should not happen. -->
  					<xsl:apply-templates select="w:sdtContent/*"/>
  				</xsl:otherwise>  				
  			</xsl:choose>
  		
  		</xsl:when>
  		<xsl:when test="contains(./w:sdtPr/w:tag/@w:val, '@class=collapse')">
  			<!-- Collapsible -->
  			<xsl:variable name="id" select="string(w:sdtPr/w:id/@w:val)"/>
			<p style="padding-left: 30px;"><a onmousedown="toggleDiv('t{$id}');" href="javascript:;">Toggle: <xsl:value-of select="w:sdtPr/w:alias/@w:val"/></a></p>
			
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
  		<xsl:otherwise>
  			<xsl:apply-templates select="w:sdtContent/*"/>
  		</xsl:otherwise>
  	</xsl:choose>
 * 
 * 
 * 
 * 
 */ 
