/*
   Licensed to Plutext Pty Ltd under one or more contributor license agreements.  
   
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
package org.docx4j.convert.out.fo;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class PlaceholderReplacementHandler extends DefaultHandler {
	public interface PlaceholderLookup {
		public boolean hasPlaceholders(StringBuilder buffer);
		public void replaceValues(StringBuilder buffer);
	}
	
	protected PlaceholderLookup placeholderLookup = null;
	protected DefaultHandler defaultHandler = null;

	protected StringBuilder buffer = new StringBuilder();
	protected char[] tmpCharArray = new char[10240];
	
	public PlaceholderReplacementHandler(DefaultHandler defaultHandler, PlaceholderLookup variableLookup) {
		this.placeholderLookup = variableLookup;
		this.defaultHandler = defaultHandler;
	}

	@Override
	public int hashCode() {
		return defaultHandler.hashCode();
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId)
			throws IOException, SAXException {
		return defaultHandler.resolveEntity(publicId, systemId);
	}

	@Override
	public void notationDecl(String name, String publicId, String systemId)
			throws SAXException {
		defaultHandler.notationDecl(name, publicId, systemId);
	}

	@Override
	public boolean equals(Object obj) {
		return defaultHandler.equals(obj);
	}

	@Override
	public void unparsedEntityDecl(String name, String publicId,
			String systemId, String notationName) throws SAXException {
		defaultHandler.unparsedEntityDecl(name, publicId, systemId,
				notationName);
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		defaultHandler.setDocumentLocator(locator);
	}

	@Override
	public void startDocument() throws SAXException {
		defaultHandler.startDocument();
	}

	@Override
	public void endDocument() throws SAXException {
		defaultHandler.endDocument();
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		defaultHandler.startPrefixMapping(prefix, uri);
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		defaultHandler.endPrefixMapping(prefix);
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		defaultHandler.startElement(uri, localName, qName, attributes);
	}

	@Override
	public String toString() {
		return defaultHandler.toString();
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (buffer.length() > 0) {
			if (placeholderLookup.hasPlaceholders(buffer)) {
				placeholderLookup.replaceValues(buffer);
			}
			if (buffer.length() > 0) {
				if (buffer.length() > tmpCharArray.length) {
					tmpCharArray = new char[(buffer.length() / 1024 + 1) * 1024];
				}
				buffer.getChars(0, buffer.length(), tmpCharArray, 0);
				defaultHandler.characters(tmpCharArray, 0, buffer.length());
				buffer.setLength(0);
			}
		}
		defaultHandler.endElement(uri, localName, qName);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		buffer.append(ch, start, length);
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		defaultHandler.ignorableWhitespace(ch, start, length);
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		defaultHandler.processingInstruction(target, data);
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		defaultHandler.skippedEntity(name);
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		defaultHandler.warning(e);
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		defaultHandler.error(e);
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		defaultHandler.fatalError(e);
	}
	
	
	
	
}
