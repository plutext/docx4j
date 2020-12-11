package org.docx4j.openpackaging.parts;

import java.util.List;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.w3c.dom.Document;
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

	
	/**
	 * Use org.apache.xpath.CachedXPathAPI, since this is much quicker than default javax.xml.xpath.XPath implementations
	 * (Apache's anyway) for large XML files.

	 * This is because Apache's old XPathAPI class instantiated a new XPathContext 
	 * (and thus building a new DTMManager, and new DTMs) each time it was called. 
	 * XPathAPIObject instead retains its context as long as the object persists, 
	 * reusing the DTMs. 
	 * 
	 * There was the question of whether to declare it here or just implement it in CustomXmlDataStoragePart
	 * (since this is mostly of value in binding the user's XML data file).  It is here since otherwise
	 * BindingHandler would fail on DocPropsCoverPagePart and StandardisedAnswersPart (both of which 
	 * extend JaxbCustomXmlDataStoragePart).
	 * 
	 * @see discardCacheXPathObject
	 * 
	 * @param xpath
	 * @param prefixMappings
	 * @return
	 * @throws Docx4JException
	 * @since 3.3.1
	 */
	public String cachedXPathGetString(String xpath, String prefixMappings) throws Docx4JException;
	
	/**
	 * Use this to null out the org.apache.xpath.CachedXPathAPI object, which you should do
	 * if you've changed your source document, or want to reclaim memory.
	 * @since 3.3.1
	 */
	public void discardCacheXPathObject();
	
	
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
	 * Set the XML data
	 * @throws Docx4JException 
	 * 
	 * @since 6.0.0
	 */
	public void setXML(Document xmlDocument) throws Docx4JException;
	
	/**
	 * @since 3.0.2
	 */
	public String getItemId();
	
	
}
