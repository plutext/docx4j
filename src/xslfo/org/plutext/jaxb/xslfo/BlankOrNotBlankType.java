
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for blank_or_not_blank_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="blank_or_not_blank_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="blank"/>
 *     &lt;enumeration value="not-blank"/>
 *     &lt;enumeration value="any"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "blank_or_not_blank_Type")
@XmlEnum
public enum BlankOrNotBlankType {

    @XmlEnumValue("blank")
    BLANK("blank"),
    @XmlEnumValue("not-blank")
    NOT_BLANK("not-blank"),
    @XmlEnumValue("any")
    ANY("any"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    BlankOrNotBlankType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BlankOrNotBlankType fromValue(String v) {
        for (BlankOrNotBlankType c: BlankOrNotBlankType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
