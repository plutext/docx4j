
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_LineEndType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_LineEndType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="triangle"/>
 *     &lt;enumeration value="stealth"/>
 *     &lt;enumeration value="diamond"/>
 *     &lt;enumeration value="oval"/>
 *     &lt;enumeration value="arrow"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_LineEndType")
@XmlEnum
public enum STLineEndType {


    /**
     * None
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * Triangle Arrow Head
     * 
     */
    @XmlEnumValue("triangle")
    TRIANGLE("triangle"),

    /**
     * Stealth Arrow
     * 
     */
    @XmlEnumValue("stealth")
    STEALTH("stealth"),

    /**
     * Diamond
     * 
     */
    @XmlEnumValue("diamond")
    DIAMOND("diamond"),

    /**
     * Oval
     * 
     */
    @XmlEnumValue("oval")
    OVAL("oval"),

    /**
     * Arrow Head
     * 
     */
    @XmlEnumValue("arrow")
    ARROW("arrow");
    private final String value;

    STLineEndType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STLineEndType fromValue(String v) {
        for (STLineEndType c: STLineEndType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
