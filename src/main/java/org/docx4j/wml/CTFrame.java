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
 * <p>Java class for CT_Frame complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Frame">
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
 *         &lt;element name="name" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="sourceFileName" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Rel" minOccurs="0"/>
 *         &lt;element name="marW" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_PixelsMeasure" minOccurs="0"/>
 *         &lt;element name="marH" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_PixelsMeasure" minOccurs="0"/>
 *         &lt;element name="scrollbar" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_FrameScrollbar" minOccurs="0"/>
 *         &lt;element name="noResizeAllowed" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="linkedToFile" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Frame", propOrder = {
    "sz",
    "name",
    "sourceFileName",
    "marW",
    "marH",
    "scrollbar",
    "noResizeAllowed",
    "linkedToFile"
})
public class CTFrame implements Child
{

    protected CTFrame.Sz sz;
    protected CTFrame.Name name;
    protected CTRel sourceFileName;
    protected CTPixelsMeasure marW;
    protected CTPixelsMeasure marH;
    protected CTFrameScrollbar scrollbar;
    protected BooleanDefaultTrue noResizeAllowed;
    protected BooleanDefaultTrue linkedToFile;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the sz property.
     * 
     * @return
     *     possible object is
     *     {@link CTFrame.Sz }
     *     
     */
    public CTFrame.Sz getSz() {
        return sz;
    }

    /**
     * Sets the value of the sz property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFrame.Sz }
     *     
     */
    public void setSz(CTFrame.Sz value) {
        this.sz = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link CTFrame.Name }
     *     
     */
    public CTFrame.Name getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFrame.Name }
     *     
     */
    public void setName(CTFrame.Name value) {
        this.name = value;
    }

    /**
     * Gets the value of the sourceFileName property.
     * 
     * @return
     *     possible object is
     *     {@link CTRel }
     *     
     */
    public CTRel getSourceFileName() {
        return sourceFileName;
    }

    /**
     * Sets the value of the sourceFileName property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRel }
     *     
     */
    public void setSourceFileName(CTRel value) {
        this.sourceFileName = value;
    }

    /**
     * Gets the value of the marW property.
     * 
     * @return
     *     possible object is
     *     {@link CTPixelsMeasure }
     *     
     */
    public CTPixelsMeasure getMarW() {
        return marW;
    }

    /**
     * Sets the value of the marW property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPixelsMeasure }
     *     
     */
    public void setMarW(CTPixelsMeasure value) {
        this.marW = value;
    }

    /**
     * Gets the value of the marH property.
     * 
     * @return
     *     possible object is
     *     {@link CTPixelsMeasure }
     *     
     */
    public CTPixelsMeasure getMarH() {
        return marH;
    }

    /**
     * Sets the value of the marH property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPixelsMeasure }
     *     
     */
    public void setMarH(CTPixelsMeasure value) {
        this.marH = value;
    }

    /**
     * Gets the value of the scrollbar property.
     * 
     * @return
     *     possible object is
     *     {@link CTFrameScrollbar }
     *     
     */
    public CTFrameScrollbar getScrollbar() {
        return scrollbar;
    }

    /**
     * Sets the value of the scrollbar property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFrameScrollbar }
     *     
     */
    public void setScrollbar(CTFrameScrollbar value) {
        this.scrollbar = value;
    }

    /**
     * Gets the value of the noResizeAllowed property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getNoResizeAllowed() {
        return noResizeAllowed;
    }

    /**
     * Sets the value of the noResizeAllowed property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setNoResizeAllowed(BooleanDefaultTrue value) {
        this.noResizeAllowed = value;
    }

    /**
     * Gets the value of the linkedToFile property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getLinkedToFile() {
        return linkedToFile;
    }

    /**
     * Sets the value of the linkedToFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setLinkedToFile(BooleanDefaultTrue value) {
        this.linkedToFile = value;
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
    public static class Name implements Child
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
