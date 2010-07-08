
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for border_collapse_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="border_collapse_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="collapse"/>
 *     &lt;enumeration value="separate"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "border_collapse_Type")
@XmlEnum
public enum BorderCollapseType {

    @XmlEnumValue("collapse")
    COLLAPSE("collapse"),
    @XmlEnumValue("separate")
    SEPARATE("separate"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    BorderCollapseType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BorderCollapseType fromValue(String v) {
        for (BorderCollapseType c: BorderCollapseType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
