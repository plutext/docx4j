
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ClipboardStyleSheet complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ClipboardStyleSheet">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="themeElements" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_BaseStyles"/>
 *         &lt;element name="clrMap" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ColorMapping"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ClipboardStyleSheet", propOrder = {
    "themeElements",
    "clrMap"
})
public class CTClipboardStyleSheet
    implements Child
{

    @XmlElement(required = true)
    protected BaseStyles themeElements;
    @XmlElement(required = true)
    protected CTColorMapping clrMap;
    @XmlTransient
    private Object parent;

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
     * Gets the value of the clrMap property.
     * 
     * @return
     *     possible object is
     *     {@link CTColorMapping }
     *     
     */
    public CTColorMapping getClrMap() {
        return clrMap;
    }

    /**
     * Sets the value of the clrMap property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColorMapping }
     *     
     */
    public void setClrMap(CTColorMapping value) {
        this.clrMap = value;
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
