package org.docx4j.openpackaging.parts.opendope;
// It would be better if this class was in org.docx4j.openpackaging.parts,
// but it is too late to move it now 

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.NamespacePrefixMappings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.utils.XPathFactoryUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class JaxbCustomXmlDataStoragePart<E> extends JaxbXmlPart<E> implements CustomXmlPart {
	// I considered extending JaxbXmlPartXPathAware,
	// but that only gives us List<Object> getJAXBNodesViaXPath
	// where what we (currently) want is String or list of nodes.
	// Besides, XPath doesn't really work with RI (it may with moXy?).
	// The XPath methods here are copied from XmlPart.
	
	
	
	public JaxbCustomXmlDataStoragePart(PartName partName) throws InvalidFormatException {
		super(partName);
		init();
	}

	public JaxbCustomXmlDataStoragePart(PartName partName, JAXBContext jc) throws InvalidFormatException {
		super(partName);
		setJAXBContext(jc);		
		init();
	}
	
	public void init() {		
		
		// Used if this Part is added to [Content_Types].xml 
		setContentType(new  org.docx4j.openpackaging.contenttype.ContentType( 
				org.docx4j.openpackaging.contenttype.ContentTypes.OFFICEDOCUMENT_CUSTOMXML_DATASTORAGE));

		// Used when this Part is added to a rels 
		setRelationshipType(Namespaces.CUSTOM_XML_DATA_STORAGE);
		
	}
	
	/**
	 * This part's XML contents.  Not guaranteed to be up to date.
	 * Whether it is or not will depend on how the class which extends
	 * Part chooses to treat it.  It may be that the class uses some
	 * other internal representation for its data. 
	 */
	protected Document doc;
	
	
	private static XPath xPath = XPathFactoryUtil.newXPath();

	/**
	 * XPaths are evaluated against a DOM document representation
	 * of the JAXBElement.  These aren't kept in sync, so you can
	 * use this method to update the DOM document from the JAXBElement.
	 */
	public void readyXPath() {
		
		doc = XmlUtils.marshaltoW3CDomDocument(getJaxbElement(), getJAXBContext());
	}
	
	public void updateJaxbElementFromDocument() {
		
		try {
			this.unmarshal(doc.getDocumentElement() );
		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	
	private NamespacePrefixMappings nsContext;
	private NamespacePrefixMappings getNamespaceContext() {
		if (nsContext==null) {
			nsContext = new NamespacePrefixMappings();
			xPath.setNamespaceContext(nsContext);
		}
		return nsContext;
	}
	
	public String xpathGetString(String xpathString, String prefixMappings)  throws Docx4JException {
		
		if (doc==null) {
			readyXPath();
			//throw new Docx4JException("You must call readyXPath() once before doing XPath stuff");
		}
		try {
			String result;
			synchronized(xPath) {
				getNamespaceContext().registerPrefixMappings(prefixMappings);
				result = xPath.evaluate(xpathString, doc );
			}
			log.debug(xpathString + " ---> " + result);
			return result;
		} catch (Exception e) {
			throw new Docx4JException("Problems evaluating xpath '" + xpathString + "'", e);
		}
	}
	
	
	/* cachedXPath falls through to xpathGetString (ie no caching is done in this class)
	 * (non-Javadoc)
	 * @see org.docx4j.openpackaging.parts.CustomXmlPart#cachedXPathGetString(java.lang.String, java.lang.String)
	 */
	public String cachedXPathGetString(String xpath, String prefixMappings) throws Docx4JException {

		if (log.isDebugEnabled()) {
			log.debug("cachedXPath not implemented for " + this.getClass().getName() + " ; falling back to xpathGetString");
		}
		return xpathGetString( xpath,  prefixMappings);
		
	}
	
	public void discardCacheXPathObject() {}	
	
	public List<Node> xpathGetNodes(String xpathString, String prefixMappings)  throws Docx4JException {

		if (doc==null) {
			readyXPath();
			//throw new Docx4JException("You must call readyXPath() once before doing XPath stuff");
		}
		
		synchronized(xPath) {
			getNamespaceContext().registerPrefixMappings(prefixMappings);
			return XmlUtils.xpath(doc, xpathString, 
					getNamespaceContext() );
		}
		
	}

	@Override
	public boolean setNodeValueAtXPath(String xpath, String value, String prefixMappings) throws Docx4JException {

		if (doc==null) {
			readyXPath();
			//throw new Docx4JException("You must call readyXPath() once before doing XPath stuff");
		}
		try {
			Node node;
			synchronized(xPath) {
				getNamespaceContext().registerPrefixMappings(prefixMappings);
				node = (Node)xPath.evaluate(xpath, doc,  XPathConstants.NODE );
				
//				System.out.println(node.getClass().getName());
				// com.sun.org.apache.xerces.internal.dom.ElementNSImpl
				if (node instanceof Element) {
					// ((Element)node).setNodeValue(nodeValue);
					((Element)node).setTextContent(value);
				} else {
					throw new Docx4JException("Expected element, but got " + node.getClass().getName() );
				}
				return true;
				
			}
		} catch (Docx4JException e) {
			throw e;
		} catch (Exception e) {
			throw new Docx4JException("Problems evaluating xpath '" + xpath + "'", e);
		}
		
	}
	
	/**
	 * @since 3.0.2
	 */
	public String getItemId() {
		
		if (this.getRelationshipsPart()==null) { 
			return null; 
		} else {
			// Look in its rels for rel of @Type customXmlProps (eg @Target="itemProps1.xml")
			Relationship r = this.getRelationshipsPart().getRelationshipByType(
					Namespaces.CUSTOM_XML_DATA_STORAGE_PROPERTIES);
			if (r==null) {
				log.warn(".. but that doesn't point to a  customXmlProps part");
				return null;
			}
			CustomXmlDataStoragePropertiesPart customXmlProps = 
				(CustomXmlDataStoragePropertiesPart)this.getRelationshipsPart().getPart(r);
			if (customXmlProps==null) {
				log.warn(".. but the target seems to be missing?");
				return null;
			} else {
				return customXmlProps.getItemId().toLowerCase();
			}
		}
	}	
	
    /**
     * Remove this part from the pkg. Beware: it is up to you to make sure
     * your content doesn't rely on this part being present!  A symptom of
     * that would be that Office now reports your file to be corrupt or in 
     * need of repair.   
     * 
     * @since 3.0.2
     */
	@Override	
    public void remove() {
		
		String itemId = this.getItemId(); 
		if (itemId!=null) {
			log.debug("removing from CustomXmlDataStorageParts " + itemId);
			this.getPackage().getCustomXmlDataStorageParts().remove(itemId);
		}		
		
		super.remove();
    	
    }	
	
	@Override
	public void setXML(Document xmlDocument) throws Docx4JException {

		try {
			this.setJaxbElement(
					(E)XmlUtils.unmarshal(xmlDocument));
		} catch (JAXBException e) {
			throw new Docx4JException("Problem unmarshalling, check content", e);
		}
		
	}
	
}
