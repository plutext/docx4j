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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_OlapPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_OlapPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="local" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="localConnection" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="localRefresh" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="sendLocale" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="rowDrillCount" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="serverFill" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="serverNumberFormat" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="serverFont" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="serverFontColor" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_OlapPr")
public class CTOlapPr {

    @XmlAttribute
    protected Boolean local;
    @XmlAttribute
    protected String localConnection;
    @XmlAttribute
    protected Boolean localRefresh;
    @XmlAttribute
    protected Boolean sendLocale;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long rowDrillCount;
    @XmlAttribute
    protected Boolean serverFill;
    @XmlAttribute
    protected Boolean serverNumberFormat;
    @XmlAttribute
    protected Boolean serverFont;
    @XmlAttribute
    protected Boolean serverFontColor;

    /**
     * Gets the value of the local property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLocal() {
        if (local == null) {
            return false;
        } else {
            return local;
        }
    }

    /**
     * Sets the value of the local property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLocal(Boolean value) {
        this.local = value;
    }

    /**
     * Gets the value of the localConnection property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalConnection() {
        return localConnection;
    }

    /**
     * Sets the value of the localConnection property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocalConnection(String value) {
        this.localConnection = value;
    }

    /**
     * Gets the value of the localRefresh property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLocalRefresh() {
        if (localRefresh == null) {
            return true;
        } else {
            return localRefresh;
        }
    }

    /**
     * Sets the value of the localRefresh property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLocalRefresh(Boolean value) {
        this.localRefresh = value;
    }

    /**
     * Gets the value of the sendLocale property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSendLocale() {
        if (sendLocale == null) {
            return false;
        } else {
            return sendLocale;
        }
    }

    /**
     * Sets the value of the sendLocale property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSendLocale(Boolean value) {
        this.sendLocale = value;
    }

    /**
     * Gets the value of the rowDrillCount property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getRowDrillCount() {
        return rowDrillCount;
    }

    /**
     * Sets the value of the rowDrillCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRowDrillCount(Long value) {
        this.rowDrillCount = value;
    }

    /**
     * Gets the value of the serverFill property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isServerFill() {
        if (serverFill == null) {
            return true;
        } else {
            return serverFill;
        }
    }

    /**
     * Sets the value of the serverFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setServerFill(Boolean value) {
        this.serverFill = value;
    }

    /**
     * Gets the value of the serverNumberFormat property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isServerNumberFormat() {
        if (serverNumberFormat == null) {
            return true;
        } else {
            return serverNumberFormat;
        }
    }

    /**
     * Sets the value of the serverNumberFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setServerNumberFormat(Boolean value) {
        this.serverNumberFormat = value;
    }

    /**
     * Gets the value of the serverFont property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isServerFont() {
        if (serverFont == null) {
            return true;
        } else {
            return serverFont;
        }
    }

    /**
     * Sets the value of the serverFont property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setServerFont(Boolean value) {
        this.serverFont = value;
    }

    /**
     * Gets the value of the serverFontColor property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isServerFontColor() {
        if (serverFontColor == null) {
            return true;
        } else {
            return serverFontColor;
        }
    }

    /**
     * Sets the value of the serverFontColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setServerFontColor(Boolean value) {
        this.serverFontColor = value;
    }

}
