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

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TransitionSpeed.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TransitionSpeed"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="slow"/&gt;
 *     &lt;enumeration value="med"/&gt;
 *     &lt;enumeration value="fast"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_TransitionSpeed")
@XmlEnum
public enum STTransitionSpeed {


    /**
     * low
     * 
     */
    @XmlEnumValue("slow")
    SLOW("slow"),

    /**
     * Medium
     * 
     */
    @XmlEnumValue("med")
    MED("med"),

    /**
     * Fast
     * 
     */
    @XmlEnumValue("fast")
    FAST("fast");
    private final String value;

    STTransitionSpeed(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTransitionSpeed fromValue(String v) {
        for (STTransitionSpeed c: STTransitionSpeed.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
