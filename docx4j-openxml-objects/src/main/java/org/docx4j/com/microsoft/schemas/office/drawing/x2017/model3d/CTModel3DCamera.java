
package org.docx4j.com.microsoft.schemas.office.drawing.x2017.model3d;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.docx4j.dml.CTPoint3D;
import org.docx4j.dml.CTVector3D;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Model3DCamera complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Model3DCamera"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="pos" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Point3D"/&gt;
 *         &lt;element name="up" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Vector3D"/&gt;
 *         &lt;element name="lookAt" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Point3D"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="orthographic" type="{http://schemas.microsoft.com/office/drawing/2017/model3d}CT_OrthographicProjection"/&gt;
 *           &lt;element name="perspective" type="{http://schemas.microsoft.com/office/drawing/2017/model3d}CT_PerspectiveProjection"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Model3DCamera", propOrder = {
    "pos",
    "up",
    "lookAt",
    "orthographic",
    "perspective",
    "extLst"
})
public class CTModel3DCamera implements Child
{

    @XmlElement(required = true)
    protected CTPoint3D pos;
    @XmlElement(required = true)
    protected CTVector3D up;
    @XmlElement(required = true)
    protected CTPoint3D lookAt;
    protected CTOrthographicProjection orthographic;
    protected CTPerspectiveProjection perspective;
    protected CTOfficeArtExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the pos property.
     * 
     * @return
     *     possible object is
     *     {@link CTPoint3D }
     *     
     */
    public CTPoint3D getPos() {
        return pos;
    }

    /**
     * Sets the value of the pos property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPoint3D }
     *     
     */
    public void setPos(CTPoint3D value) {
        this.pos = value;
    }

    /**
     * Gets the value of the up property.
     * 
     * @return
     *     possible object is
     *     {@link CTVector3D }
     *     
     */
    public CTVector3D getUp() {
        return up;
    }

    /**
     * Sets the value of the up property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTVector3D }
     *     
     */
    public void setUp(CTVector3D value) {
        this.up = value;
    }

    /**
     * Gets the value of the lookAt property.
     * 
     * @return
     *     possible object is
     *     {@link CTPoint3D }
     *     
     */
    public CTPoint3D getLookAt() {
        return lookAt;
    }

    /**
     * Sets the value of the lookAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPoint3D }
     *     
     */
    public void setLookAt(CTPoint3D value) {
        this.lookAt = value;
    }

    /**
     * Gets the value of the orthographic property.
     * 
     * @return
     *     possible object is
     *     {@link CTOrthographicProjection }
     *     
     */
    public CTOrthographicProjection getOrthographic() {
        return orthographic;
    }

    /**
     * Sets the value of the orthographic property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOrthographicProjection }
     *     
     */
    public void setOrthographic(CTOrthographicProjection value) {
        this.orthographic = value;
    }

    /**
     * Gets the value of the perspective property.
     * 
     * @return
     *     possible object is
     *     {@link CTPerspectiveProjection }
     *     
     */
    public CTPerspectiveProjection getPerspective() {
        return perspective;
    }

    /**
     * Sets the value of the perspective property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPerspectiveProjection }
     *     
     */
    public void setPerspective(CTPerspectiveProjection value) {
        this.perspective = value;
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
