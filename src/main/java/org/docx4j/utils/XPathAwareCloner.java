package org.docx4j.utils;

import java.util.List;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.JaxbValidationEventHandler;
import org.docx4j.jaxb.XPathBinderAssociationIsPartialException;
import org.docx4j.wml.P;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * Some users wish to be able to use an XPath on the result
 * of cloning a JAXB object.  This variant on XmlUtils.deepCopy 
 * allows that.
 * 
 * Note that if you use this object's deepCopy method more 
 * than once, the results returned by getJAXBNodesViaXPath
 * will only be on your last deepCopy.
 */
public class XPathAwareCloner {
	
	private static Logger log = LoggerFactory.getLogger(XPathAwareCloner.class);	
			
	/** Clone this JAXB object, using default JAXBContext. */ 
	public Object deepCopy(Object o) {		
		return deepCopy(o, Context.jc);		
	}	
	
	Object jaxbElement;
	
	/** Clone this JAXB object
	 * @param value
	 * @param jc
	 * @return
	 */
	public Object deepCopy(Object o, JAXBContext jc) {
		
		if (o==null) {
			throw new IllegalArgumentException("Can't clone a null argument");
		}
		
		try {
			// To be XPath aware, we need a binder.
			// But to unmarshall using a binder, we need to unmarshal a node.
			// So, our marshall should be to a W3C document
			org.w3c.dom.Document doc = XmlUtils.marshaltoW3CDomDocument(o, jc);
			
			// OK, unmarshall to binder
			binder = jc.createBinder();
			JaxbValidationEventHandler eventHandler = new JaxbValidationEventHandler();
			eventHandler.setContinue(false);
			binder.setEventHandler(eventHandler);
			jaxbElement =  binder.unmarshal( doc);
			
			//log.debug("Clone: " + XmlUtils.marshaltoString(jaxbElement, true, true));
			
			return jaxbElement;
		} catch (JAXBException ex) {
			throw new IllegalArgumentException(ex);
		}
	}
	

	private Binder<Node> binder;
	
	
	/**
	 * Enables synchronization between XML infoset nodes and JAXB objects 
	 * representing same XML document.
	 * 
	 * An instance of this class maintains the association between XML nodes
	 * of an infoset preserving view and a JAXB representation of an XML document. 
	 * Navigation between the two views is provided by the methods 
	 * getXMLNode(Object) and getJAXBNode(Object) .
	 * 
	 * In theory, modifications can be made to either the infoset preserving view or 
	 * the JAXB representation of the document while the other view remains
	 * unmodified. The binder ought to be able to synchronize the changes made in
	 * the modified view back into the other view using the appropriate
	 * Binder update methods, #updateXML(Object, Object) or #updateJAXB(Object).
	 * 
	 * But JAXB doesn't currently work as advertised .. access to this
	 * object is offered for advanced users on an experimental basis only.
	 */
	public Binder<Node> getBinder() {
		
		return binder;
	}
	
	/**
	 * Fetch JAXB Nodes matching an XPath (for example "//w:p").
	 * 
	 * If you have modified your JAXB objects (eg added or changed a 
	 * w:p paragraph), you need to update the association. The problem
	 * is that this can only be done ONCE, owing to a bug in JAXB:
	 * see https://jaxb.dev.java.net/issues/show_bug.cgi?id=459
	 * 
	 * So this is left for you to choose to do via the refreshXmlFirst parameter.   
	 * 
	 * @param xpathExpr
	 * @param refreshXmlFirst
	 * @return
	 * @throws JAXBException
	 * @throws XPathBinderAssociationIsPartialException 
	 */	
	public List<Object> getJAXBNodesViaXPath(String xpathExpr, boolean refreshXmlFirst) 
			throws JAXBException, XPathBinderAssociationIsPartialException {
		
		return XmlUtils.getJAXBNodesViaXPath(binder, jaxbElement, xpathExpr, refreshXmlFirst);
	}	
	
	/**
	 * @param args
	 * @throws JAXBException 
	 * @throws XPathBinderAssociationIsPartialException 
	 */
	public static void main(String[] args) throws JAXBException, XPathBinderAssociationIsPartialException {
		
	    String pString = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
	    	      +"<w:r>"
	    	        +"<w:t xml:space=\"preserve\">Here is some text.</w:t>"
	    	      +"</w:r>"
	    	      +"<w:r>"
	    	        +"<w:rPr>"
	    	          +"<w:i/>"
	    	        +"</w:rPr>"
	    	        +"<w:t>An italic run.</w:t>"
	    	      +"</w:r>"
	    	      +"<w:r>"
	    	        +"<w:rPr>"
	    	          +"<w:i/>"
	    	        +"</w:rPr>"
	    	        +"<w:t xml:space=\"preserve\">"  +"</w:t>"
	    	      +"</w:r>"
	    	      +"<w:r>"
	    	        +"<w:t>More stuff.</w:t>"
	    	      +"</w:r>"
	    	      +"<w:r>"
	    	        +"<w:t xml:space=\"preserve\">"  +"</w:t>"
	    	      +"</w:r>"
	    	      +"<w:r>"
	    	        +"<w:rPr>"
	    	          +"<w:b/>"
	    	        +"</w:rPr>"
	    	        +"<w:t>More stuff.</w:t>"
	    	      +"</w:r>"
	    	      +"<w:r>"
	    	        +"<w:t xml:space=\"preserve\">" +"</w:t>"
	    	      +"</w:r>"
	    	      +"<w:r>"
	    	        +"<w:t xml:space=\"preserve\">The run we are seeking.</w:t>"
	    	      +"</w:r>"
	    	      +"<w:r>"
	    	        +"<w:rPr>"
	    	          +"<w:b/>"
	    	        +"</w:rPr>"
	    	        +"<w:t>More stuff.</w:t>"
	    	      +"</w:r>"
	    	      +"<w:r>"
	    	        +"<w:t xml:space=\"preserve\">" +"</w:t>"
	    	      +"</w:r>"
	    	      +"<w:r>"
	    	        +"<w:t>More stuff.</w:t>"
	    	      +"</w:r>"
	    	    +"</w:p>";
	    
	    P pIn = (P)XmlUtils.unmarshalString(pString);
	    
	    XPathAwareCloner cloner = new XPathAwareCloner();
	    P clonedP = (P)cloner.deepCopy(pIn);
	    
	    List<Object> results = cloner.getJAXBNodesViaXPath("//w:r[contains( w:t, 'seeking')]", false);
	    //List<Object> results = cloner.getJAXBNodesViaXPath("//w:r", false);
	    
	    int i=1;
	    for (Object result: results) {
	    	System.out.println("\n\r" + i + ": " + XmlUtils.marshaltoString(result, true, true));
	    	i++;
	    }
		

	}

}
