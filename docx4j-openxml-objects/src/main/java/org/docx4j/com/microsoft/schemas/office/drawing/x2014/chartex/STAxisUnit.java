
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_AxisUnit.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_AxisUnit"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="hundreds"/&gt;
 *     &lt;enumeration value="thousands"/&gt;
 *     &lt;enumeration value="tenThousands"/&gt;
 *     &lt;enumeration value="hundredThousands"/&gt;
 *     &lt;enumeration value="millions"/&gt;
 *     &lt;enumeration value="tenMillions"/&gt;
 *     &lt;enumeration value="hundredMillions"/&gt;
 *     &lt;enumeration value="billions"/&gt;
 *     &lt;enumeration value="trillions"/&gt;
 *     &lt;enumeration value="percentage"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_AxisUnit")
@XmlEnum
public enum STAxisUnit {

    @XmlEnumValue("hundreds")
    HUNDREDS("hundreds"),
    @XmlEnumValue("thousands")
    THOUSANDS("thousands"),
    @XmlEnumValue("tenThousands")
    TEN_THOUSANDS("tenThousands"),
    @XmlEnumValue("hundredThousands")
    HUNDRED_THOUSANDS("hundredThousands"),
    @XmlEnumValue("millions")
    MILLIONS("millions"),
    @XmlEnumValue("tenMillions")
    TEN_MILLIONS("tenMillions"),
    @XmlEnumValue("hundredMillions")
    HUNDRED_MILLIONS("hundredMillions"),
    @XmlEnumValue("billions")
    BILLIONS("billions"),
    @XmlEnumValue("trillions")
    TRILLIONS("trillions"),
    @XmlEnumValue("percentage")
    PERCENTAGE("percentage");
    private final String value;

    STAxisUnit(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STAxisUnit fromValue(String v) {
        for (STAxisUnit c: STAxisUnit.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
