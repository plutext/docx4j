
package org.docx4j.com.microsoft.schemas.office.drawing.x2016.SVG.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SVGBlip complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SVGBlip"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/drawingml/2006/main}AG_Blob"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SVGBlip")
public class CTSVGBlip implements Child
{

    @XmlAttribute(name = "embed", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String embed;
    @XmlAttribute(name = "link", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String link;
    @XmlTransient
    private Object parent;

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
