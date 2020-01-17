
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;;


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
public class CTGeoDataEntityQueryResult implements Child
{

    protected CTGeoDataEntityQuery geoDataEntityQuery;
    protected CTGeoData geoData;
    @XmlTransient
    private Object parent;

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
