
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_GeoDataEntityQueryResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GeoDataEntityQueryResult"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="geoDataEntityQuery" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoDataEntityQuery" minOccurs="0"/&gt;
 *         &lt;element name="geoData" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoData" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GeoDataEntityQueryResult", propOrder = {
    "geoDataEntityQuery",
    "geoData"
})
public class CTGeoDataEntityQueryResult {

    protected CTGeoDataEntityQuery geoDataEntityQuery;
    protected CTGeoData geoData;

    /**
     * Gets the value of the geoDataEntityQuery property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeoDataEntityQuery }
     *     
     */
    public CTGeoDataEntityQuery getGeoDataEntityQuery() {
        return geoDataEntityQuery;
    }

    /**
     * Sets the value of the geoDataEntityQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeoDataEntityQuery }
     *     
     */
    public void setGeoDataEntityQuery(CTGeoDataEntityQuery value) {
        this.geoDataEntityQuery = value;
    }

    /**
     * Gets the value of the geoData property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeoData }
     *     
     */
    public CTGeoData getGeoData() {
        return geoData;
    }

    /**
     * Sets the value of the geoData property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeoData }
     *     
     */
    public void setGeoData(CTGeoData value) {
        this.geoData = value;
    }

}
