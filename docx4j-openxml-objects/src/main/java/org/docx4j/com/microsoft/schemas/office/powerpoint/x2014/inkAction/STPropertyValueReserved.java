
package org.docx4j.com.microsoft.schemas.office.powerpoint.x2014.inkAction;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_PropertyValueReserved.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PropertyValueReserved"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="ink"/&gt;
 *     &lt;enumeration value="pointEraser"/&gt;
 *     &lt;enumeration value="strokeEraser"/&gt;
 *     &lt;enumeration value="instant"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_PropertyValueReserved")
@XmlEnum
public enum STPropertyValueReserved {

    @XmlEnumValue("ink")
    INK("ink"),
    @XmlEnumValue("pointEraser")
    POINT_ERASER("pointEraser"),
    @XmlEnumValue("strokeEraser")
    STROKE_ERASER("strokeEraser"),
    @XmlEnumValue("instant")
    INSTANT("instant");
    private final String value;

    STPropertyValueReserved(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPropertyValueReserved fromValue(String v) {
        for (STPropertyValueReserved c: STPropertyValueReserved.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
