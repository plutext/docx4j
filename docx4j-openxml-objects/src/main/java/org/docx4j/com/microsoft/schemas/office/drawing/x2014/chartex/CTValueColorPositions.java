
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;;


/**
 * <p>Java class for CT_ValueColorPositions complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ValueColorPositions"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="min" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_ValueColorEndPosition" minOccurs="0"/&gt;
 *         &lt;element name="mid" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_ValueColorMiddlePosition" minOccurs="0"/&gt;
 *         &lt;element name="max" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_ValueColorEndPosition" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="count" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_ValueColorPositionCount" default="2" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ValueColorPositions", propOrder = {
    "min",
    "mid",
    "max"
})
public class CTValueColorPositions implements Child
{

    protected CTValueColorEndPosition min;
    protected CTValueColorMiddlePosition mid;
    protected CTValueColorEndPosition max;
    @XmlAttribute(name = "count")
    protected Integer count;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the min property.
     * 
     * @return
     *     possible object is
     *     {@link CTValueColorEndPosition }
     *     
     */
    public CTValueColorEndPosition getMin() {
        return min;
    }

    /**
     * Sets the value of the min property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTValueColorEndPosition }
     *     
     */
    public void setMin(CTValueColorEndPosition value) {
        this.min = value;
    }

    /**
     * Gets the value of the mid property.
     * 
     * @return
     *     possible object is
     *     {@link CTValueColorMiddlePosition }
     *     
     */
    public CTValueColorMiddlePosition getMid() {
        return mid;
    }

    /**
     * Sets the value of the mid property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTValueColorMiddlePosition }
     *     
     */
    public void setMid(CTValueColorMiddlePosition value) {
        this.mid = value;
    }

    /**
     * Gets the value of the max property.
     * 
     * @return
     *     possible object is
     *     {@link CTValueColorEndPosition }
     *     
     */
    public CTValueColorEndPosition getMax() {
        return max;
    }

    /**
     * Sets the value of the max property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTValueColorEndPosition }
     *     
     */
    public void setMax(CTValueColorEndPosition value) {
        this.max = value;
    }

    /**
     * Gets the value of the count property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getCount() {
        if (count == null) {
            return  2;
        } else {
            return count;
        }
    }

    /**
     * Sets the value of the count property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCount(Integer value) {
        this.count = value;
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
