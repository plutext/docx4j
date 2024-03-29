
package org.docx4j.com.microsoft.schemas.office.drawing.x2017.model3d;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTColor;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.docx4j.dml.CTPoint3D;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SpotLight complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SpotLight"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="clr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Color"/&gt;
 *         &lt;element name="intensity" type="{http://schemas.microsoft.com/office/drawing/2017/model3d}CT_PositiveRatio"/&gt;
 *         &lt;element name="pos" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Point3D"/&gt;
 *         &lt;element name="lookAt" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Point3D"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="rad" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveCoordinate" /&gt;
 *       &lt;attribute name="spotAng" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_FOVAngle" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SpotLight", propOrder = {
    "clr",
    "intensity",
    "pos",
    "lookAt",
    "extLst"
})
public class CTSpotLight implements Child
{

    @XmlElement(required = true)
    protected CTColor clr;
    @XmlElement(required = true)
    protected CTPositiveRatio intensity;
    @XmlElement(required = true)
    protected CTPoint3D pos;
    @XmlElement(required = true)
    protected CTPoint3D lookAt;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "enabled")
    protected Boolean enabled;
    @XmlAttribute(name = "rad", required = true)
    protected long rad;
    @XmlAttribute(name = "spotAng", required = true)
    protected int spotAng;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the clr property.
     * 
     * @return
     *     possible object is
     *     {@link CTColor }
     *     
     */
    public CTColor getClr() {
        return clr;
    }

    /**
     * Sets the value of the clr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColor }
     *     
     */
    public void setClr(CTColor value) {
        this.clr = value;
    }

    /**
     * Gets the value of the intensity property.
     * 
     * @return
     *     possible object is
     *     {@link CTPositiveRatio }
     *     
     */
    public CTPositiveRatio getIntensity() {
        return intensity;
    }

    /**
     * Sets the value of the intensity property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPositiveRatio }
     *     
     */
    public void setIntensity(CTPositiveRatio value) {
        this.intensity = value;
    }

    /**
     * Gets the value of the pos property.
     * 
     * @return
     *     possible object is
     *     {@link CTPoint3D }
     *     
     */
    public CTPoint3D getPos() {
        return pos;
    }

    /**
     * Sets the value of the pos property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPoint3D }
     *     
     */
    public void setPos(CTPoint3D value) {
        this.pos = value;
    }

    /**
     * Gets the value of the lookAt property.
     * 
     * @return
     *     possible object is
     *     {@link CTPoint3D }
     *     
     */
    public CTPoint3D getLookAt() {
        return lookAt;
    }

    /**
     * Sets the value of the lookAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPoint3D }
     *     
     */
    public void setLookAt(CTPoint3D value) {
        this.lookAt = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the enabled property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEnabled() {
        if (enabled == null) {
            return true;
        } else {
            return enabled;
        }
    }

    /**
     * Sets the value of the enabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnabled(Boolean value) {
        this.enabled = value;
    }

    /**
     * Gets the value of the rad property.
     * 
     */
    public long getRad() {
        return rad;
    }

    /**
     * Sets the value of the rad property.
     * 
     */
    public void setRad(long value) {
        this.rad = value;
    }

    /**
     * Gets the value of the spotAng property.
     * 
     */
    public int getSpotAng() {
        return spotAng;
    }

    /**
     * Sets the value of the spotAng property.
     * 
     */
    public void setSpotAng(int value) {
        this.spotAng = value;
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
