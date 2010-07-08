
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for leader_alignment_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="leader_alignment_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="reference-area"/>
 *     &lt;enumeration value="page"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "leader_alignment_Type")
@XmlEnum
public enum LeaderAlignmentType {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("reference-area")
    REFERENCE_AREA("reference-area"),
    @XmlEnumValue("page")
    PAGE("page"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    LeaderAlignmentType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LeaderAlignmentType fromValue(String v) {
        for (LeaderAlignmentType c: LeaderAlignmentType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
