
package org.xlsx4j.sml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_AutoSortScope complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_AutoSortScope">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pivotArea" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PivotArea"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_AutoSortScope", propOrder = {
    "pivotArea"
})
public class CTAutoSortScope implements Child
{

    @XmlElement(required = true)
    protected CTPivotArea pivotArea;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pivotArea property.
     * 
     * @return
     *     possible object is
     *     {@link CTPivotArea }
     *     
     */
    public CTPivotArea getPivotArea() {
        return pivotArea;
    }

    /**
     * Sets the value of the pivotArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPivotArea }
     *     
     */
    public void setPivotArea(CTPivotArea value) {
        this.pivotArea = value;
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
