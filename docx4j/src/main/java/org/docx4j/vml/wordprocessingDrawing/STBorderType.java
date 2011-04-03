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


package org.docx4j.vml.wordprocessingDrawing;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_BorderType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_BorderType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="single"/>
 *     &lt;enumeration value="thick"/>
 *     &lt;enumeration value="double"/>
 *     &lt;enumeration value="hairline"/>
 *     &lt;enumeration value="dot"/>
 *     &lt;enumeration value="dash"/>
 *     &lt;enumeration value="dotDash"/>
 *     &lt;enumeration value="dashDotDot"/>
 *     &lt;enumeration value="triple"/>
 *     &lt;enumeration value="thinThickSmall"/>
 *     &lt;enumeration value="thickThinSmall"/>
 *     &lt;enumeration value="thickBetweenThinSmall"/>
 *     &lt;enumeration value="thinThick"/>
 *     &lt;enumeration value="thickThin"/>
 *     &lt;enumeration value="thickBetweenThin"/>
 *     &lt;enumeration value="thinThickLarge"/>
 *     &lt;enumeration value="thickThinLarge"/>
 *     &lt;enumeration value="thickBetweenThinLarge"/>
 *     &lt;enumeration value="wave"/>
 *     &lt;enumeration value="doubleWave"/>
 *     &lt;enumeration value="dashedSmall"/>
 *     &lt;enumeration value="dashDotStroked"/>
 *     &lt;enumeration value="threeDEmboss"/>
 *     &lt;enumeration value="threeDEngrave"/>
 *     &lt;enumeration value="HTMLOutset"/>
 *     &lt;enumeration value="HTMLInset"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_BorderType")
@XmlEnum
public enum STBorderType {


    /**
     * No Border
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Single Line Border
     * 
     */
    @XmlEnumValue("single")
    SINGLE("single"),

    /**
     * Thick Line Border
     * 
     */
    @XmlEnumValue("thick")
    THICK("thick"),

    /**
     * Double Line Border
     * 
     */
    @XmlEnumValue("double")
    DOUBLE("double"),

    /**
     * Hairline Border
     * 
     */
    @XmlEnumValue("hairline")
    HAIRLINE("hairline"),

    /**
     * Dotted Border
     * 
     */
    @XmlEnumValue("dot")
    DOT("dot"),

    /**
     * pecifies a line border consisting of a dashed line around the parent object.
     * 
     */
    @XmlEnumValue("dash")
    DASH("dash"),

    /**
     * Dot Dash Border
     * 
     */
    @XmlEnumValue("dotDash")
    DOT_DASH("dotDash"),

    /**
     * Dash Dot Dot Border
     * 
     */
    @XmlEnumValue("dashDotDot")
    DASH_DOT_DOT("dashDotDot"),

    /**
     * Triple Line Border
     * 
     */
    @XmlEnumValue("triple")
    TRIPLE("triple"),

    /**
     * Thin Thick Small Gap Border
     * 
     */
    @XmlEnumValue("thinThickSmall")
    THIN_THICK_SMALL("thinThickSmall"),

    /**
     * Small thick-thin lines border
     * 
     */
    @XmlEnumValue("thickThinSmall")
    THICK_THIN_SMALL("thickThinSmall"),

    /**
     * Small thin-thick-thin Lines Border
     * 
     */
    @XmlEnumValue("thickBetweenThinSmall")
    THICK_BETWEEN_THIN_SMALL("thickBetweenThinSmall"),

    /**
     * Thin Thick Line Border
     * 
     */
    @XmlEnumValue("thinThick")
    THIN_THICK("thinThick"),

    /**
     * Thick Thin Line Border
     * 
     */
    @XmlEnumValue("thickThin")
    THICK_THIN("thickThin"),

    /**
     * Thin-thick-thin Border
     * 
     */
    @XmlEnumValue("thickBetweenThin")
    THICK_BETWEEN_THIN("thickBetweenThin"),

    /**
     * Thin Thick Large Gap Border
     * 
     */
    @XmlEnumValue("thinThickLarge")
    THIN_THICK_LARGE("thinThickLarge"),

    /**
     * Thick Thin Large Gap Border
     * 
     */
    @XmlEnumValue("thickThinLarge")
    THICK_THIN_LARGE("thickThinLarge"),

    /**
     * Large thin-thick-thin Border
     * 
     */
    @XmlEnumValue("thickBetweenThinLarge")
    THICK_BETWEEN_THIN_LARGE("thickBetweenThinLarge"),

    /**
     * Wavy Border
     * 
     */
    @XmlEnumValue("wave")
    WAVE("wave"),

    /**
     * Double Wavy Lines Border
     * 
     */
    @XmlEnumValue("doubleWave")
    DOUBLE_WAVE("doubleWave"),

    /**
     * Small Dash Border
     * 
     */
    @XmlEnumValue("dashedSmall")
    DASHED_SMALL("dashedSmall"),

    /**
     * Stroked Dash Dot Border
     * 
     */
    @XmlEnumValue("dashDotStroked")
    DASH_DOT_STROKED("dashDotStroked"),

    /**
     *  3D Embossed Border
     * 
     */
    @XmlEnumValue("threeDEmboss")
    THREE_D_EMBOSS("threeDEmboss"),

    /**
     *  3D Engraved Border
     * 
     */
    @XmlEnumValue("threeDEngrave")
    THREE_D_ENGRAVE("threeDEngrave"),

    /**
     * Outset Border
     * 
     */
    @XmlEnumValue("HTMLOutset")
    HTML_OUTSET("HTMLOutset"),

    /**
     * Inset Border
     * 
     */
    @XmlEnumValue("HTMLInset")
    HTML_INSET("HTMLInset");
    private final String value;

    STBorderType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STBorderType fromValue(String v) {
        for (STBorderType c: STBorderType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
