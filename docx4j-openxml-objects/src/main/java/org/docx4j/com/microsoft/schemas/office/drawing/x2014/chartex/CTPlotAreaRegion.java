
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_PlotAreaRegion complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PlotAreaRegion"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="plotSurface" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_PlotSurface" minOccurs="0"/&gt;
 *         &lt;element name="series" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_Series" maxOccurs="unbounded" minOccurs="0"/&gt;
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
@XmlType(name = "CT_PlotAreaRegion", propOrder = {
    "plotSurface",
    "series",
    "extLst"
})
public class CTPlotAreaRegion {

    protected CTPlotSurface plotSurface;
    protected List<CTSeries> series;
    protected CTExtensionList extLst;

    /**
     * Gets the value of the plotSurface property.
     * 
     * @return
     *     possible object is
     *     {@link CTPlotSurface }
     *     
     */
    public CTPlotSurface getPlotSurface() {
        return plotSurface;
    }

    /**
     * Sets the value of the plotSurface property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPlotSurface }
     *     
     */
    public void setPlotSurface(CTPlotSurface value) {
        this.plotSurface = value;
    }

    /**
     * Gets the value of the series property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the series property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSeries().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTSeries }
     * 
     * 
     */
    public List<CTSeries> getSeries() {
        if (series == null) {
            series = new ArrayList<CTSeries>();
        }
        return this.series;
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

}
