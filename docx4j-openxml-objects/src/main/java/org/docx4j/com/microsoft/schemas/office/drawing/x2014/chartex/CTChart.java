
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Chart complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Chart"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="title" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_ChartTitle" minOccurs="0"/&gt;
 *         &lt;element name="plotArea" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_PlotArea"/&gt;
 *         &lt;element name="legend" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_Legend" minOccurs="0"/&gt;
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
@XmlType(name = "CT_Chart", propOrder = {
    "title",
    "plotArea",
    "legend",
    "extLst"
})
public class CTChart {

    protected CTChartTitle title;
    @XmlElement(required = true)
    protected CTPlotArea plotArea;
    protected CTLegend legend;
    protected CTExtensionList extLst;

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link CTChartTitle }
     *     
     */
    public CTChartTitle getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTChartTitle }
     *     
     */
    public void setTitle(CTChartTitle value) {
        this.title = value;
    }

    /**
     * Gets the value of the plotArea property.
     * 
     * @return
     *     possible object is
     *     {@link CTPlotArea }
     *     
     */
    public CTPlotArea getPlotArea() {
        return plotArea;
    }

    /**
     * Sets the value of the plotArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPlotArea }
     *     
     */
    public void setPlotArea(CTPlotArea value) {
        this.plotArea = value;
    }

    /**
     * Gets the value of the legend property.
     * 
     * @return
     *     possible object is
     *     {@link CTLegend }
     *     
     */
    public CTLegend getLegend() {
        return legend;
    }

    /**
     * Sets the value of the legend property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLegend }
     *     
     */
    public void setLegend(CTLegend value) {
        this.legend = value;
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
