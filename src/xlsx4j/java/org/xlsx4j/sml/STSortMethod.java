
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_SortMethod.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_SortMethod">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="stroke"/>
 *     &lt;enumeration value="pinYin"/>
 *     &lt;enumeration value="none"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_SortMethod")
@XmlEnum
public enum STSortMethod {

    @XmlEnumValue("stroke")
    STROKE("stroke"),
    @XmlEnumValue("pinYin")
    PIN_YIN("pinYin"),
    @XmlEnumValue("none")
    NONE("none");
    private final String value;

    STSortMethod(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STSortMethod fromValue(String v) {
        for (STSortMethod c: STSortMethod.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
