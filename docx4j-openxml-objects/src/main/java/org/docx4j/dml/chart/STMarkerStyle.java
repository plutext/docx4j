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
 * <p>Java class for ST_MarkerStyle.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_MarkerStyle"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="circle"/&gt;
 *     &lt;enumeration value="dash"/&gt;
 *     &lt;enumeration value="diamond"/&gt;
 *     &lt;enumeration value="dot"/&gt;
 *     &lt;enumeration value="none"/&gt;
 *     &lt;enumeration value="picture"/&gt;
 *     &lt;enumeration value="plus"/&gt;
 *     &lt;enumeration value="square"/&gt;
 *     &lt;enumeration value="star"/&gt;
 *     &lt;enumeration value="triangle"/&gt;
 *     &lt;enumeration value="x"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_MarkerStyle")
@XmlEnum
public enum STMarkerStyle {


    /**
     * Circle
     * 
     */
    @XmlEnumValue("circle")
    CIRCLE("circle"),

    /**
     * Dash
     * 
     */
    @XmlEnumValue("dash")
    DASH("dash"),

    /**
     * Diamond
     * 
     */
    @XmlEnumValue("diamond")
    DIAMOND("diamond"),

    /**
     * Dot
     * 
     */
    @XmlEnumValue("dot")
    DOT("dot"),

    /**
     * None
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Picture
     * 
     */
    @XmlEnumValue("picture")
    PICTURE("picture"),

    /**
     * Plus
     * 
     */
    @XmlEnumValue("plus")
    PLUS("plus"),

    /**
     * Square
     * 
     */
    @XmlEnumValue("square")
    SQUARE("square"),

    /**
     * Star
     * 
     */
    @XmlEnumValue("star")
    STAR("star"),

    /**
     * Triangle
     * 
     */
    @XmlEnumValue("triangle")
    TRIANGLE("triangle"),

    /**
     * X
     * 
     */
    @XmlEnumValue("x")
    X("x");
    private final String value;

    STMarkerStyle(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STMarkerStyle fromValue(String v) {
        for (STMarkerStyle c: STMarkerStyle.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
