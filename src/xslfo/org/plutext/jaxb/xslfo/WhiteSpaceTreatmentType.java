
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for white_space_treatment_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="white_space_treatment_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="ignore"/>
 *     &lt;enumeration value="preserve"/>
 *     &lt;enumeration value="ignore-if-before-linefeed"/>
 *     &lt;enumeration value="ignore-if-after-linefeed"/>
 *     &lt;enumeration value="ignore-if-surrounding-linefeed"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "white_space_treatment_Type")
@XmlEnum
public enum WhiteSpaceTreatmentType {

    @XmlEnumValue("ignore")
    IGNORE("ignore"),
    @XmlEnumValue("preserve")
    PRESERVE("preserve"),
    @XmlEnumValue("ignore-if-before-linefeed")
    IGNORE_IF_BEFORE_LINEFEED("ignore-if-before-linefeed"),
    @XmlEnumValue("ignore-if-after-linefeed")
    IGNORE_IF_AFTER_LINEFEED("ignore-if-after-linefeed"),
    @XmlEnumValue("ignore-if-surrounding-linefeed")
    IGNORE_IF_SURROUNDING_LINEFEED("ignore-if-surrounding-linefeed"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    WhiteSpaceTreatmentType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static WhiteSpaceTreatmentType fromValue(String v) {
        for (WhiteSpaceTreatmentType c: WhiteSpaceTreatmentType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
