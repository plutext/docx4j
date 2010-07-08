
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for speech_rate_base_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="speech_rate_base_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="x-slow"/>
 *     &lt;enumeration value="slow"/>
 *     &lt;enumeration value="medium"/>
 *     &lt;enumeration value="fast"/>
 *     &lt;enumeration value="x-fast"/>
 *     &lt;enumeration value="faster"/>
 *     &lt;enumeration value="slower"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "speech_rate_base_Type")
@XmlEnum
public enum SpeechRateBaseType {

    @XmlEnumValue("x-slow")
    X_SLOW("x-slow"),
    @XmlEnumValue("slow")
    SLOW("slow"),
    @XmlEnumValue("medium")
    MEDIUM("medium"),
    @XmlEnumValue("fast")
    FAST("fast"),
    @XmlEnumValue("x-fast")
    X_FAST("x-fast"),
    @XmlEnumValue("faster")
    FASTER("faster"),
    @XmlEnumValue("slower")
    SLOWER("slower"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    SpeechRateBaseType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SpeechRateBaseType fromValue(String v) {
        for (SpeechRateBaseType c: SpeechRateBaseType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
