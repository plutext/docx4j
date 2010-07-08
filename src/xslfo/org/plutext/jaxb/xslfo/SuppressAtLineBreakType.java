
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for suppress_at_line_break_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="suppress_at_line_break_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="suppress"/>
 *     &lt;enumeration value="retain"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "suppress_at_line_break_Type")
@XmlEnum
public enum SuppressAtLineBreakType {

    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("suppress")
    SUPPRESS("suppress"),
    @XmlEnumValue("retain")
    RETAIN("retain"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    SuppressAtLineBreakType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SuppressAtLineBreakType fromValue(String v) {
        for (SuppressAtLineBreakType c: SuppressAtLineBreakType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
