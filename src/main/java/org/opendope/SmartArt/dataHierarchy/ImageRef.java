
package org.opendope.SmartArt.dataHierarchy;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="contentRef" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="custLinFactNeighborX" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="custLinFactNeighborY" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="custScaleY" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="custScaleX" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "imageRef")
public class ImageRef {

    @XmlAttribute
    protected String contentRef;
    @XmlAttribute
    protected Integer custLinFactNeighborX;
    @XmlAttribute
    protected Integer custLinFactNeighborY;
    @XmlAttribute
    protected Integer custScaleY;
    @XmlAttribute
    protected Integer custScaleX;

    /**
     * Gets the value of the contentRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentRef() {
        return contentRef;
    }

    /**
     * Sets the value of the contentRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentRef(String value) {
        this.contentRef = value;
    }

    /**
     * Gets the value of the custLinFactNeighborX property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCustLinFactNeighborX() {
        return custLinFactNeighborX;
    }

    /**
     * Sets the value of the custLinFactNeighborX property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCustLinFactNeighborX(Integer value) {
        this.custLinFactNeighborX = value;
    }

    /**
     * Gets the value of the custLinFactNeighborY property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCustLinFactNeighborY() {
        return custLinFactNeighborY;
    }

    /**
     * Sets the value of the custLinFactNeighborY property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCustLinFactNeighborY(Integer value) {
        this.custLinFactNeighborY = value;
    }

    /**
     * Gets the value of the custScaleY property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCustScaleY() {
        return custScaleY;
    }

    /**
     * Sets the value of the custScaleY property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCustScaleY(Integer value) {
        this.custScaleY = value;
    }

    /**
     * Gets the value of the custScaleX property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCustScaleX() {
        return custScaleX;
    }

    /**
     * Sets the value of the custScaleX property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCustScaleX(Integer value) {
        this.custScaleX = value;
    }

}
