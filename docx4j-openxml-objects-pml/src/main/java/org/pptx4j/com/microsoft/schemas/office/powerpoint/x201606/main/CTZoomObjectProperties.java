
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x201606.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.docx4j.dml.CTBlipFillProperties;
import org.docx4j.dml.CTShapeProperties;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ZoomObjectProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ZoomObjectProperties"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="blipFill" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_BlipFillProperties"/&gt;
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Guid" /&gt;
 *       &lt;attribute name="returnToParent" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="imageType" type="{http://schemas.microsoft.com/office/powerpoint/2016/6/main}ST_ZoomObjectImageType" default="preview" /&gt;
 *       &lt;attribute name="transitionDur" type="{http://schemas.microsoft.com/office/powerpoint/2010/main}ST_UniversalTimeOffset" /&gt;
 *       &lt;attribute name="showBg" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ZoomObjectProperties", propOrder = {
    "blipFill",
    "spPr"
})
public class CTZoomObjectProperties implements Child
{

    @XmlElement(required = true)
    protected CTBlipFillProperties blipFill;
    @XmlElement(required = true)
    protected CTShapeProperties spPr;
    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String id;
    @XmlAttribute(name = "returnToParent")
    protected Boolean returnToParent;
    @XmlAttribute(name = "imageType")
    protected STZoomObjectImageType imageType;
    @XmlAttribute(name = "transitionDur")
    protected String transitionDur;
    @XmlAttribute(name = "showBg")
    protected Boolean showBg;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the blipFill property.
     * 
     * @return
     *     possible object is
     *     {@link CTBlipFillProperties }
     *     
     */
    public CTBlipFillProperties getBlipFill() {
        return blipFill;
    }

    /**
     * Sets the value of the blipFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBlipFillProperties }
     *     
     */
    public void setBlipFill(CTBlipFillProperties value) {
        this.blipFill = value;
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
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the returnToParent property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isReturnToParent() {
        if (returnToParent == null) {
            return true;
        } else {
            return returnToParent;
        }
    }

    /**
     * Sets the value of the returnToParent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setReturnToParent(Boolean value) {
        this.returnToParent = value;
    }

    /**
     * Gets the value of the imageType property.
     * 
     * @return
     *     possible object is
     *     {@link STZoomObjectImageType }
     *     
     */
    public STZoomObjectImageType getImageType() {
        if (imageType == null) {
            return STZoomObjectImageType.PREVIEW;
        } else {
            return imageType;
        }
    }

    /**
     * Sets the value of the imageType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STZoomObjectImageType }
     *     
     */
    public void setImageType(STZoomObjectImageType value) {
        this.imageType = value;
    }

    /**
     * Gets the value of the transitionDur property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransitionDur() {
        return transitionDur;
    }

    /**
     * Sets the value of the transitionDur property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransitionDur(String value) {
        this.transitionDur = value;
    }

    /**
     * Gets the value of the showBg property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowBg() {
        if (showBg == null) {
            return true;
        } else {
            return showBg;
        }
    }

    /**
     * Sets the value of the showBg property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowBg(Boolean value) {
        this.showBg = value;
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
