package org.docx4j.openpackaging.parts.opendope;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.model.datastorage.InputIntegrityException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.PartName;
import org.opendope.conditions.Condition;

public class ConditionsPart extends JaxbCustomXmlDataStoragePart<org.opendope.conditions.Conditions> {
	
	private static Logger log = LoggerFactory.getLogger(ConditionsPart.class);		
	
	public ConditionsPart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public ConditionsPart(PartName partName, JAXBContext jc) throws InvalidFormatException {
		super(partName, jc);
		init();
	}
	
	public static Condition getConditionById(
			org.opendope.conditions.Conditions conditions,
			String id) {
		
		for (Condition c : conditions.getCondition() ) {
			
			if (c.getId().equals(id))
				return c;
		}
		
		throw new InputIntegrityException("No Condition with id " + id );		
	}

//	/**
//	 * Extracts xpath object from condition.  
//	 * 
//	 * Note: this does NOT build an xpath corresponding to the condition
//	 * value, but rather, is typically used to identify
//	 * questions associated with the condition.
//	 * @param c
//	 * @param xPaths
//	 * @return
//	 * @throws InputIntegrityException
//	 */
//	public static List<org.opendope.xpaths.Xpaths.Xpath> getXPathsUsedInCondition(Condition c, org.opendope.xpaths.Xpaths xPaths) 
//		throws InputIntegrityException {
//		
//		List<org.opendope.xpaths.Xpaths.Xpath> xpaths = new ArrayList<org.opendope.xpaths.Xpaths.Xpath>(); 
//		
//		if (c.getXpathref()!=null) {
//			xpaths.add(XPathsPart.getXPathById(xPaths, c.getXpathref().getId())); 	
//			return xpaths;
//		} else if (c.getNot()!=null) {
//			
//			if (c.getNot().getXpathref()!=null) {
//				xpaths.add(XPathsPart.getXPathById(xPaths, c.getNot().getXpathref().getId())); 	
//				return xpaths;
//			} else {
//				throw new InputIntegrityException("Couldn't extract xpath from not in condition " + XmlUtils.marshaltoString(c, true) );							
//			}
//		}
//		throw new InputIntegrityException("Couldn't extract xpath from condition " + XmlUtils.marshaltoString(c, true) );
//		// TODO handle complex conditions
//	}	
//	
//	public static String buildEffectiveXPath(Condition c, org.opendope.xpaths.Xpaths xPaths) 
//			throws InputIntegrityException {
//			
//			//StringBuilder effectiveXPath = new StringBuilder(); 
//			
//			if (c.getXpathref()!=null) {
//				return XPathsPart.getXPathById(xPaths, c.getXpathref().getId()).getDataBinding().getXpath(); 	
//			} else if (c.getNot()!=null) {
//				
//				if (c.getNot().getXpathref()!=null) {
//					return "not(" + XPathsPart.getXPathById(xPaths, c.getNot().getXpathref().getId()).getDataBinding().getXpath()
//								+ ")";
//				} else {
//					throw new InputIntegrityException("Couldn't extract xpath from not in condition " + XmlUtils.marshaltoString(c, true) );							
//				}
//			}
//			throw new InputIntegrityException("Couldn't extract xpath from condition " + XmlUtils.marshaltoString(c, true) );
//			// TODO handle complex conditions
//		}	
	
}
