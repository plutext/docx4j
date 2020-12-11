
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.summaryzoom;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.pptx4j.com.microsoft.schemas.office.powerpoint.x201606.main.CTZoomObjectProperties;
import org.pptx4j.pml.CTExtensionList;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SummaryZoomObject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SummaryZoomObject"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="zmPr" type="{http://schemas.microsoft.com/office/powerpoint/2016/6/main}CT_ZoomObjectProperties"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="sectionId" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Guid" /&gt;
 *       &lt;attribute name="title" type="{http://www.w3.org/2001/XMLSchema}string" default="" /&gt;
 *       &lt;attribute name="descr" type="{http://www.w3.org/2001/XMLSchema}string" default="" /&gt;
 *       &lt;attribute name="offsetFactorX" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" default="0" /&gt;
 *       &lt;attribute name="offsetFactorY" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" default="0" /&gt;
 *       &lt;attribute name="scaleFactorX" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" default="100000" /&gt;
 *       &lt;attribute name="scaleFactorY" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Percentage" default="100000" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SummaryZoomObject", propOrder = {
    "zmPr",
    "extLst"
})
public class CTSummaryZoomObject implements Child
{

    @XmlElement(required = true)
    protected CTZoomObjectProperties zmPr;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "sectionId", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String sectionId;
    @XmlAttribute(name = "title")
    protected String title;
    @XmlAttribute(name = "descr")
    protected String descr;
    @XmlAttribute(name = "offsetFactorX")
    protected Integer offsetFactorX;
    @XmlAttribute(name = "offsetFactorY")
    protected Integer offsetFactorY;
    @XmlAttribute(name = "scaleFactorX")
    protected Integer scaleFactorX;
    @XmlAttribute(name = "scaleFactorY")
    protected Integer scaleFactorY;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the zmPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTZoomObjectProperties }
     *     
     */
    public CTZoomObjectProperties getZmPr() {
        return zmPr;
    }

    /**
     * Sets the value of the zmPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTZoomObjectProperties }
     *     
     */
    public void setZmPr(CTZoomObjectProperties value) {
        this.zmPr = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the sectionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSectionId() {
        return sectionId;
    }

    /**
     * Sets the value of the sectionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSectionId(String value) {
        this.sectionId = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        if (title == null) {
            return "";
        } else {
            return title;
        }
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the descr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescr() {
        if (descr == null) {
            return "";
        } else {
            return descr;
        }
    }

    /**
     * Sets the value of the descr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescr(String value) {
        this.descr = value;
    }

    /**
     * Gets the value of the offsetFactorX property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getOffsetFactorX() {
        if (offsetFactorX == null) {
            return  0;
        } else {
            return offsetFactorX;
        }
    }

    /**
     * Sets the value of the offsetFactorX property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setOffsetFactorX(Integer value) {
        this.offsetFactorX = value;
    }

    /**
     * Gets the value of the offsetFactorY property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getOffsetFactorY() {
        if (offsetFactorY == null) {
            return  0;
        } else {
            return offsetFactorY;
        }
    }

    /**
     * Sets the value of the offsetFactorY property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setOffsetFactorY(Integer value) {
        this.offsetFactorY = value;
    }

    /**
     * Gets the value of the scaleFactorX property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getScaleFactorX() {
        if (scaleFactorX == null) {
            return  100000;
        } else {
            return scaleFactorX;
        }
    }

    /**
     * Sets the value of the scaleFactorX property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setScaleFactorX(Integer value) {
        this.scaleFactorX = value;
    }

    /**
     * Gets the value of the scaleFactorY property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getScaleFactorY() {
        if (scaleFactorY == null) {
            return  100000;
        } else {
            return scaleFactorY;
        }
    }

    /**
     * Sets the value of the scaleFactorY property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setScaleFactorY(Integer value) {
        this.scaleFactorY = value;
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
