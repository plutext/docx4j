
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_Scope.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Scope">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="selection"/>
 *     &lt;enumeration value="data"/>
 *     &lt;enumeration value="field"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Scope")
@XmlEnum
public enum STScope {

    @XmlEnumValue("selection")
    SELECTION("selection"),
    @XmlEnumValue("data")
    DATA("data"),
    @XmlEnumValue("field")
    FIELD("field");
    private final String value;

    STScope(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STScope fromValue(String v) {
        for (STScope c: STScope.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
