
package org.docx4j.wml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_Direction.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Direction"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="ltr"/&gt;
 *     &lt;enumeration value="rtl"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_Direction")
@XmlEnum
public enum STDirection {

    @XmlEnumValue("ltr")
    LTR("ltr"),
    @XmlEnumValue("rtl")
    RTL("rtl");
    private final String value;

    STDirection(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STDirection fromValue(String v) {
        for (STDirection c: STDirection.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
