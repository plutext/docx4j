
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for line_height_shift_adjustment_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="line_height_shift_adjustment_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="consider-shifts"/>
 *     &lt;enumeration value="disregard-shifts"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "line_height_shift_adjustment_Type")
@XmlEnum
public enum LineHeightShiftAdjustmentType {

    @XmlEnumValue("consider-shifts")
    CONSIDER_SHIFTS("consider-shifts"),
    @XmlEnumValue("disregard-shifts")
    DISREGARD_SHIFTS("disregard-shifts"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    LineHeightShiftAdjustmentType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LineHeightShiftAdjustmentType fromValue(String v) {
        for (LineHeightShiftAdjustmentType c: LineHeightShiftAdjustmentType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
