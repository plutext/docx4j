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
 * <p>Java class for CT_IgnoredError complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_IgnoredError">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="sqref" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Sqref" />
 *       &lt;attribute name="evalError" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="twoDigitTextYear" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="numberStoredAsText" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="formula" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="formulaRange" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="unlockedFormula" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="emptyCellReference" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="listDataValidation" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="calculatedColumn" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_IgnoredError")
public class CTIgnoredError {

    @XmlAttribute(required = true)
    protected List<String> sqref;
    @XmlAttribute
    protected Boolean evalError;
    @XmlAttribute
    protected Boolean twoDigitTextYear;
    @XmlAttribute
    protected Boolean numberStoredAsText;
    @XmlAttribute
    protected Boolean formula;
    @XmlAttribute
    protected Boolean formulaRange;
    @XmlAttribute
    protected Boolean unlockedFormula;
    @XmlAttribute
    protected Boolean emptyCellReference;
    @XmlAttribute
    protected Boolean listDataValidation;
    @XmlAttribute
    protected Boolean calculatedColumn;

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

    /**
     * Gets the value of the evalError property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEvalError() {
        if (evalError == null) {
            return false;
        } else {
            return evalError;
        }
    }

    /**
     * Sets the value of the evalError property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEvalError(Boolean value) {
        this.evalError = value;
    }

    /**
     * Gets the value of the twoDigitTextYear property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTwoDigitTextYear() {
        if (twoDigitTextYear == null) {
            return false;
        } else {
            return twoDigitTextYear;
        }
    }

    /**
     * Sets the value of the twoDigitTextYear property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTwoDigitTextYear(Boolean value) {
        this.twoDigitTextYear = value;
    }

    /**
     * Gets the value of the numberStoredAsText property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNumberStoredAsText() {
        if (numberStoredAsText == null) {
            return false;
        } else {
            return numberStoredAsText;
        }
    }

    /**
     * Sets the value of the numberStoredAsText property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNumberStoredAsText(Boolean value) {
        this.numberStoredAsText = value;
    }

    /**
     * Gets the value of the formula property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFormula() {
        if (formula == null) {
            return false;
        } else {
            return formula;
        }
    }

    /**
     * Sets the value of the formula property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFormula(Boolean value) {
        this.formula = value;
    }

    /**
     * Gets the value of the formulaRange property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFormulaRange() {
        if (formulaRange == null) {
            return false;
        } else {
            return formulaRange;
        }
    }

    /**
     * Sets the value of the formulaRange property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFormulaRange(Boolean value) {
        this.formulaRange = value;
    }

    /**
     * Gets the value of the unlockedFormula property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUnlockedFormula() {
        if (unlockedFormula == null) {
            return false;
        } else {
            return unlockedFormula;
        }
    }

    /**
     * Sets the value of the unlockedFormula property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUnlockedFormula(Boolean value) {
        this.unlockedFormula = value;
    }

    /**
     * Gets the value of the emptyCellReference property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEmptyCellReference() {
        if (emptyCellReference == null) {
            return false;
        } else {
            return emptyCellReference;
        }
    }

    /**
     * Sets the value of the emptyCellReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEmptyCellReference(Boolean value) {
        this.emptyCellReference = value;
    }

    /**
     * Gets the value of the listDataValidation property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isListDataValidation() {
        if (listDataValidation == null) {
            return false;
        } else {
            return listDataValidation;
        }
    }

    /**
     * Sets the value of the listDataValidation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setListDataValidation(Boolean value) {
        this.listDataValidation = value;
    }

    /**
     * Gets the value of the calculatedColumn property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCalculatedColumn() {
        if (calculatedColumn == null) {
            return false;
        } else {
            return calculatedColumn;
        }
    }

    /**
     * Sets the value of the calculatedColumn property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCalculatedColumn(Boolean value) {
        this.calculatedColumn = value;
    }

}
