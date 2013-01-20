
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
 * <p>Java class for CT_Control complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Control">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="controlPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ControlPr" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="shapeId" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}id use="required""/>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Control", propOrder = {
    "controlPr"
})
public class CTControl implements Child
{

    protected CTControlPr controlPr;
    @XmlAttribute(name = "shapeId", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long shapeId;
    @XmlAttribute(name = "id", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships", required = true)
    protected String id;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the controlPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTControlPr }
     *     
     */
    public CTControlPr getControlPr() {
        return controlPr;
    }

    /**
     * Sets the value of the controlPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTControlPr }
     *     
     */
    public void setControlPr(CTControlPr value) {
        this.controlPr = value;
    }

    /**
     * Gets the value of the shapeId property.
     * 
     */
    public long getShapeId() {
        return shapeId;
    }

    /**
     * Sets the value of the shapeId property.
     * 
     */
    public void setShapeId(long value) {
        this.shapeId = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
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
