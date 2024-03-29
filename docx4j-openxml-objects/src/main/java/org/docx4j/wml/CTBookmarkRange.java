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

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_BookmarkRange complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_BookmarkRange">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_MarkupRange">
 *       &lt;attribute name="colFirst" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *       &lt;attribute name="colLast" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_DecimalNumber" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_BookmarkRange")
public class CTBookmarkRange
    extends CTMarkupRange

{

    @XmlAttribute(name = "colFirst", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger colFirst;
    @XmlAttribute(name = "colLast", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected BigInteger colLast;


    /**
     * Gets the value of the colFirst property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getColFirst() {
        return colFirst;
    }

    /**
     * Sets the value of the colFirst property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setColFirst(BigInteger value) {
        this.colFirst = value;
    }

    /**
     * Gets the value of the colLast property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getColLast() {
        return colLast;
    }

    /**
     * Sets the value of the colLast property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setColLast(BigInteger value) {
        this.colLast = value;
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
