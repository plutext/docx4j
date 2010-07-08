
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for width_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="width_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="thin"/>
 *     &lt;enumeration value="medium"/>
 *     &lt;enumeration value="thick"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "width_Type")
@XmlEnum
public enum WidthType {

    @XmlEnumValue("thin")
    THIN("thin"),
    @XmlEnumValue("medium")
    MEDIUM("medium"),
    @XmlEnumValue("thick")
    THICK("thick");
    private final String value;

    WidthType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static WidthType fromValue(String v) {
        for (WidthType c: WidthType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
