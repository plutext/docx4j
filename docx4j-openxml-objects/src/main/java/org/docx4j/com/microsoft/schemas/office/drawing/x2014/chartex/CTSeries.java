
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTShapeProperties;
import org.jvnet.jaxb2_commons.ppp.Child;;


/**
 * <p>Java class for CT_Series complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Series"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="tx" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_Text" minOccurs="0"/&gt;
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties" minOccurs="0"/&gt;
 *         &lt;element name="valueColors" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_ValueColors" minOccurs="0"/&gt;
 *         &lt;element name="valueColorPositions" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_ValueColorPositions" minOccurs="0"/&gt;
 *         &lt;element name="dataPt" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_DataPoint" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="dataLabels" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_DataLabels" minOccurs="0"/&gt;
 *         &lt;element name="dataId" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_DataId" minOccurs="0"/&gt;
 *         &lt;element name="layoutPr" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_SeriesLayoutProperties" minOccurs="0"/&gt;
 *         &lt;element name="axisId" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_AxisId" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="layoutId" use="required" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_SeriesLayout" /&gt;
 *       &lt;attribute name="hidden" type="{http://www.w3.org/2001/XMLSchema}boolean" default="0" /&gt;
 *       &lt;attribute name="ownerIdx" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
 *       &lt;attribute name="uniqueId" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="formatIdx" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Series", propOrder = {
    "tx",
    "spPr",
    "valueColors",
    "valueColorPositions",
    "dataPt",
    "dataLabels",
    "dataId",
    "layoutPr",
    "axisId",
    "extLst"
})
public class CTSeries implements Child
{

    protected CTText tx;
    protected CTShapeProperties spPr;
    protected CTValueColors valueColors;
    protected CTValueColorPositions valueColorPositions;
    protected List<CTDataPoint> dataPt;
    protected CTDataLabels dataLabels;
    protected CTDataId dataId;
    protected CTSeriesLayoutProperties layoutPr;
    @XmlElement(type = Long.class)
    @XmlSchemaType(name = "unsignedInt")
    protected List<Long> axisId;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "layoutId", required = true)
    protected STSeriesLayout layoutId;
    @XmlAttribute(name = "hidden")
    protected Boolean hidden;
    @XmlAttribute(name = "ownerIdx")
    @XmlSchemaType(name = "unsignedInt")
    protected Long ownerIdx;
    @XmlAttribute(name = "uniqueId")
    protected String uniqueId;
    @XmlAttribute(name = "formatIdx")
    @XmlSchemaType(name = "unsignedInt")
    protected Long formatIdx;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the tx property.
     * 
     * @return
     *     possible object is
     *     {@link CTText }
     *     
     */
    public CTText getTx() {
        return tx;
    }

    /**
     * Sets the value of the tx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTText }
     *     
     */
    public void setTx(CTText value) {
        this.tx = value;
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
     * Gets the value of the valueColors property.
     * 
     * @return
     *     possible object is
     *     {@link CTValueColors }
     *     
     */
    public CTValueColors getValueColors() {
        return valueColors;
    }

    /**
     * Sets the value of the valueColors property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTValueColors }
     *     
     */
    public void setValueColors(CTValueColors value) {
        this.valueColors = value;
    }

    /**
     * Gets the value of the valueColorPositions property.
     * 
     * @return
     *     possible object is
     *     {@link CTValueColorPositions }
     *     
     */
    public CTValueColorPositions getValueColorPositions() {
        return valueColorPositions;
    }

    /**
     * Sets the value of the valueColorPositions property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTValueColorPositions }
     *     
     */
    public void setValueColorPositions(CTValueColorPositions value) {
        this.valueColorPositions = value;
    }

    /**
     * Gets the value of the dataPt property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dataPt property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDataPt().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTDataPoint }
     * 
     * 
     */
    public List<CTDataPoint> getDataPt() {
        if (dataPt == null) {
            dataPt = new ArrayList<CTDataPoint>();
        }
        return this.dataPt;
    }

    /**
     * Gets the value of the dataLabels property.
     * 
     * @return
     *     possible object is
     *     {@link CTDataLabels }
     *     
     */
    public CTDataLabels getDataLabels() {
        return dataLabels;
    }

    /**
     * Sets the value of the dataLabels property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDataLabels }
     *     
     */
    public void setDataLabels(CTDataLabels value) {
        this.dataLabels = value;
    }

    /**
     * Gets the value of the dataId property.
     * 
     * @return
     *     possible object is
     *     {@link CTDataId }
     *     
     */
    public CTDataId getDataId() {
        return dataId;
    }

    /**
     * Sets the value of the dataId property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDataId }
     *     
     */
    public void setDataId(CTDataId value) {
        this.dataId = value;
    }

    /**
     * Gets the value of the layoutPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTSeriesLayoutProperties }
     *     
     */
    public CTSeriesLayoutProperties getLayoutPr() {
        return layoutPr;
    }

    /**
     * Sets the value of the layoutPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSeriesLayoutProperties }
     *     
     */
    public void setLayoutPr(CTSeriesLayoutProperties value) {
        this.layoutPr = value;
    }

    /**
     * Gets the value of the axisId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the axisId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAxisId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getAxisId() {
        if (axisId == null) {
            axisId = new ArrayList<Long>();
        }
        return this.axisId;
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
     * Gets the value of the layoutId property.
     * 
     * @return
     *     possible object is
     *     {@link STSeriesLayout }
     *     
     */
    public STSeriesLayout getLayoutId() {
        return layoutId;
    }

    /**
     * Sets the value of the layoutId property.
     * 
     * @param value
     *     allowed object is
     *     {@link STSeriesLayout }
     *     
     */
    public void setLayoutId(STSeriesLayout value) {
        this.layoutId = value;
    }

    /**
     * Gets the value of the hidden property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHidden() {
        if (hidden == null) {
            return false;
        } else {
            return hidden;
        }
    }

    /**
     * Sets the value of the hidden property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHidden(Boolean value) {
        this.hidden = value;
    }

    /**
     * Gets the value of the ownerIdx property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getOwnerIdx() {
        return ownerIdx;
    }

    /**
     * Sets the value of the ownerIdx property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setOwnerIdx(Long value) {
        this.ownerIdx = value;
    }

    /**
     * Gets the value of the uniqueId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * Sets the value of the uniqueId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUniqueId(String value) {
        this.uniqueId = value;
    }

    /**
     * Gets the value of the formatIdx property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getFormatIdx() {
        return formatIdx;
    }

    /**
     * Sets the value of the formatIdx property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setFormatIdx(Long value) {
        this.formatIdx = value;
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
