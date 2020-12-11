
package org.docx4j.docProps.coverPageProps;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *       &lt;sequence>
 *         &lt;element name="PublishDate" type="{http://schemas.microsoft.com/\u200coffice/\u200c2006/\u200ccoverPageProps}ST_PublishDate"/>
 *         &lt;element name="Abstract" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CompanyAddress" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CompanyPhone" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CompanyFax" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CompanyEmail" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "publishDate",
    "_abstract",
    "companyAddress",
    "companyPhone",
    "companyFax",
    "companyEmail"
})
@XmlRootElement(name = "CoverPageProperties")
public class CoverPageProperties {

    @XmlElement(name = "PublishDate", required = true)
    protected String publishDate;
    @XmlElement(name = "Abstract", required = true)
    protected String _abstract;
    @XmlElement(name = "CompanyAddress", required = true)
    protected String companyAddress;
    @XmlElement(name = "CompanyPhone", required = true)
    protected String companyPhone;
    @XmlElement(name = "CompanyFax", required = true)
    protected String companyFax;
    @XmlElement(name = "CompanyEmail", required = true)
    protected String companyEmail;

    /**
     * Gets the value of the publishDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPublishDate() {
        return publishDate;
    }

    /**
     * Sets the value of the publishDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPublishDate(String value) {
        this.publishDate = value;
    }

    /**
     * Gets the value of the abstract property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAbstract() {
        return _abstract;
    }

    /**
     * Sets the value of the abstract property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAbstract(String value) {
        this._abstract = value;
    }

    /**
     * Gets the value of the companyAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompanyAddress() {
        return companyAddress;
    }

    /**
     * Sets the value of the companyAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompanyAddress(String value) {
        this.companyAddress = value;
    }

    /**
     * Gets the value of the companyPhone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompanyPhone() {
        return companyPhone;
    }

    /**
     * Sets the value of the companyPhone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompanyPhone(String value) {
        this.companyPhone = value;
    }

    /**
     * Gets the value of the companyFax property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompanyFax() {
        return companyFax;
    }

    /**
     * Sets the value of the companyFax property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompanyFax(String value) {
        this.companyFax = value;
    }

    /**
     * Gets the value of the companyEmail property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompanyEmail() {
        return companyEmail;
    }

    /**
     * Sets the value of the companyEmail property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompanyEmail(String value) {
        this.companyEmail = value;
    }

}
