
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_EffectContainerType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_EffectContainerType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="sib"/>
 *     &lt;enumeration value="tree"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_EffectContainerType")
@XmlEnum
public enum STEffectContainerType {


    /**
     * Sibling
     * 
     */
    @XmlEnumValue("sib")
    SIB("sib"),

    /**
     * Tree
     * 
     */
    @XmlEnumValue("tree")
    TREE("tree");
    private final String value;

    STEffectContainerType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STEffectContainerType fromValue(String v) {
        for (STEffectContainerType c: STEffectContainerType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
