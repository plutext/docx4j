
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for linefeed_treatment_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="linefeed_treatment_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="ignore"/>
 *     &lt;enumeration value="preserve"/>
 *     &lt;enumeration value="treat-as-space"/>
 *     &lt;enumeration value="treat-as-zero-width-space"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "linefeed_treatment_Type")
@XmlEnum
public enum LinefeedTreatmentType {

    @XmlEnumValue("ignore")
    IGNORE("ignore"),
    @XmlEnumValue("preserve")
    PRESERVE("preserve"),
    @XmlEnumValue("treat-as-space")
    TREAT_AS_SPACE("treat-as-space"),
    @XmlEnumValue("treat-as-zero-width-space")
    TREAT_AS_ZERO_WIDTH_SPACE("treat-as-zero-width-space"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    LinefeedTreatmentType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LinefeedTreatmentType fromValue(String v) {
        for (LinefeedTreatmentType c: LinefeedTreatmentType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
