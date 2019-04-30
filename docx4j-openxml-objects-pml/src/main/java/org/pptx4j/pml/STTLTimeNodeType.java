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
 * <p>Java class for ST_TLTimeNodeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TLTimeNodeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="clickEffect"/&gt;
 *     &lt;enumeration value="withEffect"/&gt;
 *     &lt;enumeration value="afterEffect"/&gt;
 *     &lt;enumeration value="mainSeq"/&gt;
 *     &lt;enumeration value="interactiveSeq"/&gt;
 *     &lt;enumeration value="clickPar"/&gt;
 *     &lt;enumeration value="withGroup"/&gt;
 *     &lt;enumeration value="afterGroup"/&gt;
 *     &lt;enumeration value="tmRoot"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_TLTimeNodeType")
@XmlEnum
public enum STTLTimeNodeType {


    /**
     * Node Type Enum ( Click Effect )
     * 
     */
    @XmlEnumValue("clickEffect")
    CLICK_EFFECT("clickEffect"),

    /**
     * Node Type Enum ( With Effect )
     * 
     */
    @XmlEnumValue("withEffect")
    WITH_EFFECT("withEffect"),

    /**
     * Node Type Enum ( After Effect )
     * 
     */
    @XmlEnumValue("afterEffect")
    AFTER_EFFECT("afterEffect"),

    /**
     * Node Type Enum ( Main Sequence )
     * 
     */
    @XmlEnumValue("mainSeq")
    MAIN_SEQ("mainSeq"),

    /**
     * Node Type Enum ( Interactive Sequence )
     * 
     */
    @XmlEnumValue("interactiveSeq")
    INTERACTIVE_SEQ("interactiveSeq"),

    /**
     * Node Type Enum ( Click Paragraph )
     * 
     */
    @XmlEnumValue("clickPar")
    CLICK_PAR("clickPar"),

    /**
     * Node Type Enum ( With Group )
     * 
     */
    @XmlEnumValue("withGroup")
    WITH_GROUP("withGroup"),

    /**
     * Node Type Enum ( After Group )
     * 
     */
    @XmlEnumValue("afterGroup")
    AFTER_GROUP("afterGroup"),

    /**
     * Node Type Enum ( Timing Root )
     * 
     */
    @XmlEnumValue("tmRoot")
    TM_ROOT("tmRoot");
    private final String value;

    STTLTimeNodeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTLTimeNodeType fromValue(String v) {
        for (STTLTimeNodeType c: STTLTimeNodeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
