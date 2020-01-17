
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chart;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.chart.CTMultiLvlStrData;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_MultiLvlStrFilteredLiteralCache complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_MultiLvlStrFilteredLiteralCache"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="multiLvlStrCache" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_MultiLvlStrData"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_MultiLvlStrFilteredLiteralCache", propOrder = {
    "multiLvlStrCache"
})
public class CTMultiLvlStrFilteredLiteralCache implements Child
{

    @XmlElement(required = true)
    protected CTMultiLvlStrData multiLvlStrCache;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the multiLvlStrCache property.
     * 
     * @return
     *     possible object is
     *     {@link CTMultiLvlStrData }
     *     
     */
    public CTMultiLvlStrData getMultiLvlStrCache() {
        return multiLvlStrCache;
    }

    /**
     * Sets the value of the multiLvlStrCache property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMultiLvlStrData }
     *     
     */
    public void setMultiLvlStrCache(CTMultiLvlStrData value) {
        this.multiLvlStrCache = value;
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
