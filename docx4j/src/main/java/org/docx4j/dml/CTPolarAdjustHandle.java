
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PolarAdjustHandle complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PolarAdjustHandle">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pos" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_AdjPoint2D"/>
 *       &lt;/sequence>
 *       &lt;attribute name="gdRefR" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_GeomGuideName" />
 *       &lt;attribute name="minR" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjCoordinate" />
 *       &lt;attribute name="maxR" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjCoordinate" />
 *       &lt;attribute name="gdRefAng" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_GeomGuideName" />
 *       &lt;attribute name="minAng" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjAngle" />
 *       &lt;attribute name="maxAng" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_AdjAngle" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PolarAdjustHandle", propOrder = {
    "pos"
})
public class CTPolarAdjustHandle implements Child
{

    @XmlElement(required = true)
    protected CTAdjPoint2D pos;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String gdRefR;
    @XmlAttribute
    protected String minR;
    @XmlAttribute
    protected String maxR;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String gdRefAng;
    @XmlAttribute
    protected String minAng;
    @XmlAttribute
    protected String maxAng;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pos property.
     * 
     * @return
     *     possible object is
     *     {@link CTAdjPoint2D }
     *     
     */
    public CTAdjPoint2D getPos() {
        return pos;
    }

    /**
     * Sets the value of the pos property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAdjPoint2D }
     *     
     */
    public void setPos(CTAdjPoint2D value) {
        this.pos = value;
    }

    /**
     * Gets the value of the gdRefR property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGdRefR() {
        return gdRefR;
    }

    /**
     * Sets the value of the gdRefR property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGdRefR(String value) {
        this.gdRefR = value;
    }

    /**
     * Gets the value of the minR property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinR() {
        return minR;
    }

    /**
     * Sets the value of the minR property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinR(String value) {
        this.minR = value;
    }

    /**
     * Gets the value of the maxR property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxR() {
        return maxR;
    }

    /**
     * Sets the value of the maxR property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxR(String value) {
        this.maxR = value;
    }

    /**
     * Gets the value of the gdRefAng property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGdRefAng() {
        return gdRefAng;
    }

    /**
     * Sets the value of the gdRefAng property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGdRefAng(String value) {
        this.gdRefAng = value;
    }

    /**
     * Gets the value of the minAng property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinAng() {
        return minAng;
    }

    /**
     * Sets the value of the minAng property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinAng(String value) {
        this.minAng = value;
    }

    /**
     * Gets the value of the maxAng property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxAng() {
        return maxAng;
    }

    /**
     * Sets the value of the maxAng property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxAng(String value) {
        this.maxAng = value;
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
