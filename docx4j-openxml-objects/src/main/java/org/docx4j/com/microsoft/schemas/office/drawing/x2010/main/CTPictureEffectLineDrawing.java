
package org.docx4j.com.microsoft.schemas.office.drawing.x2010.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PictureEffectLineDrawing complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PictureEffectLineDrawing"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="trans" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" default="25" /&gt;
 *       &lt;attribute name="pencilSize" type="{http://schemas.microsoft.com/office/drawing/2010/main}ST_ArtisticEffectParam100" default="0" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PictureEffectLineDrawing")
public class CTPictureEffectLineDrawing implements Child
{

    @XmlAttribute(name = "trans")
    protected Integer trans;
    @XmlAttribute(name = "pencilSize")
    protected Integer pencilSize;
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
            return  25;
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
     * Gets the value of the pencilSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getPencilSize() {
        if (pencilSize == null) {
            return  0;
        } else {
            return pencilSize;
        }
    }

    /**
     * Sets the value of the pencilSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPencilSize(Integer value) {
        this.pencilSize = value;
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
