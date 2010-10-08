
package org.opendope.conditions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;choice>
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
    "xpath",
    "and",
    "or",
    "not",
    "conditionref",
    "_true",
    "_false"
})
@XmlRootElement(name = "not")
public class Not {

    protected Xpath xpath;
    protected And and;
    protected Or or;
    protected Not not;
    protected Conditionref conditionref;
    @XmlElement(name = "true")
    protected Object _true;
    @XmlElement(name = "false")
    protected Object _false;

    /**
     * Gets the value of the xpath property.
     * 
     * @return
     *     possible object is
     *     {@link Xpath }
     *     
     */
    public Xpath getXpath() {
        return xpath;
    }

    /**
     * Sets the value of the xpath property.
     * 
     * @param value
     *     allowed object is
     *     {@link Xpath }
     *     
     */
    public void setXpath(Xpath value) {
        this.xpath = value;
    }

    /**
     * Gets the value of the and property.
     * 
     * @return
     *     possible object is
     *     {@link And }
     *     
     */
    public And getAnd() {
        return and;
    }

    /**
     * Sets the value of the and property.
     * 
     * @param value
     *     allowed object is
     *     {@link And }
     *     
     */
    public void setAnd(And value) {
        this.and = value;
    }

    /**
     * Gets the value of the or property.
     * 
     * @return
     *     possible object is
     *     {@link Or }
     *     
     */
    public Or getOr() {
        return or;
    }

    /**
     * Sets the value of the or property.
     * 
     * @param value
     *     allowed object is
     *     {@link Or }
     *     
     */
    public void setOr(Or value) {
        this.or = value;
    }

    /**
     * Gets the value of the not property.
     * 
     * @return
     *     possible object is
     *     {@link Not }
     *     
     */
    public Not getNot() {
        return not;
    }

    /**
     * Sets the value of the not property.
     * 
     * @param value
     *     allowed object is
     *     {@link Not }
     *     
     */
    public void setNot(Not value) {
        this.not = value;
    }

    /**
     * Gets the value of the conditionref property.
     * 
     * @return
     *     possible object is
     *     {@link Conditionref }
     *     
     */
    public Conditionref getConditionref() {
        return conditionref;
    }

    /**
     * Sets the value of the conditionref property.
     * 
     * @param value
     *     allowed object is
     *     {@link Conditionref }
     *     
     */
    public void setConditionref(Conditionref value) {
        this.conditionref = value;
    }

    /**
     * Gets the value of the true property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getTrue() {
        return _true;
    }

    /**
     * Sets the value of the true property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setTrue(Object value) {
        this._true = value;
    }

    /**
     * Gets the value of the false property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getFalse() {
        return _false;
    }

    /**
     * Sets the value of the false property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setFalse(Object value) {
        this._false = value;
    }

}
