
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;;


/**
 * <p>Java class for CT_SeriesLayoutProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SeriesLayoutProperties"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="parentLabelLayout" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_ParentLabelLayout" minOccurs="0"/&gt;
 *         &lt;element name="regionLabelLayout" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_RegionLabelLayout" minOccurs="0"/&gt;
 *         &lt;element name="visibility" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_SeriesElementVisibilities" minOccurs="0"/&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="aggregation" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_Aggregation" minOccurs="0"/&gt;
 *           &lt;element name="binning" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_Binning" minOccurs="0"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="geography" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_Geography" minOccurs="0"/&gt;
 *         &lt;element name="statistics" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_Statistics" minOccurs="0"/&gt;
 *         &lt;element name="subtotals" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_Subtotals" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SeriesLayoutProperties", propOrder = {
    "parentLabelLayout",
    "regionLabelLayout",
    "visibility",
    "aggregation",
    "binning",
    "geography",
    "statistics",
    "subtotals",
    "extLst"
})
public class CTSeriesLayoutProperties implements Child
{

    protected CTParentLabelLayout parentLabelLayout;
    protected CTRegionLabelLayout regionLabelLayout;
    protected CTSeriesElementVisibilities visibility;
    protected CTAggregation aggregation;
    protected CTBinning binning;
    protected CTGeography geography;
    protected CTStatistics statistics;
    protected CTSubtotals subtotals;
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the parentLabelLayout property.
     * 
     * @return
     *     possible object is
     *     {@link CTParentLabelLayout }
     *     
     */
    public CTParentLabelLayout getParentLabelLayout() {
        return parentLabelLayout;
    }

    /**
     * Sets the value of the parentLabelLayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTParentLabelLayout }
     *     
     */
    public void setParentLabelLayout(CTParentLabelLayout value) {
        this.parentLabelLayout = value;
    }

    /**
     * Gets the value of the regionLabelLayout property.
     * 
     * @return
     *     possible object is
     *     {@link CTRegionLabelLayout }
     *     
     */
    public CTRegionLabelLayout getRegionLabelLayout() {
        return regionLabelLayout;
    }

    /**
     * Sets the value of the regionLabelLayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRegionLabelLayout }
     *     
     */
    public void setRegionLabelLayout(CTRegionLabelLayout value) {
        this.regionLabelLayout = value;
    }

    /**
     * Gets the value of the visibility property.
     * 
     * @return
     *     possible object is
     *     {@link CTSeriesElementVisibilities }
     *     
     */
    public CTSeriesElementVisibilities getVisibility() {
        return visibility;
    }

    /**
     * Sets the value of the visibility property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSeriesElementVisibilities }
     *     
     */
    public void setVisibility(CTSeriesElementVisibilities value) {
        this.visibility = value;
    }

    /**
     * Gets the value of the aggregation property.
     * 
     * @return
     *     possible object is
     *     {@link CTAggregation }
     *     
     */
    public CTAggregation getAggregation() {
        return aggregation;
    }

    /**
     * Sets the value of the aggregation property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAggregation }
     *     
     */
    public void setAggregation(CTAggregation value) {
        this.aggregation = value;
    }

    /**
     * Gets the value of the binning property.
     * 
     * @return
     *     possible object is
     *     {@link CTBinning }
     *     
     */
    public CTBinning getBinning() {
        return binning;
    }

    /**
     * Sets the value of the binning property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBinning }
     *     
     */
    public void setBinning(CTBinning value) {
        this.binning = value;
    }

    /**
     * Gets the value of the geography property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeography }
     *     
     */
    public CTGeography getGeography() {
        return geography;
    }

    /**
     * Sets the value of the geography property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeography }
     *     
     */
    public void setGeography(CTGeography value) {
        this.geography = value;
    }

    /**
     * Gets the value of the statistics property.
     * 
     * @return
     *     possible object is
     *     {@link CTStatistics }
     *     
     */
    public CTStatistics getStatistics() {
        return statistics;
    }

    /**
     * Sets the value of the statistics property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStatistics }
     *     
     */
    public void setStatistics(CTStatistics value) {
        this.statistics = value;
    }

    /**
     * Gets the value of the subtotals property.
     * 
     * @return
     *     possible object is
     *     {@link CTSubtotals }
     *     
     */
    public CTSubtotals getSubtotals() {
        return subtotals;
    }

    /**
     * Sets the value of the subtotals property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSubtotals }
     *     
     */
    public void setSubtotals(CTSubtotals value) {
        this.subtotals = value;
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
