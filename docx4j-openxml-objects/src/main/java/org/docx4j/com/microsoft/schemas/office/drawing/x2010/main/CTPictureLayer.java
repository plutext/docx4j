
package org.docx4j.com.microsoft.schemas.office.drawing.x2010.main;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PictureLayer complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PictureLayer"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="imgEffect" type="{http://schemas.microsoft.com/office/drawing/2010/main}CT_PictureEffect" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}embed default="""/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PictureLayer", propOrder = {
    "imgEffect"
})
public class CTPictureLayer implements Child
{

    protected List<CTPictureEffect> imgEffect;
    @XmlAttribute(name = "embed", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String embed;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the imgEffect property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the imgEffect property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getImgEffect().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTPictureEffect }
     * 
     * 
     */
    public List<CTPictureEffect> getImgEffect() {
        if (imgEffect == null) {
            imgEffect = new ArrayList<CTPictureEffect>();
        }
        return this.imgEffect;
    }

    /**
     * Gets the value of the embed property.
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
