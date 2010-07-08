
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for writing_mode_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="writing_mode_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="lr-tb"/>
 *     &lt;enumeration value="rl-tb"/>
 *     &lt;enumeration value="tb-rl"/>
 *     &lt;enumeration value="lr"/>
 *     &lt;enumeration value="rl"/>
 *     &lt;enumeration value="tb"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "writing_mode_Type")
@XmlEnum
public enum WritingModeType {

    @XmlEnumValue("lr-tb")
    LR_TB("lr-tb"),
    @XmlEnumValue("rl-tb")
    RL_TB("rl-tb"),
    @XmlEnumValue("tb-rl")
    TB_RL("tb-rl"),
    @XmlEnumValue("lr")
    LR("lr"),
    @XmlEnumValue("rl")
    RL("rl"),
    @XmlEnumValue("tb")
    TB("tb"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    WritingModeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static WritingModeType fromValue(String v) {
        for (WritingModeType c: WritingModeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
