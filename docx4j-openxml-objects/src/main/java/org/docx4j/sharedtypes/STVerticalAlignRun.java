
package org.docx4j.sharedtypes;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_VerticalAlignRun.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_VerticalAlignRun">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="baseline"/>
 *     &lt;enumeration value="superscript"/>
 *     &lt;enumeration value="subscript"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_VerticalAlignRun", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes")
@XmlEnum
public enum STVerticalAlignRun {


    /**
     * Regular Vertical Positioning
     * 
     */
    @XmlEnumValue("baseline")
    BASELINE("baseline"),

    /**
     * Superscript
     * 
     */
    @XmlEnumValue("superscript")
    SUPERSCRIPT("superscript"),

    /**
     * Subscript
     * 
     */
    @XmlEnumValue("subscript")
    SUBSCRIPT("subscript");
    private final String value;

    STVerticalAlignRun(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STVerticalAlignRun fromValue(String v) {
        for (STVerticalAlignRun c: STVerticalAlignRun.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
