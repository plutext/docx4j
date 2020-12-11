
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2010.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.pptx4j.pml.CTExtensionList;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Media complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Media"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="trim" type="{http://schemas.microsoft.com/office/powerpoint/2010/main}CT_MediaTrim" minOccurs="0"/&gt;
 *         &lt;element name="fade" type="{http://schemas.microsoft.com/office/powerpoint/2010/main}CT_MediaFade" minOccurs="0"/&gt;
 *         &lt;element name="bmkLst" type="{http://schemas.microsoft.com/office/powerpoint/2010/main}CT_MediaBookmarkList" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/drawingml/2006/main}AG_Blob"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Media", propOrder = {
    "trim",
    "fade",
    "bmkLst",
    "extLst"
})
public class CTMedia implements Child
{

    protected CTMediaTrim trim;
    protected CTMediaFade fade;
    protected CTMediaBookmarkList bmkLst;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "embed", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String embed;
    @XmlAttribute(name = "link", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String link;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the trim property.
     * 
     * @return
     *     possible object is
     *     {@link CTMediaTrim }
     *     
     */
    public CTMediaTrim getTrim() {
        return trim;
    }

    /**
     * Sets the value of the trim property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMediaTrim }
     *     
     */
    public void setTrim(CTMediaTrim value) {
        this.trim = value;
    }

    /**
     * Gets the value of the fade property.
     * 
     * @return
     *     possible object is
     *     {@link CTMediaFade }
     *     
     */
    public CTMediaFade getFade() {
        return fade;
    }

    /**
     * Sets the value of the fade property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMediaFade }
     *     
     */
    public void setFade(CTMediaFade value) {
        this.fade = value;
    }

    /**
     * Gets the value of the bmkLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTMediaBookmarkList }
     *     
     */
    public CTMediaBookmarkList getBmkLst() {
        return bmkLst;
    }

    /**
     * Sets the value of the bmkLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMediaBookmarkList }
     *     
     */
    public void setBmkLst(CTMediaBookmarkList value) {
        this.bmkLst = value;
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
     * Embedded Picture Reference
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmbed() {
        if (embed == null) {
            return "";
        } else {
            return embed;
        }
    }

    /**
     * Sets the value of the embed property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmbed(String value) {
        this.embed = value;
    }

    /**
     * Linked Picture Reference
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLink() {
        if (link == null) {
            return "";
        } else {
            return link;
        }
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLink(String value) {
        this.link = value;
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
