
package org.docx4j.com.microsoft.schemas.office.x2006.keyEncryptor.certificate;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ST_PasswordKeyEncryptorUri.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ST_PasswordKeyEncryptorUri">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="http://schemas.microsoft.com/office/2006/keyEncryptor/certificate"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ST_PasswordKeyEncryptorUri")
@XmlEnum
public enum STPasswordKeyEncryptorUri {

    @XmlEnumValue("http://schemas.microsoft.com/office/2006/keyEncryptor/certificate")
    HTTP_SCHEMAS_MICROSOFT_COM_OFFICE_2006_KEY_ENCRYPTOR_CERTIFICATE("http://schemas.microsoft.com/office/2006/keyEncryptor/certificate");
    private final String value;

    STPasswordKeyEncryptorUri(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static STPasswordKeyEncryptorUri fromValue(String v) {
        for (STPasswordKeyEncryptorUri c: STPasswordKeyEncryptorUri.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
