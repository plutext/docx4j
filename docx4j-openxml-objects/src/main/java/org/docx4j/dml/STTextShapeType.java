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
 * <p>Java class for ST_TextShapeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TextShapeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="textNoShape"/&gt;
 *     &lt;enumeration value="textPlain"/&gt;
 *     &lt;enumeration value="textStop"/&gt;
 *     &lt;enumeration value="textTriangle"/&gt;
 *     &lt;enumeration value="textTriangleInverted"/&gt;
 *     &lt;enumeration value="textChevron"/&gt;
 *     &lt;enumeration value="textChevronInverted"/&gt;
 *     &lt;enumeration value="textRingInside"/&gt;
 *     &lt;enumeration value="textRingOutside"/&gt;
 *     &lt;enumeration value="textArchUp"/&gt;
 *     &lt;enumeration value="textArchDown"/&gt;
 *     &lt;enumeration value="textCircle"/&gt;
 *     &lt;enumeration value="textButton"/&gt;
 *     &lt;enumeration value="textArchUpPour"/&gt;
 *     &lt;enumeration value="textArchDownPour"/&gt;
 *     &lt;enumeration value="textCirclePour"/&gt;
 *     &lt;enumeration value="textButtonPour"/&gt;
 *     &lt;enumeration value="textCurveUp"/&gt;
 *     &lt;enumeration value="textCurveDown"/&gt;
 *     &lt;enumeration value="textCanUp"/&gt;
 *     &lt;enumeration value="textCanDown"/&gt;
 *     &lt;enumeration value="textWave1"/&gt;
 *     &lt;enumeration value="textWave2"/&gt;
 *     &lt;enumeration value="textDoubleWave1"/&gt;
 *     &lt;enumeration value="textWave4"/&gt;
 *     &lt;enumeration value="textInflate"/&gt;
 *     &lt;enumeration value="textDeflate"/&gt;
 *     &lt;enumeration value="textInflateBottom"/&gt;
 *     &lt;enumeration value="textDeflateBottom"/&gt;
 *     &lt;enumeration value="textInflateTop"/&gt;
 *     &lt;enumeration value="textDeflateTop"/&gt;
 *     &lt;enumeration value="textDeflateInflate"/&gt;
 *     &lt;enumeration value="textDeflateInflateDeflate"/&gt;
 *     &lt;enumeration value="textFadeRight"/&gt;
 *     &lt;enumeration value="textFadeLeft"/&gt;
 *     &lt;enumeration value="textFadeUp"/&gt;
 *     &lt;enumeration value="textFadeDown"/&gt;
 *     &lt;enumeration value="textSlantUp"/&gt;
 *     &lt;enumeration value="textSlantDown"/&gt;
 *     &lt;enumeration value="textCascadeUp"/&gt;
 *     &lt;enumeration value="textCascadeDown"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_TextShapeType")
@XmlEnum
public enum STTextShapeType {


    /**
     * No Text Shape
     * 
     */
    @XmlEnumValue("textNoShape")
    TEXT_NO_SHAPE("textNoShape"),

    /**
     * Plain Text Shape
     * 
     */
    @XmlEnumValue("textPlain")
    TEXT_PLAIN("textPlain"),

    /**
     * Stop Sign Text Shape
     * 
     */
    @XmlEnumValue("textStop")
    TEXT_STOP("textStop"),

    /**
     * Triangle Text Shape
     * 
     */
    @XmlEnumValue("textTriangle")
    TEXT_TRIANGLE("textTriangle"),

    /**
     * Inverted Triangle Text Shape
     * 
     */
    @XmlEnumValue("textTriangleInverted")
    TEXT_TRIANGLE_INVERTED("textTriangleInverted"),

    /**
     * Chevron Text Shape
     * 
     */
    @XmlEnumValue("textChevron")
    TEXT_CHEVRON("textChevron"),

    /**
     * Inverted Chevron Text Shape
     * 
     */
    @XmlEnumValue("textChevronInverted")
    TEXT_CHEVRON_INVERTED("textChevronInverted"),

    /**
     * Inside Ring Text Shape
     * 
     */
    @XmlEnumValue("textRingInside")
    TEXT_RING_INSIDE("textRingInside"),

    /**
     * Outside Ring Text Shape
     * 
     */
    @XmlEnumValue("textRingOutside")
    TEXT_RING_OUTSIDE("textRingOutside"),

    /**
     * Upward Arch Text Shape
     * 
     */
    @XmlEnumValue("textArchUp")
    TEXT_ARCH_UP("textArchUp"),

    /**
     * Downward Arch Text Shape
     * 
     */
    @XmlEnumValue("textArchDown")
    TEXT_ARCH_DOWN("textArchDown"),

    /**
     * Circle Text Shape
     * 
     */
    @XmlEnumValue("textCircle")
    TEXT_CIRCLE("textCircle"),

    /**
     * Button Text Shape
     * 
     */
    @XmlEnumValue("textButton")
    TEXT_BUTTON("textButton"),

