
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for treat_as_word_space_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="treat_as_word_space_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="auto"/>
 *     &lt;enumeration value="true"/>
 *     &lt;enumeration value="false"/>
 *     &lt;enumeration value="inherit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "treat_as_word_space_Type")
@XmlEnum
public enum TreatAsWordSpaceType {

    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("true")
    TRUE("true"),
    @XmlEnumValue("false")
    FALSE("false"),
    @XmlEnumValue("inherit")
    INHERIT("inherit");
    private final String value;

    TreatAsWordSpaceType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TreatAsWordSpaceType fromValue(String v) {
        for (TreatAsWordSpaceType c: TreatAsWordSpaceType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
