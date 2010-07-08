
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for relative_align_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="relative_align_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="before"/>
 *     &lt;enumeration value="baseline"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "relative_align_Type")
@XmlEnum
public enum RelativeAlignType {

    @XmlEnumValue("before")
    BEFORE("before"),
    @XmlEnumValue("baseline")
    BASELINE("baseline"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    RelativeAlignType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RelativeAlignType fromValue(String v) {
        for (RelativeAlignType c: RelativeAlignType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
