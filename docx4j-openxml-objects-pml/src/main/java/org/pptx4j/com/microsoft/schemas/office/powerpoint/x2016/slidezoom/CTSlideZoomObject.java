
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2016.slidezoom;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.pptx4j.com.microsoft.schemas.office.powerpoint.x201606.main.CTZoomObjectProperties;
import org.pptx4j.pml.CTExtensionList;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SlideZoomObject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SlideZoomObject"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="zmPr" type="{http://schemas.microsoft.com/office/powerpoint/2016/6/main}CT_ZoomObjectProperties"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="sldId" use="required" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_SlideId" /&gt;
 *       &lt;attribute name="cId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SlideZoomObject", propOrder = {
    "zmPr",
    "extLst"
})
public class CTSlideZoomObject implements Child
{

    @XmlElement(required = true)
    protected CTZoomObjectProperties zmPr;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "sldId", required = true)
    protected long sldId;
    @XmlAttribute(name = "cId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long cId;
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
     * Gets the value of the sldId property.
     * 
     */
    public long getSldId() {
        return sldId;
    }

    /**
     * Sets the value of the sldId property.
     * 
     */
    public void setSldId(long value) {
        this.sldId = value;
    }

    /**
     * Gets the value of the cId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCId() {
        return cId;
    }

    /**
     * Sets the value of the cId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCId(Long value) {
        this.cId = value;
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
