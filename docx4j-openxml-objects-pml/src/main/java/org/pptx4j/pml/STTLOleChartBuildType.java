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
 * <p>Java class for ST_TLOleChartBuildType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TLOleChartBuildType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="allAtOnce"/>
 *     &lt;enumeration value="series"/>
 *     &lt;enumeration value="category"/>
 *     &lt;enumeration value="seriesEl"/>
 *     &lt;enumeration value="categoryEl"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_TLOleChartBuildType")
@XmlEnum
public enum STTLOleChartBuildType {


    /**
     * Chart Build Type Enum ( All At Once )
     * 
     */
    @XmlEnumValue("allAtOnce")
    ALL_AT_ONCE("allAtOnce"),

    /**
     * Chart Build Type Enum ( Series )
     * 
     */
    @XmlEnumValue("series")
    SERIES("series"),

    /**
     * Chart Build Type Enum ( Category )
     * 
     */
    @XmlEnumValue("category")
    CATEGORY("category"),

    /**
     * Chart Build Type Enum ( Series Element )
     * 
     */
    @XmlEnumValue("seriesEl")
    SERIES_EL("seriesEl"),

    /**
     * Chart Build Type Enum ( Category Element )
     * 
     */
    @XmlEnumValue("categoryEl")
    CATEGORY_EL("categoryEl");
    private final String value;

    STTLOleChartBuildType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTLOleChartBuildType fromValue(String v) {
        for (STTLOleChartBuildType c: STTLOleChartBuildType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
