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
package org.docx4j.convert.out.common.preprocess;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.model.datastorage.CustomXmlDataStorage;
import org.docx4j.openpackaging.Base;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.relationships.Relationships;
import org.w3c.dom.Document;

/** Create a partial deep copy of the document. All the parts are copied, 
 * as they may have some references to other parts. The data in the parts is 
 * only copied if the relationship type of the part is contained in the passed
 * relationshipTypes otherwise the new part contains a reference to the data 
 * of the old part.<br>
 * If the passed relationship types is null, then it will do a complete deep copy.
 * This is probably faster than storing and reading the document but it is restricted 
 * to Parts of the types: BinaryPart, JaxbXmlPart, CustomXmlDataStoragePart, XmlPart.<br>
 * If the passed relationship types is empty, then the passed Package is returned.  
 * 
 */
public class PartialDeepCopy {
	
	protected static Logger log = LoggerFactory.getLogger(PartialDeepCopy.class);
	
	
	public static OpcPackage process(OpcPackage opcPackage, Set<String> relationshipTypes) throws Docx4JException {
		
		OpcPackage ret = null;
		RelationshipsPart relPart = null;
		if (opcPackage != null) {
			if ((relationshipTypes != null) && (relationshipTypes.isEmpty())) {
				ret = opcPackage;
			}
			else {
				ret = createPackage(opcPackage);
				
				if (ret==null) {
					log.error("createPackage returned null!");
				}
				
				
				deepCopyRelationships(ret, opcPackage, ret, relationshipTypes);
				
				// Copy the font mappings
				if (opcPackage instanceof WordprocessingMLPackage) {
					
//					// First need shortcut to MDP
//					// .. get its name
//					PartName mdpName = ((WordprocessingMLPackage)opcPackage).getMainDocumentPart().getPartName();
//					// .. get the part
//					Part mdp = ((WordprocessingMLPackage)ret).getParts().get(mdpName);
//					// .. set the shortcut
//					ret.setPartShortcut(mdp, mdp.getRelationshipType());					
					
					try {
						((WordprocessingMLPackage)ret).setFontMapper(
								((WordprocessingMLPackage)opcPackage).getFontMapper(), false); //don't repopulate, since we want to preserve existing mappings
					} catch (Exception e) {
						// shouldn't happen
						log.error(e.getMessage(),e);
						throw new Docx4JException("Error setting font mapper on copy", e);
					}
				}
				
			}
		}
		return ret;
	}

	protected static OpcPackage createPackage(OpcPackage opcPackage) throws Docx4JException {
		
		OpcPackage ret = null;
		try {
			ret = opcPackage.getClass().newInstance();
		} catch (InstantiationException e) {
			throw new Docx4JException("InstantiationException duplicating package", e);
		} catch (IllegalAccessException e) {
			throw new Docx4JException("IllegalAccessException duplicating package", e);
		}
		
//		contentType
		ret.setContentType(new ContentType(opcPackage.getContentType()));
//		partName
		ret.setPartName(opcPackage.getPartName());
//		relationships
		//is done in an another method
//		userData
//		ret.setUserData(opcPackage.getUserData());
//		contentTypeManager
		ret.setContentTypeManager(opcPackage.getContentTypeManager());
//		customXmlDataStorageParts
		ret.getCustomXmlDataStorageParts().putAll(opcPackage.getCustomXmlDataStorageParts());
//		docPropsCorePart
		ret.setPartShortcut(opcPackage.getDocPropsCorePart(), Namespaces.PROPERTIES_CORE);
//		docPropsCustomPart
		ret.setPartShortcut(opcPackage.getDocPropsCustomPart(), Namespaces.PROPERTIES_CUSTOM);
//		docPropsExtendedPart
		ret.setPartShortcut(opcPackage.getDocPropsExtendedPart(), Namespaces.PROPERTIES_EXTENDED);
		
		
//		externalResources
		ret.getExternalResources().putAll(opcPackage.getExternalResources());
//		handled
		//isn't needed as it is already loaded
//		parts
		//is done in an another method
//		partStore
		ret.setSourcePartStore(opcPackage.getSourcePartStore());
				
		return ret;
	}

