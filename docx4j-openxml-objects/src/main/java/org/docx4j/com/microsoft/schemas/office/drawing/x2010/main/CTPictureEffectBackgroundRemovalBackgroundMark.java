
package org.docx4j.com.microsoft.schemas.office.drawing.x2010.main;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PictureEffectBackgroundRemovalBackgroundMark complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PictureEffectBackgroundRemovalBackgroundMark"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="x1" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" /&gt;
 *       &lt;attribute name="y1" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" /&gt;
 *       &lt;attribute name="x2" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" /&gt;
 *       &lt;attribute name="y2" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_PositiveFixedPercentage" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PictureEffectBackgroundRemovalBackgroundMark")
public class CTPictureEffectBackgroundRemovalBackgroundMark implements Child
{

    @XmlAttribute(name = "x1", required = true)
    protected int x1;
    @XmlAttribute(name = "y1", required = true)
    protected int y1;
    @XmlAttribute(name = "x2", required = true)
    protected int x2;
    @XmlAttribute(name = "y2", required = true)
    protected int y2;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the x1 property.
     * 
     */
    public int getX1() {
        return x1;
    }

    /**
     * Sets the value of the x1 property.
     * 
     */
    public void setX1(int value) {
        this.x1 = value;
    }

    /**
     * Gets the value of the y1 property.
     * 
     */
    public int getY1() {
        return y1;
    }

    /**
     * Sets the value of the y1 property.
     * 
     */
    public void setY1(int value) {
        this.y1 = value;
    }

    /**
     * Gets the value of the x2 property.
     * 
     */
    public int getX2() {
        return x2;
    }

    /**
     * Sets the value of the x2 property.
     * 
     */
    public void setX2(int value) {
        this.x2 = value;
    }

    /**
     * Gets the value of the y2 property.
     * 
     */
    public int getY2() {
        return y2;
    }

    /**
     * Sets the value of the y2 property.
     * 
     */
    public void setY2(int value) {
        this.y2 = value;
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
