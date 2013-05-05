package org.docx4j.jaxb;

import org.w3c.dom.Node;

/**
 * Represent an association between a DOM node and a JAXB object.
 * 
 * Note, from the javadoc, that this association is partial; not all XML elements have associated JAXB objects,
 * and not all JAXB objects have associated XML elements.
 * 
 * This happens (both Sun/Oracle, and MOXy) for an element implemented as a xsd:simpleType        		
 * eg /s:worksheet[1]/s:sheetData[1]/s:row[1]/s:c[1]/s:v[1]
 * <xsd:element name="v" type="s:ST_Xstring" minOccurs="0" maxOccurs="1"/>
 * which becomes a String field in some object 
 *         		
 * Also for attributes, I'd guess.
 * 
 * Hence this design.
 * 
 * @author jharrop
 *
 */
public class JAXBAssociation {

	/**
	 * @return the domNode
	 */
	public Node getDomNode() {
		return domNode;
	}
	Node domNode;

	/**
	 * @return the jaxbObject
	 */
	public Object getJaxbObject() {
		return jaxbObject;
	}
	Object jaxbObject;
	
	public JAXBAssociation(Node n, Object o) {
		this.domNode = n;
		this.jaxbObject = o;
	}
	
}
