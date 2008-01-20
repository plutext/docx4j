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

	protected static void debugPrint( Document coreDoc) {
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