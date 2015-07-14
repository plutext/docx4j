
package org.docx4j.com.microsoft.schemas.office.x2006.keyEncryptor.password;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.docx4j.com.microsoft.schemas.office.x2006.encryption.STCipherChaining;
import org.docx4j.com.microsoft.schemas.office.x2006.encryption.STHashAlgorithm;


/**
 * <p>Java class for CT_PasswordKeyEncryptor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PasswordKeyEncryptor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="saltSize" use="required" type="{http://schemas.microsoft.com/office/2006/encryption}ST_SaltSize" />
 *       &lt;attribute name="blockSize" use="required" type="{http://schemas.microsoft.com/office/2006/encryption}ST_BlockSize" />
 *       &lt;attribute name="keyBits" use="required" type="{http://schemas.microsoft.com/office/2006/encryption}ST_KeyBits" />
 *       &lt;attribute name="hashSize" use="required" type="{http://schemas.microsoft.com/office/2006/encryption}ST_HashSize" />
 *       &lt;attribute name="cipherAlgorithm" use="required" type="{http://schemas.microsoft.com/office/2006/encryption}ST_CipherAlgorithm" />
 *       &lt;attribute name="cipherChaining" use="required" type="{http://schemas.microsoft.com/office/2006/encryption}ST_CipherChaining" />
 *       &lt;attribute name="hashAlgorithm" use="required" type="{http://schemas.microsoft.com/office/2006/encryption}ST_HashAlgorithm" />
 *       &lt;attribute name="saltValue" use="required" type="{http://www.w3.org/2001/XMLSchema}base64Binary" />
 *       &lt;attribute name="spinCount" use="required" type="{http://schemas.microsoft.com/office/2006/encryption}ST_SpinCount" />
 *       &lt;attribute name="encryptedVerifierHashInput" use="required" type="{http://www.w3.org/2001/XMLSchema}base64Binary" />
 *       &lt;attribute name="encryptedVerifierHashValue" use="required" type="{http://www.w3.org/2001/XMLSchema}base64Binary" />
 *       &lt;attribute name="encryptedKeyValue" use="required" type="{http://www.w3.org/2001/XMLSchema}base64Binary" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PasswordKeyEncryptor")
public class CTPasswordKeyEncryptor {

    @XmlAttribute(name = "saltSize", required = true)
    protected long saltSize;
    @XmlAttribute(name = "blockSize", required = true)
    protected long blockSize;
    @XmlAttribute(name = "keyBits", required = true)
    protected long keyBits;
    @XmlAttribute(name = "hashSize", required = true)
    protected long hashSize;
    @XmlAttribute(name = "cipherAlgorithm", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String cipherAlgorithm;
    @XmlAttribute(name = "cipherChaining", required = true)
    protected STCipherChaining cipherChaining;
    @XmlAttribute(name = "hashAlgorithm", required = true)
    protected STHashAlgorithm hashAlgorithm;
    @XmlAttribute(name = "saltValue", required = true)
    protected byte[] saltValue;
    @XmlAttribute(name = "spinCount", required = true)
    protected long spinCount;
    @XmlAttribute(name = "encryptedVerifierHashInput", required = true)
    protected byte[] encryptedVerifierHashInput;
    @XmlAttribute(name = "encryptedVerifierHashValue", required = true)
    protected byte[] encryptedVerifierHashValue;
    @XmlAttribute(name = "encryptedKeyValue", required = true)
    protected byte[] encryptedKeyValue;

    /**
     * Gets the value of the saltSize property.
     * 
     */
    public long getSaltSize() {
        return saltSize;
    }

    /**
     * Sets the value of the saltSize property.
     * 
     */
    public void setSaltSize(long value) {
        this.saltSize = value;
    }

    /**
     * Gets the value of the blockSize property.
     * 
     */
    public long getBlockSize() {
        return blockSize;
    }

    /**
     * Sets the value of the blockSize property.
     * 
     */
    public void setBlockSize(long value) {
        this.blockSize = value;
    }

    /**
     * Gets the value of the keyBits property.
     * 
     */
    public long getKeyBits() {
        return keyBits;
    }

    /**
     * Sets the value of the keyBits property.
     * 
     */
    public void setKeyBits(long value) {
        this.keyBits = value;
    }

    /**
     * Gets the value of the hashSize property.
     * 
     */
    public long getHashSize() {
        return hashSize;
    }

    /**
     * Sets the value of the hashSize property.
     * 
     */
    public void setHashSize(long value) {
        this.hashSize = value;
    }

    /**
     * Gets the value of the cipherAlgorithm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCipherAlgorithm() {
        return cipherAlgorithm;
    }

    /**
     * Sets the value of the cipherAlgorithm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCipherAlgorithm(String value) {
        this.cipherAlgorithm = value;
    }

    /**
     * Gets the value of the cipherChaining property.
     * 
     * @return
     *     possible object is
     *     {@link STCipherChaining }
     *     
     */
    public STCipherChaining getCipherChaining() {
        return cipherChaining;
    }

    /**
     * Sets the value of the cipherChaining property.
     * 
     * @param value
     *     allowed object is
     *     {@link STCipherChaining }
     *     
     */
    public void setCipherChaining(STCipherChaining value) {
        this.cipherChaining = value;
    }

    /**
     * Gets the value of the hashAlgorithm property.
     * 
     * @return
     *     possible object is
     *     {@link STHashAlgorithm }
     *     
     */
    public STHashAlgorithm getHashAlgorithm() {
        return hashAlgorithm;
    }

    /**
     * Sets the value of the hashAlgorithm property.
     * 
     * @param value
     *     allowed object is
     *     {@link STHashAlgorithm }
     *     
     */
    public void setHashAlgorithm(STHashAlgorithm value) {
        this.hashAlgorithm = value;
    }

    /**
     * Gets the value of the saltValue property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getSaltValue() {
        return saltValue;
    }

    /**
     * Sets the value of the saltValue property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setSaltValue(byte[] value) {
        this.saltValue = value;
    }

    /**
     * Gets the value of the spinCount property.
     * 
     */
    public long getSpinCount() {
        return spinCount;
    }

    /**
     * Sets the value of the spinCount property.
     * 
     */
    public void setSpinCount(long value) {
        this.spinCount = value;
    }

    /**
     * Gets the value of the encryptedVerifierHashInput property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getEncryptedVerifierHashInput() {
        return encryptedVerifierHashInput;
    }

    /**
     * Sets the value of the encryptedVerifierHashInput property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setEncryptedVerifierHashInput(byte[] value) {
        this.encryptedVerifierHashInput = value;
    }

    /**
     * Gets the value of the encryptedVerifierHashValue property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getEncryptedVerifierHashValue() {
        return encryptedVerifierHashValue;
    }

    /**
     * Sets the value of the encryptedVerifierHashValue property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setEncryptedVerifierHashValue(byte[] value) {
        this.encryptedVerifierHashValue = value;
    }

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

}
