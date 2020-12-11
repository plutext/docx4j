
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTTextBody;
import org.jvnet.jaxb2_commons.ppp.Child;;


/**
 * <p>Java class for CT_Axis complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Axis"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="catScaling" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_CategoryAxisScaling"/&gt;
 *           &lt;element name="valScaling" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_ValueAxisScaling"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="title" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_AxisTitle" minOccurs="0"/&gt;
 *         &lt;element name="units" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_AxisUnits" minOccurs="0"/&gt;
 *         &lt;element name="majorGridlines" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_Gridlines" minOccurs="0"/&gt;
 *         &lt;element name="minorGridlines" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_Gridlines" minOccurs="0"/&gt;
 *         &lt;element name="majorTickMarks" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_TickMarks" minOccurs="0"/&gt;
 *         &lt;element name="minorTickMarks" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_TickMarks" minOccurs="0"/&gt;
 *         &lt;element name="tickLabels" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_TickLabels" minOccurs="0"/&gt;
 *         &lt;element name="numFmt" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_NumberFormat" minOccurs="0"/&gt;
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties" minOccurs="0"/&gt;
 *         &lt;element name="txPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextBody" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" use="required" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_AxisId" /&gt;
 *       &lt;attribute name="hidden" type="{http://www.w3.org/2001/XMLSchema}boolean" default="0" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Axis", propOrder = {
    "catScaling",
    "valScaling",
    "title",
    "units",
    "majorGridlines",
    "minorGridlines",
    "majorTickMarks",
    "minorTickMarks",
    "tickLabels",
    "numFmt",
    "spPr",
    "txPr",
    "extLst"
})
public class CTAxis implements Child
{

    protected CTCategoryAxisScaling catScaling;
    protected CTValueAxisScaling valScaling;
    protected CTAxisTitle title;
    protected CTAxisUnits units;
    protected CTGridlines majorGridlines;
    protected CTGridlines minorGridlines;
    protected CTTickMarks majorTickMarks;
    protected CTTickMarks minorTickMarks;
    protected CTTickLabels tickLabels;
    protected CTNumberFormat numFmt;
    protected CTShapeProperties spPr;
    protected CTTextBody txPr;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "id", required = true)
    protected long id;
    @XmlAttribute(name = "hidden")
    protected Boolean hidden;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the catScaling property.
     * 
     * @return
     *     possible object is
     *     {@link CTCategoryAxisScaling }
     *     
     */
    public CTCategoryAxisScaling getCatScaling() {
        return catScaling;
    }

    /**
     * Sets the value of the catScaling property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCategoryAxisScaling }
     *     
     */
    public void setCatScaling(CTCategoryAxisScaling value) {
        this.catScaling = value;
    }

    /**
     * Gets the value of the valScaling property.
     * 
     * @return
     *     possible object is
     *     {@link CTValueAxisScaling }
     *     
     */
    public CTValueAxisScaling getValScaling() {
        return valScaling;
    }

    /**
     * Sets the value of the valScaling property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTValueAxisScaling }
     *     
     */
    public void setValScaling(CTValueAxisScaling value) {
        this.valScaling = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link CTAxisTitle }
     *     
     */
    public CTAxisTitle getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAxisTitle }
     *     
     */
    public void setTitle(CTAxisTitle value) {
        this.title = value;
    }

    /**
     * Gets the value of the units property.
     * 
     * @return
     *     possible object is
     *     {@link CTAxisUnits }
     *     
     */
    public CTAxisUnits getUnits() {
        return units;
    }

    /**
     * Sets the value of the units property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAxisUnits }
     *     
     */
    public void setUnits(CTAxisUnits value) {
        this.units = value;
    }

    /**
     * Gets the value of the majorGridlines property.
     * 
     * @return
     *     possible object is
     *     {@link CTGridlines }
     *     
     */
    public CTGridlines getMajorGridlines() {
        return majorGridlines;
    }

    /**
     * Sets the value of the majorGridlines property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGridlines }
     *     
     */
    public void setMajorGridlines(CTGridlines value) {
        this.majorGridlines = value;
    }

    /**
     * Gets the value of the minorGridlines property.
     * 
     * @return
     *     possible object is
     *     {@link CTGridlines }
     *     
     */
    public CTGridlines getMinorGridlines() {
        return minorGridlines;
    }

    /**
     * Sets the value of the minorGridlines property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGridlines }
     *     
     */
    public void setMinorGridlines(CTGridlines value) {
        this.minorGridlines = value;
    }

    /**
     * Gets the value of the majorTickMarks property.
     * 
     * @return
     *     possible object is
     *     {@link CTTickMarks }
     *     
     */
    public CTTickMarks getMajorTickMarks() {
        return majorTickMarks;
    }

    /**
     * Sets the value of the majorTickMarks property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTickMarks }
     *     
     */
    public void setMajorTickMarks(CTTickMarks value) {
        this.majorTickMarks = value;
    }

    /**
     * Gets the value of the minorTickMarks property.
     * 
     * @return
     *     possible object is
     *     {@link CTTickMarks }
     *     
     */
    public CTTickMarks getMinorTickMarks() {
        return minorTickMarks;
    }

    /**
     * Sets the value of the minorTickMarks property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTickMarks }
     *     
     */
    public void setMinorTickMarks(CTTickMarks value) {
        this.minorTickMarks = value;
    }

    /**
     * Gets the value of the tickLabels property.
     * 
     * @return
     *     possible object is
     *     {@link CTTickLabels }
     *     
     */
    public CTTickLabels getTickLabels() {
        return tickLabels;
    }

    /**
     * Sets the value of the tickLabels property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTickLabels }
     *     
     */
    public void setTickLabels(CTTickLabels value) {
        this.tickLabels = value;
    }

    /**
     * Gets the value of the numFmt property.
     * 
     * @return
     *     possible object is
     *     {@link CTNumberFormat }
     *     
     */
    public CTNumberFormat getNumFmt() {
        return numFmt;
    }

    /**
     * Sets the value of the numFmt property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNumberFormat }
     *     
     */
    public void setNumFmt(CTNumberFormat value) {
        this.numFmt = value;
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
     * Gets the value of the id property.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(long value) {
        this.id = value;
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
