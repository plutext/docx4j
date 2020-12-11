
package org.docx4j.com.microsoft.schemas.office.drawing.x2012.chart;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.chart.CTBoolean;
import org.docx4j.dml.chart.CTDLbl;
import org.docx4j.dml.chart.CTMarker;
import org.docx4j.dml.chart.CTUnsignedInt;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_CategoryFilterException complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CategoryFilterException"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="sqref" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties" minOccurs="0"/&gt;
 *         &lt;element name="explosion" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_UnsignedInt" minOccurs="0"/&gt;
 *         &lt;element name="invertIfNegative" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/&gt;
 *         &lt;element name="bubble3D" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Boolean" minOccurs="0"/&gt;
 *         &lt;element name="marker" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_Marker" minOccurs="0"/&gt;
 *         &lt;element name="dLbl" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_DLbl" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CategoryFilterException", propOrder = {
    "sqref",
    "spPr",
    "explosion",
    "invertIfNegative",
    "bubble3D",
    "marker",
    "dLbl"
})
public class CTCategoryFilterException implements Child
{

    @XmlElement(required = true)
    protected String sqref;
    protected CTShapeProperties spPr;
    protected CTUnsignedInt explosion;
    protected CTBoolean invertIfNegative;
    protected CTBoolean bubble3D;
    protected CTMarker marker;
    protected CTDLbl dLbl;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the sqref property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSqref() {
        return sqref;
    }

    /**
     * Sets the value of the sqref property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSqref(String value) {
        this.sqref = value;
    }

    /**
     * Gets the value of the spPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTShapeProperties }
     *     
     */
    public CTShapeProperties getSpPr() {
        return spPr;
    }

    /**
     * Sets the value of the spPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShapeProperties }
     *     
     */
    public void setSpPr(CTShapeProperties value) {
        this.spPr = value;
    }

    /**
     * Gets the value of the explosion property.
     * 
     * @return
     *     possible object is
     *     {@link CTUnsignedInt }
     *     
     */
    public CTUnsignedInt getExplosion() {
        return explosion;
    }

    /**
     * Sets the value of the explosion property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTUnsignedInt }
     *     
     */
    public void setExplosion(CTUnsignedInt value) {
        this.explosion = value;
    }

    /**
     * Gets the value of the invertIfNegative property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getInvertIfNegative() {
        return invertIfNegative;
    }

    /**
     * Sets the value of the invertIfNegative property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setInvertIfNegative(CTBoolean value) {
        this.invertIfNegative = value;
    }

    /**
     * Gets the value of the bubble3D property.
     * 
     * @return
     *     possible object is
     *     {@link CTBoolean }
     *     
     */
    public CTBoolean getBubble3D() {
        return bubble3D;
    }

    /**
     * Sets the value of the bubble3D property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBoolean }
     *     
     */
    public void setBubble3D(CTBoolean value) {
        this.bubble3D = value;
    }

    /**
     * Gets the value of the marker property.
     * 
     * @return
     *     possible object is
     *     {@link CTMarker }
     *     
     */
    public CTMarker getMarker() {
        return marker;
    }

    /**
     * Sets the value of the marker property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMarker }
     *     
     */
    public void setMarker(CTMarker value) {
        this.marker = value;
    }

    /**
     * Gets the value of the dLbl property.
     * 
     * @return
     *     possible object is
     *     {@link CTDLbl }
     *     
     */
    public CTDLbl getDLbl() {
        return dLbl;
    }

    /**
     * Sets the value of the dLbl property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDLbl }
     *     
     */
    public void setDLbl(CTDLbl value) {
        this.dLbl = value;
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
