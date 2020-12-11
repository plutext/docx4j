
package org.docx4j.sharedtypes;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_ConformanceClass.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_ConformanceClass">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="strict"/>
 *     &lt;enumeration value="transitional"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_ConformanceClass", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes")
@XmlEnum
public enum STConformanceClass {


    /**
     * Office Open XML Strict
     * 
     */
    @XmlEnumValue("strict")
    STRICT("strict"),

    /**
     * Office Open XML Transitional
     * 
     */
    @XmlEnumValue("transitional")
    TRANSITIONAL("transitional");
    private final String value;

    STConformanceClass(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STConformanceClass fromValue(String v) {
        for (STConformanceClass c: STConformanceClass.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
