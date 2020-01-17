
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;;


/**
 * <p>Java class for CT_AxisUnits complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_AxisUnits"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="unitsLabel" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_AxisUnitsLabel" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="unit" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_AxisUnit" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_AxisUnits", propOrder = {
    "unitsLabel",
    "extLst"
})
public class CTAxisUnits implements Child
{

    protected CTAxisUnitsLabel unitsLabel;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "unit")
    protected STAxisUnit unit;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the unitsLabel property.
     * 
     * @return
     *     possible object is
     *     {@link CTAxisUnitsLabel }
     *     
     */
    public CTAxisUnitsLabel getUnitsLabel() {
        return unitsLabel;
    }

    /**
     * Sets the value of the unitsLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAxisUnitsLabel }
     *     
     */
    public void setUnitsLabel(CTAxisUnitsLabel value) {
        this.unitsLabel = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the unit property.
     * 
     * @return
     *     possible object is
     *     {@link STAxisUnit }
     *     
     */
    public STAxisUnit getUnit() {
        return unit;
    }

    /**
     * Sets the value of the unit property.
     * 
     * @param value
     *     allowed object is
     *     {@link STAxisUnit }
     *     
     */
    public void setUnit(STAxisUnit value) {
        this.unit = value;
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
