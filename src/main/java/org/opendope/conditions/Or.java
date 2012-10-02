
package org.opendope.conditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded">
 *           &lt;element ref="{http://opendope.org/conditions}xpathref" minOccurs="0"/>
 *           &lt;element ref="{http://opendope.org/conditions}and" minOccurs="0"/>
 *           &lt;element ref="{http://opendope.org/conditions}or" minOccurs="0"/>
 *           &lt;element ref="{http://opendope.org/conditions}not" minOccurs="0"/>
 *           &lt;element ref="{http://opendope.org/conditions}conditionref" minOccurs="0"/>
 *           &lt;element ref="{http://opendope.org/conditions}true" minOccurs="0"/>
 *           &lt;element ref="{http://opendope.org/conditions}false" minOccurs="0"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "xpathrefOrAndOrOr"
})
@XmlRootElement(name = "or")
public class Or implements Evaluable {

    @XmlElementRefs({
        @XmlElementRef(name = "and", namespace = "http://opendope.org/conditions", type = And.class),
        @XmlElementRef(name = "xpathref", namespace = "http://opendope.org/conditions", type = Xpathref.class),
        @XmlElementRef(name = "not", namespace = "http://opendope.org/conditions", type = Not.class),
        @XmlElementRef(name = "or", namespace = "http://opendope.org/conditions", type = Or.class),
        @XmlElementRef(name = "conditionref", namespace = "http://opendope.org/conditions", type = Conditionref.class)
    })
    protected List<Evaluable> xpathrefOrAndOrOr;

    /**
     * Gets the value of the xpathrefOrAndOrOr property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the xpathrefOrAndOrOr property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getXpathrefOrAndOrOr().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link And }
     * {@link Xpathref }
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link Not }
     * {@link Or }
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link Conditionref }
     * 
     * 
     */
    public List<Evaluable> getXpathrefOrAndOrOr() {
        if (xpathrefOrAndOrOr == null) {
            xpathrefOrAndOrOr = new ArrayList<Evaluable>();
        }
        return this.xpathrefOrAndOrOr;
    }
    
	public boolean evaluate(WordprocessingMLPackage pkg, 
			Map<String, CustomXmlDataStoragePart> customXmlDataStorageParts,
			Conditions conditions,
			org.opendope.xpaths.Xpaths xPaths) {
    	
    	for (Evaluable particle : xpathrefOrAndOrOr) {
    		
        	boolean result = particle.evaluate(pkg, customXmlDataStorageParts, conditions, xPaths);
        	if (result==true) {
        		return true;
        	}    		
    	}
    	return false;
    }
    
	public void listXPaths( List<org.opendope.xpaths.Xpaths.Xpath> theList, 
			Conditions conditions,
			org.opendope.xpaths.Xpaths xPaths) {
		
    	for (Evaluable particle : xpathrefOrAndOrOr) {
    		particle.listXPaths(theList, conditions, xPaths);
    	}
		
	}

	public String toString(Conditions conditions,
			org.opendope.xpaths.Xpaths xPaths) {
		
		StringBuilder sb = new StringBuilder();
		
		int i = 0;
		int total = xpathrefOrAndOrOr.size();
    	for (Evaluable particle : xpathrefOrAndOrOr) {
    		sb.append(particle.toString(conditions, xPaths));
    		i++;
    		if (i<total) {
    			sb.append(" or ");
    		}
    	}
		
		return "(" + sb.toString() + ")";
	}
	
	public Condition repeat(String xpathBase,
			int index,
			Conditions conditions,
			org.opendope.xpaths.Xpaths xPaths)	{

    	for (Evaluable particle : xpathrefOrAndOrOr) {
    		particle.repeat(xpathBase, index, conditions, xPaths);
    	}
		return null;
	}	
	
}
