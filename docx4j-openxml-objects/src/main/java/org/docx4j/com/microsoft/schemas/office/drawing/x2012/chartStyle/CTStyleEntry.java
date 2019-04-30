
package org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTTextBodyProperties;
import org.docx4j.dml.CTTextCharacterProperties;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_StyleEntry complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_StyleEntry"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="lnRef" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleReference"/&gt;
 *         &lt;element name="lineWidthScale" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/&gt;
 *         &lt;element name="fillRef" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleReference"/&gt;
 *         &lt;element name="effectRef" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_StyleReference"/&gt;
 *         &lt;element name="fontRef" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}CT_FontReference"/&gt;
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties" minOccurs="0"/&gt;
 *         &lt;element name="defRPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextCharacterProperties" minOccurs="0"/&gt;
 *         &lt;element name="bodyPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextBodyProperties" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="mods" type="{http://schemas.microsoft.com/office/drawing/2012/chartStyle}ST_StyleEntryModifierList" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_StyleEntry", propOrder = {
    "lnRef",
    "lineWidthScale",
    "fillRef",
    "effectRef",
    "fontRef",
    "spPr",
    "defRPr",
    "bodyPr",
    "extLst"
})
public class CTStyleEntry implements Child
{

    @XmlElement(required = true)
    protected CTStyleReference lnRef;
    @XmlElement(defaultValue = "1.0")
    protected Double lineWidthScale;
    @XmlElement(required = true)
    protected CTStyleReference fillRef;
    @XmlElement(required = true)
    protected CTStyleReference effectRef;
    @XmlElement(required = true)
    protected CTFontReference fontRef;
    protected CTShapeProperties spPr;
    protected CTTextCharacterProperties defRPr;
    protected CTTextBodyProperties bodyPr;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "mods")
    protected List<String> mods;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the lnRef property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleReference }
     *     
     */
    public CTStyleReference getLnRef() {
        return lnRef;
    }

    /**
     * Sets the value of the lnRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleReference }
     *     
     */
    public void setLnRef(CTStyleReference value) {
        this.lnRef = value;
    }

    /**
     * Gets the value of the lineWidthScale property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getLineWidthScale() {
        return lineWidthScale;
    }

    /**
     * Sets the value of the lineWidthScale property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setLineWidthScale(Double value) {
        this.lineWidthScale = value;
    }

    /**
     * Gets the value of the fillRef property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleReference }
     *     
     */
    public CTStyleReference getFillRef() {
        return fillRef;
    }

    /**
     * Sets the value of the fillRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleReference }
     *     
     */
    public void setFillRef(CTStyleReference value) {
        this.fillRef = value;
    }

    /**
     * Gets the value of the effectRef property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleReference }
     *     
     */
    public CTStyleReference getEffectRef() {
        return effectRef;
    }

    /**
     * Sets the value of the effectRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleReference }
     *     
     */
    public void setEffectRef(CTStyleReference value) {
        this.effectRef = value;
    }

    /**
     * Gets the value of the fontRef property.
     * 
     * @return
     *     possible object is
     *     {@link CTFontReference }
     *     
     */
    public CTFontReference getFontRef() {
        return fontRef;
    }

    /**
     * Sets the value of the fontRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFontReference }
     *     
     */
    public void setFontRef(CTFontReference value) {
        this.fontRef = value;
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
     * Gets the value of the defRPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextCharacterProperties }
     *     
     */
    public CTTextCharacterProperties getDefRPr() {
        return defRPr;
    }

    /**
     * Sets the value of the defRPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextCharacterProperties }
     *     
     */
    public void setDefRPr(CTTextCharacterProperties value) {
        this.defRPr = value;
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
     * Gets the value of the mods property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mods property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMods().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getMods() {
        if (mods == null) {
            mods = new ArrayList<String>();
        }
        return this.mods;
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
