/**
 *  Copyright 2018, Plutext Pty Ltd.
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

 **/
package org.docx4j.model.datastorage;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.Docx4J;
import org.docx4j.TextUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.model.sdt.QueryString;
import org.docx4j.openpackaging.URIHelper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.openpackaging.parts.opendope.JaxbCustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTSdtCell;
import org.docx4j.wml.CTSdtDate;
import org.docx4j.wml.CTSdtText;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.R;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Tag;
import org.docx4j.wml.Tc;
import org.opendope.xpaths.Xpaths.Xpath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Copy content control content back to the custom XML part.
 * 
 * Microsoft Word does this automatically for a content control
 * which has a w:databinding element.  
 * 
 * It doesn't do it for a rich text control.  Here we also
 * do that, ie for a content control with a tag such as:
 * 
 *       'od:progid=Word.Document'
 *       
 * The content is converted back to escaped WordML, and
 * injected following the relevant XPath.
 * 
 * This class provides a way
 * to update the XML part for cases
 * where editing is done in something other
 * than Word.
 * 
 * Of course, this class won't work if RemovalHandler
 * has been used to remove all SDTs or the XML part! 
  * 
 * Limitations:
 * - only the Main Document Part (for escaped WordML)
 * 
 * Replaces Enterprise's BindInverse.
 * 
 * @author jharrop
 * @since 6.0.0
 */
public class UpdateXmlFromDocumentSurface {
	
	/*
	 * TODO:
	 * - components
	 * - multiline
	 * - dates
	 * - pictures
	 */
	
	private static Logger log = LoggerFactory.getLogger(UpdateXmlFromDocumentSurface.class);	
	
	private WordprocessingMLPackage pkg;
	public WordprocessingMLPackage getPkg() {
		return pkg;
	}
	
	/**
	 * Copy contents of OpenDoPE content controls, including rich text content controls with tag 'od:progid=Word.Document'
	 * (ie as escaped Flat OPC XML), back into their associated custom XML part element. 
	 * 
	 * Styles and NDP are not required in Flat OPC emitted by docx4j for that Flat OPC to be imported
	 * into another docx based on those same styles/ndp; 
	 * but those styles & NDP are necessary for Word 2010 altChunk processing (if the fragment is to use those styles).
	 * Setting this option to false gives smaller file sizes. 
	 * 
	 * @param wordMLPackage
	 * @param supportStylesInWordAltChunkProcessing
	 * @throws Docx4JException
	 */
	public UpdateXmlFromDocumentSurface(WordprocessingMLPackage wordMLPackage, boolean supportStylesInWordAltChunkProcessing) throws Docx4JException {
		
		this.pkg = wordMLPackage;
		xPathsPart = pkg.getMainDocumentPart().getXPathsPart();
		if ( xPathsPart == null) {
			throw new Docx4JException("OpenDoPE XPaths part missing");
		} 	
		customXmlDataStorageParts = pkg.getCustomXmlDataStorageParts();
		
		this.supportStylesInWordAltChunkProcessing = supportStylesInWordAltChunkProcessing;
	}
	
	private XPathsPart xPathsPart;
	private Map<String, CustomXmlPart> customXmlDataStorageParts;
	
	private List<CustomXmlPart> updatedParts = new ArrayList<CustomXmlPart>();
	
