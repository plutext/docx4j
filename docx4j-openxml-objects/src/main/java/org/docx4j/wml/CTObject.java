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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_Object complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Object">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_PictureBase">
 *       &lt;sequence>
 *         &lt;element name="control" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Control" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="dxaOrig" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
 *       &lt;attribute name="dyaOrig" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_TwipsMeasure" />
 *       &lt;attribute ref="{http://schemas.microsoft.com/office/word/2010/wordml}anchorId"/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Object", propOrder = {
    "control"
})
public class CTObject
    extends CTPictureBase
{

    protected CTControl control;
    @XmlAttribute(name = "dxaOrig", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger dxaOrig;
    @XmlAttribute(name = "dyaOrig", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger dyaOrig;
    @XmlAttribute(name = "anchorId", namespace = "http://schemas.microsoft.com/office/word/2010/wordml")
    protected String anchorId;

    /**
     * Gets the value of the control property.
     * 
     * @return
     *     possible object is
     *     {@link CTControl }
     *     
     */
    public CTControl getControl() {
        return control;
    }

    /**
     * Sets the value of the control property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTControl }
     *     
     */
    public void setControl(CTControl value) {
        this.control = value;
    }

    /**
     * Gets the value of the dxaOrig property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDxaOrig() {
        return dxaOrig;
    }

    /**
     * Sets the value of the dxaOrig property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDxaOrig(BigInteger value) {
        this.dxaOrig = value;
    }

    /**
     * Gets the value of the dyaOrig property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDyaOrig() {
        return dyaOrig;
    }

    /**
     * Sets the value of the dyaOrig property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDyaOrig(BigInteger value) {
        this.dyaOrig = value;
    }

    /**
     * Gets the value of the anchorId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAnchorId() {
        return anchorId;
    }

    /**
     * Sets the value of the anchorId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAnchorId(String value) {
        this.anchorId = value;
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
