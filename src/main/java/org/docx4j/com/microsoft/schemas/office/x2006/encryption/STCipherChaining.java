
package org.docx4j.com.microsoft.schemas.office.x2006.encryption;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_CipherChaining.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_CipherChaining">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="ChainingModeCBC"/>
 *     &lt;enumeration value="ChainingModeCFB"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_CipherChaining")
@XmlEnum
public enum STCipherChaining {


    /**
     * block chaining (CBC)
     * 
     */
    @XmlEnumValue("ChainingModeCBC")
    CHAINING_MODE_CBC("ChainingModeCBC"),

    /**
     * Cipher feedback chaining (CFB), with an 8-bit window
     * 
     */
    @XmlEnumValue("ChainingModeCFB")
    CHAINING_MODE_CFB("ChainingModeCFB");
    private final String value;

    STCipherChaining(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STCipherChaining fromValue(String v) {
        for (STCipherChaining c: STCipherChaining.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
