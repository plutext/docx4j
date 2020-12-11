
package org.docx4j.com.microsoft.schemas.office.powerpoint.x2014.inkAction;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_DataNameReserved.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_DataNameReserved"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="stroke"/&gt;
 *     &lt;enumeration value="path"/&gt;
 *     &lt;enumeration value="target"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_DataNameReserved")
@XmlEnum
public enum STDataNameReserved {

    @XmlEnumValue("stroke")
    STROKE("stroke"),
    @XmlEnumValue("path")
    PATH("path"),
    @XmlEnumValue("target")
    TARGET("target");
    private final String value;

    STDataNameReserved(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STDataNameReserved fromValue(String v) {
        for (STDataNameReserved c: STDataNameReserved.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
