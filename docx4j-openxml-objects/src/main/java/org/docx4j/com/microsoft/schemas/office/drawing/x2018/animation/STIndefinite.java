
package org.docx4j.com.microsoft.schemas.office.drawing.x2018.animation;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_Indefinite.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Indefinite"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="indefinite"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_Indefinite")
@XmlEnum
public enum STIndefinite {

    @XmlEnumValue("indefinite")
    INDEFINITE("indefinite");
    private final String value;

    STIndefinite(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STIndefinite fromValue(String v) {
        for (STIndefinite c: STIndefinite.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
