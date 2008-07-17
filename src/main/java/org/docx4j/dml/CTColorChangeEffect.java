
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ColorChangeEffect complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ColorChangeEffect">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="clrFrom" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color"/>
 *         &lt;element name="clrTo" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color"/>
 *       &lt;/sequence>
 *       &lt;attribute name="useA" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ColorChangeEffect", propOrder = {
    "clrFrom",
    "clrTo"
})
public class CTColorChangeEffect implements Child
{

    @XmlElement(required = true)
    protected CTColor clrFrom;
    @XmlElement(required = true)
    protected CTColor clrTo;
    @XmlAttribute
    protected Boolean useA;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the clrFrom property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getClrFrom() {
        return clrFrom;
    }

    /**
     * Sets the value of the clrFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setClrFrom(CTColor value) {
        this.clrFrom = value;
    }

    /**
     * Gets the value of the clrTo property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getClrTo() {
        return clrTo;
    }

    /**
     * Sets the value of the clrTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setClrTo(CTColor value) {
        this.clrTo = value;
    }

    /**
     * Gets the value of the useA property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUseA() {
        if (useA == null) {
            return true;
        } else {
            return useA;
        }
    }

    /**
     * Sets the value of the useA property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUseA(Boolean value) {
        this.useA = value;
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
