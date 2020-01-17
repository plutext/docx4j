
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;;


/**
 * <p>Java class for CT_Address complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Address"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="address1" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="countryRegion" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="adminDistrict1" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="adminDistrict2" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="postalCode" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="locality" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="isoCountryCode" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Address")
public class CTAddress implements Child
{

    @XmlAttribute(name = "address1")
    protected String address1;
    @XmlAttribute(name = "countryRegion")
    protected String countryRegion;
    @XmlAttribute(name = "adminDistrict1")
    protected String adminDistrict1;
    @XmlAttribute(name = "adminDistrict2")
    protected String adminDistrict2;
    @XmlAttribute(name = "postalCode")
    protected String postalCode;
    @XmlAttribute(name = "locality")
    protected String locality;
    @XmlAttribute(name = "isoCountryCode")
    protected String isoCountryCode;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the address1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddress1() {
        return address1;
    }

    /**
     * Sets the value of the address1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddress1(String value) {
        this.address1 = value;
    }

    /**
     * Gets the value of the countryRegion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountryRegion() {
        return countryRegion;
    }

    /**
     * Sets the value of the countryRegion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountryRegion(String value) {
        this.countryRegion = value;
    }

    /**
     * Gets the value of the adminDistrict1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdminDistrict1() {
        return adminDistrict1;
    }

    /**
     * Sets the value of the adminDistrict1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdminDistrict1(String value) {
        this.adminDistrict1 = value;
    }

    /**
     * Gets the value of the adminDistrict2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdminDistrict2() {
        return adminDistrict2;
    }

    /**
     * Sets the value of the adminDistrict2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdminDistrict2(String value) {
        this.adminDistrict2 = value;
    }

    /**
     * Gets the value of the postalCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the value of the postalCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPostalCode(String value) {
        this.postalCode = value;
    }

    /**
     * Gets the value of the locality property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocality() {
        return locality;
    }

    /**
     * Sets the value of the locality property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocality(String value) {
        this.locality = value;
    }

    /**
     * Gets the value of the isoCountryCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsoCountryCode() {
        return isoCountryCode;
    }

    /**
     * Sets the value of the isoCountryCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsoCountryCode(String value) {
        this.isoCountryCode = value;
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
