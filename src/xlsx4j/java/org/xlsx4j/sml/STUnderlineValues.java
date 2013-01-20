
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_UnderlineValues.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_UnderlineValues">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="single"/>
 *     &lt;enumeration value="double"/>
 *     &lt;enumeration value="singleAccounting"/>
 *     &lt;enumeration value="doubleAccounting"/>
 *     &lt;enumeration value="none"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_UnderlineValues")
@XmlEnum
public enum STUnderlineValues {

    @XmlEnumValue("single")
    SINGLE("single"),
    @XmlEnumValue("double")
    DOUBLE("double"),
    @XmlEnumValue("singleAccounting")
    SINGLE_ACCOUNTING("singleAccounting"),
    @XmlEnumValue("doubleAccounting")
    DOUBLE_ACCOUNTING("doubleAccounting"),
    @XmlEnumValue("none")
    NONE("none");
    private final String value;

    STUnderlineValues(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STUnderlineValues fromValue(String v) {
        for (STUnderlineValues c: STUnderlineValues.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