	private boolean supportStylesInWordAltChunkProcessing;
	
	
	/**
	 * Update the contents of the relevant custom XML parts, with any
	 * edits made by the user in a rich text content control with a 
	 * tag containing od:progid=Word.Document (and an OpenDoPE XPath,
	 * of course).  
	 * 
	 * The WordML package itself is updated, and the parts are returned
	 * for convenience. 
	 *  
	 * @return
	 * @throws Docx4JException
	 */
	public List<CustomXmlPart> updateCustomXmlParts() throws Docx4JException {
		
		// For each relevant part,
		//
		// 1. traverse looking for content controls
		SdtFinder sdtFinder = new SdtFinder();
		findSdts(sdtFinder);
		
		// 2. for each content control
		//     - get its XPath (prefer OD XPath)
		//     - update element
		//     (if it appears at multiple locations in the docx,
		//      check that the values are the same. What to do if not?
		//      return false.)
		updateXmlFromSdts(sdtFinder.contentControls);
				
		for(CustomXmlPart cxp :  updatedParts) {
			
			if (cxp instanceof JaxbCustomXmlDataStoragePart) {
			
				((JaxbCustomXmlDataStoragePart)cxp).updateJaxbElementFromDocument();
			}

//			no need to do anything if its a CustomXmlDataStoragePart
//			
//			CustomXmlDataStoragePart cdsp = (CustomXmlDataStoragePart)cxp;
//			cdsp.getData().setDocument(this.cxpDocument);
//			log.warn("Updated CustomXmlDataStoragePart");
			
		}
		
		// return the XML file
		return updatedParts;
		
	}
	
	
	private CustomXmlPart getCustomXmlPart(Xpath xpath) {

		String storeItemId = xpath.getDataBinding().getStoreItemID();
		String xpathExp = xpath.getDataBinding().getXpath();
		String prefixMappings = xpath.getDataBinding().getPrefixMappings();
					
//			if (storeItemId.toUpperCase().equals(CORE_PROPERTIES_STOREITEMID)  ) {
//				
//				return pkg.getDocPropsCorePart().xpathGetString(xpath, prefixMappings);
//				
//			} else if (storeItemId.toUpperCase().equals(EXTENDED_PROPERTIES_STOREITEMID) ) {
//				
//				return pkg.getDocPropsExtendedPart().xpathGetString(xpath, prefixMappings);
//			} 
			
			return customXmlDataStorageParts.get(storeItemId.toLowerCase());
				// Also handles cover page properties (since we've allocated it a store item id)
				// Note that Word does not create that part until the user provides one or more prop values
			
			}	
	
	
	private void updateXmlFromSdts(List<SdtElement> contentControls) {
		
		for( SdtElement sdt : contentControls) {
			handleSdt(sdt);
		}
		
	}
	
	

