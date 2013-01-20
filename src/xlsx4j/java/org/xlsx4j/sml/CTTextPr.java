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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_TextPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TextPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="textFields" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_TextFields" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="prompt" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="fileType" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_FileType" default="win" />
 *       &lt;attribute name="codePage" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="1252" />
 *       &lt;attribute name="characterSet" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="firstRow" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="1" />
 *       &lt;attribute name="sourceFile" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" default="" />
 *       &lt;attribute name="delimited" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="decimal" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" default="." />
 *       &lt;attribute name="thousands" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" default="," />
 *       &lt;attribute name="tab" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="space" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="comma" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="semicolon" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="consecutive" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="qualifier" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Qualifier" default="doubleQuote" />
 *       &lt;attribute name="delimiter" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TextPr", propOrder = {
    "textFields"
})
public class CTTextPr implements Child
{

    protected CTTextFields textFields;
    @XmlAttribute(name = "prompt")
    protected Boolean prompt;
    @XmlAttribute(name = "fileType")
    protected STFileType fileType;
    @XmlAttribute(name = "codePage")
    @XmlSchemaType(name = "unsignedInt")
    protected Long codePage;
    @XmlAttribute(name = "characterSet")
    protected String characterSet;
    @XmlAttribute(name = "firstRow")
    @XmlSchemaType(name = "unsignedInt")
    protected Long firstRow;
    @XmlAttribute(name = "sourceFile")
    protected String sourceFile;
    @XmlAttribute(name = "delimited")
    protected Boolean delimited;
    @XmlAttribute(name = "decimal")
    protected String decimal;
    @XmlAttribute(name = "thousands")
    protected String thousands;
    @XmlAttribute(name = "tab")
    protected Boolean tab;
    @XmlAttribute(name = "space")
    protected Boolean space;
    @XmlAttribute(name = "comma")
    protected Boolean comma;
    @XmlAttribute(name = "semicolon")
    protected Boolean semicolon;
    @XmlAttribute(name = "consecutive")
    protected Boolean consecutive;
    @XmlAttribute(name = "qualifier")
    protected STQualifier qualifier;
    @XmlAttribute(name = "delimiter")
    protected String delimiter;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the textFields property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextFields }
     *     
     */
    public CTTextFields getTextFields() {
        return textFields;
    }

    /**
     * Sets the value of the textFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextFields }
     *     
     */
    public void setTextFields(CTTextFields value) {
        this.textFields = value;
    }

    /**
     * Gets the value of the prompt property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPrompt() {
        if (prompt == null) {
            return true;
        } else {
            return prompt;
        }
    }

    /**
     * Sets the value of the prompt property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPrompt(Boolean value) {
        this.prompt = value;
    }

    /**
     * Gets the value of the fileType property.
     * 
     * @return
     *     possible object is
     *     {@link STFileType }
     *     
     */
    public STFileType getFileType() {
        if (fileType == null) {
            return STFileType.WIN;
        } else {
            return fileType;
        }
    }

    /**
     * Sets the value of the fileType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STFileType }
     *     
     */
    public void setFileType(STFileType value) {
        this.fileType = value;
    }

    /**
     * Gets the value of the codePage property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getCodePage() {
        if (codePage == null) {
            return  1252L;
        } else {
            return codePage;
        }
    }

    /**
     * Sets the value of the codePage property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCodePage(Long value) {
        this.codePage = value;
    }

    /**
     * Gets the value of the characterSet property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCharacterSet() {
        return characterSet;
    }

    /**
     * Sets the value of the characterSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCharacterSet(String value) {
        this.characterSet = value;
    }

    /**
     * Gets the value of the firstRow property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getFirstRow() {
        if (firstRow == null) {
            return  1L;
        } else {
            return firstRow;
        }
    }

    /**
     * Sets the value of the firstRow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setFirstRow(Long value) {
        this.firstRow = value;
    }

    /**
     * Gets the value of the sourceFile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceFile() {
        if (sourceFile == null) {
            return "";
        } else {
            return sourceFile;
        }
    }

    /**
     * Sets the value of the sourceFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceFile(String value) {
        this.sourceFile = value;
    }

    /**
     * Gets the value of the delimited property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDelimited() {
        if (delimited == null) {
            return true;
        } else {
            return delimited;
        }
    }

    /**
     * Sets the value of the delimited property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDelimited(Boolean value) {
        this.delimited = value;
    }

    /**
     * Gets the value of the decimal property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDecimal() {
        if (decimal == null) {
            return ".";
        } else {
            return decimal;
        }
    }

    /**
     * Sets the value of the decimal property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDecimal(String value) {
        this.decimal = value;
    }

    /**
     * Gets the value of the thousands property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThousands() {
        if (thousands == null) {
            return ",";
        } else {
            return thousands;
        }
    }

    /**
     * Sets the value of the thousands property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThousands(String value) {
        this.thousands = value;
    }

    /**
     * Gets the value of the tab property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTab() {
        if (tab == null) {
            return true;
        } else {
            return tab;
        }
    }

    /**
     * Sets the value of the tab property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTab(Boolean value) {
        this.tab = value;
    }

    /**
     * Gets the value of the space property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSpace() {
        if (space == null) {
            return false;
        } else {
            return space;
        }
    }

    /**
     * Sets the value of the space property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSpace(Boolean value) {
        this.space = value;
    }

    /**
     * Gets the value of the comma property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isComma() {
        if (comma == null) {
            return false;
        } else {
            return comma;
        }
    }

    /**
     * Sets the value of the comma property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setComma(Boolean value) {
        this.comma = value;
    }

    /**
     * Gets the value of the semicolon property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSemicolon() {
        if (semicolon == null) {
            return false;
        } else {
            return semicolon;
        }
    }

    /**
     * Sets the value of the semicolon property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSemicolon(Boolean value) {
        this.semicolon = value;
    }

    /**
     * Gets the value of the consecutive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isConsecutive() {
        if (consecutive == null) {
            return false;
        } else {
            return consecutive;
        }
    }

    /**
     * Sets the value of the consecutive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setConsecutive(Boolean value) {
        this.consecutive = value;
    }

    /**
     * Gets the value of the qualifier property.
     * 
     * @return
     *     possible object is
     *     {@link STQualifier }
     *     
     */
    public STQualifier getQualifier() {
        if (qualifier == null) {
            return STQualifier.DOUBLE_QUOTE;
        } else {
            return qualifier;
        }
    }

    /**
     * Sets the value of the qualifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link STQualifier }
     *     
     */
    public void setQualifier(STQualifier value) {
        this.qualifier = value;
    }

    /**
     * Gets the value of the delimiter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Sets the value of the delimiter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDelimiter(String value) {
        this.delimiter = value;
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
