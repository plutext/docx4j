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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TLTimeNodeFillType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TLTimeNodeFillType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="remove"/&gt;
 *     &lt;enumeration value="freeze"/&gt;
 *     &lt;enumeration value="hold"/&gt;
 *     &lt;enumeration value="transition"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_TLTimeNodeFillType")
@XmlEnum
public enum STTLTimeNodeFillType {


    /**
     * Remove
     * 
     */
    @XmlEnumValue("remove")
    REMOVE("remove"),

    /**
     * Freeze
     * 
     */
    @XmlEnumValue("freeze")
    FREEZE("freeze"),

    /**
     * TimeNode Fill Type Enum ( Hold )
     * 
     */
    @XmlEnumValue("hold")
    HOLD("hold"),

    /**
     * Transition
     * 
     */
    @XmlEnumValue("transition")
    TRANSITION("transition");
    private final String value;

    STTLTimeNodeFillType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTLTimeNodeFillType fromValue(String v) {
        for (STTLTimeNodeFillType c: STTLTimeNodeFillType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
