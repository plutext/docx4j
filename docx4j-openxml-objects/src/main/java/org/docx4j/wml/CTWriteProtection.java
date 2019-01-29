/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */


package org.docx4j.wml; 

import java.math.BigInteger;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_WriteProtection complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_WriteProtection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}AG_Password"/>
 *       &lt;attribute name="recommended" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_WriteProtection")
public class CTWriteProtection implements Child
{

    @XmlAttribute(name = "recommended", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected Boolean recommended;
    @XmlAttribute(name = "cryptProviderType", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STCryptProv cryptProviderType;
    @XmlAttribute(name = "cryptAlgorithmClass", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STAlgClass cryptAlgorithmClass;
    @XmlAttribute(name = "cryptAlgorithmType", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STAlgType cryptAlgorithmType;
    @XmlAttribute(name = "cryptAlgorithmSid", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger cryptAlgorithmSid;
    @XmlAttribute(name = "cryptSpinCount", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger cryptSpinCount;
    @XmlAttribute(name = "cryptProvider", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String cryptProvider;
    @XmlAttribute(name = "algIdExt", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String algIdExt;
    @XmlAttribute(name = "algIdExtSource", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String algIdExtSource;
    @XmlAttribute(name = "cryptProviderTypeExt", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String cryptProviderTypeExt;
    @XmlAttribute(name = "cryptProviderTypeExtSource", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected String cryptProviderTypeExtSource;
    @XmlAttribute(name = "hash", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected byte[] hash;
    @XmlAttribute(name = "salt", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected byte[] salt;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the recommended property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRecommended() {
        if (recommended == null) {
            return true;
        } else {
            return recommended;
        }
    }

    /**
     * Sets the value of the recommended property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRecommended(Boolean value) {
        this.recommended = value;
    }

    /**
     * Gets the value of the cryptProviderType property.
     * 
     * @return
     *     possible object is
     *     {@link STCryptProv }
     *     
     */
    public STCryptProv getCryptProviderType() {
        return cryptProviderType;
    }

    /**
     * Sets the value of the cryptProviderType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STCryptProv }
     *     
     */
    public void setCryptProviderType(STCryptProv value) {
        this.cryptProviderType = value;
    }

    /**
     * Gets the value of the cryptAlgorithmClass property.
     * 
     * @return
     *     possible object is
     *     {@link STAlgClass }
     *     
     */
    public STAlgClass getCryptAlgorithmClass() {
        return cryptAlgorithmClass;
    }

    /**
     * Sets the value of the cryptAlgorithmClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link STAlgClass }
     *     
     */
    public void setCryptAlgorithmClass(STAlgClass value) {
        this.cryptAlgorithmClass = value;
    }

    /**
     * Gets the value of the cryptAlgorithmType property.
     * 
     * @return
     *     possible object is
     *     {@link STAlgType }
     *     
     */
    public STAlgType getCryptAlgorithmType() {
        return cryptAlgorithmType;
    }

    /**
     * Sets the value of the cryptAlgorithmType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STAlgType }
     *     
     */
    public void setCryptAlgorithmType(STAlgType value) {
        this.cryptAlgorithmType = value;
    }

    /**
     * Gets the value of the cryptAlgorithmSid property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getCryptAlgorithmSid() {
        return cryptAlgorithmSid;
    }

    /**
     * Sets the value of the cryptAlgorithmSid property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setCryptAlgorithmSid(BigInteger value) {
        this.cryptAlgorithmSid = value;
    }

    /**
     * Gets the value of the cryptSpinCount property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getCryptSpinCount() {
        return cryptSpinCount;
    }

    /**
     * Sets the value of the cryptSpinCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setCryptSpinCount(BigInteger value) {
        this.cryptSpinCount = value;
    }

    /**
     * Gets the value of the cryptProvider property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCryptProvider() {
        return cryptProvider;
    }

    /**
     * Sets the value of the cryptProvider property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCryptProvider(String value) {
        this.cryptProvider = value;
    }

    /**
     * Gets the value of the algIdExt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlgIdExt() {
        return algIdExt;
    }

    /**
     * Sets the value of the algIdExt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlgIdExt(String value) {
        this.algIdExt = value;
    }

    /**
     * Gets the value of the algIdExtSource property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlgIdExtSource() {
        return algIdExtSource;
    }

    /**
     * Sets the value of the algIdExtSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlgIdExtSource(String value) {
        this.algIdExtSource = value;
    }

    /**
     * Gets the value of the cryptProviderTypeExt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCryptProviderTypeExt() {
        return cryptProviderTypeExt;
    }

    /**
     * Sets the value of the cryptProviderTypeExt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCryptProviderTypeExt(String value) {
        this.cryptProviderTypeExt = value;
    }

    /**
     * Gets the value of the cryptProviderTypeExtSource property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCryptProviderTypeExtSource() {
        return cryptProviderTypeExtSource;
    }

    /**
     * Sets the value of the cryptProviderTypeExtSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCryptProviderTypeExtSource(String value) {
        this.cryptProviderTypeExtSource = value;
    }

    /**
     * Gets the value of the hash property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getHash() {
        return hash;
    }

    /**
     * Sets the value of the hash property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setHash(byte[] value) {
        this.hash = value;
    }

    /**
     * Gets the value of the salt property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getSalt() {
        return salt;
    }

    /**
     * Sets the value of the salt property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setSalt(byte[] value) {
        this.salt = value;
    }

    /**
     * Gets the parent object in the object tree representing the unmarshalled xml document.
     * 
     * @return
     *     The parent object.
     */
    public Object getParent() {
        return this.parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    /**
     * This method is invoked by the JAXB implementation on each instance when unmarshalling completes.
     * 
     * @param parent
     *     The parent object in the object tree.
     * @param unmarshaller
     *     The unmarshaller that generated the instance.
     */
    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        setParent(parent);
    }

}
