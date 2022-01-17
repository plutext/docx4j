
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_ValueAxisScaling complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ValueAxisScaling"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="max" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_DoubleOrAutomatic" /&gt;
 *       &lt;attribute name="min" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_DoubleOrAutomatic" /&gt;
 *       &lt;attribute name="majorUnit" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_ValueAxisUnit" /&gt;
 *       &lt;attribute name="minorUnit" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_ValueAxisUnit" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ValueAxisScaling")
public class CTValueAxisScaling {

    @XmlAttribute(name = "max")
    protected String max;
    @XmlAttribute(name = "min")
    protected String min;
    @XmlAttribute(name = "majorUnit")
    protected String majorUnit;
    @XmlAttribute(name = "minorUnit")
    protected String minorUnit;

    /**
     * Gets the value of the max property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMax() {
        return max;
    }

    /**
     * Sets the value of the max property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMax(String value) {
        this.max = value;
    }

    /**
     * Gets the value of the min property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMin() {
        return min;
    }

    /**
     * Sets the value of the min property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMin(String value) {
        this.min = value;
    }

    /**
     * Gets the value of the majorUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMajorUnit() {
        return majorUnit;
    }

    /**
     * Sets the value of the majorUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMajorUnit(String value) {
        this.majorUnit = value;
    }

    /**
     * Gets the value of the minorUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinorUnit() {
        return minorUnit;
    }

    /**
     * Sets the value of the minorUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinorUnit(String value) {
        this.minorUnit = value;
    }

}
