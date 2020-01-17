
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.ppp.Child;;


/**
 * <p>Java class for CT_Geography complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Geography"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="geoCache" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoCache" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="projectionType" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_GeoProjectionType" /&gt;
 *       &lt;attribute name="viewedRegionType" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}ST_GeoMappingLevel" /&gt;
 *       &lt;attribute name="cultureLanguage" use="required" type="{http://www.w3.org/2001/XMLSchema}language" /&gt;
 *       &lt;attribute name="cultureRegion" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="attribution" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Geography", propOrder = {
    "geoCache"
})
public class CTGeography implements Child
{

    protected CTGeoCache geoCache;
    @XmlAttribute(name = "projectionType")
    protected STGeoProjectionType projectionType;
    @XmlAttribute(name = "viewedRegionType")
    protected STGeoMappingLevel viewedRegionType;
    @XmlAttribute(name = "cultureLanguage", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "language")
    protected String cultureLanguage;
    @XmlAttribute(name = "cultureRegion", required = true)
    protected String cultureRegion;
    @XmlAttribute(name = "attribution", required = true)
    protected String attribution;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the geoCache property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeoCache }
     *     
     */
    public CTGeoCache getGeoCache() {
        return geoCache;
    }

    /**
     * Sets the value of the geoCache property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeoCache }
     *     
     */
    public void setGeoCache(CTGeoCache value) {
        this.geoCache = value;
    }

    /**
     * Gets the value of the projectionType property.
     * 
     * @return
     *     possible object is
     *     {@link STGeoProjectionType }
     *     
     */
    public STGeoProjectionType getProjectionType() {
        return projectionType;
    }

    /**
     * Sets the value of the projectionType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STGeoProjectionType }
     *     
     */
    public void setProjectionType(STGeoProjectionType value) {
        this.projectionType = value;
    }

    /**
     * Gets the value of the viewedRegionType property.
     * 
     * @return
     *     possible object is
     *     {@link STGeoMappingLevel }
     *     
     */
    public STGeoMappingLevel getViewedRegionType() {
        return viewedRegionType;
    }

    /**
     * Sets the value of the viewedRegionType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STGeoMappingLevel }
     *     
     */
    public void setViewedRegionType(STGeoMappingLevel value) {
        this.viewedRegionType = value;
    }

    /**
     * Gets the value of the cultureLanguage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCultureLanguage() {
        return cultureLanguage;
    }

    /**
     * Sets the value of the cultureLanguage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCultureLanguage(String value) {
        this.cultureLanguage = value;
    }

    /**
     * Gets the value of the cultureRegion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCultureRegion() {
        return cultureRegion;
    }

    /**
     * Sets the value of the cultureRegion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCultureRegion(String value) {
        this.cultureRegion = value;
    }

    /**
     * Gets the value of the attribution property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttribution() {
        return attribution;
    }

    /**
     * Sets the value of the attribution property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttribution(String value) {
        this.attribution = value;
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
