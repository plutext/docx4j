
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_FieldSortType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_FieldSortType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="manual"/>
 *     &lt;enumeration value="ascending"/>
 *     &lt;enumeration value="descending"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_FieldSortType")
@XmlEnum
public enum STFieldSortType {

    @XmlEnumValue("manual")
    MANUAL("manual"),
    @XmlEnumValue("ascending")
    ASCENDING("ascending"),
    @XmlEnumValue("descending")
    DESCENDING("descending");
    private final String value;

    STFieldSortType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STFieldSortType fromValue(String v) {
        for (STFieldSortType c: STFieldSortType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
