/* NOTICE: This file has been changed by Plutext Pty Ltd for use with docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
 
 /* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.docx4j.dsig.crypt;

import static org.docx4j.dsig.crypt.facets.SignatureFacet.OO_DIGSIG_NS;
import static org.docx4j.dsig.crypt.facets.SignatureFacet.XML_NS;

import org.docx4j.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;

import org.docx4j.dsig.crypt.SignatureConfig.SignatureConfigurable;

/**
 * This listener class is used, to modify the to be digested xml document,
 * e.g. to register id attributes or set prefixes for registered namespaces
 * 
 * The objective is to ensure that what we sign looks exactly the same
 * as what JAXB will later save as the XMLSignaturesPart in the docx.
 * 
 */
public class SignatureMarshalListener implements EventListener, SignatureConfigurable {
	
	private static Logger LOG = LoggerFactory.getLogger(SignatureMarshalListener.class);	
	
	
	// Note, this is invoked 6 times in preSign, and 3 times in postSign
	
	
    ThreadLocal<EventTarget> target = new ThreadLocal<EventTarget>();
    
    SignatureConfig signatureConfig;
    
    public void setEventTarget(EventTarget target) {
        this.target.set(target);
    }
    
    public void handleEvent(Event e) {
        if (!(e instanceof MutationEvent)) return;
        MutationEvent mutEvt = (MutationEvent)e;
        EventTarget et = mutEvt.getTarget();
        if (et instanceof Document) {
        	/* Santuario 3 assembles the whole ds:Signature tree detached, and only
        	 * then inserts it into the document.  (Santuario 2.0.x attached
        	 * ds:Signature first, so we saw an event for each element as it was
        	 * added.)  So during marshalling there is now just this one event,
        	 * targeting the document.  If we don't handle it, the references are
        	 * digested before the namespaces are declared on ds:Signature, so what
        	 * we sign differs from what we save, and the signature is invalid.
        	 */
        	Element root = getRootElement((Document)et);
        	if (root==null) return;
        	
            EventTarget target = this.target.get();
            setListener(target, this, false);
        	registerIds(root);
            setListener(target, this, true);
            
            handleElement(root);
            return;
        }
        if (!(et instanceof Element)) return;
        handleElement((Element)et);
    }
    
    private Element getRootElement(Document doc) {
    	// not getDocumentElement(), which Xerces doesn't set until after it has dispatched the event 
    	NodeList nl = doc.getChildNodes();
    	for (int i=0; i<nl.getLength(); i++) {
    		if (nl.item(i) instanceof Element) {
    			return (Element)nl.item(i);
    		}
    	}
    	return null;
    }
    
    /**
     * handleElement would have done this for each element, had we been notified
     * as it was added.
     */
    private void registerIds(Element el) {
        if (el.hasAttribute("Id")) {
            el.setIdAttribute("Id", true);
        }
        NodeList nl = el.getChildNodes();
        for (int i=0; i<nl.getLength(); i++) {
        	if (nl.item(i) instanceof Element) {
        		registerIds((Element)nl.item(i));
        	}
        }
    }
    
    /**
     * Santuario 3 (seen in 3.0.6) uses the JDK's MIME encoder for base64 content, which breaks lines 
     * with CRLF (where earlier versions, and Word, use LF).  The CR would be saved as &amp;#13;
     * so remove it.  We're invoked as content is added, so this happens before it is digested. 
     */
    private void removeCarriageReturns(Node el) {
        NodeList nl = el.getChildNodes();
        for (int i=0; i<nl.getLength(); i++) {
        	Node n = nl.item(i);
        	if (n instanceof Text) {
        		Text text = (Text)n;
        		String data = text.getData();
        		if (data.indexOf('\r')>=0) {
        			text.setData(data.replace("\r", ""));
        		}
        	} else if (n instanceof Element) {
        		removeCarriageReturns(n);
        	}
        }
    }

    public void handleElement(Element el) {
        EventTarget target = this.target.get();
        String packageId = signatureConfig.getPackageSignatureId();
        if (el.hasAttribute("Id")) {
            el.setIdAttribute("Id", true);
            
            /* that's important.  without it:
             * 
				org.apache.xml.security.utils.resolver.ResourceResolverException: Cannot resolve element with ID idSignedProperties
					at org.apache.xml.security.utils.resolver.implementations.ResolverFragment.engineResolveURI(ResolverFragment.java:81)
					at org.apache.xml.security.utils.resolver.ResourceResolver.resolve(ResourceResolver.java:288)
					at org.apache.jcp.xml.dsig.internal.dom.DOMURIDereferencer.dereference(DOMURIDereferencer.java:112)
	             */
        }

        setListener(target, this, false);
        removeCarriageReturns(el);
        if (packageId.equals(el.getAttribute("Id"))) {        	
        	
        	//(new Throwable()).printStackTrace();
        	
        	// Declare the namespaces at the root level
            el.setAttributeNS(XML_NS, "xmlns:mdssi", OO_DIGSIG_NS);
            el.setAttributeNS(XML_NS, "xmlns:dssi", "http://schemas.microsoft.com/office/2006/digsig" );
            el.setAttributeNS(XML_NS, "xmlns:xd", "http://uri.etsi.org/01903/v1.3.2#" );            
        }
        setPrefix(el, false);
        setListener(target, this, true);
    }

    // helper method to keep it in one place
    public static void setListener(EventTarget target, EventListener listener, boolean enabled) {
        String type = "DOMSubtreeModified";
        boolean useCapture = false;
        if (enabled) {
            target.addEventListener(type, listener, useCapture);
        } else {
            target.removeEventListener(type, listener, useCapture);
        }
    }
    
    protected void setPrefix(Node el, boolean removeNS) // the boolean is so we don't removeNS at the document element
    {
//    	System.out.println(el.getLocalName());
        String prefix = signatureConfig.getNamespacePrefixes().get(el.getNamespaceURI());
        if (prefix != null && el.getPrefix() == null) {
            el.setPrefix(prefix);
        }
        
        // Remove namespace declarations JAXB has added in the sub-trees
        if (removeNS && el.hasAttributes()) {
        	
//        	LOG.debug("removing NS from : " + XmlUtils.w3CDomNodeToString(el));
        	
        	((Element)el).removeAttributeNS(XML_NS, "mdssi");
        	((Element)el).removeAttributeNS(XML_NS, "dssi");
        	((Element)el).removeAttributeNS(XML_NS, "xd");
        	((Element)el).removeAttribute("xmlns");  // doesn't work?!
        	
//        	LOG.debug("done removing NS: " + XmlUtils.w3CDomNodeToString(el));
        	
        }
        
        NodeList nl = el.getChildNodes();
        for (int i=0; i<nl.getLength(); i++) {
            setPrefix(nl.item(i), true);
        }
    }
    
    public void setSignatureConfig(SignatureConfig signatureConfig) {
        this.signatureConfig = signatureConfig;
    }
}