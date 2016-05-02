/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TrackChangesView complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TrackChangesView">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="markup" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="comments" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="insDel" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="formatting" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="inkAnnotations" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TrackChangesView")
public class CTTrackChangesView implements Child
{

    @XmlAttribute(name = "markup", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected Boolean markup;
    @XmlAttribute(name = "comments", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected Boolean comments;
    @XmlAttribute(name = "insDel", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected Boolean insDel;
    @XmlAttribute(name = "formatting", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected Boolean formatting;
    @XmlAttribute(name = "inkAnnotations", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected Boolean inkAnnotations;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the markup property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMarkup() {
        if (markup == null) {
            return true;
        } else {
            return markup;
        }
    }

    /**
     * Sets the value of the markup property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMarkup(Boolean value) {
        this.markup = value;
    }

    /**
     * Gets the value of the comments property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isComments() {
        if (comments == null) {
            return true;
        } else {
            return comments;
        }
    }

    /**
     * Sets the value of the comments property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setComments(Boolean value) {
        this.comments = value;
    }

    /**
     * Gets the value of the insDel property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isInsDel() {
        if (insDel == null) {
            return true;
        } else {
            return insDel;
        }
    }

    /**
     * Sets the value of the insDel property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInsDel(Boolean value) {
        this.insDel = value;
    }

    /**
     * Gets the value of the formatting property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFormatting() {
        if (formatting == null) {
            return true;
        } else {
            return formatting;
        }
    }

    /**
     * Sets the value of the formatting property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFormatting(Boolean value) {
        this.formatting = value;
    }

    /**
     * Gets the value of the inkAnnotations property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isInkAnnotations() {
        if (inkAnnotations == null) {
            return true;
        } else {
            return inkAnnotations;
        }
    }

    /**
     * Sets the value of the inkAnnotations property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInkAnnotations(Boolean value) {
        this.inkAnnotations = value;
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
