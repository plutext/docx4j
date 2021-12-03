/* NOTICE: This file has been changed by Plutext Pty Ltd for use in docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
 
 /* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.docx4j.org.apache.poi.util;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.events.Namespace;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class DocumentHelper {
	
    // must only be used to create empty documents, do not use it for parsing!
    private static final DocumentBuilder documentBuilderSingleton = newDocumentBuilder();

    private DocumentHelper() {}

    /**
     * Creates a new document builder, with sensible defaults
     *
     * @throws IllegalStateException If creating the DocumentBuilder fails, e.g.
     *  due to {@link ParserConfigurationException}.
     */
    public static DocumentBuilder newDocumentBuilder() {
        return XMLHelper.newDocumentBuilder();
    }

    /**
     * Parses the given stream via the default (sensible)
     * DocumentBuilder
     * @param inp Stream to read the XML data from
     * @return the parsed Document
     */
    public static Document readDocument(InputStream inp) throws IOException, SAXException {
        return newDocumentBuilder().parse(inp);
    }

    /**
     * Parses the given stream via the default (sensible)
     * DocumentBuilder
     * @param inp sax source to read the XML data from
     * @return the parsed Document 
     */
    public static Document readDocument(InputSource inp) throws IOException, SAXException {
        return newDocumentBuilder().parse(inp);
    }

    /**
     * Creates a new DOM Document
     */
    public static Document createDocument() {
        return documentBuilderSingleton.newDocument();
    }

    /**
     * Adds a namespace declaration attribute to the given element.
     */
    public static void addNamespaceDeclaration(Element element, String namespacePrefix, String namespaceURI) {
        element.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
                XMLConstants.XMLNS_ATTRIBUTE + ':' + namespacePrefix,
                namespaceURI);
    }

    /**
     * Adds a namespace declaration attribute to the given element.
     */
    public static void addNamespaceDeclaration(Element element, Namespace namespace) {
        addNamespaceDeclaration(element, namespace.getPrefix(), namespace.getNamespaceURI());
    }
}