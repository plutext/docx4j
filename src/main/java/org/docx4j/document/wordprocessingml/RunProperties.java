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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.docx4j.Namespaces;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.tree.DefaultElement;


/**
 * In a similar design to ParagraphProperties, this class stores 
 * a list of the run properties; each of these is
 * represented by an appropriate object, or if we don't have an object
 * for it, then just the snippet of XML.  This way, we preserve 100%
 * fidelity.
 * 
 * The question is what is the most elegant naming convention and
 * representation for these objects?  Should we base them on the XSD types? 
 *
 * @author Jason Harrop
 */
public final class RunProperties {


	protected HashMap properties = new HashMap();
	/** The properties HashMap is keyed by the class name.  If we were
	 *  wanting to set or get justification, you'd see whether it was
	 *  already in the HashMap.  In the case of setting, if it wasn't
	 *  there, you'd create the object and put it in the HashMap. 
	 *
	 */ 
	
	public RunProperties(Element e) {
				
	    Iterator elementIterator = e.elementIterator();
	    while(elementIterator.hasNext()){
	      Element element = (Element)elementIterator.next();
	      
		  if (element.getName()=="rStyle" ) {
			  // I'mguessing its called that
			  // In which case do something...
		      properties.put( "rStyle", new rStyle(element) );			  
		  } else {
		      //log.warn("        RP - Adding raw XML for '" + element.getName() + "'");
		      properties.put( element.getName(), element );
		  }
	    }  
	}

	/* Create an XML element from the Java object tree
	 * 
	 */
	public Element marshall() {		
		
		Element rPr = new DefaultElement(new QName(Constants.RUN_PROPERTIES_TAG_NAME,
				Namespaces.namespaceWord));
		
		List children = rPr.content();

		Iterator it = properties.keySet().iterator();
		while(it.hasNext()) {
			Object o = properties.get( it.next() );
			if ( o instanceof rStyle ) {
				children.add( ((rStyle)o).marshall() );
			} else {
				children.add( ((Element)o).createCopy() );
			}
		}		
		
		
		return rPr;		
	}
	
	
	/** Return the value of the named property, or null if the property is
	 *  not set.
	 * @param propName
	 * @return
	 */
	public Object getProperty(String propName) {
		
		return properties.get(propName);
		
	}

	public void setProperty(String propName, Object propValue) {
		properties.put(propName, propValue);
	}

	/*
	 * TODO: Jason, please check whether you agree with 
	 * this method addition
	 */
	public List getProperties() {
		return new ArrayList(properties.values());
	}

}
