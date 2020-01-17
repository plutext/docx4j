
package org.pptx4j.com.microsoft.schemas.office.powerpoint.x201703.main;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_DisplayLocation.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_DisplayLocation"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="media"/&gt;
 *     &lt;enumeration value="slide"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_DisplayLocation")
@XmlEnum
public enum STDisplayLocation {

    @XmlEnumValue("media")
    MEDIA("media"),
    @XmlEnumValue("slide")
    SLIDE("slide");
    private final String value;

    STDisplayLocation(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STDisplayLocation fromValue(String v) {
        for (STDisplayLocation c: STDisplayLocation.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
