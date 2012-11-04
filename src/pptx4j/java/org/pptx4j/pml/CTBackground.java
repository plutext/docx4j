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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTStyleMatrixReference;
import org.docx4j.dml.STBlackWhiteMode;


/**
 * <p>Java class for CT_Background complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Background">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.openxmlformats.org/presentationml/2006/main}EG_Background"/>
 *       &lt;/sequence>
 *       &lt;attribute name="bwMode" type="{http://schemas.openxmlformats.org/drawingml/2006/main}ST_BlackWhiteMode" default="white" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Background", propOrder = {
    "bgPr",
    "bgRef"
})
public class CTBackground {

    protected CTBackgroundProperties bgPr;
    protected CTStyleMatrixReference bgRef;
    @XmlAttribute(name = "bwMode")
    protected STBlackWhiteMode bwMode;

    /**
     * Gets the value of the bgPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTBackgroundProperties }
     *     
     */
    public CTBackgroundProperties getBgPr() {
        return bgPr;
    }

    /**
     * Sets the value of the bgPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTBackgroundProperties }
     *     
     */
    public void setBgPr(CTBackgroundProperties value) {
        this.bgPr = value;
    }

    /**
     * Gets the value of the bgRef property.
     * 
     * @return
     *     possible object is
     *     {@link CTStyleMatrixReference }
     *     
     */
    public CTStyleMatrixReference getBgRef() {
        return bgRef;
    }

    /**
     * Sets the value of the bgRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStyleMatrixReference }
     *     
     */
    public void setBgRef(CTStyleMatrixReference value) {
        this.bgRef = value;
    }

    /**
     * Gets the value of the bwMode property.
     * 
     * @return
     *     possible object is
     *     {@link STBlackWhiteMode }
     *     
     */
    public STBlackWhiteMode getBwMode() {
        if (bwMode == null) {
            return STBlackWhiteMode.WHITE;
        } else {
            return bwMode;
        }
    }

    /**
     * Sets the value of the bwMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link STBlackWhiteMode }
     *     
     */
    public void setBwMode(STBlackWhiteMode value) {
        this.bwMode = value;
    }

}
