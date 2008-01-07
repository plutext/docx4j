
package org.docx4j.jaxb.document;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Shd"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "shd")
public class Shd
    implements Child
{

    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String val;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    @XmlSchemaType(name = "anySimpleType")
    protected String color;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    @XmlSchemaType(name = "anySimpleType")
    protected String themeColor;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    @XmlSchemaType(name = "anySimpleType")
    protected String themeTint;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    @XmlSchemaType(name = "anySimpleType")
    protected String themeShade;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    @XmlSchemaType(name = "anySimpleType")
    protected String fill;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    @XmlSchemaType(name = "anySimpleType")
    protected String themeFill;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    @XmlSchemaType(name = "anySimpleType")
    protected String themeFillTint;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    @XmlSchemaType(name = "anySimpleType")
    protected String themeFillShade;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the val property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVal() {
        return val;
    }

    /**
     * Sets the value of the val property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVal(String value) {
        this.val = value;
    }

    /**
     * Gets the value of the color property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the value of the color property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColor(String value) {
        this.color = value;
    }

    /**
     * Gets the value of the themeColor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThemeColor() {
        return themeColor;
    }

    /**
     * Sets the value of the themeColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThemeColor(String value) {
        this.themeColor = value;
    }

    /**
     * Gets the value of the themeTint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThemeTint() {
        return themeTint;
    }

    /**
     * Sets the value of the themeTint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThemeTint(String value) {
        this.themeTint = value;
    }

    /**
     * Gets the value of the themeShade property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThemeShade() {
        return themeShade;
    }

    /**
     * Sets the value of the themeShade property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThemeShade(String value) {
        this.themeShade = value;
    }

    /**
     * Gets the value of the fill property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFill() {
        return fill;
    }

    /**
     * Sets the value of the fill property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFill(String value) {
        this.fill = value;
    }

    /**
     * Gets the value of the themeFill property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThemeFill() {
        return themeFill;
    }

    /**
     * Sets the value of the themeFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThemeFill(String value) {
        this.themeFill = value;
    }

    /**
     * Gets the value of the themeFillTint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThemeFillTint() {
        return themeFillTint;
    }

    /**
     * Sets the value of the themeFillTint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThemeFillTint(String value) {
        this.themeFillTint = value;
    }

    /**
     * Gets the value of the themeFillShade property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThemeFillShade() {
        return themeFillShade;
    }

    /**
     * Sets the value of the themeFillShade property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThemeFillShade(String value) {
        this.themeFillShade = value;
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
