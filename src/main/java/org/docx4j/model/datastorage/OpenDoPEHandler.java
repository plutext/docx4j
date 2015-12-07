/**
 *  Copyright 2010-2013, Plutext Pty Ltd.
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

import static org.docx4j.model.datastorage.XPathEnhancerParser.enhanceXPath;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang.StringUtils;
import org.docx4j.Docx4jProperties;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.finders.TcFinder;
import org.docx4j.jaxb.Context;
import org.docx4j.model.sdt.QueryString;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.opendope.ComponentsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.w15.CTSdtRepeatedSection;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.CTDataBinding;
import org.docx4j.wml.CTLock;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Id;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tag;
import org.docx4j.wml.Tc;
import org.opendope.conditions.Condition;
import org.opendope.xpaths.Xpaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

public class OpenDoPEHandler {

	private static Logger log = LoggerFactory.getLogger(OpenDoPEHandler.class);
	
	private Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap = null; 
	private Map<String, Condition> conditionsMap = null; 

	public OpenDoPEHandler(WordprocessingMLPackage wordMLPackage)
			throws Docx4JException {

		this.wordMLPackage = wordMLPackage;
		
		

		if (wordMLPackage.getMainDocumentPart().getXPathsPart() == null) {
			log.info("OpenDoPE XPaths part missing (ok if you are just processing w15 repeatingSection)");
//			xPaths = new org.opendope.xpaths.Xpaths();
			xpathsMap = new HashMap<String, org.opendope.xpaths.Xpaths.Xpath>();
		} else {
			org.opendope.xpaths.Xpaths xPaths = wordMLPackage.getMainDocumentPart().getXPathsPart()
					.getJaxbElement();
            if(log.isDebugEnabled()) {
                log.debug(XmlUtils.marshaltoString(xPaths, true, true));
            }
			
			xpathsMap = new HashMap<String, org.opendope.xpaths.Xpaths.Xpath>(2*xPaths.getXpath().size());
			
			for (Xpaths.Xpath xp : xPaths.getXpath() ) {
				
				if (xpathsMap.put(xp.getId(), xp)!=null) {
					log.error("Duplicates in XPaths part: " + xp.getId());
				}
				// TODO key should include storeItemID?
			}
			
		}
		if (wordMLPackage.getMainDocumentPart().getConditionsPart() != null) {
			org.opendope.conditions.Conditions conditions = wordMLPackage.getMainDocumentPart()
					.getConditionsPart().getJaxbElement();
            if(log.isDebugEnabled()) {
                log.debug(XmlUtils.marshaltoString(conditions, true, true));
            }
			
			conditionsMap = new HashMap<String, Condition>(2*conditions.getCondition().size());
			
			for (Condition c : conditions.getCondition()) {
				if (conditionsMap.put(c.getId(), c)!=null) {
					log.error("Duplicates in Conditions part: " + c.getId());
				}
			}
		}
		if (wordMLPackage.getMainDocumentPart().getComponentsPart() != null) {
			components = wordMLPackage.getMainDocumentPart()
					.getComponentsPart().getJaxbElement();
            if(log.isDebugEnabled()) {
                log.debug(XmlUtils.marshaltoString(components, true, true));
            }
		}

		shallowTraversor = new ShallowTraversor();
		shallowTraversor.wordMLPackage = wordMLPackage;
		
		bookmarkRenumber = new BookmarkRenumber(wordMLPackage);
	}

	private WordprocessingMLPackage wordMLPackage;
	private ShallowTraversor shallowTraversor;
	
	private BookmarkRenumber bookmarkRenumber;
	
	/**
	 * Provide a way to for user to fetch the starting bookmark ID number
	 * for use in the next stage (ie Binding Traverse).
	 * 
	 * If it isn't fetched/set, the value will have to be recalculated (less efficient).
	 *  
	 * @since 3.2.1
	 */	
	public AtomicInteger getNextBookmarkId() {
		return bookmarkRenumber.getBookmarkId();
	}

	public final static String BINDING_ROLE_XPATH = "od:xpath";

	public final static String BINDING_ROLE_CONDITIONAL = "od:condition";
	public final static String BINDING_RESULT_CONDITION_FALSE = "od:resultConditionFalse";

	public final static String BINDING_ROLE_REPEAT = "od:repeat";
	public final static String BINDING_RESULT_RPTD_ZERO = "od:resultRepeatZero";
	public final static String BINDING_RESULT_RPTD_ZERO_W15 = "w15:resultRepeatZero";
	public final static String BINDING_RESULT_RPTD = "od:rptd";
	
	// Repeat position condition (eg second last entry)
	public final static String BINDING_ROLE_RPT_POS_CON = "od:RptPosCon";  // see bind.xslt

	public final static String BINDING_ROLE_NARRATIVE = "od:narrative";

	public final static String BINDING_ROLE_COMPONENT = "od:component";
	public final static String BINDING_ROLE_COMPONENT_BEFORE = "od:continuousBefore";
	public final static String BINDING_ROLE_COMPONENT_AFTER = "od:continuousAfter";

	public final static String BINDING_CONTENTTYPE = "od:ContentType";
	public final static String BINDING_HANDLER = "od:Handler";
	public final static String BINDING_PROGID = "od:progid"; // eg =Word.Document
	/*
	 * --------------------------------------------------------------------------
	 * - Pre-processing of content controls which have a tag containing
	 * "bindingrole"
	 */

	private org.opendope.components.Components components;

// TODO consider whether to reinstate.  User would need to choose between 	
//  conditional SDT removal, and reverting functionality.
	
