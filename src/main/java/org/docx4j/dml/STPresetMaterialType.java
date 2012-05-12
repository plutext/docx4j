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
 * &lt;simpleType name="ST_PresetMaterialType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="legacyMatte"/>
 *     &lt;enumeration value="legacyPlastic"/>
 *     &lt;enumeration value="legacyMetal"/>
 *     &lt;enumeration value="legacyWireframe"/>
 *     &lt;enumeration value="matte"/>
 *     &lt;enumeration value="plastic"/>
 *     &lt;enumeration value="metal"/>
 *     &lt;enumeration value="warmMatte"/>
 *     &lt;enumeration value="translucentPowder"/>
 *     &lt;enumeration value="powder"/>
 *     &lt;enumeration value="dkEdge"/>
 *     &lt;enumeration value="softEdge"/>
 *     &lt;enumeration value="clear"/>
 *     &lt;enumeration value="flat"/>
 *     &lt;enumeration value="softmetal"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
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
