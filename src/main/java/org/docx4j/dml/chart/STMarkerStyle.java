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
 * <p>Java class for ST_MarkerStyle.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_MarkerStyle">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="circle"/>
 *     &lt;enumeration value="dash"/>
 *     &lt;enumeration value="diamond"/>
 *     &lt;enumeration value="dot"/>
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="picture"/>
 *     &lt;enumeration value="plus"/>
 *     &lt;enumeration value="square"/>
 *     &lt;enumeration value="star"/>
 *     &lt;enumeration value="triangle"/>
 *     &lt;enumeration value="x"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
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
