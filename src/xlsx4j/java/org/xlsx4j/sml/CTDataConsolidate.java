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
 * <p>Java class for CT_DataConsolidate complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DataConsolidate">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dataRefs" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_DataRefs" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="function" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DataConsolidateFunction" default="sum" />
 *       &lt;attribute name="leftLabels" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="topLabels" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="link" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DataConsolidate", propOrder = {
    "dataRefs"
})
public class CTDataConsolidate {

    protected CTDataRefs dataRefs;
    @XmlAttribute
    protected STDataConsolidateFunction function;
    @XmlAttribute
    protected Boolean leftLabels;
    @XmlAttribute
    protected Boolean topLabels;
    @XmlAttribute
    protected Boolean link;

    /**
     * Gets the value of the dataRefs property.
     * 
     * @return
     *     possible object is
     *     {@link CTDataRefs }
     *     
     */
    public CTDataRefs getDataRefs() {
        return dataRefs;
    }

    /**
     * Sets the value of the dataRefs property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDataRefs }
     *     
     */
    public void setDataRefs(CTDataRefs value) {
        this.dataRefs = value;
    }

    /**
     * Gets the value of the function property.
     * 
     * @return
     *     possible object is
     *     {@link STDataConsolidateFunction }
     *     
     */
    public STDataConsolidateFunction getFunction() {
        if (function == null) {
            return STDataConsolidateFunction.SUM;
        } else {
            return function;
        }
    }

    /**
     * Sets the value of the function property.
     * 
     * @param value
     *     allowed object is
     *     {@link STDataConsolidateFunction }
     *     
     */
    public void setFunction(STDataConsolidateFunction value) {
        this.function = value;
    }

    /**
     * Gets the value of the leftLabels property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLeftLabels() {
        if (leftLabels == null) {
            return false;
        } else {
            return leftLabels;
        }
    }

    /**
     * Sets the value of the leftLabels property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLeftLabels(Boolean value) {
        this.leftLabels = value;
    }

    /**
     * Gets the value of the topLabels property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTopLabels() {
        if (topLabels == null) {
            return false;
        } else {
            return topLabels;
        }
    }

    /**
     * Sets the value of the topLabels property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTopLabels(Boolean value) {
        this.topLabels = value;
    }

    /**
     * Gets the value of the link property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLink() {
        if (link == null) {
            return false;
        } else {
            return link;
        }
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLink(Boolean value) {
        this.link = value;
    }

}
