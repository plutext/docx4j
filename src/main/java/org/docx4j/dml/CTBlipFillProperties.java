
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_BlipFillProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_BlipFillProperties">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="blip" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Blip" minOccurs="0"/>
 *         &lt;element name="srcRect" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_RelativeRect" minOccurs="0"/>
 *         &lt;group ref="{http://schemas.openxmlformats.org/drawingml/2006/main}EG_FillModeProperties" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="dpi" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="rotWithShape" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_BlipFillProperties", propOrder = {
    "blip",
    "srcRect",
    "tile",
    "stretch"
})
public class CTBlipFillProperties implements Child
{

    protected CTBlip blip;
    protected CTRelativeRect srcRect;
    protected CTTileInfoProperties tile;
    protected CTStretchInfoProperties stretch;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long dpi;
    @XmlAttribute
    protected Boolean rotWithShape;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the blip property.
     * 
     * @return
     *     possible object is
     *     {@link CTBlip }
     *     
     */
    public CTBlip getBlip() {
        return blip;
    }

    /**
     * Sets the value of the blip property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBlip }
     *     
     */
    public void setBlip(CTBlip value) {
        this.blip = value;
    }

    /**
     * Gets the value of the srcRect property.
     * 
     * @return
     *     possible object is
     *     {@link CTRelativeRect }
     *     
     */
    public CTRelativeRect getSrcRect() {
        return srcRect;
    }

    /**
     * Sets the value of the srcRect property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRelativeRect }
     *     
     */
    public void setSrcRect(CTRelativeRect value) {
        this.srcRect = value;
    }

    /**
     * Gets the value of the tile property.
     * 
     * @return
     *     possible object is
     *     {@link CTTileInfoProperties }
     *     
     */
    public CTTileInfoProperties getTile() {
        return tile;
    }

    /**
     * Sets the value of the tile property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTileInfoProperties }
     *     
     */
    public void setTile(CTTileInfoProperties value) {
        this.tile = value;
    }

    /**
     * Gets the value of the stretch property.
     * 
     * @return
     *     possible object is
     *     {@link CTStretchInfoProperties }
     *     
     */
    public CTStretchInfoProperties getStretch() {
        return stretch;
    }

    /**
     * Sets the value of the stretch property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStretchInfoProperties }
     *     
     */
    public void setStretch(CTStretchInfoProperties value) {
        this.stretch = value;
    }

    /**
     * Gets the value of the dpi property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDpi() {
        return dpi;
    }

    /**
     * Sets the value of the dpi property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDpi(Long value) {
        this.dpi = value;
    }

    /**
     * Gets the value of the rotWithShape property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRotWithShape() {
        return rotWithShape;
    }

    /**
     * Sets the value of the rotWithShape property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRotWithShape(Boolean value) {
        this.rotWithShape = value;
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