//	private boolean removeSdtCellsOnFailedCondition;
//
//	/**
//	 * Configure, how the preprocessor handles conditions on table cells.
//	 *
//	 * If set to <code>false</code>, conditional SDT cells are replaced by empty
//	 * cells. This is the default behavior.
//	 *
//	 * If set to <code>true</code>, conditional SDT cells are removed entirely.
//	 * Note that the table geometry is not changed; hence this works better
//	 * without dynamic table widths / no global width settings.
//	 *
//	 * This affects all future calls on the {@link #preprocess} method for this
//     * instance.
//	 *
//	 * @param removeSdtCellsOnFailedCondition
//	 *            The new value for the cell removal flag.
//	 */
//	public void setRemoveSdtCellsOnFailedCondition(
//			boolean removeSdtCellsOnFailedCondition) {
//		this.removeSdtCellsOnFailedCondition = removeSdtCellsOnFailedCondition;
//	}

	/**
	 * Preprocess content controls which have tag
	 * "od:condition|od:repeat|od:component".
	 *
	 * It is "preprocess" in the sense that it is "pre" opening in Word
	 *
	 * The algorithm is as follows:
	 *
	 * Inject components first.
	 *
	 * Look at each top level SDT (ShallowTraversor). If it does not have a real
	 * data binding, it might have a bindingrole tag we need to process
	 * (processBindingRoleIfAny).
	 *
	 * Conditionals are easy.
	 *
	 * processRepeat method:
	 *
	 * - clones the sdt n times
	 *
	 * - invokes DeepTraversor which changes xpath binding on descendant sdts
	 * (both sdts with real bindings and sdts with bindingrole tags).
	 *
	 * It is not the job of DeepTraversor to expand out any other repeats it
	 * might encounter, or to resolve conditionals.
	 *
	 * Those things are done by ShallowTraversor, to which control returns, as
	 * it continues its traverse.
	 *
	 * The implementation of 13 Sept 2010 replaced the previous XPath based
	 * implementation, which did not support nested repeats. I've chosen to
	 * build this around TraversalUtil, instead of using XSLT, and this seems to
	 * have worked out nicely.
	 *
	 * The implementation of 10 October 2010 replaced the v1 conventions
	 * implementation with a v2 implementation. The main method in this class
	 * can convert v1 documents to v2. The v2 implementation is not yet
	 * complete. All v1 features are implemented, but not the new v2 stuff (eg
	 * complex conditions).
	 *
	 * @param documentPart
	 * @throws Exception
	 */
	public WordprocessingMLPackage preprocess() throws Docx4JException {

		do {
			// A component can apply in both the main document part,
			// and in headers/footers. See further
			// http://forums.opendope.org/Support-components-in-headers-footers-tp2964174p2964174.html
			// A component added to the
			// main document part could add new headers/footers.
			// So we need to work out what parts to preprocess
			// here inside this do loop.
			Set<ContentAccessor> partList = getParts(wordMLPackage);

			// Process repeats and conditionals.
			try {
				for (ContentAccessor part : partList) {
					new TraversalUtil(part, shallowTraversor);
				}
			} catch (InputIntegrityException iie) { // RuntimeException
				throw new Docx4JException(iie.getMessage(), iie);
			}

			// Convert any sdt with <w:tag w:val="od:component=comp1"/>
			// to altChunk, and for MergeDocx users, to
			// real WordML.
			for (ContentAccessor part : partList) {
				wordMLPackage = fetchComponents(wordMLPackage, part);
			}

		} while (justGotAComponent);
		// ie repeat the whole process if you got a component
		
		// Write our maps back to their parts
		// XPaths
		if (wordMLPackage.getMainDocumentPart().getXPathsPart() == null) { 
			// OpenDoPE XPaths part missing is ok if you are just processing w15 repeatingSection)");
		} else {
			wordMLPackage.getMainDocumentPart().getXPathsPart().getContents().getXpath().clear();
			wordMLPackage.getMainDocumentPart().getXPathsPart().getContents().getXpath().addAll(xpathsMap.values());
		}
		// Conditions
		if (wordMLPackage.getMainDocumentPart().getConditionsPart() != null) {
			wordMLPackage.getMainDocumentPart().getConditionsPart().getContents().getCondition().clear();
			wordMLPackage.getMainDocumentPart().getConditionsPart().getContents().getCondition().addAll(conditionsMap.values());
		}

		return wordMLPackage;
	}

	private boolean justGotAComponent = false;

	private static DocxFetcher docxFetcher;

	public static DocxFetcher getDocxFetcher() {
		return docxFetcher;
	}

	public static void setDocxFetcher(DocxFetcher docxFetcher) {
		OpenDoPEHandler.docxFetcher = docxFetcher;
	}

	protected static Set<ContentAccessor> getParts(WordprocessingMLPackage srcPackage) {

		Set<ContentAccessor> partList = new HashSet<ContentAccessor>();

		partList.add(srcPackage.getMainDocumentPart());

		// Add headers/footers
		RelationshipsPart rp = srcPackage.getMainDocumentPart()
				.getRelationshipsPart();
		for (Relationship r : rp.getRelationships().getRelationship()) {

			if (r.getType().equals(Namespaces.HEADER)) {
				partList.add((HeaderPart) rp.getPart(r));
			} else if (r.getType().equals(Namespaces.FOOTER)) {
				partList.add((FooterPart) rp.getPart(r));
			}
		}

		return partList;
	}

	private WordprocessingMLPackage fetchComponents(
			WordprocessingMLPackage srcPackage, ContentAccessor contentAccessor)
			throws Docx4JException {

		// convert components to altChunk
		Map<Integer, CTAltChunk> replacements = new HashMap<Integer, CTAltChunk>();
		Integer index = 0;
		justGotAComponent = false;

		LinkedList<Integer> continuousBeforeIndex = new LinkedList<Integer>();
		List<Boolean> continuousBefore = new ArrayList<Boolean>();

		List<Boolean> continuousAfter = new ArrayList<Boolean>();

		for (Object block : contentAccessor.getContent()) {

			// Object ublock = XmlUtils.unwrap(block);
			if (block instanceof org.docx4j.wml.SdtBlock) {

				org.docx4j.wml.SdtBlock sdt = (org.docx4j.wml.SdtBlock) block;

				Tag tag = getSdtPr(sdt).getTag();

				if (tag == null) {
					List<Object> newContent = new ArrayList<Object>();
					newContent.add(sdt);
					continue;
				}

				log.info(tag.getVal());

				HashMap<String, String> map = QueryString.parseQueryString(tag.getVal(),
						true);

				String componentId = map.get(BINDING_ROLE_COMPONENT);
				if (componentId == null)
					continue;

				// Convert the sdt to a w:altChunk
				// .. get the IRI
				String iri = ComponentsPart.getComponentById(components,
						componentId).getIri();
				log.debug("Fetching " + iri);

				if (docxFetcher == null) {
					log.error("You need a docxFetcher (and the MergeDocx extension) to fetch components");
					return srcPackage;
				}

				// .. create the part
				AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(
						getNewPartName("/chunk", ".docx", srcPackage
								.getMainDocumentPart().getRelationshipsPart()));
				afiPart.setBinaryData(docxFetcher.getDocxFromIRI(iri));

				afiPart.setContentType(new ContentType(
						"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml")); // docx

				Relationship altChunkRel = srcPackage.getMainDocumentPart()
						.addTargetPart(afiPart);
				CTAltChunk ac = Context.getWmlObjectFactory()
						.createCTAltChunk();
				ac.setId(altChunkRel.getId());

				replacements.put(index, ac);

				/*
				 * 2011 12 11 TODO.  Rethink support for
				 * od:continuousBefore and od:continuousAfter.
				 */

				// This is handled in this class
				if (map.get(BINDING_ROLE_COMPONENT_BEFORE) != null
						&& map.get(BINDING_ROLE_COMPONENT_BEFORE)
								.equals("true")) {
					continuousBefore.add(Boolean.TRUE);
					continuousBeforeIndex.addFirst(index);
					log.info("ctsBefore index: " + index);
				} else {
					continuousBefore.add(Boolean.FALSE);
					continuousBeforeIndex.addFirst(index);
				}

				// The following is handled in ProcessAltChunk
				if (map.get(BINDING_ROLE_COMPONENT_AFTER) != null
						&& map.get(BINDING_ROLE_COMPONENT_AFTER).equals("true")) {
					continuousAfter.add(Boolean.TRUE);
				} else {
					continuousAfter.add(Boolean.TRUE);
				}

				justGotAComponent = true;
			}
			index++;
		}

		if (!justGotAComponent) {
			return srcPackage;
		}

		// Now replace in list
		for (Integer key : replacements.keySet()) {
			contentAccessor.getContent().set(key, replacements.get(key));
		}

		// Go through docx in reverse order
		List<Object> bodyChildren = contentAccessor.getContent();
		int i = 0;
		for (Integer indexIntoBody : continuousBeforeIndex) {

			if (continuousBefore.get(i)) {
				// Element before the w:altChunk
				if (indexIntoBody == 0) {
					// // Insert a sectPr right at the beginning of the docx?
					// // TODO check this isn't necessary
					// SectPr newSectPr =
					// Context.getWmlObjectFactory().createSectPr();
					// SectPr.Type type =
					// Context.getWmlObjectFactory().createSectPrType();
					// type.setVal("continuous");
					// newSectPr.setType( type );
					//
					// bodyChildren.add(0, newSectPr);

				} else {
					Object block = bodyChildren
							.get(indexIntoBody.intValue() - 1);
					if (block instanceof P && ((P) block).getPPr() != null
							&& ((P) block).getPPr().getSectPr() != null) {
						makeContinuous(((P) block).getPPr().getSectPr());
					} else if (block instanceof P) {
						// More likely
						PPr ppr = ((P) block).getPPr();
						if (ppr == null) {
							ppr = Context.getWmlObjectFactory().createPPr();
							((P) block).setPPr(ppr);
						}
						SectPr newSectPr = Context.getWmlObjectFactory()
								.createSectPr();
						SectPr.Type type = Context.getWmlObjectFactory()
								.createSectPrType();
						type.setVal("continuous");
						newSectPr.setType(type);

						ppr.setSectPr(newSectPr);
					} else {
						// Equally likely - its a table or something, so add a p
						P newP = Context.getWmlObjectFactory().createP();
						PPr ppr = Context.getWmlObjectFactory().createPPr();
						newP.setPPr(ppr);

						SectPr newSectPr = Context.getWmlObjectFactory()
								.createSectPr();
						SectPr.Type type = Context.getWmlObjectFactory()
								.createSectPrType();
						type.setVal("continuous");
						newSectPr.setType(type);
						ppr.setSectPr(newSectPr);

						bodyChildren.add(indexIntoBody.intValue(), newP); // add
																			// before
																			// altChunk
					}
				}
			}
			// else nothing specified, so go with normal MergeDocx behaviour

			i++;
		}

		// process altChunk
		try {
			// Use reflection, so docx4j can be built
			// by users who don't have the MergeDocx utility
			Class<?> documentBuilder = Class
					.forName("com.plutext.merge.ProcessAltChunk");
			// Method method = documentBuilder.getMethod("merge",
			// wmlPkgList.getClass());
			Method[] methods = documentBuilder.getMethods();
			Method processMethod = null;
			for (int j = 0; j < methods.length; j++) {
				log.debug(methods[j].getName());
				if (methods[j].getName().equals("process")) {
					processMethod = methods[j];
				}
			}
			if (processMethod == null )
				throw new NoSuchMethodException();
			return (WordprocessingMLPackage) processMethod.invoke(null,
					srcPackage);

		} catch (ClassNotFoundException e) {
			extensionMissing(e);
			justGotAComponent = false;
			return srcPackage;
			// throw new Docx4JException("Problem processing w:altChunk", e);
		} catch (NoSuchMethodException e) {
			// Degrade gracefully
			extensionMissing(e);
			justGotAComponent = false;
			return srcPackage;
			// throw new Docx4JException("Problem processing w:altChunk", e);
		} catch (Exception e) {
			throw new Docx4JException("Problem processing w:altChunk", e);
		}
	}

	public void makeContinuous(SectPr sectPr) {

		if (sectPr == null) {
			log.warn("sectPr was null");
			return;
		}

		SectPr.Type type = Context.getWmlObjectFactory().createSectPrType();
		type.setVal("continuous");
		sectPr.setType(type);

		// columns, endnotes, footnotes, formprot, line numbers are OK

		// null out certain page level section properties
		sectPr.setBidi(null);
		sectPr.setDocGrid(null);
		sectPr.setPaperSrc(null);
		sectPr.setPgBorders(null);
		sectPr.setPgMar(null);
		sectPr.setPgNumType(null);
		sectPr.setPgSz(null);
		sectPr.setPrinterSettings(null);
		sectPr.setSectPrChange(null);
		sectPr.setTitlePg(null);
		sectPr.setVAlign(null);
	}

	private PartName getNewPartName(String prefix, String suffix,
			RelationshipsPart rp) throws InvalidFormatException {

		PartName proposed = null;
		int i = 1;
		do {

			if (i > 1) {
				proposed = new PartName(prefix + i + suffix);
			} else {
				proposed = new PartName(prefix + suffix);
			}
			i++;

		} while (rp.getRel(proposed) != null);

		return proposed;

	}

	public void extensionMissing(Exception e) {
		log.error("\n" + e.getClass().getName() + ": " + e.getMessage() + "\n");
		log.error("* You don't appear to have the MergeDocx paid extension,");
		log.error("* which is necessary to merge docx, or process altChunk.");
		log.error("* Purchases of this extension support the docx4j project.");
		log.error("* Please visit www.plutext.com if you want to buy it.");
	}

	// private static void preprocessRun(WordprocessingMLPackage wordMLPackage,
	// ContentAccessor content) throws Docx4JException {
	//
	// log.info("\n\n Preprocess run.. \n\n");
	//
	// MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
	//
	// if (wordMLPackage.getMainDocumentPart().getXPathsPart()==null) {
	// throw new Docx4JException("OpenDoPE XPaths part missing");
	// } else {
	// xPaths =
	// wordMLPackage.getMainDocumentPart().getXPathsPart().getJaxbElement();
	// log.debug( XmlUtils.marshaltoString(xPaths, true, true));
	// }
	// if (wordMLPackage.getMainDocumentPart().getConditionsPart()!=null) {
	// conditions =
	// wordMLPackage.getMainDocumentPart().getConditionsPart().getJaxbElement();
	// log.debug( XmlUtils.marshaltoString(conditions, true, true));
	// }
	// if (wordMLPackage.getMainDocumentPart().getComponentsPart()!=null) {
	// components =
	// wordMLPackage.getMainDocumentPart().getComponentsPart().getJaxbElement();
	// log.debug( XmlUtils.marshaltoString(components, true, true));
	// }
	//
	// org.docx4j.wml.Document wmlDocumentEl =
	// (org.docx4j.wml.Document)documentPart.getJaxbElement();
	// Body body = wmlDocumentEl.getBody();
	//
	// shallowTraversor.wordMLPackage = wordMLPackage;
	//
	// new TraversalUtil(body, shallowTraversor);
	//
	// }

	// private static XPathsPart getXPathsPart(WordprocessingMLPackage
	// wordMLPackage) {
	// wordMLPackage.getParts().
	// }

	/**
	 * This traversor duplicates the repeats, and removes false conditonals
	 */
	private class ShallowTraversor implements TraversalUtil.Callback {

		// private static Logger log = LoggerFactory.getLogger(ShallowTraversor.class);

		WordprocessingMLPackage wordMLPackage;

		@Override
		public List<Object> apply(Object wrapped) throws RuntimeException {

			// apply processSdt to any sdt
			// which might be a conditional|repeat
			Object o = XmlUtils.unwrap(wrapped);
			if (o instanceof org.docx4j.wml.SdtBlock
					|| o instanceof org.docx4j.wml.SdtRun
					|| o instanceof org.docx4j.wml.CTSdtRow
					|| o instanceof org.docx4j.wml.CTSdtCell) {

				SdtPr sdtPr = getSdtPr(o);
				if (sdtPr.getDataBinding() == null)  {
					// a real binding attribute trumps any tag
					return processBindingRoleIfAny(wordMLPackage, o);
				} else if (getW15RepeatingSection(sdtPr)!=null) {
					return  processW15Repeat( o, wordMLPackage.getCustomXmlDataStorageParts());					
				}

			} else {
				// log.warn("TODO: Handle " + o.getClass().getName() +
				// " (if that's an sdt)");
			}

			// Otherwise just preserve the content
			List<Object> newContent = new ArrayList<Object>();

			newContent.add(wrapped); // we want the JAXBElement (if any)
			return newContent;

		}

		@Override
		public boolean shouldTraverse(Object o) {

			// we want to traverse all sdts, since an sdt which
			// doesn't have a binding role might contain one
			// which does

			return true;
		}

		@Override
		public List<Object> getChildren(Object o) {
			return TraversalUtil.getChildrenImpl(o);
		}

		@Override
		public void walkJAXBElements(Object parent) {
			// Breadth first

			List<Object> newChildren = new ArrayList<Object>();

			Object parentUnwrapped = XmlUtils.unwrap(parent);
			List<Object> children = getChildren(parentUnwrapped);
			if (children == null) {
				log.debug("no children: " + parentUnwrapped.getClass().getName());
				return;
			} else {
				for (Object o : children) {
					newChildren.addAll(this.apply(o));
				}
			}
			// Replace list, so we'll traverse all the new sdts we've just
			// created
			TraversalUtil.replaceChildren(parentUnwrapped, newChildren);

			children = getChildren(parentUnwrapped);
			if (children == null) {
				log.debug("no children: " + parentUnwrapped.getClass().getName());
			} else {
				for (Object o : children) {

					// *** this.apply(o);

					if (this.shouldTraverse(o)) {
						walkJAXBElements(o);
					}

				}
			}
		}

	}
	
	private CTSdtRepeatedSection getW15RepeatingSection(SdtPr sdtPr) {
		
		return (CTSdtRepeatedSection)sdtPr.getByClass(CTSdtRepeatedSection.class);
	}

	/**
	 * This applies to any sdt which might be a conditional|repeat
	 *
	 * @param wordMLPackage
	 * @param sdtParent
	 * @param sdt
	 * @param tag
	 * @param sdtContent
	 * @return
	 * @throws Exception
	 */
	private List<Object> processBindingRoleIfAny(
			WordprocessingMLPackage wordMLPackage, Object sdt) {
		
		Id id = getSdtPr(sdt).getId();
		if (id!=null) {
			log.debug("Processing " + id.getVal());
		}
		
		Tag tag = getSdtPr(sdt).getTag();

		if (tag == null) {
			List<Object> newContent = new ArrayList<Object>();
			newContent.add(sdt);
			return newContent;
		}

		log.info(tag.getVal());

		HashMap<String, String> map = QueryString.parseQueryString(tag.getVal(), true);

		String conditionId = map.get(BINDING_ROLE_CONDITIONAL);
		String repeatId = map.get(BINDING_ROLE_REPEAT);
		String xp = map.get(BINDING_ROLE_XPATH);
		
		if (conditionId == null && repeatId == null && xp == null) {
			List<Object> newContent = new ArrayList<Object>();
			newContent.add(sdt);
			return newContent;
		}

		Map<String, CustomXmlPart> customXmlDataStorageParts = wordMLPackage.getCustomXmlDataStorageParts();

		if (conditionId != null) {

			log.info("Processing Conditional: " + tag.getVal());

			// At present, this only handles simple conditions
			//Condition c = ConditionsPart.getConditionById(conditions, conditionId);
			Condition c = conditionsMap.get(conditionId);
			if (c == null) {
				log.error("Missing condition " + conditionId);
			}

			if ( c.evaluate(wordMLPackage, customXmlDataStorageParts, conditionsMap, xpathsMap) ) {
				log.debug("so keeping");

				List<Object> newContent = new ArrayList<Object>();
				newContent.add(sdt);
				return newContent;

			} else if (reverterSupported){
				return conditionFalse(sdt);
			} else {
				return new ArrayList<Object>(); // effectively, delete
				// Potentially slightly faster processing, since 
				// the conditionFalse sdt doesn't need to be created.
				// The document is only slightly smaller, since conditionFalse sdt doesn't have a lot of content.
				// OpenDoPEIntegrity is responsible for handling the case where
				// this creates an empty table cell
			}

		} else if (repeatId != null) {

			log.info("Processing OpenDoPE Repeat: " + tag.getVal());

			return processOpenDopeRepeat(sdt, customXmlDataStorageParts);

		} else if (xp != null) {

			// Word can't handle an XPath that returns something
			// other than an element
			// eg string or boolean or number, so we'll need to work this out.
			// In principal, we could do this in this pre-processing step,
			// or via bind.xslt.

			// Doing it here means the bind.xslt step can be restricted to pure
			// Word-like processing.

			// Doing it there means we can take advantage of the multiline
			// processing we have there, and less code.
			// So as from 13 Sept 2011 (what will be 2.7.1), do it there.

			List<Object> newContent = new ArrayList<Object>();
			newContent.add(sdt);
			return newContent;
		}
		

		
		// shouldn't happen
		return null;
	}
	
	boolean reverterSupported = Docx4jProperties.getProperty("docx4j.model.datastorage.OpenDoPEReverter.Supported", true);
		
	/**
	 * Insert an empty placeholder SDT, to facilitate round-tripping
	 * (ie ability to convert instance docx back to original template),
	 * which you may wish to do if you want to insert updated data,
	 * but preserve certain manual edits.
	 * 
	 * @param sdt
	 * @return
	 */	
	private List<Object> conditionFalse(Object sdt) {

		List<Object> newContent = new ArrayList<Object>();
		
//		if (removeSdtCellsOnFailedCondition) {
//		// backward compatibility
//		// NB this is not compatible with reverting functionality, and
//		// will break if the result is an empty tc
//		return newContent;
//	}
		
		newContent.add(sdt);

		// Change the tag to od:resultConditionFalse
		SdtPr sdtPr = getSdtPr(sdt);

		CTDataBinding binding = sdtPr.getDataBinding();
		if (binding != null) {  // Shouldn't be a binding anyway
			sdtPr.getRPrOrAliasOrLock().remove(binding);
		}

		Tag tag = sdtPr.getTag(); 

		final String tagVal = tag.getVal();
		final Pattern stripConditionArgPattern = Pattern
				.compile("(.*od:condition=)([^&]*)(.*)");
		final Matcher stripPatternMatcher = stripConditionArgPattern
				.matcher(tagVal);
		if (!stripPatternMatcher.matches()) {
			log.error("Cannot find condition tag in sdtPr/tag while setting conditionFalse; something is wrong with " + tagVal);
			return newContent;
		}
		final String emptyConditionValue = BINDING_RESULT_CONDITION_FALSE + "=" + stripPatternMatcher.group(2) + stripPatternMatcher.group(3);
		tag.setVal(emptyConditionValue);	
		
		// Lock it
        CTLock lock = Context.getWmlObjectFactory().createCTLock(); 
        lock.setVal(org.docx4j.wml.STLock.SDT_CONTENT_LOCKED);		
        JAXBElement<org.docx4j.wml.CTLock> lockWrapped = Context.getWmlObjectFactory().createSdtPrLock(lock); 
        sdtPr.getRPrOrAliasOrLock().add( lockWrapped); // assumes no lock is there already

		// Empty the content
        // .. OpenDoPEIntegrity fixes this where it is not OK, but
        // where it needs to insert a tc, it has no way of adding original tcPr, so
        // we handle this here
		TcFinder tcFinder = new TcFinder();
		new TraversalUtil(((SdtElement)sdt).getSdtContent().getContent(), tcFinder);
		if (tcFinder.tcList.size()>0) {
			Tc tc = tcFinder.tcList.get(0);
			tc.getContent().clear();
			P p = Context.getWmlObjectFactory().createP();
			tc.getContent().add(p);			
	        ((SdtElement)sdt).getSdtContent().getContent().clear();
	        ((SdtElement)sdt).getSdtContent().getContent().add(tc);
		} else {
	        ((SdtElement)sdt).getSdtContent().getContent().clear();
		}
		
		return newContent;		
	}


