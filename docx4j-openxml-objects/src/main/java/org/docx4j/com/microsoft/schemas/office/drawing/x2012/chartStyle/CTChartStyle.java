
package org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ChartStyle complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ChartStyle"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="axisTitle" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="categoryAxis" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="chartArea" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="dataLabel" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="dataLabelCallout" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry" minOccurs="0"/&gt;
 *         &lt;element name="dataPoint" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="dataPoint3D" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="dataPointLine" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="dataPointMarker" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="dataPointMarkerLayout" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_MarkerLayout" minOccurs="0"/&gt;
 *         &lt;element name="dataPointWireframe" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="dataTable" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="downBar" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="dropLine" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="errorBar" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="floor" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="gridlineMajor" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="gridlineMinor" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="hiLoLine" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="leaderLine" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="legend" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="plotArea" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="plotArea3D" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="seriesAxis" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="seriesLine" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="title" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="trendline" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="trendlineLabel" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="upBar" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="valueAxis" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="wall" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleEntry"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ChartStyle", propOrder = {
    "axisTitle",
    "categoryAxis",
    "chartArea",
    "dataLabel",
    "dataLabelCallout",
    "dataPoint",
    "dataPoint3D",
    "dataPointLine",
    "dataPointMarker",
    "dataPointMarkerLayout",
    "dataPointWireframe",
    "dataTable",
    "downBar",
    "dropLine",
    "errorBar",
    "floor",
    "gridlineMajor",
    "gridlineMinor",
    "hiLoLine",
    "leaderLine",
    "legend",
    "plotArea",
    "plotArea3D",
    "seriesAxis",
    "seriesLine",
    "title",
    "trendline",
    "trendlineLabel",
    "upBar",
    "valueAxis",
    "wall",
    "extLst"
})
public class CTChartStyle implements Child
{

