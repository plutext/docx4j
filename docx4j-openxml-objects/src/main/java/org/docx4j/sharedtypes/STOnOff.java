
package org.docx4j.sharedtypes;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_OnOff.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_OnOff">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="true"/>
 *     &lt;enumeration value="false"/>
 *     &lt;enumeration value="on"/>
 *     &lt;enumeration value="off"/>
 *     &lt;enumeration value="0"/>
 *     &lt;enumeration value="1"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 * @since 3.0.0
 */
@XmlType(name = "ST_OnOff", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes")
@XmlEnum
public enum STOnOff {


    /**
     * True
     * 
     */
    @XmlEnumValue("true")
    TRUE("true"),

    /**
     * False
     * 
     */
    @XmlEnumValue("false")
    FALSE("false"),

    /**
     * True
     * 
     */
    @XmlEnumValue("on")
    ON("on"),

    /**
     * False
     * 
     */
    @XmlEnumValue("off")
    OFF("off"),

    /**
     * False
     * 
     */
    @XmlEnumValue("0")
    ZERO("0"),

    /**
     * True
     * 
     */
    @XmlEnumValue("1")
    ONE("1");
    private final String value;

    STOnOff(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STOnOff fromValue(String v) {
        for (STOnOff c: STOnOff.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
