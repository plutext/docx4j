
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for page_break_after_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="page_break_after_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="always"/>
 *     &lt;enumeration value="avoid"/>
 *     &lt;enumeration value="left"/>
 *     &lt;enumeration value="right"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "page_break_after_Type")
@XmlEnum
public enum PageBreakAfterType {

    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("always")
    ALWAYS("always"),
    @XmlEnumValue("avoid")
    AVOID("avoid"),
    @XmlEnumValue("left")
    LEFT("left"),
    @XmlEnumValue("right")
    RIGHT("right"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    PageBreakAfterType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PageBreakAfterType fromValue(String v) {
        for (PageBreakAfterType c: PageBreakAfterType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
