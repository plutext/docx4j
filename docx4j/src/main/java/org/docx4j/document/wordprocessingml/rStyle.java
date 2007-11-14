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

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;


/**
 *
 * @author Jason Harrop
 */
public final class rStyle {
	
	private static Logger log = Logger.getLogger(rStyle.class);			

	// Ultimately this might contain a pointer to the relevant style
	// object.  For now, it is the a string containing the style name.
	String val;

	public rStyle( Element e) {
		
		val = e.attributeValue("val");
		log.debug("rStyle:" + val);
		
	}

	/* Create an XML element from the Java object tree
	 * 
	 */
	public Element marshall() {		
		
		Element rStyle = new DefaultElement(new QName("rStyle",
				Namespaces.namespaceWord));
			
		//The text itself
		rStyle.addAttribute(new QName("val", Namespaces.namespaceWord),
				val);

		return rStyle;		
	}

}
