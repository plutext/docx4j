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


package org.docx4j.vml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_EditAs.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_EditAs">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="canvas"/>
 *     &lt;enumeration value="orgchart"/>
 *     &lt;enumeration value="radial"/>
 *     &lt;enumeration value="cycle"/>
 *     &lt;enumeration value="stacked"/>
 *     &lt;enumeration value="venn"/>
 *     &lt;enumeration value="bullseye"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_EditAs")
@XmlEnum
public enum STEditAs {


    /**
     * Shape Canvas
     * 
     */
    @XmlEnumValue("canvas")
    CANVAS("canvas"),

    /**
     * Organization Chart Diagram
     * 
     */
    @XmlEnumValue("orgchart")
    ORGCHART("orgchart"),

    /**
     * Radial Diagram
     * 
     */
    @XmlEnumValue("radial")
    RADIAL("radial"),

    /**
     * Cycle Diagram
     * 
     */
    @XmlEnumValue("cycle")
    CYCLE("cycle"),

    /**
     * Pyramid Diagram
     * 
     */
    @XmlEnumValue("stacked")
    STACKED("stacked"),

    /**
     * Venn Diagram
     * 
     */
    @XmlEnumValue("venn")
    VENN("venn"),

    /**
     * Bullseye Diagram
     * 
     */
    @XmlEnumValue("bullseye")
    BULLSEYE("bullseye");
    private final String value;

    STEditAs(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STEditAs fromValue(String v) {
        for (STEditAs c: STEditAs.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
