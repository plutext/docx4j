
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTSolidColorFillProperties;


/**
 * <p>Java class for CT_ValueColors complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ValueColors"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="minColor" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_SolidColorFillProperties" minOccurs="0"/&gt;
 *         &lt;element name="midColor" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_SolidColorFillProperties" minOccurs="0"/&gt;
 *         &lt;element name="maxColor" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_SolidColorFillProperties" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ValueColors", propOrder = {
    "minColor",
    "midColor",
    "maxColor"
})
public class CTValueColors {

    protected CTSolidColorFillProperties minColor;
    protected CTSolidColorFillProperties midColor;
    protected CTSolidColorFillProperties maxColor;

    /**
     * Gets the value of the minColor property.
     * 
     * @return
     *     possible object is
     *     {@link CTSolidColorFillProperties }
     *     
     */
    public CTSolidColorFillProperties getMinColor() {
        return minColor;
    }

    /**
     * Sets the value of the minColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSolidColorFillProperties }
     *     
     */
    public void setMinColor(CTSolidColorFillProperties value) {
        this.minColor = value;
    }

    /**
     * Gets the value of the midColor property.
     * 
     * @return
     *     possible object is
     *     {@link CTSolidColorFillProperties }
     *     
     */
    public CTSolidColorFillProperties getMidColor() {
        return midColor;
    }

    /**
     * Sets the value of the midColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSolidColorFillProperties }
     *     
     */
    public void setMidColor(CTSolidColorFillProperties value) {
        this.midColor = value;
    }

    /**
     * Gets the value of the maxColor property.
     * 
     * @return
     *     possible object is
     *     {@link CTSolidColorFillProperties }
     *     
     */
    public CTSolidColorFillProperties getMaxColor() {
        return maxColor;
    }

    /**
     * Sets the value of the maxColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSolidColorFillProperties }
     *     
     */
    public void setMaxColor(CTSolidColorFillProperties value) {
        this.maxColor = value;
    }

}
