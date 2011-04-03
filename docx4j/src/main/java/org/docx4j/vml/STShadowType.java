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


package org.docx4j.vml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ShadowType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ShadowType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="single"/>
 *     &lt;enumeration value="double"/>
 *     &lt;enumeration value="emboss"/>
 *     &lt;enumeration value="perspective"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ShadowType")
@XmlEnum
public enum STShadowType {


    /**
     * Single Shadow
     * 
     */
    @XmlEnumValue("single")
    SINGLE("single"),

    /**
     * Double Shadow
     * 
     */
    @XmlEnumValue("double")
    DOUBLE("double"),

    /**
     * Embossed Shadow
     * 
     */
    @XmlEnumValue("emboss")
    EMBOSS("emboss"),

    /**
     * Perspective Shadow
     * 
     */
    @XmlEnumValue("perspective")
    PERSPECTIVE("perspective");
    private final String value;

    STShadowType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STShadowType fromValue(String v) {
        for (STShadowType c: STShadowType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
