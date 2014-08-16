/**
 *  Copyright 2013, Plutext Pty Ltd.
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.sdt.QueryString;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTDataBinding;
import org.docx4j.wml.CTSdtText;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Tag;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.opendope.xpaths.Xpaths.Xpath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Reverse the action of OpenDoPEHandler.
 * 
 * Useful where a user has edited an instance docx,
 * but you want to regenerate with fresh XML data.
 * 
 * This won't work if RemovalHandler has been used to
 * removed the content controls.
 * 
 * Note that any edits made by hand within a condition
 * or repeat will be lost, so it is recommended
 * that after regenerating from this reverted template, 
 * you do a document compare (eg in Word) against your 
 * previous instance to pick up any edits in a condition 
 * or repeat which this process will have dropped).
 * 
 * This class modifies the instance, so you'll need
 * to clone or copy it first if that's a problem.
 * 
 * @author jharrop
 *
 */
public class OpenDoPEReverter {

	private static Logger log = LoggerFactory.getLogger(OpenDoPEReverter.class);	
	
	
	private WordprocessingMLPackage openDopePkg;
	private WordprocessingMLPackage instancePkg;
	
	
	public OpenDoPEReverter(WordprocessingMLPackage openDopePkg, WordprocessingMLPackage instancePkg) {
		this.openDopePkg = openDopePkg;
		this.instancePkg = instancePkg; 
	}
	
	/*
		Algorithm:
		
		- find top level repeat/condition controls in template docx 
		
		part by part:-
		- traverse instance, making 2 lists
		  + sdt to be replaced (first encounter)
		  + sdt to be deleted
		then do the replacement
		
		What about the case where the one repeat is used twice?  That's ok, as long as there is some
		other object between the two lots of repeats.  
		Limitation: The user could screw this up, by manually inserting a paragraph between 2 instances of
		a repeat.  Result would be that an instance is kept, and converted back into an extra repeat!
		
		So have a sanity check that count of repeats and conditions in result matches what we
		have in the original template.
		
		A user could also delete a condition/repeat sdt entirely, in which case it won't get reverted.
		Add option to lock it to prevent this from happening?
	 */

	Map<BigInteger, Object> templateConditionSdtsByID = new HashMap<BigInteger, Object>();  
	Map<BigInteger, Object> templateRepeatSdtsByID = new HashMap<BigInteger, Object>();  
	
	public boolean revert() throws Docx4JException {
		
		// template docx:- find top level repeat/condition controls
		TopLevelSdtTemplateFinder sdtPrFinder = new TopLevelSdtTemplateFinder(false);
		findSdtsInTemplate(openDopePkg, sdtPrFinder);
		
		templateConditionSdtsByID = sdtPrFinder.conditionSdtsByID;
		templateRepeatSdtsByID = sdtPrFinder.repeatSdtsByID;
		
		// instance docx:-
		handleSdtsInInstance();
		
		return sanityCheck();
	}
	
	/**
		What about the case where the one repeat is used twice?  That's ok, as long as there is some
		other object between the two lots of repeats.  
		Limitation: The user could screw this up, by manually inserting a paragraph between 2 instances of
		a repeat.  Result would be that an instance is kept, and converted back into an extra repeat!
		
		So have a sanity check that count of repeats and conditions in result matches what we
		have in the original template.
	 */
	private boolean sanityCheck() throws Docx4JException {

		int expectedConditions = templateConditionSdtsByID.size();
		int expectedRepeats = templateRepeatSdtsByID.size();
		
		TopLevelSdtTemplateFinder sdtPrFinder = new TopLevelSdtTemplateFinder(true);
		findSdtsInTemplate(instancePkg, sdtPrFinder); // feed the instance through here to count (not bothering to count plain binds)
		
		boolean resultC = sdtPrFinder.conditionSdtsByID.size() == expectedConditions;
		if (!resultC) {
			log.error("Restored " + sdtPrFinder.conditionSdtsByID.size() + " condition SDTs but expected " + expectedConditions);
		}
		boolean resultR = sdtPrFinder.repeatSdtsByID.size() == expectedRepeats;
		if (!resultR) {
			log.error("Restored " + sdtPrFinder.repeatSdtsByID.size() + " repeat SDTs but expected " + expectedRepeats);
		}
		
		return (resultC && resultR);
	}
	
