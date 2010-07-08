
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for border_style_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="border_style_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="inherit"/>
 *     &lt;enumeration value="none"/>
 *     &lt;enumeration value="hidden"/>
 *     &lt;enumeration value="dotted"/>
 *     &lt;enumeration value="dashed"/>
 *     &lt;enumeration value="solid"/>
 *     &lt;enumeration value="double"/>
 *     &lt;enumeration value="groove"/>
 *     &lt;enumeration value="ridge"/>
 *     &lt;enumeration value="inset"/>
 *     &lt;enumeration value="outset"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "border_style_Type")
@XmlEnum
public enum BorderStyleType {


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
    INHERIT("inherit"),
    @XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("hidden")
    HIDDEN("hidden"),
    @XmlEnumValue("dotted")
    DOTTED("dotted"),
    @XmlEnumValue("dashed")
    DASHED("dashed"),
    @XmlEnumValue("solid")
    SOLID("solid"),
    @XmlEnumValue("double")
    DOUBLE("double"),
    @XmlEnumValue("groove")
    GROOVE("groove"),
    @XmlEnumValue("ridge")
    RIDGE("ridge"),
    @XmlEnumValue("inset")
    INSET("inset"),
    @XmlEnumValue("outset")
    OUTSET("outset");
    private final String value;

    BorderStyleType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BorderStyleType fromValue(String v) {
        for (BorderStyleType c: BorderStyleType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
