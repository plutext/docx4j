
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chart;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ChartDataPointUniqueIDMapEntry complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ChartDataPointUniqueIDMapEntry"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ptidx" type="{http://www.w3.org/2001/XMLSchema}unsignedInt"/&gt;
 *         &lt;element name="uniqueID" type="{http://schemas.microsoft.com/office/drawing/2014/chart}CT_ChartUniqueID"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ChartDataPointUniqueIDMapEntry", propOrder = {
    "ptidx",
    "uniqueID"
})
public class CTChartDataPointUniqueIDMapEntry implements Child
{

    @XmlSchemaType(name = "unsignedInt")
    protected long ptidx;
    @XmlElement(required = true)
    protected CTChartUniqueID uniqueID;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the ptidx property.
     * 
     */
    public long getPtidx() {
        return ptidx;
    }

    /**
     * Sets the value of the ptidx property.
     * 
     */
    public void setPtidx(long value) {
        this.ptidx = value;
    }

    /**
     * Gets the value of the uniqueID property.
     * 
     * @return
     *     possible object is
     *     {@link CTChartUniqueID }
     *     
     */
    public CTChartUniqueID getUniqueID() {
        return uniqueID;
    }

    /**
     * Sets the value of the uniqueID property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTChartUniqueID }
     *     
     */
    public void setUniqueID(CTChartUniqueID value) {
        this.uniqueID = value;
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
