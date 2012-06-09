/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TextUnderlineType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TextUnderlineType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="words"/>
 *     &lt;enumeration value="sng"/>
 *     &lt;enumeration value="dbl"/>
 *     &lt;enumeration value="heavy"/>
 *     &lt;enumeration value="dotted"/>
 *     &lt;enumeration value="dottedHeavy"/>
 *     &lt;enumeration value="dash"/>
 *     &lt;enumeration value="dashHeavy"/>
 *     &lt;enumeration value="dashLong"/>
 *     &lt;enumeration value="dashLongHeavy"/>
 *     &lt;enumeration value="dotDash"/>
 *     &lt;enumeration value="dotDashHeavy"/>
 *     &lt;enumeration value="dotDotDash"/>
 *     &lt;enumeration value="dotDotDashHeavy"/>
 *     &lt;enumeration value="wavy"/>
 *     &lt;enumeration value="wavyHeavy"/>
 *     &lt;enumeration value="wavyDbl"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TextUnderlineType")
@XmlEnum
public enum STTextUnderlineType {


    /**
     * 
     * 						Text Underline Enum ( None )
     * 					
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * 
     * 						Text Underline Enum ( Words )
     * 					
     * 
     */
    @XmlEnumValue("words")
    WORDS("words"),

    /**
     * 
     * 						Text Underline Enum ( Single )
     * 					
     * 
     */
    @XmlEnumValue("sng")
    SNG("sng"),

    /**
     * 
     * 						Text Underline Enum ( Double )
     * 					
     * 
     */
    @XmlEnumValue("dbl")
    DBL("dbl"),

    /**
     * 
     * 						Text Underline Enum ( Heavy )
     * 					
     * 
     */
    @XmlEnumValue("heavy")
    HEAVY("heavy"),

    /**
     * 
     * 						Text Underline Enum ( Dotted )
     * 					
     * 
     */
    @XmlEnumValue("dotted")
    DOTTED("dotted"),

    /**
     * 
     * 						Text Underline Enum ( Heavy Dotted )
     * 					
     * 
     */
    @XmlEnumValue("dottedHeavy")
    DOTTED_HEAVY("dottedHeavy"),

    /**
     * 
     * 						Text Underline Enum ( Dashed )
     * 					
     * 
     */
    @XmlEnumValue("dash")
    DASH("dash"),

    /**
     * 
     * 						Text Underline Enum ( Heavy Dashed )
     * 					
     * 
     */
    @XmlEnumValue("dashHeavy")
    DASH_HEAVY("dashHeavy"),

    /**
     * 
     * 						Text Underline Enum ( Long Dashed )
     * 					
     * 
     */
    @XmlEnumValue("dashLong")
    DASH_LONG("dashLong"),

    /**
     * 
     * 						Text Underline Enum ( Heavy Long Dashed )
     * 					
     * 
     */
    @XmlEnumValue("dashLongHeavy")
    DASH_LONG_HEAVY("dashLongHeavy"),

    /**
     * 
     * 						Text Underline Enum ( Dot Dash )
     * 					
     * 
     */
    @XmlEnumValue("dotDash")
    DOT_DASH("dotDash"),

    /**
     * 
     * 						Text Underline Enum ( Heavy Dot Dash )
     * 					
     * 
     */
    @XmlEnumValue("dotDashHeavy")
    DOT_DASH_HEAVY("dotDashHeavy"),

    /**
     * 
     * 						Text Underline Enum ( Dot Dot Dash )
     * 					
     * 
     */
    @XmlEnumValue("dotDotDash")
    DOT_DOT_DASH("dotDotDash"),

    /**
     * 
     * 						Text Underline Enum ( Heavy Dot Dot Dash )
     * 					
     * 
     */
    @XmlEnumValue("dotDotDashHeavy")
    DOT_DOT_DASH_HEAVY("dotDotDashHeavy"),

    /**
     * 
     * 						Text Underline Enum ( Wavy )
     * 					
     * 
     */
    @XmlEnumValue("wavy")
    WAVY("wavy"),

    /**
     * 
     * 						Text Underline Enum ( Heavy Wavy )
     * 					
     * 
     */
    @XmlEnumValue("wavyHeavy")
    WAVY_HEAVY("wavyHeavy"),

    /**
     * 
     * 						Text Underline Enum ( Double Wavy )
     * 					
     * 
     */
    @XmlEnumValue("wavyDbl")
    WAVY_DBL("wavyDbl");
    private final String value;

    STTextUnderlineType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTextUnderlineType fromValue(String v) {
        for (STTextUnderlineType c: STTextUnderlineType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
