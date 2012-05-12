
package org.docx4j.dml.diagram2008;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTNonVisualGroupDrawingShapeProps;


/**
 * <p>Java class for CT_GroupShapeNonVisual complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GroupShapeNonVisual">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cNvPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingProps"/>
 *         &lt;element name="cNvGrpSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualGroupDrawingShapeProps"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GroupShapeNonVisual", propOrder = {
    "cNvPr",
    "cNvGrpSpPr"
})
public class CTGroupShapeNonVisual {

    @XmlElement(required = true)
    protected CTNonVisualDrawingProps cNvPr;
    @XmlElement(required = true)
    protected CTNonVisualGroupDrawingShapeProps cNvGrpSpPr;

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
     * Gets the value of the cNvGrpSpPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNonVisualGroupDrawingShapeProps }
     *     
     */
    public CTNonVisualGroupDrawingShapeProps getCNvGrpSpPr() {
        return cNvGrpSpPr;
    }

    /**
     * Sets the value of the cNvGrpSpPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNonVisualGroupDrawingShapeProps }
     *     
     */
    public void setCNvGrpSpPr(CTNonVisualGroupDrawingShapeProps value) {
        this.cNvGrpSpPr = value;
    }

}
