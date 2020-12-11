package org.docx4j.model.datastorage;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.sdt.QueryString;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.opendope.ComponentsPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Id;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Tag;
import org.opendope.conditions.Condition;
import org.opendope.xpaths.Xpaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Process OpenDoPE components.
 * 
 * From 6.1, components can be at any level in the
 * Main Document Part 
 * content hierarchy where an SdtBlock is allowed.
 * 
 * From 6.1, components support the idea of an XPath 
 * context: where an XPath is not absolute, it will be
 * interpreted relative to context. Context can be
 * provided explicitly (the id of an XPath in the XPaths 
 * part).  If not provided explicitly, we can read it
 * from an enclosing repeat.  (We don't do that for 
 * conditions right now, since they are a bit more tricky).
 * 
 * In 6.1, the section break handling is missing.
 * TODO: consider whether it is needed.  May be useful
 * for top level components which add headers/footers?
 * 
 * @author jharrop
 */
public class OpenDoPEHandlerComponents {

	private static Logger log = LoggerFactory.getLogger(OpenDoPEHandlerComponents.class);
	
	// Docx4j 6.1: re-designed component processing model:
	// 1. components don't have to be at the top paragraph level of the content tree,
	// 2. they can use an XPath context
	// BUT:
	// 3. component processing is now done before condition/repeat processing
	// 4. component processing is not recursive anymore
	// 5. components typically use the "main" answer file
	
	// We only support components in the MainDocumentPart,
	// since MergeDocx can concatenate content in that part,
	// but not in other parts.
	
	// TODO: make this step optional
	
	private WordprocessingMLPackage srcPackage;

	protected boolean justGotAComponent = false;
	
	private org.opendope.components.Components components;
		
	private Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap = null;
	private Map<String, Condition> conditionsMap = null; 

	private static DocxFetcher docxFetcher;

	public static DocxFetcher getDocxFetcher() {
		return docxFetcher;
	}

	public static void setDocxFetcher(DocxFetcher docxFetcher) {
		OpenDoPEHandlerComponents.docxFetcher = docxFetcher;
	}
	
