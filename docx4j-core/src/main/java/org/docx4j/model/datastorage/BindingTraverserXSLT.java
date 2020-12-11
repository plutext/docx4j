package org.docx4j.model.datastorage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporter;
import org.docx4j.convert.out.html.HtmlCssHelper;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.JaxbValidationEventHandler;
import org.docx4j.model.sdt.QueryString;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.StyleTree.AugmentedStyle;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.model.styles.Tree;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io3.stores.UnzippedPartStore;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.opendope.JaxbCustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart.AddPartBehaviour;
import org.docx4j.org.apache.xalan.extensions.ExpressionContext;
import org.docx4j.relationships.Relationship;
import org.docx4j.utils.ResourceUtils;
import org.docx4j.w14.CTSdtCheckbox;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.CTDataBinding;
import org.docx4j.wml.CTSdtDate;
import org.docx4j.wml.Color;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
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
			
	private DomToXPathMap domToXPathMap = null;
	
	public void setDomToXPathMap(DomToXPathMap domToXPathMap) {
		this.domToXPathMap = domToXPathMap;
	}

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
		
		org.w3c.dom.Document doc = XmlUtils.marshaltoW3CDomDocument(
				part.getJaxbElement() ); 	
		
		try {
			// We used to use a JAXBResult, which 
			// but its better to use DOMResult
			// so we can use part.unmarshal, which should create a binder where possible
			DOMResult result = new DOMResult(); 
			
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
					
			org.docx4j.XmlUtils.transform(doc, xslt, transformParameters, result);
			
//			if (log.isDebugEnabled()) {
//				
//				org.w3c.dom.Document docResult = ((org.w3c.dom.Document)result.getNode());
////				String xml = XmlUtils.w3CDomNodeToString(docResult);
//				log.debug(XmlUtils.w3CDomNodeToString(docResult));
//				return XmlUtils.unmarshal( docResult);
//			} else 
			{
				try {
					// Default behaviour is to fail in the event of content loss
					return unmarshal(((org.w3c.dom.Document)result.getNode()),
							Docx4jProperties.getProperty("docx4j.model.datastorage.BindingTraverserXSLT.ValidationEventContinue", 
									false));
				} catch (UnmarshalException e) {

					log.error("Problem: " + XmlUtils.w3CDomNodeToString(result.getNode()));
					throw e;
				}
					
			}
		} catch (UnmarshalException e) {

			if (!Docx4jProperties.getProperty("docx4j.model.datastorage.BindingTraverserXSLT.ValidationEventContinue", 
					false)) {
				log.error("Configured to fail in the case of content loss; "
						+ "you can set property docx4j.model.datastorage.BindingTraverserXSLT.ValidationEventContinue if you wish to force output to be generated"); 
			}
			
			throw new Docx4JException("Problems applying bindings", e);				
			
		} catch (Exception e) {
			throw new Docx4JException("Problems applying bindings", e);				
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
			run.setRPr(rPr);
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
			afiPart.setBinaryData(r.getBytes()); 		
			
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

		SdtPr sdtPr = null;
		Node sdtPrNode = sdtPrNodeIt.nextNode();
		try {
			sdtPr = (SdtPr)XmlUtils.unmarshal(sdtPrNode);
		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
		}
		
		
		org.w3c.dom.Document docContainer = XmlUtils.neww3cDomDocument();
		DocumentFragment docfrag = docContainer.createDocumentFragment();
		
		XHTMLImporter xHTMLImporter= null;
		Class<?> xhtmlImporterClass = null;
	    try {
	    	xhtmlImporterClass = Class.forName("org.docx4j.convert.in.xhtml.XHTMLImporterImpl");
		    Constructor<?> ctor = xhtmlImporterClass.getConstructor(WordprocessingMLPackage.class);
		    xHTMLImporter = (XHTMLImporter) ctor.newInstance(pkg);
	    } catch (Exception e) {
	        log.error("docx4j-XHTMLImport jar not found. Please add this to your classpath.");
			log.error(e.getMessage(), e);
			return xhtmlError(sdtParent, docContainer, docfrag, "Missing XHTML Handler!");
	    }		
	    
	    xHTMLImporter.setSequenceCounters(sequenceCounters);
	    
	    /* Find setBookmarkIdNext method.
	     * It's not part of the interface until 3.3.0, so use reflection
	     */
		Method[] methods = xhtmlImporterClass.getMethods(); 
		Method method = null;
		for (int j=0; j<methods.length; j++) {
			if (methods[j].getName().equals("setBookmarkIdNext")
					&& methods[j].getParameterTypes().length==1) {
				method = methods[j];
				break;
			}
		}			
		if (method==null) {
			log.info("setBookmarkIdNext method not found.  If you are using docx4j-ImportXHTML v3.2.1 or later, it should be present.");				
		} else {
			try {
			    //xHTMLImporter.setBookmarkIdNext(bookmarkCounter.bookmarkId);
				method.invoke(xHTMLImporter, bookmarkCounter.bookmarkId);
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		}
		
		// If we are in a table cell, ensure oversized images are scaled
		xHTMLImporter.setMaxWidth(-1, null); // re-init
		if (bindingTraverserState.tcStack.peek() != null) {
		    log.debug("inserting in a tc" );
		    BindingTraverserTableHelper.setupMaxWidthAndStyleForTc(
		    		bindingTraverserState.tblStack.peek(), 
		    		bindingTraverserState.tcStack.peek(), xHTMLImporter);
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
			
			
			if (r.startsWith("<span")) {
				// Wrap the XHTML in a span element with @class, @style as appropriate
				// so FS uses suitable CSS 
				
				// Code copied from XsltHTMLFunctions.createBlockForRPr
				Style defaultRunStyle = 
						(pkg.getMainDocumentPart().getStyleDefinitionsPart(false) != null ?
								pkg.getMainDocumentPart().getStyleDefinitionsPart(false).getDefaultCharacterStyle() :
						null);
				
		    	String defaultCharacterStyleId;
		    	if (defaultRunStyle.getStyleId()==null) // possible, for non MS source docx
		    		defaultCharacterStyleId = "DefaultParagraphFont";
		    	else defaultCharacterStyleId = defaultRunStyle.getStyleId();
		    	
		    	
		    	StyleTree styleTree = pkg.getMainDocumentPart().getStyleTree();
				
				// Set @class	
				String classVal =null;
				String rStyleVal = defaultCharacterStyleId;
				if ( rPrSDT!=null && rPrSDT.getRStyle()!=null) {
					rStyleVal = rPrSDT.getRStyle().getVal();
				}
				Tree<AugmentedStyle> cTree = styleTree.getCharacterStylesTree();		
				org.docx4j.model.styles.Node<AugmentedStyle> asn = cTree.get(rStyleVal);
				if (asn==null) {
					log.warn("No style node for: " + rStyleVal);
				} else {
					classVal = StyleTree.getHtmlClassAttributeValue(cTree, asn);		
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
				
				if (css==null && classVal==null) {
					// Do nothing
				} else if (classVal==null) {
					// just @style
					r = "<span style=\"" + css + "\">" + r + "</span>"; 
				} else if (css==null) {
					// just @class
					r = "<span class=\"" + classVal + "\">" + r + "</span>"; 
				} else {
					r = "<span style=\"" + css + "\" class=\"" + classVal + "\">" + r + "</span>"; 					
				}
				log.debug("\nenhanced with css: \n" + r);
				
			} else if (Docx4jProperties.getProperty("docx4j.model.datastorage.BindingTraverser.XHTML.Block.rStyle.Adopt", false)) {
				
				log.debug("Block.rStyle.Adopt..");
				
				// its block level, and we're instructed to apply the paragraph style
				// linked to w:sdtPr/w:rPr/w:rStyle (if any)
				String rStyleVal=null;
				if ( rPrSDT!=null && rPrSDT.getRStyle()!=null) {
					rStyleVal = rPrSDT.getRStyle().getVal();
					log.debug(".." + rStyleVal);
				}
				
				if (rStyleVal==null) {

					log.debug("No rStyle specified ");
					
				} else {
					
					Style pStyle = pkg.getMainDocumentPart().getStyleDefinitionsPart(false).getLinkedStyle(rStyleVal);
					
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
						
						// Recurse the XHTML, adding @class and @style
						r = XHTMLAttrInjector.injectAttrs(r, classVal, css);

						log.debug(".." + r);
						
					}
					
				}
				
			}
			
			
			xHTMLImporter.setHyperlinkStyle(BindingHandler.getHyperlinkResolver().getHyperlinkStyleId());
//	        Method setHyperlinkStyleMethod = xhtmlImporterClass.getMethod("setHyperlinkStyle", String.class);
//	        setHyperlinkStyleMethod.invoke(null, 
//	        		BindingHandler.getHyperlinkResolver().getHyperlinkStyleId());
			
			String baseUrl = null;
			List<Object> results = null;
			try {
				results = xHTMLImporter.convert(r, baseUrl );
//		        Method convertMethod = xhtmlImporterClass.getMethod("convert", String.class, String.class, WordprocessingMLPackage.class );
//		        results = (List<Object>)convertMethod.invoke(null, r, baseUrl, pkg);
		        
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
					
//					if (o instanceof R) {
//
//						// Start with rPrSDT,
//						// and then superimpose on top anything which comes
//						// from the CSS. 
//						
//						if (rPrSDT==null) {
//							// Leave the CSS rPr as it is
//						} else {
//							RPr cssRPR = ((R)o).getRPr(); 
//							if (cssRPR==null) {
//								((R)o).setRPr(rPrSDT);																
//							} else {
//								log.debug("CSS rPR: " + XmlUtils.marshaltoString(cssRPR, true, true));
//								RPr baseRPR = XmlUtils.deepCopy(rPrSDT);
//								
//								// We want to apply
//								// real CSS settings, but not the defaults eg those in 
//								// src/main/resources/XhtmlNamespaceHandler.css								
//								// CSS defaults are:
//								//  <w:color w:val="000000"/>
//								//  <w:sz w:val="22"/>	
//								// We want to ignore those.
//								if (rPrSDT.getColor()!=null
//										&& cssRPR.getColor()!=null
//										&& cssRPR.getColor().getVal().equals("000000")) {
//									cssRPR.setColor(null);
//								}
//								if (rPrSDT.getSz()!=null
//										&& cssRPR.getSz()!=null
//										&& cssRPR.getSz().getVal().toString().equals("22")) {
//									cssRPR.setSz(null);
//								}
//								
//								StyleUtil.apply(cssRPR, baseRPR);
//								((R)o).setRPr(baseRPR);								
//							}
//						}
//					}					
					
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
		
		CustomXmlDataStoragePart part = customXmlDataStorageParts.get(storeItemId.toLowerCase());
		if (part==null) {
			log.error("Couldn't locate part by storeItemId " + storeItemId);
			return null;
		}
		try {
			String xpResult = part.getData().xpathGetString(xpath, prefixMappings);
			log.debug(xpath + " yielded result length" + xpResult.length());
			
			// Base64 decode it
			byte[] bytes = Base64.decodeBase64( xpResult.getBytes("UTF8") );
			
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
		
		CustomXmlDataStoragePart part = customXmlDataStorageParts.get(storeItemId.toLowerCase());
		if (part==null) {
			log.error("Couldn't locate part by storeItemId " + storeItemId);
			return null;
		}
		try {
			String xpResult = part.getData().xpathGetString(xpath, prefixMappings);
			log.debug(xpath + " yielded result length" + xpResult.length());
			
			// Base64 decode it
			byte[] bytes = Base64.decodeBase64( xpResult.getBytes("UTF8") );
			
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
			e.printStackTrace();
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
		RPr rPr = null;
		for (Object o : sdtPr.getRPrOrAliasOrLock() ) {
			o = XmlUtils.unwrap(o); 
			if (o instanceof RPr) {					
				rPr = (RPr)o;
				break;
			}
		}

		String storeItemId = sdtPr.getDataBinding().getStoreItemID();
		String xpath = sdtPr.getDataBinding().getXpath();
		String prefixMappings = sdtPr.getDataBinding().getPrefixMappings();		
		
		CustomXmlPart part = customXmlDataStorageParts.get(storeItemId.toLowerCase());		
		
		if (part==null) {
			log.error("Couldn't locate part by storeItemId " + storeItemId);
			return null;
		}
		
		try {
			String r= part.xpathGetString(xpath, prefixMappings);				
			log.debug(xpath + " yielded result " + r);
			if (r==null) return nullResultParagraph(sdtParent, "[missing!]");
			
			CTSdtDate sdtDate = null;
			Node dateNode = dateNodeIt.nextNode();
			if (dateNode!=null) {
				//sdtDate = (CTSdtDate)XmlUtils.unmarshal(dateNode);
				sdtDate = (CTSdtDate)XmlUtils.unmarshal(dateNode, Context.jc, CTSdtDate.class);
			}
			
			/*
		        <w:date w:fullDate="2012-08-19T00:00:00Z">
		          <w:dateFormat w:val="d/MM/yyyy"/>
		          <w:lid w:val="en-AU"/>
		          <w:storeMappedDataAs w:val="dateTime"/>
		          <w:calendar w:val="gregorian"/>
		        </w:date>
		        
		        Assume our String r contains something like "2012-08-19T00:00:00Z"
		        
		        We need to convert it to the given dateFormat string.
		        
			 */
			// Drop the Z
			if (r.indexOf("Z")>0) {
				r = r.substring(0, r.indexOf("Z")-1);
				log.warn("date now " + r);
			}
			
			DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			
			String format = sdtDate.getDateFormat().getVal();
			log.debug("Using format: " + format);
			
			// C# dddd (eg "Monday') needs translation
			// to "EEEE"
			if (format.contains("dddd")) {
				format = format.replace("dddd", "EEEE");
			}
			
			Format formatter = new SimpleDateFormat(format);
			org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
			
			Date date;
//			RPr rPr = null;
			boolean parseException = false;
			try {
				date = (Date)dateTimeFormat.parse(r);
			} catch (ParseException e) {
				try {
					// 2012-08-28
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					date = (Date) dateFormat.parse(r);
				} catch (ParseException e2) {
					log.warn(e.getMessage());
					date = new Date();

					parseException = true;
					if (rPr==null) {
						rPr = factory.createRPr();						
					}
				}
			}

			String result = formatter.format(date);
			
			org.docx4j.wml.R  run = factory.createR();	
			if (rPr!=null) {
				run.setRPr(rPr);
			}
			if (parseException) {
				// <w:color w:val="FF0000"/>
				Color colorRed = factory.createColor();
				colorRed.setVal("FF0000");
				rPr.setColor(colorRed);				
			}
			org.docx4j.wml.Text text = factory.createText();
			text.setValue(result);
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
		CustomXmlPart part = customXmlDataStorageParts.get(dataBinding.getStoreItemID().toLowerCase());
				
		if (part==null) {
			log.error("Couldn't locate part by storeItemId " + dataBinding.getStoreItemID());
			return null;
		}
		
		try {

			Boolean checkBoxResult = getCheckboxResult(dataBinding, part);
			if (checkBoxResult==null) return nullResultParagraph(sdtParent, "[missing!]");
			
			org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
			org.docx4j.wml.Text text = factory.createText();
			
			// TODO: use the symbols specified for checked and uncheckedState
			if (checkBoxResult.booleanValue()) {
				
		        // <w14:checkedState w14:val="2612" w14:font="MS Gothic"/>
				text.setValue("☒");
				
			} else { // Word treats everything else as false

		        // <w14:uncheckedState w14:val="2610" w14:font="MS Gothic"/>
				text.setValue("☐");
			}
			
			/*
		        <w:p>
		          <w:r>
		            <w:rPr>
		              <w:rFonts w:ascii="MS Gothic" w:eastAsia="MS Gothic" w:hAnsi="MS Gothic" w:hint="eastAsia"/>
		            </w:rPr>
		            <w:t>☐</w:t>
		          </w:r>
		        </w:p>
			 */
			org.docx4j.wml.P  p   = factory.createP();
			
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
		CustomXmlPart part = customXmlDataStorageParts.get(dataBinding.getStoreItemID().toLowerCase());
				
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
