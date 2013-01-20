
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="all"/>
 *     &lt;enumeration value="row"/>
 *     &lt;enumeration value="column"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Type")
@XmlEnum
public enum STType {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("all")
    ALL("all"),
    @XmlEnumValue("row")
    ROW("row"),
    @XmlEnumValue("column")
    COLUMN("column");
    private final String value;

    STType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STType fromValue(String v) {
        for (STType c: STType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
