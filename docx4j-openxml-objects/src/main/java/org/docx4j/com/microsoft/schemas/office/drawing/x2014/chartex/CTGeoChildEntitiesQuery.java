
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_GeoChildEntitiesQuery complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GeoChildEntitiesQuery"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="geoChildTypes" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoChildTypes" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="entityId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GeoChildEntitiesQuery", propOrder = {
    "geoChildTypes"
})
public class CTGeoChildEntitiesQuery {

    protected CTGeoChildTypes geoChildTypes;
    @XmlAttribute(name = "entityId", required = true)
    protected String entityId;

    /**
     * Gets the value of the geoChildTypes property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeoChildTypes }
     *     
     */
    public CTGeoChildTypes getGeoChildTypes() {
        return geoChildTypes;
    }

    /**
     * Sets the value of the geoChildTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeoChildTypes }
     *     
     */
    public void setGeoChildTypes(CTGeoChildTypes value) {
        this.geoChildTypes = value;
    }

    /**
     * Gets the value of the entityId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Sets the value of the entityId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEntityId(String value) {
        this.entityId = value;
    }

}
