package org.docx4j.openpackaging.parts;

import java.util.List;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.w3c.dom.Node;

/**
 * There are two types of these:
 * - JaxbCustomXmlDataStoragePart<E>
 * - CustomXmlDataStoragePart
 * 
 * This interface doesn't provide getData,
 * because the 2 types are quite different.
 * 
 * But it could allow storeItemId to be
 * get or set.
 * 
 * @author jharrop
 *
 */
public interface CustomXmlPart {
	
	public String xpathGetString(String xpath, String prefixMappings) throws Docx4JException;	

	public List<Node> xpathGetNodes(String xpathString, String prefixMappings)  throws Docx4JException;

	/**
	 * @param xpath
	 * @param prefixMappings
	 * @param value
	 * @throws Docx4JException
	 * @since 3.0.1
	 */
	public boolean setNodeValueAtXPath(String xpath, String value, String prefixMappings) throws Docx4JException;	
	
	/**
	 * Get the XML as a String.
	 * @throws Docx4JException 
	 * 
	 * @since 3.0.1
	 */
	public String getXML() throws Docx4JException;
	
	/**
	 * @since 3.0.2
	 */
	public String getItemId();
}
