
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_AlignV.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_AlignV">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="top"/>
 *     &lt;enumeration value="bottom"/>
 *     &lt;enumeration value="center"/>
 *     &lt;enumeration value="inside"/>
 *     &lt;enumeration value="outside"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_AlignV", namespace = "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing")
@XmlEnum
public enum STAlignV {


    /**
     * Top
     * 
     */
    @XmlEnumValue("top")
    TOP("top"),

    /**
     * Bottom
     * 
     */
    @XmlEnumValue("bottom")
    BOTTOM("bottom"),

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

    STAlignV(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STAlignV fromValue(String v) {
        for (STAlignV c: STAlignV.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