	// ======================================================================================
	// ====== openDopePkg template stuff
	
	private void findSdtsInTemplate(WordprocessingMLPackage pkg, TopLevelSdtTemplateFinder sdtPrFinder) throws Docx4JException {

		findSdtsInTemplatePart(pkg.getMainDocumentPart(), sdtPrFinder);

		// Add headers/footers
		RelationshipsPart rp = pkg.getMainDocumentPart()
				.getRelationshipsPart();
		for (Relationship r : rp.getRelationships().getRelationship()) {

			if (r.getType().equals(Namespaces.HEADER)) {
				findSdtsInTemplatePart((HeaderPart) rp.getPart(r), sdtPrFinder);
			} else if (r.getType().equals(Namespaces.FOOTER)) {
				findSdtsInTemplatePart((FooterPart) rp.getPart(r), sdtPrFinder);
			}
		}
	}

	private void findSdtsInTemplatePart(ContentAccessor content, TopLevelSdtTemplateFinder sdtPrFinder) throws Docx4JException {
		
		new TraversalUtil(content.getContent(), sdtPrFinder);
	}	
	
	private static class TopLevelSdtTemplateFinder extends CallbackImpl {
		
		private boolean instanceCountOnly;
		
		TopLevelSdtTemplateFinder(boolean instanceCountOnly) {
			this.instanceCountOnly = instanceCountOnly;
		}
		
		// Separate map for each
		// Maps are by SDT ID, since 2 distinct SDT could use the one condition or repeat
		Map<BigInteger, Object> conditionSdtsByID = new HashMap<BigInteger, Object>();  
		Map<BigInteger, Object> repeatSdtsByID = new HashMap<BigInteger, Object>();  
		
		@Override
		public List<Object> apply(Object o) {
			
			if (o instanceof org.docx4j.wml.SdtBlock 
					|| o instanceof org.docx4j.wml.SdtRun
					|| o instanceof org.docx4j.wml.CTSdtRow 
					|| o instanceof org.docx4j.wml.CTSdtCell ) {
				
				SdtPr sdtPr = OpenDoPEHandler.getSdtPr(o); 
				if (sdtPr!=null) {
					
					log.debug("Processing " + OpenDoPEHandler.getSdtPr(o).getId().getVal());
					Tag tag = sdtPr.getTag();

					log.debug(tag.getVal());

					HashMap<String, String> map = QueryString.parseQueryString(tag.getVal(), true);

					String conditionId = map.get(OpenDoPEHandler.BINDING_ROLE_CONDITIONAL);
					String repeatId = map.get(OpenDoPEHandler.BINDING_ROLE_REPEAT);

					if (conditionId != null) {

						conditionSdtsByID.put(sdtPr.getId().getVal(), o);

					} else if (repeatId != null) {

						repeatSdtsByID.put(sdtPr.getId().getVal(), o);
						
					} else if (instanceCountOnly) {
						
						String resultConditionId = map.get(OpenDoPEHandler.BINDING_RESULT_CONDITION_FALSE);
						String resultRepeatId = map.get(OpenDoPEHandler.BINDING_RESULT_RPTD);
						String resultRptdZeroId = map.get(OpenDoPEHandler.BINDING_RESULT_RPTD_ZERO);
						
						if (resultConditionId != null) {

							conditionSdtsByID.put(sdtPr.getId().getVal(), o);
							
						} else if (resultRptdZeroId != null) {

							repeatSdtsByID.put(sdtPr.getId().getVal(), o);
							
						} else if (resultRepeatId != null) {

							repeatSdtsByID.put(sdtPr.getId().getVal(), o);
						} 						
					}
					// not bothering to count plain binds
				}
			}			
			return null; 
		}
		
		// Don't recurse into an SDT.
		public boolean shouldTraverse(Object o) {
						
			if (o instanceof org.docx4j.wml.SdtBlock 
					|| o instanceof org.docx4j.wml.SdtRun
					|| o instanceof org.docx4j.wml.CTSdtRow 
					|| o instanceof org.docx4j.wml.CTSdtCell ) {
				return false;
			}
			
			return true;
		}		
	}

