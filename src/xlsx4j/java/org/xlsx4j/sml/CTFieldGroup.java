
package org.xlsx4j.sml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_FieldGroup complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_FieldGroup">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="rangePr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_RangePr" minOccurs="0"/>
 *         &lt;element name="discretePr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_DiscretePr" minOccurs="0"/>
 *         &lt;element name="groupItems" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_GroupItems" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="par" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="base" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_FieldGroup", propOrder = {
    "rangePr",
    "discretePr",
    "groupItems"
})
public class CTFieldGroup implements Child
{

    protected CTRangePr rangePr;
    protected CTDiscretePr discretePr;
    protected CTGroupItems groupItems;
    @XmlAttribute(name = "par")
    @XmlSchemaType(name = "unsignedInt")
    protected Long par;
    @XmlAttribute(name = "base")
    @XmlSchemaType(name = "unsignedInt")
    protected Long base;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the rangePr property.
     * 
     * @return
     *     possible object is
     *     {@link CTRangePr }
     *     
     */
    public CTRangePr getRangePr() {
        return rangePr;
    }

    /**
     * Sets the value of the rangePr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRangePr }
     *     
     */
    public void setRangePr(CTRangePr value) {
        this.rangePr = value;
    }

    /**
     * Gets the value of the discretePr property.
     * 
     * @return
     *     possible object is
     *     {@link CTDiscretePr }
     *     
     */
    public CTDiscretePr getDiscretePr() {
        return discretePr;
    }

    /**
     * Sets the value of the discretePr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDiscretePr }
     *     
     */
    public void setDiscretePr(CTDiscretePr value) {
        this.discretePr = value;
    }

    /**
     * Gets the value of the groupItems property.
     * 
     * @return
     *     possible object is
     *     {@link CTGroupItems }
     *     
     */
    public CTGroupItems getGroupItems() {
        return groupItems;
    }

    /**
     * Sets the value of the groupItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGroupItems }
     *     
     */
    public void setGroupItems(CTGroupItems value) {
        this.groupItems = value;
    }

    /**
     * Gets the value of the par property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getPar() {
        return par;
    }

    /**
     * Sets the value of the par property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setPar(Long value) {
        this.par = value;
    }

    /**
     * Gets the value of the base property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getBase() {
        return base;
    }

    /**
     * Sets the value of the base property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setBase(Long value) {
        this.base = value;
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
