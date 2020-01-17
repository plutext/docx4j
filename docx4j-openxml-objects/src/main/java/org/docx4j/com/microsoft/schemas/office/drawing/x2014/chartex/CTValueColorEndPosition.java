
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;;


/**
 * <p>Java class for CT_ValueColorEndPosition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ValueColorEndPosition"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="extremeValue" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_ExtremeValueColorPosition"/&gt;
 *         &lt;element name="number" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_NumberColorPosition"/&gt;
 *         &lt;element name="percent" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_PercentageColorPosition"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ValueColorEndPosition", propOrder = {
    "extremeValue",
    "number",
    "percent"
})
public class CTValueColorEndPosition implements Child
{

    protected CTExtremeValueColorPosition extremeValue;
    protected CTNumberColorPosition number;
    protected CTPercentageColorPosition percent;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the extremeValue property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtremeValueColorPosition }
     *     
     */
    public CTExtremeValueColorPosition getExtremeValue() {
        return extremeValue;
    }

    /**
     * Sets the value of the extremeValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtremeValueColorPosition }
     *     
     */
    public void setExtremeValue(CTExtremeValueColorPosition value) {
        this.extremeValue = value;
    }

    /**
     * Gets the value of the number property.
     * 
     * @return
     *     possible object is
     *     {@link CTNumberColorPosition }
     *     
     */
    public CTNumberColorPosition getNumber() {
        return number;
    }

    /**
     * Sets the value of the number property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNumberColorPosition }
     *     
     */
    public void setNumber(CTNumberColorPosition value) {
        this.number = value;
    }

    /**
     * Gets the value of the percent property.
     * 
     * @return
     *     possible object is
     *     {@link CTPercentageColorPosition }
     *     
     */
    public CTPercentageColorPosition getPercent() {
        return percent;
    }

    /**
     * Sets the value of the percent property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPercentageColorPosition }
     *     
     */
    public void setPercent(CTPercentageColorPosition value) {
        this.percent = value;
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
