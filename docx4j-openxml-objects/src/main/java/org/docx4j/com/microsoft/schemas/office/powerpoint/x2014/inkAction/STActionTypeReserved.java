
package org.docx4j.com.microsoft.schemas.office.powerpoint.x2014.inkAction;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ActionTypeReserved.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ActionTypeReserved"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="add"/&gt;
 *     &lt;enumeration value="remove"/&gt;
 *     &lt;enumeration value="transform"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_ActionTypeReserved")
@XmlEnum
public enum STActionTypeReserved {

    @XmlEnumValue("add")
    ADD("add"),
    @XmlEnumValue("remove")
    REMOVE("remove"),
    @XmlEnumValue("transform")
    TRANSFORM("transform");
    private final String value;

    STActionTypeReserved(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STActionTypeReserved fromValue(String v) {
        for (STActionTypeReserved c: STActionTypeReserved.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
