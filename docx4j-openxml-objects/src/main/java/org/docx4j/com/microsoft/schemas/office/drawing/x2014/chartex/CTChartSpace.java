
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTColorMapping;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTTextBody;
import org.jvnet.jaxb2_commons.ppp.Child;;


/**
 * <p>Java class for CT_ChartSpace complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ChartSpace"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="chartData" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_ChartData"/&gt;
 *         &lt;element name="chart" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_Chart"/&gt;
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties" minOccurs="0"/&gt;
 *         &lt;element name="txPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextBody" minOccurs="0"/&gt;
 *         &lt;element name="clrMapOvr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ColorMapping" minOccurs="0"/&gt;
 *         &lt;element name="fmtOvrs" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_FormatOverrides" minOccurs="0"/&gt;
 *         &lt;element name="printSettings" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_PrintSettings" minOccurs="0"/&gt;
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
@XmlType(name = "CT_ChartSpace", propOrder = {
    "chartData",
    "chart",
    "spPr",
    "txPr",
    "clrMapOvr",
    "fmtOvrs",
    "printSettings",
    "extLst"
})
public class CTChartSpace implements Child
{

    @XmlElement(required = true)
    protected CTChartData chartData;
    @XmlElement(required = true)
    protected CTChart chart;
    protected CTShapeProperties spPr;
    protected CTTextBody txPr;
    protected CTColorMapping clrMapOvr;
    protected CTFormatOverrides fmtOvrs;
    protected CTPrintSettings printSettings;
    protected CTExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the chartData property.
     * 
     * @return
     *     possible object is
     *     {@link CTChartData }
     *     
     */
    public CTChartData getChartData() {
        return chartData;
    }

    /**
     * Sets the value of the chartData property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTChartData }
     *     
     */
    public void setChartData(CTChartData value) {
        this.chartData = value;
    }

    /**
     * Gets the value of the chart property.
     * 
     * @return
     *     possible object is
     *     {@link CTChart }
     *     
     */
    public CTChart getChart() {
        return chart;
    }

    /**
     * Sets the value of the chart property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTChart }
     *     
     */
    public void setChart(CTChart value) {
        this.chart = value;
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
     * Gets the value of the txPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBody }
     *     
     */
    public CTTextBody getTxPr() {
        return txPr;
    }

    /**
     * Sets the value of the txPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBody }
     *     
     */
    public void setTxPr(CTTextBody value) {
        this.txPr = value;
    }

    /**
     * Gets the value of the clrMapOvr property.
     * 
     * @return
     *     possible object is
     *     {@link CTColorMapping }
     *     
     */
    public CTColorMapping getClrMapOvr() {
        return clrMapOvr;
    }

    /**
     * Sets the value of the clrMapOvr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColorMapping }
     *     
     */
    public void setClrMapOvr(CTColorMapping value) {
        this.clrMapOvr = value;
    }

    /**
     * Gets the value of the fmtOvrs property.
     * 
     * @return
     *     possible object is
     *     {@link CTFormatOverrides }
     *     
     */
    public CTFormatOverrides getFmtOvrs() {
        return fmtOvrs;
    }

    /**
     * Sets the value of the fmtOvrs property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFormatOverrides }
     *     
     */
    public void setFmtOvrs(CTFormatOverrides value) {
        this.fmtOvrs = value;
    }

    /**
     * Gets the value of the printSettings property.
     * 
     * @return
     *     possible object is
     *     {@link CTPrintSettings }
     *     
     */
    public CTPrintSettings getPrintSettings() {
        return printSettings;
    }

    /**
     * Sets the value of the printSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPrintSettings }
     *     
     */
    public void setPrintSettings(CTPrintSettings value) {
        this.printSettings = value;
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
