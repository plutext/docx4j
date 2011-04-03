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


package org.docx4j.vml;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.vml.officedrawing.STInsetMode;
import org.docx4j.vml.officedrawing.STTrueFalse;
import org.docx4j.wml.CTTxbxContent;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.w3c.dom.Element;


/**
 * <p>Java class for CT_Textbox complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Textbox">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}txbxContent" minOccurs="0"/>
 *         &lt;any processContents='skip' namespace=''/>
 *       &lt;/choice>
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Style"/>
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Id"/>
 *       &lt;attribute name="inset" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}singleclick"/>
 *       &lt;attribute ref="{urn:schemas-microsoft-com:office:office}insetmode"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Textbox", propOrder = {
    "txbxContent",
    "any"
})
public class CTTextbox implements Child
{

    @XmlElement(namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected CTTxbxContent txbxContent;
    @XmlAnyElement
    protected Element any;
    @XmlAttribute(name = "inset")
    protected String inset;
    @XmlAttribute(name = "singleclick", namespace = "urn:schemas-microsoft-com:office:office")
    protected STTrueFalse singleclick;
    @XmlAttribute(name = "insetmode", namespace = "urn:schemas-microsoft-com:office:office")
    protected STInsetMode insetmode;
    @XmlAttribute(name = "style")
    protected String style;
    @XmlAttribute(name = "id")
    protected String vmlId;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the txbxContent property.
     * 
     * @return
     *     possible object is
     *     {@link CTTxbxContent }
     *     
     */
    public CTTxbxContent getTxbxContent() {
        return txbxContent;
    }

    /**
     * Sets the value of the txbxContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTxbxContent }
     *     
     */
    public void setTxbxContent(CTTxbxContent value) {
        this.txbxContent = value;
    }

    /**
     * Gets the value of the any property.
     * 
     * @return
     *     possible object is
     *     {@link Element }
     *     
     */
    public Element getAny() {
        return any;
    }

    /**
     * Sets the value of the any property.
     * 
     * @param value
     *     allowed object is
     *     {@link Element }
     *     
     */
    public void setAny(Element value) {
        this.any = value;
    }

    /**
     * Gets the value of the inset property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInset() {
        return inset;
    }

    /**
     * Sets the value of the inset property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInset(String value) {
        this.inset = value;
    }

    /**
     * Text Box Single-Click Selection Toggle
     * 
     * @return
     *     possible object is
     *     {@link STTrueFalse }
     *     
     */
    public STTrueFalse getSingleclick() {
        return singleclick;
    }

    /**
     * Sets the value of the singleclick property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTrueFalse }
     *     
     */
    public void setSingleclick(STTrueFalse value) {
        this.singleclick = value;
    }

    /**
     * Text Inset Mode
     * 
     * @return
     *     possible object is
     *     {@link STInsetMode }
     *     
     */
    public STInsetMode getInsetmode() {
        if (insetmode == null) {
            return STInsetMode.CUSTOM;
        } else {
            return insetmode;
        }
    }

    /**
     * Sets the value of the insetmode property.
     * 
     * @param value
     *     allowed object is
     *     {@link STInsetMode }
     *     
     */
    public void setInsetmode(STInsetMode value) {
        this.insetmode = value;
    }

    /**
     * Gets the value of the style property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStyle() {
        return style;
    }

    /**
     * Sets the value of the style property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStyle(String value) {
        this.style = value;
    }

    /**
     * Gets the value of the vmlId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVmlId() {
        return vmlId;
    }

    /**
     * Sets the value of the vmlId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVmlId(String value) {
        this.vmlId = value;
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
