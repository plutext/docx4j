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
 * <p>Java class for ST_Shape.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Shape">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="cone"/>
 *     &lt;enumeration value="coneToMax"/>
 *     &lt;enumeration value="box"/>
 *     &lt;enumeration value="cylinder"/>
 *     &lt;enumeration value="pyramid"/>
 *     &lt;enumeration value="pyramidToMax"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Shape")
@XmlEnum
public enum STShape {


    /**
     * Cone
     * 
     */
    @XmlEnumValue("cone")
    CONE("cone"),

    /**
     * Cone to Max
     * 
     */
    @XmlEnumValue("coneToMax")
    CONE_TO_MAX("coneToMax"),

    /**
     * Box
     * 
     */
    @XmlEnumValue("box")
    BOX("box"),

    /**
     * Cylinder
     * 
     */
    @XmlEnumValue("cylinder")
    CYLINDER("cylinder"),

    /**
     * Pyramid
     * 
     */
    @XmlEnumValue("pyramid")
    PYRAMID("pyramid"),

    /**
     * Pyramid to Maximum
     * 
     */
    @XmlEnumValue("pyramidToMax")
    PYRAMID_TO_MAX("pyramidToMax");
    private final String value;

    STShape(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STShape fromValue(String v) {
        for (STShape c: STShape.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