    @XmlElement(required = true)
    protected CTStyleEntry axisTitle;
    @XmlElement(required = true)
    protected CTStyleEntry categoryAxis;
    @XmlElement(required = true)
    protected CTStyleEntry chartArea;
    @XmlElement(required = true)
    protected CTStyleEntry dataLabel;
    protected CTStyleEntry dataLabelCallout;
    @XmlElement(required = true)
    protected CTStyleEntry dataPoint;
    @XmlElement(required = true)
    protected CTStyleEntry dataPoint3D;
    @XmlElement(required = true)
    protected CTStyleEntry dataPointLine;
    @XmlElement(required = true)
    protected CTStyleEntry dataPointMarker;
    protected CTMarkerLayout dataPointMarkerLayout;
    @XmlElement(required = true)
    protected CTStyleEntry dataPointWireframe;
    @XmlElement(required = true)
    protected CTStyleEntry dataTable;
    @XmlElement(required = true)
    protected CTStyleEntry downBar;
    @XmlElement(required = true)
    protected CTStyleEntry dropLine;
    @XmlElement(required = true)
    protected CTStyleEntry errorBar;
    @XmlElement(required = true)
    protected CTStyleEntry floor;
    @XmlElement(required = true)
    protected CTStyleEntry gridlineMajor;
    @XmlElement(required = true)
    protected CTStyleEntry gridlineMinor;
    @XmlElement(required = true)
    protected CTStyleEntry hiLoLine;
    @XmlElement(required = true)
    protected CTStyleEntry leaderLine;
    @XmlElement(required = true)
    protected CTStyleEntry legend;
    @XmlElement(required = true)
    protected CTStyleEntry plotArea;
    @XmlElement(required = true)
    protected CTStyleEntry plotArea3D;
    @XmlElement(required = true)
    protected CTStyleEntry seriesAxis;
    @XmlElement(required = true)
    protected CTStyleEntry seriesLine;
    @XmlElement(required = true)
    protected CTStyleEntry title;
    @XmlElement(required = true)
    protected CTStyleEntry trendline;
    @XmlElement(required = true)
    protected CTStyleEntry trendlineLabel;
    @XmlElement(required = true)
    protected CTStyleEntry upBar;
    @XmlElement(required = true)
    protected CTStyleEntry valueAxis;
    @XmlElement(required = true)
    protected CTStyleEntry wall;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "id")
    @XmlSchemaType(name = "unsignedInt")
    protected Long id;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the axisTitle property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getAxisTitle() {
        return axisTitle;
    }

    /**
     * Sets the value of the axisTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setAxisTitle(CTStyleEntry value) {
        this.axisTitle = value;
    }

    /**
     * Gets the value of the categoryAxis property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getCategoryAxis() {
        return categoryAxis;
    }

    /**
     * Sets the value of the categoryAxis property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setCategoryAxis(CTStyleEntry value) {
        this.categoryAxis = value;
    }

    /**
     * Gets the value of the chartArea property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getChartArea() {
        return chartArea;
    }

    /**
     * Sets the value of the chartArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setChartArea(CTStyleEntry value) {
        this.chartArea = value;
    }

    /**
     * Gets the value of the dataLabel property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getDataLabel() {
        return dataLabel;
    }

    /**
     * Sets the value of the dataLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setDataLabel(CTStyleEntry value) {
        this.dataLabel = value;
    }

    /**
     * Gets the value of the dataLabelCallout property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getDataLabelCallout() {
        return dataLabelCallout;
    }

    /**
     * Sets the value of the dataLabelCallout property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setDataLabelCallout(CTStyleEntry value) {
        this.dataLabelCallout = value;
    }

    /**
     * Gets the value of the dataPoint property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getDataPoint() {
        return dataPoint;
    }

    /**
     * Sets the value of the dataPoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setDataPoint(CTStyleEntry value) {
        this.dataPoint = value;
    }

    /**
     * Gets the value of the dataPoint3D property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getDataPoint3D() {
        return dataPoint3D;
    }

    /**
     * Sets the value of the dataPoint3D property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setDataPoint3D(CTStyleEntry value) {
        this.dataPoint3D = value;
    }

    /**
     * Gets the value of the dataPointLine property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getDataPointLine() {
        return dataPointLine;
    }

    /**
     * Sets the value of the dataPointLine property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setDataPointLine(CTStyleEntry value) {
        this.dataPointLine = value;
    }

    /**
     * Gets the value of the dataPointMarker property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getDataPointMarker() {
        return dataPointMarker;
    }

    /**
     * Sets the value of the dataPointMarker property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setDataPointMarker(CTStyleEntry value) {
        this.dataPointMarker = value;
    }

    /**
     * Gets the value of the dataPointMarkerLayout property.
     * 
     * @return
     *     possible object is
     *     {@link CTMarkerLayout }
     *     
     */
    public CTMarkerLayout getDataPointMarkerLayout() {
        return dataPointMarkerLayout;
    }

    /**
     * Sets the value of the dataPointMarkerLayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMarkerLayout }
     *     
     */
    public void setDataPointMarkerLayout(CTMarkerLayout value) {
        this.dataPointMarkerLayout = value;
    }

    /**
     * Gets the value of the dataPointWireframe property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getDataPointWireframe() {
        return dataPointWireframe;
    }

    /**
     * Sets the value of the dataPointWireframe property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setDataPointWireframe(CTStyleEntry value) {
        this.dataPointWireframe = value;
    }

    /**
     * Gets the value of the dataTable property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getDataTable() {
        return dataTable;
    }

    /**
     * Sets the value of the dataTable property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setDataTable(CTStyleEntry value) {
        this.dataTable = value;
    }

    /**
     * Gets the value of the downBar property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getDownBar() {
        return downBar;
    }

    /**
     * Sets the value of the downBar property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setDownBar(CTStyleEntry value) {
        this.downBar = value;
    }

    /**
     * Gets the value of the dropLine property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getDropLine() {
        return dropLine;
    }

    /**
     * Sets the value of the dropLine property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setDropLine(CTStyleEntry value) {
        this.dropLine = value;
    }

    /**
     * Gets the value of the errorBar property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getErrorBar() {
        return errorBar;
    }

    /**
     * Sets the value of the errorBar property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setErrorBar(CTStyleEntry value) {
        this.errorBar = value;
    }

    /**
     * Gets the value of the floor property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getFloor() {
        return floor;
    }

    /**
     * Sets the value of the floor property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setFloor(CTStyleEntry value) {
        this.floor = value;
    }

    /**
     * Gets the value of the gridlineMajor property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getGridlineMajor() {
        return gridlineMajor;
    }

    /**
     * Sets the value of the gridlineMajor property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setGridlineMajor(CTStyleEntry value) {
        this.gridlineMajor = value;
    }

    /**
     * Gets the value of the gridlineMinor property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getGridlineMinor() {
        return gridlineMinor;
    }

    /**
     * Sets the value of the gridlineMinor property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setGridlineMinor(CTStyleEntry value) {
        this.gridlineMinor = value;
    }

    /**
     * Gets the value of the hiLoLine property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getHiLoLine() {
        return hiLoLine;
    }

    /**
     * Sets the value of the hiLoLine property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setHiLoLine(CTStyleEntry value) {
        this.hiLoLine = value;
    }

    /**
     * Gets the value of the leaderLine property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getLeaderLine() {
        return leaderLine;
    }

    /**
     * Sets the value of the leaderLine property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setLeaderLine(CTStyleEntry value) {
        this.leaderLine = value;
    }

    /**
     * Gets the value of the legend property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getLegend() {
        return legend;
    }

    /**
     * Sets the value of the legend property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setLegend(CTStyleEntry value) {
        this.legend = value;
    }

    /**
     * Gets the value of the plotArea property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getPlotArea() {
        return plotArea;
    }

    /**
     * Sets the value of the plotArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setPlotArea(CTStyleEntry value) {
        this.plotArea = value;
    }

    /**
     * Gets the value of the plotArea3D property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getPlotArea3D() {
        return plotArea3D;
    }

    /**
     * Sets the value of the plotArea3D property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setPlotArea3D(CTStyleEntry value) {
        this.plotArea3D = value;
    }

    /**
     * Gets the value of the seriesAxis property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getSeriesAxis() {
        return seriesAxis;
    }

    /**
     * Sets the value of the seriesAxis property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setSeriesAxis(CTStyleEntry value) {
        this.seriesAxis = value;
    }

    /**
     * Gets the value of the seriesLine property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getSeriesLine() {
        return seriesLine;
    }

    /**
     * Sets the value of the seriesLine property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setSeriesLine(CTStyleEntry value) {
        this.seriesLine = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setTitle(CTStyleEntry value) {
        this.title = value;
    }

    /**
     * Gets the value of the trendline property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getTrendline() {
        return trendline;
    }

    /**
     * Sets the value of the trendline property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setTrendline(CTStyleEntry value) {
        this.trendline = value;
    }

    /**
     * Gets the value of the trendlineLabel property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getTrendlineLabel() {
        return trendlineLabel;
    }

    /**
     * Sets the value of the trendlineLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setTrendlineLabel(CTStyleEntry value) {
        this.trendlineLabel = value;
    }

    /**
     * Gets the value of the upBar property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getUpBar() {
        return upBar;
    }

    /**
     * Sets the value of the upBar property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setUpBar(CTStyleEntry value) {
        this.upBar = value;
    }

    /**
     * Gets the value of the valueAxis property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getValueAxis() {
        return valueAxis;
    }

    /**
     * Sets the value of the valueAxis property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setValueAxis(CTStyleEntry value) {
        this.valueAxis = value;
    }

    /**
     * Gets the value of the wall property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleEntry }
     *     
     */
    public CTStyleEntry getWall() {
        return wall;
    }

    /**
     * Sets the value of the wall property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleEntry }
     *     
     */
    public void setWall(CTStyleEntry value) {
        this.wall = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setId(Long value) {
        this.id = value;
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
