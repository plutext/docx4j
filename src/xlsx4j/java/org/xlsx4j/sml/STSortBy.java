
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_SortBy.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_SortBy">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="value"/>
 *     &lt;enumeration value="cellColor"/>
 *     &lt;enumeration value="fontColor"/>
 *     &lt;enumeration value="icon"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_SortBy")
@XmlEnum
public enum STSortBy {

    @XmlEnumValue("value")
    VALUE("value"),
    @XmlEnumValue("cellColor")
    CELL_COLOR("cellColor"),
    @XmlEnumValue("fontColor")
    FONT_COLOR("fontColor"),
    @XmlEnumValue("icon")
    ICON("icon");
    private final String value;

    STSortBy(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STSortBy fromValue(String v) {
        for (STSortBy c: STSortBy.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
