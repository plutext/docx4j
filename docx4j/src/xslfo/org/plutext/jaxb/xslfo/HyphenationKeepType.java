
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for hyphenation_keep_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="hyphenation_keep_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="column"/>
 *     &lt;enumeration value="page"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "hyphenation_keep_Type")
@XmlEnum
public enum HyphenationKeepType {

    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("column")
    COLUMN("column"),
    @XmlEnumValue("page")
    PAGE("page"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    HyphenationKeepType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static HyphenationKeepType fromValue(String v) {
        for (HyphenationKeepType c: HyphenationKeepType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
