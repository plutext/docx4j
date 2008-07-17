
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_PathFillMode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PathFillMode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="norm"/>
 *     &lt;enumeration value="lighten"/>
 *     &lt;enumeration value="lightenLess"/>
 *     &lt;enumeration value="darken"/>
 *     &lt;enumeration value="darkenLess"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_PathFillMode")
@XmlEnum
public enum STPathFillMode {


    /**
     * No Path Fill
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Normal Path Fill
     * 
     */
    @XmlEnumValue("norm")
    NORM("norm"),

    /**
     * Lighten Path Fill
     * 
     */
    @XmlEnumValue("lighten")
    LIGHTEN("lighten"),

    /**
     * Lighten Path Fill Less
     * 
     */
    @XmlEnumValue("lightenLess")
    LIGHTEN_LESS("lightenLess"),

    /**
     * Darken Path Fill
     * 
     */
    @XmlEnumValue("darken")
    DARKEN("darken"),

    /**
     * Darken Path Fill Less
     * 
     */
    @XmlEnumValue("darkenLess")
    DARKEN_LESS("darkenLess");
    private final String value;

    STPathFillMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPathFillMode fromValue(String v) {
        for (STPathFillMode c: STPathFillMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
