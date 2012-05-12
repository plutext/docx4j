/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.pptx4j.pml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTColorMRU;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="htmlPubPr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_HtmlPublishProperties" minOccurs="0"/>
 *         &lt;element name="webPr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_WebProperties" minOccurs="0"/>
 *         &lt;element name="prnPr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_PrintProperties" minOccurs="0"/>
 *         &lt;element name="showPr" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ShowProperties" minOccurs="0"/>
 *         &lt;element name="clrMru" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_ColorMRU" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/presentationml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "htmlPubPr",
    "webPr",
    "prnPr",
    "showPr",
    "clrMru",
    "extLst"
})
@XmlRootElement(name = "presentationPr")
public class PresentationPr {

    protected CTHtmlPublishProperties htmlPubPr;
    protected CTWebProperties webPr;
    protected CTPrintProperties prnPr;
    protected CTShowProperties showPr;
    protected CTColorMRU clrMru;
    protected CTExtensionList extLst;

    /**
     * Gets the value of the htmlPubPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTHtmlPublishProperties }
     *     
     */
    public CTHtmlPublishProperties getHtmlPubPr() {
        return htmlPubPr;
    }

    /**
     * Sets the value of the htmlPubPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTHtmlPublishProperties }
     *     
     */
    public void setHtmlPubPr(CTHtmlPublishProperties value) {
        this.htmlPubPr = value;
    }

    /**
     * Gets the value of the webPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTWebProperties }
     *     
     */
    public CTWebProperties getWebPr() {
        return webPr;
    }

    /**
     * Sets the value of the webPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWebProperties }
     *     
     */
    public void setWebPr(CTWebProperties value) {
        this.webPr = value;
    }

    /**
     * Gets the value of the prnPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTPrintProperties }
     *     
     */
    public CTPrintProperties getPrnPr() {
        return prnPr;
    }

    /**
     * Sets the value of the prnPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPrintProperties }
     *     
     */
    public void setPrnPr(CTPrintProperties value) {
        this.prnPr = value;
    }

    /**
     * Gets the value of the showPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTShowProperties }
     *     
     */
    public CTShowProperties getShowPr() {
        return showPr;
    }

    /**
     * Sets the value of the showPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTShowProperties }
     *     
     */
    public void setShowPr(CTShowProperties value) {
        this.showPr = value;
    }

    /**
     * Gets the value of the clrMru property.
     * 
     * @return
     *     possible object is
     *     {@link CTColorMRU }
     *     
     */
    public CTColorMRU getClrMru() {
        return clrMru;
    }

    /**
     * Sets the value of the clrMru property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTColorMRU }
     *     
     */
    public void setClrMru(CTColorMRU value) {
        this.clrMru = value;
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

}
