/*
 *  Copyright 2013, Plutext Pty Ltd.
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


package org.docx4j.w14;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_RectAlignment.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_RectAlignment">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="tl"/>
 *     &lt;enumeration value="t"/>
 *     &lt;enumeration value="tr"/>
 *     &lt;enumeration value="l"/>
 *     &lt;enumeration value="ctr"/>
 *     &lt;enumeration value="r"/>
 *     &lt;enumeration value="bl"/>
 *     &lt;enumeration value="b"/>
 *     &lt;enumeration value="br"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_RectAlignment")
@XmlEnum
public enum STRectAlignment {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("tl")
    TL("tl"),
    @XmlEnumValue("t")
    T("t"),
    @XmlEnumValue("tr")
    TR("tr"),
    @XmlEnumValue("l")
    L("l"),
    @XmlEnumValue("ctr")
    CTR("ctr"),
    @XmlEnumValue("r")
    R("r"),
    @XmlEnumValue("bl")
    BL("bl"),
    @XmlEnumValue("b")
    B("b"),
    @XmlEnumValue("br")
    BR("br");
    private final String value;

    STRectAlignment(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STRectAlignment fromValue(String v) {
        for (STRectAlignment c: STRectAlignment.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
