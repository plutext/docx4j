
package org.docx4j.com.microsoft.schemas.office.powerpoint.x2014.inkAction;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_PropertyNameReserved.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PropertyNameReserved"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="dataType"/&gt;
 *     &lt;enumeration value="style"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_PropertyNameReserved")
@XmlEnum
public enum STPropertyNameReserved {

    @XmlEnumValue("dataType")
    DATA_TYPE("dataType"),
    @XmlEnumValue("style")
    STYLE("style");
    private final String value;

    STPropertyNameReserved(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPropertyNameReserved fromValue(String v) {
        for (STPropertyNameReserved c: STPropertyNameReserved.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