    /**
     * Upward Pour Arch Text Shape
     * 
     */
    @XmlEnumValue("textArchUpPour")
    TEXT_ARCH_UP_POUR("textArchUpPour"),

    /**
     * Downward Pour Arch Text Shape
     * 
     */
    @XmlEnumValue("textArchDownPour")
    TEXT_ARCH_DOWN_POUR("textArchDownPour"),

    /**
     * Circle Pour Text Shape
     * 
     */
    @XmlEnumValue("textCirclePour")
    TEXT_CIRCLE_POUR("textCirclePour"),

    /**
     * Button Pour Text Shape
     * 
     */
    @XmlEnumValue("textButtonPour")
    TEXT_BUTTON_POUR("textButtonPour"),

    /**
     * Upward Curve Text Shape
     * 
     */
    @XmlEnumValue("textCurveUp")
    TEXT_CURVE_UP("textCurveUp"),

    /**
     * Downward Curve Text Shape
     * 
     */
    @XmlEnumValue("textCurveDown")
    TEXT_CURVE_DOWN("textCurveDown"),

    /**
     * Upward Can Text Shape
     * 
     */
    @XmlEnumValue("textCanUp")
    TEXT_CAN_UP("textCanUp"),

    /**
     * Downward Can Text Shape
     * 
     */
    @XmlEnumValue("textCanDown")
    TEXT_CAN_DOWN("textCanDown"),

    /**
     * Wave 1 Text Shape
     * 
     */
    @XmlEnumValue("textWave1")
    TEXT_WAVE_1("textWave1"),

    /**
     * Wave 2 Text Shape
     * 
     */
    @XmlEnumValue("textWave2")
    TEXT_WAVE_2("textWave2"),

    /**
     * Double Wave 1 Text Shape
     * 
     */
    @XmlEnumValue("textDoubleWave1")
    TEXT_DOUBLE_WAVE_1("textDoubleWave1"),

    /**
     * Wave 4 Text Shape
     * 
     */
    @XmlEnumValue("textWave4")
    TEXT_WAVE_4("textWave4"),

    /**
     * Inflate Text Shape
     * 
     */
    @XmlEnumValue("textInflate")
    TEXT_INFLATE("textInflate"),

    /**
     * Deflate Text Shape
     * 
     */
    @XmlEnumValue("textDeflate")
    TEXT_DEFLATE("textDeflate"),

    /**
     * Bottom Inflate Text Shape
     * 
     */
    @XmlEnumValue("textInflateBottom")
    TEXT_INFLATE_BOTTOM("textInflateBottom"),

    /**
     * Bottom Deflate Text Shape
     * 
     */
    @XmlEnumValue("textDeflateBottom")
    TEXT_DEFLATE_BOTTOM("textDeflateBottom"),

    /**
     * Top Inflate Text Shape
     * 
     */
    @XmlEnumValue("textInflateTop")
    TEXT_INFLATE_TOP("textInflateTop"),

    /**
     * Top Deflate Text Shape
     * 
     */
    @XmlEnumValue("textDeflateTop")
    TEXT_DEFLATE_TOP("textDeflateTop"),

    /**
     * Deflate-Inflate Text Shape
     * 
     */
    @XmlEnumValue("textDeflateInflate")
    TEXT_DEFLATE_INFLATE("textDeflateInflate"),

    /**
     * Deflate-Inflate-Deflate Text Shape
     * 
     */
    @XmlEnumValue("textDeflateInflateDeflate")
    TEXT_DEFLATE_INFLATE_DEFLATE("textDeflateInflateDeflate"),

    /**
     * Right Fade Text Shape
     * 
     */
    @XmlEnumValue("textFadeRight")
    TEXT_FADE_RIGHT("textFadeRight"),

    /**
     * Left Fade Text Shape
     * 
     */
    @XmlEnumValue("textFadeLeft")
    TEXT_FADE_LEFT("textFadeLeft"),

    /**
     * Upward Fade Text Shape
     * 
     */
    @XmlEnumValue("textFadeUp")
    TEXT_FADE_UP("textFadeUp"),

    /**
     * Downward Fade Text Shape
     * 
     */
    @XmlEnumValue("textFadeDown")
    TEXT_FADE_DOWN("textFadeDown"),

    /**
     * Upward Slant Text Shape
     * 
     */
    @XmlEnumValue("textSlantUp")
    TEXT_SLANT_UP("textSlantUp"),

    /**
     * Downward Slant Text Shape
     * 
     */
    @XmlEnumValue("textSlantDown")
    TEXT_SLANT_DOWN("textSlantDown"),

    /**
     * Upward Cascade Text Shape
     * 
     */
    @XmlEnumValue("textCascadeUp")
    TEXT_CASCADE_UP("textCascadeUp"),

    /**
     * Downward Cascade Text Shape
     * 
     */
    @XmlEnumValue("textCascadeDown")
    TEXT_CASCADE_DOWN("textCascadeDown");
    private final String value;

    STTextShapeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTextShapeType fromValue(String v) {
        for (STTextShapeType c: STTextShapeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
