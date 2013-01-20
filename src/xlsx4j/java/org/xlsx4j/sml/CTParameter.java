/*
 *  Copyright 2010-2013, Plutext Pty Ltd.
 *   
 *  This file is part of xlsx4j, a component of docx4j.

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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Parameter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Parameter">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="sqlType" type="{http://www.w3.org/2001/XMLSchema}int" default="0" />
 *       &lt;attribute name="parameterType" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_ParameterType" default="prompt" />
 *       &lt;attribute name="refreshOnChange" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="prompt" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="boolean" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="double" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="integer" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="string" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="cell" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Parameter")
public class CTParameter implements Child
{

    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "sqlType")
    protected Integer sqlType;
    @XmlAttribute(name = "parameterType")
    protected STParameterType parameterType;
    @XmlAttribute(name = "refreshOnChange")
    protected Boolean refreshOnChange;
    @XmlAttribute(name = "prompt")
    protected String prompt;
    @XmlAttribute(name = "boolean")
    protected Boolean _boolean;
    @XmlAttribute(name = "double")
    protected Double _double;
    @XmlAttribute(name = "integer")
    protected Integer integer;
    @XmlAttribute(name = "string")
    protected String string;
    @XmlAttribute(name = "cell")
    protected String cell;
    @XmlTransient
    private Object parent;

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
     * Gets the value of the sqlType property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getSqlType() {
        if (sqlType == null) {
            return  0;
        } else {
            return sqlType;
        }
    }

    /**
     * Sets the value of the sqlType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSqlType(Integer value) {
        this.sqlType = value;
    }

    /**
     * Gets the value of the parameterType property.
     * 
     * @return
     *     possible object is
     *     {@link STParameterType }
     *     
     */
    public STParameterType getParameterType() {
        if (parameterType == null) {
            return STParameterType.PROMPT;
        } else {
            return parameterType;
        }
    }

    /**
     * Sets the value of the parameterType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STParameterType }
     *     
     */
    public void setParameterType(STParameterType value) {
        this.parameterType = value;
    }

    /**
     * Gets the value of the refreshOnChange property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRefreshOnChange() {
        if (refreshOnChange == null) {
            return false;
        } else {
            return refreshOnChange;
        }
    }

    /**
     * Sets the value of the refreshOnChange property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRefreshOnChange(Boolean value) {
        this.refreshOnChange = value;
    }

    /**
     * Gets the value of the prompt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * Sets the value of the prompt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrompt(String value) {
        this.prompt = value;
    }

    /**
     * Gets the value of the boolean property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isBoolean() {
        return _boolean;
    }

    /**
     * Sets the value of the boolean property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBoolean(Boolean value) {
        this._boolean = value;
    }

    /**
     * Gets the value of the double property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getDouble() {
        return _double;
    }

    /**
     * Sets the value of the double property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setDouble(Double value) {
        this._double = value;
    }

    /**
     * Gets the value of the integer property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getInteger() {
        return integer;
    }

    /**
     * Sets the value of the integer property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setInteger(Integer value) {
        this.integer = value;
    }

    /**
     * Gets the value of the string property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getString() {
        return string;
    }

    /**
     * Sets the value of the string property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setString(String value) {
        this.string = value;
    }

    /**
     * Gets the value of the cell property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCell() {
        return cell;
    }

    /**
     * Sets the value of the cell property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCell(String value) {
        this.cell = value;
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