	public OpenDoPEHandlerComponents(WordprocessingMLPackage wordMLPackage)
			throws Docx4JException {

		this.srcPackage = wordMLPackage;

		if (wordMLPackage.getMainDocumentPart().getXPathsPart() == null) {
			log.info("OpenDoPE XPaths part missing (ok if you are just processing w15 repeatingSection)");
			return;
			
		} else {
			org.opendope.xpaths.Xpaths xPaths = wordMLPackage.getMainDocumentPart().getXPathsPart().getJaxbElement();
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
	}	
	
	

	
	/**
	 * altChunkRel.getId(), xpathId 
	 */
	Map<String, String> altChunkXPathContexts = new HashMap<String, String>();
		// We'd need PartName to map to that, if we were to support parts other than MDP!
	
	/**
	 * Component processing 
	 * 
	 * @param srcPackage
	 * @param contentAccessor
	 * @return
	 * @throws Docx4JException
	 */
	public WordprocessingMLPackage fetchComponents()
			throws Docx4JException {
		
		if (xpathsMap==null) return srcPackage;
		
//		System.out.println("before component processing");
//		System.out.println(wordMLPackage.getMainDocumentPart().getXML());

		justGotAComponent = false;
		
		// Convert any sdt with <w:tag w:val="od:component=comp1"/>
		// to altChunk, and for MergeDocx users, to
		// real WordML.
		ContentAccessor part = srcPackage.getMainDocumentPart();

//			LinkedList<Integer> continuousBeforeIndex = new LinkedList<Integer>();
//			List<Boolean> continuousBefore = new ArrayList<Boolean>();
//
//			List<Boolean> continuousAfter = new ArrayList<Boolean>();

		FindComponentsTraversor t = new FindComponentsTraversor();
		t.wordMLPackage = srcPackage;
		if (part instanceof MainDocumentPart /* which it will be in this release */) {
			// avoid invoking walkJAXBElements on a list,
			// (docx4j 6.1.0 doesn't know how to replace children there,
			//  which it needs to do if the component is a child of w:body)
			Body b = ((MainDocumentPart)part).getJaxbElement().getBody();
			t.walkJAXBElements(b);
		} else {
			t.walkJAXBElements(part.getContent());			
		}

		if (!justGotAComponent) {
			return srcPackage;
		}
		
		Map<QName, CustomXmlPart> answerDomDocs = new HashMap<QName, CustomXmlPart>();
		CustomXmlPart data = CustomXmlDataStoragePartSelector.getCustomXmlDataStoragePart(srcPackage);
		if (data instanceof CustomXmlDataStoragePart) {
			Document doc = ((CustomXmlDataStoragePart)data).getData().getDocument();
			answerDomDocs.put(getQName(doc.getDocumentElement()), data);
		} else {
			throw new Docx4JException("TODO: handle " + data.getClass().getName());
		}

		// process altChunk
		try {
			// Use reflection, so docx4j can be built
			// by users who don't have the Enterprise MergeDocx utility
			Class<?> documentBuilder = Class
					.forName("com.plutext.merge.altchunk.ProcessAltChunk");
			Method[] methods = documentBuilder.getMethods();
			Method processMethod = null;
			for (int j = 0; j < methods.length; j++) {
//				log.debug(methods[j].getName());
				if (methods[j].getName().equals("process")
						&& methods[j].getParameterCount()==5) {
					processMethod = methods[j];
				}
			}
			if (processMethod == null )
				throw new NoSuchMethodException();
			
			return (WordprocessingMLPackage) processMethod.invoke(null,
					srcPackage,
					answerDomDocs,
					xpathsMap,
					conditionsMap,
					altChunkXPathContexts);
			
			
		} catch (ClassNotFoundException e) {
			extensionMissing(e);
			return srcPackage;
			// throw new Docx4JException("Problem processing w:altChunk", e);
		} catch (NoSuchMethodException e) {
			// Degrade gracefully
			extensionMissing(e);
			return srcPackage;
			// throw new Docx4JException("Problem processing w:altChunk", e);
		} catch (Exception e) {
			throw new Docx4JException("Problem processing w:altChunk", e);
		}
	}
	
  private QName getQName(Element el) {
	    QName qname = new QName(el.getNamespaceURI(), el.getLocalName());
	    System.out.println(qname);
	    return qname;
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
				
	    private LinkedList<String> repeatContext = new LinkedList<String>();
		

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
			
			// Add repeat context?
			boolean mustPopRepeat = false;
			if (parentUnwrapped instanceof SdtElement) {
				Tag tag = ((SdtElement)parentUnwrapped).getSdtPr().getTag();
				if (tag == null) {
					log.debug("no tag " + XmlUtils.marshaltoString(parentUnwrapped));
				} else {
					HashMap<String, String> map = QueryString.parseQueryString(tag.getVal(), true);
					String repeatId = map.get(OpenDoPEHandler.BINDING_ROLE_REPEAT);
					if (repeatId!=null) {
						repeatContext.push(repeatId);
						mustPopRepeat = true;
					}
				}
			}

				
				
			List<Object> children = getChildren(parentUnwrapped);
			if (children == null) {
//				log.debug("no children: " + parentUnwrapped.getClass().getName());
				return;
			} else {
				for (Object o : children) {
					// apply
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
			
			// Remove repeat context?
			if (mustPopRepeat) {
				repeatContext.pop();
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
//				log.debug("Processing " + id.getVal());
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
		
		// Is there an explicit XPath context for this component?
		String componentContextId = map.get(OpenDoPEHandler.BINDING_ROLE_COMPONENT_CONTEXT);
		
		if (componentId ==null && conditionId == null && repeatId == null ) {
			List<Object> newContent = new ArrayList<Object>();
			newContent.add(sdt);
			return newContent;
		}

		Map<String, CustomXmlPart> customXmlDataStorageParts = wordMLPackage.getCustomXmlDataStorageParts();

		if (componentId!=null) {
			
			justGotAComponent = true;
			
			if (componentContextId!=null) {
				log.debug("Using explicit component context " + componentContextId);
				return processOpenDopeComponent(wordMLPackage, componentId, componentContextId);				
			} else if (!repeatContext.isEmpty()) {
				log.debug("Using component context inferred from repeat " + repeatContext.peek());
				return processOpenDopeComponent(wordMLPackage, componentId, repeatContext.peek());								
			} else {
				// No context, its just root
				log.debug("No context, its just root " );
				return processOpenDopeComponent(wordMLPackage, componentId, null);
			}
						
		} else if (conditionId != null) {

			log.debug("Encountered Conditional: " + tag.getVal());

			// later perhaps, could take context from condition eg
			//    xpath="local-name(/yaml/paths[1]/*[1]/*[1])='get'"
			
			// but ability to explicity set context on a component
			// is effective, so no need for this.

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

		/**
		 * Convert the component sdt to w:altChunk
		 * 
		 * @param srcPackage
		 * @param componentId
		 * @param xpathId  gives the context in which the component is to be used
		 * @return
		 * @throws Docx4JException
		 */
		private List<Object> processOpenDopeComponent(WordprocessingMLPackage srcPackage, 
				String componentId, String xpathId) throws Docx4JException {
			
			// TODO: here we convert component sdt to w:altChunk,
			// then, in PEJ, we process altChunks by converting them to content controls.
			// Consider adding a code path in which we just process the content control directly? 
			
			// Convert the sdt to a w:altChunk
			// .. get the IRI
			String iri = ComponentsPart.getComponentById(components,
					componentId).getIri();
			log.debug("Fetching " + iri);

			if (getDocxFetcher() == null) {
				log.error("You need a docxFetcher (and the MergeDocx extension) to fetch components");
				throw new Docx4JException("You need a docxFetcher (and the MergeDocx extension) to fetch components");
			}

			// .. create the part
			AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(
					getNewPartName("/chunk", ".docx", srcPackage
							.getMainDocumentPart().getRelationshipsPart()));
			afiPart.setBinaryData(getDocxFetcher().getDocxFromIRI(iri));

			afiPart.setContentType(new ContentType(
					"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml")); // docx

			Relationship altChunkRel = srcPackage.getMainDocumentPart()
					.addTargetPart(afiPart);
			CTAltChunk ac = Context.getWmlObjectFactory()
					.createCTAltChunk();
			ac.setId(altChunkRel.getId());
			
			altChunkXPathContexts.put(altChunkRel.getId(), xpathId);
			
			log.debug("Using altChunkRel " + altChunkRel.getId() + " and " + xpathId);

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
		
	}
	
}
