
package org.docx4j.com.microsoft.schemas.office.x2006.encryption;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * A complex type that specifies data used to verify whether the encrypted data passes an integrity check. It MUST be generated using the method specified in section 2.3.4.14 (http://msdn.microsoft.com/en-us/library/dd924068(v=office.12).aspx).
 * 
 * <p>Java class for CT_DataIntegrity complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DataIntegrity">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="encryptedHmacKey" use="required" type="{http://www.w3.org/2001/XMLSchema}base64Binary" />
 *       &lt;attribute name="encryptedHmacValue" use="required" type="{http://www.w3.org/2001/XMLSchema}base64Binary" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DataIntegrity")
public class CTDataIntegrity {

    @XmlAttribute(name = "encryptedHmacKey", required = true)
    protected byte[] encryptedHmacKey;
    @XmlAttribute(name = "encryptedHmacValue", required = true)
    protected byte[] encryptedHmacValue;

    /**
     * Gets the value of the encryptedHmacKey property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getEncryptedHmacKey() {
        return encryptedHmacKey;
    }

    /**
     * Sets the value of the encryptedHmacKey property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setEncryptedHmacKey(byte[] value) {
        this.encryptedHmacKey = value;
    }

    /**
     * Gets the value of the encryptedHmacValue property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getEncryptedHmacValue() {
        return encryptedHmacValue;
    }

    /**
     * Sets the value of the encryptedHmacValue property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setEncryptedHmacValue(byte[] value) {
        this.encryptedHmacValue = value;
    }

}
