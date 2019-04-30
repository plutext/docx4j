
package org.docx4j.dml.chart.x2007;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PivotOptions complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PivotOptions"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="dropZoneFilter" type="{http://schemas.microsoft.com/office/drawing/2007/8/2/chart}CT_BooleanFalse" minOccurs="0"/&gt;
 *         &lt;element name="dropZoneCategories" type="{http://schemas.microsoft.com/office/drawing/2007/8/2/chart}CT_BooleanFalse" minOccurs="0"/&gt;
 *         &lt;element name="dropZoneData" type="{http://schemas.microsoft.com/office/drawing/2007/8/2/chart}CT_BooleanFalse" minOccurs="0"/&gt;
 *         &lt;element name="dropZoneSeries" type="{http://schemas.microsoft.com/office/drawing/2007/8/2/chart}CT_BooleanFalse" minOccurs="0"/&gt;
 *         &lt;element name="dropZonesVisible" type="{http://schemas.microsoft.com/office/drawing/2007/8/2/chart}CT_BooleanFalse" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
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
public class CTPivotOptions implements Child
{

    protected CTBooleanFalse dropZoneFilter;
    protected CTBooleanFalse dropZoneCategories;
    protected CTBooleanFalse dropZoneData;
    protected CTBooleanFalse dropZoneSeries;
    protected CTBooleanFalse dropZonesVisible;
    @XmlTransient
    private Object parent;

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
