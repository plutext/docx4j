
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_CalcMode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_CalcMode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="manual"/>
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="autoNoTable"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_CalcMode")
@XmlEnum
public enum STCalcMode {

    @XmlEnumValue("manual")
    MANUAL("manual"),
    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("autoNoTable")
    AUTO_NO_TABLE("autoNoTable");
    private final String value;

    STCalcMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STCalcMode fromValue(String v) {
        for (STCalcMode c: STCalcMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
