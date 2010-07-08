
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for alignment_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="alignment_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="baseline"/>
 *     &lt;enumeration value="before-edge"/>
 *     &lt;enumeration value="text-before-edge"/>
 *     &lt;enumeration value="middle"/>
 *     &lt;enumeration value="central"/>
 *     &lt;enumeration value="after-edge"/>
 *     &lt;enumeration value="text-after-edge"/>
 *     &lt;enumeration value="ideographic"/>
 *     &lt;enumeration value="alphabetic"/>
 *     &lt;enumeration value="hanging"/>
 *     &lt;enumeration value="mathematical"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "alignment_Type")
@XmlEnum
public enum AlignmentType {

    @XmlEnumValue("baseline")
    BASELINE("baseline"),
    @XmlEnumValue("before-edge")
    BEFORE_EDGE("before-edge"),
    @XmlEnumValue("text-before-edge")
    TEXT_BEFORE_EDGE("text-before-edge"),
    @XmlEnumValue("middle")
    MIDDLE("middle"),
    @XmlEnumValue("central")
    CENTRAL("central"),
    @XmlEnumValue("after-edge")
    AFTER_EDGE("after-edge"),
    @XmlEnumValue("text-after-edge")
    TEXT_AFTER_EDGE("text-after-edge"),
    @XmlEnumValue("ideographic")
    IDEOGRAPHIC("ideographic"),
    @XmlEnumValue("alphabetic")
    ALPHABETIC("alphabetic"),
    @XmlEnumValue("hanging")
    HANGING("hanging"),
    @XmlEnumValue("mathematical")
    MATHEMATICAL("mathematical");
    private final String value;

    AlignmentType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AlignmentType fromValue(String v) {
        for (AlignmentType c: AlignmentType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
