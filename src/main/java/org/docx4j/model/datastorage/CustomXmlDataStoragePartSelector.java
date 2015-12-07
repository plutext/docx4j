/*
 *  Copyright 2010-14, Plutext Pty Ltd.
 *   
 *  This file is part of OpenDoPE Java simple webapp.

    OpenDoPE Java simple webapp is licensed under the Apache License, 
    Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */
package org.docx4j.model.datastorage;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.utils.SingleTraversalUtilVisitorCallback;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.SdtElement;
import org.opendope.xpaths.Xpaths.Xpath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

/**
 * Mechanism to find the user's XML data part
 * (typically to replace it with the runtime
 *  instance XML data).
 * 
 * @author jharrop
 * @since 3.0.1
 */
public class CustomXmlDataStoragePartSelector {
	
	protected static Logger log = LoggerFactory.getLogger(CustomXmlDataStoragePartSelector.class);
	
	
	/**
	 * We need the item id of the custom xml part.  	 * 
	 * @param wordMLPackage
	 * @return
	 */
	public static CustomXmlDataStoragePart getCustomXmlDataStoragePart(WordprocessingMLPackage wordMLPackage) throws Docx4JException {
		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();	
		
		if (wordMLPackage.getMainDocumentPart().getXPathsPart()==null) {
			
			// Can't do it the easy way, so inspect content controls

			TraversalUtilCCVisitor visitor = new TraversalUtilCCVisitor();
			visitor.customXmlParts = wordMLPackage.getCustomXmlDataStorageParts();
			SingleTraversalUtilVisitorCallback ccFinder 
			= new SingleTraversalUtilVisitorCallback(visitor);
			ccFinder.walkJAXBElements(
				wordMLPackage.getMainDocumentPart().getJaxbElement().getBody());
			if (visitor.customXmlDataStoragePart==null) {
				log.error("FATAL. Couldn't find CustomXmlDataStoragePart  " );
				return null;
			} else {
				return visitor.customXmlDataStoragePart;
				
			}
			
		} else {
	
			org.opendope.xpaths.Xpaths xPaths = wordMLPackage.getMainDocumentPart().getXPathsPart().getJaxbElement();
			
			for (Xpath xp : xPaths.getXpath()) {
				
				if (shouldSkip(xp.getDataBinding().getPrefixMappings())) {
					continue;
				}
				

				// OK, this one looks ok
				String itemId = xp.getDataBinding().getStoreItemID().toLowerCase();
				System.out.println("Attempting to use item id: " + itemId);
				
				CustomXmlDataStoragePart customXmlDataStoragePart 
					= (CustomXmlDataStoragePart)wordMLPackage.getCustomXmlDataStorageParts().get(itemId);
				if (customXmlDataStoragePart==null) {
                    if(log.isWarnEnabled()) {
                        log.warn("Couldn't find CustomXmlDataStoragePart referenced from " + XmlUtils.marshaltoString(xp));

                    }
					continue;			
				} else {
					log.debug("Using " + xp.getDataBinding().getStoreItemID());
					return customXmlDataStoragePart;
					
				}
				
				
			}
			log.error("Couldn't identify XML part from XPaths part entries");
			return null;
			
			
		}
	}
	
	private static boolean shouldSkip(String prefixMappings) {

		if (prefixMappings==null) return false;
		
		/*
		 * Skip:
		 * 
				<xpath id="x1">
					<dataBinding xpath="/ns0:CoverPageProperties[1]/ns0:PublishDate[1]" 
					storeItemID="{55AF091B-3C7A-41E3-B477-F2FDAA23CFDA}" 
					prefixMappings="xmlns:ns0='http://schemas.microsoft.com/office/2006/coverPageProps' "/>
				</xpath>

			 */
		if (prefixMappings.contains("http://schemas.microsoft.com/office/2006/coverPageProps")) {
			return true;
		}

		
		/*
		 * Skip:
		 * 
				
				<xpath id="x2">
					<dataBinding xpath="/ns0:properties[1]/documentManagement[1]/ns3:Current_x0020_Version[1]" 
					storeItemID="{F992F247-E04A-4DBD-9676-4A6E51A53BB6}" 
					prefixMappings="xmlns:ns0='http://schemas.microsoft.com/office/2006/metadata/properties' 
									xmlns:ns1='http://www.w3.org/2001/XMLSchema-instance' 
									xmlns:ns2='http://schemas.microsoft.com/office/infopath/2007/PartnerControls' 
									xmlns:ns3='25863374-67c0-4723-8c21-1fb7e315143b' "/>
				</xpath>

			 */
		if (prefixMappings.contains("http://schemas.microsoft.com/office/2006/metadata/properties")) {
			return true;
		}

		
		/*
		 * Skip:
		 * 
				<xpath id="x3">
					<dataBinding xpath="/ns1:coreProperties[1]/ns1:contentStatus[1]" 
					storeItemID="{6C3C8BC8-F283-45AE-878A-BAB7291924A1}" 
					prefixMappings="xmlns:ns0='http://purl.org/dc/elements/1.1/' 
									xmlns:ns1='http://schemas.openxmlformats.org/package/2006/metadata/core-properties' "/>
				</xpath>
			 */
		if (prefixMappings.contains("http://schemas.openxmlformats.org/package/2006/metadata/core-properties")) {
			return true;
		}
		
		return false;
	}

	private static class TraversalUtilCCVisitor extends TraversalUtilVisitor<SdtElement> {
		
		
		HashMap<String, CustomXmlPart> customXmlParts = null; 
		
		CustomXmlDataStoragePart customXmlDataStoragePart = null;
		
		@Override
		public void apply(SdtElement element, Object parent, List<Object> siblings) {

			if (customXmlDataStoragePart==null // not found yet
					&& element.getSdtPr()!=null
					&& element.getSdtPr().getDataBinding()!=null) {
				
				if (shouldSkip(element.getSdtPr().getDataBinding().getPrefixMappings())) {
					// continue on
				} else {
					
					String itemId = element.getSdtPr().getDataBinding().getStoreItemID().toLowerCase();
					log.debug("Attempting to use item id: " + itemId);
					
					customXmlDataStoragePart 
						= (CustomXmlDataStoragePart)customXmlParts.get(itemId);
					if (customXmlDataStoragePart==null) {
						log.warn("Couldn't find CustomXmlDataStoragePart referenced from sdt bound with  " + itemId);			
					} else {
						log.debug("Using " + itemId);
					}
					
				}
				
			}
		}
	
	}
	

}
