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
 * <p>Java class for ST_PresetShadowVal.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PresetShadowVal"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="shdw1"/&gt;
 *     &lt;enumeration value="shdw2"/&gt;
 *     &lt;enumeration value="shdw3"/&gt;
 *     &lt;enumeration value="shdw4"/&gt;
 *     &lt;enumeration value="shdw5"/&gt;
 *     &lt;enumeration value="shdw6"/&gt;
 *     &lt;enumeration value="shdw7"/&gt;
 *     &lt;enumeration value="shdw8"/&gt;
 *     &lt;enumeration value="shdw9"/&gt;
 *     &lt;enumeration value="shdw10"/&gt;
 *     &lt;enumeration value="shdw11"/&gt;
 *     &lt;enumeration value="shdw12"/&gt;
 *     &lt;enumeration value="shdw13"/&gt;
 *     &lt;enumeration value="shdw14"/&gt;
 *     &lt;enumeration value="shdw15"/&gt;
 *     &lt;enumeration value="shdw16"/&gt;
 *     &lt;enumeration value="shdw17"/&gt;
 *     &lt;enumeration value="shdw18"/&gt;
 *     &lt;enumeration value="shdw19"/&gt;
 *     &lt;enumeration value="shdw20"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_PresetShadowVal")
@XmlEnum
public enum STPresetShadowVal {


    /**
     * Top Left Drop Shadow
     * 
     */
    @XmlEnumValue("shdw1")
    SHDW_1("shdw1"),

    /**
     * Top Right Drop Shadow
     * 
     */
    @XmlEnumValue("shdw2")
    SHDW_2("shdw2"),

    /**
     * Back Left Perspective Shadow
     * 
     */
    @XmlEnumValue("shdw3")
    SHDW_3("shdw3"),

    /**
     * Back Right Perspective Shadow
     * 
     */
    @XmlEnumValue("shdw4")
    SHDW_4("shdw4"),

    /**
     * Bottom Left Drop Shadow
     * 
     */
    @XmlEnumValue("shdw5")
    SHDW_5("shdw5"),

    /**
     * Bottom Right Drop Shadow
     * 
     */
    @XmlEnumValue("shdw6")
    SHDW_6("shdw6"),

    /**
     * Front Left Perspective Shadow
     * 
     */
    @XmlEnumValue("shdw7")
    SHDW_7("shdw7"),

    /**
     * Front Right Perspective Shadow
     * 
     */
    @XmlEnumValue("shdw8")
    SHDW_8("shdw8"),

    /**
     * Top Left Small Drop Shadow
     * 
     */
    @XmlEnumValue("shdw9")
    SHDW_9("shdw9"),

    /**
     * Top Left Large Drop Shadow
     * 
     */
    @XmlEnumValue("shdw10")
    SHDW_10("shdw10"),

    /**
     * Back Left Long Perspective Shadow
     * 
     */
    @XmlEnumValue("shdw11")
    SHDW_11("shdw11"),

    /**
     * Back Right Long Perspective Shadow
     * 
     */
    @XmlEnumValue("shdw12")
    SHDW_12("shdw12"),

    /**
     * Top Left Double Drop Shadow
     * 
     */
    @XmlEnumValue("shdw13")
    SHDW_13("shdw13"),

    /**
     * Bottom Right Small Drop Shadow
     * 
     */
    @XmlEnumValue("shdw14")
    SHDW_14("shdw14"),

    /**
     * Front Left Long Perspective Shadow
     * 
     */
    @XmlEnumValue("shdw15")
    SHDW_15("shdw15"),

    /**
     * Front Right LongPerspective Shadow
     * 
     */
    @XmlEnumValue("shdw16")
    SHDW_16("shdw16"),

    /**
     *  3D Outer Box Shadow
     * 
     */
    @XmlEnumValue("shdw17")
    SHDW_17("shdw17"),

    /**
     *  3D Inner Box Shadow
     * 
     */
    @XmlEnumValue("shdw18")
    SHDW_18("shdw18"),

    /**
     * Back Center Perspective Shadow
     * 
     */
    @XmlEnumValue("shdw19")
    SHDW_19("shdw19"),

    /**
     * Front Bottom Shadow
     * 
     */
    @XmlEnumValue("shdw20")
    SHDW_20("shdw20");
    private final String value;

    STPresetShadowVal(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPresetShadowVal fromValue(String v) {
        for (STPresetShadowVal c: STPresetShadowVal.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
