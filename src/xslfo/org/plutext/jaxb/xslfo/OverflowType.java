
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for overflow_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="overflow_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="visible"/>
 *     &lt;enumeration value="hidden"/>
 *     &lt;enumeration value="scroll"/>
 *     &lt;enumeration value="error-if-overflow"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "overflow_Type")
@XmlEnum
public enum OverflowType {

    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("visible")
    VISIBLE("visible"),
    @XmlEnumValue("hidden")
    HIDDEN("hidden"),
    @XmlEnumValue("scroll")
    SCROLL("scroll"),
    @XmlEnumValue("error-if-overflow")
    ERROR_IF_OVERFLOW("error-if-overflow"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    OverflowType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static OverflowType fromValue(String v) {
        for (OverflowType c: OverflowType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
