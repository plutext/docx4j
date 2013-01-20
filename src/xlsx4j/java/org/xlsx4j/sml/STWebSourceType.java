
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_WebSourceType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_WebSourceType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="sheet"/>
 *     &lt;enumeration value="printArea"/>
 *     &lt;enumeration value="autoFilter"/>
 *     &lt;enumeration value="range"/>
 *     &lt;enumeration value="chart"/>
 *     &lt;enumeration value="pivotTable"/>
 *     &lt;enumeration value="query"/>
 *     &lt;enumeration value="label"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_WebSourceType")
@XmlEnum
public enum STWebSourceType {

    @XmlEnumValue("sheet")
    SHEET("sheet"),
    @XmlEnumValue("printArea")
    PRINT_AREA("printArea"),
    @XmlEnumValue("autoFilter")
    AUTO_FILTER("autoFilter"),
    @XmlEnumValue("range")
    RANGE("range"),
    @XmlEnumValue("chart")
    CHART("chart"),
    @XmlEnumValue("pivotTable")
    PIVOT_TABLE("pivotTable"),
    @XmlEnumValue("query")
    QUERY("query"),
    @XmlEnumValue("label")
    LABEL("label");
    private final String value;

    STWebSourceType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STWebSourceType fromValue(String v) {
        for (STWebSourceType c: STWebSourceType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
