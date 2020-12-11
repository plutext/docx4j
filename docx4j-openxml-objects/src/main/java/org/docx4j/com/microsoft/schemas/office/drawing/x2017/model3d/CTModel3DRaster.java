
package org.docx4j.com.microsoft.schemas.office.drawing.x2017.model3d;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTBlip;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Model3DRaster complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Model3DRaster"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="blip" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Blip" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="rName" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="rVer" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Model3DRaster", propOrder = {
    "blip"
})
public class CTModel3DRaster implements Child
{

    protected CTBlip blip;
    @XmlAttribute(name = "rName", required = true)
    protected String rName;
    @XmlAttribute(name = "rVer", required = true)
    protected String rVer;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the blip property.
     * 
     * @return
     *     possible object is
     *     {@link CTBlip }
     *     
     */
    public CTBlip getBlip() {
        return blip;
    }

    /**
     * Sets the value of the blip property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBlip }
     *     
     */
    public void setBlip(CTBlip value) {
        this.blip = value;
    }

    /**
     * Gets the value of the rName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRName() {
        return rName;
    }

    /**
     * Sets the value of the rName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRName(String value) {
        this.rName = value;
    }

    /**
     * Gets the value of the rVer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRVer() {
        return rVer;
    }

    /**
     * Sets the value of the rVer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRVer(String value) {
        this.rVer = value;
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
