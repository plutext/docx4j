/*
 *  Copyright 2010-2012, Plutext Pty Ltd.
 *   
 *  This file is part of pptx4j, a component of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */
package org.pptx4j.pml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTPositiveSize2D;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence minOccurs="0"&gt;
 *         &lt;element name="normalViewPr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_NormalViewProperties" minOccurs="0"/&gt;
 *         &lt;element name="slideViewPr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_SlideViewProperties" minOccurs="0"/&gt;
 *         &lt;element name="outlineViewPr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_OutlineViewProperties" minOccurs="0"/&gt;
 *         &lt;element name="notesTextViewPr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_NotesTextViewProperties" minOccurs="0"/&gt;
 *         &lt;element name="sorterViewPr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_SlideSorterViewProperties" minOccurs="0"/&gt;
 *         &lt;element name="notesViewPr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_NotesViewProperties" minOccurs="0"/&gt;
 *         &lt;element name="gridSpacing" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_PositiveSize2D" minOccurs="0"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="lastView" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_ViewType" default="sldView" /&gt;
 *       &lt;attribute name="showComments" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "normalViewPr",
    "slideViewPr",
    "outlineViewPr",
    "notesTextViewPr",
    "sorterViewPr",
    "notesViewPr",
    "gridSpacing",
    "extLst"
})
@XmlRootElement(name = "viewPr")
public class ViewPr implements Child
{

    protected CTNormalViewProperties normalViewPr;
    protected CTSlideViewProperties slideViewPr;
    protected CTOutlineViewProperties outlineViewPr;
    protected CTNotesTextViewProperties notesTextViewPr;
    protected CTSlideSorterViewProperties sorterViewPr;
    protected CTNotesViewProperties notesViewPr;
    protected CTPositiveSize2D gridSpacing;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "lastView")
    protected STViewType lastView;
    @XmlAttribute(name = "showComments")
    protected Boolean showComments;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the normalViewPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNormalViewProperties }
     *     
     */
    public CTNormalViewProperties getNormalViewPr() {
        return normalViewPr;
    }

    /**
     * Sets the value of the normalViewPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNormalViewProperties }
     *     
     */
    public void setNormalViewPr(CTNormalViewProperties value) {
        this.normalViewPr = value;
    }

    /**
     * Gets the value of the slideViewPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTSlideViewProperties }
     *     
     */
    public CTSlideViewProperties getSlideViewPr() {
        return slideViewPr;
    }

    /**
     * Sets the value of the slideViewPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSlideViewProperties }
     *     
     */
    public void setSlideViewPr(CTSlideViewProperties value) {
        this.slideViewPr = value;
    }

    /**
     * Gets the value of the outlineViewPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTOutlineViewProperties }
     *     
     */
    public CTOutlineViewProperties getOutlineViewPr() {
        return outlineViewPr;
    }

    /**
     * Sets the value of the outlineViewPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOutlineViewProperties }
     *     
     */
    public void setOutlineViewPr(CTOutlineViewProperties value) {
        this.outlineViewPr = value;
    }

    /**
     * Gets the value of the notesTextViewPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNotesTextViewProperties }
     *     
     */
    public CTNotesTextViewProperties getNotesTextViewPr() {
        return notesTextViewPr;
    }

    /**
     * Sets the value of the notesTextViewPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNotesTextViewProperties }
     *     
     */
    public void setNotesTextViewPr(CTNotesTextViewProperties value) {
        this.notesTextViewPr = value;
    }

    /**
     * Gets the value of the sorterViewPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTSlideSorterViewProperties }
     *     
     */
    public CTSlideSorterViewProperties getSorterViewPr() {
        return sorterViewPr;
    }

    /**
     * Sets the value of the sorterViewPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSlideSorterViewProperties }
     *     
     */
    public void setSorterViewPr(CTSlideSorterViewProperties value) {
        this.sorterViewPr = value;
    }

    /**
     * Gets the value of the notesViewPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTNotesViewProperties }
     *     
     */
    public CTNotesViewProperties getNotesViewPr() {
        return notesViewPr;
    }

    /**
     * Sets the value of the notesViewPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTNotesViewProperties }
     *     
     */
    public void setNotesViewPr(CTNotesViewProperties value) {
        this.notesViewPr = value;
    }

    /**
     * Gets the value of the gridSpacing property.
     * 
     * @return
     *     possible object is
     *     {@link CTPositiveSize2D }
     *     
     */
    public CTPositiveSize2D getGridSpacing() {
        return gridSpacing;
    }

    /**
     * Sets the value of the gridSpacing property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPositiveSize2D }
     *     
     */
    public void setGridSpacing(CTPositiveSize2D value) {
        this.gridSpacing = value;
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
     * Gets the value of the lastView property.
     * 
     * @return
     *     possible object is
     *     {@link STViewType }
     *     
     */
    public STViewType getLastView() {
        if (lastView == null) {
            return STViewType.SLD_VIEW;
        } else {
            return lastView;
        }
    }

    /**
     * Sets the value of the lastView property.
     * 
     * @param value
     *     allowed object is
     *     {@link STViewType }
     *     
     */
    public void setLastView(STViewType value) {
        this.lastView = value;
    }

    /**
     * Gets the value of the showComments property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowComments() {
        if (showComments == null) {
            return true;
        } else {
            return showComments;
        }
    }

    /**
     * Sets the value of the showComments property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowComments(Boolean value) {
        this.showComments = value;
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
