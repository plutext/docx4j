
package org.docx4j.com.microsoft.schemas.office.drawing.x2017.model3d;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTRatio;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Scale3D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Scale3D"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="sx" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Ratio"/&gt;
 *         &lt;element name="sy" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Ratio"/&gt;
 *         &lt;element name="sz" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Ratio"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Scale3D", propOrder = {
    "sx",
    "sy",
    "sz"
})
public class CTScale3D implements Child
{

    @XmlElement(required = true)
    protected CTRatio sx;
    @XmlElement(required = true)
    protected CTRatio sy;
    @XmlElement(required = true)
    protected CTRatio sz;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the sx property.
     * 
     * @return
     *     possible object is
     *     {@link CTRatio }
     *     
     */
    public CTRatio getSx() {
        return sx;
    }

    /**
     * Sets the value of the sx property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRatio }
     *     
     */
    public void setSx(CTRatio value) {
        this.sx = value;
    }

    /**
     * Gets the value of the sy property.
     * 
     * @return
     *     possible object is
     *     {@link CTRatio }
     *     
     */
    public CTRatio getSy() {
        return sy;
    }

    /**
     * Sets the value of the sy property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRatio }
     *     
     */
    public void setSy(CTRatio value) {
        this.sy = value;
    }

    /**
     * Gets the value of the sz property.
     * 
     * @return
     *     possible object is
     *     {@link CTRatio }
     *     
     */
    public CTRatio getSz() {
        return sz;
    }

    /**
     * Sets the value of the sz property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRatio }
     *     
     */
    public void setSz(CTRatio value) {
        this.sz = value;
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
