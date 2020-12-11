
package org.docx4j.com.microsoft.schemas.office.drawing.x2018.animation.model3d;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.com.microsoft.schemas.office.drawing.x2018.animation.CTAnimationProperties;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_EmbeddedAnimation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_EmbeddedAnimation"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="animPr" type="{http://schemas.microsoft.com/office/drawing/2018/animation}CT_AnimationProperties"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="animId" use="required" type="{http://schemas.microsoft.com/office/drawing/2018/animation/model3d}ST_EmbeddedAnimationID" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_EmbeddedAnimation", propOrder = {
    "animPr",
    "extLst"
})
public class CTEmbeddedAnimation implements Child
{

    @XmlElement(required = true)
    protected CTAnimationProperties animPr;
    protected CTOfficeArtExtensionList extLst;
    @XmlAttribute(name = "animId", required = true)
    protected long animId;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the animPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTAnimationProperties }
     *     
     */
    public CTAnimationProperties getAnimPr() {
        return animPr;
    }

    /**
     * Sets the value of the animPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAnimationProperties }
     *     
     */
    public void setAnimPr(CTAnimationProperties value) {
        this.animPr = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public CTOfficeArtExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOfficeArtExtensionList }
     *     
     */
    public void setExtLst(CTOfficeArtExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the animId property.
     * 
     */
    public long getAnimId() {
        return animId;
    }

    /**
     * Sets the value of the animId property.
     * 
     */
    public void setAnimId(long value) {
        this.animId = value;
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
