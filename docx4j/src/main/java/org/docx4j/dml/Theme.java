
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="themeElements" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_BaseStyles"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" default="" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "themeElements"
})
@XmlRootElement(name = "theme")
public class Theme {

    @XmlElement(required = true)
    protected BaseStyles themeElements;
    @XmlAttribute
    protected String name;

    /**
     * Gets the value of the themeElements property.
     * 
     * @return
     *     possible object is
     *     {@link BaseStyles }
     *     
     */
    public BaseStyles getThemeElements() {
        return themeElements;
    }

    /**
     * Sets the value of the themeElements property.
     * 
     * @param value
     *     allowed object is
     *     {@link BaseStyles }
     *     
     */
    public void setThemeElements(BaseStyles value) {
        this.themeElements = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        if (name == null) {
            return "";
        } else {
            return name;
        }
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

}
