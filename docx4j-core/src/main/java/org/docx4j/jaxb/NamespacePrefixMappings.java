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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.apache.commons.lang3.text.StrTokenizer;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.w3c.dom.Node;

/**
 * NamespacePrefixMappings, required for JAXB, and XPath.
 * 
 * The intent is to define the namespace prefix mappings in a 
 * single place.
 * 
 * This class implements NamespaceContext, so it can be used as follows:
 * 
 *  	XPathFactory factory = XPathFactory.newInstance();
 *		XPath xPath = factory.newXPath();
 *		xPath.setNamespaceContext(new NamespacePrefixMappings());
 * 
 * For JAXB, NamespacePrefixMapper (for RI) and NamespacePrefixMapperSunInternal
 * (for Java 6) both refer to this class.
 * 
 * @author jharrop
 *
 */
public class NamespacePrefixMappings implements NamespaceContext, org.docx4j.org.apache.xml.utils.PrefixResolver {

/* TODO: check what we have here against https://github.com/OfficeDev/Open-XML-SDK/blob/vNext/DocumentFormat.OpenXml/src/Framework/NamespaceIdMap.cs
 */

    /**
     * Returns a preferred prefix for the given namespace URI.
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
    protected static String getPreferredPrefixStatic(String namespaceUri, String suggestion, boolean requirePrefix) {    
    	
    	if (namespaceUri.equals(Namespaces.NS_WORD12)) {
    		return "w";
    	}
    	if (namespaceUri.equals(Namespaces.PKG_XML)) {
    		return "pkg";
    	}

    	if (namespaceUri.equals("http://schemas.openxmlformats.org/presentationml/2006/main")) {
    		return "p";
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
    	
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/package/2006/digital-signature")) {
    		return "mdssi";
    	}    	
    	
    	// DrawingML 
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing")) {
    		return "wp";
    	}    	
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/drawingml/2006/chart")) {
    		return "c";
    	}
    	if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2007/8/2/chart")) {
    		return "c14";
    	}    	
    	
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/officeDocument/2006/bibliography")) {
    		return "b";
    	}    	
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/drawingml/2006/main")) {
    		return "a";
    	}    	
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/drawingml/2006/picture")) {
    		return "pic";
    	}
		if (namespaceUri.equals("http://schemas.openxmlformats.org/drawingml/2006/diagram")) {
    		return "dgm";
		}  
    	
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2008/diagram")) {
    		return "dsp";
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

    	if (namespaceUri.equals("http://schemas.microsoft.com/office/word/2006/wordml")) {
    		return "wne";
    	}

    	if (namespaceUri.equals("http://schemas.microsoft.com/office/word/2010/wordml")) {
    		return "w14";
    	}
    	
    	if (namespaceUri.equals("http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas")) {
    		return "wpc";
    	}

    	if (namespaceUri.equals("http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing")) {
    		return "wp14";
    	}

    	if (namespaceUri.equals("http://schemas.microsoft.com/office/word/2010/wordprocessingGroup")) {
    		return "wpg";
    	}
    	
    	if (namespaceUri.equals("http://schemas.microsoft.com/office/word/2010/wordprocessingInk")) {
    		return "wpi";
    	}

    	if (namespaceUri.equals("http://schemas.microsoft.com/office/word/2010/wordprocessingShape")) {
    		return "wps";
    	}
    	
    	if (namespaceUri.equals("http://schemas.microsoft.com/office/word/2012/wordml")) {
    		return "w15";
    	}
    	
    	if (namespaceUri.equals("http://schemas.microsoft.com/office/word/2016/wordml/cid")) {
    		return "w16cid";
    	}
    	
    	if (namespaceUri.equals("http://schemas.microsoft.com/office/word/2015/wordml/symex")) {
    		return "w16se";
    	}

    	if (namespaceUri.equals("http://schemas.microsoft.com/office/webextensions/taskpanes/2010/11")) {
    		return "wetp";
    	}

    	if (namespaceUri.equals("http://schemas.microsoft.com/office/webextensions/webextension/2010/11")) {
    		return "we";
    	}
    	    	
    	if (namespaceUri.equals("http://schemas.microsoft.com/aml/2001/core")) {
    		return "aml";
    	}

    	if (namespaceUri.equals("urn:schemas-microsoft-com:office:word")) {
    		return "w10";
    	}
    	
    	if (namespaceUri.equals("urn:schemas-microsoft-com:office:excel")) {
    		return "xvml";
    	}
    	if (namespaceUri.equals("urn:schemas-microsoft-com:office:powerpoint")) {
    		return "pvml";
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
    	
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/officeDocument/2006/customXml")) {
    		return "ds";
    	}

    	// OpenDoPE
    	if (namespaceUri.equals("http://opendope.org/xpaths")) {
        	return "odx";
    	} 
		if (namespaceUri.equals("http://opendope.org/conditions")) {
    		return "odc";
		}  
		if (namespaceUri.equals("http://opendope.org/components")) {
    		return "odi";
		}  
		if (namespaceUri.equals("http://opendope.org/questions")) {
    		return "odq";
		}
		if (namespaceUri.equals("http://opendope.org/answers")) {
    		return "oda";
		}
		
		if (namespaceUri.equals("http://opendope.org/SmartArt/DataHierarchy")) {
    		return "odgm";
		}  
		
//		if (namespaceUri.equals("urn:schemas-microsoft-com:office:excel")) {
//    		return "?";
//		}  
//		if (namespaceUri.equals("http://schemas.openxmlformats.org/drawingml/2006/compatibility")) {
//    		return "?";
//		}  
//		if (namespaceUri.equals("http://schemas.openxmlformats.org/drawingml/2006/lockedCanvas")) {
//    		return "?";
//		}  
//		if (namespaceUri.equals("http://schemas.openxmlformats.org/drawingml/2006/chartDrawing")) {
//    		return "?";
//		}  
		if (namespaceUri.equals("http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing")) {
    		return "xdr";
		}  
//		if (namespaceUri.equals("http://schemas.openxmlformats.org/officeDocument/2006/bibliography")) {
//    		return "?";
//		}  
//		if (namespaceUri.equals("http://schemas.openxmlformats.org/schemaLibrary/2006/main")) {
//    		return "?";
//		}  
    	
    	if (namespaceUri.equals(Namespaces.XFORMS)) {
    		return "xf";
    	}
    	if (namespaceUri.equals(Namespaces.XML_EVENTS)) {
    		return "xe";
    	}
    	if (namespaceUri.equals(Namespaces.XML_SCHEMA)) {
    		return "xs";
    	}
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/markup-compatibility/2006")) {
    		return "mc";
    	}

    	if (namespaceUri.equals("http://uri.etsi.org/01903/v1.3.2#")) {
    		return "xd";
    	}
    	
    	if (namespaceUri.equals("http://schemas.microsoft.com/office/2006/digsig")) {
    		return "dssi";
    	}
    	
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/schemaLibrary/2006/main")) {
    		return "sl";
    	}
    	if (namespaceUri.equals("http://schemas.microsoft.com/office/2006/coverPageProps" )) {
    		return "cppr";
    	}
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/drawingml/2006/chartDrawing")) {
    		return "cdr";
    	}
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/drawingml/2006/compatibility" )) {
    		return "comp";
    	}
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/drawingml/2006/lockedCanvas")) {
    		return "lc";
    	}
    	if (namespaceUri.equals("urn:schemas-microsoft-com:mac:vml")) {
    		return "mv";
    	}
    	
    	
    	return suggestion;
    }
    
       

    // ----------------------------------------------------
    // implement NamespaceContext,
    // for use with for use with javax.xml.xpath
    
	public String getNamespaceURI(String prefix) {  // implementing NamespaceContext
		
		// Excel uses a default namespace, not a prefix.  But it is convenient
		// to be able to use a prefix in XPath
		if (prefix.equals("s"))
			return "http://schemas.openxmlformats.org/spreadsheetml/2006/main";				
		
		return getNamespaceURIStatic(prefix);
	}
	
	protected static String getNamespaceURIStatic(String prefix) {

		// Pre-defined prefixes
		if (prefix.equals("w"))
			return Namespaces.NS_WORD12;
		if (prefix.equals("r"))
			return Namespaces.RELATIONSHIPS_OFFICEDOC;
		if (prefix.equals("pkg"))
			return Namespaces.PKG_XML;

		if (prefix.equals("p"))
			return "http://schemas.openxmlformats.org/presentationml/2006/main";

		if (prefix.equals("prop"))
			return "http://schemas.openxmlformats.org/officeDocument/2006/custom-properties";

		if (prefix.equals("properties"))
			return "http://schemas.openxmlformats.org/officeDocument/2006/extended-properties";

		if (prefix.equals("cp"))
			return "http://schemas.openxmlformats.org/package/2006/metadata/core-properties";

		if (prefix.equals("vt"))
			return "http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes";

		if (prefix.equals("rel"))
			return "http://schemas.openxmlformats.org/package/2006/relationships";
		
		if (prefix.equals("mdssi"))
			return "http://schemas.openxmlformats.org/package/2006/digital-signature";
		

		// DrawingML
		if (prefix.equals("wp"))
			return "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing";

		if (prefix.equals("c"))
			return "http://schemas.openxmlformats.org/drawingml/2006/chart";
		
		if (prefix.equals("c14"))
			return "http://schemas.microsoft.com/office/drawing/2007/8/2/chart";

		if (prefix.equals("b"))
			return "http://schemas.openxmlformats.org/officeDocument/2006/bibliography";
		
		if (prefix.equals("a"))
			return "http://schemas.openxmlformats.org/drawingml/2006/main";

		if (prefix.equals("pic"))
			return "http://schemas.openxmlformats.org/drawingml/2006/picture";

		if (prefix.equals("dgm"))
			return "http://schemas.openxmlformats.org/drawingml/2006/diagram";

		if (prefix.equals("dsp"))
			return "http://schemas.microsoft.com/office/drawing/2008/diagram";

		if (prefix.equals("o"))
			return "urn:schemas-microsoft-com:office:office";

		if (prefix.equals("v"))
			return "urn:schemas-microsoft-com:vml";

		if (prefix.equals("WX"))
			return "http://schemas.microsoft.com/office/word/2003/auxHint";
		
		if (prefix.equals("wne"))
			return "http://schemas.microsoft.com/office/word/2006/wordml";

		if (prefix.equals("w14"))
			return "http://schemas.microsoft.com/office/word/2010/wordml";
		
		if (prefix.equals("wpc"))
			return "http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas";
		
		if (prefix.equals("wp14"))
			return "http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing";
		
		if (prefix.equals("wpg"))
			return "http://schemas.microsoft.com/office/word/2010/wordprocessingGroup";
		
		if (prefix.equals("wpi"))
			return "http://schemas.microsoft.com/office/word/2010/wordprocessingInk";

		if (prefix.equals("wps"))
			return "http://schemas.microsoft.com/office/word/2010/wordprocessingShape";
		
		if (prefix.equals("w15"))
			return "http://schemas.microsoft.com/office/word/2012/wordml";
				
		if (prefix.equals("w16cid"))
				return "http://schemas.microsoft.com/office/word/2016/wordml/cid";

		if (prefix.equals("w16se"))
			return "http://schemas.microsoft.com/office/word/2015/wordml/symex";
		
		if (prefix.equals("wetp"))
    		return "http://schemas.microsoft.com/office/webextensions/taskpanes/2010/11";
    			
		if (prefix.equals("we"))
    		return "http://schemas.microsoft.com/office/webextensions/webextension/2010/11";
		
		if (prefix.equals("aml"))
			return "http://schemas.microsoft.com/aml/2001/core";

		if (prefix.equals("w10"))
			return "urn:schemas-microsoft-com:office:word";
		
		if (prefix.equals("xvml"))
			return "urn:schemas-microsoft-com:office:excel";	
		if (prefix.equals("pvml"))
			return "urn:schemas-microsoft-com:office:powerpoint" ;	
		
		if (prefix.equals("m"))
			return "http://schemas.openxmlformats.org/officeDocument/2006/math";

		if (prefix.equals("xsi"))
			return "http://www.w3.org/2001/XMLSchema-instance";

		if (prefix.equals("dc"))
			return "http://purl.org/dc/elements/1.1/";

		if (prefix.equals("dcterms"))
			return "http://purl.org/dc/terms/";

		if (prefix.equals("xml"))
			return "http://www.w3.org/XML/1998/namespace";

		if (prefix.equals("ds"))
			return "http://schemas.openxmlformats.org/officeDocument/2006/customXml";

		if (prefix.equals("mc"))
			return "http://schemas.openxmlformats.org/markup-compatibility/2006";	
		
		if (prefix.equals("xdr"))
			return "http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing";	
		
		
		
		// OpenDoPE
		if (prefix.equals("odx"))
			return "http://opendope.org/xpaths";
		if (prefix.equals("odc"))
			return "http://opendope.org/conditions";
		if (prefix.equals("odi"))
			return "http://opendope.org/components";
		if (prefix.equals("odq"))
			return "http://opendope.org/questions";
		if (prefix.equals("oda"))
			return "http://opendope.org/answers";
		if (prefix.equals("odgm"))
			return "http://opendope.org/SmartArt/DataHierarchy";		
		
		if (prefix.equals("xf"))  
			return Namespaces.XFORMS;
		else if (prefix.equals("xe"))
			return Namespaces.XML_EVENTS;
		else if (prefix.equals("xs"))
			return Namespaces.XML_SCHEMA;
		
		if (prefix.equals("xd"))
			return "http://uri.etsi.org/01903/v1.3.2#";

		if (prefix.equals("dssi"))
			return "http://schemas.microsoft.com/office/2006/digsig";
		
		if (prefix.equals("sl"))
			return "http://schemas.openxmlformats.org/schemaLibrary/2006/main";	
		if (prefix.equals("cppr"))
			return "http://schemas.microsoft.com/office/2006/coverPageProps" ;	
		if (prefix.equals("cdr"))
			return "http://schemas.openxmlformats.org/drawingml/2006/chartDrawing";	
		if (prefix.equals("comp"))
			return "http://schemas.openxmlformats.org/drawingml/2006/compatibility";	
		if (prefix.equals("lc"))
			return "http://schemas.openxmlformats.org/drawingml/2006/lockedCanvas";	

		if (prefix.equals("mv"))
			return "urn:schemas-microsoft-com:mac:vml";
		
		// Registered prefixes
		String result = namespaces.get(prefix);
		if (result==null) {
			return XMLConstants.NULL_NS_URI;
		} else {
			return result;
		}
		
	}

	public String getPrefix(String namespaceURI) {  // implementing NamespaceContext
		
    	// Excel uses a default namespace, not a prefix. 
    	if (namespaceURI.equals("http://schemas.openxmlformats.org/spreadsheetml/2006/main")) {
    		return "";
    	}
		
		return getPreferredPrefixStatic(namespaceURI, null, false );
		
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
	
	private static Map<String, String> namespaces = new HashMap<String, String>();	
	public static void registerPrefixMappings(String prefixMappings) {
		// eg  w:prefixMappings="xmlns:ns0='http://schemas.medchart'"
		// according to the spec, whitespace is the delimiter
		
		if (prefixMappings==null || prefixMappings.equals("") ) return;
		
		// we get one of these each time we encounter a w:dataBinding
		// element in a content control; pity it is not done just
		// once!
		
		// first tokenise on space
		StrTokenizer tokens = new org.apache.commons.lang3.text.StrTokenizer(prefixMappings);
		while (tokens.hasNext() ) {
			String token = tokens.nextToken();
			//log.debug("Got: " + token);
			int pos = token.indexOf("=");
			String prefix = token.substring(6, pos); // drop xmlns:
			//log.debug("Got: " + prefix);
			String uri = token.substring(pos+2, token.lastIndexOf("'"));
			//log.debug("Got: " + uri);
			namespaces.put(prefix, uri);
		}
		
	}


/* Implement org.apache.xml.utils.PrefixResolver, for org.apache.xpath.CachedXPathAPI */
	
	@Override
	public String getNamespaceForPrefix(String prefix) {
		return getNamespaceURI( prefix);
	}



	@Override
	public String getNamespaceForPrefix(String prefix, Node context) {
		return getNamespaceURI( prefix);
	}



	@Override
	public String getBaseIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public boolean handlesNullPrefixes() {
		// TODO Auto-generated method stub
		return false;
	}
	
    
}
