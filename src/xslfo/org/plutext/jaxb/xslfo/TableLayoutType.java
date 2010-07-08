
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for table_layout_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="table_layout_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="fixed"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "table_layout_Type")
@XmlEnum
public enum TableLayoutType {

    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("fixed")
    FIXED("fixed"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    TableLayoutType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TableLayoutType fromValue(String v) {
        for (TableLayoutType c: TableLayoutType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
