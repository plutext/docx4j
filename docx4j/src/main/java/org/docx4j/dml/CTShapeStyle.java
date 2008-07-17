
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ShapeStyle complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ShapeStyle">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="lnRef" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_StyleMatrixReference"/>
 *         &lt;element name="fillRef" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_StyleMatrixReference"/>
 *         &lt;element name="effectRef" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_StyleMatrixReference"/>
 *         &lt;element name="fontRef" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_FontReference"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ShapeStyle", propOrder = {
    "lnRef",
    "fillRef",
    "effectRef",
    "fontRef"
})
public class CTShapeStyle
    implements Child
{

    @XmlElement(required = true)
    protected CTStyleMatrixReference lnRef;
    @XmlElement(required = true)
    protected CTStyleMatrixReference fillRef;
    @XmlElement(required = true)
    protected CTStyleMatrixReference effectRef;
    @XmlElement(required = true)
    protected CTFontReference fontRef;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the lnRef property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleMatrixReference }
     *     
     */
    public CTStyleMatrixReference getLnRef() {
        return lnRef;
    }

    /**
     * Sets the value of the lnRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleMatrixReference }
     *     
     */
    public void setLnRef(CTStyleMatrixReference value) {
        this.lnRef = value;
    }

    /**
     * Gets the value of the fillRef property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleMatrixReference }
     *     
     */
    public CTStyleMatrixReference getFillRef() {
        return fillRef;
    }

    /**
     * Sets the value of the fillRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleMatrixReference }
     *     
     */
    public void setFillRef(CTStyleMatrixReference value) {
        this.fillRef = value;
    }

    /**
     * Gets the value of the effectRef property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleMatrixReference }
     *     
     */
    public CTStyleMatrixReference getEffectRef() {
        return effectRef;
    }

    /**
     * Sets the value of the effectRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleMatrixReference }
     *     
     */
    public void setEffectRef(CTStyleMatrixReference value) {
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
