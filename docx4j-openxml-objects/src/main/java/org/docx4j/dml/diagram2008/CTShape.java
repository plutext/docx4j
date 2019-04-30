
package org.docx4j.dml.diagram2008;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTShapeStyle;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTransform2D;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Shape complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Shape"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="nvSpPr" type="{http://schemas.microsoft.com/office/drawing/2008/diagram}CT_ShapeNonVisual"/&gt;
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties"/&gt;
 *         &lt;element name="style" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeStyle" minOccurs="0"/&gt;
 *         &lt;element name="txBody" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextBody" minOccurs="0"/&gt;
 *         &lt;element name="txXfrm" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Transform2D" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="modelId" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_ModelId" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Shape", propOrder = {
    "nvSpPr",
    "spPr",
    "style",
    "txBody",
    "txXfrm",
    "extLst"
})
public class CTShape implements Child
{

    @XmlElement(required = true)
    protected CTShapeNonVisual nvSpPr;
    @XmlElement(required = true)
    protected CTShapeProperties spPr;
    protected CTShapeStyle style;
    protected CTTextBody txBody;
    protected CTTransform2D txXfrm;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "modelId", required = true)
    protected String modelId;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the nvSpPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTShapeNonVisual }
     *     
     */
    public CTShapeNonVisual getNvSpPr() {
        return nvSpPr;
    }

    /**
     * Sets the value of the nvSpPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShapeNonVisual }
     *     
     */
    public void setNvSpPr(CTShapeNonVisual value) {
        this.nvSpPr = value;
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
     * Gets the value of the txBody property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBody }
     *     
     */
    public CTTextBody getTxBody() {
        return txBody;
    }

    /**
     * Sets the value of the txBody property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBody }
     *     
     */
    public void setTxBody(CTTextBody value) {
        this.txBody = value;
    }

    /**
     * Gets the value of the txXfrm property.
     * 
     * @return
     *     possible object is
     *     {@link CTTransform2D }
     *     
     */
    public CTTransform2D getTxXfrm() {
        return txXfrm;
    }

    /**
     * Sets the value of the txXfrm property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTransform2D }
     *     
     */
    public void setTxXfrm(CTTransform2D value) {
        this.txXfrm = value;
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
     * Gets the value of the modelId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModelId() {
        return modelId;
    }

    /**
     * Sets the value of the modelId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModelId(String value) {
        this.modelId = value;
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
