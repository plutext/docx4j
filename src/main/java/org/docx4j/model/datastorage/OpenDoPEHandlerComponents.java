package org.docx4j.model.datastorage;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.sdt.QueryString;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.opendope.ComponentsPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Id;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tag;
import org.opendope.conditions.Condition;
import org.opendope.xpaths.Xpaths;
import org.opendope.xpaths.Xpaths.Xpath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenDoPEHandlerComponents {

	private org.opendope.components.Components components;
		
	private Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap;
	
	private Map<String, Condition> conditionsMap;
	
	public OpenDoPEHandlerComponents(org.opendope.components.Components components,
			Map<String, Xpath> xpathsMap2, Map<String, Condition> conditionsMap2) {

		this.components = components;
		this.xpathsMap = xpathsMap2;
		this.conditionsMap = conditionsMap2;
	}
	
	
	private static Logger log = LoggerFactory.getLogger(OpenDoPEHandlerComponents.class);

	protected boolean justGotAComponent = false;
	
	/**
	 * Component processing 
	 * 
	 * @param srcPackage
	 * @param contentAccessor
	 * @return
	 * @throws Docx4JException
	 */
	protected WordprocessingMLPackage fetchComponents(
			WordprocessingMLPackage srcPackage, ContentAccessor contentAccessor)
			throws Docx4JException {

		// convert components to altChunk
//		Map<Integer, CTAltChunk> replacements = new HashMap<Integer, CTAltChunk>();
//		Integer index = 0;
		justGotAComponent = false;

//		LinkedList<Integer> continuousBeforeIndex = new LinkedList<Integer>();
//		List<Boolean> continuousBefore = new ArrayList<Boolean>();
//
//		List<Boolean> continuousAfter = new ArrayList<Boolean>();

		FindComponentsTraversor t = new FindComponentsTraversor();
		t.wordMLPackage = srcPackage;
		t.walkJAXBElements(contentAccessor);		

		if (!justGotAComponent) {
			return srcPackage;
		}

		// process altChunk
		try {
			// Use reflection, so docx4j can be built
			// by users who don't have the MergeDocx utility
			Class<?> documentBuilder = Class
					.forName("com.plutext.merge.altchunk.ProcessAltChunk");
			// Method method = documentBuilder.getMethod("merge",
			// wmlPkgList.getClass());
			Method[] methods = documentBuilder.getMethods();
			Method processMethod = null;
			for (int j = 0; j < methods.length; j++) {
				log.debug(methods[j].getName());
				if (methods[j].getName().equals("process")
						&& methods[j].getParameterCount()==3) {
					processMethod = methods[j];
				}
			}
			if (processMethod == null )
				throw new NoSuchMethodException();
			return (WordprocessingMLPackage) processMethod.invoke(null,
					srcPackage,
					xpathsMap,
					conditionsMap);
			
			
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

//	public void makeContinuous(SectPr sectPr) {
//
//		if (sectPr == null) {
//			log.warn("sectPr was null");
//			return;
//		}
//
//		SectPr.Type type = Context.getWmlObjectFactory().createSectPrType();
//		type.setVal("continuous");
//		sectPr.setType(type);
//
//		// columns, endnotes, footnotes, formprot, line numbers are OK
//
//		// null out certain page level section properties
//		sectPr.setBidi(null);
//		sectPr.setDocGrid(null);
//		sectPr.setPaperSrc(null);
//		sectPr.setPgBorders(null);
//		sectPr.setPgMar(null);
//		sectPr.setPgNumType(null);
//		sectPr.setPgSz(null);
//		sectPr.setPrinterSettings(null);
//		sectPr.setSectPrChange(null);
//		sectPr.setTitlePg(null);
//		sectPr.setVAlign(null);
//	}

	public void extensionMissing(Exception e) {
		log.error("\n" + e.getClass().getName() + ": " + e.getMessage() + "\n");
		log.error("* You don't appear to have the MergeDocx paid extension,");
		log.error("* which is necessary to merge docx, or process altChunk.");
		log.error("* Purchases of this extension support the docx4j project.");
		log.error("* Please visit www.plutext.com if you want to buy it.");
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

	/**
	 * This traversor finds components, taking note of their XPath context.
	 * For now, that's their immediate repeat ancestor.
	 */
	private class FindComponentsTraversor extends CallbackImpl {

		WordprocessingMLPackage wordMLPackage;

		@Override
		public List<Object> apply(Object wrapped) throws RuntimeException {

			// apply processSdt to any sdt
			// which might be a conditional|repeat
			Object o = XmlUtils.unwrap(wrapped);
			if (o instanceof SdtElement) {

				SdtPr sdtPr = OpenDoPEHandler.getSdtPr(o);
				if (sdtPr.getDataBinding() == null)  {
					// a real binding attribute trumps any tag
					try {
						return processBindingRoleIfAny(wordMLPackage, (SdtElement)o);
					} catch (Docx4JException e) {
						throw new RuntimeException(e);
					}
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
		
	/**
	 * This applies to any sdt which might be a conditional|repeat
	 *
	 * @param wordMLPackage
	 * @param sdtParent
	 * @param sdt
	 * @param tag
	 * @param sdtContent
	 * @return
	 * @throws Docx4JException 
	 * @throws Exception
	 */
		private List<Object> processBindingRoleIfAny(
				WordprocessingMLPackage wordMLPackage, Object sdt) throws Docx4JException {
			
			Id id = OpenDoPEHandler.getSdtPr(sdt).getId();
			if (id!=null) {
				log.debug("Processing " + id.getVal());
			}
			
			Tag tag = OpenDoPEHandler.getSdtPr(sdt).getTag();

			if (tag == null) {
				List<Object> newContent = new ArrayList<Object>();
				newContent.add(sdt);
				return newContent;
			}

		log.info(tag.getVal());

		HashMap<String, String> map = QueryString.parseQueryString(tag.getVal(), true);

		String conditionId = map.get(OpenDoPEHandler.BINDING_ROLE_CONDITIONAL);
		String repeatId = map.get(OpenDoPEHandler.BINDING_ROLE_REPEAT);
//		String xp = map.get(OpenDoPEHandler.BINDING_ROLE_XPATH);
		String componentId = map.get(OpenDoPEHandler.BINDING_ROLE_COMPONENT);
		
		if (componentId ==null && conditionId == null && repeatId == null ) {
			List<Object> newContent = new ArrayList<Object>();
			newContent.add(sdt);
			return newContent;
		}

		Map<String, CustomXmlPart> customXmlDataStorageParts = wordMLPackage.getCustomXmlDataStorageParts();

		if (componentId!=null) {
			
			justGotAComponent = true;
			return foo(wordMLPackage, componentId);
						
		} else if (conditionId != null) {

			log.debug("Encountered Conditional: " + tag.getVal());

			// TODO later; perhaps take context from condition eg
			//    xpath="local-name(/yaml/paths[1]/*[1]/*[1])='get'" 
			
//			// this only handles simple conditions
//			Condition c = conditionsMap.get(conditionId);
//			if (c == null) {
//				log.error("Missing condition " + conditionId);
//			}

			List<Object> newContent = new ArrayList<Object>();
			newContent.add(sdt);
			return newContent;
			

		} else if (repeatId != null) {

			log.info("Processing OpenDoPE Repeat: " + tag.getVal());

//			processOpenDopeRepeat(sdt, customXmlDataStorageParts);

			List<Object> newContent = new ArrayList<Object>();
			newContent.add(sdt);
			return newContent;
			
		} 
		
		// shouldn't happen
		return null;
		
	}

		private List<Object> foo(WordprocessingMLPackage srcPackage, 
				String componentId) throws Docx4JException {
			
			// Convert the sdt to a w:altChunk
			// .. get the IRI
			String iri = ComponentsPart.getComponentById(components,
					componentId).getIri();
			log.debug("Fetching " + iri);

			if (OpenDoPEHandler.getDocxFetcher() == null) {
				log.error("You need a docxFetcher (and the MergeDocx extension) to fetch components");
				throw new Docx4JException("You need a docxFetcher (and the MergeDocx extension) to fetch components");
			}

			// .. create the part
			AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(
					getNewPartName("/chunk", ".docx", srcPackage
							.getMainDocumentPart().getRelationshipsPart()));
			afiPart.setBinaryData(OpenDoPEHandler.getDocxFetcher().getDocxFromIRI(iri));

			afiPart.setContentType(new ContentType(
					"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml")); // docx

			Relationship altChunkRel = srcPackage.getMainDocumentPart()
					.addTargetPart(afiPart);
			CTAltChunk ac = Context.getWmlObjectFactory()
					.createCTAltChunk();
			ac.setId(altChunkRel.getId());

//			replacements.put(index, ac);
			List<Object> newContent = new ArrayList<Object>();
			newContent.add(ac);
			return newContent;

			/*
			 * 2011 12 11 TODO.  Rethink support for
			 * od:continuousBefore and od:continuousAfter.
			 */

			// This is handled in this class
//			if (map.get(OpenDoPEHandler.BINDING_ROLE_COMPONENT_BEFORE) != null
//					&& map.get(OpenDoPEHandler.BINDING_ROLE_COMPONENT_BEFORE)
//							.equals("true")) {
//				continuousBefore.add(Boolean.TRUE);
//				continuousBeforeIndex.addFirst(index);
//				log.info("ctsBefore index: " + index);
//			} else {
//				continuousBefore.add(Boolean.FALSE);
//				continuousBeforeIndex.addFirst(index);
//			}
//
//			// The following is handled in ProcessAltChunk
//			if (map.get(OpenDoPEHandler.BINDING_ROLE_COMPONENT_AFTER) != null
//					&& map.get(OpenDoPEHandler.BINDING_ROLE_COMPONENT_AFTER).equals("true")) {
//				continuousAfter.add(Boolean.TRUE);
//			} else {
//				continuousAfter.add(Boolean.TRUE);
//			}

//			// Now replace in list
//			for (Integer key : replacements.keySet()) {
//				contentAccessor.getContent().set(key, replacements.get(key));
//				System.out.println("replaced at " + key);
//			}
			
//			// Go through docx in reverse order
//			List<Object> bodyChildren = contentAccessor.getContent();
//			int i = 0;
//			for (Integer indexIntoBody : continuousBeforeIndex) {
//
//				if (continuousBefore.get(i)) {
//					// Element before the w:altChunk
//					if (indexIntoBody == 0) {
//						// // Insert a sectPr right at the beginning of the docx?
//						// // TODO check this isn't necessary
//						// SectPr newSectPr =
//						// Context.getWmlObjectFactory().createSectPr();
//						// SectPr.Type type =
//						// Context.getWmlObjectFactory().createSectPrType();
//						// type.setVal("continuous");
//						// newSectPr.setType( type );
//						//
//						// bodyChildren.add(0, newSectPr);
//
//					} else {
//						Object block = bodyChildren
//								.get(indexIntoBody.intValue() - 1);
//						if (block instanceof P && ((P) block).getPPr() != null
//								&& ((P) block).getPPr().getSectPr() != null) {
//							makeContinuous(((P) block).getPPr().getSectPr());
//						} else if (block instanceof P) {
//							// More likely
//							PPr ppr = ((P) block).getPPr();
//							if (ppr == null) {
//								ppr = Context.getWmlObjectFactory().createPPr();
//								((P) block).setPPr(ppr);
//							}
//							SectPr newSectPr = Context.getWmlObjectFactory()
//									.createSectPr();
//							SectPr.Type type = Context.getWmlObjectFactory()
//									.createSectPrType();
//							type.setVal("continuous");
//							newSectPr.setType(type);
//
//							ppr.setSectPr(newSectPr);
//						} else {
//							// Equally likely - its a table or something, so add a p
//							P newP = Context.getWmlObjectFactory().createP();
//							PPr ppr = Context.getWmlObjectFactory().createPPr();
//							newP.setPPr(ppr);
//
//							SectPr newSectPr = Context.getWmlObjectFactory()
//									.createSectPr();
//							SectPr.Type type = Context.getWmlObjectFactory()
//									.createSectPrType();
//							type.setVal("continuous");
//							newSectPr.setType(type);
//							ppr.setSectPr(newSectPr);
//
//							bodyChildren.add(indexIntoBody.intValue(), newP); // add
//																				// before
//																				// altChunk
//						}
//					}
//				}
//				// else nothing specified, so go with normal MergeDocx behaviour
//
//				i++;
//			}
			
		}
		
		
	private List<Object> processOpenDopeRepeat(Object sdt,
			Map<String, CustomXmlPart> customXmlDataStorageParts) {

		Tag tag = OpenDoPEHandler.getSdtPr(sdt).getTag();

		HashMap<String, String> map = QueryString.parseQueryString(
				tag.getVal(), true);

		String repeatId = map.get(OpenDoPEHandler.BINDING_ROLE_REPEAT);

		// Check, whether we are in an old repeat case. These can be removed.
		if (StringUtils.isEmpty(repeatId))
			return new ArrayList<Object>();

//		org.opendope.xpaths.Xpaths.Xpath xpathObj = XPathsPart.getXPathById(
//				xPaths, repeatId);		
		org.opendope.xpaths.Xpaths.Xpath xpathObj = xpathsMap.get(repeatId);

		String storeItemId = xpathObj.getDataBinding().getStoreItemID();
		String xpath = xpathObj.getDataBinding().getXpath();
		String prefixMappings = xpathObj.getDataBinding().getPrefixMappings();

		// TODO: set context from xpath
		return null;
	}
	}
	
}
