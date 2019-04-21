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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTTextListStyle;


/**
 * <p>Java class for CT_SlideMasterTextStyles complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_SlideMasterTextStyles">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="titleStyle" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextListStyle" minOccurs="0"/>
 *         &lt;element name="bodyStyle" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextListStyle" minOccurs="0"/>
 *         &lt;element name="otherStyle" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextListStyle" minOccurs="0"/>
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
@XmlType(name = "CT_SlideMasterTextStyles", propOrder = {
    "titleStyle",
    "bodyStyle",
    "otherStyle",
    "extLst"
})
public class CTSlideMasterTextStyles {

    protected CTTextListStyle titleStyle;
    protected CTTextListStyle bodyStyle;
    protected CTTextListStyle otherStyle;
    protected CTExtensionList extLst;

    /**
     * Gets the value of the titleStyle property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextListStyle }
     *     
     */
    public CTTextListStyle getTitleStyle() {
        return titleStyle;
    }

    /**
     * Sets the value of the titleStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextListStyle }
     *     
     */
    public void setTitleStyle(CTTextListStyle value) {
        this.titleStyle = value;
    }

    /**
     * Gets the value of the bodyStyle property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextListStyle }
     *     
     */
    public CTTextListStyle getBodyStyle() {
        return bodyStyle;
    }

    /**
     * Sets the value of the bodyStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextListStyle }
     *     
     */
    public void setBodyStyle(CTTextListStyle value) {
        this.bodyStyle = value;
    }

    /**
     * Gets the value of the otherStyle property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextListStyle }
     *     
     */
    public CTTextListStyle getOtherStyle() {
        return otherStyle;
    }

    /**
     * Sets the value of the otherStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextListStyle }
     *     
     */
    public void setOtherStyle(CTTextListStyle value) {
        this.otherStyle = value;
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
