
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for relative_position_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="relative_position_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="static"/>
 *     &lt;enumeration value="relative"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "relative_position_Type")
@XmlEnum
public enum RelativePositionType {

    @XmlEnumValue("static")
    STATIC("static"),
    @XmlEnumValue("relative")
    RELATIVE("relative"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    RelativePositionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RelativePositionType fromValue(String v) {
        for (RelativePositionType c: RelativePositionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
