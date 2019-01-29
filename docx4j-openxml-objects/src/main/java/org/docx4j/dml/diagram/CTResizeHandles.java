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


package org.docx4j.dml.diagram;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_ResizeHandles complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ResizeHandles">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="val" type="{http://schemas.openxmlformats.org/drawingml/2006/diagram}ST_ResizeHandlesStr" default="rel" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ResizeHandles")
public class CTResizeHandles {

    @XmlAttribute
    protected STResizeHandlesStr val;

    /**
     * Gets the value of the val property.
     * 
     * @return
     *     possible object is
     *     {@link STResizeHandlesStr }
     *     
     */
    public STResizeHandlesStr getVal() {
        if (val == null) {
            return STResizeHandlesStr.REL;
        } else {
            return val;
        }
    }

    /**
     * Sets the value of the val property.
     * 
     * @param value
     *     allowed object is
     *     {@link STResizeHandlesStr }
     *     
     */
    public void setVal(STResizeHandlesStr value) {
        this.val = value;
    }

}
