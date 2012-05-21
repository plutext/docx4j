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

package org.docx4j.openpackaging.parts.WordprocessingML;


import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;



public final class AlternativeFormatInputPart extends BinaryPart {
	
	public AlternativeFormatInputPart(PartName partName) throws InvalidFormatException {
		super(partName);		
		 
		if (partName.getExtension().toLowerCase().equals("xhtml") )  {
			this.type = AltChunkType.Xhtml;
		} else if (partName.getExtension().toLowerCase().equals("mht") )  {
			this.type = AltChunkType.Mht;
		} else if (partName.getExtension().toLowerCase().equals("xml") )  {
			this.type = AltChunkType.Xml;
		} else if (partName.getExtension().toLowerCase().equals("txt") )  {
			this.type = AltChunkType.TextPlain;
		} else if (partName.getExtension().toLowerCase().equals("docx") )  {
			this.type = AltChunkType.WordprocessingML;
		} else if (partName.getExtension().toLowerCase().equals("docx") )  {
			this.type = AltChunkType.OfficeWordMacroEnabled;
		} else if (partName.getExtension().toLowerCase().equals("dotx") )  {
			this.type = AltChunkType.OfficeWordTemplate;
		} else if (partName.getExtension().toLowerCase().equals("dotm") )  {
			this.type = AltChunkType.OfficeWordMacroEnabledTemplate;
		} else if (partName.getExtension().toLowerCase().equals("rtf") )  {
			this.type = AltChunkType.Rtf;
		} else if (partName.getExtension().toLowerCase().equals("html") )  {
			this.type = AltChunkType.Html;
		}		
		setContentType();
		
		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.AF);
		
	}
	
	AltChunkType type;
	public AltChunkType getAltChunkType() {
		return type;
	}

	public AlternativeFormatInputPart(AltChunkType type) throws InvalidFormatException {
				
		super( new PartName("/chunk"));
		
		this.type = type;
		if (type.equals(AltChunkType.Xhtml) ) {
			this.partName = new PartName("/chunk.xhtml");
		} else if (type.equals(AltChunkType.Mht) ) {
			this.partName = new PartName("/chunk.mht");
		} else if (type.equals(AltChunkType.Xml) ) {
			this.partName = new PartName("/chunk.xml");
		} else if (type.equals(AltChunkType.TextPlain) ) {
			this.partName = new PartName("/chunk.txt");
		} else if (type.equals(AltChunkType.WordprocessingML) ) { //Docx
			this.partName = new PartName("/chunk.docx");
		} else if (type.equals(AltChunkType.OfficeWordMacroEnabled) ) {
			this.partName = new PartName("/chunk.docm");
		} else if (type.equals(AltChunkType.OfficeWordTemplate) ) {
			this.partName = new PartName("/chunk.dotx");
		} else if (type.equals(AltChunkType.OfficeWordMacroEnabledTemplate) ) {
			this.partName = new PartName("/chunk.dotm");
		} else if (type.equals(AltChunkType.Rtf) ) {
			this.partName = new PartName("/chunk.rtf");
		} else if (type.equals(AltChunkType.Html) ) {
			this.partName = new PartName("/chunk.html");
		}
		 
		// ContentType will vary - see spec 11.3.1 
		setContentType();
		
		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.AF);
		
	}
	
	private void setContentType() {
		
		if (type.equals(AltChunkType.Xhtml) ) {
			this.setContentType(new ContentType("application/xhtml+xml"));
			// Alternatively, you can serve your XHTML (any version) as application/xml, or even as text/xml
			// but which is right for Word?
		} else if (type.equals(AltChunkType.Mht) ) {
			this.setContentType(new ContentType("multipart/related"));
		} else if (type.equals(AltChunkType.Xml) ) {
			this.setContentType(new ContentType("application/xml")); // or text/xml?
		} else if (type.equals(AltChunkType.TextPlain) ) {
			this.setContentType(new ContentType("text/plain")); 
		} else if (type.equals(AltChunkType.WordprocessingML) ) { //Docx
			this.setContentType(new ContentType(ContentTypes.WORDPROCESSINGML_DOCUMENT)); 
		} else if (type.equals(AltChunkType.OfficeWordMacroEnabled) ) {
			this.setContentType(new ContentType(ContentTypes.WORDPROCESSINGML_DOCUMENT_MACROENABLED)); 
		} else if (type.equals(AltChunkType.OfficeWordTemplate) ) {
			this.setContentType(new ContentType(ContentTypes.WORDPROCESSINGML_TEMPLATE));
		} else if (type.equals(AltChunkType.OfficeWordMacroEnabledTemplate) ) {
			this.setContentType(new ContentType(ContentTypes.WORDPROCESSINGML_TEMPLATE_MACROENABLED));
		} else if (type.equals(AltChunkType.Rtf) ) {
			this.setContentType(new ContentType("text/rtf"));
		} else if (type.equals(AltChunkType.Html) ) {
			this.setContentType(new ContentType("text/html"));
		}
		
	}

	public void registerInContentTypeManager() {
		
		ContentTypeManager ctm = this.getPackage().getContentTypeManager(); 
		if (type.equals(AltChunkType.Xhtml) ) {
			ctm.addDefaultContentType("xhtml", "application/xhtml+xml");
		} else if (type.equals(AltChunkType.Mht) ) {
			ctm.addDefaultContentType("mht", "multipart/related");
		} else if (type.equals(AltChunkType.Xml) ) {
			ctm.addDefaultContentType("xml", "application/xml");
		} else if (type.equals(AltChunkType.TextPlain) ) {
			ctm.addDefaultContentType("txt", "text/plain");
		} else if (type.equals(AltChunkType.WordprocessingML) ) { //Docx
			// In case we're being added to a docm/dotx/dotm
			ctm.addDefaultContentType("docx", ContentTypes.WORDPROCESSINGML_DOCUMENT);
		} else if (type.equals(AltChunkType.OfficeWordMacroEnabled) ) {
			ctm.addDefaultContentType("docm", ContentTypes.WORDPROCESSINGML_DOCUMENT_MACROENABLED);
		} else if (type.equals(AltChunkType.OfficeWordTemplate) ) {
			ctm.addDefaultContentType("dotx", ContentTypes.WORDPROCESSINGML_TEMPLATE);
		} else if (type.equals(AltChunkType.OfficeWordMacroEnabledTemplate) ) {
			ctm.addDefaultContentType("dotm", ContentTypes.WORDPROCESSINGML_TEMPLATE_MACROENABLED);
		} else if (type.equals(AltChunkType.Rtf) ) {
			ctm.addDefaultContentType("rtf", "text/rtf");
		} else if (type.equals(AltChunkType.Html) ) {
			ctm.addDefaultContentType("html", "text/html");
		}
		
	}
}
