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

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.sdt.QueryString;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.SdtPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

public class SdtWriter {
	
	private static Logger log = LoggerFactory.getLogger(SdtWriter.class);		
	
	private static Map<String, SdtTagHandler> handlers = new HashMap<String, SdtTagHandler>();
	
	public static void registerTagHandler(String key, SdtTagHandler handler) {
		
		handlers.put(key, handler);
	}
	
	static IdentityHandler identity = new IdentityHandler();
	
	private static Node debug(Node n) {
		String xml = XmlUtils.w3CDomNodeToString(n);
		log.debug("result: " + xml);
		
		return n;
	}
	
	public static Node toNode(HTMLConversionContext context,
    		NodeIterator sdtPrNodeIt,
			NodeIterator childResults) throws TransformerException {
	
		Node result = null;
		SdtTagHandler handler;

		/* We avoid unmarshalling the sdt itself, since 
		 * it could be one of a number of docx4j classes
		 * [note 2012 06 03: but ContentAccessor interface
		 *  overcomes this issue?]
		 * 
		 * If we did need to unmarshall it, we could either
		 * use its parent element as a hint, or try 
		 * unmarshalling as SdtBlock, then SdtRow, then SdtCell
		 * etc.
		 * 
		 * Still, you have access to all of sdtPr, and the
		 * transformed w:sdtContent/*. 
		 * 
		 * What more could you want? Well, the raw w:sdtContent/*
		 * but that is not passed in. 
		 */
		
		SdtPr sdtPr;
		try {
    		Node n = sdtPrNodeIt.nextNode();
    		sdtPr = (SdtPr)XmlUtils.unmarshal(n, Context.jc, SdtPr.class);
		} catch (JAXBException e1) {
			throw new TransformerException("Missing or broken w:sdtPr", e1);
		}
		
		log.debug("in sdt");
		if (sdtPr.getTag()==null) {
			log.debug(".. no w:tag");
			
			if (handlers.get("*")!=null) {
				// handler '*' only gets applied if no other one has been				
				log.debug("'*' handler");
				handler = handlers.get("*");			
				result = handler.toNode(context.getWmlPackage(), null, null, childResults);
			} else {
				// Just return the contents!
				log.debug("identity handler");
				result = identity.toNode(context.getWmlPackage(), null, null, childResults);
			}
			return debug(result);
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
				log.info("No model registered for sdt tag key " + key + "; ignoring ..");
				continue;
			} else {
				log.debug("Using model " + handler.getClass().getName() + " for sdt tag key "
						+ key);
			}
			
			if (result==null) {
				result = handler.toNode(context.getWmlPackage(), sdtPr, map, childResults);
			} else {
				result = handler.toNode(context.getWmlPackage(), sdtPr, map, result);
			}			
		}
		
		// Always apply handler called '**'
		if (handlers.get("**")!=null) {
			handler = handlers.get("**");			
			log.info("applying handler '**' " );
			if (result==null) {
				result = handler.toNode(context.getWmlPackage(), sdtPr, map, childResults);
			} else {
				result = handler.toNode(context.getWmlPackage(), sdtPr, map, result);
			}			
		}
		
		// If nothing matched, make sure we still return something.
		// If you want to ignore the sdt contents, you need use the null handler
		if (result==null) {
			if (handlers.get("*")!=null) {
				// handler '*' only gets applied if no other one has been				
				log.info("applying handler '*' " );
				handler = handlers.get("*");			
				result = handler.toNode(context.getWmlPackage(), sdtPr, map, childResults);
			} else {
				// Just return the contents!
				log.info("using identity " );
				result = identity.toNode(context.getWmlPackage(), sdtPr, map, childResults);
			}
		}

		if (log.isDebugEnabled()) {
			return debug(result);
		} else {
			return result;
		}
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
				Document document = XmlUtils.getNewDocumentBuilder().newDocument();
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
				Document document = XmlUtils.getNewDocumentBuilder().newDocument();
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
			
			Document document = XmlUtils.getNewDocumentBuilder().newDocument();
			return document.createDocumentFragment();
			
		}
	}
	
}

