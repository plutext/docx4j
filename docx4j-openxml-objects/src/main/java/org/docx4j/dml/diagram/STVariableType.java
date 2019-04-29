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


package org.docx4j.dml.diagram;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_VariableType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_VariableType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="none"/&gt;
 *     &lt;enumeration value="orgChart"/&gt;
 *     &lt;enumeration value="chMax"/&gt;
 *     &lt;enumeration value="chPref"/&gt;
 *     &lt;enumeration value="bulEnabled"/&gt;
 *     &lt;enumeration value="dir"/&gt;
 *     &lt;enumeration value="hierBranch"/&gt;
 *     &lt;enumeration value="animOne"/&gt;
 *     &lt;enumeration value="animLvl"/&gt;
 *     &lt;enumeration value="resizeHandles"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_VariableType")
@XmlEnum
public enum STVariableType {


    /**
     * Unknown
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Organizational Chart Algorithm
     * 
     */
    @XmlEnumValue("orgChart")
    ORG_CHART("orgChart"),

    /**
     * Child Max
     * 
     */
    @XmlEnumValue("chMax")
    CH_MAX("chMax"),

    /**
     * Child Preference
     * 
     */
    @XmlEnumValue("chPref")
    CH_PREF("chPref"),

    /**
     * Bullets Enabled
     * 
     */
    @XmlEnumValue("bulEnabled")
    BUL_ENABLED("bulEnabled"),

    /**
     * Direction
     * 
     */
    @XmlEnumValue("dir")
    DIR("dir"),

    /**
     * Hierarchy Branch
     * 
     */
    @XmlEnumValue("hierBranch")
    HIER_BRANCH("hierBranch"),

    /**
     * Animate One
     * 
     */
    @XmlEnumValue("animOne")
    ANIM_ONE("animOne"),

    /**
     * Animation Level
     * 
     */
    @XmlEnumValue("animLvl")
    ANIM_LVL("animLvl"),

    /**
     * Resize Handles
     * 
     */
    @XmlEnumValue("resizeHandles")
    RESIZE_HANDLES("resizeHandles");
    private final String value;

    STVariableType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STVariableType fromValue(String v) {
        for (STVariableType c: STVariableType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
