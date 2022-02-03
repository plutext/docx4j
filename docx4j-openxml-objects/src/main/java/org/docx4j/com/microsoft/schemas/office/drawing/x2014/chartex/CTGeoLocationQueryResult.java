
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_GeoLocationQueryResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GeoLocationQueryResult"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="geoLocationQuery" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoLocationQuery" minOccurs="0"/&gt;
 *         &lt;element name="geoLocations" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoLocations" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GeoLocationQueryResult", propOrder = {
    "geoLocationQuery",
    "geoLocations"
})
public class CTGeoLocationQueryResult {

    protected CTGeoLocationQuery geoLocationQuery;
    protected CTGeoLocations geoLocations;

    /**
     * Gets the value of the geoLocationQuery property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeoLocationQuery }
     *     
     */
    public CTGeoLocationQuery getGeoLocationQuery() {
        return geoLocationQuery;
    }

    /**
     * Sets the value of the geoLocationQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeoLocationQuery }
     *     
     */
    public void setGeoLocationQuery(CTGeoLocationQuery value) {
        this.geoLocationQuery = value;
    }

    /**
     * Gets the value of the geoLocations property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeoLocations }
     *     
     */
    public CTGeoLocations getGeoLocations() {
        return geoLocations;
    }

    /**
     * Sets the value of the geoLocations property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeoLocations }
     *     
     */
    public void setGeoLocations(CTGeoLocations value) {
        this.geoLocations = value;
    }

}
