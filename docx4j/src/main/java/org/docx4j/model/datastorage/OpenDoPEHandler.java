package org.docx4j.model.datastorage;

import static org.docx4j.model.datastorage.XPathEnhancerParser.enhanceXPath;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.NamespaceContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.customXmlProperties.SchemaRefs;
import org.docx4j.customXmlProperties.SchemaRefs.SchemaRef;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMappings;
import org.docx4j.model.sdt.QueryString;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.opendope.ComponentsPart;
import org.docx4j.openpackaging.parts.opendope.ConditionsPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.CTDataBinding;
import org.docx4j.wml.CTSdtCell;
import org.docx4j.wml.CTSdtContentCell;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tag;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.Text;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.opendope.conditions.Condition;
import org.w3c.dom.Node;

public class OpenDoPEHandler {
	
	private static Logger log = Logger.getLogger(OpenDoPEHandler.class);	
	
	public OpenDoPEHandler(WordprocessingMLPackage wordMLPackage) throws Docx4JException {

		this.wordMLPackage = wordMLPackage;
		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();			
		if (wordMLPackage.getMainDocumentPart().getXPathsPart()==null) {
			throw new Docx4JException("OpenDoPE XPaths part missing");
		} else {
			xPaths = wordMLPackage.getMainDocumentPart().getXPathsPart().getJaxbElement();
			log.debug( XmlUtils.marshaltoString(xPaths, true, true));
		}
		if (wordMLPackage.getMainDocumentPart().getConditionsPart()!=null) {
			conditions = wordMLPackage.getMainDocumentPart().getConditionsPart().getJaxbElement();
			log.debug( XmlUtils.marshaltoString(conditions, true, true));
		}
		if (wordMLPackage.getMainDocumentPart().getComponentsPart()!=null) {
			components = wordMLPackage.getMainDocumentPart().getComponentsPart().getJaxbElement();
			log.debug( XmlUtils.marshaltoString(components, true, true));
		}

		shallowTraversor = new ShallowTraversor();		
		shallowTraversor.wordMLPackage = wordMLPackage; 
	}
	
	private WordprocessingMLPackage wordMLPackage;
	private ShallowTraversor shallowTraversor;
	
	public final static String BINDING_ROLE_REPEAT = "od:repeat";
	public final static String BINDING_ROLE_CONDITIONAL = "od:condition";
	public final static String BINDING_ROLE_XPATH = "od:xpath";
	public final static String BINDING_ROLE_COMPONENT = "od:component";
	public final static String BINDING_ROLE_COMPONENT_BEFORE = "od:continuousBefore";
	public final static String BINDING_ROLE_COMPONENT_AFTER = "od:continuousAfter";
	
	
	/* ---------------------------------------------------------------------------
	 * Pre-processing of content controls which have a tag containing "bindingrole"
	 * 
	 */
		
		private  org.opendope.conditions.Conditions conditions;
		private  org.opendope.xpaths.Xpaths xPaths;
		private  org.opendope.components.Components components;
		
    private  boolean removeSdtCellsOnFailedCondition;

  /**
   * Configure, how the preprocessor handles conditions on table cells.
   * 
   * If set to <code>false</code>, conditional SDT cells are replaced by empty cells. This is the default behavior.
   * 
   * If set to <code>true</code>, conditional SDT cells are removed entirely. Note that the table geometry is not
   * changed; hence this works better without dynamic table widths / no global width settings.
   * 
   * Due to the architecture of this class, this is a static flag changing the behavior of all following calls to
   * {@link #preprocess}.
   * 
   * @param removeSdtCellsOnFailedCondition The new value for the cell removal flag.
   */
    public void setRemoveSdtCellsOnFailedCondition(boolean removeSdtCellsOnFailedCondition) {
      this.removeSdtCellsOnFailedCondition = removeSdtCellsOnFailedCondition;
    }
		
