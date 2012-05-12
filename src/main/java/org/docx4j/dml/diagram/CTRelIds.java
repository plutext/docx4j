/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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


package org.docx4j.dml.diagram;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_RelIds complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RelIds">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}dm use="required""/>
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}lo use="required""/>
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}qs use="required""/>
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}cs use="required""/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_RelIds")
public class CTRelIds {

    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships", required = true)
    protected String dm;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships", required = true)
    protected String lo;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships", required = true)
    protected String qs;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships", required = true)
    protected String cs;

    /**
     * 
     * 					Explicit Relationship to Diagram Data Part
     * 				
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDm() {
        if (dm == null) {
            return "";
        } else {
            return dm;
        }
    }

    /**
     * Sets the value of the dm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDm(String value) {
        this.dm = value;
    }

    /**
     * 
     * 					Explicit Relationship to Diagram Layout Definition
     * 					Part
     * 				
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLo() {
        if (lo == null) {
            return "";
        } else {
            return lo;
        }
    }

    /**
     * Sets the value of the lo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLo(String value) {
        this.lo = value;
    }

    /**
     * 
     * 					Explicit Relationship to Style Definition Part
     * 				
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQs() {
        if (qs == null) {
            return "";
        } else {
            return qs;
        }
    }

    /**
     * Sets the value of the qs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQs(String value) {
        this.qs = value;
    }

    /**
     * 
     * 					Explicit Relationship to Diagram Colors Part
     * 				
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCs() {
        if (cs == null) {
            return "";
        } else {
            return cs;
        }
    }

    /**
     * Sets the value of the cs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCs(String value) {
        this.cs = value;
    }

}
