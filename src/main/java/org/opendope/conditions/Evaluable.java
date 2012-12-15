package org.opendope.conditions;

import java.util.List;
import java.util.Map;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlPart;

public interface Evaluable {

	public boolean evaluate(WordprocessingMLPackage pkg, 
			Map<String, CustomXmlPart> customXmlDataStorageParts,
			Conditions conditions,
			org.opendope.xpaths.Xpaths xPaths);
	
	/**
	 * List the XPaths used in this condition.
	 * @param theList
	 * @param conditions
	 * @param xPaths
	 */
	public void listXPaths( List<org.opendope.xpaths.Xpaths.Xpath> theList, 
			Conditions conditions,
			org.opendope.xpaths.Xpaths xPaths);
	
	/**
	 * Build the XPath expression
	 * @param conditions
	 * @param xPaths
	 * @return
	 */
	public String toString(Conditions conditions,
			org.opendope.xpaths.Xpaths xPaths);
	
	/**
	 * Create a condition for this instance of a repeat.
	 * 
	 * @param xpathBase
	 * @param index
	 * @param conditions
	 * @param xPaths
	 * @return
	 */
	public Condition repeat(String xpathBase,
			int index,
			Conditions conditions,
			org.opendope.xpaths.Xpaths xPaths);
}
