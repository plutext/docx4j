
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_EffectExtent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_EffectExtent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="l" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" />
 *       &lt;attribute name="t" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" />
 *       &lt;attribute name="r" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" />
 *       &lt;attribute name="b" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_Coordinate" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_EffectExtent", namespace = "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing")
public class CTEffectExtent
    implements Child
{

    @XmlAttribute(required = true)
    protected long l;
    @XmlAttribute(required = true)
    protected long t;
    @XmlAttribute(required = true)
    protected long r;
    @XmlAttribute(required = true)
    protected long b;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the l property.
     * 
     */
    public long getL() {
        return l;
    }

    /**
     * Sets the value of the l property.
     * 
     */
    public void setL(long value) {
        this.l = value;
    }

    /**
     * Gets the value of the t property.
     * 
     */
    public long getT() {
        return t;
    }

    /**
     * Sets the value of the t property.
     * 
     */
    public void setT(long value) {
        this.t = value;
    }

    /**
     * Gets the value of the r property.
     * 
     */
    public long getR() {
        return r;
    }

    /**
     * Sets the value of the r property.
     * 
     */
    public void setR(long value) {
        this.r = value;
    }

    /**
     * Gets the value of the b property.
     * 
     */
    public long getB() {
        return b;
    }

    /**
     * Sets the value of the b property.
     * 
     */
    public void setB(long value) {
        this.b = value;
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
