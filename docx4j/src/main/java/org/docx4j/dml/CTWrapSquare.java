
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_WrapSquare complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_WrapSquare">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="effectExtent" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}CT_EffectExtent" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="wrapText" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapText" />
 *       &lt;attribute name="distT" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" />
 *       &lt;attribute name="distB" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}ST_WrapDistance" />
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
@XmlType(name = "CT_WrapSquare", namespace = "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", propOrder = {
    "effectExtent"
})
public class CTWrapSquare
    implements Child
{

    protected CTEffectExtent effectExtent;
    @XmlAttribute(required = true)
    protected STWrapText wrapText;
    @XmlAttribute(required = true)
    protected long distT;
    @XmlAttribute(required = true)
    protected long distB;
    @XmlAttribute(required = true)
    protected long distL;
    @XmlAttribute(required = true)
    protected long distR;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the effectExtent property.
     * 
     * @return
     *     possible object is
     *     {@link CTEffectExtent }
     *     
     */
    public CTEffectExtent getEffectExtent() {
        return effectExtent;
    }

    /**
     * Sets the value of the effectExtent property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEffectExtent }
     *     
     */
    public void setEffectExtent(CTEffectExtent value) {
        this.effectExtent = value;
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
     * Gets the value of the distT property.
     * 
     */
    public long getDistT() {
        return distT;
    }

    /**
     * Sets the value of the distT property.
     * 
     */
    public void setDistT(long value) {
        this.distT = value;
    }

    /**
     * Gets the value of the distB property.
     * 
     */
    public long getDistB() {
        return distB;
    }

    /**
     * Sets the value of the distB property.
     * 
     */
    public void setDistB(long value) {
        this.distB = value;
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
