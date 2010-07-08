
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for relative_size_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="relative_size_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="larger"/>
 *     &lt;enumeration value="smaller"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "relative_size_Type")
@XmlEnum
public enum RelativeSizeType {

    @XmlEnumValue("larger")
    LARGER("larger"),
    @XmlEnumValue("smaller")
    SMALLER("smaller");
    private final String value;

    RelativeSizeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RelativeSizeType fromValue(String v) {
        for (RelativeSizeType c: RelativeSizeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