	private void handleSdt(SdtElement sdt) {

		log.debug(sdt.getParent().getClass().getName());
		
		SdtPr sdtPr = sdt.getSdtPr();
		if (sdtPr!=null) {
			
			Tag tag = sdtPr.getTag();
			
			HashMap<String, String> map = null;
			if (tag!=null) {
				log.info(tag.getVal());
				map = QueryString.parseQueryString(
					tag.getVal(), true);
			}			
			
			SdtPr.Picture pic = (SdtPr.Picture)sdtPr.getByClass(SdtPr.Picture.class);
			if (sdtPr.getDataBinding()!=null && pic!=null) {

				log.warn("TODO: consider Picture handling {}", sdtPr.getId().getVal());
				
			} else if (map!=null && map.containsKey(OpenDoPEHandler.BINDING_CONTENTTYPE)
						&& map.get(OpenDoPEHandler.BINDING_CONTENTTYPE).equals("application/xhtml+xml")) {
					// Convert XHTML.
				log.error("TODO: add HTML import support");

			} else if (map!=null && map.containsKey(OpenDoPEHandler.BINDING_ROLE_COMPONENT) ) {
				String componentId = map.get(OpenDoPEHandler.BINDING_ROLE_COMPONENT);
				log.warn("TODO: consider component handling {}", sdtPr.getId().getVal());
				
			} else if (map!=null && map.containsKey(OpenDoPEHandler.BINDING_ROLE_XPATH) ) {
				
				boolean isMultiline = isMultiline(sdtPr);
				if (isMultiline) {
					// TODO convert multiline representation; not enough just to extract text!
					log.warn("TODO: add multiline support");					
				}

				String xpathId = map.get(OpenDoPEHandler.BINDING_ROLE_XPATH);
				
//				System.out.println(
//						XmlUtils.marshaltoString(
//								sdt.getSdtContent()
//								));
				
					
				if (sdtPr.getByClass(CTSdtDate.class)!=null) {

					log.warn("TODO: consider SdtDate handling {}", sdtPr.getId().getVal());
					
				} else {
					// Assume we can handle it, but there is also:
					/*
				     CTSdtCheckbox
				     CTSdtComboBox
				     CTSdtText
				     SdtPr.Equation
				     CTSdtDropDownList
			     */
					
//					DataBinding db = this.xpathsMap.get(xpathId).getDataBinding();
//					
//					String xpath = db.getXpath();
					String value = null;

					if (sdtPr.getByClass(SdtPr.RichText.class)!=null
							||  (map.containsKey(OpenDoPEHandler.BINDING_PROGID)
									&& map.get(OpenDoPEHandler.BINDING_PROGID).equals("Word.Document")) ) {

						// For now, we'll just 
						log.info(XmlUtils.marshaltoString(sdt.getSdtContent()));
						log.info("containing " + sdt.getClass().getName() );
					
						if (sdt instanceof SdtBlock
								|| sdt instanceof CTSdtCell) {
							try {
								WordprocessingMLPackage blockPkg = getAsDocx(sdt);
								
								// remove extraneous parts
								trimParts(blockPkg, blockPkg.getRelationshipsPart(),false);
						        
								// List the parts by walking the rels tree
								//debugListParts(blockPkg);
								
								if (blockPkg!=null) {
									// FlatOPC
									ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
									blockPkg.save(baos, Docx4J.FLAG_SAVE_FLAT_XML);
										// unused namespaces are trimmed in there
									
									value = baos.toString("UTF-8");
								}
							} catch (Docx4JException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							// unexpected
							log.warn("TODO: " + sdt.getClass().getName());
						}
						
					} else {

						value = TextUtils.getText(sdt.getSdtContent());
//						String value = TextUtils.getText(sdt.getSdtContent()) + System.currentTimeMillis();
//						System.out.println(value);
						
					}
					

					
					try {
						
						Xpath xpath = xPathsPart.getXPathById(xPathsPart.getJaxbElement(), xpathId);
						if (xpath==null) {
							throw new Docx4JException("Couldn't find xpath with id: " + xpathId);
						}
						
						String xpathExp = xpath.getDataBinding().getXpath();
						String prefixMappings = xpath.getDataBinding().getPrefixMappings();
						
						log.debug("Processing " + xpathExp);
						
						// inject it into the XML file
						CustomXmlPart cxp = getCustomXmlPart(xpath);
						if (cxp ==null) throw new Docx4JException("Couldn't find cxp with id: " + xpath.getDataBinding().getStoreItemID());
						
						//System.out.println(cxp.getClass().getName());

						cxp.setNodeValueAtXPath(xpathExp, value, prefixMappings);
						
						// add this cxp to the list of parts we've updated (usually there'll just be one)
						if (!updatedParts.contains(cxp))
							updatedParts.add(cxp);
						
					} catch (Docx4JException e) {
						log.error("Problems updating {}", xpathId);
						log.error(e.getMessage(), e);
					}
				}
				
				
			} else if (sdtPr.getDataBinding()!=null ) {

				log.debug("Adding non-OpenDoPE databound control {}", sdtPr.getId().getVal());
				
				// Microsoft repeats out of scope for now
				
				// if isRichText(sdtPr)
				// TODO and not(w:sdtPr/w:docPartGallery)
				// .. but which is that?
		        //@XmlElementRef(name = "docPartList", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
		        //@XmlElementRef(name = "docPartObj", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
				
				
			} else {
				if(log.isDebugEnabled()) {
					log.debug("Ignoring control with tag {}", sdtPr.getTag().getVal() );
                    log.debug("Not processing " + XmlUtils.marshaltoString(sdtPr, true));
                }
				 
			}
			
			
		}
		
	}
	
//	private void debugListParts(WordprocessingMLPackage pkg1) {
//
//		// List the parts by walking the rels tree
//		RelationshipsPart rp = pkg1.getRelationshipsPart();
//		StringBuilder sb = new StringBuilder();
//		PartsList.printInfo(rp.getPartName().getName(), null, rp, sb, "");
//		PartsList.traverseRelationships(pkg1, rp, sb, "    ");
//		System.out.println(sb.toString());
//		
//	}
	
//	private String extractValueFromTc(Tc tc) {
//		
//	}
//	private String extractValueFromP(P p) {
//		
//	}
//	private String extractValueFromR(R r) {
//		
//	}
	
	private void findSdts(SdtFinder sdtPrFinder) throws Docx4JException {

		findSdtsInPart(pkg.getMainDocumentPart(), sdtPrFinder);

		// Add headers/footers
		RelationshipsPart rp = pkg.getMainDocumentPart()
				.getRelationshipsPart();
		for (Relationship r : rp.getRelationships().getRelationship()) {

			if (r.getType().equals(Namespaces.HEADER)) {
				findSdtsInPart((HeaderPart) rp.getPart(r), sdtPrFinder);
			} else if (r.getType().equals(Namespaces.FOOTER)) {
				findSdtsInPart((FooterPart) rp.getPart(r), sdtPrFinder);
			}
		}
	}

	private void findSdtsInPart(ContentAccessor content, SdtFinder sdtFinder) throws Docx4JException {
		
		new TraversalUtil(content.getContent(), sdtFinder);
	}	
	
	private static class SdtFinder extends CallbackImpl {
		
		List<SdtElement> contentControls = new ArrayList<SdtElement>();  
		
		@Override
		public List<Object> apply(Object o) {
			
			if (o instanceof SdtElement ) {
				
				SdtPr sdtPr = OpenDoPEHandler.getSdtPr(o); 
				if (sdtPr!=null) {
					
					log.debug("Processing " + OpenDoPEHandler.getSdtPr(o).getId().getVal());
					Tag tag = sdtPr.getTag();

					log.debug(tag.getVal());
					// LibreOffice 5.3.3.2 drops tag!

					HashMap<String, String> map = QueryString.parseQueryString(tag.getVal(), true);

					String conditionId = map.get(OpenDoPEHandler.BINDING_ROLE_CONDITIONAL);
					String repeatId = map.get(OpenDoPEHandler.BINDING_ROLE_REPEAT);
					String xpathId = map.get(OpenDoPEHandler.BINDING_ROLE_XPATH);
					String componentId = map.get(OpenDoPEHandler.BINDING_ROLE_COMPONENT);

					if (conditionId != null) {

						log.debug("Ignoring condition {}", sdtPr.getId().getVal());

					} else if (repeatId != null) {

						log.debug("Ignoring repeat {}", sdtPr.getId().getVal());
						
					} else if (xpathId != null) {

						contentControls.add( (SdtElement)o);
						
					} else if (componentId != null) {

						log.warn("TODO: consider component handling {}", sdtPr.getId().getVal());

					} else if (sdtPr.getDataBinding() != null) {
						
						log.debug("Adding non-OpenDoPE databound control {}", sdtPr.getId().getVal());
						contentControls.add( (SdtElement)o);
						
						// Microsoft repeats out of scope for now
						
					} else {

						log.debug("Ignoring control with tag {}", sdtPr.getTag().getVal() );
						
					}
					
				}
			}			
			return null; 
		}
		
		public boolean shouldTraverse(Object o) {
						
			if (o instanceof R) {
				return false;
			}
			
			return true;
		}		
	}

	private boolean isMultiline(SdtPr sdtPr) {

		for (Object o : sdtPr.getRPrOrAliasOrLock() ) {
			
			o = XmlUtils.unwrap(o);
			if (o instanceof CTSdtText) {
				return ((CTSdtText)o).isMultiLine();
			}
		}
		return false;
	}

	
	private WordprocessingMLPackage getAsDocx(SdtElement sdtBlock) throws InvalidFormatException {
		
		WordprocessingMLPackage clone = (WordprocessingMLPackage)pkg.clone(); // TODO reuse this clone
		clone.getMainDocumentPart().getContent().clear();
		
		log.info("SdtContent: " + sdtBlock.getSdtContent().getContent().get(0).getClass().getName());
		
		if (sdtBlock.getSdtContent().getContent().size()==1
				&& (XmlUtils.unwrap(sdtBlock.getSdtContent().getContent().get(0)) instanceof Tc)) {
			
			// <w:sdtContent >
		    //    <w:tc>
			Tc tc = (Tc)XmlUtils.unwrap(sdtBlock.getSdtContent().getContent().get(0));
			clone.getMainDocumentPart().getContent().addAll(tc.getContent());
			
		} else {
			clone.getMainDocumentPart().getContent().addAll(sdtBlock.getSdtContent().getContent());
		}
		
		// Remove headers/footers 
		clone.getMainDocumentPart().getJaxbElement().getBody().setSectPr(null);
		
		//log.debug(clone.getMainDocumentPart().getXML());
		
		// remove CXPs
		List<Relationship> relsToRemove = new ArrayList<Relationship>();
		for (Relationship rel : clone.getMainDocumentPart().getRelationshipsPart().getJaxbElement().getRelationship() ) {
		
//	        /word/document.xml's rId4 is /customXml/item4.xml [org.docx4j.openpackaging.parts.opendope.QuestionsPart] http://schemas.openxmlformats.org/officeDocument/2006/relationships/customXml containing:org.opendope.questions.Questionnaire
			if (rel.getType().contains("customXml")) {
				relsToRemove.add(rel);
			}
			
		}
		for (Relationship rel : relsToRemove) {
			clone.getMainDocumentPart().getRelationshipsPart().removeRelationship(rel);
		}		
		
		
		try {
			// The advantage to using MergeDocx here is mainly that we don't save irrelevant images.
			List<WordprocessingMLPackage> wmlPkgList=new ArrayList<WordprocessingMLPackage>();
			wmlPkgList.add(clone);

			// Use reflection, so docx4j can be built
			// by users who don't have the MergeDocx utility
			Class<?> documentBuilder = Class.forName("com.plutext.merge.DocumentBuilder");			
			//Method method = documentBuilder.getMethod("merge", wmlPkgList.getClass());			
			Method[] methods = documentBuilder.getMethods(); 
			Method method = null;
			for (int j=0; j<methods.length; j++) {
				//System.out.println(methods[j].getName());
				if (methods[j].getName().equals("merge")) {
					method = methods[j];
					break;
				}
			}			
			if (method==null) throw new NoSuchMethodException();
			
			return (WordprocessingMLPackage)method.invoke(null, wmlPkgList);
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			log.warn("MergeDocx not available; falling back...");
			
			// bloated fallback...
			return clone;
		}		
	}


	private void trimParts(WordprocessingMLPackage output, RelationshipsPart rp, boolean modeDelete) throws Docx4JException {
        // keep footnotes, endnotes, numbering (think about that)
		
		boolean stripPropertiesParts = true;
		
		List<Relationship> deletions = new ArrayList<Relationship>();
		
		for ( Relationship r : rp.getRelationships().getRelationship() ) {
			
//			log.debug("For Relationship Id=" + r.getId() 
//					+ " Source is " + rp.getSourceP().getPartName() 
//					+ ", Target is " + r.getTarget() );
		
			if (r.getTargetMode() != null
					&& r.getTargetMode().equals("External") ) {
				continue;				
			}
			
			try {
				String resolvedPartUri = URIHelper.resolvePartUri(rp.getSourceURI(), new URI(r.getTarget() ) ).toString();		
				resolvedPartUri = resolvedPartUri.substring(1);	
				
				Part part = rp.getPart(r);
				
				if (part==null) {
					
				} else if (modeDelete) { //TODO: is this necessary?
					
					deletions.add(r );	
					// recursively delete
					if (part.getRelationshipsPart()==null) {
					} else {
						trimParts(output, part.getRelationshipsPart(), true);
					}
					
				} else if ( part instanceof org.docx4j.openpackaging.parts.ThemePart
							|| part instanceof org.docx4j.openpackaging.parts.WordprocessingML.FontTablePart) {				
					deletions.add(r );						
				} else if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart
						&& !supportStylesInWordAltChunkProcessing) {				
					deletions.add(r );						
				} else if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart) {
					
					if (supportStylesInWordAltChunkProcessing) {
						((StyleDefinitionsPart)part).getContents().setLatentStyles(null);
					} else {
						deletions.add(r );						
					}
				} else if (part.getPartName().getName().contains("stylesWithEffects")) {
					// Since docx4j 3.0 doesn't have a dedicated object for this part
					deletions.add(r );											
				} else if (stripPropertiesParts
							&& ( part instanceof org.docx4j.openpackaging.parts.DocPropsExtendedPart
									|| part instanceof org.docx4j.openpackaging.parts.DocPropsCorePart
									|| part instanceof org.docx4j.openpackaging.parts.DocPropsCustomPart
									|| part instanceof org.docx4j.openpackaging.parts.WordprocessingML.WebSettingsPart
									|| part instanceof org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart)) {
						
						deletions.add(r );
						
				} else if ( part instanceof org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart
						|| part instanceof org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart
						|| part instanceof org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart) {
					if (part.getRelationshipsPart()==null) {
					} else {
						trimParts(output, part.getRelationshipsPart(), false);
					}
				} else if (part instanceof JaxbCustomXmlDataStoragePart
						|| part instanceof org.docx4j.openpackaging.parts.CustomXmlDataStoragePart) {
					deletions.add(r );
				} else if (part instanceof org.docx4j.openpackaging.parts.WordprocessingML.GlossaryDocumentPart) {
					deletions.add(r );
					trimParts(output, part.getRelationshipsPart(), true);
				} else {
					log.debug(part.getClass().getName());
//						System.out.println(part.getClass().getName());
				}
								
					
			} catch (Exception e) {
				throw new Docx4JException("Failed to add parts from relationships", e);				
			}
						
		}
		
		for ( Relationship r : deletions) {
			rp.removeRelationship(r);
		}
		
	}
	
	public static void main(String[] args) throws Docx4JException {
		
		String input_DOCX = System.getProperty("user.dir") + "/Altered2010.docx";
		WordprocessingMLPackage wordMLPackage = Docx4J.load(new File(input_DOCX));
		
		UpdateXmlFromDocumentSurface updater = new UpdateXmlFromDocumentSurface(wordMLPackage, false);
		List<CustomXmlPart> parts = updater.updateCustomXmlParts();

		System.out.println(
				parts.get(0).getXML()
		);
		
		// or we can save the docx 
		updater.pkg.save(new File(System.getProperty("user.dir") + "/OUT_UpdateXmlFromDocumentSurface.docx"));
	}
	
}
