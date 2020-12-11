
package org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.dml.CTGroupShapeProperties;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTNonVisualGroupDrawingShapeProps;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.docx4j.dml.picture.Pic;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_WordprocessingGroup complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_WordprocessingGroup"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="cNvPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualDrawingProps" minOccurs="0"/&gt;
 *         &lt;element name="cNvGrpSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_NonVisualGroupDrawingShapeProps"/&gt;
 *         &lt;element name="grpSpPr" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_GroupShapeProperties"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element ref="{http://schemas.microsoft.com/office/word/2010/wordprocessingShape}wsp"/&gt;
 *           &lt;element name="grpSp" type="{http://schemas.microsoft.com/office/word/2010/wordprocessingGroup}CT_WordprocessingGroup"/&gt;
 *           &lt;element name="graphicFrame" type="{http://schemas.microsoft.com/office/word/2010/wordprocessingGroup}CT_GraphicFrame"/&gt;
 *           &lt;element ref="{http://schemas.openxmlformats.org/drawingml/2006/picture}pic"/&gt;
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
@XmlType(name = "CT_WordprocessingGroup", propOrder = {
    "cNvPr",
    "cNvGrpSpPr",
    "grpSpPr",
    "wspOrGrpSpOrGraphicFrame",
    "extLst"
})
public class CTWordprocessingGroup implements Child
{

    protected CTNonVisualDrawingProps cNvPr;
    @XmlElement(required = true)
    protected CTNonVisualGroupDrawingShapeProps cNvGrpSpPr;
    @XmlElement(required = true)
    protected CTGroupShapeProperties grpSpPr;
    @XmlElements({
        @XmlElement(name = "wsp", namespace = "http://schemas.microsoft.com/office/word/2010/wordprocessingShape", type = CTWordprocessingShape.class),
        @XmlElement(name = "grpSp", type = CTWordprocessingGroup.class),
        @XmlElement(name = "graphicFrame", type = CTGraphicFrame.class),
        @XmlElement(name = "pic", namespace = "http://schemas.openxmlformats.org/drawingml/2006/picture", type = Pic.class)
    })
    protected List<Object> wspOrGrpSpOrGraphicFrame;
    protected CTOfficeArtExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the cNvPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNonVisualDrawingProps }
     *     
     */
    public CTNonVisualDrawingProps getCNvPr() {
        return cNvPr;
    }

    /**
     * Sets the value of the cNvPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNonVisualDrawingProps }
     *     
     */
    public void setCNvPr(CTNonVisualDrawingProps value) {
        this.cNvPr = value;
    }

    /**
     * Gets the value of the cNvGrpSpPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNonVisualGroupDrawingShapeProps }
     *     
     */
    public CTNonVisualGroupDrawingShapeProps getCNvGrpSpPr() {
        return cNvGrpSpPr;
    }

    /**
     * Sets the value of the cNvGrpSpPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNonVisualGroupDrawingShapeProps }
     *     
     */
    public void setCNvGrpSpPr(CTNonVisualGroupDrawingShapeProps value) {
        this.cNvGrpSpPr = value;
    }

    /**
     * Gets the value of the grpSpPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTGroupShapeProperties }
     *     
     */
    public CTGroupShapeProperties getGrpSpPr() {
        return grpSpPr;
    }

    /**
     * Sets the value of the grpSpPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTGroupShapeProperties }
     *     
     */
    public void setGrpSpPr(CTGroupShapeProperties value) {
        this.grpSpPr = value;
    }

    /**
     * Gets the value of the wspOrGrpSpOrGraphicFrame property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the wspOrGrpSpOrGraphicFrame property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWspOrGrpSpOrGraphicFrame().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTWordprocessingShape }
     * {@link CTWordprocessingGroup }
     * {@link CTGraphicFrame }
     * {@link Pic }
     * 
     * 
     */
    public List<Object> getWspOrGrpSpOrGraphicFrame() {
        if (wspOrGrpSpOrGraphicFrame == null) {
            wspOrGrpSpOrGraphicFrame = new ArrayList<Object>();
        }
        return this.wspOrGrpSpOrGraphicFrame;
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
