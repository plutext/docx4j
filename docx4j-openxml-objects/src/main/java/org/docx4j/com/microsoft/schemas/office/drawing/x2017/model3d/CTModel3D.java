
package org.docx4j.com.microsoft.schemas.office.drawing.x2017.model3d;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.com.microsoft.schemas.office.drawing.x201611.main.CTPictureAttributionSourceURL;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.docx4j.dml.CTShapeProperties;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Model3D complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Model3D"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="spPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ShapeProperties"/&gt;
 *         &lt;element name="camera" type="{http://schemas.microsoft.com/office/drawing/2017/model3d}CT_Model3DCamera"/&gt;
 *         &lt;element name="trans" type="{http://schemas.microsoft.com/office/drawing/2017/model3d}CT_Model3DTransform"/&gt;
 *         &lt;element name="attrSrcUrl" type="{http://schemas.microsoft.com/office/drawing/2016/11/main}CT_PictureAttributionSourceURL" minOccurs="0"/&gt;
 *         &lt;element name="raster" type="{http://schemas.microsoft.com/office/drawing/2017/model3d}CT_Model3DRaster" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_OfficeArtExtensionList" minOccurs="0"/&gt;
 *         &lt;choice&gt;
 *           &lt;element name="objViewport" type="{http://schemas.microsoft.com/office/drawing/2017/model3d}CT_ObjectViewport"/&gt;
 *           &lt;element name="winViewport" type="{http://schemas.microsoft.com/office/drawing/2017/model3d}CT_WindowViewport"/&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="ambientLight" type="{http://schemas.microsoft.com/office/drawing/2017/model3d}CT_AmbientLight" minOccurs="0"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="ptLight" type="{http://schemas.microsoft.com/office/drawing/2017/model3d}CT_PointLight"/&gt;
 *           &lt;element name="spotLight" type="{http://schemas.microsoft.com/office/drawing/2017/model3d}CT_SpotLight"/&gt;
 *           &lt;element name="dirLight" type="{http://schemas.microsoft.com/office/drawing/2017/model3d}CT_DirectionalLight"/&gt;
 *           &lt;element name="unkLight" type="{http://schemas.microsoft.com/office/drawing/2017/model3d}CT_UnknownLight"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/drawingml/2006/main}AG_Blob"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Model3D", propOrder = {
    "spPr",
    "camera",
    "trans",
    "attrSrcUrl",
    "raster",
    "extLst",
    "objViewport",
    "winViewport",
    "ambientLight",
    "ptLightOrSpotLightOrDirLight"
})
public class CTModel3D implements Child
{

