
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;;


/**
 * <p>Java class for CT_GeoData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GeoData"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="geoPolygons" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_GeoPolygons" minOccurs="0"/&gt;
 *         &lt;element name="copyrights" type="{http://schemas.microsoft.com/office/drawing/2014/chartex}CT_Copyrights" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="entityName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="entityId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="east" use="required" type="{http://www.w3.org/2001/XMLSchema}double" /&gt;
 *       &lt;attribute name="west" use="required" type="{http://www.w3.org/2001/XMLSchema}double" /&gt;
 *       &lt;attribute name="north" use="required" type="{http://www.w3.org/2001/XMLSchema}double" /&gt;
 *       &lt;attribute name="south" use="required" type="{http://www.w3.org/2001/XMLSchema}double" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GeoData", propOrder = {
    "geoPolygons",
    "copyrights"
})
public class CTGeoData implements Child
{

    protected CTGeoPolygons geoPolygons;
    protected CTCopyrights copyrights;
    @XmlAttribute(name = "entityName", required = true)
    protected String entityName;
    @XmlAttribute(name = "entityId", required = true)
    protected String entityId;
    @XmlAttribute(name = "east", required = true)
    protected double east;
    @XmlAttribute(name = "west", required = true)
    protected double west;
    @XmlAttribute(name = "north", required = true)
    protected double north;
    @XmlAttribute(name = "south", required = true)
    protected double south;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the geoPolygons property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeoPolygons }
     *     
     */
    public CTGeoPolygons getGeoPolygons() {
        return geoPolygons;
    }

    /**
     * Sets the value of the geoPolygons property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeoPolygons }
     *     
     */
    public void setGeoPolygons(CTGeoPolygons value) {
        this.geoPolygons = value;
    }

    /**
     * Gets the value of the copyrights property.
     * 
     * @return
     *     possible object is
     *     {@link CTCopyrights }
     *     
     */
    public CTCopyrights getCopyrights() {
        return copyrights;
    }

    /**
     * Sets the value of the copyrights property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCopyrights }
     *     
     */
    public void setCopyrights(CTCopyrights value) {
        this.copyrights = value;
    }

    /**
     * Gets the value of the entityName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Sets the value of the entityName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEntityName(String value) {
        this.entityName = value;
    }

    /**
     * Gets the value of the entityId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Sets the value of the entityId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEntityId(String value) {
        this.entityId = value;
    }

    /**
     * Gets the value of the east property.
     * 
     */
    public double getEast() {
        return east;
    }

    /**
     * Sets the value of the east property.
     * 
     */
    public void setEast(double value) {
        this.east = value;
    }

    /**
     * Gets the value of the west property.
     * 
     */
    public double getWest() {
        return west;
    }

    /**
     * Sets the value of the west property.
     * 
     */
    public void setWest(double value) {
        this.west = value;
    }

    /**
     * Gets the value of the north property.
     * 
     */
    public double getNorth() {
        return north;
    }

    /**
     * Sets the value of the north property.
     * 
     */
    public void setNorth(double value) {
        this.north = value;
    }

    /**
     * Gets the value of the south property.
     * 
     */
    public double getSouth() {
        return south;
    }

    /**
     * Sets the value of the south property.
     * 
     */
    public void setSouth(double value) {
        this.south = value;
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
