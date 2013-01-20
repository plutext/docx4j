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
 * <p>Java class for CT_RevisionDefinedName complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RevisionDefinedName">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="formula" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Formula" minOccurs="0"/>
 *         &lt;element name="oldFormula" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Formula" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}AG_RevData"/>
 *       &lt;attribute name="localSheetId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="customView" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="name" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="function" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="oldFunction" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="functionGroupId" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" />
 *       &lt;attribute name="oldFunctionGroupId" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" />
 *       &lt;attribute name="shortcutKey" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" />
 *       &lt;attribute name="oldShortcutKey" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" />
 *       &lt;attribute name="hidden" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="oldHidden" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="customMenu" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="oldCustomMenu" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="description" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="oldDescription" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="help" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="oldHelp" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="statusBar" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="oldStatusBar" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="comment" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="oldComment" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_RevisionDefinedName", propOrder = {
    "formula",
    "oldFormula",
    "extLst"
})
public class CTRevisionDefinedName implements Child
{

    protected String formula;
    protected String oldFormula;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "localSheetId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long localSheetId;
    @XmlAttribute(name = "customView")
    protected Boolean customView;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "function")
    protected Boolean function;
    @XmlAttribute(name = "oldFunction")
    protected Boolean oldFunction;
    @XmlAttribute(name = "functionGroupId")
    @XmlSchemaType(name = "unsignedByte")
    protected Short functionGroupId;
    @XmlAttribute(name = "oldFunctionGroupId")
    @XmlSchemaType(name = "unsignedByte")
    protected Short oldFunctionGroupId;
    @XmlAttribute(name = "shortcutKey")
    @XmlSchemaType(name = "unsignedByte")
    protected Short shortcutKey;
    @XmlAttribute(name = "oldShortcutKey")
    @XmlSchemaType(name = "unsignedByte")
    protected Short oldShortcutKey;
    @XmlAttribute(name = "hidden")
    protected Boolean hidden;
    @XmlAttribute(name = "oldHidden")
    protected Boolean oldHidden;
    @XmlAttribute(name = "customMenu")
    protected String customMenu;
    @XmlAttribute(name = "oldCustomMenu")
    protected String oldCustomMenu;
    @XmlAttribute(name = "description")
    protected String description;
    @XmlAttribute(name = "oldDescription")
    protected String oldDescription;
    @XmlAttribute(name = "help")
    protected String help;
    @XmlAttribute(name = "oldHelp")
    protected String oldHelp;
    @XmlAttribute(name = "statusBar")
    protected String statusBar;
    @XmlAttribute(name = "oldStatusBar")
    protected String oldStatusBar;
    @XmlAttribute(name = "comment")
    protected String comment;
    @XmlAttribute(name = "oldComment")
    protected String oldComment;
    @XmlAttribute(name = "rId", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long rId;
    @XmlAttribute(name = "ua")
    protected Boolean ua;
    @XmlAttribute(name = "ra")
    protected Boolean ra;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the formula property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormula() {
        return formula;
    }

    /**
     * Sets the value of the formula property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormula(String value) {
        this.formula = value;
    }

    /**
     * Gets the value of the oldFormula property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOldFormula() {
        return oldFormula;
    }

    /**
     * Sets the value of the oldFormula property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOldFormula(String value) {
        this.oldFormula = value;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
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
     * Gets the value of the customView property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCustomView() {
        if (customView == null) {
            return false;
        } else {
            return customView;
        }
    }

    /**
     * Sets the value of the customView property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCustomView(Boolean value) {
        this.customView = value;
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
     * Gets the value of the oldFunction property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOldFunction() {
        if (oldFunction == null) {
            return false;
        } else {
            return oldFunction;
        }
    }

    /**
     * Sets the value of the oldFunction property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOldFunction(Boolean value) {
        this.oldFunction = value;
    }

    /**
     * Gets the value of the functionGroupId property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getFunctionGroupId() {
        return functionGroupId;
    }

    /**
     * Sets the value of the functionGroupId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setFunctionGroupId(Short value) {
        this.functionGroupId = value;
    }

    /**
     * Gets the value of the oldFunctionGroupId property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getOldFunctionGroupId() {
        return oldFunctionGroupId;
    }

    /**
     * Sets the value of the oldFunctionGroupId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setOldFunctionGroupId(Short value) {
        this.oldFunctionGroupId = value;
    }

    /**
     * Gets the value of the shortcutKey property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getShortcutKey() {
        return shortcutKey;
    }

    /**
     * Sets the value of the shortcutKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setShortcutKey(Short value) {
        this.shortcutKey = value;
    }

    /**
     * Gets the value of the oldShortcutKey property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getOldShortcutKey() {
        return oldShortcutKey;
    }

    /**
     * Sets the value of the oldShortcutKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setOldShortcutKey(Short value) {
        this.oldShortcutKey = value;
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
     * Gets the value of the oldHidden property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOldHidden() {
        if (oldHidden == null) {
            return false;
        } else {
            return oldHidden;
        }
    }

    /**
     * Sets the value of the oldHidden property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOldHidden(Boolean value) {
        this.oldHidden = value;
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
     * Gets the value of the oldCustomMenu property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOldCustomMenu() {
        return oldCustomMenu;
    }

    /**
     * Sets the value of the oldCustomMenu property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOldCustomMenu(String value) {
        this.oldCustomMenu = value;
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
     * Gets the value of the oldDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOldDescription() {
        return oldDescription;
    }

    /**
     * Sets the value of the oldDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOldDescription(String value) {
        this.oldDescription = value;
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
     * Gets the value of the oldHelp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOldHelp() {
        return oldHelp;
    }

    /**
     * Sets the value of the oldHelp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOldHelp(String value) {
        this.oldHelp = value;
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
     * Gets the value of the oldStatusBar property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOldStatusBar() {
        return oldStatusBar;
    }

    /**
     * Sets the value of the oldStatusBar property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOldStatusBar(String value) {
        this.oldStatusBar = value;
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
     * Gets the value of the oldComment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOldComment() {
        return oldComment;
    }

    /**
     * Sets the value of the oldComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOldComment(String value) {
        this.oldComment = value;
    }

    /**
     * Gets the value of the rId property.
     * 
     */
    public long getRId() {
        return rId;
    }

    /**
     * Sets the value of the rId property.
     * 
     */
    public void setRId(long value) {
        this.rId = value;
    }

    /**
     * Gets the value of the ua property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUa() {
        if (ua == null) {
            return false;
        } else {
            return ua;
        }
    }

    /**
     * Sets the value of the ua property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUa(Boolean value) {
        this.ua = value;
    }

    /**
     * Gets the value of the ra property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRa() {
        if (ra == null) {
            return false;
        } else {
            return ra;
        }
    }

    /**
     * Sets the value of the ra property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRa(Boolean value) {
        this.ra = value;
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
