
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_FileType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_FileType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="mac"/>
 *     &lt;enumeration value="win"/>
 *     &lt;enumeration value="dos"/>
 *     &lt;enumeration value="lin"/>
 *     &lt;enumeration value="other"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_FileType")
@XmlEnum
public enum STFileType {

    @XmlEnumValue("mac")
    MAC("mac"),
    @XmlEnumValue("win")
    WIN("win"),
    @XmlEnumValue("dos")
    DOS("dos"),
    @XmlEnumValue("lin")
    LIN("lin"),
    @XmlEnumValue("other")
    OTHER("other");
    private final String value;

    STFileType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STFileType fromValue(String v) {
        for (STFileType c: STFileType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
