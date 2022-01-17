
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Statistics complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Statistics"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="quartileMethod" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_QuartileMethod" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Statistics")
public class CTStatistics {

    @XmlAttribute(name = "quartileMethod")
    protected STQuartileMethod quartileMethod;

    /**
     * Gets the value of the quartileMethod property.
     * 
     * @return
     *     possible object is
     *     {@link STQuartileMethod }
     *     
     */
    public STQuartileMethod getQuartileMethod() {
        return quartileMethod;
    }

    /**
     * Sets the value of the quartileMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link STQuartileMethod }
     *     
     */
    public void setQuartileMethod(STQuartileMethod value) {
        this.quartileMethod = value;
    }

}
