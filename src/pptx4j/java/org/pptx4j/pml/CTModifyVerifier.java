/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.pptx4j.pml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_ModifyVerifier complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ModifyVerifier">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="cryptProviderType" use="required" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_CryptProv" />
 *       &lt;attribute name="cryptAlgorithmClass" use="required" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_AlgClass" />
 *       &lt;attribute name="cryptAlgorithmType" use="required" type="{http://schemas.openxmlformats.org/presentationml/2006/main}ST_AlgType" />
 *       &lt;attribute name="cryptAlgorithmSid" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="spinCount" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="saltData" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="hashData" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="cryptProvider" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="algIdExt" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="algIdExtSource" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="cryptProviderTypeExt" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="cryptProviderTypeExtSource" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ModifyVerifier")
public class CTModifyVerifier {

    @XmlAttribute(required = true)
    protected STCryptProv cryptProviderType;
    @XmlAttribute(required = true)
    protected STAlgClass cryptAlgorithmClass;
    @XmlAttribute(required = true)
    protected STAlgType cryptAlgorithmType;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long cryptAlgorithmSid;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long spinCount;
    @XmlAttribute(required = true)
    protected String saltData;
    @XmlAttribute(required = true)
    protected String hashData;
    @XmlAttribute
    protected String cryptProvider;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long algIdExt;
    @XmlAttribute
    protected String algIdExtSource;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long cryptProviderTypeExt;
    @XmlAttribute
    protected String cryptProviderTypeExtSource;

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
     */
    public long getCryptAlgorithmSid() {
        return cryptAlgorithmSid;
    }

    /**
     * Sets the value of the cryptAlgorithmSid property.
     * 
     */
    public void setCryptAlgorithmSid(long value) {
        this.cryptAlgorithmSid = value;
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
     * Gets the value of the saltData property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSaltData() {
        return saltData;
    }

    /**
     * Sets the value of the saltData property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSaltData(String value) {
        this.saltData = value;
    }

    /**
     * Gets the value of the hashData property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHashData() {
        return hashData;
    }

    /**
     * Sets the value of the hashData property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHashData(String value) {
        this.hashData = value;
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
     *     {@link Long }
     *     
     */
    public Long getAlgIdExt() {
        return algIdExt;
    }

    /**
     * Sets the value of the algIdExt property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setAlgIdExt(Long value) {
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
     *     {@link Long }
     *     
     */
    public Long getCryptProviderTypeExt() {
        return cryptProviderTypeExt;
    }

    /**
     * Sets the value of the cryptProviderTypeExt property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCryptProviderTypeExt(Long value) {
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

}
