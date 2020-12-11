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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_AnimationDgmOnlyBuildType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_AnimationDgmOnlyBuildType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="one"/&gt;
 *     &lt;enumeration value="lvlOne"/&gt;
 *     &lt;enumeration value="lvlAtOnce"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_AnimationDgmOnlyBuildType")
@XmlEnum
public enum STAnimationDgmOnlyBuildType {


    /**
     * Elements One-by-One
     * 
     */
    @XmlEnumValue("one")
    ONE("one"),

    /**
     * Level One-by-One
     * 
     */
    @XmlEnumValue("lvlOne")
    LVL_ONE("lvlOne"),

    /**
     * Each Level at Once
     * 
     */
    @XmlEnumValue("lvlAtOnce")
    LVL_AT_ONCE("lvlAtOnce");
    private final String value;

    STAnimationDgmOnlyBuildType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STAnimationDgmOnlyBuildType fromValue(String v) {
        for (STAnimationDgmOnlyBuildType c: STAnimationDgmOnlyBuildType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
