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


package org.xlsx4j.sml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_PivotTableStyle complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PivotTableStyle">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="showRowHeaders" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="showColHeaders" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="showRowStripes" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="showColStripes" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="showLastColumn" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PivotTableStyle")
public class CTPivotTableStyle {

    @XmlAttribute
    protected String name;
    @XmlAttribute
    protected Boolean showRowHeaders;
    @XmlAttribute
    protected Boolean showColHeaders;
    @XmlAttribute
    protected Boolean showRowStripes;
    @XmlAttribute
    protected Boolean showColStripes;
    @XmlAttribute
    protected Boolean showLastColumn;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the showRowHeaders property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isShowRowHeaders() {
        return showRowHeaders;
    }

    /**
     * Sets the value of the showRowHeaders property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowRowHeaders(Boolean value) {
        this.showRowHeaders = value;
    }

    /**
     * Gets the value of the showColHeaders property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isShowColHeaders() {
        return showColHeaders;
    }

    /**
     * Sets the value of the showColHeaders property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowColHeaders(Boolean value) {
        this.showColHeaders = value;
    }

    /**
     * Gets the value of the showRowStripes property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isShowRowStripes() {
        return showRowStripes;
    }

    /**
     * Sets the value of the showRowStripes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowRowStripes(Boolean value) {
        this.showRowStripes = value;
    }

    /**
     * Gets the value of the showColStripes property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isShowColStripes() {
        return showColStripes;
    }

    /**
     * Sets the value of the showColStripes property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowColStripes(Boolean value) {
        this.showColStripes = value;
    }

    /**
     * Gets the value of the showLastColumn property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isShowLastColumn() {
        return showLastColumn;
    }

    /**
     * Sets the value of the showLastColumn property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowLastColumn(Boolean value) {
        this.showLastColumn = value;
    }

}
