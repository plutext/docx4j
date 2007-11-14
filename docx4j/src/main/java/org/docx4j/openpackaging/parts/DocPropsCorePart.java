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

package org.docx4j.openpackaging.parts;

import java.net.URI;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import org.docx4j.document.wordprocessingml.Constants;
import org.docx4j.document.wordprocessingml.Paragraph;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.OpenXML4JException;
import org.docx4j.openpackaging.packages.Package;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentPart;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;


//import org.openxml4j.opc.Package;
//import org.openxml4j.opc.PackagePart;
//import org.openxml4j.opc.PackagingURIHelper;
//import org.openxml4j.opc.StreamHelper;
//
//import org.openxml4j.opc.PackagePartName;
//import org.openxml4j.opc.internal.marshallers.ZipPartMarshaller;


public class DocPropsCorePart extends AbstractDocPropsPart  {
	
	/*
	 * <?xml version="1.0" encoding="UTF-8" standalone="yes"?> 
	 * <cp:coreProperties 
		 * xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" 
		 * xmlns:dc="http://purl.org/dc/elements/1.1/" 
		 * xmlns:dcterms="http://purl.org/dc/terms/" 
		 * xmlns:dcmitype="http://purl.org/dc/dcmitype/" 
		 * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
		 * <dc:creator>Jason Harrop</dc:creator>
		 * <cp:lastModifiedBy>Jason Harrop</cp:lastModifiedBy>
		 * <cp:revision>2</cp:revision>
		 * <dcterms:created xsi:type="dcterms:W3CDTF">2007-08-12T00:34:00Z</dcterms:created>
		 * <dcterms:modified xsi:type="dcterms:W3CDTF">2007-08-12T00:34:00Z</dcterms:modified>
	 * </cp:coreProperties>
	 */
	
	
	private static Logger log = Logger.getLogger(DocPropsCorePart.class);
	
	 /** 
	 * @throws InvalidFormatException
	 */
	//public MainDocumentPart(Package pack, PackagePartName partUri) {
	public DocPropsCorePart(PartName partName) throws InvalidFormatException {
		super(partName);
	}

	public void setDocument(Document document) {
		this.document = document;
		//unmarshall(document);
		
		// TODO - unmarshall this
	}
	
	public Document getDocument() {
		
		return document;
		
		// TODO - marshall this
		//return marshall();
	}
			
	
}

	
