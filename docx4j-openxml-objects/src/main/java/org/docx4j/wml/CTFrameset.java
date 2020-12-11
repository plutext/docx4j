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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Frameset complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Frameset">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sz" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="framesetSplitbar" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FramesetSplitbar" minOccurs="0"/>
 *         &lt;element name="frameLayout" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FrameLayout" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="frameset" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Frameset" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="frame" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Frame" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Frameset", propOrder = {
    "sz",
    "framesetSplitbar",
    "frameLayout",
    "framesetOrFrame"
})
public class CTFrameset implements Child
{

    protected CTFrameset.Sz sz;
    protected CTFramesetSplitbar framesetSplitbar;
    protected CTFrameLayout frameLayout;
    @XmlElements({
        @XmlElement(name = "frameset", type = CTFrameset.class),
        @XmlElement(name = "frame", type = CTFrame.class)
    })
    protected List<Object> framesetOrFrame  = new ArrayListWml<Object>(this);
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the sz property.
     * 
     * @return
     *     possible object is
     *     {@link CTFrameset.Sz }
     *     
     */
    public CTFrameset.Sz getSz() {
        return sz;
    }

    /**
     * Sets the value of the sz property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFrameset.Sz }
     *     
     */
    public void setSz(CTFrameset.Sz value) {
        this.sz = value;
    }

    /**
     * Gets the value of the framesetSplitbar property.
     * 
     * @return
     *     possible object is
     *     {@link CTFramesetSplitbar }
     *     
     */
    public CTFramesetSplitbar getFramesetSplitbar() {
        return framesetSplitbar;
    }

    /**
     * Sets the value of the framesetSplitbar property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFramesetSplitbar }
     *     
     */
    public void setFramesetSplitbar(CTFramesetSplitbar value) {
        this.framesetSplitbar = value;
    }

    /**
     * Gets the value of the frameLayout property.
     * 
     * @return
     *     possible object is
     *     {@link CTFrameLayout }
     *     
     */
    public CTFrameLayout getFrameLayout() {
        return frameLayout;
    }

    /**
     * Sets the value of the frameLayout property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFrameLayout }
     *     
     */
    public void setFrameLayout(CTFrameLayout value) {
        this.frameLayout = value;
    }

    /**
     * Gets the value of the framesetOrFrame property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the framesetOrFrame property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFramesetOrFrame().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTFrameset }
     * {@link CTFrame }
     * 
     * 
     */
    public List<Object> getFramesetOrFrame() {
        if (framesetOrFrame == null) {
            framesetOrFrame  = new ArrayListWml<Object>(this);
        }
        return this.framesetOrFrame;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Sz implements Child
    {

        @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
        protected String val;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the val property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getVal() {
            return val;
        }

        /**
         * Sets the value of the val property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setVal(String value) {
            this.val = value;
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

}
