
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for no_limit_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="no_limit_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="no-limit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "no_limit_Type")
@XmlEnum
public enum NoLimitType {

    @XmlEnumValue("no-limit")
    NO_LIMIT("no-limit");
    private final String value;

    NoLimitType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static NoLimitType fromValue(String v) {
        for (NoLimitType c: NoLimitType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
