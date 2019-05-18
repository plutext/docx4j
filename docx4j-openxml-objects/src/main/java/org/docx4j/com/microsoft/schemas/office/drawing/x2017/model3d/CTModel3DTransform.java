
package org.docx4j.com.microsoft.schemas.office.drawing.x2017.model3d;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.docx4j.dml.CTVector3D;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Model3DTransform complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Model3DTransform"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="meterPerModelUnit" type="{http://schemas.microsoft.com/office/drawing/2017/model3d}CT_PositiveRatio" minOccurs="0"/&gt;
 *         &lt;element name="preTrans" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Vector3D" minOccurs="0"/&gt;
 *         &lt;element name="scale" type="{http://schemas.microsoft.com/office/drawing/2017/model3d}CT_Scale3D" minOccurs="0"/&gt;
 *         &lt;element name="rot" type="{http://schemas.microsoft.com/office/drawing/2017/model3d}CT_Rotate3D" minOccurs="0"/&gt;
 *         &lt;element name="postTrans" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_Vector3D" minOccurs="0"/&gt;
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
@XmlType(name = "CT_Model3DTransform", propOrder = {
    "meterPerModelUnit",
    "preTrans",
    "scale",
    "rot",
    "postTrans",
    "extLst"
})
public class CTModel3DTransform implements Child
{

    protected CTPositiveRatio meterPerModelUnit;
    protected CTVector3D preTrans;
    protected CTScale3D scale;
    protected CTRotate3D rot;
    protected CTVector3D postTrans;
    protected CTOfficeArtExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the meterPerModelUnit property.
     * 
     * @return
     *     possible object is
     *     {@link CTPositiveRatio }
     *     
     */
    public CTPositiveRatio getMeterPerModelUnit() {
        return meterPerModelUnit;
    }

    /**
     * Sets the value of the meterPerModelUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPositiveRatio }
     *     
     */
    public void setMeterPerModelUnit(CTPositiveRatio value) {
        this.meterPerModelUnit = value;
    }

    /**
     * Gets the value of the preTrans property.
     * 
     * @return
     *     possible object is
     *     {@link CTVector3D }
     *     
     */
    public CTVector3D getPreTrans() {
        return preTrans;
    }

    /**
     * Sets the value of the preTrans property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTVector3D }
     *     
     */
    public void setPreTrans(CTVector3D value) {
        this.preTrans = value;
    }

    /**
     * Gets the value of the scale property.
     * 
     * @return
     *     possible object is
     *     {@link CTScale3D }
     *     
     */
    public CTScale3D getScale() {
        return scale;
    }

    /**
     * Sets the value of the scale property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTScale3D }
     *     
     */
    public void setScale(CTScale3D value) {
        this.scale = value;
    }

    /**
     * Gets the value of the rot property.
     * 
     * @return
     *     possible object is
     *     {@link CTRotate3D }
     *     
     */
    public CTRotate3D getRot() {
        return rot;
    }

    /**
     * Sets the value of the rot property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRotate3D }
     *     
     */
    public void setRot(CTRotate3D value) {
        this.rot = value;
    }

    /**
     * Gets the value of the postTrans property.
     * 
     * @return
     *     possible object is
     *     {@link CTVector3D }
     *     
     */
    public CTVector3D getPostTrans() {
        return postTrans;
    }

    /**
     * Sets the value of the postTrans property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTVector3D }
     *     
     */
    public void setPostTrans(CTVector3D value) {
        this.postTrans = value;
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
