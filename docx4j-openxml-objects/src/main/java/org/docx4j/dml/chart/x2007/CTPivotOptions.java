
package org.docx4j.dml.chart.x2007;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_PivotOptions complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PivotOptions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dropZoneFilter" type="{http://schemas.microsoft.com/office/drawing/2007/8/2/chart}CT_BooleanFalse" minOccurs="0"/>
 *         &lt;element name="dropZoneCategories" type="{http://schemas.microsoft.com/office/drawing/2007/8/2/chart}CT_BooleanFalse" minOccurs="0"/>
 *         &lt;element name="dropZoneData" type="{http://schemas.microsoft.com/office/drawing/2007/8/2/chart}CT_BooleanFalse" minOccurs="0"/>
 *         &lt;element name="dropZoneSeries" type="{http://schemas.microsoft.com/office/drawing/2007/8/2/chart}CT_BooleanFalse" minOccurs="0"/>
 *         &lt;element name="dropZonesVisible" type="{http://schemas.microsoft.com/office/drawing/2007/8/2/chart}CT_BooleanFalse" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PivotOptions", propOrder = {
    "dropZoneFilter",
    "dropZoneCategories",
    "dropZoneData",
    "dropZoneSeries",
    "dropZonesVisible"
})
public class CTPivotOptions {

    protected CTBooleanFalse dropZoneFilter;
    protected CTBooleanFalse dropZoneCategories;
    protected CTBooleanFalse dropZoneData;
    protected CTBooleanFalse dropZoneSeries;
    protected CTBooleanFalse dropZonesVisible;

    /**
     * Gets the value of the dropZoneFilter property.
     * 
     * @return
     *     possible object is
     *     {@link CTBooleanFalse }
     *     
     */
    public CTBooleanFalse getDropZoneFilter() {
        return dropZoneFilter;
    }

    /**
     * Sets the value of the dropZoneFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBooleanFalse }
     *     
     */
    public void setDropZoneFilter(CTBooleanFalse value) {
        this.dropZoneFilter = value;
    }

    /**
     * Gets the value of the dropZoneCategories property.
     * 
     * @return
     *     possible object is
     *     {@link CTBooleanFalse }
     *     
     */
    public CTBooleanFalse getDropZoneCategories() {
        return dropZoneCategories;
    }

    /**
     * Sets the value of the dropZoneCategories property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBooleanFalse }
     *     
     */
    public void setDropZoneCategories(CTBooleanFalse value) {
        this.dropZoneCategories = value;
    }

    /**
     * Gets the value of the dropZoneData property.
     * 
     * @return
     *     possible object is
     *     {@link CTBooleanFalse }
     *     
     */
    public CTBooleanFalse getDropZoneData() {
        return dropZoneData;
    }

    /**
     * Sets the value of the dropZoneData property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBooleanFalse }
     *     
     */
    public void setDropZoneData(CTBooleanFalse value) {
        this.dropZoneData = value;
    }

    /**
     * Gets the value of the dropZoneSeries property.
     * 
     * @return
     *     possible object is
     *     {@link CTBooleanFalse }
     *     
     */
    public CTBooleanFalse getDropZoneSeries() {
        return dropZoneSeries;
    }

    /**
     * Sets the value of the dropZoneSeries property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBooleanFalse }
     *     
     */
    public void setDropZoneSeries(CTBooleanFalse value) {
        this.dropZoneSeries = value;
    }

    /**
     * Gets the value of the dropZonesVisible property.
     * 
     * @return
     *     possible object is
     *     {@link CTBooleanFalse }
     *     
     */
    public CTBooleanFalse getDropZonesVisible() {
        return dropZonesVisible;
    }

    /**
     * Sets the value of the dropZonesVisible property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBooleanFalse }
     *     
     */
    public void setDropZonesVisible(CTBooleanFalse value) {
        this.dropZonesVisible = value;
    }

}
