
package org.docx4j.com.microsoft.schemas.office.drawing.x2012.chartStyle;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_StyleEntryModifierEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_StyleEntryModifierEnum"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="allowNoFillOverride"/&gt;
 *     &lt;enumeration value="allowNoLineOverride"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_StyleEntryModifierEnum")
@XmlEnum
public enum STStyleEntryModifierEnum {

    @XmlEnumValue("allowNoFillOverride")
    ALLOW_NO_FILL_OVERRIDE("allowNoFillOverride"),
    @XmlEnumValue("allowNoLineOverride")
    ALLOW_NO_LINE_OVERRIDE("allowNoLineOverride");
    private final String value;

    STStyleEntryModifierEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STStyleEntryModifierEnum fromValue(String v) {
        for (STStyleEntryModifierEnum c: STStyleEntryModifierEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
