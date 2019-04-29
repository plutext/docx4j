
package org.docx4j.com.microsoft.schemas.ink.x2010.main;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_Dir.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_Dir"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="to"/&gt;
 *     &lt;enumeration value="from"/&gt;
 *     &lt;enumeration value="with"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ST_Dir")
@XmlEnum
public enum STDir {

    @XmlEnumValue("to")
    TO("to"),
    @XmlEnumValue("from")
    FROM("from"),
    @XmlEnumValue("with")
    WITH("with");
    private final String value;

    STDir(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STDir fromValue(String v) {
        for (STDir c: STDir.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
