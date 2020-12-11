
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_FormulaDirection.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_FormulaDirection"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="col"/&gt;
 *     &lt;enumeration value="row"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_FormulaDirection")
@XmlEnum
public enum STFormulaDirection {

    @XmlEnumValue("col")
    COL("col"),
    @XmlEnumValue("row")
    ROW("row");
    private final String value;

    STFormulaDirection(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STFormulaDirection fromValue(String v) {
        for (STFormulaDirection c: STFormulaDirection.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
