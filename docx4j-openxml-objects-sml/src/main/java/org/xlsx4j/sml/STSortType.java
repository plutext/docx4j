/*
 *  Copyright 2010-2013, Plutext Pty Ltd.
 *   
 *  This file is part of xlsx4j, a component of docx4j.

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
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_SortType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_SortType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="ascending"/>
 *     &lt;enumeration value="descending"/>
 *     &lt;enumeration value="ascendingAlpha"/>
 *     &lt;enumeration value="descendingAlpha"/>
 *     &lt;enumeration value="ascendingNatural"/>
 *     &lt;enumeration value="descendingNatural"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_SortType")
@XmlEnum
public enum STSortType {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("ascending")
    ASCENDING("ascending"),
    @XmlEnumValue("descending")
    DESCENDING("descending"),
    @XmlEnumValue("ascendingAlpha")
    ASCENDING_ALPHA("ascendingAlpha"),
    @XmlEnumValue("descendingAlpha")
    DESCENDING_ALPHA("descendingAlpha"),
    @XmlEnumValue("ascendingNatural")
    ASCENDING_NATURAL("ascendingNatural"),
    @XmlEnumValue("descendingNatural")
    DESCENDING_NATURAL("descendingNatural");
    private final String value;

    STSortType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STSortType fromValue(String v) {
        for (STSortType c: STSortType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
