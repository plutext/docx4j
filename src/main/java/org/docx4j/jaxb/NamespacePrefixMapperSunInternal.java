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

package org.docx4j.jaxb;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.docx4j.openpackaging.parts.relationships.Namespaces;

public class NamespacePrefixMapperSunInternal extends com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper 
	implements NamespaceContext{
	// Must use 'internal' for Java 6

	
    /**
     * Returns a preferred prefix for the given namespace URI.
     * 
     * This method is intended to be overrided by a derived class.
     * 
     * @param namespaceUri
     *      The namespace URI for which the prefix needs to be found.
     *      Never be null. "" is used to denote the default namespace.
     * @param suggestion
     *      When the content tree has a suggestion for the prefix
     *      to the given namespaceUri, that suggestion is passed as a
     *      parameter. Typically this value comes from QName.getPrefix()
     *      to show the preference of the content tree. This parameter
     *      may be null, and this parameter may represent an already
     *      occupied prefix. 
     * @param requirePrefix
     *      If this method is expected to return non-empty prefix.
     *      When this flag is true, it means that the given namespace URI
     *      cannot be set as the default namespace.
     * 
     * @return
     *      null if there's no preferred prefix for the namespace URI.
     *      In this case, the system will generate a prefix for you.
     * 
     *      Otherwise the system will try to use the returned prefix,
     *      but generally there's no guarantee if the prefix will be
     *      actually used or not.
     * 
     *      return "" to map this namespace URI to the default namespace.
     *      Again, there's no guarantee that this preference will be
     *      honored.
     * 
     *      If this method returns "" when requirePrefix=true, the return
     *      value will be ignored and the system will generate one.
     */
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {    
    	
    	if (namespaceUri.equals(Namespaces.NS_WORD12)) {
    		return "w";
    	}
    	if (namespaceUri.equals(Namespaces.PKG_XML)) {
    		return "pkg";
    	}
    	
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/officeDocument/2006/custom-properties")) {
    		return "prop";
    	}

    	if (namespaceUri.equals("http://schemas.openxmlformats.org/officeDocument/2006/extended-properties")) {
    		return "properties";
    	}
    	
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/package/2006/metadata/core-properties")) {
    		return "cp";
    	}

    	
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes")) {
    		return "vt";
    	}
    	    	
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/package/2006/relationships")) {
    		return "rel";
    	}

    	if (namespaceUri.equals(Namespaces.RELATIONSHIPS_OFFICEDOC)) {
    		return "r";
    	}
    	
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing")) {
    		return "wp";
    	}
    	
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/drawingml/2006/main")) {
    		return "a";
    	}
    	
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/drawingml/2006/picture")) {
    		return "pic";
    	}

    	if (namespaceUri.equals("urn:schemas-microsoft-com:office:office")) {
    		return "o";
    	}
    	
    	if (namespaceUri.equals("urn:schemas-microsoft-com:vml")) {
    		return "v";
    	}

    	if (namespaceUri.equals("http://schemas.microsoft.com/office/word/2003/auxHint")) {
    		return "WX";
    	}

    	if (namespaceUri.equals("http://schemas.microsoft.com/aml/2001/core")) {
    		return "aml";
    	}

    	if (namespaceUri.equals("urn:schemas-microsoft-com:office:word")) {
    		return "w10";
    	}

    	if (namespaceUri.equals("http://schemas.openxmlformats.org/officeDocument/2006/math")) {
    		return "m";
    	}
    	
    	if (namespaceUri.equals("http://www.w3.org/2001/XMLSchema-instance")) {
    		return "xsi";
    	}

    	if (namespaceUri.equals("http://purl.org/dc/elements/1.1/")) {
    		return "dc";
    	}
    	if (namespaceUri.equals("http://purl.org/dc/terms/")) {
    		return "dcterms";
    	}
    	
    	if (namespaceUri.equals("http://www.w3.org/XML/1998/namespace")) {
    		return "xml";
    	}
    	
    	
    	return suggestion;
    }
    
       
    
    /**
     * Returns a list of namespace URIs that should be declared
     * at the root element.
     * <p>
     * By default, the JAXB RI produces namespace declarations only when
     * they are necessary, only at where they are used. Because of this
     * lack of look-ahead, sometimes the marshaller produces a lot of
     * namespace declarations that look redundant to human eyes. For example,
     * <pre><xmp>
     * <?xml version="1.0"?>
     * <root>
     *   <ns1:child xmlns:ns1="urn:foo"> ... </ns1:child>
     *   <ns2:child xmlns:ns2="urn:foo"> ... </ns2:child>
     *   <ns3:child xmlns:ns3="urn:foo"> ... </ns3:child>
     *   ...
     * </root>
     * <xmp></pre>
     * <p>
     * If you know in advance that you are going to use a certain set of
     * namespace URIs, you can override this method and have the marshaller
     * declare those namespace URIs at the root element. 
     * <p>
     * For example, by returning <code>new String[]{"urn:foo"}</code>,
     * the marshaller will produce:
     * <pre><xmp>
     * <?xml version="1.0"?>
     * <root xmlns:ns1="urn:foo">
     *   <ns1:child> ... </ns1:child>
     *   <ns1:child> ... </ns1:child>
     *   <ns1:child> ... </ns1:child>
     *   ...
     * </root>
     * <xmp></pre>
     * <p>
     * To control prefixes assigned to those namespace URIs, use the
     * {@link #getPreferredPrefix} method. 
     * 
     * @return
     *      A list of namespace URIs as an array of {@link String}s.
     *      This method can return a length-zero array but not null.
     *      None of the array component can be null. To represent
     *      the empty namespace, use the empty string <code>""</code>.
     * 
     * @since
     *      JAXB RI 1.0.2 
     */
//    public String[] getPreDeclaredNamespaceUris() {
//        return new String[] { "urn:abc", "urn:def" };
//    }

    // ----------------------------------------------------
    // implement NamespaceContext,
    // for use with for use with javax.xml.xpath
    
	public String getNamespaceURI(String prefix) {
		
		if (prefix.equals("w"))  
			return Namespaces.NS_WORD12;
		else if (prefix.equals("r"))
			return Namespaces.RELATIONSHIPS_OFFICEDOC;
		else if (prefix.equals("pkg"))
			return Namespaces.PKG_XML;
		else
			return XMLConstants.NULL_NS_URI;
		
	}

	public String getPrefix(String namespaceURI) {
		
		return getPreferredPrefix(namespaceURI, null, false );
		
//		if (namespaceURI.equals(Namespaces.NS_WORD12))
//			return "w";
//		else if (namespaceURI.equals(Namespaces.RELATIONSHIPS_OFFICEDOC))
//			return "r";
//		else if (namespaceURI.equals(Namespaces.PKG_XML))
//			return "pkg";
//		else return null;
	}

	public Iterator getPrefixes(String namespaceURI) {
		return null;
	}
    
}
