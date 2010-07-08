
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for background_repeat_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="background_repeat_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="repeat"/>
 *     &lt;enumeration value="repeat-x"/>
 *     &lt;enumeration value="repeat-y"/>
 *     &lt;enumeration value="no-repeat"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "background_repeat_Type")
@XmlEnum
public enum BackgroundRepeatType {

    @XmlEnumValue("repeat")
    REPEAT("repeat"),
    @XmlEnumValue("repeat-x")
    REPEAT_X("repeat-x"),
    @XmlEnumValue("repeat-y")
    REPEAT_Y("repeat-y"),
    @XmlEnumValue("no-repeat")
    NO_REPEAT("no-repeat"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    BackgroundRepeatType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BackgroundRepeatType fromValue(String v) {
        for (BackgroundRepeatType c: BackgroundRepeatType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
