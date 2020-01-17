
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;;


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
public class CTGeoLocationQueryResult implements Child
{

    protected CTGeoLocationQuery geoLocationQuery;
    protected CTGeoLocations geoLocations;
    @XmlTransient
    private Object parent;

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
