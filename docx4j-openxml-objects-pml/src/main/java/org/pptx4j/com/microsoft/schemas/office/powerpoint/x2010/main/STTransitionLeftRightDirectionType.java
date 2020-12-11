
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2010.main;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TransitionLeftRightDirectionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TransitionLeftRightDirectionType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="l"/&gt;
 *     &lt;enumeration value="r"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_TransitionLeftRightDirectionType")
@XmlEnum
public enum STTransitionLeftRightDirectionType {

    @XmlEnumValue("l")
    L("l"),
    @XmlEnumValue("r")
    R("r");
    private final String value;

    STTransitionLeftRightDirectionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTransitionLeftRightDirectionType fromValue(String v) {
        for (STTransitionLeftRightDirectionType c: STTransitionLeftRightDirectionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
