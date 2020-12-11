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
 * <p>Java class for ST_RectAlignment.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_RectAlignment"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="tl"/&gt;
 *     &lt;enumeration value="t"/&gt;
 *     &lt;enumeration value="tr"/&gt;
 *     &lt;enumeration value="l"/&gt;
 *     &lt;enumeration value="ctr"/&gt;
 *     &lt;enumeration value="r"/&gt;
 *     &lt;enumeration value="bl"/&gt;
 *     &lt;enumeration value="b"/&gt;
 *     &lt;enumeration value="br"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_RectAlignment")
@XmlEnum
public enum STRectAlignment {


    /**
     * Rectangle Alignment Enum ( Top Left )
     * 
     */
    @XmlEnumValue("tl")
    TL("tl"),

    /**
     * Rectangle Alignment Enum ( Top )
     * 
     */
    @XmlEnumValue("t")
    T("t"),

    /**
     * Rectangle Alignment Enum ( Top Right )
     * 
     */
    @XmlEnumValue("tr")
    TR("tr"),

    /**
     * Rectangle Alignment Enum ( Left )
     * 
     */
    @XmlEnumValue("l")
    L("l"),

    /**
     * Rectangle Alignment Enum ( Center )
     * 
     */
    @XmlEnumValue("ctr")
    CTR("ctr"),

    /**
     * Rectangle Alignment Enum ( Right )
     * 
     */
    @XmlEnumValue("r")
    R("r"),

    /**
     * Rectangle Alignment Enum ( Bottom Left )
     * 
     */
    @XmlEnumValue("bl")
    BL("bl"),

    /**
     * Rectangle Alignment Enum ( Bottom )
     * 
     */
    @XmlEnumValue("b")
    B("b"),

    /**
     * Rectangle Alignment Enum ( Bottom Right )
     * 
     */
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
