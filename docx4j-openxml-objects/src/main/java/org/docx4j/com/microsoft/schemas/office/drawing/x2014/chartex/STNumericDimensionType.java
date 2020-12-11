
package org.docx4j.com.microsoft.schemas.office.drawing.x2014.chartex;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_NumericDimensionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_NumericDimensionType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="val"/&gt;
 *     &lt;enumeration value="x"/&gt;
 *     &lt;enumeration value="y"/&gt;
 *     &lt;enumeration value="size"/&gt;
 *     &lt;enumeration value="colorVal"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_NumericDimensionType")
@XmlEnum
public enum STNumericDimensionType {

    @XmlEnumValue("val")
    VAL("val"),
    @XmlEnumValue("x")
    X("x"),
    @XmlEnumValue("y")
    Y("y"),
    @XmlEnumValue("size")
    SIZE("size"),
    @XmlEnumValue("colorVal")
    COLOR_VAL("colorVal");
    private final String value;

    STNumericDimensionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STNumericDimensionType fromValue(String v) {
        for (STNumericDimensionType c: STNumericDimensionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
