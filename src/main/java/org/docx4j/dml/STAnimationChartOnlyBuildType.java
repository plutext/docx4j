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
 * <p>Java class for ST_AnimationChartOnlyBuildType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_AnimationChartOnlyBuildType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="series"/>
 *     &lt;enumeration value="category"/>
 *     &lt;enumeration value="seriesEl"/>
 *     &lt;enumeration value="categoryEl"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_AnimationChartOnlyBuildType")
@XmlEnum
public enum STAnimationChartOnlyBuildType {


    /**
     * Series
     * 
     */
    @XmlEnumValue("series")
    SERIES("series"),

    /**
     * Catefory
     * 
     */
    @XmlEnumValue("category")
    CATEGORY("category"),

    /**
     * Series Element
     * 
     */
    @XmlEnumValue("seriesEl")
    SERIES_EL("seriesEl"),

    /**
     * Category Element
     * 
     */
    @XmlEnumValue("categoryEl")
    CATEGORY_EL("categoryEl");
    private final String value;

    STAnimationChartOnlyBuildType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STAnimationChartOnlyBuildType fromValue(String v) {
        for (STAnimationChartOnlyBuildType c: STAnimationChartOnlyBuildType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
