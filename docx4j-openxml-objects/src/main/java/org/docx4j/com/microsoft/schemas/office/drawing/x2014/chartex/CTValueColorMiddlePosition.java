
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_ValueColorMiddlePosition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ValueColorMiddlePosition"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="number" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_NumberColorPosition"/&gt;
 *         &lt;element name="percent" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_PercentageColorPosition"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ValueColorMiddlePosition", propOrder = {
    "number",
    "percent"
})
public class CTValueColorMiddlePosition {

    protected CTNumberColorPosition number;
    protected CTPercentageColorPosition percent;

    /**
     * Gets the value of the number property.
     * 
     * @return
     *     possible object is
     *     {@link CTNumberColorPosition }
     *     
     */
    public CTNumberColorPosition getNumber() {
        return number;
    }

    /**
     * Sets the value of the number property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNumberColorPosition }
     *     
     */
    public void setNumber(CTNumberColorPosition value) {
        this.number = value;
    }

    /**
     * Gets the value of the percent property.
     * 
     * @return
     *     possible object is
     *     {@link CTPercentageColorPosition }
     *     
     */
    public CTPercentageColorPosition getPercent() {
        return percent;
    }

    /**
     * Sets the value of the percent property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPercentageColorPosition }
     *     
     */
    public void setPercent(CTPercentageColorPosition value) {
        this.percent = value;
    }

}
