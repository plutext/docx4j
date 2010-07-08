
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for active_state_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="active_state_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="link"/>
 *     &lt;enumeration value="visited"/>
 *     &lt;enumeration value="active"/>
 *     &lt;enumeration value="hover"/>
 *     &lt;enumeration value="focus"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "active_state_Type")
@XmlEnum
public enum ActiveStateType {

    @XmlEnumValue("link")
    LINK("link"),
    @XmlEnumValue("visited")
    VISITED("visited"),
    @XmlEnumValue("active")
    ACTIVE("active"),
    @XmlEnumValue("hover")
    HOVER("hover"),
    @XmlEnumValue("focus")
    FOCUS("focus");
    private final String value;

    ActiveStateType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ActiveStateType fromValue(String v) {
        for (ActiveStateType c: ActiveStateType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
