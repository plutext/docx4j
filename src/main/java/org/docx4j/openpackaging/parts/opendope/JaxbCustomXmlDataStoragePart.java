package org.docx4j.openpackaging.parts.opendope;
// It would be better if this class was in org.docx4j.openpackaging.parts,
// but it is too late to move it now 

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.NamespacePrefixMappings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.w3c.dom.Document;
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
	private static XPathFactory xPathFactory;
	private static XPath xPath;

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
	
	static {
		xPathFactory = XPathFactory.newInstance();
		xPath = xPathFactory.newXPath();		
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
			getNamespaceContext().registerPrefixMappings(prefixMappings);
			
			String result = xPath.evaluate(xpathString, doc );
			log.debug(xpathString + " ---> " + result);
			return result;
		} catch (Exception e) {
			throw new Docx4JException("Problems evaluating xpath '" + xpathString + "'", e);
		}
	}
	
	public List<Node> xpathGetNodes(String xpathString, String prefixMappings)  throws Docx4JException {

		if (doc==null) {
			readyXPath();
			//throw new Docx4JException("You must call readyXPath() once before doing XPath stuff");
		}
		
		getNamespaceContext().registerPrefixMappings(prefixMappings);
		
		return XmlUtils.xpath(doc, xpathString, 
				getNamespaceContext() );
		
	}

}
