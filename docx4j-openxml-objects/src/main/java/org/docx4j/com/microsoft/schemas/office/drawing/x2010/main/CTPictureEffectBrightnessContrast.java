
package org.docx4j.com.microsoft.schemas.office.drawing.x2010.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PictureEffectBrightnessContrast complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PictureEffectBrightnessContrast"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="bright" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_FixedPercentage" default="0" /&gt;
 *       &lt;attribute name="contrast" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_FixedPercentage" default="0" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PictureEffectBrightnessContrast")
public class CTPictureEffectBrightnessContrast implements Child
{

    @XmlAttribute(name = "bright")
    protected Integer bright;
    @XmlAttribute(name = "contrast")
    protected Integer contrast;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the bright property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getBright() {
        if (bright == null) {
            return  0;
        } else {
            return bright;
        }
    }

    /**
     * Sets the value of the bright property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setBright(Integer value) {
        this.bright = value;
    }

    /**
     * Gets the value of the contrast property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getContrast() {
        if (contrast == null) {
            return  0;
        } else {
            return contrast;
        }
    }

    /**
     * Sets the value of the contrast property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setContrast(Integer value) {
        this.contrast = value;
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
