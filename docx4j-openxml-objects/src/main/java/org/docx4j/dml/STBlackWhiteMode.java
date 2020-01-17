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


package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_BlackWhiteMode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_BlackWhiteMode"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="clr"/&gt;
 *     &lt;enumeration value="auto"/&gt;
 *     &lt;enumeration value="gray"/&gt;
 *     &lt;enumeration value="ltGray"/&gt;
 *     &lt;enumeration value="invGray"/&gt;
 *     &lt;enumeration value="grayWhite"/&gt;
 *     &lt;enumeration value="blackGray"/&gt;
 *     &lt;enumeration value="blackWhite"/&gt;
 *     &lt;enumeration value="black"/&gt;
 *     &lt;enumeration value="white"/&gt;
 *     &lt;enumeration value="hidden"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_BlackWhiteMode")
@XmlEnum
public enum STBlackWhiteMode {


    /**
     * Color
     * 
     */
    @XmlEnumValue("clr")
    CLR("clr"),

    /**
     * Automatic
     * 
     */
    @XmlEnumValue("auto")
    AUTO("auto"),

    /**
     * Gray
     * 
     */
    @XmlEnumValue("gray")
    GRAY("gray"),

    /**
     * Light Gray
     * 
     */
    @XmlEnumValue("ltGray")
    LT_GRAY("ltGray"),

    /**
     * Inverse Gray
     * 
     */
    @XmlEnumValue("invGray")
    INV_GRAY("invGray"),

    /**
     * Gray and White
     * 
     */
    @XmlEnumValue("grayWhite")
    GRAY_WHITE("grayWhite"),

    /**
     * Black and Gray
     * 
     */
    @XmlEnumValue("blackGray")
    BLACK_GRAY("blackGray"),

    /**
     * Black and White
     * 
     */
    @XmlEnumValue("blackWhite")
    BLACK_WHITE("blackWhite"),

    /**
     * Black
     * 
     */
    @XmlEnumValue("black")
    BLACK("black"),

    /**
     * White
     * 
     */
    @XmlEnumValue("white")
    WHITE("white"),

    /**
     * Hidden
     * 
     */
    @XmlEnumValue("hidden")
    HIDDEN("hidden");
    private final String value;

    STBlackWhiteMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STBlackWhiteMode fromValue(String v) {
        for (STBlackWhiteMode c: STBlackWhiteMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
