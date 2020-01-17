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


package org.docx4j.vml.officedrawing;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_BWMode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_BWMode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="color"/>
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="grayScale"/>
 *     &lt;enumeration value="lightGrayscale"/>
 *     &lt;enumeration value="inverseGray"/>
 *     &lt;enumeration value="grayOutline"/>
 *     &lt;enumeration value="highContrast"/>
 *     &lt;enumeration value="black"/>
 *     &lt;enumeration value="white"/>
 *     &lt;enumeration value="hide"/>
 *     &lt;enumeration value="undrawn"/>
 *     &lt;enumeration value="blackTextAndLines"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_BWMode")
@XmlEnum
public enum STBWMode {


    /**
     * Color
     * 
     */
    @XmlEnumValue("color")
    COLOR("color"),

    /**
     * Automatic
     * 
     */
    @XmlEnumValue("auto")
    AUTO("auto"),

    /**
     * Grayscale
     * 
     */
    @XmlEnumValue("grayScale")
    GRAY_SCALE("grayScale"),

    /**
     * Light grayscale
     * 
     */
    @XmlEnumValue("lightGrayscale")
    LIGHT_GRAYSCALE("lightGrayscale"),

    /**
     * Inverse Grayscale
     * 
     */
    @XmlEnumValue("inverseGray")
    INVERSE_GRAY("inverseGray"),

    /**
     * Gray Outlines
     * 
     */
    @XmlEnumValue("grayOutline")
    GRAY_OUTLINE("grayOutline"),

    /**
     * Black And White
     * 
     */
    @XmlEnumValue("highContrast")
    HIGH_CONTRAST("highContrast"),

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
     * Hide Object When Displayed in Black and White
     * 
     */
    @XmlEnumValue("hide")
    HIDE("hide"),

    /**
     * Do Not Show
     * 
     */
    @XmlEnumValue("undrawn")
    UNDRAWN("undrawn"),

    /**
     * Black Text And Lines
     * 
     */
    @XmlEnumValue("blackTextAndLines")
    BLACK_TEXT_AND_LINES("blackTextAndLines");
    private final String value;

    STBWMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STBWMode fromValue(String v) {
        for (STBWMode c: STBWMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
