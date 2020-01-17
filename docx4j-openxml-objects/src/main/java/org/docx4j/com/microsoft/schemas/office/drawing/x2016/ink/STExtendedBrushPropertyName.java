
package org.docx4j.com.microsoft.schemas.office.drawing.x2016.ink;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ExtendedBrushPropertyName.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ExtendedBrushPropertyName"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="inkEffects"/&gt;
 *     &lt;enumeration value="anchorX"/&gt;
 *     &lt;enumeration value="anchorY"/&gt;
 *     &lt;enumeration value="scaleFactor"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_ExtendedBrushPropertyName", namespace = "http://schemas.microsoft.com/office/drawing/2016/ink")
@XmlEnum
public enum STExtendedBrushPropertyName {

    @XmlEnumValue("inkEffects")
    INK_EFFECTS("inkEffects"),
    @XmlEnumValue("anchorX")
    ANCHOR_X("anchorX"),
    @XmlEnumValue("anchorY")
    ANCHOR_Y("anchorY"),
    @XmlEnumValue("scaleFactor")
    SCALE_FACTOR("scaleFactor");
    private final String value;

    STExtendedBrushPropertyName(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STExtendedBrushPropertyName fromValue(String v) {
        for (STExtendedBrushPropertyName c: STExtendedBrushPropertyName.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
