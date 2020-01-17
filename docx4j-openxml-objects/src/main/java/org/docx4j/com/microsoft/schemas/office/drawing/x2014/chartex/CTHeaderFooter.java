
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;;


/**
 * <p>Java class for CT_HeaderFooter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_HeaderFooter"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="oddHeader" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="oddFooter" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="evenHeader" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="evenFooter" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="firstHeader" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="firstFooter" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="alignWithMargins" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="differentOddEven" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="differentFirst" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_HeaderFooter", propOrder = {
    "oddHeader",
    "oddFooter",
    "evenHeader",
    "evenFooter",
    "firstHeader",
    "firstFooter"
})
public class CTHeaderFooter implements Child
{

    protected String oddHeader;
    protected String oddFooter;
    protected String evenHeader;
    protected String evenFooter;
    protected String firstHeader;
    protected String firstFooter;
    @XmlAttribute(name = "alignWithMargins")
    protected Boolean alignWithMargins;
    @XmlAttribute(name = "differentOddEven")
    protected Boolean differentOddEven;
    @XmlAttribute(name = "differentFirst")
    protected Boolean differentFirst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the oddHeader property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOddHeader() {
        return oddHeader;
    }

    /**
     * Sets the value of the oddHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOddHeader(String value) {
        this.oddHeader = value;
    }

    /**
     * Gets the value of the oddFooter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOddFooter() {
        return oddFooter;
    }

    /**
     * Sets the value of the oddFooter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOddFooter(String value) {
        this.oddFooter = value;
    }

    /**
     * Gets the value of the evenHeader property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEvenHeader() {
        return evenHeader;
    }

    /**
     * Sets the value of the evenHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEvenHeader(String value) {
        this.evenHeader = value;
    }

    /**
     * Gets the value of the evenFooter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEvenFooter() {
        return evenFooter;
    }

    /**
     * Sets the value of the evenFooter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEvenFooter(String value) {
        this.evenFooter = value;
    }

    /**
     * Gets the value of the firstHeader property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFirstHeader() {
        return firstHeader;
    }

    /**
     * Sets the value of the firstHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFirstHeader(String value) {
        this.firstHeader = value;
    }

    /**
     * Gets the value of the firstFooter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFirstFooter() {
        return firstFooter;
    }

    /**
     * Sets the value of the firstFooter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFirstFooter(String value) {
        this.firstFooter = value;
    }

    /**
     * Gets the value of the alignWithMargins property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAlignWithMargins() {
        if (alignWithMargins == null) {
            return true;
        } else {
            return alignWithMargins;
        }
    }

    /**
     * Sets the value of the alignWithMargins property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAlignWithMargins(Boolean value) {
        this.alignWithMargins = value;
    }

    /**
     * Gets the value of the differentOddEven property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDifferentOddEven() {
        if (differentOddEven == null) {
            return false;
        } else {
            return differentOddEven;
        }
    }

    /**
     * Sets the value of the differentOddEven property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDifferentOddEven(Boolean value) {
        this.differentOddEven = value;
    }

    /**
     * Gets the value of the differentFirst property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDifferentFirst() {
        if (differentFirst == null) {
            return false;
        } else {
            return differentFirst;
        }
    }

    /**
     * Sets the value of the differentFirst property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDifferentFirst(Boolean value) {
        this.differentFirst = value;
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
