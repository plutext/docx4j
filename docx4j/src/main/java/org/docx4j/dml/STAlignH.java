
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_AlignH.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_AlignH">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="left"/>
 *     &lt;enumeration value="right"/>
 *     &lt;enumeration value="center"/>
 *     &lt;enumeration value="inside"/>
 *     &lt;enumeration value="outside"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_AlignH", namespace = "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing")
@XmlEnum
public enum STAlignH {


    /**
     * Left
     * 
     */
    @XmlEnumValue("left")
    LEFT("left"),

    /**
     * Right Side
     * 
     */
    @XmlEnumValue("right")
    RIGHT("right"),

    /**
     * Center
     * 
     */
    @XmlEnumValue("center")
    CENTER("center"),

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

    STAlignH(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STAlignH fromValue(String v) {
        for (STAlignH c: STAlignH.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
