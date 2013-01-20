
package org.xlsx4j.sml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Top10 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Top10">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="top" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="percent" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="val" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="filterVal" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Top10")
public class CTTop10 implements Child
{

    @XmlAttribute(name = "top")
    protected Boolean top;
    @XmlAttribute(name = "percent")
    protected Boolean percent;
    @XmlAttribute(name = "val", required = true)
    protected double val;
    @XmlAttribute(name = "filterVal")
    protected Double filterVal;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the top property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTop() {
        if (top == null) {
            return true;
        } else {
            return top;
        }
    }

    /**
     * Sets the value of the top property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTop(Boolean value) {
        this.top = value;
    }

    /**
     * Gets the value of the percent property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPercent() {
        if (percent == null) {
            return false;
        } else {
            return percent;
        }
    }

    /**
     * Sets the value of the percent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPercent(Boolean value) {
        this.percent = value;
    }

    /**
     * Gets the value of the val property.
     * 
     */
    public double getVal() {
        return val;
    }

    /**
     * Sets the value of the val property.
     * 
     */
    public void setVal(double value) {
        this.val = value;
    }

    /**
     * Gets the value of the filterVal property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getFilterVal() {
        return filterVal;
    }

    /**
     * Sets the value of the filterVal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setFilterVal(Double value) {
        this.filterVal = value;
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