	// ======================================================================================
	// ====== instancePkg instance stuff
	
	private void handleSdtsInInstance() throws Docx4JException {

		handleSdtsInInstancePart(instancePkg.getMainDocumentPart());

		// Add headers/footers
		RelationshipsPart rp = instancePkg.getMainDocumentPart()
				.getRelationshipsPart();
		for (Relationship r : rp.getRelationships().getRelationship()) {

			if (r.getType().equals(Namespaces.HEADER)) {
				handleSdtsInInstancePart((HeaderPart) rp.getPart(r));
			} else if (r.getType().equals(Namespaces.FOOTER)) {
				handleSdtsInInstancePart((FooterPart) rp.getPart(r));
			}
		}
	}

	private TopLevelSdtInstanceFinder instanceSdtPrFinder;
	private void handleSdtsInInstancePart(ContentAccessor content ) throws Docx4JException {
		
		instanceSdtPrFinder = new TopLevelSdtInstanceFinder();		
		new TraversalUtil(content.getContent(), instanceSdtPrFinder);
		
		// Handle the condition sdt's we've found
		replaceConditions();
		
		handleRepeats();
		
		repairBoundSdts();
	}	
	
	private void replaceConditions() {
		
		/*
		 * A potential refinement would be to leave conditions which 
		 * evaluated to true, as-is, since there is no need to replace them.
		 * 
		 * But we'd have to recurse into them, to handle any repeats/conditions
		 * in them.   
		 * 
		 * We'd also have to recurse into the corresponding part of the 
		 * template docx, to get the replacement content controls.
		 */
		
		for ( Object entry : instanceSdtPrFinder.sdtsByConditionIDtoReplace ) {
			
			BigInteger key = OpenDoPEHandler.getSdtPr(entry).getId().getVal(); 
			
			Object replacement = templateConditionSdtsByID.get(key);
			
			// ok, replace
			Child child = (Child)entry;
			Object parent = child.getParent();
			log.info("parent: " + parent.getClass().getName() );
			
			if (parent instanceof ContentAccessor
					|| parent instanceof java.util.ArrayList) {
				
				List<Object> list = (parent instanceof ContentAccessor) ? 
										((ContentAccessor)parent).getContent() : (java.util.ArrayList)parent;	
				
				int index = 0;
				boolean found = false;
				for (Object o : list) {
					if (XmlUtils.unwrap(o).equals(child)) {
						found = true;
						break;
					}
					index++;
				}
				if (found) {
					list.set(index, replacement);
				} else {
					log.error("Couldn't find condition sdt: " + key );
				}
				
			} else {
				log.error("TODO " + parent.getClass().getName() );
			}
		}
	}

