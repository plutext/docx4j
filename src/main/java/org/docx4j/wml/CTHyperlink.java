/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

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


package org.docx4j.wml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Hyperlink complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Hyperlink">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;group ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}EG_PContent" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;attribute name="tgtFrame" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
 *       &lt;attribute name="tooltip" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
 *       &lt;attribute name="docLocation" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
 *       &lt;attribute name="history" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="anchor" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_String" />
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}id"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Hyperlink", propOrder = {
    "paragraphContent"
})
public class CTHyperlink
    implements Child
{

    @XmlElementRefs({
        @XmlElementRef(name = "moveFrom", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "moveTo", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "ins", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = RunIns.class),
        @XmlElementRef(name = "hyperlink", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlMoveToRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "fldSimple", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "customXml", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "moveToRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "smartTag", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "subDoc", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "moveFromRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "bookmarkStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlMoveFromRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "commentRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = CommentRangeEnd.class),
        @XmlElementRef(name = "customXmlMoveFromRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "permStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "moveFromRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "sdt", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "proofErr", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = ProofErr.class),
        @XmlElementRef(name = "customXmlDelRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlInsRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "bookmarkEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "permEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "commentRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = CommentRangeStart.class),
        @XmlElementRef(name = "customXmlDelRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "customXmlMoveToRangeStart", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "del", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = RunDel.class),
        @XmlElementRef(name = "moveToRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class),
        @XmlElementRef(name = "r", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = R.class),
        @XmlElementRef(name = "customXmlInsRangeEnd", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", type = JAXBElement.class)
    })
    protected List<Object> paragraphContent;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String tgtFrame;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String tooltip;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String docLocation;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected Boolean history;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String anchor;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String id;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the paragraphContent property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the paragraphContent property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParagraphContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}
     * {@link JAXBElement }{@code <}{@link RunTrackChange }{@code >}
     * {@link RunIns }
     * {@link JAXBElement }{@code <}{@link CTHyperlink }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSimpleField }{@code >}
     * {@link JAXBElement }{@code <}{@link CTCustomXmlRun }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMoveBookmark }{@code >}
     * {@link JAXBElement }{@code <}{@link CTSmartTagRun }{@code >}
     * {@link JAXBElement }{@code <}{@link CTRel }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMoveBookmark }{@code >}
     * {@link JAXBElement }{@code <}{@link CTBookmark }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
     * {@link CommentRangeEnd }
     * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
     * {@link JAXBElement }{@code <}{@link RangePermissionStart }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}
     * {@link JAXBElement }{@code <}{@link SdtRun }{@code >}
     * {@link ProofErr }
     * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTPerm }{@code >}
     * {@link CommentRangeStart }
     * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
     * {@link JAXBElement }{@code <}{@link CTTrackChange }{@code >}
     * {@link RunDel }
     * {@link JAXBElement }{@code <}{@link CTMarkupRange }{@code >}
     * {@link R }
     * {@link JAXBElement }{@code <}{@link CTMarkup }{@code >}
     * 
     * 
     */
    public List<Object> getParagraphContent() {
        if (paragraphContent == null) {
            paragraphContent = new ArrayList<Object>();
        }
        return this.paragraphContent;
    }

    /**
     * Gets the value of the tgtFrame property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTgtFrame() {
        return tgtFrame;
    }

    /**
     * Sets the value of the tgtFrame property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTgtFrame(String value) {
        this.tgtFrame = value;
    }

    /**
     * Gets the value of the tooltip property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTooltip() {
        return tooltip;
    }

    /**
     * Sets the value of the tooltip property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTooltip(String value) {
        this.tooltip = value;
    }

    /**
     * Gets the value of the docLocation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocLocation() {
        return docLocation;
    }

    /**
     * Sets the value of the docLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocLocation(String value) {
        this.docLocation = value;
    }

    /**
     * Gets the value of the history property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHistory() {
        if (history == null) {
            return true;
        } else {
            return history;
        }
    }

    /**
     * Sets the value of the history property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHistory(Boolean value) {
        this.history = value;
    }

    /**
     * Gets the value of the anchor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAnchor() {
        return anchor;
    }

    /**
     * Sets the value of the anchor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAnchor(String value) {
        this.anchor = value;
    }

    /**
     * Hyperlink Target
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
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
