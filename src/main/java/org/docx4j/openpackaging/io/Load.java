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

package org.docx4j.openpackaging.io;

import java.io.StringWriter;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.docx4j.document.wordprocessingml.Constants;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.Package;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;


public class Load {

	private static Logger log = Logger.getLogger(Load.class);
	
	public Load() {
		super();
	}

	public ContentTypeManager ctm;

	protected void debugPrint( Document coreDoc) {
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
			
			StringWriter sWriter = new StringWriter();			 					
		    XMLWriter xmlWriter = new XMLWriter( sWriter, format );
		    xmlWriter.write( coreDoc );
		    log.warn(sWriter.toString() );			
		} catch (Exception e ) {
			e.printStackTrace();
		}	    
	}

}