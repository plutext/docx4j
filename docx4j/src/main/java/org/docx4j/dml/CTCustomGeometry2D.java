
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_CustomGeometry2D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CustomGeometry2D">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="avLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GeomGuideList" minOccurs="0"/>
 *         &lt;element name="gdLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GeomGuideList" minOccurs="0"/>
 *         &lt;element name="ahLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_AdjustHandleList" minOccurs="0"/>
 *         &lt;element name="cxnLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ConnectionSiteList" minOccurs="0"/>
 *         &lt;element name="rect" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GeomRect" minOccurs="0"/>
 *         &lt;element name="pathLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Path2DList"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CustomGeometry2D", propOrder = {
    "avLst",
    "gdLst",
    "ahLst",
    "cxnLst",
    "rect",
    "pathLst"
})
public class CTCustomGeometry2D
    implements Child
{

    protected CTGeomGuideList avLst;
    protected CTGeomGuideList gdLst;
    protected CTAdjustHandleList ahLst;
    protected CTConnectionSiteList cxnLst;
    protected CTGeomRect rect;
    @XmlElement(required = true)
    protected CTPath2DList pathLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the avLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeomGuideList }
     *     
     */
    public CTGeomGuideList getAvLst() {
        return avLst;
    }

    /**
     * Sets the value of the avLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeomGuideList }
     *     
     */
    public void setAvLst(CTGeomGuideList value) {
        this.avLst = value;
    }

    /**
     * Gets the value of the gdLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeomGuideList }
     *     
     */
    public CTGeomGuideList getGdLst() {
        return gdLst;
    }

    /**
     * Sets the value of the gdLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeomGuideList }
     *     
     */
    public void setGdLst(CTGeomGuideList value) {
        this.gdLst = value;
    }

    /**
     * Gets the value of the ahLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTAdjustHandleList }
     *     
     */
    public CTAdjustHandleList getAhLst() {
        return ahLst;
    }

    /**
     * Sets the value of the ahLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAdjustHandleList }
     *     
     */
    public void setAhLst(CTAdjustHandleList value) {
        this.ahLst = value;
    }

    /**
     * Gets the value of the cxnLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTConnectionSiteList }
     *     
     */
    public CTConnectionSiteList getCxnLst() {
        return cxnLst;
    }

    /**
     * Sets the value of the cxnLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTConnectionSiteList }
     *     
     */
    public void setCxnLst(CTConnectionSiteList value) {
        this.cxnLst = value;
    }

    /**
     * Gets the value of the rect property.
     * 
     * @return
     *     possible object is
     *     {@link CTGeomRect }
     *     
     */
    public CTGeomRect getRect() {
        return rect;
    }

    /**
     * Sets the value of the rect property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGeomRect }
     *     
     */
    public void setRect(CTGeomRect value) {
        this.rect = value;
    }

    /**
     * Gets the value of the pathLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTPath2DList }
     *     
     */
    public CTPath2DList getPathLst() {
        return pathLst;
    }

    /**
     * Sets the value of the pathLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPath2DList }
     *     
     */
    public void setPathLst(CTPath2DList value) {
        this.pathLst = value;
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
