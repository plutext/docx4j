
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
 * <p>Java class for CT_NonVisualPictureProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_NonVisualPictureProperties">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="picLocks" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_PictureLocking" minOccurs="0"/>
 *         &lt;element name="relativeResize" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ext" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtension" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="preferRelativeResize" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_NonVisualPictureProperties", propOrder = {
    "picLocks",
    "relativeResize",
    "ext"
})
public class CTNonVisualPictureProperties
    implements Child
{

    protected CTPictureLocking picLocks;
    @XmlElement(defaultValue = "true")
    protected Boolean relativeResize;
    protected CTOfficeArtExtension ext;
    @XmlAttribute
    protected Boolean preferRelativeResize;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the picLocks property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureLocking }
     *     
     */
    public CTPictureLocking getPicLocks() {
        return picLocks;
    }

    /**
     * Sets the value of the picLocks property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureLocking }
     *     
     */
    public void setPicLocks(CTPictureLocking value) {
        this.picLocks = value;
    }

    /**
     * Gets the value of the relativeResize property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRelativeResize() {
        return relativeResize;
    }

    /**
     * Sets the value of the relativeResize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRelativeResize(Boolean value) {
        this.relativeResize = value;
    }

    /**
     * Gets the value of the ext property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtension }
     *     
     */
    public CTOfficeArtExtension getExt() {
        return ext;
    }

    /**
     * Sets the value of the ext property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtension }
     *     
     */
    public void setExt(CTOfficeArtExtension value) {
        this.ext = value;
    }

    /**
     * Gets the value of the preferRelativeResize property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPreferRelativeResize() {
        if (preferRelativeResize == null) {
            return true;
        } else {
            return preferRelativeResize;
        }
    }

    /**
     * Sets the value of the preferRelativeResize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPreferRelativeResize(Boolean value) {
        this.preferRelativeResize = value;
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
