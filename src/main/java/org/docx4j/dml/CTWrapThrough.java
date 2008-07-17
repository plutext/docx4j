
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_WrapThrough complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_WrapThrough">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="wrapPolygon" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}CT_WrapPath"/>
 *       &lt;/sequence>
 *       &lt;attribute name="wrapText" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapText" />
 *       &lt;attribute name="distL" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" />
 *       &lt;attribute name="distR" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_WrapThrough", namespace = "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", propOrder = {
    "wrapPolygon"
})
public class CTWrapThrough
    implements Child
{

    @XmlElement(required = true)
    protected CTWrapPath wrapPolygon;
    @XmlAttribute(required = true)
    protected STWrapText wrapText;
    @XmlAttribute(required = true)
    protected long distL;
    @XmlAttribute(required = true)
    protected long distR;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the wrapPolygon property.
     * 
     * @return
     *     possible object is
     *     {@link CTWrapPath }
     *     
     */
    public CTWrapPath getWrapPolygon() {
        return wrapPolygon;
    }

    /**
     * Sets the value of the wrapPolygon property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWrapPath }
     *     
     */
    public void setWrapPolygon(CTWrapPath value) {
        this.wrapPolygon = value;
    }

    /**
     * Gets the value of the wrapText property.
     * 
     * @return
     *     possible object is
     *     {@link STWrapText }
     *     
     */
    public STWrapText getWrapText() {
        return wrapText;
    }

    /**
     * Sets the value of the wrapText property.
     * 
     * @param value
     *     allowed object is
     *     {@link STWrapText }
     *     
     */
    public void setWrapText(STWrapText value) {
        this.wrapText = value;
    }

    /**
     * Gets the value of the distL property.
     * 
     */
    public long getDistL() {
        return distL;
    }

    /**
     * Sets the value of the distL property.
     * 
     */
    public void setDistL(long value) {
        this.distL = value;
    }

    /**
     * Gets the value of the distR property.
     * 
     */
    public long getDistR() {
        return distR;
    }

    /**
     * Sets the value of the distR property.
     * 
     */
    public void setDistR(long value) {
        this.distR = value;
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
