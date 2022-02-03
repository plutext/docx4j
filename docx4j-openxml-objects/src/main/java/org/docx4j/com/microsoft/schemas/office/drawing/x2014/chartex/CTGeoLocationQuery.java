
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_GeoLocationQuery complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GeoLocationQuery"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="countryRegion" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="adminDistrict1" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="adminDistrict2" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="postalCode" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="entityType" use="required" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_EntityType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GeoLocationQuery")
public class CTGeoLocationQuery {

    @XmlAttribute(name = "countryRegion")
    protected String countryRegion;
    @XmlAttribute(name = "adminDistrict1")
    protected String adminDistrict1;
    @XmlAttribute(name = "adminDistrict2")
    protected String adminDistrict2;
    @XmlAttribute(name = "postalCode")
    protected String postalCode;
    @XmlAttribute(name = "entityType", required = true)
    protected STEntityType entityType;

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
     * Gets the value of the entityType property.
     * 
     * @return
     *     possible object is
     *     {@link STEntityType }
     *     
     */
    public STEntityType getEntityType() {
        return entityType;
    }

    /**
     * Sets the value of the entityType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STEntityType }
     *     
     */
    public void setEntityType(STEntityType value) {
        this.entityType = value;
    }

}
