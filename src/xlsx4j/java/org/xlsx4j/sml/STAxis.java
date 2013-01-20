
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_Axis.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Axis">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="axisRow"/>
 *     &lt;enumeration value="axisCol"/>
 *     &lt;enumeration value="axisPage"/>
 *     &lt;enumeration value="axisValues"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Axis")
@XmlEnum
public enum STAxis {

    @XmlEnumValue("axisRow")
    AXIS_ROW("axisRow"),
    @XmlEnumValue("axisCol")
    AXIS_COL("axisCol"),
    @XmlEnumValue("axisPage")
    AXIS_PAGE("axisPage"),
    @XmlEnumValue("axisValues")
    AXIS_VALUES("axisValues");
    private final String value;

    STAxis(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STAxis fromValue(String v) {
        for (STAxis c: STAxis.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
