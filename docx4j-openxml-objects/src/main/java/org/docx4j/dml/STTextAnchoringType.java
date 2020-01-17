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
 * <p>Java class for ST_TextAnchoringType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TextAnchoringType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="t"/&gt;
 *     &lt;enumeration value="ctr"/&gt;
 *     &lt;enumeration value="b"/&gt;
 *     &lt;enumeration value="just"/&gt;
 *     &lt;enumeration value="dist"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_TextAnchoringType")
@XmlEnum
public enum STTextAnchoringType {


    /**
     * Text Anchoring Type Enum ( Top )
     * 
     */
    @XmlEnumValue("t")
    T("t"),

    /**
     * Text Anchor Enum ( Center )
     * 
     */
    @XmlEnumValue("ctr")
    CTR("ctr"),

    /**
     * Text Anchor Enum ( Bottom )
     * 
     */
    @XmlEnumValue("b")
    B("b"),

    /**
     * Text Anchor Enum ( Justified )
     * 
     */
    @XmlEnumValue("just")
    JUST("just"),

    /**
     * Text Anchor Enum ( Distributed )
     * 
     */
    @XmlEnumValue("dist")
    DIST("dist");
    private final String value;

    STTextAnchoringType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTextAnchoringType fromValue(String v) {
        for (STTextAnchoringType c: STTextAnchoringType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