//	private Object obtainParent(Object sdt) {
//		if (!(sdt instanceof Child))
//			throw new IllegalArgumentException("Object of class "
//					+ sdt.getClass().getName() + " is not a Child");
//
//		return ((Child) sdt).getParent();
//	}
//
//	private int countContentChildren(final Object tc) {
//		final List<Object> selfAndSiblings = obtainChildren(tc);
//		int contentChildCount = 0;
//		for (final Object child : selfAndSiblings)
//			if (!(child instanceof TcPr))
//				contentChildCount++;
//		return contentChildCount;
//	}
//
//	private List<Object> obtainChildren(Object element) {
//		Object unwrapped = XmlUtils.unwrap(element);
//		if (unwrapped instanceof ContentAccessor) {
//			return ((ContentAccessor) unwrapped).getContent();
//		} else if (unwrapped instanceof SdtElement) {
//			return ((SdtElement) unwrapped).getSdtContent().getContent();
//		}
//		log.warn("Don't know how to access the children of "
//				+ (unwrapped == null ? "null" : unwrapped.getClass().getName()));
//		return Collections.emptyList();
//	}
	 

	private List<Object> processOpenDopeRepeat(Object sdt,
			Map<String, CustomXmlPart> customXmlDataStorageParts) {

		Tag tag = getSdtPr(sdt).getTag();

		HashMap<String, String> map = QueryString.parseQueryString(
				tag.getVal(), true);

		String repeatId = map.get(BINDING_ROLE_REPEAT);

		// Check, whether we are in an old repeat case. These can be removed.
		if (StringUtils.isEmpty(repeatId))
			return new ArrayList<Object>();

//		org.opendope.xpaths.Xpaths.Xpath xpathObj = XPathsPart.getXPathById(
//				xPaths, repeatId);		
		org.opendope.xpaths.Xpaths.Xpath xpathObj = xpathsMap.get(repeatId);

		String storeItemId = xpathObj.getDataBinding().getStoreItemID();
		String xpath = xpathObj.getDataBinding().getXpath();
		String prefixMappings = xpathObj.getDataBinding().getPrefixMappings();

		try {
			return processRepeat(sdt, customXmlDataStorageParts, storeItemId,
					xpath, prefixMappings, false);
		} catch (W15RepeatZeroException w15) {
			// won't happen in this case
			return null;
		}
	}

	/**
	 * @param repeatingSectionSdt
	 * @param customXmlDataStorageParts
	 * @return
	 * 
	 * @since 3.2.2.
	 */
	private List<Object> processW15Repeat(Object repeatingSectionSdt,
			Map<String, CustomXmlPart> customXmlDataStorageParts) {

		CTDataBinding w15Databinding = getSdtPr(repeatingSectionSdt).getDataBinding();

		String storeItemId = w15Databinding.getStoreItemID();
		String xpath = w15Databinding.getXpath();
		String prefixMappings = w15Databinding.getPrefixMappings();
		
		// for a w15 repeat, we clone the child repeatingSectionItem sdt
		// TODO: review
		ContentAccessor ca = ((SdtElement)repeatingSectionSdt).getSdtContent();
		
		// replace its content
		SdtElement repeatingItem = (SdtElement)XmlUtils.unwrap(ca.getContent().get(0));
		
		try {
			List<Object> repeatingSectionItems = processRepeat(repeatingItem, customXmlDataStorageParts, storeItemId, xpath, prefixMappings, true);
			//that'll throw an exception for repeat zero
			
			ca.getContent().clear();
			ca.getContent().addAll(repeatingSectionItems);

		} catch (W15RepeatZeroException w15) {
			log.warn(w15.getMessage());
			// mimic w15 zero case (which is to leave the document surface unaltered!)
			// achieved by falling through to the below
			
			// If the binding step were to try binding the contents,
			// it'd insert empty placeholders (at least for anything repeated).
			// TODO: see what Word does with any content which is bound to outside the repeat. 
			// So we'll signal to the binding step that it should not process the contents
			// of the repeat.
			
			SdtPr sdtPr = getSdtPr(repeatingSectionSdt);
			Tag tag = new Tag();
			tag.setVal(BINDING_RESULT_RPTD_ZERO_W15 + "=true");
			sdtPr.setTag(tag);
		}
		
		// retain/return the repeatingSection sdt
		List<Object> newContent = new ArrayList<Object>();
		newContent.add(repeatingSectionSdt);	
		return newContent;
	}
	
	public long cloneTime = 0;
	public long fixBTime = 0;
	
	/**
	 * Process a repeat, whether its an OpenDoPE repeat, or a w15:RepeatingSection
	 * @param sdt
	 * @param customXmlDataStorageParts
	 * @param storeItemId
	 * @param xpath
	 * @param prefixMappings
	 * @return
	 */
	private List<Object> processRepeat(Object sdt,
			Map<String, CustomXmlPart> customXmlDataStorageParts,
			String storeItemId,
			String xpath,
			String prefixMappings,
			boolean isW15RepeatingSection) throws W15RepeatZeroException {


		long startTime = System.currentTimeMillis();
		
		// Get the bound XML
		String xpathBase;
		// if (xpath.endsWith("/*")) {
		// xpathBase = xpath.substring(0, xpath.length()-2);
		// } else
		if (xpath.endsWith("/")) {
			xpathBase = xpath.substring(0, xpath.length() - 1);

			// Check, whether the xpath ends with a [1]. If so, guess it comes
			// from a round-tripped path and strip it
		} else if (xpath.endsWith("[1]")) {
			xpathBase = xpath.substring(0, xpath.length() - 3);

		} else {
			xpathBase = xpath;
		}

		// DON'T Drop any trailing position! That breaks nested repeats
		// if (xpathBase.endsWith("]"))
		// xpathBase = xpathBase.substring(0, xpathBase.lastIndexOf("["));

		log.info("/n/n Repeat: using xpath: " + xpathBase + " and " + prefixMappings);
		List<Node> repeatedSiblings = xpathGetNodes(customXmlDataStorageParts,
				storeItemId, xpathBase, prefixMappings);
		// storeItemId, xpathBase+"/*", prefixMappings);

		// Count siblings
		int numRepeats = repeatedSiblings.size();
		log.debug("yields REPEATS: " + numRepeats);

		if (numRepeats == 0) {
			
			if (isW15RepeatingSection) {
				throw new W15RepeatZeroException("Zero repeats found for " + xpathBase + "; leaving existing content (as Word does)!");
			} else if (reverterSupported){
				// Change tag to od:resultRepeatZero=id
				return repeatZero(sdt);
			} else {
				return new ArrayList<Object>(); // effectively, delete
				// The document is only slightly smaller, since the repeatZero sdt doesn't have a lot of content.
				
				// OpenDoPEIntegrity is responsible for handling the case where
				// this creates an empty table cell
			}
		}

		// duplicate content here ...
		List<Object> repeated = cloneRepeatSdt(sdt, xpathBase, numRepeats);

		cloneTime += (System.currentTimeMillis()-startTime);
		
		startTime = System.currentTimeMillis();
		
		// deep traverse to fix binding
		DeepTraversor dt = new DeepTraversor();
		dt.xpathBase = xpathBase;
		for (int i = 0; i < repeated.size(); i++) {

			log.info("\n Traversing clone " + i);

			dt.index = i;
			new TraversalUtil(repeated.get(i), dt);
		}
		log.info(".. deep traversals done ");
		
		fixBTime += (System.currentTimeMillis()-startTime);
		
		// make bookmarks (if any) unique
		for (int i = 0; i < repeated.size(); i++) {
			try {
				
				// Use the sdt id for uniqueness
				Id id = ((SdtElement)repeated.get(i)).getSdtPr().getId();
				if (id==null) {
					((SdtElement)repeated.get(i)).getSdtPr().setId();
					id = ((SdtElement)repeated.get(i)).getSdtPr().getId();
				}
				long global= id.getVal().longValue();
				
				bookmarkRenumber.fixRange(
						((SdtElement)repeated.get(i)).getSdtContent().getContent(), 
						"CTBookmark", "CTMarkupRange", null, global, i);
			} catch (Exception e) {
				// Shouldn't happen .. TODO remove reflection?
				log.error(e.getMessage(),e);
			}
		}

		return repeated;
	}

	/**
	 * Insert an empty placeholder SDT, to facilitate round-tripping
	 * (ie ability to convert instance docx back to original template),
	 * which you may wish to do if you want to insert updated data,
	 * but preserve certain manual edits.
	 * 
	 * @param sdt
	 * @return
	 */
	private List<Object> repeatZero(Object sdt) {

		List<Object> newContent = new ArrayList<Object>();
		
//		if (removeSdtCellsOnFailedCondition) {
//			// backward compatibility
//			// NB this is not compatible with reverting functionality, and
//			// will break if the result is an empty tc
//			return newContent;
//		}
		
		newContent.add(sdt);

		// Change the tag to od:resultRepeatZero
		SdtPr sdtPr = getSdtPr(sdt);

		CTDataBinding binding = sdtPr.getDataBinding();
		if (binding != null) {  // Shouldn't be a binding anyway
			sdtPr.getRPrOrAliasOrLock().remove(binding);
		}

		Tag tag = sdtPr.getTag(); 

		final String tagVal = tag.getVal();
		final Pattern stripRepeatArgPattern = Pattern
				.compile("(.*od:repeat=)([^&]*)(.*)");
		final Matcher stripPatternMatcher = stripRepeatArgPattern
				.matcher(tagVal);
		if (!stripPatternMatcher.matches()) {
			log.error("Cannot find repeat tag in sdtPr/tag while processing repeat; something is wrong with " + tagVal);
			return newContent;
		}
		final String emptyRepeatValue = BINDING_RESULT_RPTD_ZERO + "=" + stripPatternMatcher.group(2) + stripPatternMatcher.group(3);
		tag.setVal(emptyRepeatValue);	
		
		// Lock it
        CTLock lock = Context.getWmlObjectFactory().createCTLock(); 
        lock.setVal(org.docx4j.wml.STLock.SDT_CONTENT_LOCKED);		
        JAXBElement<org.docx4j.wml.CTLock> lockWrapped = Context.getWmlObjectFactory().createSdtPrLock(lock); 
        sdtPr.getRPrOrAliasOrLock().add( lockWrapped); // assumes no lock is there already

		// Empty the content
        // .. OpenDoPEIntegrity fixes this where it is not OK, but
        // where it needs to insert a tc, it has no way of adding original tcPr, so
        // we handle this here
		TcFinder tcFinder = new TcFinder();
		new TraversalUtil(((SdtElement)sdt).getSdtContent().getContent(), tcFinder);
		if (tcFinder.tcList.size()>0) {
			Tc tc = tcFinder.tcList.get(0);
			tc.getContent().clear();
			P p = Context.getWmlObjectFactory().createP();
			tc.getContent().add(p);
	        ((SdtElement)sdt).getSdtContent().getContent().clear();
	        ((SdtElement)sdt).getSdtContent().getContent().add(tc);
		} else {
	        ((SdtElement)sdt).getSdtContent().getContent().clear();
		}
        
		
		return newContent;
	}
	
	private List<Object> cloneRepeatSdt(Object sdt, String xpathBase,
			int numRepeats) {

		List<Object> newContent = new ArrayList<Object>();

		SdtPr sdtPr = getSdtPr(sdt);

        if(log.isDebugEnabled()) {
            log.debug(XmlUtils.marshaltoString(sdtPr, true, true));
        }

		// CTDataBinding binding =
		// (CTDataBinding)XmlUtils.unwrap(sdtPr.getDataBinding());
		CTDataBinding binding = sdtPr.getDataBinding();
		if (binding != null) {  // Shouldn't be a binding anyway
			sdtPr.getRPrOrAliasOrLock().remove(binding);
		}

		emptyRepeatTagValue(sdtPr.getTag()); // 2012 07 15: do it to the first one

		for (int i = 0; i < numRepeats; i++) {

			// 2012 07 13: for "od:RptPosCon" processing to
			// work (conditional inclusion dependant on position
			// in repeat), we need each entry (ie including the
			// original) to have the same tag (which I've changed
			// to od:rptd).

			if (i > 0) {
				// Change ID
				sdtPr.setId();
			} // preserve ID on index 0, important for OpenDoPEReverter!
			
			// Clone
			newContent.add(XmlUtils.deepCopy(sdt));
		}

		return newContent;
	}

	private void emptyRepeatTagValue(final Tag tag) {
		
		if (tag==null) {
			// TODO: this is ok for a w15 repeat
			log.warn("No tag");
			return;
		}

		final String tagVal = tag.getVal();
		final Pattern stripRepeatArgPattern = Pattern
				.compile("(.*od:repeat=)([^&]*)(.*)");
		final Matcher stripPatternMatcher = stripRepeatArgPattern
				.matcher(tagVal);
		if (!stripPatternMatcher.matches()) {
			log.error("Cannot find repeat tag in sdtPr/tag while processing repeat; something is wrong with " + tagVal);
			return;
		}
//		final String emptyRepeatValue = stripPatternMatcher.group(1)
//				+ stripPatternMatcher.group(3);
		final String emptyRepeatValue = BINDING_RESULT_RPTD + "=" + stripPatternMatcher.group(2) + stripPatternMatcher.group(3);
		tag.setVal(emptyRepeatValue);
	}

	class DeepTraversor implements TraversalUtil.Callback {

		// private static Logger log = LoggerFactory.getLogger(DeepTraversor.class);

		int index = 0;
		String xpathBase = null;

		@Override
		public List<Object> apply(Object o) {

			// log.debug("apply for " + o.getClass().getName());

			if (o instanceof org.docx4j.wml.SdtBlock
					|| o instanceof org.docx4j.wml.SdtRun
					|| o instanceof org.docx4j.wml.CTSdtRow
					|| o instanceof org.docx4j.wml.CTSdtCell) { // SdtCell as
																// well, here

				processDescendantBindings(o, xpathBase, index);
				// whether it has a databinding or not

			} else {
				// log.warn("TODO: Handle " + o.getClass().getName()
				// + " (if that's an sdt)");
			}

			return null; // doesn't matter in this implementation
		}

		@Override
		public void walkJAXBElements(Object parent) {

			List<Object> children = getChildren(parent);
			if (children != null) {

				for (Object o : children) {

					o = XmlUtils.unwrap(o);
					this.apply(o);

					if (this.shouldTraverse(o)) {
						walkJAXBElements(o);
					}

				}
			}
		}

		@Override
		public List<Object> getChildren(Object o) {
			// log.debug("getChildren for " + o.getClass().getName());
			return TraversalUtil.getChildrenImpl(o);
		}

		@Override
		public boolean shouldTraverse(Object o) {
			return true;
		}

	}

	private void processDescendantBindings(Object sdt, String xpathBase,
			int index) {

		SdtPr sdtPr = getSdtPr(sdt);

		// log.debug(XmlUtils.marshaltoString(sdtPr, true, true));

		// Give it a unique ID (supersedes above?)
		sdtPr.setId();

		// log.debug(XmlUtils.marshaltoString(sdtPr, true, true));
		
		CTDataBinding binding = sdtPr.getDataBinding(); // could be a w15 binding

		String thisXPath = null;

		// It'll have one of these three...
		String conditionId = null;
		String repeatId = null;
		String bindingId = null;

		org.opendope.xpaths.Xpaths.Xpath xpathObj = null;

		HashMap<String, String> map = null;
		
		Tag tag = sdtPr.getTag();
		if (tag == null) {
			
			map = new HashMap<String, String>();
//			return;
		} else {
			map = QueryString.parseQueryString(tag.getVal(), true);
		}

		if (binding == null) {

			conditionId = map.get(BINDING_ROLE_CONDITIONAL);
			repeatId = map.get(BINDING_ROLE_REPEAT);

			if (conditionId != null) {

//				c = ConditionsPart.getConditionById(conditions, conditionId);
//				if (c == null) {
//					log.error("Missing condition " + conditionId);
//					throw new InputIntegrityException("Required condition '" + conditionId + "' is missing");
//				}
//
//				// TODO: this code assumes the condition contains
//				// a simple xpath
//				log.debug("Using condition"
//						+ XmlUtils.marshaltoString(c, true, true));
//				xpathObj = getXPathFromCondition(c);
//				thisXPath = xpathObj.getDataBinding().getXpath();

				processDescendantCondition( sdt,  xpathBase,
						 index,  tag );
				return;

			} else if (repeatId != null) {

//				xpathObj = XPathsPart.getXPathById(xPaths, repeatId);
				xpathObj = xpathsMap.get(repeatId);
				
				thisXPath = xpathObj.getDataBinding().getXpath();

			} else if (map.containsKey(BINDING_CONTENTTYPE)
					|| map.containsKey(BINDING_HANDLER)
					|| map.containsKey(BINDING_PROGID)) {

//				xpathObj = XPathsPart.getXPathById(xPaths, map.get(BINDING_ROLE_XPATH) );
				xpathObj = xpathsMap.get(map.get(BINDING_ROLE_XPATH));

				thisXPath = xpathObj.getDataBinding().getXpath();
				
			} else {

				log.warn("couldn't find binding or bindingrole!");
				// not all sdt's need have a binding;
				// they could be present in the docx for other purposes
				return;

				// NB an OpenDoPE xpath tag (with no w:binding element)
				// eg as created by authoring tool for a "count(" XPath
				// ends up here.
			}

		} else {
			thisXPath = binding.getXpath();

			// Set this stuff up now
			bindingId = map.get(BINDING_ROLE_XPATH);
			try {
				//xpathObj = XPathsPart.getXPathById(xPaths, bindingId);
				xpathObj = xpathsMap.get(bindingId);
			} catch (InputIntegrityException iie) {
				log.warn(iie.getMessage());
			}

			// Sanity test
			if (xpathObj==null) {
				// a normal binding might not have a 
				log.warn("No XPaths part object for " + binding.getXpath());
				
			} else if (!thisXPath.equals(xpathObj.getDataBinding().getXpath())) {
				log.error("XPaths didn't match for id " + bindingId + ": \n\r    " + thisXPath + "\n\rcf. "
						+ xpathObj.getDataBinding().getXpath());
			}

			// 2012 09 20 - when did this break?
			//thisXPath = xpathObj.getDataBinding().getXpath();
		}

//		System.out.println("xpathBase: " + xpathBase);
//		System.out.println("index: " + index);
//		System.out.println("thisXPath: " + thisXPath);


		final String newPath = enhanceXPath(xpathBase, index + 1, thisXPath);

//		System.out.println("newPath: " + newPath);

		if (log.isDebugEnabled() && !thisXPath.equals(newPath)) {
			log.debug("xpath prefix enhanced " + thisXPath + " to " + newPath);
		}

		if (binding == null) {

			if (repeatId != null) {

				// Create the new xpath object
				org.opendope.xpaths.Xpaths.Xpath newXPathObj = createNewXPathObject(
						newPath, xpathObj, index);

				// set sdt to use it
				map.put(BINDING_ROLE_REPEAT, newXPathObj.getId());
				tag.setVal(QueryString.create(map));

			} else if (map.containsKey(BINDING_CONTENTTYPE)
					|| map.containsKey(BINDING_HANDLER)
					|| map.containsKey(BINDING_PROGID)) {

				// Also need to create new xpath id, and add that
				org.opendope.xpaths.Xpaths.Xpath newXPathObj = createNewXPathObject(
						newPath, xpathObj, index);

				// set sdt to use it
				map.put(BINDING_ROLE_XPATH, newXPathObj.getId());
				tag.setVal(QueryString.create(map));

			}

		} else {
			binding.setXpath(newPath);

			// Also need to create new xpath id, and add that
			if (xpathObj==null) {
				// not required for w15 repeats, and why should it be required for a plain bind?
				log.debug("Not setting tag");
				
			} else {
				// Usual case (for OpenDoPE, anyway)
				
				org.opendope.xpaths.Xpaths.Xpath newXPathObj = createNewXPathObject(
						newPath, xpathObj, index);
	
				// set sdt to use it
				map.put(BINDING_ROLE_XPATH, newXPathObj.getId());
				tag.setVal(QueryString.create(map));
			}
		}

	}

	private void processDescendantCondition(Object sdt, String xpathBase,
			int index, Tag tag ) {

		Condition c = null;

		HashMap<String, String> map = QueryString.parseQueryString(
				tag.getVal(), true);


		String conditionId = map.get(BINDING_ROLE_CONDITIONAL);

		if (conditionId != null) {

			//c = ConditionsPart.getConditionById(conditions, conditionId);
			c = conditionsMap.get(conditionId);
			
			if (c == null) {
				log.error("Missing condition " + conditionId);
				throw new InputIntegrityException("Required condition '" + conditionId + "' is missing");
			}

			// TODO: this code assumes the condition contains
			// a simple xpath
            if(log.isDebugEnabled()) {
                log.debug("Using condition"
                        + XmlUtils.marshaltoString(c, true, true));
            }

			Condition newCondition = c.repeat(xpathBase, index, conditionsMap, xpathsMap);


			// set sdt to use it
			map.put(BINDING_ROLE_CONDITIONAL, newCondition.getId() );
			tag.setVal(QueryString.create(map));
		}

	}

	private org.opendope.xpaths.Xpaths.Xpath createNewXPathObject(
			String newPath, org.opendope.xpaths.Xpaths.Xpath xpathObj, int index) {

//		org.opendope.xpaths.Xpaths.Xpath newXPathObj = XmlUtils
//				.deepCopy(xpathObj);
		
		org.opendope.xpaths.Xpaths.Xpath newXPathObj = new org.opendope.xpaths.Xpaths.Xpath();		
		
		String newXPathId = xpathObj.getId() + "_" + index;
		newXPathObj.setId(newXPathId);
		
		org.opendope.xpaths.Xpaths.Xpath.DataBinding dataBinding = new org.opendope.xpaths.Xpaths.Xpath.DataBinding();
		newXPathObj.setDataBinding(dataBinding);
		
		dataBinding.setXpath(newPath);
		dataBinding.setStoreItemID(
				xpathObj.getDataBinding().getStoreItemID());
		dataBinding.setPrefixMappings(
				xpathObj.getDataBinding().getPrefixMappings());
		
		//xPaths.getXpath().add(newXPathObj);
		if (xpathsMap.put(newXPathId, newXPathObj)!=null) {
			log.error("New xpath entry overwrites existing xpath " + newXPathId);
		}
		return newXPathObj;
	}

	// private static boolean removeSdt(Object sdtParent, Object sdt) {
	//
	// if (sdtParent instanceof org.docx4j.wml.Body) {
	// return
	// ((org.docx4j.wml.Body)sdtParent).getEGBlockLevelElts().remove(sdt);
	// } else if (sdtParent instanceof org.docx4j.wml.P) {
	// return ((org.docx4j.wml.P)sdtParent).getParagraphContent().remove(sdt);
	// } else if (sdtParent instanceof org.docx4j.wml.Tbl) {
	// return
	// ((org.docx4j.wml.Tbl)sdtParent).getEGContentRowContent().remove(sdt);
	// } else if (sdtParent instanceof org.docx4j.wml.Tr) {
	// return
	// ((org.docx4j.wml.Tr)sdtParent).getEGContentCellContent().remove(sdt);
	// } else if (sdtParent instanceof org.docx4j.wml.Tc) {
	// return ((org.docx4j.wml.Tc)sdtParent).getEGBlockLevelElts().remove(sdt);
	// } else {
	// log.error("TODO: handle removal from parent " +
	// sdtParent.getClass().getName() );
	// return false;
	// }
	// }

	public static SdtPr getSdtPr(Object o) {

		if (o instanceof org.docx4j.wml.SdtBlock) {
			return ((org.docx4j.wml.SdtBlock) o).getSdtPr();
		} else if (o instanceof org.docx4j.wml.SdtRun) { // sdt in paragraph
			return ((org.docx4j.wml.SdtRun) o).getSdtPr();
		} else if (o instanceof org.docx4j.wml.CTSdtRow) { // sdt wrapping row
			return ((org.docx4j.wml.CTSdtRow) o).getSdtPr();
		} else if (o instanceof org.docx4j.wml.CTSdtCell) { // sdt wrapping cell
			return ((org.docx4j.wml.CTSdtCell) o).getSdtPr();
		} else {
			log.warn("TODO: Handle " + o.getClass().getName());
		}
		return null;

		/*
		 * Or could have done:
		 *
		 * Object o = XmlUtils.unwrap(raw); Method m =
		 * o.getClass().getDeclaredMethod("getSdtPr"); SdtPr sdtPr =
		 * (SdtPr)m.invoke(o);
		 */
	}

	// public static List<Object> getSdtContent(Object o) {
	//
	// if (o instanceof org.docx4j.wml.SdtBlock) {
	// return ((org.docx4j.wml.SdtBlock)
	// o).getSdtContent().getEGContentBlockContent();
	// } else if (o instanceof org.docx4j.wml.SdtRun) { // sdt in paragraph
	// return ((org.docx4j.wml.SdtRun) o).getSdtContent().getParagraphContent();
	// } else if (o instanceof org.docx4j.wml.CTSdtRow) { // sdt wrapping row
	// return ((org.docx4j.wml.CTSdtRow)
	// o).getSdtContent().getEGContentRowContent();
	// } else if (o instanceof org.docx4j.wml.CTSdtCell) { // sdt wrapping cell
	// return
	// ((org.docx4j.wml.CTSdtCell)o).getSdtContent().getEGContentCellContent();
	// } else {
	// log.warn("TODO: Handle " + o.getClass().getName() +
	// " (if that's an sdt)");
	// }
	// return null;
	// }

	private List<Node> xpathGetNodes(
			Map<String, CustomXmlPart> customXmlDataStorageParts,
			String storeItemId, String xpath, String prefixMappings) {

		CustomXmlPart part = customXmlDataStorageParts
				.get(storeItemId.toLowerCase());
		if (part == null) {
			log.error("Couldn't locate part by storeItemId " + storeItemId);
			return null;
		}
		try {
			return part.xpathGetNodes(xpath, prefixMappings);
		} catch (Docx4JException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

//	public static void main(String[] args) throws Exception {
//
//		String inputfilepath = System.getProperty("user.dir") + "/item.docx";
//
//		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
//
//		String filepathprefix = inputfilepath.substring(0, inputfilepath.lastIndexOf("."));
//		System.out.println(filepathprefix);
//
//		// Process conditionals and repeats
//		OpenDoPEHandler odh = new OpenDoPEHandler(wordMLPackage);
//		odh.preprocess();
//
//		System.out.println(
//				XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true)
//				);
//	}

}
