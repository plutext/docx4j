
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;;


/**
 * <p>Java class for CT_Binning complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Binning"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice minOccurs="0"&gt;
 *         &lt;element name="binSize" type="{http://www.w3.org/2001/XMLSchema}double"/&gt;
 *         &lt;element name="binCount" type="{http://www.w3.org/2001/XMLSchema}unsignedInt"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="intervalClosed" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_IntervalClosedSide" /&gt;
 *       &lt;attribute name="underflow" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_DoubleOrAutomatic" /&gt;
 *       &lt;attribute name="overflow" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_DoubleOrAutomatic" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Binning", propOrder = {
    "binSize",
    "binCount"
})
public class CTBinning implements Child
{

    protected Double binSize;
    @XmlSchemaType(name = "unsignedInt")
    protected Long binCount;
    @XmlAttribute(name = "intervalClosed")
    protected STIntervalClosedSide intervalClosed;
    @XmlAttribute(name = "underflow")
    protected String underflow;
    @XmlAttribute(name = "overflow")
    protected String overflow;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the binSize property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getBinSize() {
        return binSize;
    }

    /**
     * Sets the value of the binSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setBinSize(Double value) {
        this.binSize = value;
    }

    /**
     * Gets the value of the binCount property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getBinCount() {
        return binCount;
    }

    /**
     * Sets the value of the binCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setBinCount(Long value) {
        this.binCount = value;
    }

    /**
     * Gets the value of the intervalClosed property.
     * 
     * @return
     *     possible object is
     *     {@link STIntervalClosedSide }
     *     
     */
    public STIntervalClosedSide getIntervalClosed() {
        return intervalClosed;
    }

    /**
     * Sets the value of the intervalClosed property.
     * 
     * @param value
     *     allowed object is
     *     {@link STIntervalClosedSide }
     *     
     */
    public void setIntervalClosed(STIntervalClosedSide value) {
        this.intervalClosed = value;
    }

    /**
     * Gets the value of the underflow property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnderflow() {
        return underflow;
    }

    /**
     * Sets the value of the underflow property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnderflow(String value) {
        this.underflow = value;
    }

    /**
     * Gets the value of the overflow property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOverflow() {
        return overflow;
    }

    /**
     * Sets the value of the overflow property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOverflow(String value) {
        this.overflow = value;
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
