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
 * <p>Java class for ST_PresetMaterialType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PresetMaterialType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="legacyMatte"/&gt;
 *     &lt;enumeration value="legacyPlastic"/&gt;
 *     &lt;enumeration value="legacyMetal"/&gt;
 *     &lt;enumeration value="legacyWireframe"/&gt;
 *     &lt;enumeration value="matte"/&gt;
 *     &lt;enumeration value="plastic"/&gt;
 *     &lt;enumeration value="metal"/&gt;
 *     &lt;enumeration value="warmMatte"/&gt;
 *     &lt;enumeration value="translucentPowder"/&gt;
 *     &lt;enumeration value="powder"/&gt;
 *     &lt;enumeration value="dkEdge"/&gt;
 *     &lt;enumeration value="softEdge"/&gt;
 *     &lt;enumeration value="clear"/&gt;
 *     &lt;enumeration value="flat"/&gt;
 *     &lt;enumeration value="softmetal"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_PresetMaterialType")
@XmlEnum
public enum STPresetMaterialType {


    /**
     * Legacy Matte
     * 
     */
    @XmlEnumValue("legacyMatte")
    LEGACY_MATTE("legacyMatte"),

    /**
     * Legacy Plastic
     * 
     */
    @XmlEnumValue("legacyPlastic")
    LEGACY_PLASTIC("legacyPlastic"),

    /**
     * Legacy Metal
     * 
     */
    @XmlEnumValue("legacyMetal")
    LEGACY_METAL("legacyMetal"),

    /**
     * Legacy Wireframe
     * 
     */
    @XmlEnumValue("legacyWireframe")
    LEGACY_WIREFRAME("legacyWireframe"),

    /**
     * Matte
     * 
     */
    @XmlEnumValue("matte")
    MATTE("matte"),

    /**
     * Plastic
     * 
     */
    @XmlEnumValue("plastic")
    PLASTIC("plastic"),

    /**
     * Metal
     * 
     */
    @XmlEnumValue("metal")
    METAL("metal"),

    /**
     * Warm Matte
     * 
     */
    @XmlEnumValue("warmMatte")
    WARM_MATTE("warmMatte"),

    /**
     * Translucent Powder
     * 
     */
    @XmlEnumValue("translucentPowder")
    TRANSLUCENT_POWDER("translucentPowder"),

    /**
     * Powder
     * 
     */
    @XmlEnumValue("powder")
    POWDER("powder"),

    /**
     * Dark Edge
     * 
     */
    @XmlEnumValue("dkEdge")
    DK_EDGE("dkEdge"),

    /**
     * Soft Edge
     * 
     */
    @XmlEnumValue("softEdge")
    SOFT_EDGE("softEdge"),

    /**
     * Clear
     * 
     */
    @XmlEnumValue("clear")
    CLEAR("clear"),

    /**
     * Flat
     * 
     */
    @XmlEnumValue("flat")
    FLAT("flat"),

    /**
     * Soft Metal
     * 
     */
    @XmlEnumValue("softmetal")
    SOFTMETAL("softmetal");
    private final String value;

    STPresetMaterialType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPresetMaterialType fromValue(String v) {
        for (STPresetMaterialType c: STPresetMaterialType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
