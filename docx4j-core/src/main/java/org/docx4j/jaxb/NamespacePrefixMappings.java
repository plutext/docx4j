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

/* Refer https://github.com/OfficeDev/Open-XML-SDK/blob/master/src/DocumentFormat.OpenXml/Framework/NamespaceIdMap.cs
 * (copied in our xsd dir) for canonical mappings. */

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
    public static String getPreferredPrefixStatic(String namespaceUri, String suggestion, boolean requirePrefix) {    
    	
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
		
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2010/chartDrawing")) {
			return "cdr14";
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2010/diagram")) {
			return "dgm14";
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2010/main")) {
			return "a14";
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2010/picture")) {
			return "pic14";
		}

		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2012/chart")) {
			return "c15";
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2012/chartStyle")) {
			return "cs";
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2012/main")) {
			return "a15";
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
    	if (namespaceUri.equals("http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing")) {
    		return "wp14";
    	}
    	
    	if (namespaceUri.equals("http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas")) {
    		return "wpc";
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
    	if (namespaceUri.equals("http://schemas.microsoft.com/office/word/2012/wordprocessingDrawing")) {
    		return "wp15";
    	}
    	
    	if (namespaceUri.equals("http://schemas.microsoft.com/office/word/2018/wordml")) {
    		return "w16";
    	}

    	if (namespaceUri.equals("http://schemas.microsoft.com/office/word/2018/wordml/cex")) {
    		return "w16cex";
    	}
    	
    	if (namespaceUri.equals("http://schemas.microsoft.com/office/word/2016/wordml/cid")) {
    		return "w16cid";
    	}
    	
    	if (namespaceUri.equals("http://schemas.microsoft.com/office/word/2015/wordml/symex")) {
    		return "w16se";
    	}

    	if (namespaceUri.equals("http://schemas.microsoft.com/office/word/2020/wordml/sdtdatahash")) {
    		return "w16sdtdh";
    	}

    	if (namespaceUri.equals("http://schemas.microsoft.com/office/powerpoint/2010/main")) {
    		return "p14";
    	}
    	
    	if (namespaceUri.equals("http://schemas.microsoft.com/office/powerpoint/2012/main")) {
    		return "p15";
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

    	if (namespaceUri.equals("http://schemas.microsoft.com/ink/2010/main")) {
    		return "msink";
    	}
    	
    	if (namespaceUri.equals("http://schemas.openxmlformats.org/officeDocument/2006/math")) {
    		return "m";
    	}

    	if (namespaceUri.equals("http://schemas.microsoft.com/office/thememl/2012/main")) {
    		return "thm15";
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
		if (namespaceUri.equals("http://schemas.microsoft.com/office/excel/2010/spreadsheetDrawing")) {
    		return "xdr14";
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

		// some of the below are invented since nothing is prescribed in [MS-ODRAWML] or NamespaceIdMap    	
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2013/main/command")) {
			return "a13cmd"; // made up
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2014/chart/ac")) {
			return "c16ac";
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2014/chartex")) {
			return "cx";
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2014/chart")) {
			return "c16";
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2014/main")) {
			return "a16";
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2016/11/diagram")) {
			return "dgm1611"; // made up
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2016/11/main")) {
			return "a1611"; // made up
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2016/12/diagram")) {
			return "dgm1612"; // made up
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2016/ink")) {
			return "ink16"; // made up
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2016/SVG/main")) {
			return "a16svg"; // made up
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2017/03/chart")) {
			return "c173"; // made up
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2017/decorative")) {
			return "adec"; // made up
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2017/model3d")) {
			return "am3d";
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2018/animation/model3d")) {
			return "anam3d"; // made up
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2018/animation")) {
			return "an18"; // made up
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/drawing/2018/hyperlinkcolor")) {
			return "a18hc"; // made up
			
		// [MS-PPTX]			
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/powerpoint/2014/inkAction")) {
			return "iact";
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/powerpoint/2013/main/command")) {
			return "p13cmd"; // made up
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/powerpoint/2015/09/main")) {
			return "p159";
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/powerpoint/2015/10/main")) {
			return "p1510";
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/powerpoint/2015/main")) {
			return "p16";
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/powerpoint/2016/sectionzoom")) {
			return "psez";
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/powerpoint/2016/slidezoom")) {
			return "pslz";
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/powerpoint/2016/summaryzoom")) {
			return "psuz";
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/powerpoint/2017/10/main")) {
			return "p1710";
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/powerpoint/2017/3/main")) {
			return "p173";
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/powerpoint/2018/4/main")) {
			return "p184"; // made up
		}
		if (namespaceUri.equals("http://schemas.microsoft.com/office/powerpoint/2016/6/main")) {
			return "p166";
		}    	

		if (namespaceUri.equals("http://schemas.microsoft.com/office/spreadsheetml/2009/9/main")) {
			return "x14";
		}   

		if (namespaceUri.equals("http://schemas.microsoft.com/office/spreadsheetml/2009/9/ac")) {
			return "x14ac";
		}   
		
		if (namespaceUri.equals("http://schemas.microsoft.com/office/spreadsheetml/2010/11/main")) {
			return "x15";
		}   

		if (namespaceUri.equals("http://schemas.microsoft.com/office/spreadsheetml/2010/11/ac")) {
			return "x15ac";
		}   
		
		// XLSX (2020 additions)
		if (namespaceUri.equals("http://schemas.microsoft.com/office/spreadsheetml/2014/revision"))
			return "xr";	
		
		if (namespaceUri.equals("http://schemas.microsoft.com/office/spreadsheetml/2015/revision2"))
			return "xr2";	

		if (namespaceUri.equals("http://schemas.microsoft.com/office/spreadsheetml/2016/revision3"))
			return "xr3";	
		
		if (namespaceUri.equals("http://schemas.microsoft.com/office/spreadsheetml/2016/revision6"))
			return "xr6";	

		if (namespaceUri.equals("http://schemas.microsoft.com/office/spreadsheetml/2016/revision10"))
			return "xr10";	
		
				
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
		
		if (prefix.equals("cdr14"))
			return "http://schemas.microsoft.com/office/drawing/2010/chartDrawing";
		if (prefix.equals("dgm14"))
			return "http://schemas.microsoft.com/office/drawing/2010/diagram";
		if (prefix.equals("a14"))
			return "http://schemas.microsoft.com/office/drawing/2010/main";
		if (prefix.equals("pic14"))
			return "http://schemas.microsoft.com/office/drawing/2010/picture";

		if (prefix.equals("c15"))
			return "http://schemas.microsoft.com/office/drawing/2012/chart";
		if (prefix.equals("cs"))
			return "http://schemas.microsoft.com/office/drawing/2012/chartStyle";
		if (prefix.equals("a15"))
			return "http://schemas.microsoft.com/office/drawing/2012/main";		

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
				
		if (prefix.equals("wp14"))
			return "http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing";
		
		if (prefix.equals("wpc"))
			return "http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas";
		
		if (prefix.equals("wpg"))
			return "http://schemas.microsoft.com/office/word/2010/wordprocessingGroup";
		
		if (prefix.equals("wpi"))
			return "http://schemas.microsoft.com/office/word/2010/wordprocessingInk";

		if (prefix.equals("wps"))
			return "http://schemas.microsoft.com/office/word/2010/wordprocessingShape";
		
		if (prefix.equals("w15"))
			return "http://schemas.microsoft.com/office/word/2012/wordml";
		
		if (prefix.equals("wp15"))
			return "http://schemas.microsoft.com/office/word/2012/wordprocessingDrawing";

		if (prefix.equals("w16"))
			return "http://schemas.microsoft.com/office/word/2018/wordml";
		
		if (prefix.equals("w16cex"))
			return "http://schemas.microsoft.com/office/word/2018/wordml/cex";
		
		if (prefix.equals("w16cid"))
				return "http://schemas.microsoft.com/office/word/2016/wordml/cid";

		if (prefix.equals("w16se"))
			return "http://schemas.microsoft.com/office/word/2015/wordml/symex";

		if (prefix.equals("w16sdtdh"))
			return "http://schemas.microsoft.com/office/word/2020/wordml/sdtdatahash";

		if (prefix.equals("p14"))
			return "http://schemas.microsoft.com/office/powerpoint/2010/main";

		if (prefix.equals("p15"))
			return "http://schemas.microsoft.com/office/powerpoint/2012/main";
		
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
		
		if (prefix.equals("xdr14"))
			return "http://schemas.microsoft.com/office/excel/2010/spreadsheetDrawing";
		
		if (prefix.equals("msink"))
			return "http://schemas.microsoft.com/ink/2010/main";

		if (prefix.equals("thm15"))
			return "http://schemas.microsoft.com/office/thememl/2012/main";

		// XLSX (2020 additions)
		if (prefix.equals("xr"))
			return "http://schemas.microsoft.com/office/spreadsheetml/2014/revision";	
		
		if (prefix.equals("xr2"))
			return "http://schemas.microsoft.com/office/spreadsheetml/2015/revision2";	

		if (prefix.equals("xr3"))
			return "http://schemas.microsoft.com/office/spreadsheetml/2016/revision3";	
		
		if (prefix.equals("xr6"))
			return "http://schemas.microsoft.com/office/spreadsheetml/2016/revision6";	

		if (prefix.equals("xr10"))
			return "http://schemas.microsoft.com/office/spreadsheetml/2016/revision10";	
		
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
		
		// some of the below are invented since nothing is prescribed in [MS-ODRAWML] or NamespaceIdMap
		// See inverse above to identify which
		if (prefix.equals("a13cmd"))
			return "http://schemas.microsoft.com/office/drawing/2013/main/command";
		if (prefix.equals("c16ac"))
			return "http://schemas.microsoft.com/office/drawing/2014/chart/ac";
		if (prefix.equals("cx"))
			return "http://schemas.microsoft.com/office/drawing/2014/chartex";
		if (prefix.equals("c16"))
			return "http://schemas.microsoft.com/office/drawing/2014/chart";
		if (prefix.equals("a16"))
			return "http://schemas.microsoft.com/office/drawing/2014/main";
		if (prefix.equals("dgm1611"))
			return "http://schemas.microsoft.com/office/drawing/2016/11/diagram";
		if (prefix.equals("a1611"))
			return "http://schemas.microsoft.com/office/drawing/2016/11/main";
		if (prefix.equals("dgm1612"))
			return "http://schemas.microsoft.com/office/drawing/2016/12/diagram";
		if (prefix.equals("ink16"))
			return "http://schemas.microsoft.com/office/drawing/2016/ink";
		if (prefix.equals("a16svg"))
			return "http://schemas.microsoft.com/office/drawing/2016/SVG/main";
		if (prefix.equals("c173"))
			return "http://schemas.microsoft.com/office/drawing/2017/03/chart";
		if (prefix.equals("adec"))
			return "http://schemas.microsoft.com/office/drawing/2017/decorative";
		if (prefix.equals("am3d"))
			return "http://schemas.microsoft.com/office/drawing/2017/model3d";
		if (prefix.equals("anam3d"))
			return "http://schemas.microsoft.com/office/drawing/2018/animation/model3d";
		if (prefix.equals("an18"))
			return "http://schemas.microsoft.com/office/drawing/2018/animation";
		if (prefix.equals("a18hc"))
			return "http://schemas.microsoft.com/office/drawing/2018/hyperlinkcolor";
		
		// [MS-PPTX]
		if (prefix.equals("iact"))
			return "http://schemas.microsoft.com/office/powerpoint/2014/inkAction";
		if (prefix.equals("p13cmd"))
			return "http://schemas.microsoft.com/office/powerpoint/2013/main/command";
		if (prefix.equals("p159"))
			return "http://schemas.microsoft.com/office/powerpoint/2015/09/main";
		if (prefix.equals("p1510"))
			return "http://schemas.microsoft.com/office/powerpoint/2015/10/main";
		if (prefix.equals("p16"))
			return "http://schemas.microsoft.com/office/powerpoint/2015/main";
		if (prefix.equals("psez"))
			return "http://schemas.microsoft.com/office/powerpoint/2016/sectionzoom";
		if (prefix.equals("pslz"))
			return "http://schemas.microsoft.com/office/powerpoint/2016/slidezoom";
		if (prefix.equals("psuz"))
			return "http://schemas.microsoft.com/office/powerpoint/2016/summaryzoom";
		if (prefix.equals("p1710"))
			return "http://schemas.microsoft.com/office/powerpoint/2017/10/main";
		if (prefix.equals("p173"))
			return "http://schemas.microsoft.com/office/powerpoint/2017/3/main";
		if (prefix.equals("p184"))
			return "http://schemas.microsoft.com/office/powerpoint/2018/4/main";
		if (prefix.equals("p166"))
			return "http://schemas.microsoft.com/office/powerpoint/2016/6/main";

		if (prefix.equals("x14"))
			return "http://schemas.microsoft.com/office/spreadsheetml/2009/9/main";
		
		if (prefix.equals("x14ac"))
			return "http://schemas.microsoft.com/office/spreadsheetml/2009/9/ac";
		
		if (prefix.equals("x15"))
			return "http://schemas.microsoft.com/office/spreadsheetml/2010/11/main";

		if (prefix.equals("x15ac"))
			return "http://schemas.microsoft.com/office/spreadsheetml/2010/11/ac";
		
		
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
