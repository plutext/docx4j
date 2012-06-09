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
 
package org.docx4j.dml.wordprocessingDrawing;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_RelFromH.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_RelFromH">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="margin"/>
 *     &lt;enumeration value="page"/>
 *     &lt;enumeration value="column"/>
 *     &lt;enumeration value="character"/>
 *     &lt;enumeration value="leftMargin"/>
 *     &lt;enumeration value="rightMargin"/>
 *     &lt;enumeration value="insideMargin"/>
 *     &lt;enumeration value="outsideMargin"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_RelFromH")
@XmlEnum
public enum STRelFromH {


    /**
     * Page Margin
     * 
     */
    @XmlEnumValue("margin")
    MARGIN("margin"),

    /**
     * Page Edge
     * 
     */
    @XmlEnumValue("page")
    PAGE("page"),

    /**
     * Column
     * 
     */
    @XmlEnumValue("column")
    COLUMN("column"),

    /**
     * Character
     * 
     */
    @XmlEnumValue("character")
    CHARACTER("character"),

    /**
     * Left Margin
     * 
     */
    @XmlEnumValue("leftMargin")
    LEFT_MARGIN("leftMargin"),

    /**
     * Right Margin
     * 
     */
    @XmlEnumValue("rightMargin")
    RIGHT_MARGIN("rightMargin"),

    /**
     * Inside Margin
     * 
     */
    @XmlEnumValue("insideMargin")
    INSIDE_MARGIN("insideMargin"),

    /**
     * 
     * 						Outside Margin
     * 					
     * 
     */
    @XmlEnumValue("outsideMargin")
    OUTSIDE_MARGIN("outsideMargin");
    private final String value;

    STRelFromH(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STRelFromH fromValue(String v) {
        for (STRelFromH c: STRelFromH.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
