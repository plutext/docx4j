
package org.opendope.conditions;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *           &lt;element ref="{http://opendope.org/conditions}xpath" minOccurs="0"/>
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
    "xpathOrAndOrOr"
})
@XmlRootElement(name = "and")
public class And {

    @XmlElementRefs({
        @XmlElementRef(name = "and", namespace = "http://opendope.org/conditions", type = And.class),
        @XmlElementRef(name = "xpath", namespace = "http://opendope.org/conditions", type = Xpath.class),
        @XmlElementRef(name = "false", namespace = "http://opendope.org/conditions", type = JAXBElement.class),
        @XmlElementRef(name = "not", namespace = "http://opendope.org/conditions", type = Not.class),
        @XmlElementRef(name = "or", namespace = "http://opendope.org/conditions", type = Or.class),
        @XmlElementRef(name = "true", namespace = "http://opendope.org/conditions", type = JAXBElement.class),
        @XmlElementRef(name = "conditionref", namespace = "http://opendope.org/conditions", type = Conditionref.class)
    })
    protected List<Object> xpathOrAndOrOr;

    /**
     * Gets the value of the xpathOrAndOrOr property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the xpathOrAndOrOr property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getXpathOrAndOrOr().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link And }
     * {@link Xpath }
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link Not }
     * {@link Or }
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * {@link Conditionref }
     * 
     * 
     */
    public List<Object> getXpathOrAndOrOr() {
        if (xpathOrAndOrOr == null) {
            xpathOrAndOrOr = new ArrayList<Object>();
        }
        return this.xpathOrAndOrOr;
    }

}
