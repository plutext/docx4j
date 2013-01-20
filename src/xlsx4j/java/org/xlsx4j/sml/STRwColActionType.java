
package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_rwColActionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_rwColActionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="insertRow"/>
 *     &lt;enumeration value="deleteRow"/>
 *     &lt;enumeration value="insertCol"/>
 *     &lt;enumeration value="deleteCol"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_rwColActionType")
@XmlEnum
public enum STRwColActionType {

    @XmlEnumValue("insertRow")
    INSERT_ROW("insertRow"),
    @XmlEnumValue("deleteRow")
    DELETE_ROW("deleteRow"),
    @XmlEnumValue("insertCol")
    INSERT_COL("insertCol"),
    @XmlEnumValue("deleteCol")
    DELETE_COL("deleteCol");
    private final String value;

    STRwColActionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STRwColActionType fromValue(String v) {
        for (STRwColActionType c: STRwColActionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
