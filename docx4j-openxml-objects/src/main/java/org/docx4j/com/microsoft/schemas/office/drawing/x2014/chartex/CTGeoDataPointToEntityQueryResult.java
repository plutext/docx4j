
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;;


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
public class CTGeoDataPointToEntityQueryResult implements Child
{

    protected CTGeoDataPointQuery geoDataPointQuery;
    protected CTGeoDataPointToEntityQuery geoDataPointToEntityQuery;
    @XmlTransient
    private Object parent;

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
