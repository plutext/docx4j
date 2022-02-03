
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_GeoChildEntitiesQueryResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GeoChildEntitiesQueryResult"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="geoChildEntitiesQuery" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoChildEntitiesQuery" minOccurs="0"/&gt;
 *         &lt;element name="geoChildEntities" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoChildEntities" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GeoChildEntitiesQueryResult", propOrder = {
    "geoChildEntitiesQuery",
    "geoChildEntities"
})
public class CTGeoChildEntitiesQueryResult {

    protected CTGeoChildEntitiesQuery geoChildEntitiesQuery;
    protected CTGeoChildEntities geoChildEntities;

    /**
     * Gets the value of the geoChildEntitiesQuery property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeoChildEntitiesQuery }
     *     
     */
    public CTGeoChildEntitiesQuery getGeoChildEntitiesQuery() {
        return geoChildEntitiesQuery;
    }

    /**
     * Sets the value of the geoChildEntitiesQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeoChildEntitiesQuery }
     *     
     */
    public void setGeoChildEntitiesQuery(CTGeoChildEntitiesQuery value) {
        this.geoChildEntitiesQuery = value;
    }

    /**
     * Gets the value of the geoChildEntities property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeoChildEntities }
     *     
     */
    public CTGeoChildEntities getGeoChildEntities() {
        return geoChildEntities;
    }

    /**
     * Sets the value of the geoChildEntities property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeoChildEntities }
     *     
     */
    public void setGeoChildEntities(CTGeoChildEntities value) {
        this.geoChildEntities = value;
    }

}
