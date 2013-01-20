/*
 *  Copyright 2010-2013, Plutext Pty Ltd.
 *   
 *  This file is part of xlsx4j, a component of docx4j.

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
package org.xlsx4j.sml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_HeaderFooter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_HeaderFooter">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="oddHeader" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" minOccurs="0"/>
 *         &lt;element name="oddFooter" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" minOccurs="0"/>
 *         &lt;element name="evenHeader" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" minOccurs="0"/>
 *         &lt;element name="evenFooter" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" minOccurs="0"/>
 *         &lt;element name="firstHeader" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" minOccurs="0"/>
 *         &lt;element name="firstFooter" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="differentOddEven" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="differentFirst" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="scaleWithDoc" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="alignWithMargins" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_HeaderFooter", propOrder = {
    "oddHeader",
    "oddFooter",
    "evenHeader",
    "evenFooter",
    "firstHeader",
    "firstFooter"
})
public class CTHeaderFooter implements Child
{

    protected String oddHeader;
    protected String oddFooter;
    protected String evenHeader;
    protected String evenFooter;
    protected String firstHeader;
    protected String firstFooter;
    @XmlAttribute(name = "differentOddEven")
    protected Boolean differentOddEven;
    @XmlAttribute(name = "differentFirst")
    protected Boolean differentFirst;
    @XmlAttribute(name = "scaleWithDoc")
    protected Boolean scaleWithDoc;
    @XmlAttribute(name = "alignWithMargins")
    protected Boolean alignWithMargins;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the oddHeader property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOddHeader() {
        return oddHeader;
    }

    /**
     * Sets the value of the oddHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOddHeader(String value) {
        this.oddHeader = value;
    }

    /**
     * Gets the value of the oddFooter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOddFooter() {
        return oddFooter;
    }

    /**
     * Sets the value of the oddFooter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOddFooter(String value) {
        this.oddFooter = value;
    }

    /**
     * Gets the value of the evenHeader property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEvenHeader() {
        return evenHeader;
    }

    /**
     * Sets the value of the evenHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEvenHeader(String value) {
        this.evenHeader = value;
    }

    /**
     * Gets the value of the evenFooter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEvenFooter() {
        return evenFooter;
    }

    /**
     * Sets the value of the evenFooter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEvenFooter(String value) {
        this.evenFooter = value;
    }

    /**
     * Gets the value of the firstHeader property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFirstHeader() {
        return firstHeader;
    }

    /**
     * Sets the value of the firstHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFirstHeader(String value) {
        this.firstHeader = value;
    }

    /**
     * Gets the value of the firstFooter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFirstFooter() {
        return firstFooter;
    }

    /**
     * Sets the value of the firstFooter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFirstFooter(String value) {
        this.firstFooter = value;
    }

    /**
     * Gets the value of the differentOddEven property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDifferentOddEven() {
        if (differentOddEven == null) {
            return false;
        } else {
            return differentOddEven;
        }
    }

    /**
     * Sets the value of the differentOddEven property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDifferentOddEven(Boolean value) {
        this.differentOddEven = value;
    }

    /**
     * Gets the value of the differentFirst property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDifferentFirst() {
        if (differentFirst == null) {
            return false;
        } else {
            return differentFirst;
        }
    }

    /**
     * Sets the value of the differentFirst property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDifferentFirst(Boolean value) {
        this.differentFirst = value;
    }

    /**
     * Gets the value of the scaleWithDoc property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isScaleWithDoc() {
        if (scaleWithDoc == null) {
            return true;
        } else {
            return scaleWithDoc;
        }
    }

    /**
     * Sets the value of the scaleWithDoc property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setScaleWithDoc(Boolean value) {
        this.scaleWithDoc = value;
    }

    /**
     * Gets the value of the alignWithMargins property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAlignWithMargins() {
        if (alignWithMargins == null) {
            return true;
        } else {
            return alignWithMargins;
        }
    }

    /**
     * Sets the value of the alignWithMargins property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAlignWithMargins(Boolean value) {
        this.alignWithMargins = value;
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
