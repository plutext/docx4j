
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Inline complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Inline">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="extent" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_PositiveSize2D"/>
 *         &lt;element name="effectExtent" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}CT_EffectExtent" minOccurs="0"/>
 *         &lt;element name="docPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingProps"/>
 *         &lt;element name="cNvGraphicFramePr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualGraphicFrameProperties" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/drawingml/2006/main}graphic"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Inline", namespace = "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", propOrder = {
    "extent",
    "effectExtent",
    "docPr",
    "cNvGraphicFramePr",
    "graphic"
})
public class Inline implements Child
{

    @XmlElement(required = true)
    protected CTPositiveSize2D extent;
    protected CTEffectExtent effectExtent;
    @XmlElement(required = true)
    protected CTNonVisualDrawingProps docPr;
    protected CTNonVisualGraphicFrameProperties cNvGraphicFramePr;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", required = true)
    protected Graphic graphic;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the extent property.
     * 
     * @return
     *     possible object is
     *     {@link CTPositiveSize2D }
     *     
     */
    public CTPositiveSize2D getExtent() {
        return extent;
    }

    /**
     * Sets the value of the extent property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPositiveSize2D }
     *     
     */
    public void setExtent(CTPositiveSize2D value) {
        this.extent = value;
    }

    /**
     * Gets the value of the effectExtent property.
     * 
     * @return
     *     possible object is
     *     {@link CTEffectExtent }
     *     
     */
    public CTEffectExtent getEffectExtent() {
        return effectExtent;
    }

    /**
     * Sets the value of the effectExtent property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEffectExtent }
     *     
     */
    public void setEffectExtent(CTEffectExtent value) {
        this.effectExtent = value;
    }

    /**
     * Gets the value of the docPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNonVisualDrawingProps }
     *     
     */
    public CTNonVisualDrawingProps getDocPr() {
        return docPr;
    }

    /**
     * Sets the value of the docPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNonVisualDrawingProps }
     *     
     */
    public void setDocPr(CTNonVisualDrawingProps value) {
        this.docPr = value;
    }

    /**
     * Gets the value of the cNvGraphicFramePr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNonVisualGraphicFrameProperties }
     *     
     */
    public CTNonVisualGraphicFrameProperties getCNvGraphicFramePr() {
        return cNvGraphicFramePr;
    }

    /**
     * Sets the value of the cNvGraphicFramePr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNonVisualGraphicFrameProperties }
     *     
     */
    public void setCNvGraphicFramePr(CTNonVisualGraphicFrameProperties value) {
        this.cNvGraphicFramePr = value;
    }

    /**
     * Gets the value of the graphic property.
     * 
     * @return
     *     possible object is
     *     {@link Graphic }
     *     
     */
    public Graphic getGraphic() {
        return graphic;
    }

    /**
     * Sets the value of the graphic property.
     * 
     * @param value
     *     allowed object is
     *     {@link Graphic }
     *     
     */
    public void setGraphic(Graphic value) {
        this.graphic = value;
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
