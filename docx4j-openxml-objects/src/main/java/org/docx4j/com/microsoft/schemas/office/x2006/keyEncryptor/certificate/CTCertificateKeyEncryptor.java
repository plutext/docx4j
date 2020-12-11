
package org.docx4j.com.microsoft.schemas.office.x2006.keyEncryptor.certificate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_CertificateKeyEncryptor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CertificateKeyEncryptor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="encryptedKeyValue" use="required" type="{http://www.w3.org/2001/XMLSchema}base64Binary" />
 *       &lt;attribute name="X509Certificate" use="required" type="{http://www.w3.org/2001/XMLSchema}base64Binary" />
 *       &lt;attribute name="certVerifier" use="required" type="{http://www.w3.org/2001/XMLSchema}base64Binary" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CertificateKeyEncryptor")
public class CTCertificateKeyEncryptor {

    @XmlAttribute(name = "encryptedKeyValue", required = true)
    protected byte[] encryptedKeyValue;
    @XmlAttribute(name = "X509Certificate", required = true)
    protected byte[] x509Certificate;
    @XmlAttribute(name = "certVerifier", required = true)
    protected byte[] certVerifier;

    /**
     * Gets the value of the encryptedKeyValue property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getEncryptedKeyValue() {
        return encryptedKeyValue;
    }

    /**
     * Sets the value of the encryptedKeyValue property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setEncryptedKeyValue(byte[] value) {
        this.encryptedKeyValue = value;
    }

    /**
     * Gets the value of the x509Certificate property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getX509Certificate() {
        return x509Certificate;
    }

    /**
     * Sets the value of the x509Certificate property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setX509Certificate(byte[] value) {
        this.x509Certificate = value;
    }

    /**
     * Gets the value of the certVerifier property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getCertVerifier() {
        return certVerifier;
    }

    /**
     * Sets the value of the certVerifier property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setCertVerifier(byte[] value) {
        this.certVerifier = value;
    }

}
