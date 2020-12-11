
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;;


/**
 * <p>Java class for CT_SeriesElementVisibilities complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SeriesElementVisibilities"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="connectorLines" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="meanLine" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="meanMarker" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="nonoutliers" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="outliers" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SeriesElementVisibilities")
public class CTSeriesElementVisibilities implements Child
{

    @XmlAttribute(name = "connectorLines")
    protected Boolean connectorLines;
    @XmlAttribute(name = "meanLine")
    protected Boolean meanLine;
    @XmlAttribute(name = "meanMarker")
    protected Boolean meanMarker;
    @XmlAttribute(name = "nonoutliers")
    protected Boolean nonoutliers;
    @XmlAttribute(name = "outliers")
    protected Boolean outliers;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the connectorLines property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isConnectorLines() {
        return connectorLines;
    }

    /**
     * Sets the value of the connectorLines property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setConnectorLines(Boolean value) {
        this.connectorLines = value;
    }

    /**
     * Gets the value of the meanLine property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMeanLine() {
        return meanLine;
    }

    /**
     * Sets the value of the meanLine property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMeanLine(Boolean value) {
        this.meanLine = value;
    }

    /**
     * Gets the value of the meanMarker property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMeanMarker() {
        return meanMarker;
    }

    /**
     * Sets the value of the meanMarker property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMeanMarker(Boolean value) {
        this.meanMarker = value;
    }

    /**
     * Gets the value of the nonoutliers property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isNonoutliers() {
        return nonoutliers;
    }

    /**
     * Sets the value of the nonoutliers property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNonoutliers(Boolean value) {
        this.nonoutliers = value;
    }

    /**
     * Gets the value of the outliers property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isOutliers() {
        return outliers;
    }

    /**
     * Sets the value of the outliers property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOutliers(Boolean value) {
        this.outliers = value;
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
