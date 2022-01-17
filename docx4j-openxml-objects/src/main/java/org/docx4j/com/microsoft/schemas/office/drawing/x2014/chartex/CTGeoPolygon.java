
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import java.math.BigInteger;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_GeoPolygon complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_GeoPolygon"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="polygonId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="numPoints" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute name="pcaRings" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_GeoPolygon")
public class CTGeoPolygon {

    @XmlAttribute(name = "polygonId", required = true)
    protected String polygonId;
    @XmlAttribute(name = "numPoints", required = true)
    protected BigInteger numPoints;
    @XmlAttribute(name = "pcaRings", required = true)
    protected String pcaRings;

    /**
     * Gets the value of the polygonId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPolygonId() {
        return polygonId;
    }

    /**
     * Sets the value of the polygonId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPolygonId(String value) {
        this.polygonId = value;
    }

    /**
     * Gets the value of the numPoints property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumPoints() {
        return numPoints;
    }

    /**
     * Sets the value of the numPoints property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumPoints(BigInteger value) {
        this.numPoints = value;
    }

    /**
     * Gets the value of the pcaRings property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPcaRings() {
        return pcaRings;
    }

    /**
     * Sets the value of the pcaRings property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPcaRings(String value) {
        this.pcaRings = value;
    }

}
