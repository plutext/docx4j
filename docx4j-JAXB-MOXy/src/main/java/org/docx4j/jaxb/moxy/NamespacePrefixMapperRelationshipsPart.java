/*
 *  Copyright 2007-2018, Plutext Pty Ltd.
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

package org.docx4j.jaxb.moxy;

import org.docx4j.jaxb.McIgnorableNamespaceDeclarator;
import org.docx4j.jaxb.NamespacePrefixMapperInterface;

/**
 * @author jharrop
 * @since 6.1.0
 */
public class NamespacePrefixMapperRelationshipsPart 
	extends org.eclipse.persistence.oxm.NamespacePrefixMapper  implements NamespacePrefixMapperInterface, McIgnorableNamespaceDeclarator {
	
//	private String mcIgnorable;
	public void setMcIgnorable(String mcIgnorable) {
//		this.mcIgnorable = mcIgnorable;
	}
	
    /**
     * Returns a preferred prefix for the given namespace URI;
     * this one is used *only* when we marshal the relationships part.
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
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) 
    // Implement the interface    
    {

    	return getPreferredPrefixStatic( namespaceUri,  suggestion,  requirePrefix);
    }
    
    protected static String getPreferredPrefixStatic(String namespaceUri, String suggestion, boolean requirePrefix) {
    
//    	if (namespaceUri.equals("http://schemas.openxmlformats.org/wordprocessingml/2006/main")) {
//		return "w";
//	}
//	if (namespaceUri.equals("http://schemas.microsoft.com/office/2006/xmlPackage")) {
//		return "pkg";
//	}
//	
//	if (namespaceUri.equals("http://schemas.openxmlformats.org/officeDocument/2006/custom-properties")) {
//		return "prop";
//	}
//	
//	if (namespaceUri.equals("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes")) {
//		return "vt";
//	}
	    	
	if (namespaceUri.equals("http://schemas.openxmlformats.org/package/2006/relationships")) {
		return ""; // Make it the default namespace
	}
	
//	if (namespaceUri.equals("http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing")) {
//		return "wp";
//	}
//	
//	if (namespaceUri.equals("http://schemas.openxmlformats.org/drawingml/2006/main")) {
//		return "a";
//	}
//	
//	if (namespaceUri.equals("http://schemas.openxmlformats.org/drawingml/2006/picture")) {
//		return "pic";
//	}
    	
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

    
}
