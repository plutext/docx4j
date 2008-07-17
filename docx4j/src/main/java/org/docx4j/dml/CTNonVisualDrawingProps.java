
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * Non-Visual Properties
 * 
 * <p>Java class for CT_NonVisualDrawingProps complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_NonVisualDrawingProps">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="hlinkClick" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Hyperlink" minOccurs="0"/>
 *         &lt;element name="hlinkHover" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Hyperlink" minOccurs="0"/>
 *         &lt;element name="ext" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtension" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_DrawingElementId" />
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="descr" type="{http://www.w3.org/2001/XMLSchema}string" default="" />
 *       &lt;attribute name="hidden" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_NonVisualDrawingProps", propOrder = {
    "hlinkClick",
    "hlinkHover",
    "ext"
})
public class CTNonVisualDrawingProps
    implements Child
{

    protected CTHyperlink hlinkClick;
    protected CTHyperlink hlinkHover;
    protected CTOfficeArtExtension ext;
    @XmlAttribute(required = true)
    protected long id;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute
    protected String descr;
    @XmlAttribute
    protected Boolean hidden;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the hlinkClick property.
     * 
     * @return
     *     possible object is
     *     {@link CTHyperlink }
     *     
     */
    public CTHyperlink getHlinkClick() {
        return hlinkClick;
    }

    /**
     * Sets the value of the hlinkClick property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTHyperlink }
     *     
     */
    public void setHlinkClick(CTHyperlink value) {
        this.hlinkClick = value;
    }

    /**
     * Gets the value of the hlinkHover property.
     * 
     * @return
     *     possible object is
     *     {@link CTHyperlink }
     *     
     */
    public CTHyperlink getHlinkHover() {
        return hlinkHover;
    }

    /**
     * Sets the value of the hlinkHover property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTHyperlink }
     *     
     */
    public void setHlinkHover(CTHyperlink value) {
        this.hlinkHover = value;
    }

    /**
     * Gets the value of the ext property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtension }
     *     
     */
    public CTOfficeArtExtension getExt() {
        return ext;
    }

    /**
     * Sets the value of the ext property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtension }
     *     
     */
    public void setExt(CTOfficeArtExtension value) {
        this.ext = value;
    }

    /**
     * Gets the value of the id property.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(long value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
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
     * Gets the value of the hidden property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHidden() {
        if (hidden == null) {
            return false;
        } else {
            return hidden;
        }
    }

    /**
     * Sets the value of the hidden property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHidden(Boolean value) {
        this.hidden = value;
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