		/**
		 * Preprocess content controls which have tag "od:condition|od:repeat|od:component".
		 * 
		 * It is "preprocess" in the sense that it is "pre" opening in Word
		 * 
		 * The algorithm is as follows:
		 * 
		 *  Inject components first.  
		 * 
		 * 	Look at each top level SDT (ShallowTraversor).  
		 *  If it does not have a real data binding, it might have a bindingrole tag
		 *  we need to process (processBindingRoleIfAny).
		 *  
		 *  Conditionals are easy.
		 *  
		 *  processRepeat method:
		 *  
		 *  - clones the sdt n times
		 *  
		 *  - invokes DeepTraversor which changes xpath binding on descendant sdts
		 *    (both sdts with real bindings and sdts with bindingrole tags).
		 *    
		 *  It is not the job of DeepTraversor to expand out any other repeats it
		 *  might encounter, or to resolve conditionals.
		 *  
		 *  Those things are done by ShallowTraversor, to which control returns,
		 *  as it continues its traverse.
		 * 
		 *  The implementation of 13 Sept 2010 replaced the previous XPath based
		 *  implementation, which did not support nested repeats.  I've chosen to
		 *  build this around TraversalUtil, instead of using XSLT, and this
		 *  seems to have worked out nicely.
		 *  
		 *  The implementation of 10 October 2010 replaced the v1 conventions
		 *  implementation with a v2 implementation.  The main method in this
		 *  class can convert v1 documents to v2. The v2 implementation is not yet
		 *  complete. All v1 features are implemented, but not the new v2
		 *  stuff (eg complex conditions).
		 * 
		 * @param documentPart
		 * @throws Docx4JException
		 */
		public WordprocessingMLPackage preprocess() throws Docx4JException {

			do {
				// A component can apply in both the main document part,
				// and in headers/footers. See further http://forums.opendope.org/Support-components-in-headers-footers-tp2964174p2964174.html 
				// A component added to the
				// main document part could add new headers/footers.
				// So we need to work out what parts to preprocess 
				// here inside this do loop. 
				Set<ContentAccessor> partList = getParts(wordMLPackage);
					
				// Process repeats and conditionals.
				for (ContentAccessor part : partList) {					
					new TraversalUtil(part, shallowTraversor); 
				}
			      
				// Convert any sdt with <w:tag w:val="od:component=comp1"/>
				// to altChunk, and for MergeDocx users, to 
				// real WordML.
				for (ContentAccessor part : partList) {
					wordMLPackage = fetchComponents(wordMLPackage, part);
				}
				
			
			} while (justGotAComponent);
			// ie repeat the whole process if you got a component
			
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
		
		private Set<ContentAccessor> getParts(WordprocessingMLPackage srcPackage) {
			
			Set<ContentAccessor> partList = new HashSet<ContentAccessor>();			
			
			partList.add(srcPackage.getMainDocumentPart());
			
			// Add headers/footers
			RelationshipsPart rp = srcPackage.getMainDocumentPart().getRelationshipsPart();
			for ( Relationship r : rp.getRelationships().getRelationship()  ) {
				
				if (r.getType().equals(Namespaces.HEADER)) {
					partList.add((HeaderPart)rp.getPart(r));					
				} else if ( r.getType().equals(Namespaces.FOOTER) ) {
					partList.add((FooterPart)rp.getPart(r));
				}			
			}
			
			return partList;
		}

		private WordprocessingMLPackage fetchComponents(WordprocessingMLPackage srcPackage,
				ContentAccessor contentAccessor) throws Docx4JException {
			
			// convert components to altChunk
			Map<Integer, CTAltChunk> replacements = new HashMap<Integer, CTAltChunk>();
			Integer index = 0;
			justGotAComponent = false;
			
			LinkedList<Integer> continuousBeforeIndex = new LinkedList<Integer>();
			List<Boolean> continuousBefore = new ArrayList<Boolean>();
			
			List<Boolean> continuousAfter = new ArrayList<Boolean>();
			
	    	for (Object block : contentAccessor.getContent() ) {
	    		
	    		//Object ublock = XmlUtils.unwrap(block);
	    		if (block instanceof org.docx4j.wml.SdtBlock) {
	    			
	    			org.docx4j.wml.SdtBlock sdt = (org.docx4j.wml.SdtBlock)block;
	    			
	    			Tag tag = getSdtPr(sdt).getTag();										
	    			
	    			if (tag==null) {
	    				List<Object> newContent = new ArrayList<Object>();
	    				newContent.add(sdt);
	    				continue;
	    			} 
	    			
	    			log.info(tag.getVal());

	    			QueryString qs = new QueryString();
	    			HashMap<String, String> map = qs.parseQueryString(tag.getVal(), true);
	    			
	    			String componentId = map.get(BINDING_ROLE_COMPONENT);
	    			if (componentId==null) continue;
	    			
	    			// Convert the sdt to a w:altChunk
	    			// .. get the IRI
	    			String iri = ComponentsPart.getComponentById(components, componentId).getIri();
	    			log.debug("Fetching " + iri);
	    			
					if (docxFetcher==null) {
						log.error("You need a docxFetcher (and the MergeDocx extension) to fetch components");
						return srcPackage;
					} 
	    			
	    			// .. create the part
	    			AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(
	    					getNewPartName("/chunk", ".docx", srcPackage.getMainDocumentPart().getRelationshipsPart()) );
	    			afiPart.setBinaryData(
	    					docxFetcher.getDocxFromIRI(iri) );
	    			
	    			afiPart.setContentType(new ContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml")); //docx

	    			Relationship altChunkRel = srcPackage.getMainDocumentPart().addTargetPart(afiPart);
	    			CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk();
	    			ac.setId(altChunkRel.getId());

	    			replacements.put(index, ac);
	    			
	    			// This is handled in this class
	    			if (map.get(BINDING_ROLE_COMPONENT_BEFORE)!=null
	    					&& map.get(BINDING_ROLE_COMPONENT_BEFORE).equals("true") ) {
	    				continuousBefore.add(Boolean.TRUE);
	    				continuousBeforeIndex.addFirst(index);
	    				log.info("ctsBefore index: " + index);
	    			} else {
	    				continuousBefore.add(Boolean.FALSE);
	    				continuousBeforeIndex.addFirst(index);
	    			}
	    			
	    			// The following is handled in ProcessAltChunk
	    			if (map.get(BINDING_ROLE_COMPONENT_AFTER)!=null
	    					&& map.get(BINDING_ROLE_COMPONENT_AFTER).equals("true") ) {
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
	    	for(Integer key : replacements.keySet() ) {
	    		contentAccessor.getContent().set(key, replacements.get(key));	    		
	    	}
	    	
	    	// Go through docx in reverse order 
	    	List<Object> bodyChildren = contentAccessor.getContent(); 
	    	int i = 0;
	    	for (Integer indexIntoBody : continuousBeforeIndex ) {
	    		
	    		if (continuousBefore.get(i) ) {
	    			// Element before the w:altChunk
	    			if (indexIntoBody==0) {
//	    				// Insert a sectPr right at the beginning of the docx?
//	    				// TODO check this isn't necessary
//	    				SectPr newSectPr = Context.getWmlObjectFactory().createSectPr();
//	    	    		SectPr.Type type = Context.getWmlObjectFactory().createSectPrType();
//	    	    		type.setVal("continuous");
//	    	    		newSectPr.setType( type );
//	    	    		
//	    	    		bodyChildren.add(0, newSectPr);	    				
	    				
	    			} else {
		    			Object block = bodyChildren.get(indexIntoBody.intValue()-1); 
		    			if (block  instanceof P
		    	    		&& ((P)block).getPPr()!=null 
		    	    				&& ((P) block).getPPr().getSectPr() !=null) {
		    				makeContinuous(((P) block).getPPr().getSectPr());
		    			} else if (block  instanceof P) {
		    				// More likely
		    				PPr ppr = ((P) block).getPPr();
		    				if (ppr==null) {
		    					ppr = Context.getWmlObjectFactory().createPPr();
		    					((P) block).setPPr(ppr);
		    				} 
		    				SectPr newSectPr = Context.getWmlObjectFactory().createSectPr();
		    	    		SectPr.Type type = Context.getWmlObjectFactory().createSectPrType();
		    	    		type.setVal("continuous");
		    	    		newSectPr.setType( type );
		    				
		    	    		ppr.setSectPr(newSectPr);
		    			} else {
		    				// Equally likely - its a table or something, so add a p
		    				P newP = Context.getWmlObjectFactory().createP();
		    				PPr ppr = Context.getWmlObjectFactory().createPPr();
		    				newP.setPPr(ppr);
		    				
		    				SectPr newSectPr = Context.getWmlObjectFactory().createSectPr();
		    	    		SectPr.Type type = Context.getWmlObjectFactory().createSectPrType();
		    	    		type.setVal("continuous");
		    	    		newSectPr.setType( type );
		    	    		ppr.setSectPr(newSectPr);
		    	    		
		    	    		bodyChildren.add(indexIntoBody.intValue(), newP);	// add before altChunk    				
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
				Class<?> documentBuilder = Class.forName("com.plutext.merge.ProcessAltChunk");			
				//Method method = documentBuilder.getMethod("merge", wmlPkgList.getClass());			
				Method[] methods = documentBuilder.getMethods(); 
				Method processMethod = null;
				Method setEnsureContinuousMethod = null;
				for (int j=0; j<methods.length; j++) {
					log.debug(methods[j].getName());
					if (methods[j].getName().equals("process")) {
						processMethod = methods[j];
					} else if (methods[j].getName().equals("setEnsureContinuous")) {
						setEnsureContinuousMethod = methods[j];
					} 
				}			
				if (processMethod==null
						|| setEnsureContinuousMethod == null) throw new NoSuchMethodException();
				setEnsureContinuousMethod.invoke(null, continuousAfter);
				return (WordprocessingMLPackage)processMethod.invoke(null, srcPackage);

			} catch (ClassNotFoundException e) {
				extensionMissing(e);
    			justGotAComponent = false;
				return srcPackage;
				//throw new Docx4JException("Problem processing w:altChunk", e);
			} catch (NoSuchMethodException e) {
				//Degrade gracefully
				extensionMissing(e);
    			justGotAComponent = false;
				return srcPackage;
				//throw new Docx4JException("Problem processing w:altChunk", e);
			} catch (Exception e) {
				throw new Docx4JException("Problem processing w:altChunk", e);
			} 
		}
	    	
    	public void makeContinuous(SectPr sectPr) {
    		
    		if (sectPr==null) {
    			log.warn("sectPr was null");
    			return;
    		}
    		
    		SectPr.Type type = Context.getWmlObjectFactory().createSectPrType();
    		type.setVal("continuous");
    		sectPr.setType( type );
    		
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
	    	
		
        private PartName getNewPartName(String prefix, String suffix, RelationshipsPart rp) throws InvalidFormatException {
        	
        	PartName proposed = null;
        	int i=1;
        	do {
        		
        		if (i>1) {
        			proposed = new PartName( prefix + i + suffix);
        		} else {
        			proposed = new PartName( prefix + suffix);        			
        		}
        		i++;
        		
        	} while (rp.getRel(proposed)!=null);
        	
        	return proposed;
        	
        }
		
		public void extensionMissing(Exception e) {
			log.error("\n" + e.getClass().getName() + ": " + e.getMessage() + "\n");
			log.error("* You don't appear to have the MergeDocx paid extension,");
			log.error("* which is necessary to merge docx, or process altChunk.");
			log.error("* Purchases of this extension support the docx4j project.");
			log.error("* Please visit www.plutext.com if you want to buy it.");
		}
		
//		private static void preprocessRun(WordprocessingMLPackage wordMLPackage, ContentAccessor content) throws Docx4JException {
//			
//			log.info("\n\n Preprocess run.. \n\n");
//
//			MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
//			
//			if (wordMLPackage.getMainDocumentPart().getXPathsPart()==null) {
//				throw new Docx4JException("OpenDoPE XPaths part missing");
//			} else {
//				xPaths = wordMLPackage.getMainDocumentPart().getXPathsPart().getJaxbElement();
//				log.debug( XmlUtils.marshaltoString(xPaths, true, true));
//			}
//			if (wordMLPackage.getMainDocumentPart().getConditionsPart()!=null) {
//				conditions = wordMLPackage.getMainDocumentPart().getConditionsPart().getJaxbElement();
//				log.debug( XmlUtils.marshaltoString(conditions, true, true));
//			}
//			if (wordMLPackage.getMainDocumentPart().getComponentsPart()!=null) {
//				components = wordMLPackage.getMainDocumentPart().getComponentsPart().getJaxbElement();
//				log.debug( XmlUtils.marshaltoString(components, true, true));
//			}
//
//			org.docx4j.wml.Document wmlDocumentEl = (org.docx4j.wml.Document)documentPart.getJaxbElement();
//			Body body =  wmlDocumentEl.getBody();
//			
//			shallowTraversor.wordMLPackage = wordMLPackage; 
//			
//			new TraversalUtil(body, shallowTraversor); 
//			
//		}
		
		//private static XPathsPart getXPathsPart(WordprocessingMLPackage wordMLPackage) {
		//	wordMLPackage.getParts().
		//}
		
		
		
		/**
		 * This traversor duplicates the repeats, and removes false conditonals
		 */
		private class ShallowTraversor implements TraversalUtil.Callback {

//			private static Logger log = Logger.getLogger(ShallowTraversor.class);		
			
			WordprocessingMLPackage wordMLPackage;
			
			@Override
			public List<Object> apply(Object o) {
				
				// apply processSdt to any sdt
				// which might be a conditional|repeat

				if (o instanceof org.docx4j.wml.SdtBlock 
						|| o instanceof org.docx4j.wml.SdtRun
						|| o instanceof org.docx4j.wml.CTSdtRow 
						|| o instanceof org.docx4j.wml.CTSdtCell ) {
					
					if (getSdtPr(o).getDataBinding()==null) {
						// a real binding attribute trumps any tag
						return processBindingRoleIfAny(wordMLPackage, o);
					}
									
				} else {
//					log.warn("TODO: Handle " + o.getClass().getName()  + " (if that's an sdt)");					
				}
				
				// Otherwise just preserve the content
				List<Object> newContent = new ArrayList<Object>();
				newContent.add(o);
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
				List children = getChildren(parentUnwrapped);
				if (children == null) {
					log.warn("no children: " + parentUnwrapped.getClass().getName());
					return;
				} else {
					for (Object o : children) {

						newChildren.addAll(
											this.apply(
												XmlUtils.unwrap(o)));
						// TODO: Post 2.7.0, review use of XmlUtils.unwrap(o) here;
						// it can result in MarshalException for
						// things which don't have @XmlRootElement.
						// We really need some unit tests to be confident
						// that making this change would be ok.
					}
				}
				// Replace list, so we'll traverse all the new sdts we've just
				// created
				TraversalUtil.replaceChildren(parentUnwrapped, newChildren);
				

				children = getChildren(parentUnwrapped);
				if (children == null) {
					log.warn("no children: " + parentUnwrapped.getClass().getName());
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
		
		/**
		 * This applies to any sdt which might be a conditional|repeat
		 *  
		 * @param wordMLPackage
		 * @param sdtParent
		 * @param sdt
		 * @param tag
		 * @param sdtContent
		 * @return
		 */
		private List<Object> processBindingRoleIfAny(WordprocessingMLPackage wordMLPackage,
				Object sdt) 
		{
			log.debug("Processing " + getSdtPr(sdt).getId().getVal() );
			Tag tag = getSdtPr(sdt).getTag();										
			
			if (tag==null) {
				List<Object> newContent = new ArrayList<Object>();
				newContent.add(sdt);
				return newContent;
			} 
			
			log.info(tag.getVal());

			QueryString qs = new QueryString();
			HashMap<String, String> map = qs.parseQueryString(tag.getVal(), true);
			
			String conditionId = map.get(BINDING_ROLE_CONDITIONAL);
			String repeatId = map.get(BINDING_ROLE_REPEAT);
			String xp = map.get(BINDING_ROLE_XPATH);
			if (conditionId==null
					&& repeatId==null
					&& xp==null
					) {
				List<Object> newContent = new ArrayList<Object>();
				newContent.add(sdt);
				return newContent;
			} 

			Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts
				= wordMLPackage.getCustomXmlDataStorageParts();		
			
			
			if (conditionId!=null) {

				log.info("Processing Conditional: " + tag.getVal());
							
				// At present, this only handles simple conditions
				Condition c = ConditionsPart.getConditionById(conditions, conditionId);
				if (c==null) {
					log.error("Missing condition " + conditionId);
				}
				org.opendope.xpaths.Xpaths.Xpath xpath = getXPathFromCondition(c);
				
				String val = BindingHandler.xpathGetString(wordMLPackage,
						customXmlDataStorageParts,
						xpath.getDataBinding().getStoreItemID(),
						xpath.getDataBinding().getXpath(),
						xpath.getDataBinding().getPrefixMappings() );					
				
				log.info("Got value: " + val);
				
				if (new Boolean(val) ) {
					log.debug("so keeping");
					
					List<Object> newContent = new ArrayList<Object>();
					newContent.add(sdt);
					return newContent;
					
        } else {
				  return eventuallyEmptyList(sdt);
				}
				
			} else if ( repeatId!=null) {

				log.info("Processing Repeat: " + tag.getVal());
				
				final List<Object> repeatResult = processRepeat(sdt,
						customXmlDataStorageParts,
						wordMLPackage.getMainDocumentPart().getXPathsPart());
				
				if (repeatResult.isEmpty()) {
				  return eventuallyEmptyList(sdt);
				  
				} else {
				  return repeatResult;
				}
		        	
			} else if ( xp!=null) {
				
				if (getSdtPr(sdt).getDataBinding()!=null) {					
					// the XPath evaluates to an element, which Word can
					// handle, so do nothing (shouldn't get here anyway though)
					List<Object> newContent = new ArrayList<Object>();
					newContent.add(sdt);
					return newContent;					
				}
				log.info("Processing XPath expression: " + tag.getVal());
				log.info(XmlUtils.marshaltoString(sdt, true, true));
				
				List<Object> contentList = null;
				if (sdt instanceof org.docx4j.wml.SdtBlock) {
					contentList =  ((org.docx4j.wml.SdtBlock) sdt).getSdtContent().getContent();
				} else if (sdt instanceof org.docx4j.wml.SdtRun) { // sdt in paragraph
					contentList =  ((org.docx4j.wml.SdtRun) sdt).getSdtContent().getContent();
				} 
				// An CTSdtRow or CTSdtCell shouldn't be bound
				if (contentList==null || contentList.size()==0 ) {
					List<Object> newContent = new ArrayList<Object>();
					newContent.add(sdt);
					return newContent;										
				}
				
				// Word can't handle an XPath that returns something else
				// eg string or boolean or number, so work this out.
				// In principal, we could do this in this pre-processing step,
				// or via bind.xslt.  But probably slightly better to do it here.
				org.opendope.xpaths.Xpaths.Xpath xpathObj = XPathsPart.getXPathById(xPaths, xp);
				String value = BindingHandler.xpathGetString(
												wordMLPackage,
												customXmlDataStorageParts, 
												xpathObj.getDataBinding().getStoreItemID(),
												xpathObj.getDataBinding().getXpath(),
												xpathObj.getDataBinding().getPrefixMappings() );					
				log.info(xpathObj.getDataBinding().getXpath());
				
				// Now insert
				R r = null;
				Object firstBlock = contentList.get(0);
				if (firstBlock instanceof P ) {
					if (((P)firstBlock).getParagraphContent().get(0) instanceof R) {
						r = (R)((P)firstBlock).getParagraphContent().get(0);						
					}
				} else if (firstBlock instanceof R ) {
						r = ((R)firstBlock);
				}
				if (r==null) {
					// Give up
					log.warn("Couldn't find a run in which to insert xpath value");
					List<Object> newContent = new ArrayList<Object>();
					newContent.add(sdt);
					return newContent;															
				}
				Text  wt = null;
				Object firstInline = XmlUtils.unwrap(r.getRunContent().get(0));
				if (firstInline instanceof Text) {
					wt = (Text)firstInline;
				} else {
					log.warn("First was " + firstInline );
					wt = Context.getWmlObjectFactory().createText();
					r.getRunContent().add(wt);
				}
				wt.setValue(value);
				// Return the sdt with this value set
				List<Object> newContent = new ArrayList<Object>();
				newContent.add(sdt);
				return newContent;					
			}	
			// shouldn't happen
			return null;
		}
		
  /**
   * Under normal instances, return an empty list in order to remove content.
   * 
   * If, however, this would produce a table cell without a content, add an empty content node to this table cell.
   * 
   * For backward reasons, also replace a cell to be removed upon a condition or due to a zero item repeat with an empty
   * cell according to this condition, unless the global flag {@link #removeSdtCellsOnFailedCondition} is set.
   * 
   * @param sdt The SDT node currently being processed.
   * @return The "eventually empty" node list to replace the content of <code>sdt</code>.
   */
  private List<Object> eventuallyEmptyList(final Object sdt) {

    final boolean sdtIsCell = sdt instanceof CTSdtCell;

    final Object parent = obtainParent(sdt);
    final int contentChildCount = countContentChildren(parent);
    final boolean sdtIsSingleCellChild = parent instanceof Tc && contentChildCount == 1;

    final List<Object> newContent;

    if (sdtIsCell && !removeSdtCellsOnFailedCondition) {

      final CTSdtContentCell sdtCellContent = (CTSdtContentCell) ((org.docx4j.wml.CTSdtCell) sdt).getSdtContent();
      final Tc tc = (Tc) XmlUtils.unwrap(sdtCellContent.getContent().get(0));
      tc.getContent().clear();
      final P p = Context.getWmlObjectFactory().createP();
      tc.getContent().add(p);
      newContent = Arrays.asList((Object) tc);

    } else if (sdtIsSingleCellChild) {

      final Object p = Context.getWmlObjectFactory().createP();
      newContent = Arrays.asList(p);

    } else {
      newContent = Arrays.asList();
    }

    return newContent;
  }
		
    private Object obtainParent(Object sdt) {
      if (! (sdt instanceof Child )) 
        throw new IllegalArgumentException("Object of class "+ sdt.getClass().getName() + " is not a Child");

      return ((Child) sdt).getParent();
    }
    
    private int countContentChildren(final Object tc) {
      final List<Object> selfAndSiblings = obtainChildren(tc);
      int contentChildCount = 0;
      for (final Object child: selfAndSiblings)
        if (! (child instanceof TcPr))
          contentChildCount ++;
      return contentChildCount;
    }
    
    private List<Object> obtainChildren(Object element) {
      if (element instanceof ContentAccessor )
        return ((ContentAccessor) element).getContent();
      
      log.warn("Don't know how to access the children of " + element.getClass().getName());
      return Collections.emptyList();
    }

		public org.opendope.xpaths.Xpaths.Xpath getXPathFromCondition(Condition c) {
			
			org.opendope.conditions.Xpathref xpathRef = c.getXpathref();
			
			if (xpathRef==null) {
				log.error("Condition " + c.getId() + " references a missing xpath!" );
			}
			
			// Now get the xpath
			return XPathsPart.getXPathById(xPaths, xpathRef.getId()); 					
		}
		
		private List<Object>  processRepeat(Object sdt,
				Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
				XPathsPart xPathsPart) {
			
			Tag tag = getSdtPr(sdt).getTag();										
			
			HashMap<String, String> map = QueryString.parseQueryString(tag.getVal(), true);
			
			String repeatId = map.get(BINDING_ROLE_REPEAT);
			
			// Check, whether we are in an old repeat case. These can be removed.
			if (StringUtils.isEmpty(repeatId))
			    return new ArrayList<Object>();

			org.opendope.xpaths.Xpaths.Xpath xpathObj = XPathsPart.getXPathById(xPaths, repeatId);

			String storeItemId = xpathObj.getDataBinding().getStoreItemID();
			String xpath = xpathObj.getDataBinding().getXpath();
			String prefixMappings = xpathObj.getDataBinding().getPrefixMappings();
			
      // Get the bound XML  
      String xpathBase;
//      if (xpath.endsWith("/*")) {
//        xpathBase = xpath.substring(0, xpath.length()-2);
//      } else
        if (xpath.endsWith("/")) {
        xpathBase = xpath.substring(0, xpath.length()-1);
  
        // Check, whether the xpath ends with a [1]. If so, guess it comes from a round-tripped path and strip it
      } else if (xpath.endsWith("[1]")) {
        xpathBase = xpath.substring(0, xpath.length() - 3);
        
      } else {
        xpathBase = xpath;
      }
			
	// DON'T Drop any trailing position! That breaks nested repeats 
//			if (xpathBase.endsWith("]")) 
//				xpathBase = xpathBase.substring(0, xpathBase.lastIndexOf("["));
			
			log.info("/n/n Repeat: using xpath: " + xpathBase);
			NamespaceContext nsContext = new NamespacePrefixMappings();			
			List<Node> repeatedSiblings = xpathGetNodes(customXmlDataStorageParts,
					storeItemId, xpathBase, prefixMappings);	
//					storeItemId, xpathBase+"/*", prefixMappings);	
			
			// Count siblings
			int numRepeats = repeatedSiblings.size();
			log.debug("yields REPEATS: " + numRepeats );
			
			if (numRepeats==0) {
				return new ArrayList<Object>();  // effectively, delete
			}
			
			// duplicate content here ...
			List<Object> repeated = cloneRepeatSdt( sdt,
					 xpathBase,  numRepeats);
			
			// deep traverse to fix binding
			DeepTraversor dt = new DeepTraversor();
			dt.xpathBase = xpathBase; 
			for (int i=0; i<repeated.size(); i++) {
				
				log.info("\n Traversing clone " + i);
				
				dt.index = i;			
				new TraversalUtil(repeated.get(i), dt); 			
			}
			log.info(".. deep traversals done " );
					
			return repeated;	        	
		}
		
		
	private List<Object> cloneRepeatSdt(Object sdt, String xpathBase,
			int numRepeats) {

		List<Object> newContent = new ArrayList<Object>();

		SdtPr sdtPr = getSdtPr(sdt);

		log.debug(XmlUtils.marshaltoString(sdtPr, true, true));

		// CTDataBinding binding =
		// (CTDataBinding)XmlUtils.unwrap(sdtPr.getDataBinding());
		CTDataBinding binding = sdtPr.getDataBinding();

		for (int i = 0; i < numRepeats; i++) {

			// the first sdt is just copied to the output, the rest has a
			// removed repeat value and no binding
			if (i > 0) {

				// This needs to be done only once, as we're operating on this
				// same object tree.
				if (i == 1) {

					emptyRepeatTagValue(sdtPr.getTag());

					if (binding != null) {
						sdtPr.getRPrOrAliasOrLock().remove(binding);
					}

					// Change ID
					sdtPr.setId();
				}
			}

			// Clone
			newContent.add(XmlUtils.deepCopy(sdt));
		}

		return newContent;
	}
		
	private void emptyRepeatTagValue(final Tag tag) {

		final String tagVal = tag.getVal();
		final Pattern stripRepeatArgPattern = Pattern
				.compile("(.*od:repeat=)([^&]*)(.*)");
		final Matcher stripPatternMatcher = stripRepeatArgPattern
				.matcher(tagVal);
		if (!stripPatternMatcher.matches()) {
			log.fatal("Cannot find repeat tag in sdtPr/tag while processing repeat, sth's very wrong with "
					+ tagVal);
			return;
		}
		final String emptyRepeatValue = stripPatternMatcher.group(1)
				+ stripPatternMatcher.group(3);
		tag.setVal(emptyRepeatValue);
	}
		
		class DeepTraversor implements TraversalUtil.Callback {

//			private static Logger log = Logger.getLogger(DeepTraversor.class);

			int index = 0;
			String xpathBase = null;

			@Override
			public List<Object> apply(Object o) {

//				log.debug("apply for " + o.getClass().getName());

				if (o instanceof org.docx4j.wml.SdtBlock
						|| o instanceof org.docx4j.wml.SdtRun
						|| o instanceof org.docx4j.wml.CTSdtRow
						|| o instanceof org.docx4j.wml.CTSdtCell) { // SdtCell as well, here

					processDescendantBindings(o, xpathBase, index);
						// whether it has a databinding or not

				} else {
//					log.warn("TODO: Handle " + o.getClass().getName()
//							+ " (if that's an sdt)");
				}

				return null; // doesn't matter in this implementation
			}

			@Override
			public void walkJAXBElements(Object parent) {

				List children = getChildren(parent);
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
//				log.debug("getChildren for " + o.getClass().getName());
				return TraversalUtil.getChildrenImpl(o);
			}

			@Override
			public boolean shouldTraverse(Object o) {
				return true;
			}

		}
		
		private void processDescendantBindings(Object sdt,
				String xpathBase, int index) {
			
			SdtPr sdtPr =  getSdtPr(sdt); 
			
			//log.debug(XmlUtils.marshaltoString(sdtPr, true, true));
			
			// Give it a unique ID (supersedes above?)
			sdtPr.setId();
			
			//log.debug(XmlUtils.marshaltoString(sdtPr, true, true));
			CTDataBinding binding = (CTDataBinding)XmlUtils.unwrap(sdtPr.getDataBinding());
			
			String thisXPath = null;
			
			// It'll have one of these three...
			String conditionId =null;
			String repeatId = null;
			String bindingId = null;
			
			Condition c = null;
			org.opendope.xpaths.Xpaths.Xpath xpathObj = null;

			Tag tag = sdtPr.getTag();	
			if (tag==null) return;			
			HashMap<String, String> map = QueryString.parseQueryString(tag.getVal(), true);
			
			if (binding==null) {
				
				conditionId = map.get(BINDING_ROLE_CONDITIONAL);
				repeatId = map.get(BINDING_ROLE_REPEAT);
				
				if (conditionId!=null) {
					
					c = ConditionsPart.getConditionById(conditions, conditionId);
					if (c==null) {
						log.error("Missing condition " + conditionId);
					}
					
					// TODO: this code assumes the condition contains
					// a simple xpath
					log.debug("Using condition" + XmlUtils.marshaltoString(c, true, true));
					xpathObj = getXPathFromCondition(c);
					thisXPath = xpathObj.getDataBinding().getXpath();
					
				} else if (repeatId!=null) {			

					xpathObj = XPathsPart.getXPathById(xPaths, repeatId);
					thisXPath = xpathObj.getDataBinding().getXpath();
					
				} else {
					
					log.warn("couldn't find binding or bindingrole!"); 
					// not all sdt's need have a binding; 
					// they could be present in the docx for other purposes
					return;
				}
				
			} else {
				thisXPath = binding.getXpath();			

				// Set this stuff up now
				bindingId = map.get(BINDING_ROLE_XPATH);
				xpathObj = XPathsPart.getXPathById(xPaths, bindingId);
				
				// Sanity test
				if (!thisXPath.equals(xpathObj.getDataBinding().getXpath())) {
					log.error("XPaths didn't match for id " + bindingId + ": " 
							+ xpathObj.getDataBinding().getXpath() );
				}
			}
					
			final String newPath = enhanceXPath(xpathBase, index + 1, thisXPath);
			
			if (log.isDebugEnabled() && ! thisXPath.equals(newPath)) {
			  log.debug("xpath prefix enhanced " + thisXPath + " to " + newPath);
			}
			
			if (binding==null) {
								
				if (conditionId!=null) {
					
					// Create and add new condition
					Condition newCondition = XmlUtils.deepCopy(c);
					String newConditionId = conditionId + "_" + index;
					newCondition.setId(newConditionId);					
					conditions.getCondition().add(newCondition);
					
					// set sdt to use it
					map.put(BINDING_ROLE_CONDITIONAL, newConditionId);
					tag.setVal(QueryString.create(map));
					
					// TODO: this code assumes the condition contains
					// a simple xpath
					
					// Clone the condition's xpath
					org.opendope.xpaths.Xpaths.Xpath newXPathObj = createNewXPathObject(newPath, 
							xpathObj, index);
					
					// Use it
					newCondition.getXpathref().setId(newXPathObj.getId());					
					
				} else if (repeatId!=null) {			
			
					// Create the new xpath object
					org.opendope.xpaths.Xpaths.Xpath newXPathObj = createNewXPathObject(newPath, 
							xpathObj, index);

					// set sdt to use it
					map.put(BINDING_ROLE_REPEAT, newXPathObj.getId());					
					tag.setVal(QueryString.create(map));
				}
				
			} else {
				binding.setXpath(newPath);
				
				// Also need to create new xpath id, and add that
				org.opendope.xpaths.Xpaths.Xpath newXPathObj = createNewXPathObject(newPath, 
						xpathObj, index);

				// set sdt to use it
				map.put(BINDING_ROLE_XPATH, newXPathObj.getId());					
				tag.setVal(QueryString.create(map));
			}
				
		}
		
    private org.opendope.xpaths.Xpaths.Xpath createNewXPathObject(String newPath, 
				org.opendope.xpaths.Xpaths.Xpath xpathObj, int index) {
			
			org.opendope.xpaths.Xpaths.Xpath newXPathObj = XmlUtils.deepCopy(xpathObj);
			String newXPathId = newXPathObj.getId() + "_" + index;
			newXPathObj.setId(newXPathId);
			newXPathObj.getDataBinding().setXpath(newPath);
			xPaths.getXpath().add(newXPathObj);
			return newXPathObj;
		}
		
//		private static boolean removeSdt(Object sdtParent, Object sdt) {
//			
//			if (sdtParent instanceof org.docx4j.wml.Body) {			
//				return ((org.docx4j.wml.Body)sdtParent).getEGBlockLevelElts().remove(sdt);
//			} else if (sdtParent instanceof org.docx4j.wml.P) {
//				return ((org.docx4j.wml.P)sdtParent).getParagraphContent().remove(sdt);
//			} else if (sdtParent instanceof org.docx4j.wml.Tbl) {
//				return ((org.docx4j.wml.Tbl)sdtParent).getEGContentRowContent().remove(sdt);
//			} else if (sdtParent instanceof org.docx4j.wml.Tr) {
//				return ((org.docx4j.wml.Tr)sdtParent).getEGContentCellContent().remove(sdt);
//			} else if (sdtParent instanceof org.docx4j.wml.Tc) {
//				return ((org.docx4j.wml.Tc)sdtParent).getEGBlockLevelElts().remove(sdt);
//	        } else {
//	        	log.error("TODO: handle removal from parent " + sdtParent.getClass().getName() );
//	        	return false;
//	        }
//		}

		public static SdtPr getSdtPr(Object o) {
			
			if (o instanceof org.docx4j.wml.SdtBlock ) {
				return ((org.docx4j.wml.SdtBlock)o).getSdtPr();
			} else if ( o instanceof org.docx4j.wml.SdtRun ) { // sdt in paragraph
				return ((org.docx4j.wml.SdtRun)o).getSdtPr();
			} else if (  o instanceof org.docx4j.wml.CTSdtRow ) { // sdt wrapping row
				return ((org.docx4j.wml.CTSdtRow)o).getSdtPr();
			} else if (o instanceof org.docx4j.wml.CTSdtCell ) { // sdt wrapping cell
				return ((org.docx4j.wml.CTSdtCell)o).getSdtPr();
			} else {
				log.warn("TODO: Handle " + o.getClass().getName() );					
			}		
			return null;
			
			/* Or could have done:
			 * 
			 * 	Object o = XmlUtils.unwrap(raw);
				Method m = o.getClass().getDeclaredMethod("getSdtPr");
				SdtPr sdtPr = (SdtPr)m.invoke(o);
			 * 
			 */
		}
		
//		public static List<Object> getSdtContent(Object o) {
//			
//			if (o instanceof org.docx4j.wml.SdtBlock) {
//				return ((org.docx4j.wml.SdtBlock) o).getSdtContent().getEGContentBlockContent();
//			} else if (o instanceof org.docx4j.wml.SdtRun) { // sdt in paragraph
//				return ((org.docx4j.wml.SdtRun) o).getSdtContent().getParagraphContent();
//			} else if (o instanceof org.docx4j.wml.CTSdtRow) { // sdt wrapping row
//				return ((org.docx4j.wml.CTSdtRow) o).getSdtContent().getEGContentRowContent();
//			} else if (o instanceof org.docx4j.wml.CTSdtCell) { // sdt wrapping cell
//				return ((org.docx4j.wml.CTSdtCell)o).getSdtContent().getEGContentCellContent();
//			} else {
//				log.warn("TODO: Handle " + o.getClass().getName() + " (if that's an sdt)");
//			}
//			return null;
//		}
		
		private List<Node> xpathGetNodes(Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
				String storeItemId, String xpath, String prefixMappings) {
			
			CustomXmlDataStoragePart part = customXmlDataStorageParts.get(storeItemId.toLowerCase());
			if (part==null) {
				log.error("Couldn't locate part by storeItemId " + storeItemId);
				return null;
			}
			return part.getData().xpathGetNodes(xpath, prefixMappings);
		}
		
	

	public static void main(String[] args) throws Exception {
		
//		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/databinding/invoice_old.docx";
//		String save_converted = System.getProperty("user.dir") + "/sample-docs/databinding/invoice.docx";

		String inputfilepath = System.getProperty("user.dir") + "/sample-docs/databinding/CountryRegions_old.xml";
		String save_converted = System.getProperty("user.dir") + "/sample-docs/databinding/CountryRegions.xml";

		
		
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));		
		
		// Create an XPathsPart, and a props part
		XPathsPart xPathsPart = new XPathsPart(new PartName("/customXml/xpaths.xml"));
		wordMLPackage.getMainDocumentPart().addTargetPart(xPathsPart);
		
		xpathsFactory = new org.opendope.xpaths.ObjectFactory();
		org.opendope.xpaths.Xpaths xpaths = xpathsFactory.createXpaths();
		xPathsPart.setJaxbElement(xpaths);
		
		CustomXmlDataStoragePropertiesPart xPathsPropsPart = new CustomXmlDataStoragePropertiesPart(
				new PartName("/customXml/xpathsProps.xml")); 
		
		/* <ds:datastoreItem ds:itemID="{D08E37B0-BBEF-496D-874B-C21E2168A259}" xmlns:ds="http://schemas.openxmlformats.org/officeDocument/2006/customXml">
		 * 	<ds:schemaRefs>
		 * 		<ds:schemaRef ds:uri="http://www.w3.org/2001/XMLSchema"/>
		 * 		<ds:schemaRef ds:uri="http://opendope.org/xpaths"/><
			/ds:schemaRefs>
			 * </ds:datastoreItem> */
		
		org.docx4j.customXmlProperties.ObjectFactory dsf = new org.docx4j.customXmlProperties.ObjectFactory();
		org.docx4j.customXmlProperties.DatastoreItem dsi = dsf.createDatastoreItem();
		dsi.setItemID("{" + UUID.randomUUID().toString().toUpperCase() + "}");
		SchemaRefs schemaRefs = dsf.createSchemaRefs();
		dsi.setSchemaRefs(schemaRefs);
		SchemaRef schemaRef = dsf.createSchemaRefsSchemaRef();
		schemaRef.setUri("http://www.w3.org/2001/XMLSchema");
		schemaRefs.getSchemaRef().add(schemaRef);
		schemaRef = dsf.createSchemaRefsSchemaRef();
		schemaRef.setUri("http://opendope.org/xpaths");
		schemaRefs.getSchemaRef().add(schemaRef);
		
		xPathsPropsPart.setJaxbElement(dsi);
		xPathsPart.addTargetPart(xPathsPropsPart);
		
		// Create a ConditionsPart
		ConditionsPart conditionsPart = new ConditionsPart(new PartName("/customXml/conditions.xml"));		
		wordMLPackage.getMainDocumentPart().addTargetPart(conditionsPart);
		
		conditionsFactory = new org.opendope.conditions.ObjectFactory();
		org.opendope.conditions.Conditions conditions = conditionsFactory.createConditions();
		conditionsPart.setJaxbElement(conditions);
		
		CustomXmlDataStoragePropertiesPart conditionsPropsPart = new CustomXmlDataStoragePropertiesPart(
				new PartName("/customXml/conditionsProps.xml")); 
		
		/*
		<ds:datastoreItem ds:itemID="{E8637D7C-41A8-4079-A250-062638BD1ADF}" xmlns:ds="http://schemas.openxmlformats.org/officeDocument/2006/c
			ustomXml"><ds:schemaRefs><ds:schemaRef ds:uri="http://www.w3.org/2001/XMLSchema"/>
			<ds:schemaRef ds:uri="http://opendope.org/conditions
			"/></ds:schemaRefs></ds:datastoreItem>
*/

		dsi = dsf.createDatastoreItem();
		dsi.setItemID("{" + UUID.randomUUID().toString().toUpperCase() + "}");
		schemaRefs = dsf.createSchemaRefs();
		dsi.setSchemaRefs(schemaRefs);
		schemaRef = dsf.createSchemaRefsSchemaRef();
		schemaRef.setUri("http://www.w3.org/2001/XMLSchema");
		schemaRefs.getSchemaRef().add(schemaRef);
		schemaRef = dsf.createSchemaRefsSchemaRef();
		schemaRef.setUri("http://opendope.org/conditions");
		schemaRefs.getSchemaRef().add(schemaRef);
		
		conditionsPropsPart.setJaxbElement(dsi);
		conditionsPart.addTargetPart(conditionsPropsPart);
		
		// Go through all the sdt's
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();		
		String xpathSdt = "//w:sdt";
		List<Object> list = null;
		try {
			list = documentPart.getJAXBNodesViaXPath(xpathSdt, false);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int i = 0;
		for(Object raw : list) {
			i++;
			
			log.info(raw.getClass().getName() );
			
			Object o = XmlUtils.unwrap(raw);

			Method m = o.getClass().getDeclaredMethod("getSdtPr");
			
			SdtPr sdtPr = (SdtPr)m.invoke(o);
			
			Tag tag = sdtPr.getTag();
			
			if (sdtPr.getDataBinding()!=null) {

				// Just want to add this xpath to xpaths part.
				xpaths.getXpath().add(
						
						tmpCreateXPath("x" + i, 
								sdtPr.getDataBinding().getStoreItemID(),
								sdtPr.getDataBinding().getXpath(),
								sdtPr.getDataBinding().getPrefixMappings() )

				);
				
				// Write tag
				if (tag==null) {
					tag = Context.getWmlObjectFactory().createTag();
					sdtPr.setTag(tag);
				}
				HashMap<String, String> map;
				if (tag.getVal()!=null) {
					map = QueryString.parseQueryString(tag.getVal(), true);
				} else {
					map = new HashMap<String, String>();
				}
				
				map.remove("bindingrole");
				map.put(BINDING_ROLE_XPATH, "x"+i);					
				tag.setVal(QueryString.create(map));					
				
			} else {
									
				if (tag==null) continue;
				log.info(tag.getVal());
				
				HashMap<String, String> map = QueryString.parseQueryString(tag.getVal(), true);
				
				String bindingrole = map.get("bindingrole");
				if (bindingrole==null) continue;  // do nothing

				// get the value
				String storeItemId = map.get("w:storeItemID").toLowerCase();
				String xpath = map.get("w:xpath");
				String prefixMappings = map.get("w:prefixMappings");

				map.remove("w:storeItemID");
				map.remove("w:xpath");
				map.remove("w:prefixMappings");
				
				if (bindingrole.equals("conditional")) {

					log.info("Processing Conditional: " + tag.getVal());
					
					// Create xpath
					xpaths.getXpath().add(
							
							tmpCreateXPath("x" + i, 
									storeItemId,
									xpath,
									prefixMappings )
					);
					
					// Create condition
					Condition c = conditionsFactory.createCondition();
					c.setId("c"+i);
					conditions.getCondition().add(c);
					org.opendope.conditions.Xpathref cxp = conditionsFactory.createXpathref();
					c.setXpathref(cxp);
					cxp.setId("x"+i);
					
					// Write tag
					map.remove("bindingrole");
					map.put(BINDING_ROLE_CONDITIONAL, "c"+i);					
					tag.setVal(QueryString.create(map));					
					
				} else if (bindingrole.equals("repeat")) {

					log.info("Processing Repeat: " + tag.getVal());
						
					// Create xpath
					xpaths.getXpath().add(
							
							tmpCreateXPath("x" + i, 
									storeItemId,
									xpath,
									prefixMappings )
					);

					// Write tag
					map.remove("bindingrole");
					map.put(BINDING_ROLE_REPEAT, "x"+i);					
					tag.setVal(QueryString.create(map));	
				}
					
			}			
		}
		

		SaveToZipFile saver = new SaveToZipFile(wordMLPackage);
		saver.save(save_converted);
		System.out.println("Saved: " + save_converted);
				
	}
	
		
	private static org.opendope.xpaths.Xpaths.Xpath tmpCreateXPath(String id, String storeItemId, String xpx, String prefixMappings) {
		
		org.opendope.xpaths.Xpaths.Xpath xpath = xpathsFactory.createXpathsXpath();
		
		xpath.setId(id);
		org.opendope.xpaths.Xpaths.Xpath.DataBinding db = xpathsFactory.createXpathsXpathDataBinding();
		xpath.setDataBinding(db);
		db.setPrefixMappings( prefixMappings);
		db.setStoreItemID( storeItemId.toUpperCase() );  // Word writes these in UPPER CASE
		db.setXpath( xpx );				
		
		return xpath;
	}

	private static org.opendope.xpaths.ObjectFactory xpathsFactory;
		
	private static org.opendope.conditions.ObjectFactory conditionsFactory;
	
	
}
