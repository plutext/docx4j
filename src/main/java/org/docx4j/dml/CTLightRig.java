
package org.docx4j.dml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_LightRig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_LightRig">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="rot" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_SphereCoords" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="rig" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_LightRigType" />
 *       &lt;attribute name="dir" use="required" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_LightRigDirection" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_LightRig", propOrder = {
    "rot"
})
public class CTLightRig
    implements Child
{

    protected CTSphereCoords rot;
    @XmlAttribute(required = true)
    protected STLightRigType rig;
    @XmlAttribute(required = true)
    protected STLightRigDirection dir;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the rot property.
     * 
     * @return
     *     possible object is
     *     {@link CTSphereCoords }
     *     
     */
    public CTSphereCoords getRot() {
        return rot;
    }

    /**
     * Sets the value of the rot property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSphereCoords }
     *     
     */
    public void setRot(CTSphereCoords value) {
        this.rot = value;
    }

    /**
     * Gets the value of the rig property.
     * 
     * @return
     *     possible object is
     *     {@link STLightRigType }
     *     
     */
    public STLightRigType getRig() {
        return rig;
    }

    /**
     * Sets the value of the rig property.
     * 
     * @param value
     *     allowed object is
     *     {@link STLightRigType }
     *     
     */
    public void setRig(STLightRigType value) {
        this.rig = value;
    }

    /**
     * Gets the value of the dir property.
     * 
     * @return
     *     possible object is
     *     {@link STLightRigDirection }
     *     
     */
    public STLightRigDirection getDir() {
        return dir;
    }

    /**
     * Sets the value of the dir property.
     * 
     * @param value
     *     allowed object is
     *     {@link STLightRigDirection }
     *     
     */
    public void setDir(STLightRigDirection value) {
        this.dir = value;
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
