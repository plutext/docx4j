
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_GeoDataPointToEntityQueryResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GeoDataPointToEntityQueryResult"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="geoDataPointQuery" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoDataPointQuery" minOccurs="0"/&gt;
 *         &lt;element name="geoDataPointToEntityQuery" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoDataPointToEntityQuery" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GeoDataPointToEntityQueryResult", propOrder = {
    "geoDataPointQuery",
    "geoDataPointToEntityQuery"
})
public class CTGeoDataPointToEntityQueryResult {

    protected CTGeoDataPointQuery geoDataPointQuery;
    protected CTGeoDataPointToEntityQuery geoDataPointToEntityQuery;

    /**
     * Gets the value of the geoDataPointQuery property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeoDataPointQuery }
     *     
     */
    public CTGeoDataPointQuery getGeoDataPointQuery() {
        return geoDataPointQuery;
    }

    /**
     * Sets the value of the geoDataPointQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeoDataPointQuery }
     *     
     */
    public void setGeoDataPointQuery(CTGeoDataPointQuery value) {
        this.geoDataPointQuery = value;
    }

    /**
     * Gets the value of the geoDataPointToEntityQuery property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeoDataPointToEntityQuery }
     *     
     */
    public CTGeoDataPointToEntityQuery getGeoDataPointToEntityQuery() {
        return geoDataPointToEntityQuery;
    }

    /**
     * Sets the value of the geoDataPointToEntityQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeoDataPointToEntityQuery }
     *     
     */
    public void setGeoDataPointToEntityQuery(CTGeoDataPointToEntityQuery value) {
        this.geoDataPointToEntityQuery = value;
    }

}
