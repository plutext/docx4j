
package org.docx4j.com.microsoft.schemas.office.x2006.encryption;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Encryption complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Encryption">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="keyData" type="{http://schemas.microsoft.com/office/2006/encryption}CT_KeyData"/>
 *         &lt;element name="dataIntegrity" type="{http://schemas.microsoft.com/office/2006/encryption}CT_DataIntegrity"/>
 *         &lt;element name="keyEncryptors" type="{http://schemas.microsoft.com/office/2006/encryption}CT_KeyEncryptors"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Encryption", propOrder = {
    "keyData",
    "dataIntegrity",
    "keyEncryptors"
})
@XmlRootElement (name="encryption")
public class CTEncryption {

    @XmlElement(required = true)
    protected CTKeyData keyData;
    @XmlElement(required = true)
    protected CTDataIntegrity dataIntegrity;
    @XmlElement(required = true)
    protected CTKeyEncryptors keyEncryptors;

    /**
     * Gets the value of the keyData property.
     * 
     * @return
     *     possible object is
     *     {@link CTKeyData }
     *     
     */
    public CTKeyData getKeyData() {
        return keyData;
    }

    /**
     * Sets the value of the keyData property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTKeyData }
     *     
     */
    public void setKeyData(CTKeyData value) {
        this.keyData = value;
    }

    /**
     * Gets the value of the dataIntegrity property.
     * 
     * @return
     *     possible object is
     *     {@link CTDataIntegrity }
     *     
     */
    public CTDataIntegrity getDataIntegrity() {
        return dataIntegrity;
    }

    /**
     * Sets the value of the dataIntegrity property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDataIntegrity }
     *     
     */
    public void setDataIntegrity(CTDataIntegrity value) {
        this.dataIntegrity = value;
    }

    /**
     * Gets the value of the keyEncryptors property.
     * 
     * @return
     *     possible object is
     *     {@link CTKeyEncryptors }
     *     
     */
    public CTKeyEncryptors getKeyEncryptors() {
        return keyEncryptors;
    }

    /**
     * Sets the value of the keyEncryptors property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTKeyEncryptors }
     *     
     */
    public void setKeyEncryptors(CTKeyEncryptors value) {
        this.keyEncryptors = value;
    }

}
