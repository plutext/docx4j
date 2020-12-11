
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;;


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
public class CTGeoChildEntitiesQueryResult implements Child
{

    protected CTGeoChildEntitiesQuery geoChildEntitiesQuery;
    protected CTGeoChildEntities geoChildEntities;
    @XmlTransient
    private Object parent;

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
