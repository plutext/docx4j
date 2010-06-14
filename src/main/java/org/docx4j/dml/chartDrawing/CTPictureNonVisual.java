
package org.docx4j.dml.chartDrawing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTNonVisualPictureProperties;


/**
 * <p>Java class for CT_PictureNonVisual complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PictureNonVisual">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cNvPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingProps"/>
 *         &lt;element name="cNvPicPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualPictureProperties"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace="http://schemas.openxmlformats.org/drawingml/2006/chartDrawing", name = "CT_PictureNonVisual", propOrder = {
    "cNvPr",
    "cNvPicPr"
})
public class CTPictureNonVisual {

    @XmlElement(required = true)
    protected CTNonVisualDrawingProps cNvPr;
    @XmlElement(required = true)
    protected CTNonVisualPictureProperties cNvPicPr;

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
     * Gets the value of the cNvPicPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNonVisualPictureProperties }
     *     
     */
    public CTNonVisualPictureProperties getCNvPicPr() {
        return cNvPicPr;
    }

    /**
     * Sets the value of the cNvPicPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNonVisualPictureProperties }
     *     
     */
    public void setCNvPicPr(CTNonVisualPictureProperties value) {
        this.cNvPicPr = value;
    }

}