	private void handleRepeats() {
		
		// the sdts to replace
		for ( Object entry : instanceSdtPrFinder.repeatSdtToReplace ) {
			
//			String key = getRptdId( OpenDoPEHandler.getSdtPr(entry) ); 
			BigInteger key = OpenDoPEHandler.getSdtPr(entry).getId().getVal(); 
//			log.debug("repeat id: " + key);
			Object replacement = templateRepeatSdtsByID.get(key);
			if (replacement==null) {
				// shouldn't happen provided OpenDoPEHandler preserved SDT ID on first instance of repeat
				log.error( OpenDoPEHandler.getSdtPr(entry).getTag().getVal() + " - No replacement SDT with ID " + key.toString() );
			}
			
			// ok, replace
			Child child = (Child)entry;
			Object parent = child.getParent();
			log.info("parent: " + parent.getClass().getName() );
			
			if (parent instanceof ContentAccessor
					|| parent instanceof java.util.ArrayList) {
				
				List<Object> list = (parent instanceof ContentAccessor) ? 
										((ContentAccessor)parent).getContent() : (java.util.ArrayList)parent;	
				
				int index = 0;
				boolean found = false;
				for (Object o : list) {
					if (XmlUtils.unwrap(o).equals(child)) {
						found = true;
						break;
					}
					index++;
				}
				if (found) {
					list.set(index, replacement);
				} else {
					log.error("Couldn't find repeat sdt: " + key );
				}
								
			} else {
				log.error("TODO " + parent.getClass().getName() );
			}
		}
		
		// the sdts to remove
		for ( Object entry : instanceSdtPrFinder.repeatSdtToDelete ) {
			
			Child child = (Child)entry;
			Object parent = child.getParent();
			log.info("parent: " + parent.getClass().getName() );
			
			if (parent instanceof ContentAccessor
					|| parent instanceof java.util.ArrayList) {
				
				List<Object> list = (parent instanceof ContentAccessor) ? 
										((ContentAccessor)parent).getContent() : (java.util.ArrayList)parent;	

				int index = 0;
				boolean found = false;
				for (Object o : list) {
					
					if (o==null) {
						log.error(list.getClass().getName() + " contained null entry at index " + index);	
						// should not happen, provided replacements above are successful
					} else if (XmlUtils.unwrap(o).equals(child)) {
						found = true;
						break;
					}
					index++;
				}
				if (found ) {
					list.remove(index);
					log.debug("removed entry at index " + index);	
				} else {
					log.error("Couldn't find repeat sdt to delete");
				}
				
			} else {
				log.error("TODO " + parent.getClass().getName() );
			}
		}
		
	}
	
	
//	private String getRptdId(SdtPr sdtPr) {
//
//		Tag tag = sdtPr.getTag();
//		log.debug(tag.getVal());
//		HashMap<String, String> map = QueryString.parseQueryString(tag.getVal(), true);
//		String resultRepeatId = map.get(OpenDoPEHandler.BINDING_RESULT_RPTD);
//		String resultRptdZeroId = map.get(OpenDoPEHandler.BINDING_RESULT_RPTD_ZERO);	
//		
//		return resultRepeatId==null ? resultRptdZeroId : resultRepeatId;		
//	}
	
	/**
	 * If an SDT contained a hyperlink, we will have removed its w:dataBinding and w:text.
	 * So check SDT's and add back in if necessary.
	 * @throws Docx4JException 
	 */
	private void repairBoundSdts() throws Docx4JException {
		
		// We need to lookup the XPaths part.
		// In principle, that part should be identical in both 
		// openDopePkg and instancePkg.
		// We'll get it from instancePkg
		XPathsPart xPathsPart = ((WordprocessingMLPackage)instancePkg).getMainDocumentPart().getXPathsPart();
		if ( xPathsPart==null) {
			throw new Docx4JException("OpenDoPE XPaths part missing");
		} 
		
		for ( Object entry : instanceSdtPrFinder.boundSdtPotentialRepair ) {
			
			// If the SDT was made to contain a w:hyperlink, we had to remove:
			//   <w:dataBinding w:storeItemID="{5448916C-134B-45E6-B8FE-88CC1FFC17C3}" w:xpath="/myxml[1]/element2[1]" w:prefixMappings=""/>
			//   <w:text w:multiLine="true"/>
			// Now we need to add those back in
			SdtElement sdt = (SdtElement)entry;
			SdtPr sdtPr = sdt.getSdtPr();
			
			if (sdtPr.getDataBinding()==null ) {
				
				// Identify xpathId
				Tag tag = sdtPr.getTag();
				HashMap<String, String> map = QueryString.parseQueryString(tag.getVal(), true);
				String xpathId= map.get(OpenDoPEHandler.BINDING_ROLE_XPATH);
				
				// Look up - throws InputIntegrityException if not found
				Xpath xp = xPathsPart.getXPathById(xpathId);
			    
			    // Create object for dataBinding
			    CTDataBinding databinding = Context.getWmlObjectFactory().createCTDataBinding(); 
			    sdtPr.setDataBinding(databinding); 
			        databinding.setXpath( xp.getDataBinding().getXpath()); 
			        databinding.setPrefixMappings( xp.getDataBinding().getPrefixMappings()); 
			        databinding.setStoreItemID( xp.getDataBinding().getStoreItemID() );		
			        
			    // Create object for text (wrapped in JAXBElement) 
			    CTSdtText sdttext = Context.getWmlObjectFactory().createCTSdtText(); 
			    sdttext.setMultiLine(true);
			    JAXBElement<org.docx4j.wml.CTSdtText> sdttextWrapped = Context.getWmlObjectFactory().createSdtPrText(sdttext); 
			    sdtPr.getRPrOrAliasOrLock().add( sdttextWrapped);
			
			    // Empty the content
			    sdt.getSdtContent().getContent().clear();
			    	// TODO resolve the binding sans hyperlink?
			    	// TODO eventuallyEmptyList stuff
			}

		}
	}
	
