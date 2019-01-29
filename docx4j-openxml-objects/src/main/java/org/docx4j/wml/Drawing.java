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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Drawing complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Drawing">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element ref="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}anchor" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}inline" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Drawing", propOrder = {
    "anchorOrInline"
})
@XmlRootElement(name ="drawing")
public class Drawing
    implements Child
{

    @XmlElements({
        @XmlElement(name = "anchor", namespace = "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", type = Anchor.class),
        @XmlElement(name = "inline", namespace = "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", type = Inline.class)
    })
    protected List<Object> anchorOrInline  = new ArrayListWml<Object>(this);
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the anchorOrInline property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the anchorOrInline property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnchorOrInline().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Anchor }
     * {@link Inline }
     * 
     * 
     */
    public List<Object> getAnchorOrInline() {
        if (anchorOrInline == null) {
            anchorOrInline  = new ArrayListWml<Object>(this);
        }
        return this.anchorOrInline;
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
