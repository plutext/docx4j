/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
 */

package org.docx4j.openpackaging.parts;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
//import java.net.URI;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.WebSettingsPart;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;



public abstract class AbstractDocPropsPart extends Dom4jXmlPart {
	
	/** Abstract class from which classes to implement the following
	 *  inherit:
	 *  

            <Override PartName="/docProps/app.xml" 		ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
            <Override PartName="/docProps/custom.xml" 	ContentType="application/vnd.openxmlformats-officedocument.custom-properties+xml"/>
            <Override PartName="/docProps/core.xml" 	ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
	 *  
	 *  Each of these parts has different looking xml, 
	 *  so they marshall/unmarshall differently, 
	 *  but we can and do abstract out here the get/set property
	 *  which is all each of them does. 
	 *   
	 *  Haven't looked to see whether Microsoft's Microsoft.Office.DocumentFormat.OpenXML has
	 *  an equivalent of this?
	 */ 
	
	
	public AbstractDocPropsPart(PartName partName) throws InvalidFormatException {
		super(partName);
	}
	
	// hash map of properties
	HashMap properties = new HashMap();
		
	// set property - assume it is a string for now
	public void setProperty(String name, String value) {
		properties.put(name, value);
	}
	
	// get property - cast to String!
	public String getProperty(String name) {
		return (String)properties.get(name);
	}
	
	protected void debugPrint( Document coreDoc) {
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
		    XMLWriter writer = new XMLWriter( System.out, format );
		    writer.write( coreDoc );
		} catch (Exception e ) {
			e.printStackTrace();
		}	    
	}
	
}
