/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */


package org.docx4j.dml.chart;

import org.docx4j.dml.ArrayListDml;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTShapeProperties;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PlotArea complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PlotArea"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="layout" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Layout" minOccurs="0"/&gt;
 *         &lt;choice maxOccurs="unbounded"&gt;
 *           &lt;element name="areaChart" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_AreaChart"/&gt;
 *           &lt;element name="area3DChart" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Area3DChart"/&gt;
 *           &lt;element name="lineChart" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_LineChart"/&gt;
 *           &lt;element name="line3DChart" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Line3DChart"/&gt;
 *           &lt;element name="stockChart" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_StockChart"/&gt;
 *           &lt;element name="radarChart" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_RadarChart"/&gt;
 *           &lt;element name="scatterChart" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ScatterChart"/&gt;
 *           &lt;element name="pieChart" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_PieChart"/&gt;
 *           &lt;element name="pie3DChart" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Pie3DChart"/&gt;
 *           &lt;element name="doughnutChart" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_DoughnutChart"/&gt;
 *           &lt;element name="barChart" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_BarChart"/&gt;
 *           &lt;element name="bar3DChart" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Bar3DChart"/&gt;
 *           &lt;element name="ofPieChart" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_OfPieChart"/&gt;
 *           &lt;element name="surfaceChart" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_SurfaceChart"/&gt;
 *           &lt;element name="surface3DChart" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Surface3DChart"/&gt;
 *           &lt;element name="bubbleChart" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_BubbleChart"/&gt;
 *         &lt;/choice&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="valAx" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ValAx"/&gt;
 *           &lt;element name="catAx" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_CatAx"/&gt;
 *           &lt;element name="dateAx" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_DateAx"/&gt;
 *           &lt;element name="serAx" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_SerAx"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="dTable" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_DTable" minOccurs="0"/&gt;
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PlotArea", propOrder = {
    "layout",
    "areaChartOrArea3DChartOrLineChart",
    "valAxOrCatAxOrDateAx",
    "dTable",
    "spPr",
    "extLst"
})
public class CTPlotArea implements Child
{

    protected CTLayout layout;
    @XmlElements({
        @XmlElement(name = "areaChart", type = CTAreaChart.class),
        @XmlElement(name = "area3DChart", type = CTArea3DChart.class),
        @XmlElement(name = "lineChart", type = CTLineChart.class),
        @XmlElement(name = "line3DChart", type = CTLine3DChart.class),
        @XmlElement(name = "stockChart", type = CTStockChart.class),
        @XmlElement(name = "radarChart", type = CTRadarChart.class),
        @XmlElement(name = "scatterChart", type = CTScatterChart.class),
        @XmlElement(name = "pieChart", type = CTPieChart.class),
        @XmlElement(name = "pie3DChart", type = CTPie3DChart.class),
        @XmlElement(name = "doughnutChart", type = CTDoughnutChart.class),
        @XmlElement(name = "barChart", type = CTBarChart.class),
        @XmlElement(name = "bar3DChart", type = CTBar3DChart.class),
        @XmlElement(name = "ofPieChart", type = CTOfPieChart.class),
        @XmlElement(name = "surfaceChart", type = CTSurfaceChart.class),
        @XmlElement(name = "surface3DChart", type = CTSurface3DChart.class),
        @XmlElement(name = "bubbleChart", type = CTBubbleChart.class)
    })
    protected List<Object> areaChartOrArea3DChartOrLineChart  = new ArrayListDml<Object>(this);

    @XmlElements({
        @XmlElement(name = "valAx", type = CTValAx.class),
        @XmlElement(name = "catAx", type = CTCatAx.class),
        @XmlElement(name = "dateAx", type = CTDateAx.class),
        @XmlElement(name = "serAx", type = CTSerAx.class)
    })
    protected List<Object> valAxOrCatAxOrDateAx = new ArrayListDml<Object>(this);

    protected CTDTable dTable;
    protected CTShapeProperties spPr;
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the layout property.
     * 
     * @return
     *     possible object is
     *     {@link CTLayout }
     *     
     */
    public CTLayout getLayout() {
        return layout;
    }

    /**
     * Sets the value of the layout property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLayout }
     *     
     */
    public void setLayout(CTLayout value) {
        this.layout = value;
    }

    /**
     * Gets the value of the areaChartOrArea3DChartOrLineChart property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the areaChartOrArea3DChartOrLineChart property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAreaChartOrArea3DChartOrLineChart().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTAreaChart }
     * {@link CTArea3DChart }
     * {@link CTLineChart }
     * {@link CTLine3DChart }
     * {@link CTStockChart }
     * {@link CTRadarChart }
     * {@link CTScatterChart }
     * {@link CTPieChart }
     * {@link CTPie3DChart }
     * {@link CTDoughnutChart }
     * {@link CTBarChart }
     * {@link CTBar3DChart }
     * {@link CTOfPieChart }
     * {@link CTSurfaceChart }
     * {@link CTSurface3DChart }
     * {@link CTBubbleChart }
     * 
     * 
     */
    public List<Object> getAreaChartOrArea3DChartOrLineChart() {
        if (areaChartOrArea3DChartOrLineChart == null) {
            areaChartOrArea3DChartOrLineChart = new ArrayListDml<Object>(this);
        }
        return this.areaChartOrArea3DChartOrLineChart;
    }

    /**
     * Gets the value of the valAxOrCatAxOrDateAx property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the valAxOrCatAxOrDateAx property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValAxOrCatAxOrDateAx().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTValAx }
     * {@link CTCatAx }
     * {@link CTDateAx }
     * {@link CTSerAx }
     * 
     * 
     */
    public List<Object> getValAxOrCatAxOrDateAx() {
        if (valAxOrCatAxOrDateAx == null) {
            valAxOrCatAxOrDateAx = new ArrayListDml<Object>(this);
        }
        return this.valAxOrCatAxOrDateAx;
    }

    /**
     * Gets the value of the dTable property.
     * 
     * @return
     *     possible object is
     *     {@link CTDTable }
     *     
     */
    public CTDTable getDTable() {
        return dTable;
    }

    /**
     * Sets the value of the dTable property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDTable }
     *     
     */
    public void setDTable(CTDTable value) {
        this.dTable = value;
    }

    /**
     * Gets the value of the spPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTShapeProperties }
     *     
     */
    public CTShapeProperties getSpPr() {
        return spPr;
    }

    /**
     * Sets the value of the spPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShapeProperties }
     *     
     */
    public void setSpPr(CTShapeProperties value) {
        this.spPr = value;
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
