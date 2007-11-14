/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of Docx4J.

    Docx4J is free software: you can redistribute it and/or modify
    it under the terms of version 3 of the GNU General Public License 
    as published by the Free Software Foundation.

    Docx4J is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License   
    along with Docx4J.  If not, see <http://www.gnu.org/licenses/>.
    
 */

package org.docx4j.document.wordprocessingml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import org.docx4j.Namespaces;
import org.docx4j.document.wordprocessingml.RunProperties;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;



/**
 * @author Jason Harrop
 */
public final class Run {
	
	private static Logger log = Logger.getLogger(Run.class);		
	
	
/*  For example:
 * 
 *      <w:r w:rsidRPr="00637B47">
        <w:rPr>
          <w:b/>
          <w:color w:val="FF0000"/>
        </w:rPr>
        <w:t>char formatting</w:t>
 */	
	

	// For now, this is just null if there is no run properties node on
	// the parent run.
	protected RunProperties rPr;
	
	protected ArrayList runContents;
	
	// The text contained in the run
	protected String t;
	
	public Run( Element e) {
		runContents = new ArrayList();

		StringBuffer sb = new StringBuffer();
		boolean hasTextChild = false;
	    Iterator elementIterator = e.elementIterator();
	    while (elementIterator.hasNext()) {
			Element element = (Element) elementIterator.next();

			if (element.getName() == "rPr") {
				rPr = new RunProperties(element);
				runContents.add(rPr);
			} else if (element.getName() == "t") {
				hasTextChild = true;
				String txt = element.getText();
				if (txt == null) {
					txt = "";
					log.warn("Encountered empty Run!");
				}
				sb.append(txt);
				runContents.add(element);
			} else {
				// log.info(" Adding raw XML for '" + element.getName() + "'");
				runContents.add(element);
			}
		}
		if (!hasTextChild) {
	    	log.warn("Encountered Run without a t element!");			
		}
		t = sb.toString();
        //log.info("text of run was '" + t + "'");

		// log.warn(" TODO - unmarshall the attributes on w:r");
        //log.info("text of run was '" + t + "'");
	    
	}
	
	public Run( String t) {
		this.t = t;
	}

	public List getRunContents() {
		return this.runContents;
	}

	/* Create an XML element from the Java object tree
	 * 
	 */
	public Element marshall() {		

		//log.warn("    TODO - marshall the attributes on w:r");		
		
		Element r = new DefaultElement(new QName(Constants.PARAGRAPH_RUN_TAG_NAME,
				Namespaces.namespaceWord));

		List children = r.content();
		
		// The RunProperties
		if (rPr !=null) {
			children.add( ((RunProperties)rPr).marshall() );
		}
		
		//The text itself
		Element elText = new DefaultElement(
								new QName(Constants.RUN_TEXT,
										Namespaces.namespaceWord));
		elText.setText(t);		
		children.add(elText);

		return r;		
	}
	
	protected String getText() {
		return t;
	}
	
	
	
	
	
}
