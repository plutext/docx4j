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
 * <p>Java class for CT_Query complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Query">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="tpls" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Tuples" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="mdx" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Query", propOrder = {
    "tpls"
})
public class CTQuery {

    protected CTTuples tpls;
    @XmlAttribute(required = true)
    protected String mdx;

    /**
     * Gets the value of the tpls property.
     * 
     * @return
     *     possible object is
     *     {@link CTTuples }
     *     
     */
    public CTTuples getTpls() {
        return tpls;
    }

    /**
     * Sets the value of the tpls property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTuples }
     *     
     */
    public void setTpls(CTTuples value) {
        this.tpls = value;
    }

    /**
     * Gets the value of the mdx property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMdx() {
        return mdx;
    }

    /**
     * Sets the value of the mdx property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMdx(String value) {
        this.mdx = value;
    }

}
