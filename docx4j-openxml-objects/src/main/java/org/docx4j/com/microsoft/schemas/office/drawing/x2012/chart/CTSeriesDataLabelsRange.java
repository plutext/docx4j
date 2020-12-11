
package org.docx4j.com.microsoft.schemas.office.drawing.x2012.chart;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.chart.CTStrData;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SeriesDataLabelsRange complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SeriesDataLabelsRange"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="f" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dlblRangeCache" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_StrData" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SeriesDataLabelsRange", propOrder = {
    "f",
    "dlblRangeCache"
})
public class CTSeriesDataLabelsRange implements Child
{

    @XmlElement(required = true)
    protected String f;
    protected CTStrData dlblRangeCache;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the f property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getF() {
        return f;
    }

    /**
     * Sets the value of the f property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setF(String value) {
        this.f = value;
    }

    /**
     * Gets the value of the dlblRangeCache property.
     * 
     * @return
     *     possible object is
     *     {@link CTStrData }
     *     
     */
    public CTStrData getDlblRangeCache() {
        return dlblRangeCache;
    }

    /**
     * Sets the value of the dlblRangeCache property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStrData }
     *     
     */
    public void setDlblRangeCache(CTStrData value) {
        this.dlblRangeCache = value;
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
