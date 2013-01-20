
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_FormatAction.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_FormatAction">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="blank"/>
 *     &lt;enumeration value="formatting"/>
 *     &lt;enumeration value="drill"/>
 *     &lt;enumeration value="formula"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_FormatAction")
@XmlEnum
public enum STFormatAction {

    @XmlEnumValue("blank")
    BLANK("blank"),
    @XmlEnumValue("formatting")
    FORMATTING("formatting"),
    @XmlEnumValue("drill")
    DRILL("drill"),
    @XmlEnumValue("formula")
    FORMULA("formula");
    private final String value;

    STFormatAction(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STFormatAction fromValue(String v) {
        for (STFormatAction c: STFormatAction.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