	private static class TopLevelSdtInstanceFinder extends CallbackImpl {
		
		// not maps, since the one repeat (or condition) might occur twice
		
		List<Object> sdtsByConditionIDtoReplace = new ArrayList<Object>();  
		
		List<Object> repeatSdtToReplace = new ArrayList<Object>(); 
		List<Object> repeatSdtToDelete = new ArrayList<Object>();
		
		// If an SDT contained a hyperlink, we will have removed its w:dataBinding and w:text
		List<Object> boundSdtPotentialRepair = new ArrayList<Object>();
		
		// in order to distinguish between instances of a repeat which is used twice
		String previousRepeatID = null;
		
		@Override
		public List<Object> apply(Object o) {
			
			if (o instanceof org.docx4j.wml.SdtBlock 
					|| o instanceof org.docx4j.wml.SdtRun
					|| o instanceof org.docx4j.wml.CTSdtRow 
					|| o instanceof org.docx4j.wml.CTSdtCell ) {
				
				SdtPr sdtPr = OpenDoPEHandler.getSdtPr(o); 
				if (sdtPr!=null) {
					
					log.debug("Processing " + OpenDoPEHandler.getSdtPr(o).getId().getVal());
					Tag tag = sdtPr.getTag();

					if (tag!=null) {
						
						log.debug(tag.getVal());
	
						HashMap<String, String> map = QueryString.parseQueryString(tag.getVal(), true);
						
						String conditionId = map.get(OpenDoPEHandler.BINDING_ROLE_CONDITIONAL);
						String resultConditionId = map.get(OpenDoPEHandler.BINDING_RESULT_CONDITION_FALSE);
						String resultRepeatId = map.get(OpenDoPEHandler.BINDING_RESULT_RPTD);
						String resultRptdZeroId = map.get(OpenDoPEHandler.BINDING_RESULT_RPTD_ZERO);
	
						String xpathId= map.get(OpenDoPEHandler.BINDING_ROLE_XPATH);
						
						if (conditionId != null ) {
	
							sdtsByConditionIDtoReplace.add(o);
							previousRepeatID = null; 
							
						} else if (resultConditionId != null) { 
	
							sdtsByConditionIDtoReplace.add(o);
							previousRepeatID = null; 
							
						} else if (resultRptdZeroId != null) {
	
							repeatSdtToReplace.add( o);
							previousRepeatID = null; 
							
						} else if (resultRepeatId != null) {
	
							if (previousRepeatID!=null && previousRepeatID.equals(resultRepeatId)) {
								// it is second or subsequent
								repeatSdtToDelete.add( o);
							} else {
								repeatSdtToReplace.add( o);							
							}
							previousRepeatID = resultRepeatId; 
							
						} else if (xpathId != null) {
							
							boundSdtPotentialRepair.add(o);
							
						} else if (tag.getVal().contains("od:")){
							log.info("Ignoring (!) tag: " + tag.getVal());
						} else {
							log.debug("Ignoring tag: " + tag.getVal());
						}
					}
				}
			} else {
				previousRepeatID = null;
			}
			return null; 
		}
		
		// Don't recurse into an SDT.
		public boolean shouldTraverse(Object o) {
						
			if (o instanceof org.docx4j.wml.SdtBlock 
					|| o instanceof org.docx4j.wml.SdtRun
					|| o instanceof org.docx4j.wml.CTSdtRow 
					|| o instanceof org.docx4j.wml.CTSdtCell ) {
				return false;
			}
			
			return true;
		}		
	}
	
}
