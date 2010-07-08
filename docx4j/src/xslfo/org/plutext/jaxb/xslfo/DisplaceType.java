
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for displace_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="displace_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="inherit"/>
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="line"/>
 *     &lt;enumeration value="indent"/>
 *     &lt;enumeration value="block"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "displace_Type")
@XmlEnum
public enum DisplaceType {

    @XmlEnumValue("inherit")
    INHERIT("inherit"),
    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("line")
    LINE("line"),
    @XmlEnumValue("indent")
    INDENT("indent"),
    @XmlEnumValue("block")
    BLOCK("block");
    private final String value;

    DisplaceType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DisplaceType fromValue(String v) {
        for (DisplaceType c: DisplaceType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
