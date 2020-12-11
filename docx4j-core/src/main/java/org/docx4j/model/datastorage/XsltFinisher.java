package org.docx4j.model.datastorage;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMResult;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.JaxbValidationEventHandler;
import org.docx4j.model.sdt.QueryString;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.wml.CTDataBinding;
import org.docx4j.wml.SdtPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

/**
 * As an optional step after binding, apply user-defined XSLT to transform
 * this content control.
 * 
 * A template is attached to a content control (a repeat/condition/or normal bind),
 * using tag od:call=XYZ where XYZ is the template to call.
 * 
 * For example, by placing this on a repeat, a table row could be coloured red if
 * its contents met some condition. 
 * 
 * @author jharrop
 * @6.1.0
 */
public class XsltFinisher {

	private static Logger log = LoggerFactory.getLogger(BindingTraverserXSLT.class);
	
	private WordprocessingMLPackage wordMLPackage;
	
	private XsltFinisher() {}
	
	public XsltFinisher(WordprocessingMLPackage wordMLPackage) {
		this.wordMLPackage = wordMLPackage;
	}
	
	
	// a user defined way of selecting the correct XSLT,
	// so users can organize their templates if they want (ie instead of a single monolithic file)
	static XsltProvider xsltProvider;			
	public static void setXsltProvider(XsltProvider xsltProvider) {
		XsltFinisher.xsltProvider = xsltProvider;
	}

	/**
	 * 
	 * finisherParams is a map of parameter values you can pass in,
	 * which named templates can be sensitive to (eg to set a color).
	 *  
	 * @param part
	 * @param xpathsMap
	 * @param finisherParams
	 * @throws Docx4JException
	 */
	public void apply(JaxbXmlPart part,
//			org.docx4j.openpackaging.packages.OpcPackage pkg,
			Map<String, org.opendope.xpaths.Xpaths.Xpath> xpathsMap,
			String filename, Map<String, Map<String, Object>> finisherParams
			)
			throws Docx4JException {

		if (xsltProvider==null) {
			log.debug("No XsltProvider, skipping finisher.");
			return;
		}
		
		Templates template;
		try {
			template = xsltProvider.getFinisherXslt(filename);
			if (template==null) {
				log.debug("No XsltFinisher, skipping.");
			}
		} catch (TransformerConfigurationException e1) {
			throw new Docx4JException(e1.getMessage(), e1);
		}
		
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
			transformParameters.put("wmlPackage", wordMLPackage);			
			transformParameters.put("sourcePart", part);			
			transformParameters.put("xPathsMap", xpathsMap);
			
			transformParameters.put("finisherParams", finisherParams);
			
			org.docx4j.XmlUtils.transform(doc, 
					template, 
					transformParameters, result);
			
//			if (log.isDebugEnabled()) {
//				
//				org.w3c.dom.Document docResult = ((org.w3c.dom.Document)result.getNode());
//				String xml = XmlUtils.w3CDomNodeToString(docResult);
//				System.out.println(XmlUtils.w3CDomNodeToString(docResult));
//				return XmlUtils.unmarshal( docResult);
//			} else 
			{
				try {
					// Default behaviour is to fail in the event of content loss
					part.setJaxbElement(
							unmarshal(((org.w3c.dom.Document)result.getNode()),
							Docx4jProperties.getProperty("docx4j.model.datastorage.BindingTraverserXSLT.ValidationEventContinue", 
									false)));
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
	
	public static String getTemplateName(String tag) {

		HashMap<String, String> map = QueryString.parseQueryString(tag, true);

		return map.get(OpenDoPEHandler.BINDING_ROLE_FINISHER);		
	}
	
	/*
	 * TODO:
	 * - method to get value from od:xpath tag
	 * 
	 * - greater than, less than, equals?
	 * 
	 * - string vs int / number?
	 * 
	 * - method to add image
	 * 
	 */
	
	public static String getXPathValue(
			Map<String, String> pathMap,
			WordprocessingMLPackage pkg, 			
			Map<String, CustomXmlPart> customXmlDataStorageParts,
			NodeIterator sdtPrNodeIt
			) {
		
		SdtPr sdtPr = null;
		Node sdtPrNode = sdtPrNodeIt.nextNode();
		try {
			sdtPr = (SdtPr)XmlUtils.unmarshal(sdtPrNode);
		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
			return null;
		}
		CTDataBinding dataBinding = sdtPr.getDataBinding();
		String storeItemId = dataBinding.getStoreItemID();
		String xpath = dataBinding.getXpath();
		String prefixMappings = dataBinding.getPrefixMappings();
		
		return getXPathValue(pathMap, pkg, customXmlDataStorageParts, storeItemId, xpath, prefixMappings);
	}

	public static String getXPathValue(
			Map<String, String> pathMap,
			WordprocessingMLPackage pkg, 			
			Map<String, CustomXmlPart> customXmlDataStorageParts,
			String storeItemId,
			String xpath,
			String prefixMappings
			) {
		
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
		
		return r;
	}
	
	private static String normalisePath(String xpIn) {
		
		return xpIn.replace("][1]", "]");
	}

	public static Object getParam(Map<String, Map<String, Object>> finisherParams,
			String templateName, String paramName) {
		
		return finisherParams.get(templateName).get(paramName);
		
	}
	
}
