
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_AxisUnits complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_AxisUnits"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="unitsLabel" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_AxisUnitsLabel" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="unit" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_AxisUnit" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_AxisUnits", propOrder = {
    "unitsLabel",
    "extLst"
})
public class CTAxisUnits {

    protected CTAxisUnitsLabel unitsLabel;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "unit")
    protected STAxisUnit unit;

    /**
     * Gets the value of the unitsLabel property.
     * 
     * @return
     *     possible object is
     *     {@link CTAxisUnitsLabel }
     *     
     */
    public CTAxisUnitsLabel getUnitsLabel() {
        return unitsLabel;
    }

    /**
     * Sets the value of the unitsLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAxisUnitsLabel }
     *     
     */
    public void setUnitsLabel(CTAxisUnitsLabel value) {
        this.unitsLabel = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the unit property.
     * 
     * @return
     *     possible object is
     *     {@link STAxisUnit }
     *     
     */
    public STAxisUnit getUnit() {
        return unit;
    }

    /**
     * Sets the value of the unit property.
     * 
     * @param value
     *     allowed object is
     *     {@link STAxisUnit }
     *     
     */
    public void setUnit(STAxisUnit value) {
        this.unit = value;
    }

}
