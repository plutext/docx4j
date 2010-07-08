
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for empty_cells_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="empty_cells_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="show"/>
 *     &lt;enumeration value="hide"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "empty_cells_Type")
@XmlEnum
public enum EmptyCellsType {

    @XmlEnumValue("show")
    SHOW("show"),
    @XmlEnumValue("hide")
    HIDE("hide"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    EmptyCellsType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EmptyCellsType fromValue(String v) {
        for (EmptyCellsType c: EmptyCellsType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
