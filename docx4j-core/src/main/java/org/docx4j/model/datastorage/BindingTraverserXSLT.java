package org.docx4j.model.datastorage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.html.HtmlCssHelper;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.JaxbValidationEventHandler;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.sdt.QueryString;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.StyleTree.AugmentedStyle;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.model.styles.Tree;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io3.stores.UnzippedPartStore;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.model.fields.FormattingSwitchHelper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkHtml;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.opendope.JaxbCustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.org.apache.xalan.extensions.ExpressionContext;
import org.docx4j.relationships.Relationship;
import org.docx4j.utils.CompoundTraversalUtilVisitorCallback;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.w14.CTSdtCheckbox;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.CTDataBinding;
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.CTSdtDate;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.PStyle;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tag;
import org.opendope.xpaths.Xpaths;
import org.opendope.xpaths.Xpaths.Xpath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.UnmarshalException;
import jakarta.xml.bind.Unmarshaller;


public class BindingTraverserXSLT extends BindingTraverserCommonImpl {
	
	private static Logger log = LoggerFactory.getLogger(BindingTraverserXSLT.class);		
	
	public static boolean ENABLE_XPATH_CACHE = true;

	static Templates xslt;			
	static {
		try {
			Source xsltSource = new StreamSource(
						ResourceUtils.getResourceViaProperty(
								"docx4j.model.datastorage.BindingTraverserXSLT.xslt",
								"org/docx4j/model/datastorage/bind.xslt"));
			xslt = XmlUtils.getTransformerTemplate(xsltSource);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		
	}
			
	// domToXPathMap field and setter are inherited from BindingTraverserCommonImpl
	// (pulled up in 17.0.4 so all implementations can use the cache)

	/**
	 * @param part
	 * @param pkg
	 * @param doc
	 * @param xPathsPart
	 * @throws Docx4JException
	 */
	public Object traverseToBind(JaxbXmlPart part,
			org.docx4j.openpackaging.packages.OpcPackage pkg,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap)
			throws Docx4JException {
		
		javax.xml.transform.Source source = null;		
		javax.xml.transform.Result result = null;
		
		// If we're using a StAXSource
		XMLStreamReader xmlReader = null;
//        XMLStreamWriter xmlWriter = null; 
        ByteArrayOutputStream baos = null;
        
		if ( ((JaxbXmlPart)part).isUnmarshalled() ) {
			
			log.debug( ((JaxbXmlPart)part).getPartName().getName() + " already unmarshalled.");		
			org.w3c.dom.Document doc = XmlUtils.marshaltoW3CDomDocument(
				part.getJaxbElement() );
			source = new javax.xml.transform.dom.DOMSource(doc);
			
			// We used to use a JAXBResult, which 
			// but its better to use DOMResult
			// so we can use part.unmarshal, which should create a binder where possible
			result = new DOMResult(); 
			
		} else {
			log.debug( ((JaxbXmlPart)part).getPartName().getName() + " not yet unmarshalled.");
			try {
				xmlReader = part.getXMLStreamReader(null);
				source = new StAXSource(xmlReader);
//		        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();            
		        baos = new ByteArrayOutputStream(); 
//				xmlWriter = outputFactory.createXMLStreamWriter(baos, "UTF-8");	
				result = new StreamResult(baos);  // Xalan TransformerImpl doesn't support StAXResult: https://issues.apache.org/jira/browse/XALANJ-2550 
//				result = new StAXResult(xmlWriter);
			} catch (Exception e) {
				throw new Docx4JException(e.getMessage(), e);
			}
		}
		
//		try {
			
			Map<String, Object> transformParameters = new HashMap<String, Object>();
			transformParameters.put("customXmlDataStorageParts", 
					part.getPackage().getCustomXmlDataStorageParts());			
			transformParameters.put("wmlPackage", (WordprocessingMLPackage)pkg);			
			transformParameters.put("sourcePart", part);			
			transformParameters.put("xPathsMap", xpathsMap);			
			transformParameters.put("sequenceCounters", new HashMap<String, Integer>() );
			transformParameters.put("bookmarkIdCounter", new BookmarkCounter(bookmarkId)  );
			BindingTraverserState bindingTraverserState = new BindingTraverserState();
			transformParameters.put("bindingTraverserState",  bindingTraverserState );  // new for 3.3.0
			
			// Set up XPath "cache"; substantially quicker than Xalan XPath for many lookups in large XML data files
			// Our strategy is to try the cache first (if enabled),
			// then if there is a cache miss, use org.apache.xpath.CachedXPathAPI 
			// (which is quicker than default javax.xml.xpath.XPath implementations)
			if (ENABLE_XPATH_CACHE) {
				
				if (domToXPathMap==null /* should be passed from ODH */) {

					// INIT
					//				Xpath xp = xpathsMap.values().iterator().next();
	//				CustomXmlPart cxp  = pkg.getCustomXmlDataStorageParts().get(xp.getDataBinding().getStoreItemID().toLowerCase());
	//				System.out.println("mycxp: " + cxp.getClass().getName());
	//				org.docx4j.openpackaging.parts.CustomXmlDataStoragePart cdsp = (CustomXmlDataStoragePart)cxp;
					
					// We're only caching the first one we encounter
					// (even though, in principle, there could be multiple)
					CustomXmlPart cxp =
							CustomXmlDataStoragePartSelector.getCustomXmlDataStoragePart(
									(WordprocessingMLPackage)pkg);
					
					if (cxp==null) {
						log.warn("No CustomXmlDataStoragePart found; can't cache.");
						/* TODO: would fail on StandardisedAnswersPart
						 * since that extends JaxbCustomXmlDataStoragePart<org.opendope.answers.Answers>
						 */
					} else if (cxp instanceof CustomXmlDataStoragePart) {
						
						CustomXmlDataStoragePart cdsp = (CustomXmlDataStoragePart)cxp;
						
						long start = System.currentTimeMillis();
					
						Document data = cdsp.getData().getDocument();
						
						domToXPathMap = new DomToXPathMap(data);
						domToXPathMap.map();
						long end = System.currentTimeMillis();
						long time = end - start;
			
						log.debug("Mapped in " + time + "ms");
						
					} else if (cxp instanceof JaxbCustomXmlDataStoragePart) {
						
						Document data = XmlUtils.neww3cDomDocument();
						try {
							((JaxbCustomXmlDataStoragePart)cxp).marshal(data);
						} catch (JAXBException e) {
							throw new Docx4JException("Problem caching JaxbCustomXmlDataStoragePart", e);
						}
						domToXPathMap = new DomToXPathMap(data);
						domToXPathMap.map();
//						countMap = domToXPathMap.getCountMap();
						log.debug("Mapped " + domToXPathMap.getCountMap().size() );
						
					} else {
						log.warn("TODO: cache " + cxp.getClass().getName() );
					}
				}
				
				Map<String, String> pathMap = null; 
				if (domToXPathMap!=null) {
					pathMap = domToXPathMap.getPathMap();
				}
				bindingTraverserState.setPathMap(pathMap);
				
			}

			log.debug("Transforming, using " + source.getClass().getName());
			StoreItemResolver.clearStrictFailure();
			org.docx4j.XmlUtils.transform(source, xslt, transformParameters, result);
			log.debug("Transform done");
			InputIntegrityException strictFailure = StoreItemResolver.strictFailure();
			if (strictFailure != null) {
				// raised in an extension function, where Xalan's error listener logs it and goes on (CR-058)
				StoreItemResolver.clearStrictFailure();
				throw strictFailure;
			}
			
			if (result instanceof DOMResult) {
	//			if (log.isDebugEnabled()) {
	//				
	//				org.w3c.dom.Document docResult = ((org.w3c.dom.Document)result.getNode());
	////				String xml = XmlUtils.w3CDomNodeToString(docResult);
	//				log.debug(XmlUtils.w3CDomNodeToString(docResult));
	//				return XmlUtils.unmarshal( docResult);
	//			} else 
			
					try {
						// Default behaviour is to fail in the event of content loss
						
						return unmarshal(((org.w3c.dom.Document)((DOMResult)result).getNode()),
								Docx4jProperties.getProperty("docx4j.model.datastorage.BindingTraverserXSLT.ValidationEventContinue", 
										false));
						
					} catch (UnmarshalException e) {
					
						if (!Docx4jProperties.getProperty("docx4j.model.datastorage.BindingTraverserXSLT.ValidationEventContinue", 
								false)) {
							log.error("Configured to fail in the case of content loss; "
									+ "you can set property docx4j.model.datastorage.BindingTraverserXSLT.ValidationEventContinue if you wish to force output to be generated"); 
						}
						
						throw new Docx4JException("Problems applying bindings", e);				
								
					} catch (Exception e) {
	
						log.error("Problem: " + XmlUtils.w3CDomNodeToString(((DOMResult)result).getNode()));
						throw new Docx4JException(e.getMessage(), e);
					}
						
				} else {
		        
			        try {
				        xmlReader.close();
				        baos.flush();
				        if (log.isDebugEnabled() ) {
				        	byte[] bytes = baos.toByteArray();
				        	log.debug(new String(bytes));
				        	((JaxbXmlPart)part).replacePartContent(bytes);
				        } else {
				        	((JaxbXmlPart)part).replacePartContent(baos.toByteArray());
				        }
				        baos.close(); 
					} catch (Exception e) {
						throw new Docx4JException(e.getMessage(), e);				
					}
			        // No need to return anything, since we've done replacePartContent
					return null;
					
				}
		}
	
	
	/**
	 * Unmarshal a node using Context.jc, WITHOUT fallback to pre-processing in case of failure.
	 * @param n
	 * @return
	 * @throws JAXBException
	 */
	private Object unmarshal(Node n, boolean continu) throws JAXBException {
			
		Unmarshaller u = Context.jc.createUnmarshaller();		
		
		JaxbValidationEventHandler veh = new org.docx4j.jaxb.JaxbValidationEventHandler();
		veh.setContinue(continu);
		
		u.setEventHandler(veh);

		return u.unmarshal( n );
	}
	
	
	/**
	 * Workaround for the fact that Xalan doesn't let us pass an AtomicInteger into an extension
	 * function.  Instead, it converts it into an int, which means the object in our 
	 * bookmarkIdCounter parameter isn't updated.
	 * 
	 * So here we wrap the AtomicInteger in a class, 
	 * 
	 * @author jharrop
	 *
	 */
	public static class BookmarkCounter {
		
		protected AtomicInteger bookmarkId;		
		
		BookmarkCounter(AtomicInteger bookmarkId) {
			this.bookmarkId = bookmarkId;
		}
		
	}
	
	
	public static void log(ExpressionContext expressionContext, String message ) {
		
		//log.info( com.sun.org.apache.xalan.internal.lib.NodeInfo.lineNumber(expressionContext ) + "  " +  message);
			// com.sun.org.apache hell 
		// but that only gives line number of input XML anyway, whereas more useful is
		// currently executing line number of XSLT.  ErrorListener seems to know this?  Explore some time...
		
		log.info( "[String] " + message);
	}

	/**
	 * @param nodeIterator
	 * @deprecated
	 */
	public static void log(NodeIterator nodeIterator ) {
	
		Node n = nodeIterator.nextNode();		
		log.info(XmlUtils.w3CDomNodeToString(n));
	}
	
	public static void logXml(NodeIterator nodeIterator ) {
		// Has different method, to prevent Xalan preferring the String log method
		log(nodeIterator);
	}
	
	//&lt;html&gt;&lt;body&gt;  &lt;p&gt;hello &lt;/p&gt; &lt;/body&gt;&lt;/html&gt;
	
	private static DocumentFragment placeholderFragment = null;
	private static byte[] placeholderBytes = null;
	private static final String placeholderResourceFallback = "org/docx4j/model/datastorage/placeholder.xml";
	private static final String placeholderResource = "OpenDoPE/placeholder.xml"; // default, can be overridden since 3.2.0

	/**
	 * Calling code should set w:sdtPr/w:showingPlaceholder (ie bind.xslt), so RemovalHandler can do
	 * the right thing for Quantifier.ALL_BUT_PLACEHOLDERS case.
	 * 
	 * bind.xslt inserts the correct element structure for a simple bind, so 
	 * all we do here is return the w:r element.
	 * 
	 * @param rPr
	 * @param sdtParent
	 * @return
	 * @throws Exception
	 */
	protected static DocumentFragment createPlaceholder(RPr rPr) throws Exception {
		return createPlaceholder(rPr, "p"); // this returns the w:r, which bind.xslt then wraps as appropriate.
	}
	/**
	 * Used from convertXHTML, since bind.xslt leaves it to extension function
	 * to insert correct element structure.
	 * 
	 * @param rPr
	 * @param sdtParent
	 * @return
	 * @throws Exception
	 */
	protected static DocumentFragment createPlaceholder(RPr rPr, String sdtParent) throws Exception {
		
		// One time
		if (placeholderFragment==null) {
			createPlaceholderFragment();
		}
		if (placeholderBytes==null) {
			createPlaceholderBytes();
		}

		if (sdtParent.equals("p")) {

			if (rPr==null) {
				// Usual case, just reuse the fragment
				return placeholderFragment;
			} else {
				// Specific formatting
				// Note that changing the stylename will affect Quantifier.ALL_BUT_PLACEHOLDERS processing
				R run = (R)XmlUtils.unmarshal(new ByteArrayInputStream(placeholderBytes));
				
				// preserve existing rPr, but apply extra
				if (run.getRPr()==null) {
					run.setRPr(new RPr());
				}
				StyleUtil.apply(rPr, run.getRPr());
				
				Document tmpDoc = XmlUtils.marshaltoW3CDomDocument(run);
				DocumentFragment docfrag = tmpDoc.createDocumentFragment();
				XmlUtils.treeCopy(tmpDoc.getDocumentElement(), docfrag);						
				return docfrag;
			}
			
		} else {
						
			R run = (R)XmlUtils.unmarshal(new ByteArrayInputStream(placeholderBytes));
			// the control's rPr applied over the run's, which keeps the Placeholder Text style the
			// w:showingPlcHdr decision (and CR-063's entry lookup) is made on; until 17.3.5 the run's
			// rPr was replaced, so a block-level control with a w:rPr lost the style
			if (rPr!=null) {
				if (run.getRPr()==null) {
					run.setRPr(new RPr());
				}
				StyleUtil.apply(rPr, run.getRPr());
			}
			Document tmpDoc = XmlUtils.marshaltoW3CDomDocument(run);
			
			DocumentFragment docfrag = tmpDoc.createDocumentFragment();
			
			if (sdtParent.equals("tbl")) {
				
				org.w3c.dom.Element wtr = tmpDoc.createElementNS(Namespaces.NS_WORD12, "tr");
				docfrag.appendChild(wtr);
				
				org.w3c.dom.Element wtc = tmpDoc.createElementNS(Namespaces.NS_WORD12, "tc");
				wtr.appendChild(wtc);
				
				org.w3c.dom.Element wp = tmpDoc.createElementNS(Namespaces.NS_WORD12, "p");
				wtc.appendChild(wp);
				
				wp.appendChild(tmpDoc.getDocumentElement());
				return docfrag;
				
			} else if (sdtParent.equals("tr")) {
				
				org.w3c.dom.Element wtc = tmpDoc.createElementNS(Namespaces.NS_WORD12, "tc");
				docfrag.appendChild(wtc);
				
				org.w3c.dom.Element wp = tmpDoc.createElementNS(Namespaces.NS_WORD12, "p");
				wtc.appendChild(wp);
				
				wp.appendChild(tmpDoc.getDocumentElement());
				return docfrag;
				
			} else if (sdtParent.equals("tc")
					|| sdtParent.equals("body")) {
								
				org.w3c.dom.Element wp = tmpDoc.createElementNS(Namespaces.NS_WORD12, "p");
				docfrag.appendChild(wp);
				
				wp.appendChild(tmpDoc.getDocumentElement());
				return docfrag;
				
			} else {
				// can't happen
				return null;
			}
			
			
		}
	}
	
	private static void createPlaceholderFragment() throws Exception {
		// create it - one time operation
		InputStream is;
		try {
			is = ResourceUtils.getResourceViaProperty("docx4j.model.datastorage.placeholder"  ,  placeholderResource);
			
		} catch (IOException e) {
			log.info("No resource on classpath for property docx4j.model.datastorage.placeholder; falling back to using org/docx4j/model/datastorage/placeholder.xml");
			is = ResourceUtils.getResource(placeholderResourceFallback);
			
		}
		Document tmpDoc = XmlUtils.getNewDocumentBuilder().parse(is);
		placeholderFragment = tmpDoc.createDocumentFragment();
		XmlUtils.treeCopy(tmpDoc.getDocumentElement(), placeholderFragment);		
	}
	
	private static void createPlaceholderBytes() throws Exception {
		// Only want to do this once
		InputStream is;
		try {
			is = ResourceUtils.getResourceViaProperty("docx4j.model.datastorage.placeholder"  ,  placeholderResource);
			
		} catch (IOException e) {
			log.info("No resource on classpath at docx4j.model.datastorage.placeholder; falling back to using org/docx4j/model/datastorage/placeholder.xml");
			is = ResourceUtils.getResource(placeholderResourceFallback);
			
		}
		placeholderBytes = IOUtils.toByteArray(is);		
	}

	private static R configuredPlaceholderRun = null;
	private static boolean configuredPlaceholderRunResolved = false;

	/**
	 * CR-063 D4: the placeholder run a deployment configured (docx4j.model.datastorage.placeholder),
	 * or null where the run in use is docx4j's own (placeholder.xml), whose glossary entry is Word's
	 * default. Not to be modified by the caller.
	 * @since 17.3.5
	 */
	static synchronized R configuredPlaceholderRun() {
		if (!configuredPlaceholderRunResolved) {
			configuredPlaceholderRunResolved = true;
			try {
				if (placeholderBytes==null) {
					createPlaceholderBytes();
				}
				R run = (R)XmlUtils.unmarshal(new ByteArrayInputStream(placeholderBytes));
				R fallback = (R)XmlUtils.unmarshal(ResourceUtils.getResource(placeholderResourceFallback));
				if (!XmlUtils.marshaltoString(run, true, false).equals(XmlUtils.marshaltoString(fallback, true, false))) {
					configuredPlaceholderRun = run;
					log.info("A configured placeholder run is in use; its glossary entry will be its own (CR-063 D4)");
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return configuredPlaceholderRun;
	}

	/**
	 * CR-063 D11 (check 71), for bind.xslt's picture branch: true where the picture control's bound node
	 * is empty, so the control is to show docx4j's placeholder picture in Word's form. False where the
	 * node holds a value, or no part can be resolved (the branch then does as before).
	 * @since 17.3.5
	 */
	public static boolean pictureValueIsEmpty(WordprocessingMLPackage pkg,
			Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
			String storeItemId, String xpath, String prefixMappings) {
		CustomXmlPart part = StoreItemResolver.resolve(customXmlDataStorageParts, storeItemId, xpath, prefixMappings);
		if (part==null) return false;
		try {
			return PicturePlaceholder.isEmpty(part.xpathGetString(xpath, prefixMappings));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * CR-063 D11: the placeholder picture's drawing for a picture control whose node is empty, shaped
	 * for the control's place as xpathInjectImage shapes an image ({@link PicturePlaceholder}).
	 * @since 17.3.5
	 */
	public static DocumentFragment picturePlaceholder(WordprocessingMLPackage pkg, JaxbXmlPart sourcePart,
			String sdtParent, String contentChild) {
		try {
			return PicturePlaceholder.placeholderFragment(pkg, sourcePart, sdtParent, contentChild);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return XmlUtils.neww3cDomDocument().createDocumentFragment();
		}
	}

	/**
	 * CR-063 D10: the main styles part given Placeholder Text where it lacks it, for bind.xslt's w:sdtPr
	 * template, wherever a control's content carries a placeholder run ({@link
	 * PlaceholderEntries#ensurePlaceholderStyle}). Returns an empty fragment.
	 * @since 17.3.5
	 */
	public static DocumentFragment placeholderStyle(WordprocessingMLPackage pkg) {
		PlaceholderEntries.ensurePlaceholderStyle(pkg);
		return XmlUtils.neww3cDomDocument().createDocumentFragment();
	}

	/**
	 * CR-063: the w:placeholder of a control showing its placeholder, for bind.xslt's w:sdtPr template:
	 * the entry the control shows ({@link PlaceholderEntries#referenceFor}), where the placeholder run
	 * in the content is the control's own (not a nested control's); otherwise the reference the control
	 * has, if any. The glossary gains the entry where it lacks it. Returns an empty fragment where the
	 * control is to have no w:placeholder.
	 * @since 17.3.5
	 */
	public static DocumentFragment placeholderReference(WordprocessingMLPackage pkg,
			NodeIterator sdtPrNodeIt, NodeIterator contentNodeIt) {

		Document doc = XmlUtils.neww3cDomDocument();
		DocumentFragment frag = doc.createDocumentFragment();
		try {
			SdtPr sdtPr = (SdtPr)XmlUtils.unmarshal(sdtPrNodeIt.nextNode());
			Node content = contentNodeIt==null ? null : contentNodeIt.nextNode();
			String name = null;
			if (content!=null && ownPlaceholderRun(content, false)!=null) {
				name = PlaceholderEntries.referenceFor(pkg, sdtPr);
			}
			if (name==null) {
				name = PlaceholderEntries.nameOf(sdtPr);
			}
			if (name==null) return frag;
			org.docx4j.wml.CTPlaceholder ph = Context.getWmlObjectFactory().createCTPlaceholder();
			if (!name.isEmpty()) {
				org.docx4j.wml.CTPlaceholder.DocPart dp = Context.getWmlObjectFactory().createCTPlaceholderDocPart();
				dp.setVal(name);
				ph.setDocPart(dp);
			}
			Document marshalled = XmlUtils.marshaltoW3CDomDocument(
					Context.getWmlObjectFactory().createSdtPrPlaceholder(ph));
			frag.appendChild(doc.importNode(marshalled.getDocumentElement(), true));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return frag;
	}

	/**
	 * CR-063 D2: the content a control shows, for bind.xslt: where the generated content is the control's
	 * own placeholder run (Placeholder Text style, not inside a nested control) and the control shows a
	 * glossary entry ({@link PlaceholderEntries#referenceFor}), the run is replaced by a copy of the
	 * entry's content as the glossary holds it (D9): its first paragraph's run-level content in the
	 * run's place and, where the run is in a paragraph, the entry's further paragraphs after it (each
	 * under the paragraph's own w:pPr). Otherwise a copy of the content as it came.
	 * @since 17.3.5
	 */
	public static DocumentFragment placeholderContent(WordprocessingMLPackage pkg,
			NodeIterator sdtPrNodeIt, NodeIterator contentNodeIt) {

		Document doc = XmlUtils.neww3cDomDocument();
		DocumentFragment frag = doc.createDocumentFragment();
		Node content = contentNodeIt.nextNode();
		if (content==null) return frag;
		if (content.getNodeType()==Node.DOCUMENT_NODE || content.getNodeType()==Node.DOCUMENT_FRAGMENT_NODE) {
			for (Node n = content.getFirstChild(); n!=null; n = n.getNextSibling()) {
				frag.appendChild(doc.importNode(n, true));
			}
		} else {
			frag.appendChild(doc.importNode(content, true));
		}
		try {
			org.w3c.dom.Element run = ownPlaceholderRun(frag, false);
			if (run==null) return frag;
			SdtPr sdtPr = (SdtPr)XmlUtils.unmarshal(sdtPrNodeIt.nextNode());
			String entry = PlaceholderEntries.referenceFor(pkg, sdtPr);
			if (entry==null) return frag;
			List<P> paragraphs = PlaceholderEntries.contentOf(pkg, entry);
			if (paragraphs==null) return frag;

			Node parent = run.getParentNode();
			boolean inParagraph = parent instanceof org.w3c.dom.Element
					&& Namespaces.NS_WORD12.equals(parent.getNamespaceURI()) && "p".equals(parent.getLocalName());

			org.w3c.dom.Element first = (org.w3c.dom.Element)doc.importNode(
					XmlUtils.marshaltoW3CDomDocument(paragraphs.get(0)).getDocumentElement(), true);
			while (first.getFirstChild()!=null) {
				parent.insertBefore(first.getFirstChild(), run);
			}
			parent.removeChild(run);

			if (inParagraph && paragraphs.size()>1) {
				Node container = parent.getParentNode();
				Node after = parent.getNextSibling();
				org.w3c.dom.Element pPr = null;
				for (Node n = parent.getFirstChild(); n!=null; n = n.getNextSibling()) {
					if (n.getNodeType()==Node.ELEMENT_NODE && "pPr".equals(n.getLocalName())
							&& Namespaces.NS_WORD12.equals(n.getNamespaceURI())) {
						pPr = (org.w3c.dom.Element)n;
					}
				}
				for (int i=1; i<paragraphs.size(); i++) {
					org.w3c.dom.Element p = (org.w3c.dom.Element)doc.importNode(
							XmlUtils.marshaltoW3CDomDocument(paragraphs.get(i)).getDocumentElement(), true);
					if (pPr!=null) {
						p.insertBefore(pPr.cloneNode(true), p.getFirstChild());
					}
					container.insertBefore(p, after);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return frag;
	}

	/**
	 * The first w:r in Placeholder Text style under this node that is not inside a w:sdt, or null.
	 */
	private static org.w3c.dom.Element ownPlaceholderRun(Node node, boolean insideSdt) {
		for (Node n = node.getFirstChild(); n!=null; n = n.getNextSibling()) {
			if (n.getNodeType()!=Node.ELEMENT_NODE) continue;
			boolean w = Namespaces.NS_WORD12.equals(n.getNamespaceURI());
			String local = n.getLocalName();
			if (w && "sdt".equals(local)) {
				org.w3c.dom.Element found = ownPlaceholderRun(n, true);
				if (found!=null) return found;
				continue;
			}
			if (w && "r".equals(local) && !insideSdt && isPlaceholderRun((org.w3c.dom.Element)n)) {
				return (org.w3c.dom.Element)n;
			}
			org.w3c.dom.Element found = ownPlaceholderRun(n, insideSdt);
			if (found!=null) return found;
		}
		return null;
	}

	private static boolean isPlaceholderRun(org.w3c.dom.Element r) {
		for (Node n = r.getFirstChild(); n!=null; n = n.getNextSibling()) {
			if (n.getNodeType()==Node.ELEMENT_NODE && "rPr".equals(n.getLocalName())) {
				for (Node m = n.getFirstChild(); m!=null; m = m.getNextSibling()) {
					if (m.getNodeType()==Node.ELEMENT_NODE && "rStyle".equals(m.getLocalName())
							&& PlaceholderEntries.PLACEHOLDER_STYLE.equals(
									((org.w3c.dom.Element)m).getAttributeNS(Namespaces.NS_WORD12, "val"))) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private static Boolean importXHTMLMissing = null;
	
	/**
	 * @since 8.2.1
	 * @return
	 */
	public static Boolean importXHTMLMissing() {
		
		if (importXHTMLMissing==null) {
			
			Class<?> xhtmlImporterClass = null;
		    try {
		    	xhtmlImporterClass = Class.forName("org.docx4j.convert.in.xhtml.XHTMLImporterImpl");
		    	importXHTMLMissing=Boolean.FALSE;
		    } catch (Exception e) {
		    	importXHTMLMissing=Boolean.TRUE;
		    }		
			
		}
		return importXHTMLMissing;
	}
	
	private static Xpath getXpathFromTag(Tag tag, Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap) {

		QueryString qs = new QueryString();
		HashMap<String, String> map = qs.parseQueryString(tag.getVal(), true);
		
		String xpathId = map.get(OpenDoPEHandler.BINDING_ROLE_XPATH);
		
		log.info("Looking for xpath by id: " + xpathId);
		
		//Xpath xpath = xPathsPart.getXPathById(xPathsPart.getJaxbElement(), xpathId);
		Xpath xpath = xpathsMap.get(xpathId);
		if (xpath==null) {
			log.warn("Couldn't find xpath with id: " + xpathId);
		}
		return xpath;
		
	}
	
	private static String evaluate(Xpath xpath, BindingTraverserState bindingTraverserState, 
			WordprocessingMLPackage pkg, Map<String, CustomXmlPart> customXmlDataStorageParts) {
		
		String storeItemId = xpath.getDataBinding().getStoreItemID();
		String xpathExp = xpath.getDataBinding().getXpath();
		String prefixMappings = xpath.getDataBinding().getPrefixMappings();
		
		String r=null;
		if (bindingTraverserState.getPathMap()!=null ) {
			// Try the "cache"
			r = bindingTraverserState.getPathMap().get(normalisePath(xpathExp));
		}
		if (r==null) {			
			log.debug("cache miss for " + xpathExp);
			r = BindingHandler.xpathGetString(pkg, customXmlDataStorageParts, storeItemId, xpathExp, prefixMappings);
		} else if (log.isDebugEnabled()
				&& r.trim().length()==0) {	
			// fallback removed for further speed improvement since we are comfortable there are no "cache query"
			r = BindingHandler.xpathGetString(pkg, customXmlDataStorageParts, storeItemId, xpathExp, prefixMappings);
			// sanity check - results should never differ!
			if (r.trim().length()>0) {	
				log.warn("cache query " + xpathExp);
			}
		} 
		
		return r;
	}
	
	
	/**
	 * Convert the input XHTML into an altChunk, which you'll rely on Word
	 * to convert to real Word content.
	 *
	 * Note that the input XHTML must be suitable for the context 
	 * ie you can't insert block level stuff (eg p) into a run level sdt.
	 * 
	 * For Word to be happy, you'll need to be binding something like:
	 * 
	 *     &lt;html&gt;&lt;head&gt;&lt;title&gt;Import me&lt;/title&gt;&lt;/head&gt;&lt;body&gt;&lt;p&gt;Hello World!&lt;/p&gt;&lt;/body&gt;&lt;/html&gt;
	 *     
	 * rather than eg &lt;p&gt;Hello World!&lt;/p&gt;
	 * @since 8.2.1
	 */
	public static DocumentFragment convertXHTMLtoAltChunk(
			BindingTraverserState bindingTraverserState,
			WordprocessingMLPackage pkg, 
			JaxbXmlPart sourcePart,				
			Map<String, CustomXmlPart> customXmlDataStorageParts,
			//String storeItemId, String xpath, String prefixMappings,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap,
			NodeIterator sdtPrNodeIt, 
			String sdtParent,
			String contentChild,				
//			NodeIterator rPrNodeIt, 
//			String tag,
			Map<String, Integer> sequenceCounters,
			BookmarkCounter bookmarkCounter) {

		log.debug("convertXHTMLtoAltChunk extension function for: " + sdtParent + "/w:sdt/w:sdtContent/" + contentChild);

		SdtPr sdtPr = null;
		Node sdtPrNode = sdtPrNodeIt.nextNode();
		try {
			sdtPr = (SdtPr)XmlUtils.unmarshal(sdtPrNode);
		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
		}
		
		Xpath xpath = getXpathFromTag(sdtPr.getTag(), xpathsMap);
				
		if (xpath==null) {
			return null;
		}
		
		String r = evaluate(xpath, bindingTraverserState, pkg, customXmlDataStorageParts);
		try {

			RPr rPrSDT = (RPr)sdtPr.getByClass(RPr.class);
			
			if (r==null || r.trim().equals("")) {
				// sdtPr.setShowingPlcHdr(true); altering here doesn't work; must do it in XSLT.				
				return createPlaceholder(rPrSDT, sdtParent);
			}

			r = r.trim();
//			log.debug(r);
			//String unescaped = StringEscapeUtils.unescapeHtml(r);
			//log.info("Unescaped: " + unescaped);
			
			// It comes to us unescaped, so the above is unnecessary.
						
			// the AFIP
			AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(AltChunkType.Html); 
			Relationship altChunkRel = pkg.getMainDocumentPart().addTargetPart(afiPart, AddPartBehaviour.RENAME_IF_NAME_EXISTS); 
			// now that its attached to the package ..
			afiPart.registerInContentTypeManager();			
			// UTF-8 with a BOM, without the XML declaration Word refuses (CR-053 D1, D2)
			afiPart.setBinaryData(AltChunkHtml.utf8WithoutXmlDeclaration(r)); 		
			
			// .. the bit in document body 
			CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk(); 
			ac.setId(altChunkRel.getId() ); 
			
			log.debug("context: " + sdtParent);
			
					
			Document tmpDoc = XmlUtils.marshaltoW3CDomDocument(ac);
			
			if (log.isDebugEnabled() ) {
				log.debug(XmlUtils.w3CDomNodeToString(tmpDoc));
			}
			
			DocumentFragment docfrag = XmlUtils.neww3cDomDocument().createDocumentFragment();
			XmlUtils.treeCopy(tmpDoc.getDocumentElement(), docfrag);						
			return docfrag;			
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
		
	/**
	 * Convert the input XHTML into a WordML w3c DocumentFragment, which Xalan 
	 * can insert into XSLT output.
	 *
	 * Note that the input XHTML must be suitable for the context 
	 * ie you can't insert block level stuff (eg p) into a run level sdt.
	 * 
	 * This method requires docx4j-XHTMLImport.jar (LGPL) and its dependencies
	 * in order to function.
	 */
	public static DocumentFragment convertXHTML(
			BindingTraverserState bindingTraverserState,
			WordprocessingMLPackage pkg, 
			JaxbXmlPart sourcePart,				
			Map<String, CustomXmlPart> customXmlDataStorageParts,
			//String storeItemId, String xpath, String prefixMappings,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap,
			NodeIterator sdtPrNodeIt, 
			String sdtParent,
			String contentChild,				
//			NodeIterator rPrNodeIt, 
//			String tag,
			Map<String, Integer> sequenceCounters,
			BookmarkCounter bookmarkCounter) {

		log.debug("convertXHTML extension function for: " + sdtParent + "/w:sdt/w:sdtContent/" + contentChild);

		// Get the SdtPr 
		SdtPr sdtPr = null;
		Node sdtPrNode = sdtPrNodeIt.nextNode();
		if (sdtPrNode==null) {
			log.warn("No sdtPr Node available");
			return null;
		}
		try {
			sdtPr = (SdtPr)XmlUtils.unmarshal(sdtPrNode);
		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
		}
		if (sdtPr==null) {
			return null;
		}
		
		org.w3c.dom.Document docContainer = XmlUtils.neww3cDomDocument();
		DocumentFragment docfrag = docContainer.createDocumentFragment();
		
		// Initialize xHTMLImporter via reflection
		Object xHTMLImporter= null;
		Class<?> xhtmlImporterClass = null;
	    try {
	    	xhtmlImporterClass = Class.forName("org.docx4j.convert.in.xhtml.XHTMLImporterImpl");
		    Constructor<?> ctor = xhtmlImporterClass.getConstructor(WordprocessingMLPackage.class);
		    xHTMLImporter = ctor.newInstance(pkg);
	    } catch (Exception e) {
	        log.error("docx4j-XHTMLImport jar not found. Please add this to your classpath.");
			log.error(e.getMessage(), e);
			return xhtmlError(sdtParent, docContainer, docfrag, "Missing XHTML Handler!");
	    }		
	    
		Method[] methods = xhtmlImporterClass.getMethods(); 
		boolean setBookmarkIdNext = false;
		boolean setSequenceCounters = false;
		boolean setMaxWidth = false;
		
		Method xhtmlMaxWidthMethod = null;
		
		for (int j=0; j<methods.length; j++) {
			
			if (methods[j].getName().equals("setBookmarkIdNext")
					&& methods[j].getParameterTypes().length==1) {
				try {
				    //xHTMLImporter.setBookmarkIdNext(bookmarkCounter.bookmarkId);
					methods[j].invoke(xHTMLImporter, bookmarkCounter.bookmarkId);
					setBookmarkIdNext = true;
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
			}
			
			if (methods[j].getName().equals("setSequenceCounters")
					&& methods[j].getParameterTypes().length==1) {
				try {
				    //xHTMLImporter.setSequenceCounters(sequenceCounters);
					methods[j].invoke(xHTMLImporter, sequenceCounters);
					setSequenceCounters = true;
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
			}
			
			if (methods[j].getName().equals("setMaxWidth")
					&& methods[j].getParameterTypes().length==2) {
				try {
					// xHTMLImporter.setMaxWidth(-1, null); // re-init
					xhtmlMaxWidthMethod = methods[j]; 
					xhtmlMaxWidthMethod.invoke(xHTMLImporter, -1, null);
					setMaxWidth = true;
				} catch (Exception e1) {
					log.error(e1.getMessage(), e1);
				}
			}
			
			
		}			
		if (!setBookmarkIdNext) {
			log.error("setBookmarkIdNext method not found. ");				
		} 
		if (!setSequenceCounters) {
			log.error("setSequenceCounters method not found. ");				
		} 
		if (!setMaxWidth) {
			log.error("setMaxWidth method not found. ");				
		} 
		
		// since 17.2.0 (CR-013): docx4j.properties may set the importer's FormattingOptions
		XHTMLImporterFormatting.apply(xHTMLImporter, xhtmlImporterClass);
		
		// If we are in a table cell, ensure oversized images are scaled
		if (bindingTraverserState.tcStack.peek() != null) {
		    log.debug("inserting in a tc" );
		    if (xhtmlMaxWidthMethod!=null) {
			    try {
					BindingTraverserTableHelper.setupMaxWidthAndStyleForTc(
							bindingTraverserState.tblStack.peek(), 
							bindingTraverserState.tcStack.peek(), xHTMLImporter, xhtmlMaxWidthMethod);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
			    }
		    }
		} 

		// Use the XPath to get the XHTML
		Xpath xpath = getXpathFromTag(sdtPr.getTag(), xpathsMap);		
		if (xpath==null) {
			return null;
		}
		String r = evaluate(xpath, bindingTraverserState, pkg, customXmlDataStorageParts);
		
		
		try {

			RPr rPrSDT = (RPr)sdtPr.getByClass(RPr.class);
			
			if (r==null || r.trim().equals("")) {
				// sdtPr.setShowingPlcHdr(true); altering here doesn't work; must do it in XSLT.				
				return createPlaceholder(rPrSDT, sdtParent);
			}

			r = r.trim();
//			log.debug(r);
			//String unescaped = StringEscapeUtils.unescapeHtml(r);
			if (log.isDebugEnabled()) {
				log.debug("Input XHTML: " + r);
			}			
			// It comes to us unescaped, so the above is unnecessary.
			
			PropertyResolver propertyResolver = pkg.getMainDocumentPart().getPropertyResolver(); 
			
			PPr effectivePPr = null; // if there is a linked pStyle
			
			// RPr
			RPr effectiveRPr = propertyResolver.getEffectiveRPr(rPrSDT);
			log.info("effectiveRPr: " + XmlUtils.marshaltoString(effectiveRPr));
			
			String rStyleVal = null;
			Style pStyle = null;
			
			// PPr				
			if ( rPrSDT!=null && rPrSDT.getRStyle()!=null && rPrSDT.getRStyle().getVal()!=null) {
				rStyleVal = rPrSDT.getRStyle().getVal();
				log.debug(".." + rStyleVal);
				pStyle = pkg.getMainDocumentPart().getStyleDefinitionsPart(false).getLinkedStyle(rStyleVal);	
				/*
				 * NB: the Word interface doesn't render formatting in an SDT's linked P style!!
				 * For example, indent.
				 * This means that our output might look different (eg be indented differently)
				 * to what the author saw in Word.
				 * TODO:  consider whether this is a bug in Word (or a feature...).
				 * Maybe this means we should be ignoring the linked P style?
				 * XHTML handling should be consistent with normal block SDTs?
				 */
				if (pStyle==null) {						
					log.debug("No linked style for " + rStyleVal);						
					// This is OK, use default P style
					effectivePPr = propertyResolver.getResolvedDefaultParagraphStyle(); // no need to clone since we won't change it
				} else {						
					effectivePPr = propertyResolver.getEffectivePPr(pStyle.getStyleId());
				}
			} else {
				log.debug("No rStyle specified ");	
				// so no linked pStyle.  Use default pStyle				
				effectivePPr = propertyResolver.getResolvedDefaultParagraphStyle(); // no need to clone since we won't change it
				// Edge case TODO: what if something higher up in the character style hierarchy had a linked P style?					
			}
				
			if (!Docx4jProperties.getProperty("docx4j.model.datastorage.BindingTraverser.XHTML.PrioritiseRPr", false)) {
				// since 11.5.12					

				// Apply everything in the rPr  
				// since applying direct formatting to the SDT is the easiest way to author in Word.
				// but allow XHTML formatting to override it.
				// Note: PrioritiseRPr==true case is handled further below
				log.info("Giving priority to XHTML formatting");
				
				// Hierarchy:
				// linked pStyle (if any) hierarchy, for pPr only
				// rStyle hierarchy including rPrDefault				
				// direct rPr
				
				// Note that @class in CSS does not give priority order;
				// the last match in the stylesheet(s) wins.
				
				// So better for us here to determine a single @style
				// and not use @class at all.  Or use just the top level style?
				// Maybe we should be doing that, with just express rPr.
				
				// If any css in the XHTML is to be ignored, that is done in XHTMLImporter config

				// Sanity check: we expect parent to be p
				if (r.startsWith("<span") && !"p".equals(sdtParent)) {
						log.warn("Attempting to insert <span> in " + sdtParent + "/w:sdt. Incompatible.");					
				}
				
				// since 17.2.0 (CR-013): under CLASS_TO_STYLE_ONLY the importer ignores @style
				// at that level, so don't generate CSS it would only discard
				boolean paragraphCssWanted = !XHTMLImporterFormatting.isClassToStyleOnly(XHTMLImporterFormatting.PARAGRAPH);
				boolean runCssWanted = !XHTMLImporterFormatting.isClassToStyleOnly(XHTMLImporterFormatting.RUN);
				
				String css = null;
				StringBuilder result = new StringBuilder();
				if (!r.startsWith("<span") && paragraphCssWanted) {
					HtmlCssHelper.createCss(pkg, effectivePPr, result, true, false);  // honour indent.  TODO: Consider list item case.
				}
				if (runCssWanted) {
					HtmlCssHelper.createCss(pkg, effectiveRPr, result); 
				}
				// that method intentionally skips rFonts, so handle it here for now
				RFonts rFonts = runCssWanted ? effectiveRPr.getRFonts() : null;
				if (rFonts==null) {
					if (runCssWanted) log.info("No rFonts known.");
				} else {
					// eg <w:rFonts w:asciiTheme="minorHAnsi" w:hAnsiTheme="minorHAnsi" w:eastAsiaTheme="minorEastAsia" w:cstheme="minorBidi"/>
					// TODO, we really should we using the full RunFontSelector algorithm, applied to each text node in the HTML
					
					ThemePart themePart = pkg.getMainDocumentPart().getThemePart();
					
					if (rFonts.getAsciiTheme()!=null
							&& themePart!=null) {
						try {
							CTLanguage themeFontLang = null;
							if (pkg.getMainDocumentPart().getDocumentSettingsPart()!=null) {
								try {
									themeFontLang = pkg.getMainDocumentPart().getDocumentSettingsPart().getContents().getThemeFontLang();
								} catch (Docx4JException e) {
									// TODO Auto-generated catch block
									log.error(e.getMessage(), e);
								}
							}
							result.append("font-family: '" +  themePart.getFont(rFonts.getAsciiTheme(), themeFontLang) + "'");
						} catch (Docx4JException e) {
							// TODO Auto-generated catch block
							log.error(e.getMessage(), e);
						}
					} else {
						// No theme, so 
						result.append("font-family: '" +  rFonts.getAscii() + "'");
					}					
				}
				
				css = result.toString();
				if (css.isEmpty()) {
					log.debug("No CSS to add (FormattingOption CLASS_TO_STYLE_ONLY); XHTML left as is");
				} else if (r.startsWith("<span")) {
					r = "<span style=\"" + css + "\">" + r + "</span>";
				} else {
					r = "<div style=\"" + css + "\">" + r + "</div>";					
				}
				log.info("\nenhanced with css: \n" + r);
				
				
			} else if (Docx4jProperties.getProperty("docx4j.model.datastorage.BindingTraverser.XHTML.Block.rStyle.Adopt", false)) {
				// @Deprecated
				// old approach, prior to 11.5.12
				
				/*
					# For block level XHTML content, apply the paragraph style
					# linked to w:sdtPr/w:rPr/w:rStyle (if any).
					# This is how Word allows you to specify pPr, there is no w:sdtPr/w:pPr. 
					# @class will contain style names
					# @css will contain express rPr
					# Note well: this old option does not even apply direct w:sdtPr/w:rPr (!!)
					# UNLESS:
					# (1) there is w:sdtPr/w:rPr/w:rStyle, and
					# (2) that w:rStyle has a linked pStyle. 
				 */
				
				log.debug("Block.rStyle.Adopt..");
				
				// its block level, and we're instructed to apply the paragraph style
				// linked to w:sdtPr/w:rPr/w:rStyle (if any)
				if (rStyleVal==null) {
					log.debug("No rStyle specified ");					
				} else {
					
					if (pStyle==null) {						
						log.warn("No linked style for " + rStyleVal);						
					} else {
						
						// Got the pStyle .. now apply it in the XHTML
				    	StyleTree styleTree = pkg.getMainDocumentPart().getStyleTree();
				    	
				    	String pStyleVal = pStyle.getStyleId();
						log.debug(".." + pStyleVal);
				    									
						// Set @class	
						String classVal =null;
						Tree<AugmentedStyle> pTree = styleTree.getParagraphStylesTree();		
						org.docx4j.model.styles.Node<AugmentedStyle> asn = pTree.get(pStyleVal);
						if (asn==null) {
							log.warn("No style node for: " + pStyleVal);
						} else {
							classVal = StyleTree.getHtmlClassAttributeValue(pTree, asn);		
						}
						
						String css = null;
						if ( rPrSDT!=null) {
							StringBuilder result = new StringBuilder();
							HtmlCssHelper.createCss(pkg, rPrSDT, result);
							css = result.toString();
							if (css.equals("")) {
								css =null;
							}
						}
						// Recurse the XHTML block level content, adding @class and @style
						// to any elements where it is not already present.
						// Hmmm TODO we should probably stop recursing as soon as something is
						// encountered, since otherwise we'll be overriding perhaps inadvertently
						r = XHTMLAttrInjector.injectAttrs(r, classVal, css);

						log.debug(".." + r);
					}
				}
				
			} else {
				if (log.isInfoEnabled()) {
					log.info("Block level XHTML content; ignoring sdtPr/rPr.  ");
				}
				
			}
			
	        Method setHyperlinkStyleMethod = xhtmlImporterClass.getMethod("setHyperlinkStyle", String.class);
	        setHyperlinkStyleMethod.invoke(xHTMLImporter, 
	        		BindingHandler.getHyperlinkResolver().getHyperlinkStyleId());
			
			// since 17.2.0 (CR-013): user hook; last, so whatever it sets wins
			XHTMLImporterCustomizer customizer = BindingHandler.getXHTMLImporterCustomizer();
			if (customizer!=null) {
				try {
					customizer.customize(xHTMLImporter, sdtPr, bindingTraverserState.tcStack.peek()!=null);
				} catch (Exception e) {
					log.error("XHTMLImporterCustomizer failed for " + sdtPr.getTag().getVal() + ": " + e.getMessage(), e);
				}
			}
			
			String baseUrl = null;
			List<Object> results = null;
			try {
		        Method convertMethod = xhtmlImporterClass.getMethod("convert", String.class, String.class );
		        results = (List<Object>)convertMethod.invoke(xHTMLImporter, r, baseUrl);
		        
			} catch (Exception e) {
				if (e instanceof NullPointerException) {
					((NullPointerException)e).printStackTrace();
				}
				log.error("with XHTML: " + r, e);
				//throw new Docx4JException("Problem converting XHTML", e);
				
				String errMsg = e.getMessage() + " with XHTML from " + sdtPr.getTag().getVal() + " : " + r; 
				
				return xhtmlError(sdtParent, docContainer, docfrag, errMsg);
			}

			
			if (results==null) {
				log.error("Couldn't convert " + r);
				return docfrag;
			}
			
			log.info("Got results: " + results.size() );	
			
			if (Docx4jProperties.getProperty("docx4j.model.datastorage.BindingTraverser.XHTML.PrioritiseRPr", false)) {
				// since 11.5.12

				// Apply everything in the rPr
				// (since applying direct formatting to the SDT is the easiest way to author in Word)
				// *after* XHTML importing
				// so it overrides any XHTML formatting
				// Note: PrioritiseRPr==false case is handled above
				log.info("Giving priority to SDT formatting over XHTML");

				// We want to set these styles and directRPr on the content,
				// and remove any direct rPr (ie from the XHTML)
				// which would override those settings
				
				// So iterate through the results, and recursively apply.
				String pStyleVal = null;
				if (pStyle==null) {
					String defaultParagraphStyleId = pkg.getMainDocumentPart().getStyleDefinitionsPart().getDefaultParagraphStyle().getStyleId();
					if (defaultParagraphStyleId!=null) {
						pStyleVal = defaultParagraphStyleId;
					} // or equivalently, could just leave it blank
				} else {
					pStyleVal = pStyle.getStyleId();
				} // doesn't hurt to set this, even for span level content, since we simply won't encounter any P
				CompoundTraversalUtilVisitorCallback visitor = new CompoundTraversalUtilVisitorCallback(
						List.of(new PFromXHTMLVisitor( pStyleVal,  effectivePPr), 
								new RFromXHTMLVisitor( rPrSDT, effectiveRPr)));			
				visitor.walkJAXBElements(results);				
			}			
			
			log.debug("context: " + sdtParent);
			
			if (results.size()>0  
					&& results.get(0) instanceof P
					&& sdtParent.equals("p")) {
				// Importer class always returns run-level content wrapped in a w:p 
				// so extract contents				
				if (results.size()>1) {
					log.warn("In paragraph context, so extra block-level content is being discarded!");
				}
								
				for (Object o : ((P)results.get(0)).getContent() ) {

					Document tmpDoc = XmlUtils.marshaltoW3CDomDocument(o);
					
					if (log.isDebugEnabled() ) {
						log.debug(XmlUtils.w3CDomNodeToString(tmpDoc));
					}
					XmlUtils.treeCopy(tmpDoc.getDocumentElement(), docfrag);													
				}
				
			} else {
				// Either the first result is not w:p, or context is not inline 
				
				for(Object o : results) {

					if (sdtParent.equals("p") && o instanceof P) {
                        if(log.isWarnEnabled()) {
                            log.warn("DISCARDING conversion result (can't add in context p): " + XmlUtils.marshaltoString(o, true));
                        }
                        continue;
					} else if (log.isDebugEnabled()) {
						log.debug("Conversion result: " + XmlUtils.marshaltoString(o, true));						
					}
					
					Document tmpDoc = XmlUtils.marshaltoW3CDomDocument(o);
					
					if (log.isDebugEnabled() ) {
						log.debug(XmlUtils.w3CDomNodeToString(tmpDoc));
					}
					
					XmlUtils.treeCopy(tmpDoc.getDocumentElement(), docfrag);											
				}
			}
			return docfrag;
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	public static class PFromXHTMLVisitor extends TraversalUtilVisitor<P> {
		
		String pStyleVal;
		PPr effectivePPr;
		
		PFromXHTMLVisitor(String pStyleVal, PPr effectivePPr) {
			this.pStyleVal = pStyleVal;
			this.effectivePPr = effectivePPr;
		}
		
		@Override
		public void apply(P p, Object parent, List<Object> siblings) {

			if (p.getPPr()==null) {
				p.setPPr(new PPr());
				
				// for each property set,
				// - is it in effectivePPr?  if so, unset it
				//    No need here, since no pPr
				
			} else {

				// for each property set,
				// - is it in effectiveRPr?  if so, unset it
				StyleUtil.unset(effectivePPr, p.getPPr());
			}
			
			if (pStyleVal!=null) {
				PStyle pStyle = new PStyle();
				pStyle.setVal(pStyleVal);
				
				p.getPPr().setPStyle(pStyle);
			}
		}
	}
	
	public static class RFromXHTMLVisitor extends TraversalUtilVisitor<R> {

		RPr rPrSDT;
		RPr effectiveRPr;
		
		RFromXHTMLVisitor(RPr rPrSDT, RPr effectiveRPr) {
			this.rPrSDT=rPrSDT;
			this.effectiveRPr = effectiveRPr;
		}
		
		@Override
		public void apply(R r, Object parent, List<Object> siblings) {

			if (r.getRPr()==null) {
				r.setRPr(new RPr());
				
				// for each property set,
				// - is it in effectiveRPr?  if so, unset it
				//    No need here, since no rPr
				
				// - is it in rPrSDT?  if so, copy the setting
				StyleUtil.apply(rPrSDT, r.getRPr());
				
			} else {

				// for each property set,
				// - is it in effectiveRPr?  if so, unset it
				StyleUtil.unset(effectiveRPr, r.getRPr());
				
				// - is it in rPrSDT?  if so, copy the setting
				StyleUtil.apply(rPrSDT, r.getRPr());
				
			}
		}
	}	
	

	/**
	 * @param sdtParent
	 * @param docContainer
	 * @param docfrag
	 * @param errMsg
	 * @return
	 */
	private static DocumentFragment xhtmlError(String sdtParent,
			org.w3c.dom.Document docContainer, DocumentFragment docfrag,
			String errMsg) {
		org.w3c.dom.Element wr = docContainer.createElementNS(Namespaces.NS_WORD12, "r");
		org.w3c.dom.Element wt = docContainer.createElementNS(Namespaces.NS_WORD12, "t");
		wt.setTextContent(errMsg);
		wr.appendChild(wt);
		
		if (sdtParent.equals("p")) {
			docfrag.appendChild(wr);
			return docfrag;
		} else if (sdtParent.equals("tbl")) {
			
			org.w3c.dom.Element wtr = docContainer.createElementNS(Namespaces.NS_WORD12, "tr");
			docfrag.appendChild(wtr);
			
			org.w3c.dom.Element wtc = docContainer.createElementNS(Namespaces.NS_WORD12, "tc");
			wtr.appendChild(wtc);
			
			org.w3c.dom.Element wp = docContainer.createElementNS(Namespaces.NS_WORD12, "p");
			wtc.appendChild(wp);
			
			wp.appendChild(wr);
			
			return docfrag;
		} else if (sdtParent.equals("tr")) {
			org.w3c.dom.Element wtc = docContainer.createElementNS(Namespaces.NS_WORD12, "tc");
			docfrag.appendChild(wtc);
			
			org.w3c.dom.Element wp = docContainer.createElementNS(Namespaces.NS_WORD12, "p");
			wtc.appendChild(wp);
			
			wp.appendChild(wr);
			return docfrag;
		} else if (sdtParent.equals("tc")) {
			org.w3c.dom.Element wp = docContainer.createElementNS(Namespaces.NS_WORD12, "p");
			docfrag.appendChild(wp);
			
			wp.appendChild(wr);
			return docfrag;
		} else {
			// eg body
			org.w3c.dom.Element wp = docContainer.createElementNS(Namespaces.NS_WORD12, "p");
			docfrag.appendChild(wp);
			
			wp.appendChild(wr);
			return docfrag;

		}
	}
	
	/**
	 * bind.xslt calls this, for case where 'od:xpath' is present
	 */	
	public static DocumentFragment xpathGenerateRuns(
			BindingTraverserState bindingTraverserState,
			WordprocessingMLPackage pkg, 
			JaxbXmlPart sourcePart,				
			Map<String, CustomXmlPart> customXmlDataStorageParts,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap,
			NodeIterator sdtPrNodeIt, 
			String sdtParent,
			String contentChild,				
			boolean multiLine,
			BookmarkCounter bookmarkCounter) {
		
		SdtPr sdtPr = null;
		Node sdtPrNode = sdtPrNodeIt.nextNode();
		try {
			sdtPr = (SdtPr)XmlUtils.unmarshal(sdtPrNode);
		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
		}
		String odTag = sdtPr.getTag().getVal();
		
		QueryString qs = new QueryString();
		HashMap<String, String> map = qs.parseQueryString(odTag, true);
		
		String xpathId = map.get(OpenDoPEHandler.BINDING_ROLE_XPATH);
		
		log.debug("Looking for xpath with id: " + xpathId + " referenced from part " + sourcePart.getPartName().getName() + " at " + odTag);
		
		Xpath xpath = null;
		try {
			//xpath = xPathsPart.getXPathById(xPathsPart.getJaxbElement(), xpathId);
			xpath = xpathsMap.get(xpathId);

		} catch (InputIntegrityException iie) {
			throw new InputIntegrityException("Couldn't find xpath with id: " + xpathId + " referenced from part " + sourcePart.getPartName().getName() + " at " + odTag,iie);
			
			// Could fallback to trying to use the databinding sdtPr, but would need to pass that in
		}
		if (xpath ==null) {
			log.error("Couldn't find xpath with id: " + xpathId + " referenced from part " + sourcePart.getPartName().getName() + " at " + odTag);
			throw new InputIntegrityException("Couldn't find xpath with id: " + xpathId );
		}
		String storeItemId = xpath.getDataBinding().getStoreItemID();
		String xpathExp = xpath.getDataBinding().getXpath();
		String prefixMappings = xpath.getDataBinding().getPrefixMappings();
		
		return xpathGenerateRuns(
				bindingTraverserState.getPathMap(),
				 pkg, 
				 sourcePart,				
				 customXmlDataStorageParts,
				 storeItemId,  xpathExp,  prefixMappings,
				 sdtPr, sdtParent, contentChild,
				  multiLine, bookmarkCounter);
	}
	
	
	/**
	 * bind.xslt calls this, for case where 'od:xpath' is not present
	 */
	public static DocumentFragment xpathGenerateRuns(
			BindingTraverserState bindingTraverserState,			
			WordprocessingMLPackage pkg, 
			JaxbXmlPart sourcePart,				
			Map<String, CustomXmlPart> customXmlDataStorageParts,
			String storeItemId, String xpath, String prefixMappings,
			NodeIterator sdtPrNodeIt, 			
			String sdtParent,
			String contentChild,				
			boolean multiLine,
			BookmarkCounter bookmarkCounter) {

		SdtPr sdtPr = null;
		Node sdtPrNode = sdtPrNodeIt.nextNode();
		try {
			sdtPr = (SdtPr)XmlUtils.unmarshal(sdtPrNode);
		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
		}
		
		return xpathGenerateRuns(
				bindingTraverserState.getPathMap(),				
				 pkg, 
				 sourcePart,				
				 customXmlDataStorageParts,
				 storeItemId,  xpath,  prefixMappings,
				 sdtPr, 			
				 sdtParent,
				 contentChild,				
				  multiLine, bookmarkCounter);
	}
	
	/**
	 * bind.xslt calls this for a bound drop-down list or combo box: the {@code w:lastValue} Word
	 * writes, the bound value the shown text stands for ({@link ListControls#lastValueFor}).
	 *
	 * @param listNodeIt the {@code w:dropDownList} or {@code w:comboBox} element
	 * @param shownText the text of the generated content
	 * @since 17.3.4
	 */
	public static String listLastValue(NodeIterator listNodeIt, String shownText) {
		Node listNode = listNodeIt.nextNode();
		String text = shownText == null ? null : shownText.trim();
		if (listNode == null || text == null) return text;
		// the w:listItem children read from the DOM (the list element is no JAXB root of its own)
		for (Node child = listNode.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() != Node.ELEMENT_NODE || !"listItem".equals(child.getLocalName())) continue;
			org.w3c.dom.Element item = (org.w3c.dom.Element) child;
			String display = item.getAttributeNS(Namespaces.NS_WORD12, "displayText");
			String value = item.getAttributeNS(Namespaces.NS_WORD12, "value");
			if (text.equals(display) && value != null && !value.isEmpty()) return value;
		}
		return text;
	}

	/**
	 * Massage an XPath into the form it is in in our cache, 
	 * so a hit is likely.  For example, finding[6][1]/assets[1]/asset[32][1] 
	 * to finding[6]/assets[1]/asset[32] 
	 * 
	 * @param xpIn
	 * @return
	 */
	private static String normalisePath(String xpIn) {
		
		return xpIn.replace("][1]", "]");
	}
	
	public static DocumentFragment xpathGenerateRuns(
			Map<String, String> pathMap,
			WordprocessingMLPackage pkg, 
			JaxbXmlPart sourcePart,				
			Map<String, CustomXmlPart> customXmlDataStorageParts,
			String storeItemId, String xpath, String prefixMappings,
			SdtPr sdtPr, 			
			String sdtParent,
			String contentChild,				
			 boolean multiLine,
			 BookmarkCounter bookmarkCounter) {
		
		/**
		 * TODO test cases:
		 * 
		 * - multiline data, including cases which start/end with empty token
		 * - multiline data with w:multiLine absent or set to 0 ie false
		 * - cases with and without rPr
		 * - inline and block level sdt
		 */

		String r=null;
		if (pathMap!=null ) {
			// Try the "cache"
			r = pathMap.get(normalisePath(xpath));
		}
		if (r==null) {
			log.debug("cache miss for " + xpath);
			r = BindingHandler.xpathGetString(pkg, customXmlDataStorageParts, storeItemId, xpath, prefixMappings);
			if (r==null) {
				// eg part not found, or invalid XPath; leave the sdt as it is
				log.warn(xpath + " yielded null");
				return null;
			}

		} else if (log.isDebugEnabled()
				&& r.trim().length()==0) {
			// fallback removed for further speed improvement since we are comfortable there are no "cache query"
			r = BindingHandler.xpathGetString(pkg, customXmlDataStorageParts, storeItemId, xpath, prefixMappings);
			// sanity check - results should never differ!
			if (r.trim().length()>0) {	
				log.warn("cache query "+ xpath);
			}
		} 
		
		// trim whitespace. 
		r = r.trim();
		
		if (xpath.startsWith("local-name")) {
			r=XmlNameUtil.descapeXmlTypeName(r);
		}
		
		// a drop-down list or combo box shows the item whose value this is, by its display text (ListControls)
		r = ListControls.displayTextFor(sdtPr, r);
		
		try {
			log.info(xpath + "\n yielded result '" + r + "'");
			
			RPr rPr = null;
			for (Object o : sdtPr.getRPrOrAliasOrLock() ) {
				o = XmlUtils.unwrap(o); // Sun/Oracle JAXB (recent versions?) wraps RPR in JAXBElement 
				if (o instanceof RPr) {					
					rPr = (RPr)o;
					break;
				}
			}

			
			Xpaths.Xpath.DataBinding dataBinding = new Xpaths.Xpath.DataBinding();
			dataBinding.setXpath(xpath);
			dataBinding.setPrefixMappings(prefixMappings);
			dataBinding.setStoreItemID(storeItemId);
			return BindingHandler.getValueInserterPlainText().toOpenXml(dataBinding,  rPr, multiLine, bookmarkCounter,
					r, sourcePart);				
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
		
	}

	
	/**
	 * @param wmlPackage
	 * @param sourcePart
	 * @param customXmlDataStorageParts
	 * @param xpathsMap
	 * @param odTag
	 * @param sdtParent
	 * @param contentChild
	 * @param cx
	 * @param cy
	 * @return
	 * @since 11.1.8
	 */
	public static DocumentFragment xpathInjectImage(WordprocessingMLPackage wmlPackage,
			JaxbXmlPart sourcePart,
			Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap,			
			String odTag, 
			String sdtParent,
			String contentChild) {
		
		QueryString qs = new QueryString();
		HashMap<String, String> map = qs.parseQueryString(odTag, true);
		
		String xpathId = map.get(OpenDoPEHandler.BINDING_ROLE_XPATH);
		
		log.debug("Looking for xpath with id: " + xpathId + " referenced from part " + sourcePart.getPartName().getName() + " at " + odTag);
		
		Xpath xpath = null;
		try {
			//xpath = xPathsPart.getXPathById(xPathsPart.getJaxbElement(), xpathId);
			xpath = xpathsMap.get(xpathId);

		} catch (InputIntegrityException iie) {
			throw new InputIntegrityException("Couldn't find xpath with id: " + xpathId + " referenced from part " + sourcePart.getPartName().getName() + " at " + odTag,iie);
			
			// Could fallback to trying to use the databinding sdtPr, but would need to pass that in
		}
		if (xpath ==null) {
			log.error("Couldn't find xpath with id: " + xpathId + " referenced from part " + sourcePart.getPartName().getName() + " at " + odTag);
			throw new InputIntegrityException("Couldn't find xpath with id: " + xpathId );
		}
		String storeItemId = xpath.getDataBinding().getStoreItemID();
		String xpathExp = xpath.getDataBinding().getXpath();
		String prefixMappings = xpath.getDataBinding().getPrefixMappings();
		
		String width=map.get("width"); 
		log.info("Image width: " + width);
		
		if (width.equals("auto")) {
		
			return xpathInjectImage( wmlPackage,
					 sourcePart,
					customXmlDataStorageParts,
					storeItemId, xpathExp, prefixMappings, 
					sdtParent,
					contentChild,
					"0", "0"); // let BPAI scale
		} else {
			return xpathInjectImage( wmlPackage,
					 sourcePart,
					customXmlDataStorageParts,
					storeItemId, xpathExp, prefixMappings, 
					sdtParent,
					contentChild,
					width, "0"); 			
		}
	}

	
	public static DocumentFragment xpathInjectImage(WordprocessingMLPackage wmlPackage,
			JaxbXmlPart sourcePart,
			Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
			String storeItemId, String xpath, String prefixMappings, 
			String sdtParent,
			String contentChild,
			String cx, String cy) {
		
		log.info("Falling back to pre-v3 picture processing for " + xpath);

		log.debug("parent: " + sdtParent);
		log.debug("child: " + contentChild);
		
		// TODO: remove any images in package which are no longer used.
		// Needs to be done once after BindingHandler has been done
		// for all parts for which it is to be called (eg mdp, header parts etc).
		
		CustomXmlPart resolved = StoreItemResolver.resolve(customXmlDataStorageParts, storeItemId, xpath, prefixMappings);
		CustomXmlDataStoragePart part = resolved instanceof CustomXmlDataStoragePart ? (CustomXmlDataStoragePart) resolved : null;
		if (part==null) {
			log.error("Couldn't locate part by storeItemId " + storeItemId);
			return null;
		}
		try {
			String xpResult = part.getData().xpathGetString(xpath, prefixMappings);
			log.debug(xpath + " yielded result length" + xpResult.length());
			
			// Base64 decode it (lenient: ignores whitespace/non-alphabet chars)
			byte[] bytes = java.util.Base64.getMimeDecoder().decode(xpResult);
			
			// Create image part and add it
	        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wmlPackage, sourcePart, bytes);
	        
			
	        String filenameHint = null;
	        String altText = null;
	        int id1 = 0;
	        int id2 = 1;		        		
	        Inline inline = null;
	        long cxl = 0;
	        long cyl = 0;
	        try {
	        	cxl = Long.parseLong(cx);
	        	cyl = Long.parseLong(cy);
	        } catch (Exception e) {}
	        if (cxl>0 &&  cyl==0) {
	        	
				// This signature can scale the image to specified maxWidth		        
		        inline = imagePart.createImageInline( filenameHint, altText, 
		    			id1, id2, false, (int)cxl);
		        
	        } else if (cxl==0 || cyl==0) {
	        	// Let BPAI work out size
	        	log.debug("image size - from image");
		        inline = imagePart.createImageInline( filenameHint, altText, 
		    			id1, id2, false);
	        } else {
	        	// Use existing size
	        	log.debug("image size - from content control size");
                // Respect aspect ratio of injected image
                ImageSize size = imagePart.getImageInfo().getSize();
                double ratio = (double) size.getHeightPx() / (double) size.getWidthPx();
                log.debug("fit ratio: " + ratio);
                if (ratio > 1) {
                    cxl =  (long)((double) cyl / ratio);
                } else {
                    cyl =  (long)((double) cxl * ratio);
                }
		        inline = imagePart.createImageInline( filenameHint, altText, 
		    			id1, id2, cxl, cyl, false);		        	

	        }

	        // In certain circumstances, save it immediately
	        if (wmlPackage.getTargetPartStore()!=null
	        		&& wmlPackage.getTargetPartStore() instanceof UnzippedPartStore) {
	        	log.debug("incrementally saving " + imagePart.getPartName().getName());  
	        	((UnzippedPartStore)wmlPackage.getTargetPartStore()).saveBinaryPart(imagePart);
	        	// remove it from memory
	        	ByteBuffer bb = null;
	        	imagePart.setBinaryData(bb);//new byte[0]);
	        	imagePart.setImageInfo(null); // this might help as well
	        }
	        
	        
	        // Now add the inline in w:p/w:r/w:drawing
			org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
			org.docx4j.wml.Tc tc  = factory.createTc();
			org.docx4j.wml.P  p   = factory.createP();
			if (sdtParent.equals("tr")) {
				tc.getContent().add(p);
			}
			org.docx4j.wml.R  run = factory.createR();		
			org.docx4j.wml.Drawing drawing = factory.createDrawing();		
			run.getContent().add(drawing);		
			drawing.getAnchorOrInline().add(inline);

			if (sdtParent.equals("body")
					|| sdtParent.equals("tr") 
					|| sdtParent.equals("tc") ) {
				p.getContent().add(run);
			} 
			
			
			/* return following node
			 * 
			 *     <w:p>
			          <w:r>
			            <w:drawing>
			              <wp:inline distT="0" distB="0" distL="0" distR="0">
			              	etc
				 */
			
			
			Document document = null;
			
			if (sdtParent.equals("body")
					|| sdtParent.equals("tc") ) {
				document = XmlUtils.marshaltoW3CDomDocument(p);
                if(log.isDebugEnabled()) {
                    log.debug(XmlUtils.marshaltoString(p, true, true));
                }
			} else if ( sdtParent.equals("tr") ) {
				document = XmlUtils.marshaltoW3CDomDocument(tc);
                if(log.isDebugEnabled()) {
                    log.debug(XmlUtils.marshaltoString(tc, true, true));
                }
			} else if ( sdtParent.equals("p") ) {
				document = XmlUtils.marshaltoW3CDomDocument(run);
                if(log.isDebugEnabled()) {
                    log.debug(XmlUtils.marshaltoString(run, true, true));
                }
			} else if ( sdtParent.equals("sdtContent") ) {					
				log.info("contentChild: " + contentChild);
				if (contentChild.equals("p")) {
					p.getContent().add(run);
					document = XmlUtils.marshaltoW3CDomDocument(p);
                    if(log.isDebugEnabled()) {
                        log.debug(XmlUtils.marshaltoString(p, true, true));
                    }
				} else if (contentChild.equals("r")) {
					document = XmlUtils.marshaltoW3CDomDocument(run);
                    if(log.isDebugEnabled()) {
                        log.debug(XmlUtils.marshaltoString(run, true, true));
                    }
				} else {
					log.error("how to inject image for unexpected sdt's content: " + contentChild);					
				}
			} else {
				log.error("how to inject image for unexpected sdt's parent: " + sdtParent);
			}
			
			DocumentFragment docfrag = document.createDocumentFragment();
			docfrag.appendChild(document.getDocumentElement());

			return docfrag;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	/**
	 * Process a rich text control containing an image.
	 * 
	 * @param wmlPackage
	 * @param sourcePart
	 * @param customXmlDataStorageParts
	 * @param xPathsPart
	 * @param tag
	 * @return
	 * @since 3.0.1
	 */
	public static String xpathInjectImageRelId(WordprocessingMLPackage wmlPackage,
			JaxbXmlPart sourcePart,
			Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap,
			String tag) {

		QueryString qs = new QueryString();
		HashMap<String, String> map = qs.parseQueryString(tag, true);
		
		String xpathId = map.get(OpenDoPEHandler.BINDING_ROLE_XPATH);
		
		log.info("Looking for xpath by id: " + xpathId);
	
		
		//Xpath xpath = xPathsPart.getXPathById(xPathsPart.getJaxbElement(), xpathId);
		Xpath xpath = xpathsMap.get(xpathId);
		
		if (xpath==null) {
			log.warn("Couldn't find xpath with id: " + xpathId);
			return null;
		}
		
		String storeItemId = xpath.getDataBinding().getStoreItemID();
		String xpathExp = xpath.getDataBinding().getXpath();
		String prefixMappings = xpath.getDataBinding().getPrefixMappings();	
		
		return xpathInjectImageRelId( wmlPackage,
				sourcePart,
				customXmlDataStorageParts,
				storeItemId,  xpathExp,  prefixMappings);
	}
	
	/**
	 * Pass back to XSLT, the value of w:blip/@r:embed, preserving everything
	 * else about the existing template image.
	 * 
	 * @param wmlPackage
	 * @param sourcePart
	 * @param customXmlDataStorageParts
	 * @param storeItemId
	 * @param xpath
	 * @param prefixMappings
	 * @return
	 * @since 3.0.0
	 */
	public static String xpathInjectImageRelId(WordprocessingMLPackage wmlPackage,
			JaxbXmlPart sourcePart,
			Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
			String storeItemId, String xpath, String prefixMappings) {

		// TODO: remove any images in package which are no longer used.
		// Needs to be done once after BindingHandler has been done
		// for all parts for which it is to be called (eg mdp, header parts etc).
		
		CustomXmlPart resolved = StoreItemResolver.resolve(customXmlDataStorageParts, storeItemId, xpath, prefixMappings);
		CustomXmlDataStoragePart part = resolved instanceof CustomXmlDataStoragePart ? (CustomXmlDataStoragePart) resolved : null;
		if (part==null) {
			log.error("Couldn't locate part by storeItemId " + storeItemId);
			return null;
		}
		try {
			String xpResult = part.getData().xpathGetString(xpath, prefixMappings);
			log.debug(xpath + " yielded result length" + xpResult.length());

			return createImagePartReturnRelId(wmlPackage, sourcePart, xpResult);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Create an image part from base64 image data, related to sourcePart,
	 * returning the new relationship's id (or null on failure).
	 *
	 * @since 17.0.4
	 */
	public static String createImagePartReturnRelId(WordprocessingMLPackage wmlPackage,
			JaxbXmlPart sourcePart, String base64) {

		try {
			// Base64 decode it (lenient: ignores whitespace/non-alphabet chars)
			byte[] bytes = java.util.Base64.getMimeDecoder().decode(base64);

			// Create image part and add it
	        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wmlPackage, sourcePart, bytes);

	        // In certain circumstances, save it immediately
	        if (wmlPackage.getTargetPartStore()!=null
	        		&& wmlPackage.getTargetPartStore() instanceof UnzippedPartStore) {
	        	log.debug("incrementally saving " + imagePart.getPartName().getName());
	        	((UnzippedPartStore)wmlPackage.getTargetPartStore()).saveBinaryPart(imagePart);
	        	// remove it from memory
	        	ByteBuffer bb = null;
	        	imagePart.setBinaryData(bb);//new byte[0]);
	        	imagePart.setImageInfo(null); // this might help as well
	        }

			return imagePart.getRelLast().getId();

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	public static String getRepeatPositionCondition(
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap,
			String odTag) {

		QueryString qs = new QueryString();
		HashMap<String, String> map = qs.parseQueryString(odTag, true);
		
		String xpathId = map.get(OpenDoPEHandler.BINDING_ROLE_RPT_POS_CON);
		
		log.info("Looking for xpath by id: " + xpathId);
		//Xpath xpath = xPathsPart.getXPathById(xPathsPart.getJaxbElement(), xpathId);
		Xpath xpath = xpathsMap.get(xpathId);
		
		String expression =xpath.getDataBinding().getXpath() ;
		log.info(expression);

		return expression;		
	}
	
	public static DocumentFragment nullResultParagraph(String sdtParent, String message) {

		try
		{
			org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
			org.docx4j.wml.R  run = factory.createR();	
			org.docx4j.wml.Text text = factory.createText();
			text.setValue(message);
			run.getContent().add(text);
				
			org.w3c.dom.Document docContainer = XmlUtils.neww3cDomDocument();
			if (sdtParent.equals("p")) {
				// Stuff it in a run
				docContainer = XmlUtils.marshaltoW3CDomDocument(run);						
			} else {
				// Stuff it in a p
				org.docx4j.wml.P  p   = factory.createP();
				p.getContent().add(run);
				docContainer = XmlUtils.marshaltoW3CDomDocument(p);						
			}
			
			DocumentFragment docfrag = docContainer.createDocumentFragment();
			docfrag.appendChild(docContainer.getDocumentElement());
		
			return docfrag;
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
		
	}
	
	public static DocumentFragment xpathDate(WordprocessingMLPackage wmlPackage,
			JaxbXmlPart sourcePart,
			Map<String, CustomXmlPart> customXmlDataStorageParts,
			NodeIterator sdtPrNodeIt, 			
			String sdtParent,
			String contentChild,
			NodeIterator dateNodeIt) {
		
		SdtPr sdtPr = null;
		Node sdtPrNode = sdtPrNodeIt.nextNode();
		try {
			sdtPr = (SdtPr)XmlUtils.unmarshal(sdtPrNode);
		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
		}

		try {
			org.docx4j.wml.R run = xpathDateRun(customXmlDataStorageParts, sdtPr, wmlPackage);
			if (run==null) return nullResultParagraph(sdtParent, "[missing!]");
			if (run.getContent().isEmpty()) {
				// an empty stored value: the placeholder, as for an empty text binding (Word 365, CR-058 D3)
				return createPlaceholder((RPr) sdtPr.getByClass(RPr.class), sdtParent);
			}

			org.w3c.dom.Document docContainer = XmlUtils.neww3cDomDocument();
			if (sdtParent.equals("p")) {
				// Stuff it in a run
				docContainer = XmlUtils.marshaltoW3CDomDocument(run);
			} else {
				// Stuff it in a p
				org.docx4j.wml.P  p   = Context.getWmlObjectFactory().createP();
				p.getContent().add(run);
				docContainer = XmlUtils.marshaltoW3CDomDocument(p);
			}

			DocumentFragment docfrag = docContainer.createDocumentFragment();
			docfrag.appendChild(docContainer.getDocumentElement());

			return docfrag;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}

	}

	/**
	 * The formatted run for a date content control, per its w:date settings
	 * (rPr from the sdtPr applied; an unparseable value formats the current
	 * date in red, as Word does).  Null where the data part or value is
	 * missing, or there are no usable w:date settings.
	 *
	 * @since 17.0.4
	 */
	/**
	 * The date a date control's binding yields, as Word shows it: the stored value is an xs:dateTime
	 * or xs:date, {@code 2012-08-19T00:00:00Z}, {@code 2012-08-19Z}, {@code 2012-08-19T10:00:00+10:00}
	 * or {@code 2012-08-19}, and Word displays its nominal fields (the 19th, whatever the zone), so any
	 * offset is read past, not applied. Until 17.3.4 the Z was dropped with the character before it,
	 * so {@code 2012-08-19Z} became the 1st ({@code 2012-08-1}) and a date-time lost a digit of its
	 * seconds; an offset was left for {@code DateFormat.parse} to ignore (docx4j-portfolio's
	 * reading, 2026-10-22).
	 *
	 * @throws java.time.format.DateTimeParseException when the value is neither
	 */
	static Date parseStoredDate(String stored) {
		String r = stored.trim();
		java.time.LocalDateTime local;
		try {
			local = java.time.LocalDateTime.parse(r, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
		} catch (java.time.format.DateTimeParseException notADateTime) {
			local = java.time.LocalDate.parse(r, java.time.format.DateTimeFormatter.ISO_DATE).atStartOfDay();
		}
		return Date.from(local.atZone(java.time.ZoneId.systemDefault()).toInstant());
	}

	public static org.docx4j.wml.R xpathDateRun(
			Map<String, CustomXmlPart> customXmlDataStorageParts, SdtPr sdtPr) {
		return xpathDateRun(customXmlDataStorageParts, sdtPr, null);
	}

	/** The property naming the zone a stored {@code dateTime} with an offset or {@code Z} is shown in (CR-058 D6). */
	public static final String DATE_TIME_ZONE_PROPERTY = "docx4j.model.datastorage.BindingHandler.dateTimeZone";

	/**
	 * The run for a bound date control, as Word 365 shows it (CR-058, measured 2026-10-23 on
	 * docx4j-portfolio's bind-typed-formats probe): the stored value formatted in Word's date picture
	 * language ({@code w:dateFormat} is the DATE field's {@code \@} syntax: {@code ddd} the weekday,
	 * {@code am/pm} upper-case AM/PM, quoted literals) in the language of {@code w:lid}, else the
	 * document's default language ({@code w:docDefaults}; Word uses its own editing language, never the
	 * run's), else the platform's. A stored value that is not a date is shown as it is, in the control's
	 * formatting (until 17.3.5 it became today's date in red). An empty value gives a run with no
	 * content, which the callers show as the placeholder, as an empty text binding is. The time is
	 * the stored value's nominal time; where the package opts into the conversion
	 * ({@code OpcPackage.setBoundDatesConvertedAsWord(true)}, or the property
	 * {@code docx4j.model.datastorage.BindingHandler.boundDatesConvertedAsWord}), a value carrying an offset
	 * or {@code Z} is converted to the package's zone ({@code OpcPackage.effectiveDateTimeZone()}: its own,
	 * else {@link #DATE_TIME_ZONE_PROPERTY}, else the JVM's), as Word converts to the machine's.
	 *
	 * @param pkg the package, for the document default language; may be null
	 * @return the run; null where the part, the value or usable {@code w:date} settings are missing
	 * @since 17.3.5
	 */
	public static org.docx4j.wml.R xpathDateRun(
			Map<String, CustomXmlPart> customXmlDataStorageParts, SdtPr sdtPr, WordprocessingMLPackage pkg) {

		RPr rPr = (RPr)sdtPr.getByClass(RPr.class);
		CTSdtDate sdtDate = (CTSdtDate)sdtPr.getByClass(CTSdtDate.class);
		CTDataBinding dataBinding = sdtPr.getDataBinding();

		if (sdtDate==null || sdtDate.getDateFormat()==null
				|| sdtDate.getDateFormat().getVal()==null) {
			log.warn("No usable w:date/w:dateFormat");
			return null;
		}

		CustomXmlPart part = StoreItemResolver.resolve(customXmlDataStorageParts, dataBinding.getStoreItemID(), dataBinding.getXpath(), dataBinding.getPrefixMappings());
		if (part==null) {
			log.error("Couldn't locate part by storeItemId " + dataBinding.getStoreItemID());
			return null;
		}

		try {
			String r = part.xpathGetString(dataBinding.getXpath(), dataBinding.getPrefixMappings());
			log.debug(dataBinding.getXpath() + " yielded result " + r);
			if (r==null) return null;

			org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
			org.docx4j.wml.R  run = factory.createR();
			if (rPr!=null) {
				run.setRPr(rPr);
			}
			if (r.trim().isEmpty()) {
				return run; // the placeholder (Word 365 shows it; bind-plain's B5 for a text binding)
			}

			String format = sdtDate.getDateFormat().getVal();
			String lang = dateLanguage(sdtDate, pkg);
			log.debug("Using format: " + format + ", language " + lang);

			String result;
			try {
				Date date = parseStoredDate(r, conversionZone(pkg));
				result = FormattingSwitchHelper.formatDate(format, date, lang);
			} catch (java.time.format.DateTimeParseException e) {
				// Word 365 shows the stored text as it is (bind-typed-formats DF8, DF9)
				log.warn("Bound value '" + r + "' of " + dataBinding.getXpath() + " is not a date: " + e.getMessage());
				result = r;
			}

			org.docx4j.wml.Text text = factory.createText();
			text.setValue(result);
			if (result.startsWith(" ") || result.endsWith(" ")) text.setSpace("preserve");
			run.getContent().add(text);

			return run;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	/** {@code w:lid}, else the document's default language, else null (the platform's). */
	static String dateLanguage(CTSdtDate sdtDate, WordprocessingMLPackage pkg) {
		if (sdtDate.getLid() != null && sdtDate.getLid().getVal() != null && !sdtDate.getLid().getVal().trim().isEmpty()) {
			return sdtDate.getLid().getVal().trim();
		}
		return documentDefaultLanguage(pkg);
	}

	/** {@code w:docDefaults/w:rPrDefault/w:rPr/w:lang/@w:val}, or null. */
	static String documentDefaultLanguage(WordprocessingMLPackage pkg) {
		try {
			if (pkg == null || pkg.getMainDocumentPart() == null
					|| pkg.getMainDocumentPart().getStyleDefinitionsPart() == null) return null;
			org.docx4j.wml.DocDefaults dd = pkg.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement().getDocDefaults();
			if (dd == null || dd.getRPrDefault() == null || dd.getRPrDefault().getRPr() == null
					|| dd.getRPrDefault().getRPr().getLang() == null) return null;
			String val = dd.getRPrDefault().getRPr().getLang().getVal();
			return val == null || val.trim().isEmpty() ? null : val.trim();
		} catch (Exception e) {
			log.debug("No document default language: " + e.getMessage());
			return null;
		}
	}

	/**
	 * The zone a bound date is converted to, or null for the nominal reading: the package's
	 * {@link org.docx4j.openpackaging.packages.OpcPackage#effectiveDateTimeZone()} where the package opts into the
	 * conversion ({@link org.docx4j.openpackaging.packages.OpcPackage#isBoundDatesConvertedAsWord()}); a zone set
	 * alone does not convert (the portfolio's date rule, 2026-10-28, amending CR-058 D6). Without a package, the
	 * JVM-wide property pair.
	 *
	 * @since 17.3.5
	 */
	static java.time.ZoneId conversionZone(org.docx4j.openpackaging.packages.OpcPackage pkg) {
		if (pkg != null) return pkg.isBoundDatesConvertedAsWord() ? pkg.effectiveDateTimeZone() : null;
		if (!Docx4jProperties.getProperty(org.docx4j.openpackaging.packages.OpcPackage.BOUND_DATES_CONVERTED_PROPERTY, false)) return null;
		java.time.ZoneId zone = dateTimeZone();
		return zone != null ? zone : java.time.ZoneId.systemDefault();
	}

	/** The zone {@link #DATE_TIME_ZONE_PROPERTY} names, or null. Until 17.3.5 its being set turned the conversion on; see {@link #conversionZone}. */
	static java.time.ZoneId dateTimeZone() {
		String id = Docx4jProperties.getProperty(DATE_TIME_ZONE_PROPERTY);
		if (id == null || id.trim().isEmpty()) return null;
		try {
			return java.time.ZoneId.of(id.trim());
		} catch (java.time.DateTimeException e) {
			log.warn(DATE_TIME_ZONE_PROPERTY + "=" + id + " is not a zone ID; using the nominal time");
			return null;
		}
	}

	/**
	 * {@link #parseStoredDate(String)}, converted to {@code zone} when the value carries an offset or
	 * {@code Z} and a zone is given (Word reads the {@code Z} as UTC and shows the machine's local time:
	 * on a machine at UTC+11, {@code 2026-10-22T15:05:00Z} shows "2:05 AM" the next day). A value with
	 * no offset, a date-only value, or no zone: the nominal fields, unchanged.
	 *
	 * @since 17.3.5
	 */
	static Date parseStoredDate(String stored, java.time.ZoneId zone) {
		String r = stored.trim();
		if (zone != null) {
			try {
				java.time.LocalDateTime local = java.time.OffsetDateTime.parse(r).atZoneSameInstant(zone).toLocalDateTime();
				return Date.from(local.atZone(java.time.ZoneId.systemDefault()).toInstant());
			} catch (java.time.format.DateTimeParseException noOffset) {
				// no offset to convert from: nominal
			}
		}
		return parseStoredDate(r);
	}
	
	/**
	 * Convert the FlatOPC into an AltChunk, which Xalan 
	 * can insert into XSLT output.
	 * 
	 * @since 3.0.1
	 */
	public static DocumentFragment convertFlatOPC(
			WordprocessingMLPackage pkg, 
			JaxbXmlPart sourcePart,				
			Map<String, CustomXmlPart> customXmlDataStorageParts,
			//String storeItemId, String xpath, String prefixMappings,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap,
			String sdtParent,
			String contentChild,				
			NodeIterator rPrNodeIt, 
			String tag) {

		try {
			log.debug("convertFlatOPC extension function for: " + sdtParent + "/w:sdt/w:sdtContent/" + contentChild);
						
			QueryString qs = new QueryString();
			HashMap<String, String> map = qs.parseQueryString(tag, true);
			
			String xpathId = map.get(OpenDoPEHandler.BINDING_ROLE_XPATH);
			
			log.info("Looking for xpath by id: " + xpathId);
		
			
			//Xpath xpath = xPathsPart.getXPathById(xPathsPart.getJaxbElement(), xpathId);
			Xpath xpath = xpathsMap.get(xpathId);
			
			if (xpath==null) {
				log.warn("Couldn't find xpath with id: " + xpathId);
				return null;
			}
			
			String storeItemId = xpath.getDataBinding().getStoreItemID();
			String xpathExp = xpath.getDataBinding().getXpath();
			String prefixMappings = xpath.getDataBinding().getPrefixMappings();
						
			String r = BindingHandler.xpathGetString(pkg, customXmlDataStorageParts, storeItemId, xpathExp, prefixMappings);
			if (r==null) return nullResultParagraph(sdtParent, "[missing!]");
			if (!r.startsWith("<?xml")) {
				/*
				 * <?xml version="1.0" encoding="utf-8" standalone="yes"?> // Word can't open it without this!
				   <?mso-application progid="Word.Document"?> // optional
				 */
				r = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\n" + r;
			}
			//System.out.println(r);
			
			// .. create the part
			AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(
					getNewPartName("/chunk", ".xml", sourcePart.getRelationshipsPart()));
			
			afiPart.setBinaryData(r.getBytes("UTF-8"));
	
			afiPart.setAltChunkType(AltChunkType.Xml); // Flat OPC XML
			
			
			Relationship altChunkRel =sourcePart.addTargetPart(afiPart);
			
			// now that its attached to the package ..
			afiPart.registerInContentTypeManager();
			
	
			CTAltChunk ac = Context.getWmlObjectFactory()
					.createCTAltChunk();
			ac.setId(altChunkRel.getId());

			// This setting makes no difference in that the altChunk
			// still won't use the style from the containing docx 
			// if it isn't in the styles part in the altChunk!
			
//			// http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/matchSrc.html
//			CTAltChunkPr acPr = Context.getWmlObjectFactory()
//					.createCTAltChunkPr();	
//			BooleanDefaultTrue bft = new BooleanDefaultTrue();
//			bft.setVal(false);
//			acPr.setMatchSrc(bft);
//			ac.setAltChunkPr(acPr);
			
			
			org.w3c.dom.Document docContainer = XmlUtils.marshaltoW3CDomDocument(ac);						
			DocumentFragment docfrag = docContainer.createDocumentFragment();
			docfrag.appendChild(docContainer.getDocumentElement());
			return docfrag;
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
		
	}
	
	/**
	 * Support for w14 checkbox.
	 * 
	 * @since 3.2.2
	 */
	public static DocumentFragment w14Checkbox(WordprocessingMLPackage wmlPackage,
			JaxbXmlPart sourcePart,
			Map<String, CustomXmlPart> customXmlDataStorageParts,			
			NodeIterator sdtPrNodeIt,
			String sdtParent,
			String contentChild) {
		
		SdtPr sdtPr = null;
		Node sdtPrNode = sdtPrNodeIt.nextNode();
		if (sdtPrNode==null) {
			log.error("Couldn't get sdtPr!");
			return null;			
		} else {
			try {
				sdtPr = (SdtPr)XmlUtils.unmarshal(sdtPrNode, Context.jc, SdtPr.class);
			} catch (JAXBException e) {
				log.error(e.getMessage(), e);
			}
		}

    	RPr sdtRPr = null;
	    Object sdtRPrObj = sdtPr.getByClass(RPr.class); 
	    if (sdtRPrObj!=null) {
	    	sdtRPr = (RPr)sdtRPrObj;
	    }
		
		/*
	        <w14:checkbox>
	          <w14:checked w14:val="0"/>
	          <w14:checkedState w14:val="2612" w14:font="MS Gothic"/>
	          <w14:uncheckedState w14:val="2610" w14:font="MS Gothic"/>
	        </w14:checkbox>
        */
		CTSdtCheckbox sdtCheckbox = (CTSdtCheckbox)sdtPr.getByClass(CTSdtCheckbox.class);

		CTDataBinding dataBinding = sdtPr.getDataBinding();
		CustomXmlPart part = StoreItemResolver.resolve(customXmlDataStorageParts, dataBinding.getStoreItemID(), dataBinding.getXpath(), dataBinding.getPrefixMappings());
				
		if (part==null) {
			log.error("Couldn't locate part by storeItemId " + dataBinding.getStoreItemID());
			return null;
		}
		
		try {

			Boolean checkBoxResult = getCheckboxResult(dataBinding, part);
			if (checkBoxResult==null) return nullResultParagraph(sdtParent, "[missing!]");

			org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();

			org.docx4j.wml.P  p   = factory.createP();

			org.docx4j.wml.R  run = checkboxRun(checkBoxResult.booleanValue(), sdtRPr);

			org.docx4j.wml.Tc tc  = factory.createTc();
			if (sdtParent.equals("tr")) {
				tc.getContent().add(p);
			}

			if (sdtParent.equals("body")
					|| sdtParent.equals("tr") 
					|| sdtParent.equals("tc") ) {
				p.getContent().add(run);
			} 
			
			Document document = null;
			
			if (sdtParent.equals("body")
					|| sdtParent.equals("tc") ) {
				document = XmlUtils.marshaltoW3CDomDocument(p);
                if(log.isDebugEnabled()) {
                    log.debug(XmlUtils.marshaltoString(p, true, true));
                }
			} else if ( sdtParent.equals("tr") ) {
				document = XmlUtils.marshaltoW3CDomDocument(tc);
                if(log.isDebugEnabled()) {
                    log.debug(XmlUtils.marshaltoString(tc, true, true));
                }
			} else if ( sdtParent.equals("p") ) {
				document = XmlUtils.marshaltoW3CDomDocument(run);
                if(log.isDebugEnabled()) {
                    log.debug(XmlUtils.marshaltoString(run, true, true));
                }
			} else if ( sdtParent.equals("sdtContent") ) {					
				log.info("contentChild: " + contentChild);
				if (contentChild.equals("p")) {
					p.getContent().add(run);
					document = XmlUtils.marshaltoW3CDomDocument(p);
                    if(log.isDebugEnabled()) {
                        log.debug(XmlUtils.marshaltoString(p, true, true));
                    }
				} else if (contentChild.equals("r")) {
					document = XmlUtils.marshaltoW3CDomDocument(run);
                    if(log.isDebugEnabled()) {
                        log.debug(XmlUtils.marshaltoString(run, true, true));
                    }
				} else {
					log.error("how to inject checkbox for unexpected sdt's content: " + contentChild);					
				}
			} else {
				log.error("how to inject checkbox for unexpected sdt's parent: " + sdtParent);
			}
			
			DocumentFragment docfrag = document.createDocumentFragment();
			docfrag.appendChild(document.getDocumentElement());

			return docfrag;
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		} 			
	}

	/**
	 * The glyph run for a checkbox content control, in the usual MS Gothic,
	 * preserving font size from the sdtPr's rPr (if any).
	 *
	 * @since 17.0.4
	 */
	public static org.docx4j.wml.R checkboxRun(boolean checked, RPr sdtRPr) {

		org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
		org.docx4j.wml.Text text = factory.createText();

		// TODO: use the symbols specified for checked and uncheckedState
		if (checked) {
	        // <w14:checkedState w14:val="2612" w14:font="MS Gothic"/>
			text.setValue("☒");
		} else { // Word treats everything else as false
	        // <w14:uncheckedState w14:val="2610" w14:font="MS Gothic"/>
			text.setValue("☐");
		}

		/*
	        <w:r>
	          <w:rPr>
	            <w:rFonts w:ascii="MS Gothic" w:eastAsia="MS Gothic" w:hAnsi="MS Gothic" w:hint="eastAsia"/>
	          </w:rPr>
	          <w:t>☐</w:t>
	        </w:r>
		 */
		org.docx4j.wml.R  run = factory.createR();
		RPr rpr = factory.createRPr();
	    run.setRPr(rpr);

	    RFonts rfonts = factory.createRFonts();
	    rpr.setRFonts(rfonts);
	        rfonts.setEastAsia( "MS Gothic");
	        rfonts.setHint(org.docx4j.wml.STHint.EAST_ASIA);
	        rfonts.setHAnsi( "MS Gothic");
	        rfonts.setAscii( "MS Gothic");

	    if (sdtRPr!=null) {
	    	// Preserve checkbox font size
	    	 if (sdtRPr.getSz()!=null) rpr.setSz(sdtRPr.getSz());
	    	 if (sdtRPr.getSzCs()!=null) rpr.setSzCs(sdtRPr.getSzCs());
	    }

	    run.getContent().add(text);
	    return run;
	}

	/**
	 * The bound checkbox value ("true"/"1" is checked, anything else unchecked),
	 * or null where the data part or value is missing.
	 *
	 * @since 17.0.4
	 */
	public static Boolean getCheckboxResult(
			Map<String, CustomXmlPart> customXmlDataStorageParts, SdtPr sdtPr) {

		CTDataBinding dataBinding = sdtPr.getDataBinding();
		CustomXmlPart part = StoreItemResolver.resolve(customXmlDataStorageParts, dataBinding.getStoreItemID(), dataBinding.getXpath(), dataBinding.getPrefixMappings());
		if (part==null) {
			log.error("Couldn't locate part by storeItemId " + dataBinding.getStoreItemID());
			return null;
		}
		try {
			return getCheckboxResult(dataBinding, part);
		} catch (Docx4JException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	private static Boolean getCheckboxResult(CTDataBinding dataBinding, CustomXmlPart part)
			throws Docx4JException {
		
		String r = part.xpathGetString(dataBinding.getXpath(), dataBinding.getPrefixMappings());
		log.debug(dataBinding.getXpath() + " yielded result " + r);
		
		if (r==null) return null;
		
		if (r.equals("true") || r.equals("1")) {

			return true;
			
		} else { // Word treats everything else as false

			return false;
		}
	}

	/**
	 * Set w14:checked correctly
	 * 
	 * @since 6.0.0
	 */
	public static String w14CheckboxAttr(Map<String, CustomXmlPart> customXmlDataStorageParts,			
			NodeIterator sdtPrNodeIt) {

		/*
        <w14:checkbox>
          <w14:checked w14:val="0"/>
          <w14:checkedState w14:val="2612" w14:font="MS Gothic"/>
          <w14:uncheckedState w14:val="2610" w14:font="MS Gothic"/>
        </w14:checkbox>
    */
		
		SdtPr sdtPr = null;
		Node sdtPrNode = sdtPrNodeIt.nextNode();
		if (sdtPrNode==null) {
			log.error("Couldn't get sdtPr!");
			return "0";			
		} else {
			try {
				sdtPr = (SdtPr)XmlUtils.unmarshal(sdtPrNode, Context.jc, SdtPr.class);
			} catch (JAXBException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		CTDataBinding dataBinding = sdtPr.getDataBinding();
		CustomXmlPart part = StoreItemResolver.resolve(customXmlDataStorageParts, dataBinding.getStoreItemID(), dataBinding.getXpath(), dataBinding.getPrefixMappings());
				
		if (part==null) {
			log.error("Couldn't locate part by storeItemId " + dataBinding.getStoreItemID());
			return "0";
		}
		
		try {
			Boolean checkBoxResult = getCheckboxResult(dataBinding, part);
			if (checkBoxResult==null) {
				return "0";
			} else if (checkBoxResult.booleanValue()) {
				return "1";				
			} else {
				return "0";				
			}
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return "0";				
		} 			
	}
	
	
	// TODO - add something like this to RelationshipsPart?? 
	private static PartName getNewPartName(String prefix, String suffix,
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
	
}
