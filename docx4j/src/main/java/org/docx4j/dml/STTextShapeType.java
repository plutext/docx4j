/*
 *  Copyright 2007, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is free software: you can use it, redistribute it and/or modify
    it under the terms of version 3 of the GNU Affero General Public License 
    as published by the Free Software Foundation.

    If you need the right to use it under a different license, please
    contact Plutext.

    docx4j is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License   
    along with docx4j.  If not, see <http://www.fsf.org/licensing/licenses/>.
    
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
 * &lt;simpleType name="ST_TextShapeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="textNoShape"/>
 *     &lt;enumeration value="textPlain"/>
 *     &lt;enumeration value="textStop"/>
 *     &lt;enumeration value="textTriangle"/>
 *     &lt;enumeration value="textTriangleInverted"/>
 *     &lt;enumeration value="textChevron"/>
 *     &lt;enumeration value="textChevronInverted"/>
 *     &lt;enumeration value="textRingInside"/>
 *     &lt;enumeration value="textRingOutside"/>
 *     &lt;enumeration value="textArchUp"/>
 *     &lt;enumeration value="textArchDown"/>
 *     &lt;enumeration value="textCircle"/>
 *     &lt;enumeration value="textButton"/>
 *     &lt;enumeration value="textArchUpPour"/>
 *     &lt;enumeration value="textArchDownPour"/>
 *     &lt;enumeration value="textCirclePour"/>
 *     &lt;enumeration value="textButtonPour"/>
 *     &lt;enumeration value="textCurveUp"/>
 *     &lt;enumeration value="textCurveDown"/>
 *     &lt;enumeration value="textCanUp"/>
 *     &lt;enumeration value="textCanDown"/>
 *     &lt;enumeration value="textWave1"/>
 *     &lt;enumeration value="textWave2"/>
 *     &lt;enumeration value="textDoubleWave1"/>
 *     &lt;enumeration value="textWave4"/>
 *     &lt;enumeration value="textInflate"/>
 *     &lt;enumeration value="textDeflate"/>
 *     &lt;enumeration value="textInflateBottom"/>
 *     &lt;enumeration value="textDeflateBottom"/>
 *     &lt;enumeration value="textInflateTop"/>
 *     &lt;enumeration value="textDeflateTop"/>
 *     &lt;enumeration value="textDeflateInflate"/>
 *     &lt;enumeration value="textDeflateInflateDeflate"/>
 *     &lt;enumeration value="textFadeRight"/>
 *     &lt;enumeration value="textFadeLeft"/>
 *     &lt;enumeration value="textFadeUp"/>
 *     &lt;enumeration value="textFadeDown"/>
 *     &lt;enumeration value="textSlantUp"/>
 *     &lt;enumeration value="textSlantDown"/>
 *     &lt;enumeration value="textCascadeUp"/>
 *     &lt;enumeration value="textCascadeDown"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
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
     * Inverted Triangle Text
     * 						Shape
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
     * Inverted Chevron Text
     * 						Shape
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
     * Upward Pour Arch Text
     * 						Shape
     * 
     */
    @XmlEnumValue("textArchUpPour")
    TEXT_ARCH_UP_POUR("textArchUpPour"),

    /**
     * Downward Pour Arch Text
     * 						Shape
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
     * Downward Curve Text
     * 						Shape
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
     * Bottom Inflate Text
     * 						Shape
     * 
     */
    @XmlEnumValue("textInflateBottom")
    TEXT_INFLATE_BOTTOM("textInflateBottom"),

    /**
     * Bottom Deflate Text
     * 						Shape
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
     * Deflate-Inflate Text
     * 						Shape
     * 
     */
    @XmlEnumValue("textDeflateInflate")
    TEXT_DEFLATE_INFLATE("textDeflateInflate"),

    /**
     * Deflate-Inflate-Deflate Text
     * 						Shape
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
     * Downward Slant Text
     * 						Shape
     * 
     */
    @XmlEnumValue("textSlantDown")
    TEXT_SLANT_DOWN("textSlantDown"),

    /**
     * Upward Cascade Text
     * 						Shape
     * 
     */
    @XmlEnumValue("textCascadeUp")
    TEXT_CASCADE_UP("textCascadeUp"),

    /**
     * Downward Cascade Text
     * 						Shape
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
