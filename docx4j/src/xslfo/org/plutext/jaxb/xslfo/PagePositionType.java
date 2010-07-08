
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for page_position_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="page_position_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="first"/>
 *     &lt;enumeration value="last"/>
 *     &lt;enumeration value="rest"/>
 *     &lt;enumeration value="any"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "page_position_Type")
@XmlEnum
public enum PagePositionType {

    @XmlEnumValue("first")
    FIRST("first"),
    @XmlEnumValue("last")
    LAST("last"),
    @XmlEnumValue("rest")
    REST("rest"),
    @XmlEnumValue("any")
    ANY("any"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    PagePositionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PagePositionType fromValue(String v) {
        for (PagePositionType c: PagePositionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
