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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_ShowInfoKiosk complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ShowInfoKiosk">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="restart" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="300000" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ShowInfoKiosk")
public class CTShowInfoKiosk {

    @XmlAttribute(name = "restart")
    @XmlSchemaType(name = "unsignedInt")
    protected Long restart;

    /**
     * Gets the value of the restart property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getRestart() {
        if (restart == null) {
            return  300000L;
        } else {
            return restart;
        }
    }

    /**
     * Sets the value of the restart property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRestart(Long value) {
        this.restart = value;
    }

}
