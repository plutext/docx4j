
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;;


/**
 * <p>Java class for CT_Clear complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Clear"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="geoLocationQueryResults" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoLocationQueryResults" minOccurs="0"/&gt;
 *         &lt;element name="geoDataEntityQueryResults" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoDataEntityQueryResults" minOccurs="0"/&gt;
 *         &lt;element name="geoDataPointToEntityQueryResults" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoDataPointToEntityQueryResults" minOccurs="0"/&gt;
 *         &lt;element name="geoChildEntitiesQueryResults" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoChildEntitiesQueryResults" minOccurs="0"/&gt;
 *         &lt;element name="geoParentEntitiesQueryResults" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoParentEntitiesQueryResults" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Clear", propOrder = {
    "geoLocationQueryResults",
    "geoDataEntityQueryResults",
    "geoDataPointToEntityQueryResults",
    "geoChildEntitiesQueryResults",
    "geoParentEntitiesQueryResults"
})
public class CTClear implements Child
{

    protected CTGeoLocationQueryResults geoLocationQueryResults;
    protected CTGeoDataEntityQueryResults geoDataEntityQueryResults;
    protected CTGeoDataPointToEntityQueryResults geoDataPointToEntityQueryResults;
    protected CTGeoChildEntitiesQueryResults geoChildEntitiesQueryResults;
    protected CTGeoParentEntitiesQueryResults geoParentEntitiesQueryResults;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the geoLocationQueryResults property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeoLocationQueryResults }
     *     
     */
    public CTGeoLocationQueryResults getGeoLocationQueryResults() {
        return geoLocationQueryResults;
    }

    /**
     * Sets the value of the geoLocationQueryResults property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeoLocationQueryResults }
     *     
     */
    public void setGeoLocationQueryResults(CTGeoLocationQueryResults value) {
        this.geoLocationQueryResults = value;
    }

    /**
     * Gets the value of the geoDataEntityQueryResults property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeoDataEntityQueryResults }
     *     
     */
    public CTGeoDataEntityQueryResults getGeoDataEntityQueryResults() {
        return geoDataEntityQueryResults;
    }

    /**
     * Sets the value of the geoDataEntityQueryResults property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeoDataEntityQueryResults }
     *     
     */
    public void setGeoDataEntityQueryResults(CTGeoDataEntityQueryResults value) {
        this.geoDataEntityQueryResults = value;
    }

    /**
     * Gets the value of the geoDataPointToEntityQueryResults property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeoDataPointToEntityQueryResults }
     *     
     */
    public CTGeoDataPointToEntityQueryResults getGeoDataPointToEntityQueryResults() {
        return geoDataPointToEntityQueryResults;
    }

    /**
     * Sets the value of the geoDataPointToEntityQueryResults property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeoDataPointToEntityQueryResults }
     *     
     */
    public void setGeoDataPointToEntityQueryResults(CTGeoDataPointToEntityQueryResults value) {
        this.geoDataPointToEntityQueryResults = value;
    }

    /**
     * Gets the value of the geoChildEntitiesQueryResults property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeoChildEntitiesQueryResults }
     *     
     */
    public CTGeoChildEntitiesQueryResults getGeoChildEntitiesQueryResults() {
        return geoChildEntitiesQueryResults;
    }

    /**
     * Sets the value of the geoChildEntitiesQueryResults property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeoChildEntitiesQueryResults }
     *     
     */
    public void setGeoChildEntitiesQueryResults(CTGeoChildEntitiesQueryResults value) {
        this.geoChildEntitiesQueryResults = value;
    }

    /**
     * Gets the value of the geoParentEntitiesQueryResults property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeoParentEntitiesQueryResults }
     *     
     */
    public CTGeoParentEntitiesQueryResults getGeoParentEntitiesQueryResults() {
        return geoParentEntitiesQueryResults;
    }

    /**
     * Sets the value of the geoParentEntitiesQueryResults property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeoParentEntitiesQueryResults }
     *     
     */
    public void setGeoParentEntitiesQueryResults(CTGeoParentEntitiesQueryResults value) {
        this.geoParentEntitiesQueryResults = value;
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
