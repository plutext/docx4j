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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_HtmlPublishProperties complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_HtmlPublishProperties"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{http://schemas.openxmlformats.org/presentationml/2006/main}EG_SlideListChoice"/&gt;
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionList" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="showSpeakerNotes" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="target" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="title" type="{http://www.w3.org/2001/XMLSchema}string" default="" /&gt;
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}id use="required""/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_HtmlPublishProperties", propOrder = {
    "sldAll",
    "sldRg",
    "custShow",
    "extLst"
})
public class CTHtmlPublishProperties implements Child
{

    protected CTEmpty sldAll;
    protected CTIndexRange sldRg;
    protected CTCustomShowId custShow;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "showSpeakerNotes")
    protected Boolean showSpeakerNotes;
    @XmlAttribute(name = "target")
    protected String target;
    @XmlAttribute(name = "title")
    protected String title;
    @XmlAttribute(name = "id", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships", required = true)
    protected String id;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the sldAll property.
     * 
     * @return
     *     possible object is
     *     {@link CTEmpty }
     *     
     */
    public CTEmpty getSldAll() {
        return sldAll;
    }

    /**
     * Sets the value of the sldAll property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTEmpty }
     *     
     */
    public void setSldAll(CTEmpty value) {
        this.sldAll = value;
    }

    /**
     * Gets the value of the sldRg property.
     * 
     * @return
     *     possible object is
     *     {@link CTIndexRange }
     *     
     */
    public CTIndexRange getSldRg() {
        return sldRg;
    }

    /**
     * Sets the value of the sldRg property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTIndexRange }
     *     
     */
    public void setSldRg(CTIndexRange value) {
        this.sldRg = value;
    }

    /**
     * Gets the value of the custShow property.
     * 
     * @return
     *     possible object is
     *     {@link CTCustomShowId }
     *     
     */
    public CTCustomShowId getCustShow() {
        return custShow;
    }

    /**
     * Sets the value of the custShow property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCustomShowId }
     *     
     */
    public void setCustShow(CTCustomShowId value) {
        this.custShow = value;
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
     * Gets the value of the showSpeakerNotes property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowSpeakerNotes() {
        if (showSpeakerNotes == null) {
            return true;
        } else {
            return showSpeakerNotes;
        }
    }

    /**
     * Sets the value of the showSpeakerNotes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowSpeakerNotes(Boolean value) {
        this.showSpeakerNotes = value;
    }

    /**
     * Gets the value of the target property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTarget() {
        return target;
    }

    /**
     * Sets the value of the target property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTarget(String value) {
        this.target = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        if (title == null) {
            return "";
        } else {
            return title;
        }
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Publish Path
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
