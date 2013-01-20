
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_CellType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_CellType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="b"/>
 *     &lt;enumeration value="n"/>
 *     &lt;enumeration value="e"/>
 *     &lt;enumeration value="s"/>
 *     &lt;enumeration value="str"/>
 *     &lt;enumeration value="inlineStr"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_CellType")
@XmlEnum
public enum STCellType {

    @XmlEnumValue("b")
    B("b"),
    @XmlEnumValue("n")
    N("n"),
    @XmlEnumValue("e")
    E("e"),
    @XmlEnumValue("s")
    S("s"),
    @XmlEnumValue("str")
    STR("str"),
    @XmlEnumValue("inlineStr")
    INLINE_STR("inlineStr");
    private final String value;

    STCellType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STCellType fromValue(String v) {
        for (STCellType c: STCellType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
