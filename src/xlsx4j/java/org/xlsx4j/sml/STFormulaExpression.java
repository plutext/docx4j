
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_FormulaExpression.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_FormulaExpression">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ref"/>
 *     &lt;enumeration value="refError"/>
 *     &lt;enumeration value="area"/>
 *     &lt;enumeration value="areaError"/>
 *     &lt;enumeration value="computedArea"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_FormulaExpression")
@XmlEnum
public enum STFormulaExpression {

    @XmlEnumValue("ref")
    REF("ref"),
    @XmlEnumValue("refError")
    REF_ERROR("refError"),
    @XmlEnumValue("area")
    AREA("area"),
    @XmlEnumValue("areaError")
    AREA_ERROR("areaError"),
    @XmlEnumValue("computedArea")
    COMPUTED_AREA("computedArea");
    private final String value;

    STFormulaExpression(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STFormulaExpression fromValue(String v) {
        for (STFormulaExpression c: STFormulaExpression.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
