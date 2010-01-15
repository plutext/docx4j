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


package org.docx4j.dml.chart;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.dml.CTTextBody;


/**
 * <p>Java class for CT_Tx complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Tx">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="strRef" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}CT_StrRef"/>
 *           &lt;element name="rich" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextBody"/>
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
@XmlType(name = "CT_Tx", propOrder = {
    "strRef",
    "rich"
})
public class CTTx {

    protected CTStrRef strRef;
    protected CTTextBody rich;

    /**
     * Gets the value of the strRef property.
     * 
     * @return
     *     possible object is
     *     {@link CTStrRef }
     *     
     */
    public CTStrRef getStrRef() {
        return strRef;
    }

    /**
     * Sets the value of the strRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTStrRef }
     *     
     */
    public void setStrRef(CTStrRef value) {
        this.strRef = value;
    }

    /**
     * Gets the value of the rich property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextBody }
     *     
     */
    public CTTextBody getRich() {
        return rich;
    }

    /**
     * Sets the value of the rich property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextBody }
     *     
     */
    public void setRich(CTTextBody value) {
        this.rich = value;
    }

}
