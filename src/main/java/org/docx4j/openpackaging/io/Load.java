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

package org.docx4j.openpackaging.io;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.docProps.coverPageProps.CoverPageProperties;
import org.docx4j.jaxb.Context;
import org.docx4j.model.datastorage.BindingHandler;
import org.docx4j.model.datastorage.CustomXmlDataStorage;
import org.docx4j.model.datastorage.CustomXmlDataStorageImpl;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.DocPropsCoverPagePart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BibliographyPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart;
import org.docx4j.openpackaging.parts.opendope.ComponentsPart;
import org.docx4j.openpackaging.parts.opendope.ConditionsPart;
import org.docx4j.openpackaging.parts.opendope.QuestionsPart;
import org.docx4j.openpackaging.parts.opendope.StandardisedAnswersPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;


@Deprecated
public class Load {

	private static Logger log = LoggerFactory.getLogger(Load.class);
	


	public Load() {
		super();
	}

	//public ContentTypeManager ctm;

	protected boolean loadExternalTargets = false; 
	public void loadExternalTargets(boolean loadExternalTargets) {
		this.loadExternalTargets = loadExternalTargets;
	}
	
	
	/**
	 * TODO.  I'd prefer this not to be static, but it needs to be,
	 * given that getRawPart is.  Maybe its not such a big deal,
	 * because its reasonable to assume that most people using docx4j
	 * will standardise on a single implementation of CustomXmlDataStorage? 
	 */
	static protected CustomXmlDataStorage customXmlDataStorageClass = null;
	/**
	 * Set your preferred implementation of the CustomXmlDataStorage
	 * interface.  Its factory method will be used to create new instances.
	 *  
	 * @param customXmlDataStorageClass the customXmlDataStorageClass to set
	 */
	static public void setCustomXmlDataStorageClass(
			CustomXmlDataStorage customXmlDataStorageClassVal) {
		customXmlDataStorageClass = customXmlDataStorageClassVal;
	}
	
