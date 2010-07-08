
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for box_alignment_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="box_alignment_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="top"/>
 *     &lt;enumeration value="text-top"/>
 *     &lt;enumeration value="bottom"/>
 *     &lt;enumeration value="text-bottom"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "box_alignment_Type")
@XmlEnum
public enum BoxAlignmentType {

    @XmlEnumValue("top")
    TOP("top"),
    @XmlEnumValue("text-top")
    TEXT_TOP("text-top"),
    @XmlEnumValue("bottom")
    BOTTOM("bottom"),
    @XmlEnumValue("text-bottom")
    TEXT_BOTTOM("text-bottom");
    private final String value;

    BoxAlignmentType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BoxAlignmentType fromValue(String v) {
        for (BoxAlignmentType c: BoxAlignmentType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
