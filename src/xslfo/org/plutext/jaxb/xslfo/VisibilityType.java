
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for visibility_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="visibility_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="visible"/>
 *     &lt;enumeration value="hidden"/>
 *     &lt;enumeration value="collapse"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "visibility_Type")
@XmlEnum
public enum VisibilityType {

    @XmlEnumValue("visible")
    VISIBLE("visible"),
    @XmlEnumValue("hidden")
    HIDDEN("hidden"),
    @XmlEnumValue("collapse")
    COLLAPSE("collapse"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    VisibilityType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VisibilityType fromValue(String v) {
        for (VisibilityType c: VisibilityType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
