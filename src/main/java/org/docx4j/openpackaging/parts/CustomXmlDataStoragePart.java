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

package org.docx4j.openpackaging.parts;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMappings;
import org.docx4j.model.datastorage.CustomXmlDataStorage;
import org.docx4j.model.sdt.QueryString;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.CTDataBinding;
import org.docx4j.wml.CTSdtContentRow;
import org.docx4j.wml.CTSdtContentRun;
import org.docx4j.wml.SdtContentBlock;
import org.docx4j.wml.Tag;
import org.w3c.dom.Node;


public final class CustomXmlDataStoragePart extends Part {
	
	private static Logger log = Logger.getLogger(CustomXmlDataStoragePart.class);		
	
	public static void log(String message ) {
		
		log.info(message);
	}
	
	/*
	 * If this contains XML which is bound in an sdt
	 * via w:sdtPr/w:dataBinding, then its rels
	 * will point to CustomXmlDataStoragePropertiesPart
	 * which gives its datastore itemID.
	 * 
	 * (The datastore itemID is used in the w:dataBinding)
	 *  
	 * The Package contains a hashmap<String, CustomXmlDataStoragePart>
	 * to make it easy to get the part to which we apply the 
	 * XPath.  The part is registered as the document is loaded.
	 *
	 * There are only 2 things we need to do here:
	 * 
	 * 1. inject the XML (form data) into the package.
	 *    - this is simply a matter of attaching it to this part.
	 *    
	 * 2. copy the XML data into the sdt's, so it is there
	 *    for PDF, HTML output.  (we don't actually need to
	 *    do anything for it to appear in the Word 2007 UI - 
	 *    Word does this step itself).  This will be a new
	 *    static method in this class.
	 *    
	 * A user who wishes to set up the bindings is advised
	 * to use the Content Control Toolkit.
	 */
	
	static Templates xslt;			
	static {
		try {
			Source xsltSource = new StreamSource(
						org.docx4j.utils.ResourceUtils.getResource(
								"org/docx4j/model/datastorage/bind.xslt"));
			xslt = XmlUtils.getTransformerTemplate(xsltSource);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
	}

	public CustomXmlDataStoragePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}
	
//	public CustomXmlDataStoragePart() throws InvalidFormatException {
//		super(new PartName("/customXml/item1.xml"));
//		init();
//	}

