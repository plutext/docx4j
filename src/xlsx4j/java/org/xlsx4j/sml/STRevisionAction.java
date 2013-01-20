
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_RevisionAction.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_RevisionAction">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="add"/>
 *     &lt;enumeration value="delete"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_RevisionAction")
@XmlEnum
public enum STRevisionAction {

    @XmlEnumValue("add")
    ADD("add"),
    @XmlEnumValue("delete")
    DELETE("delete");
    private final String value;

    STRevisionAction(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STRevisionAction fromValue(String v) {
        for (STRevisionAction c: STRevisionAction.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
