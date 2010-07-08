
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for display_align_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="display_align_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="before"/>
 *     &lt;enumeration value="center"/>
 *     &lt;enumeration value="after"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "display_align_Type")
@XmlEnum
public enum DisplayAlignType {

    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("before")
    BEFORE("before"),
    @XmlEnumValue("center")
    CENTER("center"),
    @XmlEnumValue("after")
    AFTER("after"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    DisplayAlignType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DisplayAlignType fromValue(String v) {
        for (DisplayAlignType c: DisplayAlignType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
