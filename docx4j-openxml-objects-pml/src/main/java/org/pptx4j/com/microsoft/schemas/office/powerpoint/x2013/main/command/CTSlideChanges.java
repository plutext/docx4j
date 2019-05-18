
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2013.main.command;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.com.microsoft.schemas.office.drawing.x2013.main.command.CTChangesData;
import org.docx4j.com.microsoft.schemas.office.drawing.x2013.main.command.CTConnectorChanges;
import org.docx4j.com.microsoft.schemas.office.drawing.x2013.main.command.CTGraphicFrameChanges;
import org.docx4j.com.microsoft.schemas.office.drawing.x2013.main.command.CTGroupShapeChanges;
import org.docx4j.com.microsoft.schemas.office.drawing.x2013.main.command.CTInkChanges;
import org.docx4j.com.microsoft.schemas.office.drawing.x2013.main.command.CTPictureChanges;
import org.docx4j.com.microsoft.schemas.office.drawing.x2013.main.command.CTShapeChanges;
import org.pptx4j.pml.CTExtensionList;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_SlideChanges complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SlideChanges"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="chgData" type="{http://schemas.microsoft.com/office/drawing/2013/main/command}CT_ChangesData" minOccurs="0"/&gt;
 *         &lt;element name="sldMkLst" type="{http://schemas.microsoft.com/office/powerpoint/2013/main/command}CT_SlideMonikerList"/&gt;
 *         &lt;element name="spChg" type="{http://schemas.microsoft.com/office/drawing/2013/main/command}CT_ShapeChanges" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="grpChg" type="{http://schemas.microsoft.com/office/drawing/2013/main/command}CT_GroupShapeChanges" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="graphicFrameChg" type="{http://schemas.microsoft.com/office/drawing/2013/main/command}CT_GraphicFrameChanges" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="picChg" type="{http://schemas.microsoft.com/office/drawing/2013/main/command}CT_PictureChanges" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="inkChg" type="{http://schemas.microsoft.com/office/drawing/2013/main/command}CT_InkChanges" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="cxnChg" type="{http://schemas.microsoft.com/office/drawing/2013/main/command}CT_ConnectorChanges" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="cmChg" type="{http://schemas.microsoft.com/office/powerpoint/2013/main/command}CT_CommentChanges" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="chg" use="required" type="{http://schemas.microsoft.com/office/powerpoint/2013/main/command}ST_SlideChangeBits" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_SlideChanges", propOrder = {
    "chgData",
    "sldMkLst",
    "spChg",
    "grpChg",
    "graphicFrameChg",
    "picChg",
    "inkChg",
    "cxnChg",
    "cmChg",
    "extLst"
})
public class CTSlideChanges implements Child
{

    protected CTChangesData chgData;
    @XmlElement(required = true)
    protected CTSlideMonikerList sldMkLst;
    protected List<CTShapeChanges> spChg;
    protected List<CTGroupShapeChanges> grpChg;
    protected List<CTGraphicFrameChanges> graphicFrameChg;
    protected List<CTPictureChanges> picChg;
    protected List<CTInkChanges> inkChg;
    protected List<CTConnectorChanges> cxnChg;
    protected List<CTCommentChanges> cmChg;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "chg", required = true)
    protected List<STSlideChangeBit> chg;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the chgData property.
     * 
     * @return
     *     possible object is
     *     {@link CTChangesData }
     *     
     */
    public CTChangesData getChgData() {
        return chgData;
    }

    /**
     * Sets the value of the chgData property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTChangesData }
     *     
     */
    public void setChgData(CTChangesData value) {
        this.chgData = value;
    }

    /**
     * Gets the value of the sldMkLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTSlideMonikerList }
     *     
     */
    public CTSlideMonikerList getSldMkLst() {
        return sldMkLst;
    }

    /**
     * Sets the value of the sldMkLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSlideMonikerList }
     *     
     */
    public void setSldMkLst(CTSlideMonikerList value) {
        this.sldMkLst = value;
    }

    /**
     * Gets the value of the spChg property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the spChg property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSpChg().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTShapeChanges }
     * 
     * 
     */
    public List<CTShapeChanges> getSpChg() {
        if (spChg == null) {
            spChg = new ArrayList<CTShapeChanges>();
        }
        return this.spChg;
    }

    /**
     * Gets the value of the grpChg property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the grpChg property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGrpChg().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTGroupShapeChanges }
     * 
     * 
     */
    public List<CTGroupShapeChanges> getGrpChg() {
        if (grpChg == null) {
            grpChg = new ArrayList<CTGroupShapeChanges>();
        }
        return this.grpChg;
    }

    /**
     * Gets the value of the graphicFrameChg property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the graphicFrameChg property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGraphicFrameChg().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTGraphicFrameChanges }
     * 
     * 
     */
    public List<CTGraphicFrameChanges> getGraphicFrameChg() {
        if (graphicFrameChg == null) {
            graphicFrameChg = new ArrayList<CTGraphicFrameChanges>();
        }
        return this.graphicFrameChg;
    }

    /**
     * Gets the value of the picChg property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the picChg property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPicChg().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTPictureChanges }
     * 
     * 
     */
    public List<CTPictureChanges> getPicChg() {
        if (picChg == null) {
            picChg = new ArrayList<CTPictureChanges>();
        }
        return this.picChg;
    }

    /**
     * Gets the value of the inkChg property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the inkChg property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInkChg().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTInkChanges }
     * 
     * 
     */
    public List<CTInkChanges> getInkChg() {
        if (inkChg == null) {
            inkChg = new ArrayList<CTInkChanges>();
        }
        return this.inkChg;
    }

    /**
     * Gets the value of the cxnChg property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cxnChg property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCxnChg().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTConnectorChanges }
     * 
     * 
     */
    public List<CTConnectorChanges> getCxnChg() {
        if (cxnChg == null) {
            cxnChg = new ArrayList<CTConnectorChanges>();
        }
        return this.cxnChg;
    }

    /**
     * Gets the value of the cmChg property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cmChg property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCmChg().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTCommentChanges }
     * 
     * 
     */
    public List<CTCommentChanges> getCmChg() {
        if (cmChg == null) {
            cmChg = new ArrayList<CTCommentChanges>();
        }
        return this.cmChg;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
    }

    /**
     * Gets the value of the chg property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the chg property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChg().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link STSlideChangeBit }
     * 
     * 
     */
    public List<STSlideChangeBit> getChg() {
        if (chg == null) {
            chg = new ArrayList<STSlideChangeBit>();
        }
        return this.chg;
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
