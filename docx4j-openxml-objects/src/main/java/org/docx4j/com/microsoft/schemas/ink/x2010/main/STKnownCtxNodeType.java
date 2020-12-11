
package org.docx4j.com.microsoft.schemas.ink.x2010.main;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_KnownCtxNodeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_KnownCtxNodeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="root"/&gt;
 *     &lt;enumeration value="unclassifiedInk"/&gt;
 *     &lt;enumeration value="writingRegion"/&gt;
 *     &lt;enumeration value="analysisHint"/&gt;
 *     &lt;enumeration value="object"/&gt;
 *     &lt;enumeration value="inkDrawing"/&gt;
 *     &lt;enumeration value="image"/&gt;
 *     &lt;enumeration value="paragraph"/&gt;
 *     &lt;enumeration value="line"/&gt;
 *     &lt;enumeration value="inkBullet"/&gt;
 *     &lt;enumeration value="inkWord"/&gt;
 *     &lt;enumeration value="textWord"/&gt;
 *     &lt;enumeration value="customRecognizer"/&gt;
 *     &lt;enumeration value="mathRegion"/&gt;
 *     &lt;enumeration value="mathEquation"/&gt;
 *     &lt;enumeration value="mathStruct"/&gt;
 *     &lt;enumeration value="mathSymbol"/&gt;
 *     &lt;enumeration value="mathIdentifier"/&gt;
 *     &lt;enumeration value="mathOperator"/&gt;
 *     &lt;enumeration value="mathNumber"/&gt;
 *     &lt;enumeration value="nonInkDrawing"/&gt;
 *     &lt;enumeration value="groupNode"/&gt;
 *     &lt;enumeration value="mixedDrawing"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_KnownCtxNodeType")
@XmlEnum
public enum STKnownCtxNodeType {

    @XmlEnumValue("root")
    ROOT("root"),
    @XmlEnumValue("unclassifiedInk")
    UNCLASSIFIED_INK("unclassifiedInk"),
    @XmlEnumValue("writingRegion")
    WRITING_REGION("writingRegion"),
    @XmlEnumValue("analysisHint")
    ANALYSIS_HINT("analysisHint"),
    @XmlEnumValue("object")
    OBJECT("object"),
    @XmlEnumValue("inkDrawing")
    INK_DRAWING("inkDrawing"),
    @XmlEnumValue("image")
    IMAGE("image"),
    @XmlEnumValue("paragraph")
    PARAGRAPH("paragraph"),
    @XmlEnumValue("line")
    LINE("line"),
    @XmlEnumValue("inkBullet")
    INK_BULLET("inkBullet"),
    @XmlEnumValue("inkWord")
    INK_WORD("inkWord"),
    @XmlEnumValue("textWord")
    TEXT_WORD("textWord"),
    @XmlEnumValue("customRecognizer")
    CUSTOM_RECOGNIZER("customRecognizer"),
    @XmlEnumValue("mathRegion")
    MATH_REGION("mathRegion"),
    @XmlEnumValue("mathEquation")
    MATH_EQUATION("mathEquation"),
    @XmlEnumValue("mathStruct")
    MATH_STRUCT("mathStruct"),
    @XmlEnumValue("mathSymbol")
    MATH_SYMBOL("mathSymbol"),
    @XmlEnumValue("mathIdentifier")
    MATH_IDENTIFIER("mathIdentifier"),
    @XmlEnumValue("mathOperator")
    MATH_OPERATOR("mathOperator"),
    @XmlEnumValue("mathNumber")
    MATH_NUMBER("mathNumber"),
    @XmlEnumValue("nonInkDrawing")
    NON_INK_DRAWING("nonInkDrawing"),
    @XmlEnumValue("groupNode")
    GROUP_NODE("groupNode"),
    @XmlEnumValue("mixedDrawing")
    MIXED_DRAWING("mixedDrawing");
    private final String value;

    STKnownCtxNodeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STKnownCtxNodeType fromValue(String v) {
        for (STKnownCtxNodeType c: STKnownCtxNodeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
