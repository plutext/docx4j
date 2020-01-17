
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2012.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PresetTransition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PresetTransition"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="prst" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="invX" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="invY" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PresetTransition")
public class CTPresetTransition implements Child
{

    @XmlAttribute(name = "prst")
    protected String prst;
    @XmlAttribute(name = "invX")
    protected Boolean invX;
    @XmlAttribute(name = "invY")
    protected Boolean invY;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the prst property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrst() {
        return prst;
    }

    /**
     * Sets the value of the prst property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrst(String value) {
        this.prst = value;
    }

    /**
     * Gets the value of the invX property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isInvX() {
        if (invX == null) {
            return false;
        } else {
            return invX;
        }
    }

    /**
     * Sets the value of the invX property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInvX(Boolean value) {
        this.invX = value;
    }

    /**
     * Gets the value of the invY property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isInvY() {
        if (invY == null) {
            return false;
        } else {
            return invY;
        }
    }

    /**
     * Sets the value of the invY property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInvY(Boolean value) {
        this.invY = value;
    }

    /**
     * Gets the parent object in the object tree representing the unmarshalled xml document.
     * 
     * @return
     *     The parent object.
     */
    public Object getParent() {
        return this.parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    /**
     * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
     * 
     * @param parent
     *     The parent object in the object tree.
     * @param unmarshaller
     *     The unmarshaller that generated the instance.
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        setParent(parent);
    }

}
