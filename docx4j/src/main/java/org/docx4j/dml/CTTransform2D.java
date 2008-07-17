
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Transform2D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Transform2D">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="off" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Point2D" minOccurs="0"/>
 *         &lt;element name="ext" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_PositiveSize2D" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="rot" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Angle" default="0" />
 *       &lt;attribute name="flipH" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="flipV" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Transform2D", propOrder = {
    "off",
    "ext"
})
public class CTTransform2D
    implements Child
{

    protected CTPoint2D off;
    protected CTPositiveSize2D ext;
    @XmlAttribute
    protected Integer rot;
    @XmlAttribute
    protected Boolean flipH;
    @XmlAttribute
    protected Boolean flipV;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the off property.
     * 
     * @return
     *     possible object is
     *     {@link CTPoint2D }
     *     
     */
    public CTPoint2D getOff() {
        return off;
    }

    /**
     * Sets the value of the off property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPoint2D }
     *     
     */
    public void setOff(CTPoint2D value) {
        this.off = value;
    }

    /**
     * Gets the value of the ext property.
     * 
     * @return
     *     possible object is
     *     {@link CTPositiveSize2D }
     *     
     */
    public CTPositiveSize2D getExt() {
        return ext;
    }

    /**
     * Sets the value of the ext property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPositiveSize2D }
     *     
     */
    public void setExt(CTPositiveSize2D value) {
        this.ext = value;
    }

    /**
     * Gets the value of the rot property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getRot() {
        if (rot == null) {
            return  0;
        } else {
            return rot;
        }
    }

    /**
     * Sets the value of the rot property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRot(Integer value) {
        this.rot = value;
    }

    /**
     * Gets the value of the flipH property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFlipH() {
        if (flipH == null) {
            return false;
        } else {
            return flipH;
        }
    }

    /**
     * Sets the value of the flipH property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFlipH(Boolean value) {
        this.flipH = value;
    }

    /**
     * Gets the value of the flipV property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFlipV() {
        if (flipV == null) {
            return false;
        } else {
            return flipV;
        }
    }

    /**
     * Sets the value of the flipV property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFlipV(Boolean value) {
        this.flipV = value;
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
