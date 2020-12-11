
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2010.main;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TransitionPattern.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TransitionPattern"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="diamond"/&gt;
 *     &lt;enumeration value="hexagon"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_TransitionPattern")
@XmlEnum
public enum STTransitionPattern {

    @XmlEnumValue("diamond")
    DIAMOND("diamond"),
    @XmlEnumValue("hexagon")
    HEXAGON("hexagon");
    private final String value;

    STTransitionPattern(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTransitionPattern fromValue(String v) {
        for (STTransitionPattern c: STTransitionPattern.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
