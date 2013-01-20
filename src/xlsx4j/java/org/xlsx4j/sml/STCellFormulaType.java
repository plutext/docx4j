
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_CellFormulaType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_CellFormulaType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="normal"/>
 *     &lt;enumeration value="array"/>
 *     &lt;enumeration value="dataTable"/>
 *     &lt;enumeration value="shared"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_CellFormulaType")
@XmlEnum
public enum STCellFormulaType {

    @XmlEnumValue("normal")
    NORMAL("normal"),
    @XmlEnumValue("array")
    ARRAY("array"),
    @XmlEnumValue("dataTable")
    DATA_TABLE("dataTable"),
    @XmlEnumValue("shared")
    SHARED("shared");
    private final String value;

    STCellFormulaType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STCellFormulaType fromValue(String v) {
        for (STCellFormulaType c: STCellFormulaType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
