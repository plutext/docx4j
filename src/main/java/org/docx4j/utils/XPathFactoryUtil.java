package org.docx4j.utils;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XPathFactoryUtil {
	
	private static Logger log = LoggerFactory.getLogger(XPathFactoryUtil.class);		
	
	private static XPathFactory xPathFactory;
	
	public static synchronized XPathFactory getXPathFactory() {
		
		if (xPathFactory==null) {
			xPathFactory = XPathFactory.newInstance();
	        log.info("xpath implementation: " + xPathFactory.getClass().getName());
		}
		return xPathFactory;
		
	}
	
	public static XPath newXPath() {
		
		getXPathFactory();
		
		synchronized(xPathFactory) {
			return xPathFactory.newXPath();
		}
		
	}
	
	
//	log.info("Using XPathFactory: " + XPathFactory.DEFAULT_PROPERTY_NAME + ": " 
//	+ System.getProperty(XPathFactory.DEFAULT_PROPERTY_NAME));    
//System.setProperty(XPathFactory.DEFAULT_PROPERTY_NAME, 
//	"org.apache.xpath.jaxp.XPathFactoryImpl");
// com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl

	

}
