
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2010.main;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TransitionCenterDirectionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TransitionCenterDirectionType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="center"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_TransitionCenterDirectionType")
@XmlEnum
public enum STTransitionCenterDirectionType {

    @XmlEnumValue("center")
    CENTER("center");
    private final String value;

    STTransitionCenterDirectionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTransitionCenterDirectionType fromValue(String v) {
        for (STTransitionCenterDirectionType c: STTransitionCenterDirectionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