    @XmlElement(required = true)
    protected CTShapeProperties spPr;
    @XmlElement(required = true)
    protected CTModel3DCamera camera;
    @XmlElement(required = true)
    protected CTModel3DTransform trans;
    protected CTPictureAttributionSourceURL attrSrcUrl;
    protected CTModel3DRaster raster;
    protected CTOfficeArtExtensionList extLst;
    protected CTObjectViewport objViewport;
    protected CTWindowViewport winViewport;
    protected CTAmbientLight ambientLight;
    @XmlElements({
        @XmlElement(name = "ptLight", type = CTPointLight.class),
        @XmlElement(name = "spotLight", type = CTSpotLight.class),
        @XmlElement(name = "dirLight", type = CTDirectionalLight.class),
        @XmlElement(name = "unkLight", type = CTUnknownLight.class)
    })
    protected List<Object> ptLightOrSpotLightOrDirLight;
    @XmlAttribute(name = "embed", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String embed;
    @XmlAttribute(name = "link", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String link;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the spPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTShapeProperties }
     *     
     */
    public CTShapeProperties getSpPr() {
        return spPr;
    }

    /**
     * Sets the value of the spPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShapeProperties }
     *     
     */
    public void setSpPr(CTShapeProperties value) {
        this.spPr = value;
    }

    /**
     * Gets the value of the camera property.
     * 
     * @return
     *     possible object is
     *     {@link CTModel3DCamera }
     *     
     */
    public CTModel3DCamera getCamera() {
        return camera;
    }

    /**
     * Sets the value of the camera property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTModel3DCamera }
     *     
     */
    public void setCamera(CTModel3DCamera value) {
        this.camera = value;
    }

    /**
     * Gets the value of the trans property.
     * 
     * @return
     *     possible object is
     *     {@link CTModel3DTransform }
     *     
     */
    public CTModel3DTransform getTrans() {
        return trans;
    }

    /**
     * Sets the value of the trans property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTModel3DTransform }
     *     
     */
    public void setTrans(CTModel3DTransform value) {
        this.trans = value;
    }

    /**
     * Gets the value of the attrSrcUrl property.
     * 
     * @return
     *     possible object is
     *     {@link CTPictureAttributionSourceURL }
     *     
     */
    public CTPictureAttributionSourceURL getAttrSrcUrl() {
        return attrSrcUrl;
    }

    /**
     * Sets the value of the attrSrcUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPictureAttributionSourceURL }
     *     
     */
    public void setAttrSrcUrl(CTPictureAttributionSourceURL value) {
        this.attrSrcUrl = value;
    }

    /**
     * Gets the value of the raster property.
     * 
     * @return
     *     possible object is
     *     {@link CTModel3DRaster }
     *     
     */
    public CTModel3DRaster getRaster() {
        return raster;
    }

    /**
     * Sets the value of the raster property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTModel3DRaster }
     *     
     */
    public void setRaster(CTModel3DRaster value) {
        this.raster = value;
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
     * Gets the value of the objViewport property.
     * 
     * @return
     *     possible object is
     *     {@link CTObjectViewport }
     *     
     */
    public CTObjectViewport getObjViewport() {
        return objViewport;
    }

    /**
     * Sets the value of the objViewport property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTObjectViewport }
     *     
     */
    public void setObjViewport(CTObjectViewport value) {
        this.objViewport = value;
    }

    /**
     * Gets the value of the winViewport property.
     * 
     * @return
     *     possible object is
     *     {@link CTWindowViewport }
     *     
     */
    public CTWindowViewport getWinViewport() {
        return winViewport;
    }

    /**
     * Sets the value of the winViewport property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWindowViewport }
     *     
     */
    public void setWinViewport(CTWindowViewport value) {
        this.winViewport = value;
    }

    /**
     * Gets the value of the ambientLight property.
     * 
     * @return
     *     possible object is
     *     {@link CTAmbientLight }
     *     
     */
    public CTAmbientLight getAmbientLight() {
        return ambientLight;
    }

    /**
     * Sets the value of the ambientLight property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTAmbientLight }
     *     
     */
    public void setAmbientLight(CTAmbientLight value) {
        this.ambientLight = value;
    }

    /**
     * Gets the value of the ptLightOrSpotLightOrDirLight property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the ptLightOrSpotLightOrDirLight property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPtLightOrSpotLightOrDirLight().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTPointLight }
     * {@link CTSpotLight }
     * {@link CTDirectionalLight }
     * {@link CTUnknownLight }
     * 
     * 
     */
    public List<Object> getPtLightOrSpotLightOrDirLight() {
        if (ptLightOrSpotLightOrDirLight == null) {
            ptLightOrSpotLightOrDirLight = new ArrayList<Object>();
        }
        return this.ptLightOrSpotLightOrDirLight;
    }

    /**
     * Embedded Picture Reference
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmbed() {
        if (embed == null) {
            return "";
        } else {
            return embed;
        }
    }

    /**
     * Sets the value of the embed property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmbed(String value) {
        this.embed = value;
    }

    /**
     * Linked Picture Reference
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLink() {
        if (link == null) {
            return "";
        } else {
            return link;
        }
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLink(String value) {
        this.link = value;
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
