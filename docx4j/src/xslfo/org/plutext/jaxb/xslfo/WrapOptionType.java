
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for wrap_option_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="wrap_option_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="no-wrap"/>
 *     &lt;enumeration value="wrap"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "wrap_option_Type")
@XmlEnum
public enum WrapOptionType {

    @XmlEnumValue("no-wrap")
    NO_WRAP("no-wrap"),
    @XmlEnumValue("wrap")
    WRAP("wrap"),

    /**
     * 
     *             
     * <pre>
     * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;fop_fail xmlns="http://www.w3.org/2001/XMLSchema" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:jaxb="http://java.sun.com/xml/ns/jaxb" xmlns:xjc="http://java.sun.com/xml/ns/jaxb/xjc" xmlns:xs="http://www.w3.org/2001/XMLSchema"&gt;Unknown enumerated value&lt;/fop_fail&gt;
     * </pre>
     * 
     *           
     * 
     */
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    WrapOptionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static WrapOptionType fromValue(String v) {
        for (WrapOptionType c: WrapOptionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