	/**
	 * @return the customXmlDataStorageClass
	 */
	static public CustomXmlDataStorage getCustomXmlDataStorageClass() {
		try {
			if (customXmlDataStorageClass==null) {
				customXmlDataStorageClass = new CustomXmlDataStorageImpl();
			}			
			return customXmlDataStorageClass;
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * Get a Part (except a relationships part), but not its relationships part
	 * or related parts.  Useful if you need quick access to just this part,
	 * or if you wish to add a foreign part (ie a part from some other package).
	 * This can be called directly from outside the library, in which case 
	 * the Part will not be owned by a Package until the calling code makes it so.  
	 * @see  To get a Part and all its related parts, and add all to a package, use
	 * getPart.
	 * @param is
	 * @param ctm the ContentTypeManager associated with the foreign package
	 * @param resolvedPartUri  the part name
	 * @return
	 * 
	 * @throws URISyntaxException
	 * @throws InvalidFormatException
	 */
	public static Part getRawPart(InputStream is, ContentTypeManager ctm, String resolvedPartUri, Relationship rel)
			throws Docx4JException {
		
		Part part = null;

		try {
						
			try {
				
				part = ctm.getPart("/" + resolvedPartUri, rel);
								
				
				if (part instanceof org.docx4j.openpackaging.parts.ThemePart) {

					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcThemePart);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( is );
					
				} else if (part instanceof org.docx4j.openpackaging.parts.DocPropsCorePart ) {

					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcDocPropsCore);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( is );
					
				} else if (part instanceof org.docx4j.openpackaging.parts.DocPropsCustomPart ) {

					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcDocPropsCustom);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( is );
					
				} else if (part instanceof org.docx4j.openpackaging.parts.DocPropsExtendedPart ) {

					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcDocPropsExtended);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( is );
					
				} else if (part instanceof org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart ) {

					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jcCustomXmlProperties);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( is );
				
				} else if (part instanceof org.docx4j.openpackaging.parts.JaxbXmlPart) {
					
					// MainDocument part, Styles part, Font part etc

					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).setJAXBContext(Context.jc);
					((org.docx4j.openpackaging.parts.JaxbXmlPart)part).unmarshal( is );
					
				} else if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.BinaryPart) {
					
					log.debug("Detected BinaryPart " + part.getClass().getName() );
					((BinaryPart)part).setBinaryData(is);
					
				} else if (part instanceof org.docx4j.openpackaging.parts.CustomXmlDataStoragePart ) {
					
					// Is it a part we know?
					try {
				        XMLInputFactory xif = XMLInputFactory.newInstance();
				        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
				        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false); // a DTD is merely ignored, its presence doesn't cause an exception
				        XMLStreamReader xsr = xif.createXMLStreamReader(is);									
						
						Unmarshaller u = Context.jc.createUnmarshaller();
						Object o = u.unmarshal( xsr );						
						log.info(o.getClass().getName());
						
						PartName name = part.getPartName();
						
						if (o instanceof CoverPageProperties) {
							
							part = new DocPropsCoverPagePart(name);							
							((DocPropsCoverPagePart)part).setJaxbElement(
									(CoverPageProperties)o);
							
						} else if (o instanceof org.opendope.conditions.Conditions) {
							
							part = new ConditionsPart(name);
							((ConditionsPart)part).setJaxbElement(
									(org.opendope.conditions.Conditions)o);							
							
						} else if (o instanceof org.opendope.xpaths.Xpaths) {
							
							part = new XPathsPart(name);
							((XPathsPart)part).setJaxbElement(
									(org.opendope.xpaths.Xpaths)o);

						} else if (o instanceof org.opendope.questions.Questionnaire) {
							
							part = new QuestionsPart(name);
							((QuestionsPart)part).setJaxbElement(
									(org.opendope.questions.Questionnaire)o);

						} else if (o instanceof org.opendope.answers.Answers) {
							
							part = new StandardisedAnswersPart(name);
							((StandardisedAnswersPart)part).setJaxbElement(
									(org.opendope.answers.Answers)o);
							
						} else if (o instanceof org.opendope.components.Components) {
							
							part = new ComponentsPart(name);
							((ComponentsPart)part).setJaxbElement(
									(org.opendope.components.Components)o);

						} else if (o instanceof JAXBElement<?>
								&& XmlUtils.unwrap(o) instanceof org.docx4j.bibliography.CTSources) {
							part = new BibliographyPart(name);
							((BibliographyPart) part)
									.setJaxbElement((JAXBElement<org.docx4j.bibliography.CTSources>)o);

						} else {
							
							log.warn("No known part after all for CustomXmlPart " + o.getClass().getName());

							CustomXmlDataStorage data = getCustomXmlDataStorageClass().factory();					
							is.reset();
							data.setDocument(is); // Not necessarily JAXB, that's just our method name
							((org.docx4j.openpackaging.parts.CustomXmlDataStoragePart)part).setData(data);						
							
						}
						
					} catch (javax.xml.bind.UnmarshalException ue) {
						
						// No ...
						CustomXmlDataStorage data = getCustomXmlDataStorageClass().factory();	
						is.reset();
						data.setDocument(is); // Not necessarily JAXB, that's just our method name
						((org.docx4j.openpackaging.parts.CustomXmlDataStoragePart)part).setData(data);						
					}					
										
				} else {
					// Shouldn't happen, since ContentTypeManagerImpl should
					// return an instance of one of the above, or throw an
					// Exception.
					
					log.error("No suitable part found for: " + resolvedPartUri);
					return null;					
				}
			} catch (PartUnrecognisedException e) {
				
				// Try to get it as a binary part				
				part = new BinaryPart(new PartName("/" + resolvedPartUri));
				((BinaryPart) part).setBinaryData(is);
					
			}
		} catch (Exception ex) {
			// PathNotFoundException, ValueFormatException, RepositoryException, URISyntaxException
			ex.printStackTrace();
			throw new Docx4JException("Failed to getPart", ex);
		} 
		return part;
	}
		
	
	/**
	 * Find any /customXml/itemN.xml which have a props part
	 * which specifies a data store item ID.  
	 * 
	 * Register such parts.
	 * 
	 * @param p
	 */
	public static void registerCustomXmlDataStorageParts(OpcPackage pkg) {
		
		HashMap<PartName, Part> parts = pkg.getParts().getParts();
		
		// Strictly speaking, we're only interested in CustomXmlDataStorageParts
		// which are referred to in document.xml.rels ?
		// But it doesn't do much harm to register a CustomXmlDataStoragePart
		// which has a data store item ID, even if it isn't in document.xml.rels 
		
		//Iterator iterator = parts.entrySet().iterator();
		Collection col = parts.values();
		Iterator iterator = col.iterator();
		while( iterator.hasNext() ) {
			Part entry = (Part)iterator.next();
			
			if (entry instanceof CustomXmlPart) {
				log.debug("Found a CustomXmlPart, named " + entry.getPartName().getName() );
				String itemId = null;
				if (entry.getRelationshipsPart()==null) { 
					continue; 
				} else {
					log.debug(".. it has a rels part");
					// Look in its rels for rel of @Type customXmlProps (eg @Target="itemProps1.xml")
					Relationship r = entry.getRelationshipsPart().getRelationshipByType(
							Namespaces.CUSTOM_XML_DATA_STORAGE_PROPERTIES);
					if (r==null) {
						log.debug(".. but that doesn't point to a  customXmlProps part");
						continue;
					}
					CustomXmlDataStoragePropertiesPart customXmlProps = 
						(CustomXmlDataStoragePropertiesPart)entry.getRelationshipsPart().getPart(r);
					if (customXmlProps==null) {
						log.error(".. but the target seems to be missing?");
						
						if (entry instanceof CustomXmlDataStoragePart) {
							try {
								org.w3c.dom.Document document = ((CustomXmlDataStoragePart)entry).getData().getDocument();
								String localName = document.getDocumentElement().getLocalName();
								log.debug(localName);
								if (document.getDocumentElement().isDefaultNamespace("http://schemas.microsoft.com/?office/?2006/?coverPageProps")
										|| localName.equals("CoverPageProperties" ) ) {
									// Special case: CoverPageProperties
									// See "Office Well Defined Custom XML Parts"; see documentinteropinitiative.org/additionalinfo/IS29500/sect5.aspx
									// Has a rels part, but sometimes no target?  Sometimes it definitely does ...
									// Give it the store item id, Word 2007 seems to consistently allocate  
									itemId = BindingHandler.COVERPAGE_PROPERTIES_STOREITEMID.toLowerCase();
								} else {
									continue;
								}
							} catch (Docx4JException e) {
								e.printStackTrace();
								continue;
							}
						}
					} else {
						itemId = customXmlProps.getItemId().toLowerCase();
					}
				}
				log.debug("Identified/registered ds:itemId " + itemId);
				if (pkg.getCustomXmlDataStorageParts().get(itemId.toLowerCase())!=null) {
					log.warn("Duplicate CustomXML itemId " + itemId + "; check your source docx!");
				}
				pkg.getCustomXmlDataStorageParts().put(itemId, 
						(CustomXmlPart)entry );
			}
		}			
		
		
		
		
	}

}
