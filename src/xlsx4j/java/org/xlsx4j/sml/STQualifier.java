
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_Qualifier.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Qualifier">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="doubleQuote"/>
 *     &lt;enumeration value="singleQuote"/>
 *     &lt;enumeration value="none"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_Qualifier")
@XmlEnum
public enum STQualifier {

    @XmlEnumValue("doubleQuote")
    DOUBLE_QUOTE("doubleQuote"),
    @XmlEnumValue("singleQuote")
    SINGLE_QUOTE("singleQuote"),
    @XmlEnumValue("none")
    NONE("none");
    private final String value;

    STQualifier(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STQualifier fromValue(String v) {
        for (STQualifier c: STQualifier.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
