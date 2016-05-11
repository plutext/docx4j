package org.docx4j.openpackaging.parts.opendope;

import javax.xml.bind.JAXBContext;

import org.docx4j.model.datastorage.InputIntegrityException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.PartName;
import org.opendope.xpaths.Xpaths.Xpath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XPathsPart extends JaxbCustomXmlDataStoragePart<org.opendope.xpaths.Xpaths> {
	
	private static Logger log = LoggerFactory.getLogger(XPathsPart.class);		
	
	public XPathsPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public XPathsPart(PartName partName, JAXBContext jc) throws InvalidFormatException {
		super(partName, jc);
		init();
	}
	
	/**
	 * For performance reasons, avoid using this if you have a lot of xpaths
	 * @param id
	 * @return
	 * @since 3.0.0
	 */
	public  Xpath getXPathById(String id) {
		
		return getXPathById(this.getJaxbElement(), id);
	}
	
	/**
	 * For performance reasons, avoid using this if you have a lot of xpaths
	 * @param xpaths
	 * @param id
	 * @return
	 */
	public static Xpath getXPathById(org.opendope.xpaths.Xpaths xpaths, String id) {
		
		for (Xpath x : xpaths.getXpath() ) {
			
			if (x.getId().equals(id))
				return x;
		}
		throw new InputIntegrityException("Couldn't find xpath " + id );		
	}

	/**
	 * @param id
	 * @return
	 * @since 3.0.0
	 */
	public  Xpath getXPathByQuestionId(String id) {

		return getXPathByQuestionId(this.getJaxbElement(), id);
		
	}
	
	public static Xpath getXPathByQuestionId(org.opendope.xpaths.Xpaths xpaths, String id) {
		
		for (Xpath x : xpaths.getXpath() ) {
			
			if (x.getQuestionID()!=null
					&& x.getQuestionID().equals(id))
				return x;
		}
		throw new InputIntegrityException("No XPath with question id " + id );		
	}

}
