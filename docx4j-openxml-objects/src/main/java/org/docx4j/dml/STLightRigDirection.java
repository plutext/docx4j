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
 * <p>Java class for ST_LightRigDirection.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_LightRigDirection"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="tl"/&gt;
 *     &lt;enumeration value="t"/&gt;
 *     &lt;enumeration value="tr"/&gt;
 *     &lt;enumeration value="l"/&gt;
 *     &lt;enumeration value="r"/&gt;
 *     &lt;enumeration value="bl"/&gt;
 *     &lt;enumeration value="b"/&gt;
 *     &lt;enumeration value="br"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_LightRigDirection")
@XmlEnum
public enum STLightRigDirection {


    /**
     * Top Left
     * 
     */
    @XmlEnumValue("tl")
    TL("tl"),

    /**
     * Top
     * 
     */
    @XmlEnumValue("t")
    T("t"),

    /**
     * Top Right
     * 
     */
    @XmlEnumValue("tr")
    TR("tr"),

    /**
     * Left
     * 
     */
    @XmlEnumValue("l")
    L("l"),

    /**
     * Right
     * 
     */
    @XmlEnumValue("r")
    R("r"),

    /**
     * Bottom Left
     * 
     */
    @XmlEnumValue("bl")
    BL("bl"),

    /**
     * Bottom
     * 
     */
    @XmlEnumValue("b")
    B("b"),

    /**
     * Bottom Right
     * 
     */
    @XmlEnumValue("br")
    BR("br");
    private final String value;

    STLightRigDirection(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STLightRigDirection fromValue(String v) {
        for (STLightRigDirection c: STLightRigDirection.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
