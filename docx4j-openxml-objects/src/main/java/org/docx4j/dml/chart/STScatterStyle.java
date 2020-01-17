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
 * &lt;simpleType name="ST_ScatterStyle"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="none"/&gt;
 *     &lt;enumeration value="line"/&gt;
 *     &lt;enumeration value="lineMarker"/&gt;
 *     &lt;enumeration value="marker"/&gt;
 *     &lt;enumeration value="smooth"/&gt;
 *     &lt;enumeration value="smoothMarker"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
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
