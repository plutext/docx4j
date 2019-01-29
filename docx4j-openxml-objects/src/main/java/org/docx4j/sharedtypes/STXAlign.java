
package org.docx4j.sharedtypes;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_XAlign.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_XAlign">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="left"/>
 *     &lt;enumeration value="center"/>
 *     &lt;enumeration value="right"/>
 *     &lt;enumeration value="inside"/>
 *     &lt;enumeration value="outside"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_XAlign", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes")
@XmlEnum
public enum STXAlign {


    /**
     * Left Aligned Horizontally
     * 
     */
    @XmlEnumValue("left")
    LEFT("left"),

    /**
     * Centered Horizontally
     * 
     */
    @XmlEnumValue("center")
    CENTER("center"),

    /**
     * Right Aligned Horizontally
     * 
     */
    @XmlEnumValue("right")
    RIGHT("right"),

    /**
     * Inside
     * 
     */
    @XmlEnumValue("inside")
    INSIDE("inside"),

    /**
     * Outside
     * 
     */
    @XmlEnumValue("outside")
    OUTSIDE("outside");
    private final String value;

    STXAlign(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STXAlign fromValue(String v) {
        for (STXAlign c: STXAlign.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
