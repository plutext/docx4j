
package org.docx4j.com.microsoft.schemas.office.x2006.encryption;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.docx4j.com.microsoft.schemas.office.x2006.keyEncryptor.certificate.CTCertificateKeyEncryptor;
import org.docx4j.com.microsoft.schemas.office.x2006.keyEncryptor.password.CTPasswordKeyEncryptor;


/**
 * A complex type that specifies the parameters used to encrypt an intermediate key, which is used to perform the final encryption of the document. To ensure extensibility, arbitrary elements can be defined to encrypt the intermediate key. The intermediate key MUST be the same for all KeyEncryptor elements.
 * 
 * <p>Java class for CT_KeyEncryptor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_KeyEncryptor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{http://schemas.microsoft.com/office/2006/keyEncryptor/password}encryptedKey"/>
 *         &lt;element ref="{http://schemas.microsoft.com/office/2006/keyEncryptor/certificate}encryptedKey"/>
 *       &lt;/choice>
 *       &lt;attribute name="uri">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *             &lt;enumeration value="http://schemas.microsoft.com/office/2006/keyEncryptor/password"/>
 *             &lt;enumeration value="http://schemas.microsoft.com/office/2006/keyEncryptor/certificate"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_KeyEncryptor", propOrder = {
    "encryptedPasswordKey",
    "encryptedCertificateKey"
})
public class CTKeyEncryptor {

    @XmlElement(name = "encryptedKey", namespace = "http://schemas.microsoft.com/office/2006/keyEncryptor/password")
    protected CTPasswordKeyEncryptor encryptedPasswordKey;
    @XmlElement(name = "encryptedKey", namespace = "http://schemas.microsoft.com/office/2006/keyEncryptor/certificate")
    protected CTCertificateKeyEncryptor encryptedCertificateKey;
    @XmlAttribute(name = "uri")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String uri;

    /**
     * Gets the value of the encryptedPasswordKey property.
     * 
     * @return
     *     possible object is
     *     {@link CTPasswordKeyEncryptor }
     *     
     */
    public CTPasswordKeyEncryptor getEncryptedPasswordKey() {
        return encryptedPasswordKey;
    }

    /**
     * Sets the value of the encryptedPasswordKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPasswordKeyEncryptor }
     *     
     */
    public void setEncryptedPasswordKey(CTPasswordKeyEncryptor value) {
        this.encryptedPasswordKey = value;
    }

    /**
     * Gets the value of the encryptedCertificateKey property.
     * 
     * @return
     *     possible object is
     *     {@link CTCertificateKeyEncryptor }
     *     
     */
    public CTCertificateKeyEncryptor getEncryptedCertificateKey() {
        return encryptedCertificateKey;
    }

    /**
     * Sets the value of the encryptedCertificateKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCertificateKeyEncryptor }
     *     
     */
    public void setEncryptedCertificateKey(CTCertificateKeyEncryptor value) {
        this.encryptedCertificateKey = value;
    }

    /**
     * Gets the value of the uri property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the value of the uri property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUri(String value) {
        this.uri = value;
    }

}
