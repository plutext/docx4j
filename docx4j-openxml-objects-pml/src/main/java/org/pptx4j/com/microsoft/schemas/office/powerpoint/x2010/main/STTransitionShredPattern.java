
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x2010.main;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TransitionShredPattern.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TransitionShredPattern"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="strip"/&gt;
 *     &lt;enumeration value="rectangle"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_TransitionShredPattern")
@XmlEnum
public enum STTransitionShredPattern {

    @XmlEnumValue("strip")
    STRIP("strip"),
    @XmlEnumValue("rectangle")
    RECTANGLE("rectangle");
    private final String value;

    STTransitionShredPattern(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTransitionShredPattern fromValue(String v) {
        for (STTransitionShredPattern c: STTransitionShredPattern.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
