
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x201509.main;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_TransitionMorphOption.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_TransitionMorphOption"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="byObject"/&gt;
 *     &lt;enumeration value="byWord"/&gt;
 *     &lt;enumeration value="byChar"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_TransitionMorphOption")
@XmlEnum
public enum STTransitionMorphOption {

    @XmlEnumValue("byObject")
    BY_OBJECT("byObject"),
    @XmlEnumValue("byWord")
    BY_WORD("byWord"),
    @XmlEnumValue("byChar")
    BY_CHAR("byChar");
    private final String value;

    STTransitionMorphOption(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STTransitionMorphOption fromValue(String v) {
        for (STTransitionMorphOption c: STTransitionMorphOption.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
