
package org.docx4j.dml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_FontCollectionIndex.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_FontCollectionIndex">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="major"/>
 *     &lt;enumeration value="minor"/>
 *     &lt;enumeration value="none"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_FontCollectionIndex")
@XmlEnum
public enum STFontCollectionIndex {


    /**
     * Major Font
     * 
     */
    @XmlEnumValue("major")
    MAJOR("major"),

    /**
     * Minor Font
     * 
     */
    @XmlEnumValue("minor")
    MINOR("minor"),

    /**
     * None
     * 
     */
    @XmlEnumValue("none")
    NONE("none");
    private final String value;

    STFontCollectionIndex(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STFontCollectionIndex fromValue(String v) {
        for (STFontCollectionIndex c: STFontCollectionIndex.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
