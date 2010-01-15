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


package org.docx4j.dml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_TextSpacing complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TextSpacing">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="spcPct" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextSpacingPercent"/>
 *         &lt;element name="spcPts" type="{http://schemas.openxmlformats.org/drawingml/2006/main}CT_TextSpacingPoint"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TextSpacing", propOrder = {
    "spcPct",
    "spcPts"
})
public class CTTextSpacing {

    protected CTTextSpacingPercent spcPct;
    protected CTTextSpacingPoint spcPts;

    /**
     * Gets the value of the spcPct property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextSpacingPercent }
     *     
     */
    public CTTextSpacingPercent getSpcPct() {
        return spcPct;
    }

    /**
     * Sets the value of the spcPct property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextSpacingPercent }
     *     
     */
    public void setSpcPct(CTTextSpacingPercent value) {
        this.spcPct = value;
    }

    /**
     * Gets the value of the spcPts property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextSpacingPoint }
     *     
     */
    public CTTextSpacingPoint getSpcPts() {
        return spcPts;
    }

    /**
     * Sets the value of the spcPts property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextSpacingPoint }
     *     
     */
    public void setSpcPts(CTTextSpacingPoint value) {
        this.spcPts = value;
    }

}
