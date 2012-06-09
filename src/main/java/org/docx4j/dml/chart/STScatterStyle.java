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
 
package org.docx4j.dml.chart;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ScatterStyle.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ScatterStyle">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="line"/>
 *     &lt;enumeration value="lineMarker"/>
 *     &lt;enumeration value="marker"/>
 *     &lt;enumeration value="smooth"/>
 *     &lt;enumeration value="smoothMarker"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ScatterStyle")
@XmlEnum
public enum STScatterStyle {


    /**
     * None
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Line
     * 
     */
    @XmlEnumValue("line")
    LINE("line"),

    /**
     * Line with Markers
     * 
     */
    @XmlEnumValue("lineMarker")
    LINE_MARKER("lineMarker"),

    /**
     * Marker
     * 
     */
    @XmlEnumValue("marker")
    MARKER("marker"),

    /**
     * Smooth
     * 
     */
    @XmlEnumValue("smooth")
    SMOOTH("smooth"),

    /**
     * Smooth with Markers
     * 
     */
    @XmlEnumValue("smoothMarker")
    SMOOTH_MARKER("smoothMarker");
    private final String value;

    STScatterStyle(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STScatterStyle fromValue(String v) {
        for (STScatterStyle c: STScatterStyle.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
