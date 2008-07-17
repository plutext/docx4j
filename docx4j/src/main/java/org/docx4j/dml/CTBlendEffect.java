
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_BlendEffect complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_BlendEffect">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cont" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_EffectContainer"/>
 *       &lt;/sequence>
 *       &lt;attribute name="blend" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_BlendMode" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_BlendEffect", propOrder = {
    "cont"
})
public class CTBlendEffect implements Child
{

    @XmlElement(required = true)
    protected CTEffectContainer cont;
    @XmlAttribute(required = true)
    protected STBlendMode blend;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the cont property.
     * 
     * @return
     *     possible object is
     *     {@link CTEffectContainer }
     *     
     */
    public CTEffectContainer getCont() {
        return cont;
    }

    /**
     * Sets the value of the cont property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEffectContainer }
     *     
     */
    public void setCont(CTEffectContainer value) {
        this.cont = value;
    }

    /**
     * Gets the value of the blend property.
     * 
     * @return
     *     possible object is
     *     {@link STBlendMode }
     *     
     */
    public STBlendMode getBlend() {
        return blend;
    }

    /**
     * Sets the value of the blend property.
     * 
     * @param value
     *     allowed object is
     *     {@link STBlendMode }
     *     
     */
    public void setBlend(STBlendMode value) {
        this.blend = value;
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
