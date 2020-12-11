
package org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ColorStyleMethodEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ColorStyleMethodEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="cycle"/&gt;
 *     &lt;enumeration value="withinLinear"/&gt;
 *     &lt;enumeration value="acrossLinear"/&gt;
 *     &lt;enumeration value="withinLinearReversed"/&gt;
 *     &lt;enumeration value="acrossLinearReversed"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_ColorStyleMethodEnum")
@XmlEnum
public enum STColorStyleMethodEnum {

    @XmlEnumValue("cycle")
    CYCLE("cycle"),
    @XmlEnumValue("withinLinear")
    WITHIN_LINEAR("withinLinear"),
    @XmlEnumValue("acrossLinear")
    ACROSS_LINEAR("acrossLinear"),
    @XmlEnumValue("withinLinearReversed")
    WITHIN_LINEAR_REVERSED("withinLinearReversed"),
    @XmlEnumValue("acrossLinearReversed")
    ACROSS_LINEAR_REVERSED("acrossLinearReversed");
    private final String value;

    STColorStyleMethodEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STColorStyleMethodEnum fromValue(String v) {
        for (STColorStyleMethodEnum c: STColorStyleMethodEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
