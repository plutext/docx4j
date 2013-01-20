
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_Objects.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Objects">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="all"/>
 *     &lt;enumeration value="placeholders"/>
 *     &lt;enumeration value="none"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Objects")
@XmlEnum
public enum STObjects {

    @XmlEnumValue("all")
    ALL("all"),
    @XmlEnumValue("placeholders")
    PLACEHOLDERS("placeholders"),
    @XmlEnumValue("none")
    NONE("none");
    private final String value;

    STObjects(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STObjects fromValue(String v) {
        for (STObjects c: STObjects.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