	protected static void deepCopyRelationships(OpcPackage opcPackage,
			Base sourcePart,
			Base targetPart,
			Set<String> relationshipTypes) throws Docx4JException {
		
		RelationshipsPart sourceRelationshipsPart = sourcePart.getRelationshipsPart(false);
		Relationships sourceRelationships = (sourceRelationshipsPart != null ? 
									   		 sourceRelationshipsPart.getRelationships() : 
									   null);
		List<Relationship> sourceRelationshipList = (sourceRelationships != null ? 
									   				 sourceRelationships.getRelationship() : 
													 null);
		
		RelationshipsPart targetRelationshipsPart = null;
		Relationships targetRelationships = null;
		
		Relationship sourceRelationship = null;
		Relationship targetRelationship = null;
		
		Part sourceChild = null;
		Part targetChild = null;
		
		if ((sourceRelationshipList != null) && 
			(!sourceRelationshipList.isEmpty())) {
			
			targetRelationshipsPart = targetPart.getRelationshipsPart(); //create if needed
			targetRelationships = targetRelationshipsPart.getRelationships();
			
			for (int i=0; i<sourceRelationshipList.size(); i++) {
				
				sourceRelationship = sourceRelationshipList.get(i);
				//the Relationship doesn't have any references to parts, therefore it can be reused
				targetRelationships.getRelationship().add(sourceRelationship);
				
				if (sourceRelationship.getTargetMode()==null
						// per ECMA 376 4ed Part 2, capitalisation should be thus: "External"
						// but we can relax this..
						|| !"external".equals(sourceRelationship.getTargetMode().toLowerCase())) {
					sourceChild = sourceRelationshipsPart.getPart(sourceRelationship);
					targetChild = deepCopyPart(opcPackage, targetPart, sourceChild, relationshipTypes);
					if (sourceChild != targetChild) {
						deepCopyRelationships(opcPackage, sourceChild, targetChild, relationshipTypes);
					}
				}
			}
		}
	}

	protected static Part deepCopyPart(OpcPackage opcPackage, Base targetParent, Part sourcePart, Set<String> relationshipTypes) throws Docx4JException {

		//check if already handled
		Part ret = opcPackage.getParts().get(sourcePart.getPartName());
		if (ret == null) {
			//
			ret = copyPart(sourcePart, 
						   opcPackage, ((relationshipTypes == null) || 
								        relationshipTypes.contains(sourcePart.getRelationshipType()))
						   );
			opcPackage.getParts().put(ret);
			targetParent.setPartShortcut(ret, ret.getRelationshipType());
		}
		return ret;
	}


	protected static Part copyPart(Part part, OpcPackage targetPackage, boolean deepCopy) throws Docx4JException {
	Part ret = null;
		try {
			ret = part.getClass().getConstructor(PartName.class).newInstance(part.getPartName());
		} catch (Exception e) {
			throw new Docx4JException("Error cloning part of class " + part.getClass().getName(), e);
		}
		ret.setRelationshipType(part.getRelationshipType());
		ret.setContentType(new ContentType(part.getContentType()));
		if (targetPackage != null) {
			ret.setPackage(targetPackage);
		}
		if (deepCopy) {
			deepCopyContent(part, ret);
		}
		else {
			shallowCopyContent(part, ret);
		}
		return ret;
	}


	protected static void deepCopyContent(Part source, Part destination) throws Docx4JException {
		if (source instanceof BinaryPart) {
			byte[] byteData = new byte[((BinaryPart)source).getBuffer().limit()]; // = remaining() when current pos = 0
			((BinaryPart)source).getBuffer().get(byteData);
			((BinaryPart)destination).setBinaryData(ByteBuffer.wrap(byteData));
		}
		else if (source instanceof JaxbXmlPart) {
			((JaxbXmlPart)destination).setJaxbElement(XmlUtils.deepCopy(((JaxbXmlPart)source).getJaxbElement(), 
							((JaxbXmlPart)source).getJAXBContext()));
			((JaxbXmlPart)destination).setJAXBContext(((JaxbXmlPart)source).getJAXBContext());
			
			if (log.isDebugEnabled()
					&& (source instanceof MainDocumentPart)) {
				log.debug("source: " + ((JaxbXmlPart)source).getXML());
				log.debug("destination: " + ((JaxbXmlPart)destination).getXML());
			}
			
			
		}
		else if (source instanceof CustomXmlDataStoragePart) {
			CustomXmlDataStorage dataStorage = ((CustomXmlDataStoragePart)source).getData().factory();
			dataStorage.setDocument(
					(Document)((CustomXmlDataStoragePart)source).getData().getDocument().cloneNode(true));
			((CustomXmlDataStoragePart)destination).setData(dataStorage);
		}
		else if (source instanceof XmlPart) {
			((XmlPart)destination).setDocument((Document)((XmlPart)source).getDocument().cloneNode(true));
		}
		else {
			throw new IllegalArgumentException("Dont know how to handle a part of type " + source.getClass().getName());
		}
	}

	protected static void shallowCopyContent(Part source, Part destination) throws Docx4JException {
		if (source instanceof BinaryPart) {
			((BinaryPart)destination).setBinaryData(((BinaryPart)source).getBuffer());
		}
		else if (source instanceof JaxbXmlPart) {
			((JaxbXmlPart)destination).setJaxbElement(((JaxbXmlPart)source).getJaxbElement());
			((JaxbXmlPart)destination).setJAXBContext(((JaxbXmlPart)source).getJAXBContext());
		}
		else if (source instanceof CustomXmlDataStoragePart) {
			((CustomXmlDataStoragePart)destination).setData(((CustomXmlDataStoragePart)source).getData());
		}
		else if (source instanceof XmlPart) {
			((XmlPart)destination).setDocument(((XmlPart)source).getDocument());
		}
		else {
			throw new IllegalArgumentException("Dont know how to handle a part of type " + source.getClass().getName());
		}
	}
}
