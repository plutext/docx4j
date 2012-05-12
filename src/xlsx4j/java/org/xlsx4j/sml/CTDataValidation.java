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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_DataValidation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DataValidation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="formula1" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Formula" minOccurs="0"/>
 *         &lt;element name="formula2" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Formula" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DataValidationType" default="none" />
 *       &lt;attribute name="errorStyle" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DataValidationErrorStyle" default="stop" />
 *       &lt;attribute name="imeMode" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DataValidationImeMode" default="noControl" />
 *       &lt;attribute name="operator" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DataValidationOperator" default="between" />
 *       &lt;attribute name="allowBlank" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showDropDown" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showInputMessage" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showErrorMessage" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="errorTitle" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="error" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="promptTitle" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="prompt" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="sqref" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Sqref" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DataValidation", propOrder = {
    "formula1",
    "formula2"
})
public class CTDataValidation {

    protected String formula1;
    protected String formula2;
    @XmlAttribute
    protected STDataValidationType type;
    @XmlAttribute
    protected STDataValidationErrorStyle errorStyle;
    @XmlAttribute
    protected STDataValidationImeMode imeMode;
    @XmlAttribute
    protected STDataValidationOperator operator;
    @XmlAttribute
    protected Boolean allowBlank;
    @XmlAttribute
    protected Boolean showDropDown;
    @XmlAttribute
    protected Boolean showInputMessage;
    @XmlAttribute
    protected Boolean showErrorMessage;
    @XmlAttribute
    protected String errorTitle;
    @XmlAttribute
    protected String error;
    @XmlAttribute
    protected String promptTitle;
    @XmlAttribute
    protected String prompt;
    @XmlAttribute(required = true)
    protected List<String> sqref;

    /**
     * Gets the value of the formula1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormula1() {
        return formula1;
    }

    /**
     * Sets the value of the formula1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormula1(String value) {
        this.formula1 = value;
    }

    /**
     * Gets the value of the formula2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormula2() {
        return formula2;
    }

    /**
     * Sets the value of the formula2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormula2(String value) {
        this.formula2 = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link STDataValidationType }
     *     
     */
    public STDataValidationType getType() {
        if (type == null) {
            return STDataValidationType.NONE;
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link STDataValidationType }
     *     
     */
    public void setType(STDataValidationType value) {
        this.type = value;
    }

    /**
     * Gets the value of the errorStyle property.
     * 
     * @return
     *     possible object is
     *     {@link STDataValidationErrorStyle }
     *     
     */
    public STDataValidationErrorStyle getErrorStyle() {
        if (errorStyle == null) {
            return STDataValidationErrorStyle.STOP;
        } else {
            return errorStyle;
        }
    }

    /**
     * Sets the value of the errorStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link STDataValidationErrorStyle }
     *     
     */
    public void setErrorStyle(STDataValidationErrorStyle value) {
        this.errorStyle = value;
    }

    /**
     * Gets the value of the imeMode property.
     * 
     * @return
     *     possible object is
     *     {@link STDataValidationImeMode }
     *     
     */
    public STDataValidationImeMode getImeMode() {
        if (imeMode == null) {
            return STDataValidationImeMode.NO_CONTROL;
        } else {
            return imeMode;
        }
    }

    /**
     * Sets the value of the imeMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link STDataValidationImeMode }
     *     
     */
    public void setImeMode(STDataValidationImeMode value) {
        this.imeMode = value;
    }

    /**
     * Gets the value of the operator property.
     * 
     * @return
     *     possible object is
     *     {@link STDataValidationOperator }
     *     
     */
    public STDataValidationOperator getOperator() {
        if (operator == null) {
            return STDataValidationOperator.BETWEEN;
        } else {
            return operator;
        }
    }

    /**
     * Sets the value of the operator property.
     * 
     * @param value
     *     allowed object is
     *     {@link STDataValidationOperator }
     *     
     */
    public void setOperator(STDataValidationOperator value) {
        this.operator = value;
    }

    /**
     * Gets the value of the allowBlank property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAllowBlank() {
        if (allowBlank == null) {
            return false;
        } else {
            return allowBlank;
        }
    }

    /**
     * Sets the value of the allowBlank property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAllowBlank(Boolean value) {
        this.allowBlank = value;
    }

    /**
     * Gets the value of the showDropDown property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowDropDown() {
        if (showDropDown == null) {
            return false;
        } else {
            return showDropDown;
        }
    }

    /**
     * Sets the value of the showDropDown property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowDropDown(Boolean value) {
        this.showDropDown = value;
    }

    /**
     * Gets the value of the showInputMessage property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowInputMessage() {
        if (showInputMessage == null) {
            return false;
        } else {
            return showInputMessage;
        }
    }

    /**
     * Sets the value of the showInputMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowInputMessage(Boolean value) {
        this.showInputMessage = value;
    }

    /**
     * Gets the value of the showErrorMessage property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowErrorMessage() {
        if (showErrorMessage == null) {
            return false;
        } else {
            return showErrorMessage;
        }
    }

    /**
     * Sets the value of the showErrorMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowErrorMessage(Boolean value) {
        this.showErrorMessage = value;
    }

    /**
     * Gets the value of the errorTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorTitle() {
        return errorTitle;
    }

    /**
     * Sets the value of the errorTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorTitle(String value) {
        this.errorTitle = value;
    }

    /**
     * Gets the value of the error property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the value of the error property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setError(String value) {
        this.error = value;
    }

    /**
     * Gets the value of the promptTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPromptTitle() {
        return promptTitle;
    }

    /**
     * Sets the value of the promptTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPromptTitle(String value) {
        this.promptTitle = value;
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
     * Gets the value of the sqref property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sqref property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSqref().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getSqref() {
        if (sqref == null) {
            sqref = new ArrayList<String>();
        }
        return this.sqref;
    }

}
