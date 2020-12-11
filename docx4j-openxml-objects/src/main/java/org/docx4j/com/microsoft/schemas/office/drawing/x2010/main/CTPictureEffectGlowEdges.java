
package org.docx4j.com.microsoft.schemas.office.drawing.x2010.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PictureEffectGlowEdges complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PictureEffectGlowEdges"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="trans" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" default="15" /&gt;
 *       &lt;attribute name="smoothness" type="{http://schemas.microsoft.com/office/drawing/2010/main}ST_ArtisticEffectParam10" default="3" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PictureEffectGlowEdges")
public class CTPictureEffectGlowEdges implements Child
{

    @XmlAttribute(name = "trans")
    protected Integer trans;
    @XmlAttribute(name = "smoothness")
    protected Integer smoothness;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the trans property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getTrans() {
        if (trans == null) {
            return  15;
        } else {
            return trans;
        }
    }

    /**
     * Sets the value of the trans property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTrans(Integer value) {
        this.trans = value;
    }

    /**
     * Gets the value of the smoothness property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getSmoothness() {
        if (smoothness == null) {
            return  3;
        } else {
            return smoothness;
        }
    }

    /**
     * Sets the value of the smoothness property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSmoothness(Integer value) {
        this.smoothness = value;
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
