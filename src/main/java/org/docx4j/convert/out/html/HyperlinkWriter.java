/*
 *  Copyright 2009, Plutext Pty Ltd.
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

import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.convert.out.AbstractWmlConversionContext;
import org.docx4j.convert.out.common.writer.AbstractHyperlinkWriter;
import org.docx4j.convert.out.common.writer.AbstractSymbolWriter;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.model.HyperlinkModel;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.traversal.NodeIterator;

/**
 * HTML HyperlinkWriter
 * @author jharrop
 * @since 3.0.0
 */
public class HyperlinkWriter extends AbstractHyperlinkWriter {
	
  private final static Logger log = Logger.getLogger(HyperlinkWriter.class);

  @Override
  protected Node toNode(AbstractWmlConversionContext context, 
		  HyperlinkModel hm, Document doc) throws TransformerException {
	  
	    P.Hyperlink hyperlink = hm.getHyperlink();	  
	  
	  	String hTemp = resolveHref(context, hyperlink.getId() );
	  	
	  	hTemp = this.convertExtension(hTemp, context.getHyperlinkExtensionMappings());
	  	
	  	if (hyperlink.getAnchor()!=null) {
	  		hTemp =hTemp + "#" + hyperlink.getAnchor(); 
	  	} else if (hyperlink.getDocLocation()!=null) {
	  		// review this.
	  		hTemp =hTemp + "#" + hyperlink.getDocLocation(); 
	  	}
	  	
	    Element a = doc.createElement("a");
	    a.setAttribute("href", hTemp);		    
	    
		XmlUtils.treeCopy(hm.getContent(), a);
		
		DocumentFragment docfrag = doc.createDocumentFragment();	    
	    docfrag.appendChild(a);
		
	    
    
    return docfrag;
  }
  
  
}