	/**
	 * @param parts The parts present in the package to which this will be added.
	 * If for example /customXml/item1.xml already exists, this allows
	 * the name /customXml/item2.xml to be generated.
	 * @throws InvalidFormatException
	 */
	public CustomXmlDataStoragePart(Parts parts) throws InvalidFormatException {
		
		int partNum = 1;
		if (parts!=null) {
			while (parts.get(new PartName("/customXml/item" + partNum + ".xml"))!=null) {
				partNum++;			
			}
		}
		
		this.partName = new PartName("/customXml/item" + partNum + ".xml");
		log.info("Using PartName /customXml/item" + partNum + ".xml");
		init();
	}
	
	
	public void init() {		
	
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_CUSTOMXML_DATASTORAGE));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.CUSTOM_XML_DATA_STORAGE);
		
	}

	private CustomXmlDataStorage data;
	/**
	 * @return the data
	 */
	public CustomXmlDataStorage getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(CustomXmlDataStorage data) {
		this.data = data;
	}
	
	/**
	 * Preprocess content controls which have tag bindingrole="conditional|repeat".
	 * 
	 * Limitations:
	 * - nested repeats?
	 * 
	 * @param documentPart
	 * @throws Docx4JException
	 */
	public static void preprocess(WordprocessingMLPackage wordMLPackage) throws Docx4JException {

		//Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts = wordMLPackage.getCustomXmlDataStorageParts();		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
				
		String xpathSdt = "//w:sdt";
		List<Object> list = null;
		try {
			list = documentPart.getJAXBNodesViaXPath(xpathSdt, false);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(Object raw : list) {
			
			log.info(raw.getClass().getName() );
			
			Object o = XmlUtils.unwrap(raw);
			
			if (o instanceof org.docx4j.wml.SdtBlock ) {
				
				org.docx4j.wml.SdtBlock sdt = (org.docx4j.wml.SdtBlock)o;
				if (sdt.getSdtPr().getDataBinding()!=null) continue; // a real binding attribute trumps any tag
				Tag tag = sdt.getSdtPr().getTag();								
				processSdt(wordMLPackage, sdt.getParent(), sdt, tag, sdt.getSdtContent() );
				
			} else if ( o instanceof org.docx4j.wml.SdtRun ) { // sdt in paragraph
					
				org.docx4j.wml.SdtRun sdtrun = (org.docx4j.wml.SdtRun)o;
				if (sdtrun.getSdtPr().getDataBinding()!=null) continue; // a real binding attribute trumps any tag
				Tag tag = sdtrun.getSdtPr().getTag();	
				processSdt(wordMLPackage, sdtrun.getParent(), sdtrun, tag, sdtrun.getSdtContent() );					
				
			} else if (  o instanceof org.docx4j.wml.CTSdtRow ) { // sdt wrapping row

				org.docx4j.wml.CTSdtRow sdtrow = (org.docx4j.wml.CTSdtRow)o;
				if (sdtrow.getSdtPr().getDataBinding()!=null) continue; // a real binding attribute trumps any tag
				Tag tag = sdtrow.getSdtPr().getTag();	
				processSdt(wordMLPackage, sdtrow.getParent(), sdtrow, tag, sdtrow.getSdtContent() );
				
			} else if (o instanceof org.docx4j.wml.CTSdtCell ) { // sdt wrapping cell
				
				log.warn("Cowardly ignoring bindingrole on SdtCell");
				
			} else {
				log.warn("TODO: Handle " + o.getClass().getName() );					
			}
			
		}
		
	}
	
	private static void processSdt(WordprocessingMLPackage wordMLPackage,
			Object sdtParent, Object sdt, Tag tag, Object sdtContent) {
		
		if (tag==null) return;
		
		log.info(tag.getVal());

		QueryString qs = new QueryString();
		HashMap<String, String> map = qs.parseQueryString(tag.getVal(), true);
		
		String bindingrole = map.get("bindingrole");
		if (bindingrole==null) {
			return; // do nothing
		} 

		Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts = wordMLPackage.getCustomXmlDataStorageParts();		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		
		// get the value
		String storeItemId = map.get("w:storeItemID").toLowerCase();
		String xpath = map.get("w:xpath");
		String prefixMappings = map.get("w:prefixMappings");
		
		if (bindingrole.equals("conditional")) {

			log.info("Processing Conditional: " + tag.getVal());
			
			/*
			 * eg   <w:tag w:val="bindingrole=conditional
			 * 						&amp;w:xpath=/root/inornot
			 * 						&amp;w:storeItemID={543111b2-fc12-4cae-89cc-1d5996e6a7a3}"/>
                    
                    <w:tag w:val="bindingrole=conditional
                    				&amp;w:xpath=/root/inornot/text()
                    				&amp;w:storeItemID={543111b2-fc12-4cae-89cc-1d5996e6a7a3}"/>-->
			 * 
			 */
			
			String val = xpathGetString(customXmlDataStorageParts,
					storeItemId, xpath, prefixMappings);	
			
			log.info("Got value: " + val);
			
			if (new Boolean(val) ) {
				log.debug("so keeping");
			} else {
				// Remove it
				log.info("Removed? " + removeSdt(sdtParent, sdt) );
			}
			
		} else if (bindingrole.equals("repeat")) {

			log.info("Processing Repeat: " + tag.getVal());
			
			// Get the bound XML	
			String xpathBase;
			if (xpath.endsWith("/*")) {
				xpathBase = xpath.substring(0, xpath.length()-2);
			} else if (xpath.endsWith("/")) {
				xpathBase = xpath.substring(0, xpath.length()-1);
			} else {
				xpathBase = xpath;
			}
			log.debug("Repeat: using xpath: " + xpath);
	        NamespaceContext nsContext = new NamespacePrefixMappings();			
	        List<Node> repeatChildren = xpathGetNodes(customXmlDataStorageParts,
					storeItemId, xpathBase+"/*", prefixMappings);	
			
			// Count children
	        int numRepeats = repeatChildren.size();
	        log.debug("Got children: " + numRepeats );
	        
	        if (numRepeats==0) {
	        	// Remove repeat std
				log.info("Removed? " + removeSdt(sdtParent, sdt) );
				return;
	        }
			
	        
	        List<List<Object>> newContent = new ArrayList<List<Object>>(); 
	        for (int i=0; i<numRepeats; i++) {
	        	newContent.add(new ArrayList<Object>() );
	        }
	        
			// Process the sdt contents
	        List<Object> content;
	        if (sdtContent instanceof CTSdtContentRow) {
	        	content = ((CTSdtContentRow)sdtContent).getEGContentRowContent();
	        } else if (sdtContent instanceof SdtContentBlock) {	        	
	        	content = ((SdtContentBlock)sdtContent).getEGContentBlockContent();	        	
	        } else if (sdtContent instanceof CTSdtContentRun) {	        	
	        	content = ((CTSdtContentRun)sdtContent).getParagraphContent();	        	
	        } else {
	        	log.error("TODO: handle " + sdtContent.getClass().getName() );
	        	return;
	        }
	        
        	for (Object c : content) {
        		
        		// Find child content controls.
        		// We expect some .. that's the whole point
    			// Limitation: For now, we only mangle real binding elements,
    			// not things with @bindingrole
        		String xpathDataBindings = "//w:sdt/w:sdtPr/w:dataBinding";
        		List<Object> bindingsToMangle = null;
        		try {
        			bindingsToMangle = documentPart.getJAXBNodesViaXPath(xpathDataBindings, c, false);
        		} catch (JAXBException e) {
        			e.printStackTrace();
        		}        		
        		
        		if (bindingsToMangle.size()>0) {

        	        for (int i=0; i<numRepeats; i++) {
        			
	        			// Alter XPaths in any child content controls
        	        	// For now, we don't worry about the fact that there will be duplicate ids.
	        			
	        			// First, mangle
	        			// but keep a copy so we can restore state
	        			List<String> initialBindingXPath = new ArrayList<String>();
	        			for (Object ob : bindingsToMangle) {
	        				
	        				
	        				CTDataBinding binding = (CTDataBinding)XmlUtils.unwrap(ob);
//	        				if (ob instanceof CTDataBinding) {
//	        					binding	= (CTDataBinding)ob;
//	        				} else if (ob instanceof javax.xml.bind.JAXBElement) {
//	        					binding	= (CTDataBinding)((JAXBElement)ob).getValue();
//	        				}
	        				initialBindingXPath.add(binding.getXpath());
	        				
	        				String thisXPath = binding.getXpath();
        					log.debug("existing xpath: " + thisXPath);
	        				
	        				// The tricky part
	        				if (thisXPath.startsWith(xpathBase)) {
	        					log.debug("xpathBase: " + xpathBase);
	        					int beginIndex = thisXPath.indexOf("/", xpathBase.length()+1 ); // +1 for good measure	        					
	        					String newPath = xpathBase + "/*[" + (i+1) + "]/" + thisXPath.substring(beginIndex+1);	        					
	        					log.debug("newPath: " + newPath);
	        					binding.setXpath(newPath);
	        				}
	        			}
	        			        			
	        			// Clone
        	        	newContent.get(i).add(
        	        			XmlUtils.deepCopy(c)	        	        	
        	        	);
	        			
	        			// Unmangle, ready for next iteration
	        			int it = 0;
	        			for (Object ob : bindingsToMangle) {        				
	        				CTDataBinding binding = (CTDataBinding)XmlUtils.unwrap(ob);
//	        				CTDataBinding binding = null;
//	        				if (ob instanceof CTDataBinding) {
//	        					binding	= (CTDataBinding)ob;
//	        				} else if (ob instanceof javax.xml.bind.JAXBElement) {
//	        					binding	= (CTDataBinding)((JAXBElement)ob).getValue();
//	        				}
	        				binding.setXpath( initialBindingXPath.get(it));
	        				it++;
	        			}
        	        }        			
        		} else {
        			
        			log.warn("No descendant content controls found in the repeat");
        			
        	        for (int i=0; i<numRepeats; i++) {
        	        	newContent.get(i).add(
        	        			XmlUtils.deepCopy(c)	        	        	
        	        	);
        	        }
        			
        		}	        		
        	}
        		        	
			// Replace
	        if (sdtContent instanceof CTSdtContentRow) {
	        	((CTSdtContentRow)sdtContent).getEGContentRowContent().clear();
		        for (int i=0; i<numRepeats; i++) {
		        	((CTSdtContentRow)sdtContent).getEGContentRowContent().addAll(
	    	        	newContent.get(i)	        	        	
		        	);
		        }	        	
	        } else if (sdtContent instanceof SdtContentBlock) {	        	
	        	((SdtContentBlock)sdtContent).getEGContentBlockContent().clear();
		        for (int i=0; i<numRepeats; i++) {
		        	((SdtContentBlock)sdtContent).getEGContentBlockContent().addAll(
	    	        	newContent.get(i)	        	        	
		        	);
		        }	        	
	        } else if (sdtContent instanceof CTSdtContentRun) {	        	
	        	((CTSdtContentRun)sdtContent).getParagraphContent().clear();
		        for (int i=0; i<numRepeats; i++) {
		        	((CTSdtContentRun)sdtContent).getParagraphContent().addAll(
	    	        	newContent.get(i)	        	        	
		        	);
		        }	        	
	        } else {
	        	log.error("TODO: handle " + sdtContent.getClass().getName() );
	        	return;
	        }
	        	
		}	
	}
		
	private static boolean removeSdt(Object sdtParent, Object sdt) {
		
		if (sdtParent instanceof org.docx4j.wml.Body) {			
			return ((org.docx4j.wml.Body)sdtParent).getEGBlockLevelElts().remove(sdt);
		} else if (sdtParent instanceof org.docx4j.wml.P) {
			return ((org.docx4j.wml.P)sdtParent).getParagraphContent().remove(sdt);
		} else if (sdtParent instanceof org.docx4j.wml.Tbl) {
			return ((org.docx4j.wml.Tbl)sdtParent).getEGContentRowContent().remove(sdt);
		} else if (sdtParent instanceof org.docx4j.wml.Tr) {
			return ((org.docx4j.wml.Tr)sdtParent).getEGContentCellContent().remove(sdt);
		} else if (sdtParent instanceof org.docx4j.wml.Tc) {
			return ((org.docx4j.wml.Tc)sdtParent).getEGBlockLevelElts().remove(sdt);
        } else {
        	log.error("TODO: handle removal from parent " + sdtParent.getClass().getName() );
        	return false;
        }
	}
	
	private static List<Node> xpathGetNodes(Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
			String storeItemId, String xpath, String prefixMappings) {
		
		CustomXmlDataStoragePart part = customXmlDataStorageParts.get(storeItemId);
		if (part==null) {
			log.error("Couldn't locate part by storeItemId " + storeItemId);
			return null;
		}
		return part.getData().xpathGetNodes(xpath, prefixMappings);
	}
	
	
	public static void applyBindings(DocumentPart documentPart) throws Docx4JException {
		
		// TODO: need to be able to apply to other parts eg header|footer
		
		// Get the package from documentPart
		org.docx4j.openpackaging.packages.OpcPackage pkg 
			= documentPart.getPackage();		
			// Binding is a concept which applies more broadly
			// than just Word documents.
		
		org.w3c.dom.Document doc = XmlUtils.marshaltoW3CDomDocument(
				documentPart.getJaxbElement() ); 	
		
		JAXBContext jc = Context.jc;
		try {
			javax.xml.bind.util.JAXBResult result = new javax.xml.bind.util.JAXBResult(jc );
			
			Map<String, Object> transformParameters = new HashMap<String, Object>();
			transformParameters.put("customXmlDataStorageParts", 
					documentPart.getPackage().getCustomXmlDataStorageParts());			
					
			org.docx4j.XmlUtils.transform(doc, xslt, transformParameters, result);
			
			//javax.xml.bind.JAXBElement je = (javax.xml.bind.JAXBElement)result.getResult();
			org.docx4j.wml.Document d = (org.docx4j.wml.Document)result.getResult();
			documentPart.setJaxbElement(d);
		} catch (Exception e) {
			throw new Docx4JException("Problems applying bindings", e);			
		}
				
	}
	
	/**
	 * @param customXmlDataStorageParts
	 * @param storeItemId
	 * @param xpath
	 * @param prefixMappings a string such as "xmlns:ns0='http://schemas.medchart'"
	 * @return
	 */
	public static String xpathGetString(Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
			String storeItemId, String xpath, String prefixMappings) {
		
		CustomXmlDataStoragePart part = customXmlDataStorageParts.get(storeItemId.toLowerCase());
		if (part==null) {
			log.error("Couldn't locate part by storeItemId " + storeItemId);
			return null;
		}
		try {
			if (log.isDebugEnabled() ) {
				String r = part.getData().xpathGetString(xpath, prefixMappings);
				log.debug(xpath + " yielded result " + r);
				return r;
			} else {
				return part.getData().xpathGetString(xpath, prefixMappings);
			}
		} catch (Docx4JException e) {
			e.printStackTrace();
			return null;
		}
	}

	
	/* ---------------------------------------------------------------------------
	 * Pre-processing of content controls which have a tag containing "bindingrole"
	 * 
	 * 
	 */
	
}
