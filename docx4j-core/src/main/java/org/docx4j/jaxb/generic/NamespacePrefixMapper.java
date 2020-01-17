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

package org.docx4j.jaxb.generic;

import org.docx4j.jaxb.McIgnorableNamespaceDeclarator;
import org.docx4j.jaxb.NamespacePrefixMapperInterface;
import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.jaxb.NamespacePrefixMappings;

/**
 * NamespacePrefixMapper, which could be used by MOXy
 * (relying on org.eclipse.persistence.internal.oxm.record.namespaces.NamespacePrefixMapperWrapper)
 * 
 * But instead (to avoid additional reflection), we use org.docx4j.jaxb.moxy.NamespacePrefixMapper
 * 
 * So this class isn't currently used.
 * 
 * @author jharrop
 *
 */
public class NamespacePrefixMapper implements NamespacePrefixMapperInterface, McIgnorableNamespaceDeclarator {

	private String mcIgnorable;
	public void setMcIgnorable(String mcIgnorable) {
		this.mcIgnorable = mcIgnorable;
	}

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
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) 
    // Implement the interface
    {    	
    	return NamespacePrefixMappings.getPreferredPrefixStatic(namespaceUri, suggestion, requirePrefix);    	
    }

// Where used???    
//    public static String getPreferredPrefix(String namespaceUri) 
//    // Implement the interface
//    {    	
//    	return NamespacePrefixMappings.getPreferredPrefixStatic(namespaceUri, null, false);    	
//    }
    
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
    public String[] getPreDeclaredNamespaceUris() {
    	return NamespacePrefixMapperUtils.getPreDeclaredNamespaceUris(mcIgnorable);
    }

	@Override
	public String[] getPreDeclaredNamespaceUris2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getContextualNamespaceDecls() {
		// TODO Auto-generated method stub
		return null;
	}
    
    /**
     * Similar to {@link #getPreDeclaredNamespaceUris()} but allows the
     * (prefix,nsUri) pairs to be returned.
     *
     * <p>
     * With {@link #getPreDeclaredNamespaceUris()}, applications who wish to control
     * the prefixes as well as the namespaces needed to implement both
     * {@link #getPreDeclaredNamespaceUris()} and {@link #getPreferredPrefix(String, String, boolean)}.
     *
     * <p>
     * This version eliminates the needs by returning an array of pairs.
     *
     * @return
     *      always return a non-null (but possibly empty) array. The array stores
     *      data like (prefix1,nsUri1,prefix2,nsUri2,...) Use an empty string to represent
     *      the empty namespace URI and the default prefix. Null is not allowed as a value
     *      in the array.
     *
     * @since
     *      JAXB RI 2.0 beta
     */
//    public String[] getPreDeclaredNamespaceUris2() {    	
//      return EMPTY_STRING;    	
//    }
    
    /*  WARNING: don't use getPreDeclaredNamespaceUris2; it is buggy, at least in Java 1.6.0_27
     * 
		Attribute "xmlns:w14" was already specified for element "w:document".
		org.xml.sax.SAXParseException: Attribute "xmlns:w14" was already specified for element "w:document".
			at com.sun.org.apache.xerces.internal.parsers.DOMParser.parse(Unknown Source)
			at com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderImpl.parse(Unknown Source)
			at org.docx4j.org.apache.xml.security.utils.XMLUtils$DocumentBuilderProxy.parse(XMLUtils.java:1140)
			at org.docx4j.org.apache.xml.security.c14n.Canonicalizer.canonicalize(Canonicalizer.java:286)
	     */

    /**
     * Returns a list of (prefix,namespace URI) pairs that represents
     * namespace bindings available on ancestor elements (that need not be repeated
     * by the JAXB RI.)
     *
     * <p>
     * Sometimes JAXB is used to marshal an XML document, which will be
     * used as a subtree of a bigger document. When this happens, it's nice
     * for a JAXB marshaller to be able to use in-scope namespace bindings
     * of the larger document and avoid declaring redundant namespace URIs.
     *
     * <p>
     * This is automatically done when you are marshalling to {@link XMLStreamWriter},
     * {@link XMLEventWriter}, {@link DOMResult}, or {@link Node}, because
     * those output format allows us to inspect what's currently available
     * as in-scope namespace binding. However, with other output format,
     * such as {@link OutputStream}, the JAXB RI cannot do this automatically.
     * That's when this method comes into play.
     *
     * <p>
     * Namespace bindings returned by this method will be used by the JAXB RI,
     * but will not be re-declared. They are assumed to be available when you insert
     * this subtree into a bigger document.
     *
     * <p>
     * It is <b>NOT</b> OK to return  the same binding, or give
     * the receiver a conflicting binding information.
     * It's a responsibility of the caller to make sure that this doesn't happen
     * even if the ancestor elements look like:
     * <pre><xmp>
     *   <foo:abc xmlns:foo="abc">
     *     <foo:abc xmlns:foo="def">
     *       <foo:abc xmlns:foo="abc">
     *         ... JAXB marshalling into here.
     *       </foo:abc>
     *     </foo:abc>
     *   </foo:abc>
     * </xmp></pre>
     *
     * @return
     *      always return a non-null (but possibly empty) array. The array stores
     *      data like (prefix1,nsUri1,prefix2,nsUri2,...) Use an empty string to represent
     *      the empty namespace URI and the default prefix. Null is not allowed as a value
     *      in the array.
     *
     * @since JAXB RI 2.0 beta
     */
//    public String[] getContextualNamespaceDecls() {
//        return EMPTY_STRING;
//    }    
    
}
