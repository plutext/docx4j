
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for font_style_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="font_style_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="normal"/>
 *     &lt;enumeration value="italic"/>
 *     &lt;enumeration value="oblique"/>
 *     &lt;enumeration value="backslant"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "font_style_Type")
@XmlEnum
public enum FontStyleType {

    @XmlEnumValue("normal")
    NORMAL("normal"),
    @XmlEnumValue("italic")
    ITALIC("italic"),
    @XmlEnumValue("oblique")
    OBLIQUE("oblique"),

    /**
     * 
     *             
     * <pre>
     * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;fop_fail xmlns="http://www.w3.org/2001/XMLSchema" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:jaxb="http://java.sun.com/xml/ns/jaxb" xmlns:xjc="http://java.sun.com/xml/ns/jaxb/xjc" xmlns:xs="http://www.w3.org/2001/XMLSchema"&gt;unknown font&lt;/fop_fail&gt;
     * </pre>
     * 
     *           
     * 
     */
    @XmlEnumValue("backslant")
    BACKSLANT("backslant"),

    /**
     * 
     *             
     * <pre>
     * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;fop_fail xmlns="http://www.w3.org/2001/XMLSchema" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:jaxb="http://java.sun.com/xml/ns/jaxb" xmlns:xjc="http://java.sun.com/xml/ns/jaxb/xjc" xmlns:xs="http://www.w3.org/2001/XMLSchema"&gt;unknown font&lt;/fop_fail&gt;
     * </pre>
     * 
     *           
     * 
     */
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    FontStyleType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FontStyleType fromValue(String v) {
        for (FontStyleType c: FontStyleType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
