
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_CellComments.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_CellComments">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="asDisplayed"/>
 *     &lt;enumeration value="atEnd"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_CellComments")
@XmlEnum
public enum STCellComments {

    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("asDisplayed")
    AS_DISPLAYED("asDisplayed"),
    @XmlEnumValue("atEnd")
    AT_END("atEnd");
    private final String value;

    STCellComments(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STCellComments fromValue(String v) {
        for (STCellComments c: STCellComments.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
