
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Camera complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Camera">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="rot" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_SphereCoords" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="prst" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PresetCameraType" />
 *       &lt;attribute name="fov" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_FOVAngle" />
 *       &lt;attribute name="zoom" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositivePercentage" default="100000" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Camera", propOrder = {
    "rot"
})
public class CTCamera
    implements Child
{

    protected CTSphereCoords rot;
    @XmlAttribute(required = true)
    protected STPresetCameraType prst;
    @XmlAttribute
    protected Integer fov;
    @XmlAttribute
    protected Integer zoom;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the rot property.
     * 
     * @return
     *     possible object is
     *     {@link CTSphereCoords }
     *     
     */
    public CTSphereCoords getRot() {
        return rot;
    }

    /**
     * Sets the value of the rot property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSphereCoords }
     *     
     */
    public void setRot(CTSphereCoords value) {
        this.rot = value;
    }

    /**
     * Gets the value of the prst property.
     * 
     * @return
     *     possible object is
     *     {@link STPresetCameraType }
     *     
     */
    public STPresetCameraType getPrst() {
        return prst;
    }

    /**
     * Sets the value of the prst property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPresetCameraType }
     *     
     */
    public void setPrst(STPresetCameraType value) {
        this.prst = value;
    }

    /**
     * Gets the value of the fov property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getFov() {
        return fov;
    }

    /**
     * Sets the value of the fov property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setFov(Integer value) {
        this.fov = value;
    }

    /**
     * Gets the value of the zoom property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getZoom() {
        if (zoom == null) {
            return  100000;
        } else {
            return zoom;
        }
    }

    /**
     * Sets the value of the zoom property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setZoom(Integer value) {
        this.zoom = value;
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
