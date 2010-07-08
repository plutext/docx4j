
package org.plutext.jaxb.xslfo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for conditionality_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="conditionality_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="retain"/>
 *     &lt;enumeration value="discard"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "conditionality_Type")
@XmlEnum
public enum ConditionalityType {

    @XmlEnumValue("retain")
    RETAIN("retain"),
    @XmlEnumValue("discard")
    DISCARD("discard");
    private final String value;

    ConditionalityType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ConditionalityType fromValue(String v) {
        for (ConditionalityType c: ConditionalityType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
