/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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


package org.docx4j.wml; 

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UnderlineEnumeration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="UnderlineEnumeration">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="single"/>
 *     &lt;enumeration value="words"/>
 *     &lt;enumeration value="double"/>
 *     &lt;enumeration value="thick"/>
 *     &lt;enumeration value="dotted"/>
 *     &lt;enumeration value="dottedHeavy"/>
 *     &lt;enumeration value="dash"/>
 *     &lt;enumeration value="dashedHeavy"/>
 *     &lt;enumeration value="dashLong"/>
 *     &lt;enumeration value="dashLongHeavy"/>
 *     &lt;enumeration value="dotDash"/>
 *     &lt;enumeration value="dashDotHeavy"/>
 *     &lt;enumeration value="dotDotDash"/>
 *     &lt;enumeration value="dashDotDotHeavy"/>
 *     &lt;enumeration value="wave"/>
 *     &lt;enumeration value="wavyHeavy"/>
 *     &lt;enumeration value="wavyDouble"/>
 *     &lt;enumeration value="none"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "UnderlineEnumeration")
@XmlEnum
public enum UnderlineEnumeration {


    /**
     * Single Underline
     * 
     */
    @XmlEnumValue("single")
    SINGLE("single"),

    /**
     * Underline Non-Space Characters
     * 						Only
     * 
     */
    @XmlEnumValue("words")
    WORDS("words"),

    /**
     * Double Underline
     * 
     */
    @XmlEnumValue("double")
    DOUBLE("double"),

    /**
     * Thick Underline
     * 
     */
    @XmlEnumValue("thick")
    THICK("thick"),

    /**
     * Dotted Underline
     * 
     */
    @XmlEnumValue("dotted")
    DOTTED("dotted"),

    /**
     * Thick Dotted Underline
     * 
     */
    @XmlEnumValue("dottedHeavy")
    DOTTED_HEAVY("dottedHeavy"),

    /**
     * Dashed Underline
     * 
     */
    @XmlEnumValue("dash")
    DASH("dash"),

    /**
     * Thick Dashed Underline
     * 
     */
    @XmlEnumValue("dashedHeavy")
    DASHED_HEAVY("dashedHeavy"),

    /**
     * Long Dashed Underline
     * 
     */
    @XmlEnumValue("dashLong")
    DASH_LONG("dashLong"),

    /**
     * Thick Long Dashed
     * 						Underline
     * 
     */
    @XmlEnumValue("dashLongHeavy")
    DASH_LONG_HEAVY("dashLongHeavy"),

    /**
     * Dash-Dot Underline
     * 
     */
    @XmlEnumValue("dotDash")
    DOT_DASH("dotDash"),

    /**
     * Thick Dash-Dot
     * 						Underline
     * 
     */
    @XmlEnumValue("dashDotHeavy")
    DASH_DOT_HEAVY("dashDotHeavy"),

    /**
     * Dash-Dot-Dot Underline
     * 
     */
    @XmlEnumValue("dotDotDash")
    DOT_DOT_DASH("dotDotDash"),

    /**
     * Thick Dash-Dot-Dot
     * 						Underline
     * 
     */
    @XmlEnumValue("dashDotDotHeavy")
    DASH_DOT_DOT_HEAVY("dashDotDotHeavy"),

    /**
     * Wave Underline
     * 
     */
    @XmlEnumValue("wave")
    WAVE("wave"),

    /**
     * Heavy Wave Underline
     * 
     */
    @XmlEnumValue("wavyHeavy")
    WAVY_HEAVY("wavyHeavy"),

    /**
     * Double Wave Underline
     * 
     */
    @XmlEnumValue("wavyDouble")
    WAVY_DOUBLE("wavyDouble"),

    /**
     * No Underline
     * 
     */
    @XmlEnumValue("none")
    NONE("none");
    private final String value;

    UnderlineEnumeration(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static UnderlineEnumeration fromValue(String v) {
        for (UnderlineEnumeration c: UnderlineEnumeration.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
