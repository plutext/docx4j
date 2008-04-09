/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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