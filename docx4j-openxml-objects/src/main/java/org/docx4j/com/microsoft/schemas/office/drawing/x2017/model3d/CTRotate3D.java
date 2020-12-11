
package org.docx4j.com.microsoft.schemas.office.drawing.x2017.model3d;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Rotate3D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Rotate3D"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="ax" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Angle" default="0" /&gt;
 *       &lt;attribute name="ay" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Angle" default="0" /&gt;
 *       &lt;attribute name="az" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Angle" default="0" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Rotate3D")
public class CTRotate3D implements Child
{

    @XmlAttribute(name = "ax")
    protected Integer ax;
    @XmlAttribute(name = "ay")
    protected Integer ay;
    @XmlAttribute(name = "az")
    protected Integer az;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the ax property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getAx() {
        if (ax == null) {
            return  0;
        } else {
            return ax;
        }
    }

    /**
     * Sets the value of the ax property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAx(Integer value) {
        this.ax = value;
    }

    /**
     * Gets the value of the ay property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getAy() {
        if (ay == null) {
            return  0;
        } else {
            return ay;
        }
    }

    /**
     * Sets the value of the ay property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAy(Integer value) {
        this.ay = value;
    }

    /**
     * Gets the value of the az property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getAz() {
        if (az == null) {
            return  0;
        } else {
            return az;
        }
    }

    /**
     * Sets the value of the az property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAz(Integer value) {
        this.az = value;
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
