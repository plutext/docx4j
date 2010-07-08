
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for starting_state_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="starting_state_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="show"/>
 *     &lt;enumeration value="hide"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "starting_state_Type")
@XmlEnum
public enum StartingStateType {

    @XmlEnumValue("show")
    SHOW("show"),
    @XmlEnumValue("hide")
    HIDE("hide");
    private final String value;

    StartingStateType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static StartingStateType fromValue(String v) {
        for (StartingStateType c: StartingStateType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
