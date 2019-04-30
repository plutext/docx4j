
package org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingCanvas;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup.CTGraphicFrame;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingGroup.CTWordprocessingGroup;
import org.docx4j.com.microsoft.schemas.office.word.x2010.wordprocessingShape.CTWordprocessingShape;
import org.docx4j.dml.CTBackgroundFormatting;
import org.docx4j.dml.CTOfficeArtExtensionList;
import org.docx4j.dml.CTWholeE2OFormatting;
import org.docx4j.dml.picture.Pic;
import org.docx4j.w14.CTWordContentPart;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_WordprocessingCanvas complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_WordprocessingCanvas"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="bg" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_BackgroundFormatting" minOccurs="0"/&gt;
 *         &lt;element name="whole" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_WholeE2oFormatting" minOccurs="0"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element ref="{http://schemas.microsoft.com/office/word/2010/wordprocessingShape}wsp"/&gt;
 *           &lt;element ref="{http://schemas.openxmlformats.org/drawingml/2006/picture}pic"/&gt;
 *           &lt;element ref="{http://schemas.microsoft.com/office/word/2010/wordml}contentPart"/&gt;
 *           &lt;element ref="{http://schemas.microsoft.com/office/word/2010/wordprocessingGroup}wgp"/&gt;
 *           &lt;element name="graphicFrame" type="{http://schemas.microsoft.com/office/word/2010/wordprocessingGroup}CT_GraphicFrame"/&gt;
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
@XmlType(name = "CT_WordprocessingCanvas", propOrder = {
    "bg",
    "whole",
    "wspOrPicOrContentPart",
    "extLst"
})
public class CTWordprocessingCanvas implements Child
{

    protected CTBackgroundFormatting bg;
    protected CTWholeE2OFormatting whole;
    @XmlElements({
        @XmlElement(name = "wsp", namespace = "http://schemas.microsoft.com/office/word/2010/wordprocessingShape", type = CTWordprocessingShape.class),
        @XmlElement(name = "pic", namespace = "http://schemas.openxmlformats.org/drawingml/2006/picture", type = Pic.class),
        @XmlElement(name = "contentPart", namespace = "http://schemas.microsoft.com/office/word/2010/wordml", type = CTWordContentPart.class),
        @XmlElement(name = "wgp", namespace = "http://schemas.microsoft.com/office/word/2010/wordprocessingGroup", type = CTWordprocessingGroup.class),
        @XmlElement(name = "graphicFrame", type = CTGraphicFrame.class)
    })
    protected List<Object> wspOrPicOrContentPart;
    protected CTOfficeArtExtensionList extLst;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the bg property.
     * 
     * @return
     *     possible object is
     *     {@link CTBackgroundFormatting }
     *     
     */
    public CTBackgroundFormatting getBg() {
        return bg;
    }

    /**
     * Sets the value of the bg property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBackgroundFormatting }
     *     
     */
    public void setBg(CTBackgroundFormatting value) {
        this.bg = value;
    }

    /**
     * Gets the value of the whole property.
     * 
     * @return
     *     possible object is
     *     {@link CTWholeE2OFormatting }
     *     
     */
    public CTWholeE2OFormatting getWhole() {
        return whole;
    }

    /**
     * Sets the value of the whole property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWholeE2OFormatting }
     *     
     */
    public void setWhole(CTWholeE2OFormatting value) {
        this.whole = value;
    }

    /**
     * Gets the value of the wspOrPicOrContentPart property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the wspOrPicOrContentPart property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWspOrPicOrContentPart().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTWordprocessingShape }
     * {@link Pic }
     * {@link CTWordContentPart }
     * {@link CTWordprocessingGroup }
     * {@link CTGraphicFrame }
     * 
     * 
     */
    public List<Object> getWspOrPicOrContentPart() {
        if (wspOrPicOrContentPart == null) {
            wspOrPicOrContentPart = new ArrayList<Object>();
        }
        return this.wspOrPicOrContentPart;
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
