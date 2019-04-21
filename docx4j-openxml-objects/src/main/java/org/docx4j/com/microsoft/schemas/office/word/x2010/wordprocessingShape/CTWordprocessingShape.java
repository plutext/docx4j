
package org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTNonVisualConnectorProperties;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTNonVisualDrawingShapeProps;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTShapeStyle;
import org.docx4j.dml.CTTextBodyProperties;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_WordprocessingShape complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_WordprocessingShape"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="cNvPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingProps" minOccurs="0"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="cNvSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingShapeProps"/&gt;
 *           &lt;element name="cNvCnPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualConnectorProperties"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties"/&gt;
 *         &lt;element name="style" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeStyle" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="txbx" type="{http://schemas.microsoft.com/office/word/2010/wordprocessingShape}CT_TextboxInfo"/&gt;
 *           &lt;element name="linkedTxbx" type="{http://schemas.microsoft.com/office/word/2010/wordprocessingShape}CT_LinkedTextboxInformation"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="bodyPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextBodyProperties"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="normalEastAsianFlow" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_WordprocessingShape", propOrder = {
    "cNvPr",
    "cNvSpPr",
    "cNvCnPr",
    "spPr",
    "style",
    "extLst",
    "txbx",
    "linkedTxbx",
    "bodyPr"
})
public class CTWordprocessingShape implements Child
{

    protected CTNonVisualDrawingProps cNvPr;
    protected CTNonVisualDrawingShapeProps cNvSpPr;
    protected CTNonVisualConnectorProperties cNvCnPr;
    @XmlElement(required = true)
    protected CTShapeProperties spPr;
    protected CTShapeStyle style;
    protected CTOfficeArtExtensionList extLst;
    protected CTTextboxInfo txbx;
    protected CTLinkedTextboxInformation linkedTxbx;
    @XmlElement(required = true)
    protected CTTextBodyProperties bodyPr;
    @XmlAttribute(name = "normalEastAsianFlow")
    protected Boolean normalEastAsianFlow;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the cNvPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNonVisualDrawingProps }
     *     
     */
    public CTNonVisualDrawingProps getCNvPr() {
        return cNvPr;
    }

    /**
     * Sets the value of the cNvPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNonVisualDrawingProps }
     *     
     */
    public void setCNvPr(CTNonVisualDrawingProps value) {
        this.cNvPr = value;
    }

    /**
     * Gets the value of the cNvSpPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNonVisualDrawingShapeProps }
     *     
     */
    public CTNonVisualDrawingShapeProps getCNvSpPr() {
        return cNvSpPr;
    }

    /**
     * Sets the value of the cNvSpPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNonVisualDrawingShapeProps }
     *     
     */
    public void setCNvSpPr(CTNonVisualDrawingShapeProps value) {
        this.cNvSpPr = value;
    }

    /**
     * Gets the value of the cNvCnPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNonVisualConnectorProperties }
     *     
     */
    public CTNonVisualConnectorProperties getCNvCnPr() {
        return cNvCnPr;
    }

    /**
     * Sets the value of the cNvCnPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNonVisualConnectorProperties }
     *     
     */
    public void setCNvCnPr(CTNonVisualConnectorProperties value) {
        this.cNvCnPr = value;
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
     * Gets the value of the style property.
     * 
     * @return
     *     possible object is
     *     {@link CTShapeStyle }
     *     
     */
    public CTShapeStyle getStyle() {
        return style;
    }

    /**
     * Sets the value of the style property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShapeStyle }
     *     
     */
    public void setStyle(CTShapeStyle value) {
        this.style = value;
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
     * Gets the value of the txbx property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextboxInfo }
     *     
     */
    public CTTextboxInfo getTxbx() {
        return txbx;
    }

    /**
     * Sets the value of the txbx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextboxInfo }
     *     
     */
    public void setTxbx(CTTextboxInfo value) {
        this.txbx = value;
    }

    /**
     * Gets the value of the linkedTxbx property.
     * 
     * @return
     *     possible object is
     *     {@link CTLinkedTextboxInformation }
     *     
     */
    public CTLinkedTextboxInformation getLinkedTxbx() {
        return linkedTxbx;
    }

    /**
     * Sets the value of the linkedTxbx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTLinkedTextboxInformation }
     *     
     */
    public void setLinkedTxbx(CTLinkedTextboxInformation value) {
        this.linkedTxbx = value;
    }

    /**
     * Gets the value of the bodyPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBodyProperties }
     *     
     */
    public CTTextBodyProperties getBodyPr() {
        return bodyPr;
    }

    /**
     * Sets the value of the bodyPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBodyProperties }
     *     
     */
    public void setBodyPr(CTTextBodyProperties value) {
        this.bodyPr = value;
    }

    /**
     * Gets the value of the normalEastAsianFlow property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNormalEastAsianFlow() {
        if (normalEastAsianFlow == null) {
            return false;
        } else {
            return normalEastAsianFlow;
        }
    }

    /**
     * Sets the value of the normalEastAsianFlow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNormalEastAsianFlow(Boolean value) {
        this.normalEastAsianFlow = value;
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
