
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ColorMappingOverride complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ColorMappingOverride">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="masterClrMapping" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_EmptyElement"/>
 *           &lt;element name="overrideClrMapping" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ColorMapping"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ColorMappingOverride", propOrder = {
    "masterClrMapping",
    "overrideClrMapping"
})
public class CTColorMappingOverride
    implements Child
{

    protected CTEmptyElement masterClrMapping;
    protected CTColorMapping overrideClrMapping;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the masterClrMapping property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmptyElement }
     *     
     */
    public CTEmptyElement getMasterClrMapping() {
        return masterClrMapping;
    }

    /**
     * Sets the value of the masterClrMapping property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmptyElement }
     *     
     */
    public void setMasterClrMapping(CTEmptyElement value) {
        this.masterClrMapping = value;
    }

    /**
     * Gets the value of the overrideClrMapping property.
     * 
     * @return
     *     possible object is
     *     {@link CTColorMapping }
     *     
     */
    public CTColorMapping getOverrideClrMapping() {
        return overrideClrMapping;
    }

    /**
     * Sets the value of the overrideClrMapping property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColorMapping }
     *     
     */
    public void setOverrideClrMapping(CTColorMapping value) {
        this.overrideClrMapping = value;
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
