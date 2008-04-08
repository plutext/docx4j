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

package org.docx4j.wml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TextDirection.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TextDirection">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="lrTb"/>
 *     &lt;enumeration value="tbRl"/>
 *     &lt;enumeration value="btLr"/>
 *     &lt;enumeration value="lrTbV"/>
 *     &lt;enumeration value="tbRlV"/>
 *     &lt;enumeration value="tbLrV"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TextDirection")
@XmlEnum
public enum STTextDirection {


    /**
     * Left to Right, Top to
     * 						Bottom
     * 
     */
    @XmlEnumValue("lrTb")
    LR_TB("lrTb"),

    /**
     * Top to Bottom, Right to
     * 						Left
     * 
     */
    @XmlEnumValue("tbRl")
    TB_RL("tbRl"),

    /**
     * Bottom to Top, Left to
     * 						Right
     * 
     */
    @XmlEnumValue("btLr")
    BT_LR("btLr"),

    /**
     * Left to Right, Top to Bottom
     * 						Rotated
     * 
     */
    @XmlEnumValue("lrTbV")
    LR_TB_V("lrTbV"),

    /**
     * Top to Bottom, Right to Left
     * 						Rotated
     * 
     */
    @XmlEnumValue("tbRlV")
    TB_RL_V("tbRlV"),

    /**
     * Top to Bottom, Left to Right
     * 						Rotated
     * 
     */
    @XmlEnumValue("tbLrV")
    TB_LR_V("tbLrV");
    private final String value;

    STTextDirection(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTextDirection fromValue(String v) {
        for (STTextDirection c: STTextDirection.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
