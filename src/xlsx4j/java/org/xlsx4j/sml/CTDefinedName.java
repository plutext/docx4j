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
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for CT_DefinedName complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DefinedName">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://schemas.openxmlformats.org/spreadsheetml/2006/main>ST_Formula">
 *       &lt;attribute name="name" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="comment" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="customMenu" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="description" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="help" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="statusBar" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="localSheetId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="hidden" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="function" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="vbProcedure" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="xlm" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="functionGroupId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="shortcutKey" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="publishToServer" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="workbookParameter" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DefinedName", propOrder = {
    "value"
})
public class CTDefinedName {

    @XmlValue
    protected String value;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute
    protected String comment;
    @XmlAttribute
    protected String customMenu;
    @XmlAttribute
    protected String description;
    @XmlAttribute
    protected String help;
    @XmlAttribute
    protected String statusBar;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long localSheetId;
    @XmlAttribute
    protected Boolean hidden;
    @XmlAttribute
    protected Boolean function;
    @XmlAttribute
    protected Boolean vbProcedure;
    @XmlAttribute
    protected Boolean xlm;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long functionGroupId;
    @XmlAttribute
    protected String shortcutKey;
    @XmlAttribute
    protected Boolean publishToServer;
    @XmlAttribute
    protected Boolean workbookParameter;

    /**
     * Formula
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

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
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

    /**
     * Gets the value of the customMenu property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomMenu() {
        return customMenu;
    }

    /**
     * Sets the value of the customMenu property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomMenu(String value) {
        this.customMenu = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the help property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHelp() {
        return help;
    }

    /**
     * Sets the value of the help property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHelp(String value) {
        this.help = value;
    }

    /**
     * Gets the value of the statusBar property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatusBar() {
        return statusBar;
    }

    /**
     * Sets the value of the statusBar property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatusBar(String value) {
        this.statusBar = value;
    }

    /**
     * Gets the value of the localSheetId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getLocalSheetId() {
        return localSheetId;
    }

    /**
     * Sets the value of the localSheetId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setLocalSheetId(Long value) {
        this.localSheetId = value;
    }

    /**
     * Gets the value of the hidden property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHidden() {
        if (hidden == null) {
            return false;
        } else {
            return hidden;
        }
    }

    /**
     * Sets the value of the hidden property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHidden(Boolean value) {
        this.hidden = value;
    }

    /**
     * Gets the value of the function property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFunction() {
        if (function == null) {
            return false;
        } else {
            return function;
        }
    }

    /**
     * Sets the value of the function property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFunction(Boolean value) {
        this.function = value;
    }

    /**
     * Gets the value of the vbProcedure property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isVbProcedure() {
        if (vbProcedure == null) {
            return false;
        } else {
            return vbProcedure;
        }
    }

    /**
     * Sets the value of the vbProcedure property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVbProcedure(Boolean value) {
        this.vbProcedure = value;
    }

    /**
     * Gets the value of the xlm property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isXlm() {
        if (xlm == null) {
            return false;
        } else {
            return xlm;
        }
    }

    /**
     * Sets the value of the xlm property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setXlm(Boolean value) {
        this.xlm = value;
    }

    /**
     * Gets the value of the functionGroupId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getFunctionGroupId() {
        return functionGroupId;
    }

    /**
     * Sets the value of the functionGroupId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setFunctionGroupId(Long value) {
        this.functionGroupId = value;
    }

    /**
     * Gets the value of the shortcutKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShortcutKey() {
        return shortcutKey;
    }

    /**
     * Sets the value of the shortcutKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShortcutKey(String value) {
        this.shortcutKey = value;
    }

    /**
     * Gets the value of the publishToServer property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPublishToServer() {
        if (publishToServer == null) {
            return false;
        } else {
            return publishToServer;
        }
    }

    /**
     * Sets the value of the publishToServer property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPublishToServer(Boolean value) {
        this.publishToServer = value;
    }

    /**
     * Gets the value of the workbookParameter property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isWorkbookParameter() {
        if (workbookParameter == null) {
            return false;
        } else {
            return workbookParameter;
        }
    }

    /**
     * Sets the value of the workbookParameter property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setWorkbookParameter(Boolean value) {
        this.workbookParameter = value;
    }

}
