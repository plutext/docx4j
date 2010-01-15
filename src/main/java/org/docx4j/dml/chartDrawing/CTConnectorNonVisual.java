
package org.docx4j.dml.chartDrawing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTNonVisualConnectorProperties;
import org.docx4j.dml.CTNonVisualDrawingProps;


/**
 * <p>Java class for CT_ConnectorNonVisual complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ConnectorNonVisual">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cNvPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingProps"/>
 *         &lt;element name="cNvCxnSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualConnectorProperties"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ConnectorNonVisual", propOrder = {
    "cNvPr",
    "cNvCxnSpPr"
})
public class CTConnectorNonVisual {

    @XmlElement(required = true)
    protected CTNonVisualDrawingProps cNvPr;
    @XmlElement(required = true)
    protected CTNonVisualConnectorProperties cNvCxnSpPr;

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
     * Gets the value of the cNvCxnSpPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNonVisualConnectorProperties }
     *     
     */
    public CTNonVisualConnectorProperties getCNvCxnSpPr() {
        return cNvCxnSpPr;
    }

    /**
     * Sets the value of the cNvCxnSpPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNonVisualConnectorProperties }
     *     
     */
    public void setCNvCxnSpPr(CTNonVisualConnectorProperties value) {
        this.cNvCxnSpPr = value;
    }

}
