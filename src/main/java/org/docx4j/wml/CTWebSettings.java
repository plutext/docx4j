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

import java.math.BigInteger;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_WebSettings complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_WebSettings">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="frameset" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Frameset" minOccurs="0"/>
 *         &lt;element name="divs" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Divs" minOccurs="0"/>
 *         &lt;element name="encoding" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="optimizeForBrowser" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="relyOnVML" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="allowPNG" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotRelyOnCSS" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotSaveAsSingleFile" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotOrganizeInFolder" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="doNotUseLongFileNames" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *         &lt;element name="pixelsPerInch" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="val" use="required">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="targetScreenSz" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_TargetScreenSz" minOccurs="0"/>
 *         &lt;element name="saveSmartTagsAsXml" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}BooleanDefaultTrue" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_WebSettings", propOrder = {
    "frameset",
    "divs",
    "encoding",
    "optimizeForBrowser",
    "relyOnVML",
    "allowPNG",
    "doNotRelyOnCSS",
    "doNotSaveAsSingleFile",
    "doNotOrganizeInFolder",
    "doNotUseLongFileNames",
    "pixelsPerInch",
    "targetScreenSz",
    "saveSmartTagsAsXml"
})
@XmlRootElement(name = "webSettings")
public class CTWebSettings
    implements Child
{

    protected CTFrameset frameset;
    protected CTDivs divs;
    protected CTWebSettings.Encoding encoding;
    protected BooleanDefaultTrue optimizeForBrowser;
    protected BooleanDefaultTrue relyOnVML;
    protected BooleanDefaultTrue allowPNG;
    protected BooleanDefaultTrue doNotRelyOnCSS;
    protected BooleanDefaultTrue doNotSaveAsSingleFile;
    protected BooleanDefaultTrue doNotOrganizeInFolder;
    protected BooleanDefaultTrue doNotUseLongFileNames;
    protected CTWebSettings.PixelsPerInch pixelsPerInch;
    protected CTTargetScreenSz targetScreenSz;
    protected BooleanDefaultTrue saveSmartTagsAsXml;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the frameset property.
     * 
     * @return
     *     possible object is
     *     {@link CTFrameset }
     *     
     */
    public CTFrameset getFrameset() {
        return frameset;
    }

    /**
     * Sets the value of the frameset property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFrameset }
     *     
     */
    public void setFrameset(CTFrameset value) {
        this.frameset = value;
    }

    /**
     * Gets the value of the divs property.
     * 
     * @return
     *     possible object is
     *     {@link CTDivs }
     *     
     */
    public CTDivs getDivs() {
        return divs;
    }

    /**
     * Sets the value of the divs property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDivs }
     *     
     */
    public void setDivs(CTDivs value) {
        this.divs = value;
    }

    /**
     * Gets the value of the encoding property.
     * 
     * @return
     *     possible object is
     *     {@link CTWebSettings.Encoding }
     *     
     */
    public CTWebSettings.Encoding getEncoding() {
        return encoding;
    }

    /**
     * Sets the value of the encoding property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWebSettings.Encoding }
     *     
     */
    public void setEncoding(CTWebSettings.Encoding value) {
        this.encoding = value;
    }

    /**
     * Gets the value of the optimizeForBrowser property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getOptimizeForBrowser() {
        return optimizeForBrowser;
    }

    /**
     * Sets the value of the optimizeForBrowser property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setOptimizeForBrowser(BooleanDefaultTrue value) {
        this.optimizeForBrowser = value;
    }

    /**
     * Gets the value of the relyOnVML property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getRelyOnVML() {
        return relyOnVML;
    }

    /**
     * Sets the value of the relyOnVML property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setRelyOnVML(BooleanDefaultTrue value) {
        this.relyOnVML = value;
    }

    /**
     * Gets the value of the allowPNG property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getAllowPNG() {
        return allowPNG;
    }

    /**
     * Sets the value of the allowPNG property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setAllowPNG(BooleanDefaultTrue value) {
        this.allowPNG = value;
    }

    /**
     * Gets the value of the doNotRelyOnCSS property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotRelyOnCSS() {
        return doNotRelyOnCSS;
    }

    /**
     * Sets the value of the doNotRelyOnCSS property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotRelyOnCSS(BooleanDefaultTrue value) {
        this.doNotRelyOnCSS = value;
    }

    /**
     * Gets the value of the doNotSaveAsSingleFile property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotSaveAsSingleFile() {
        return doNotSaveAsSingleFile;
    }

    /**
     * Sets the value of the doNotSaveAsSingleFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotSaveAsSingleFile(BooleanDefaultTrue value) {
        this.doNotSaveAsSingleFile = value;
    }

    /**
     * Gets the value of the doNotOrganizeInFolder property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotOrganizeInFolder() {
        return doNotOrganizeInFolder;
    }

    /**
     * Sets the value of the doNotOrganizeInFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotOrganizeInFolder(BooleanDefaultTrue value) {
        this.doNotOrganizeInFolder = value;
    }

    /**
     * Gets the value of the doNotUseLongFileNames property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getDoNotUseLongFileNames() {
        return doNotUseLongFileNames;
    }

    /**
     * Sets the value of the doNotUseLongFileNames property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setDoNotUseLongFileNames(BooleanDefaultTrue value) {
        this.doNotUseLongFileNames = value;
    }

    /**
     * Gets the value of the pixelsPerInch property.
     * 
     * @return
     *     possible object is
     *     {@link CTWebSettings.PixelsPerInch }
     *     
     */
    public CTWebSettings.PixelsPerInch getPixelsPerInch() {
        return pixelsPerInch;
    }

    /**
     * Sets the value of the pixelsPerInch property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWebSettings.PixelsPerInch }
     *     
     */
    public void setPixelsPerInch(CTWebSettings.PixelsPerInch value) {
        this.pixelsPerInch = value;
    }

    /**
     * Gets the value of the targetScreenSz property.
     * 
     * @return
     *     possible object is
     *     {@link CTTargetScreenSz }
     *     
     */
    public CTTargetScreenSz getTargetScreenSz() {
        return targetScreenSz;
    }

    /**
     * Sets the value of the targetScreenSz property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTargetScreenSz }
     *     
     */
    public void setTargetScreenSz(CTTargetScreenSz value) {
        this.targetScreenSz = value;
    }

    /**
     * Gets the value of the saveSmartTagsAsXml property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public BooleanDefaultTrue getSaveSmartTagsAsXml() {
        return saveSmartTagsAsXml;
    }

    /**
     * Sets the value of the saveSmartTagsAsXml property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanDefaultTrue }
     *     
     */
    public void setSaveSmartTagsAsXml(BooleanDefaultTrue value) {
        this.saveSmartTagsAsXml = value;
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
    public static class Encoding implements Child
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
     *       &lt;attribute name="val" use="required">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}integer">
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class PixelsPerInch implements Child
    {

        @XmlAttribute(name = "val", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main", required = true)
        protected BigInteger val;
        @XmlTransient
        private Object parent;

        /**
         * Gets the value of the val property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        public BigInteger getVal() {
            return val;
        }

        /**
         * Sets the value of the val property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        public void setVal(BigInteger value) {
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
