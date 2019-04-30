
package org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTNonVisualGraphicFrameProperties;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.docx4j.dml.CTTransform2D;
import org.docx4j.dml.Graphic;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_GraphicFrame complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GraphicFrame"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="cNvPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingProps"/&gt;
 *         &lt;element name="cNvFrPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualGraphicFrameProperties"/&gt;
 *         &lt;element name="xfrm" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Transform2D"/&gt;
 *         &lt;element ref="{http://schemas.openxmlformats.org/drawingml/2006/main}graphic"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GraphicFrame", propOrder = {
    "cNvPr",
    "cNvFrPr",
    "xfrm",
    "graphic",
    "extLst"
})
public class CTGraphicFrame implements Child
{

    @XmlElement(required = true)
    protected CTNonVisualDrawingProps cNvPr;
    @XmlElement(required = true)
    protected CTNonVisualGraphicFrameProperties cNvFrPr;
    @XmlElement(required = true)
    protected CTTransform2D xfrm;
    @XmlElement(namespace = "http://schemas.openxmlformats.org/drawingml/2006/main", required = true)
    protected Graphic graphic;
    protected CTOfficeArtExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the cNvPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNonVisualDrawingProps }
     *     
     */
    public CTNonVisualDrawingProps getCNvPr() {
        return cNvPr;
    }

    /**
     * Sets the value of the cNvPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNonVisualDrawingProps }
     *     
     */
    public void setCNvPr(CTNonVisualDrawingProps value) {
        this.cNvPr = value;
    }

    /**
     * Gets the value of the cNvFrPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNonVisualGraphicFrameProperties }
     *     
     */
    public CTNonVisualGraphicFrameProperties getCNvFrPr() {
        return cNvFrPr;
    }

    /**
     * Sets the value of the cNvFrPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNonVisualGraphicFrameProperties }
     *     
     */
    public void setCNvFrPr(CTNonVisualGraphicFrameProperties value) {
        this.cNvFrPr = value;
    }

    /**
     * Gets the value of the xfrm property.
     * 
     * @return
     *     possible object is
     *     {@link CTTransform2D }
     *     
     */
    public CTTransform2D getXfrm() {
        return xfrm;
    }

    /**
     * Sets the value of the xfrm property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTransform2D }
     *     
     */
    public void setXfrm(CTTransform2D value) {
        this.xfrm = value;
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
