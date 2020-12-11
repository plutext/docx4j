
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;;


/**
 * <p>Java class for CT_ValueAxisScaling complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ValueAxisScaling"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="max" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_DoubleOrAutomatic" /&gt;
 *       &lt;attribute name="min" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_DoubleOrAutomatic" /&gt;
 *       &lt;attribute name="majorUnit" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_ValueAxisUnit" /&gt;
 *       &lt;attribute name="minorUnit" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_ValueAxisUnit" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ValueAxisScaling")
public class CTValueAxisScaling implements Child
{

    @XmlAttribute(name = "max")
    protected String max;
    @XmlAttribute(name = "min")
    protected String min;
    @XmlAttribute(name = "majorUnit")
    protected String majorUnit;
    @XmlAttribute(name = "minorUnit")
    protected String minorUnit;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the max property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMax() {
        return max;
    }

    /**
     * Sets the value of the max property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMax(String value) {
        this.max = value;
    }

    /**
     * Gets the value of the min property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMin() {
        return min;
    }

    /**
     * Sets the value of the min property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMin(String value) {
        this.min = value;
    }

    /**
     * Gets the value of the majorUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMajorUnit() {
        return majorUnit;
    }

    /**
     * Sets the value of the majorUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMajorUnit(String value) {
        this.majorUnit = value;
    }

    /**
     * Gets the value of the minorUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinorUnit() {
        return minorUnit;
    }

    /**
     * Sets the value of the minorUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinorUnit(String value) {
        this.minorUnit = value;
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
