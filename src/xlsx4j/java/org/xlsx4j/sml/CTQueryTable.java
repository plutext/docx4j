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
 * <p>Java class for CT_QueryTable complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_QueryTable">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="queryTableRefresh" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_QueryTableRefresh" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}AG_AutoFormat"/>
 *       &lt;attribute name="name" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="headers" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="rowNumbers" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="disableRefresh" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="backgroundRefresh" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="firstBackgroundRefresh" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="refreshOnLoad" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="growShrinkType" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_GrowShrinkType" default="insertDelete" />
 *       &lt;attribute name="fillFormulas" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="removeDataOnSave" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="disableEdit" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="preserveFormatting" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="adjustColumnWidth" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="intermediate" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="connectionId" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_QueryTable", propOrder = {
    "queryTableRefresh",
    "extLst"
})
public class CTQueryTable implements Child
{

    protected CTQueryTableRefresh queryTableRefresh;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "headers")
    protected Boolean headers;
    @XmlAttribute(name = "rowNumbers")
    protected Boolean rowNumbers;
    @XmlAttribute(name = "disableRefresh")
    protected Boolean disableRefresh;
    @XmlAttribute(name = "backgroundRefresh")
    protected Boolean backgroundRefresh;
    @XmlAttribute(name = "firstBackgroundRefresh")
    protected Boolean firstBackgroundRefresh;
    @XmlAttribute(name = "refreshOnLoad")
    protected Boolean refreshOnLoad;
    @XmlAttribute(name = "growShrinkType")
    protected STGrowShrinkType growShrinkType;
    @XmlAttribute(name = "fillFormulas")
    protected Boolean fillFormulas;
    @XmlAttribute(name = "removeDataOnSave")
    protected Boolean removeDataOnSave;
    @XmlAttribute(name = "disableEdit")
    protected Boolean disableEdit;
    @XmlAttribute(name = "preserveFormatting")
    protected Boolean preserveFormatting;
    @XmlAttribute(name = "adjustColumnWidth")
    protected Boolean adjustColumnWidth;
    @XmlAttribute(name = "intermediate")
    protected Boolean intermediate;
    @XmlAttribute(name = "connectionId", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long connectionId;
    @XmlAttribute(name = "autoFormatId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long autoFormatId;
    @XmlAttribute(name = "applyNumberFormats")
    protected Boolean applyNumberFormats;
    @XmlAttribute(name = "applyBorderFormats")
    protected Boolean applyBorderFormats;
    @XmlAttribute(name = "applyFontFormats")
    protected Boolean applyFontFormats;
    @XmlAttribute(name = "applyPatternFormats")
    protected Boolean applyPatternFormats;
    @XmlAttribute(name = "applyAlignmentFormats")
    protected Boolean applyAlignmentFormats;
    @XmlAttribute(name = "applyWidthHeightFormats")
    protected Boolean applyWidthHeightFormats;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the queryTableRefresh property.
     * 
     * @return
     *     possible object is
     *     {@link CTQueryTableRefresh }
     *     
     */
    public CTQueryTableRefresh getQueryTableRefresh() {
        return queryTableRefresh;
    }

    /**
     * Sets the value of the queryTableRefresh property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTQueryTableRefresh }
     *     
     */
    public void setQueryTableRefresh(CTQueryTableRefresh value) {
        this.queryTableRefresh = value;
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
     * Gets the value of the headers property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHeaders() {
        if (headers == null) {
            return true;
        } else {
            return headers;
        }
    }

    /**
     * Sets the value of the headers property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHeaders(Boolean value) {
        this.headers = value;
    }

    /**
     * Gets the value of the rowNumbers property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRowNumbers() {
        if (rowNumbers == null) {
            return false;
        } else {
            return rowNumbers;
        }
    }

    /**
     * Sets the value of the rowNumbers property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRowNumbers(Boolean value) {
        this.rowNumbers = value;
    }

    /**
     * Gets the value of the disableRefresh property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDisableRefresh() {
        if (disableRefresh == null) {
            return false;
        } else {
            return disableRefresh;
        }
    }

    /**
     * Sets the value of the disableRefresh property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDisableRefresh(Boolean value) {
        this.disableRefresh = value;
    }

    /**
     * Gets the value of the backgroundRefresh property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isBackgroundRefresh() {
        if (backgroundRefresh == null) {
            return true;
        } else {
            return backgroundRefresh;
        }
    }

    /**
     * Sets the value of the backgroundRefresh property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBackgroundRefresh(Boolean value) {
        this.backgroundRefresh = value;
    }

    /**
     * Gets the value of the firstBackgroundRefresh property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFirstBackgroundRefresh() {
        if (firstBackgroundRefresh == null) {
            return false;
        } else {
            return firstBackgroundRefresh;
        }
    }

    /**
     * Sets the value of the firstBackgroundRefresh property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFirstBackgroundRefresh(Boolean value) {
        this.firstBackgroundRefresh = value;
    }

    /**
     * Gets the value of the refreshOnLoad property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRefreshOnLoad() {
        if (refreshOnLoad == null) {
            return false;
        } else {
            return refreshOnLoad;
        }
    }

    /**
     * Sets the value of the refreshOnLoad property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRefreshOnLoad(Boolean value) {
        this.refreshOnLoad = value;
    }

    /**
     * Gets the value of the growShrinkType property.
     * 
     * @return
     *     possible object is
     *     {@link STGrowShrinkType }
     *     
     */
    public STGrowShrinkType getGrowShrinkType() {
        if (growShrinkType == null) {
            return STGrowShrinkType.INSERT_DELETE;
        } else {
            return growShrinkType;
        }
    }

    /**
     * Sets the value of the growShrinkType property.
     * 
     * @param value
     *     allowed object is
     *     {@link STGrowShrinkType }
     *     
     */
    public void setGrowShrinkType(STGrowShrinkType value) {
        this.growShrinkType = value;
    }

    /**
     * Gets the value of the fillFormulas property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFillFormulas() {
        if (fillFormulas == null) {
            return false;
        } else {
            return fillFormulas;
        }
    }

    /**
     * Sets the value of the fillFormulas property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFillFormulas(Boolean value) {
        this.fillFormulas = value;
    }

    /**
     * Gets the value of the removeDataOnSave property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRemoveDataOnSave() {
        if (removeDataOnSave == null) {
            return false;
        } else {
            return removeDataOnSave;
        }
    }

    /**
     * Sets the value of the removeDataOnSave property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRemoveDataOnSave(Boolean value) {
        this.removeDataOnSave = value;
    }

    /**
     * Gets the value of the disableEdit property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDisableEdit() {
        if (disableEdit == null) {
            return false;
        } else {
            return disableEdit;
        }
    }

    /**
     * Sets the value of the disableEdit property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDisableEdit(Boolean value) {
        this.disableEdit = value;
    }

    /**
     * Gets the value of the preserveFormatting property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPreserveFormatting() {
        if (preserveFormatting == null) {
            return true;
        } else {
            return preserveFormatting;
        }
    }

    /**
     * Sets the value of the preserveFormatting property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPreserveFormatting(Boolean value) {
        this.preserveFormatting = value;
    }

    /**
     * Gets the value of the adjustColumnWidth property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAdjustColumnWidth() {
        if (adjustColumnWidth == null) {
            return true;
        } else {
            return adjustColumnWidth;
        }
    }

    /**
     * Sets the value of the adjustColumnWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAdjustColumnWidth(Boolean value) {
        this.adjustColumnWidth = value;
    }

    /**
     * Gets the value of the intermediate property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIntermediate() {
        if (intermediate == null) {
            return false;
        } else {
            return intermediate;
        }
    }

    /**
     * Sets the value of the intermediate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIntermediate(Boolean value) {
        this.intermediate = value;
    }

    /**
     * Gets the value of the connectionId property.
     * 
     */
    public long getConnectionId() {
        return connectionId;
    }

    /**
     * Sets the value of the connectionId property.
     * 
     */
    public void setConnectionId(long value) {
        this.connectionId = value;
    }

    /**
     * Gets the value of the autoFormatId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getAutoFormatId() {
        return autoFormatId;
    }

    /**
     * Sets the value of the autoFormatId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setAutoFormatId(Long value) {
        this.autoFormatId = value;
    }

    /**
     * Gets the value of the applyNumberFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyNumberFormats() {
        return applyNumberFormats;
    }

    /**
     * Sets the value of the applyNumberFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyNumberFormats(Boolean value) {
        this.applyNumberFormats = value;
    }

    /**
     * Gets the value of the applyBorderFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyBorderFormats() {
        return applyBorderFormats;
    }

    /**
     * Sets the value of the applyBorderFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyBorderFormats(Boolean value) {
        this.applyBorderFormats = value;
    }

    /**
     * Gets the value of the applyFontFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyFontFormats() {
        return applyFontFormats;
    }

    /**
     * Sets the value of the applyFontFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyFontFormats(Boolean value) {
        this.applyFontFormats = value;
    }

    /**
     * Gets the value of the applyPatternFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyPatternFormats() {
        return applyPatternFormats;
    }

    /**
     * Sets the value of the applyPatternFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyPatternFormats(Boolean value) {
        this.applyPatternFormats = value;
    }

    /**
     * Gets the value of the applyAlignmentFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyAlignmentFormats() {
        return applyAlignmentFormats;
    }

    /**
     * Sets the value of the applyAlignmentFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyAlignmentFormats(Boolean value) {
        this.applyAlignmentFormats = value;
    }

    /**
     * Gets the value of the applyWidthHeightFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isApplyWidthHeightFormats() {
        return applyWidthHeightFormats;
    }

    /**
     * Sets the value of the applyWidthHeightFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setApplyWidthHeightFormats(Boolean value) {
        this.applyWidthHeightFormats = value;
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
