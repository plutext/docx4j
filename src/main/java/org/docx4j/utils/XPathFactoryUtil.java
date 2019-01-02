package org.docx4j.utils;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XPathFactoryUtil {
	
	private static Logger log = LoggerFactory.getLogger(XPathFactoryUtil.class);		
	
	private static XPathFactory xPathFactory;
	
    /**
     * Specify the XPath factory used in getJAXBNodesViaXPath.
     * org.apache.xpath.jaxp.XPathFactoryImpl is recommended. Use something else at your own risk!
     * Note that setting this currently doesn't affect the XPath implementation used in OpenDoPE processing.
     * @param xPathFactory
     * @since 6.1.0
     */
    public static void setxPathFactory(XPathFactory xPathFactory) {
    	
		// Often org.apache.xpath.jaxp.XPathImpl,
    	// but for XPath > 1.0, you can use net.sf.saxon.xpath.XPathFactoryImpl()
    	
		XPathFactoryUtil.xPathFactory = xPathFactory;
        log.info("xpath implementation: " + XPathFactoryUtil.xPathFactory.getClass().getName());
	}

	private static final String DTM_MANAGER_PROP_NAME = "org.apache.xml.dtm.DTMManager";  
    private static final String DTM_MANAGER_CLASS_NAME = "org.apache.xml.dtm.ref.DTMManagerDefault";  	
	
	public static synchronized XPathFactory getXPathFactory() {
		
		if (xPathFactory==null) {
			xPathFactory = XPathFactory.newInstance();
	        log.info("xpath implementation: " + xPathFactory.getClass().getName());
	        // expect org.apache.xpath.jaxp.XPathFactoryImpl
	        
	        // See http://www.docx4java.org/forums/data-binding-java-f16/opendope-xpath-performance-t1696.html
	        // System.setProperty(DTM_MANAGER_PROP_NAME, DTM_MANAGER_CLASS_NAME);  
		}
		return xPathFactory;
		
	}
	
	public static XPath newXPath() {
		
		getXPathFactory();
		
		synchronized(xPathFactory) {
			return xPathFactory.newXPath();
		}
	}
	

}
