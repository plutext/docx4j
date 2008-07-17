
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Scene3D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Scene3D">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="camera" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Camera"/>
 *         &lt;element name="lightRig" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_LightRig"/>
 *         &lt;element name="backdrop" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Backdrop" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Scene3D", propOrder = {
    "camera",
    "lightRig",
    "backdrop",
    "extLst"
})
public class CTScene3D
    implements Child
{

    @XmlElement(required = true)
    protected CTCamera camera;
    @XmlElement(required = true)
    protected CTLightRig lightRig;
    protected CTBackdrop backdrop;
    protected CTOfficeArtExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the camera property.
     * 
     * @return
     *     possible object is
     *     {@link CTCamera }
     *     
     */
    public CTCamera getCamera() {
        return camera;
    }

    /**
     * Sets the value of the camera property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCamera }
     *     
     */
    public void setCamera(CTCamera value) {
        this.camera = value;
    }

    /**
     * Gets the value of the lightRig property.
     * 
     * @return
     *     possible object is
     *     {@link CTLightRig }
     *     
     */
    public CTLightRig getLightRig() {
        return lightRig;
    }

    /**
     * Sets the value of the lightRig property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLightRig }
     *     
     */
    public void setLightRig(CTLightRig value) {
        this.lightRig = value;
    }

    /**
     * Gets the value of the backdrop property.
     * 
     * @return
     *     possible object is
     *     {@link CTBackdrop }
     *     
     */
    public CTBackdrop getBackdrop() {
        return backdrop;
    }

    /**
     * Sets the value of the backdrop property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBackdrop }
     *     
     */
    public void setBackdrop(CTBackdrop value) {
        this.backdrop = value;
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
