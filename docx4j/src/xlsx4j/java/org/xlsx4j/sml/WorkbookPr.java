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


/**
 * <p>Java class for CT_WorkbookPr complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_WorkbookPr">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="date1904" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showObjects" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Objects" default="all" />
 *       &lt;attribute name="showBorderUnselectedTables" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="filterPrivacy" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="promptedSolutions" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showInkAnnotation" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="backupFile" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="saveExternalLinkValues" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="updateLinks" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_UpdateLinks" default="userSet" />
 *       &lt;attribute name="codeName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="hidePivotFieldList" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="showPivotChartFilter" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="allowRefreshQuery" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="publishItems" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="checkCompatibility" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="autoCompressPictures" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="refreshAllConnections" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="defaultThemeVersion" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_WorkbookPr")
public class WorkbookPr {

    @XmlAttribute
    protected Boolean date1904;
    @XmlAttribute
    protected STObjects showObjects;
    @XmlAttribute
    protected Boolean showBorderUnselectedTables;
    @XmlAttribute
    protected Boolean filterPrivacy;
    @XmlAttribute
    protected Boolean promptedSolutions;
    @XmlAttribute
    protected Boolean showInkAnnotation;
    @XmlAttribute
    protected Boolean backupFile;
    @XmlAttribute
    protected Boolean saveExternalLinkValues;
    @XmlAttribute
    protected STUpdateLinks updateLinks;
    @XmlAttribute
    protected String codeName;
    @XmlAttribute
    protected Boolean hidePivotFieldList;
    @XmlAttribute
    protected Boolean showPivotChartFilter;
    @XmlAttribute
    protected Boolean allowRefreshQuery;
    @XmlAttribute
    protected Boolean publishItems;
    @XmlAttribute
    protected Boolean checkCompatibility;
    @XmlAttribute
    protected Boolean autoCompressPictures;
    @XmlAttribute
    protected Boolean refreshAllConnections;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long defaultThemeVersion;

    /**
     * Gets the value of the date1904 property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDate1904() {
        if (date1904 == null) {
            return false;
        } else {
            return date1904;
        }
    }

    /**
     * Sets the value of the date1904 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDate1904(Boolean value) {
        this.date1904 = value;
    }

    /**
     * Gets the value of the showObjects property.
     * 
     * @return
     *     possible object is
     *     {@link STObjects }
     *     
     */
    public STObjects getShowObjects() {
        if (showObjects == null) {
            return STObjects.ALL;
        } else {
            return showObjects;
        }
    }

    /**
     * Sets the value of the showObjects property.
     * 
     * @param value
     *     allowed object is
     *     {@link STObjects }
     *     
     */
    public void setShowObjects(STObjects value) {
        this.showObjects = value;
    }

    /**
     * Gets the value of the showBorderUnselectedTables property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowBorderUnselectedTables() {
        if (showBorderUnselectedTables == null) {
            return true;
        } else {
            return showBorderUnselectedTables;
        }
    }

    /**
     * Sets the value of the showBorderUnselectedTables property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowBorderUnselectedTables(Boolean value) {
        this.showBorderUnselectedTables = value;
    }

    /**
     * Gets the value of the filterPrivacy property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFilterPrivacy() {
        if (filterPrivacy == null) {
            return false;
        } else {
            return filterPrivacy;
        }
    }

    /**
     * Sets the value of the filterPrivacy property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFilterPrivacy(Boolean value) {
        this.filterPrivacy = value;
    }

    /**
     * Gets the value of the promptedSolutions property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPromptedSolutions() {
        if (promptedSolutions == null) {
            return false;
        } else {
            return promptedSolutions;
        }
    }

    /**
     * Sets the value of the promptedSolutions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPromptedSolutions(Boolean value) {
        this.promptedSolutions = value;
    }

    /**
     * Gets the value of the showInkAnnotation property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowInkAnnotation() {
        if (showInkAnnotation == null) {
            return true;
        } else {
            return showInkAnnotation;
        }
    }

    /**
     * Sets the value of the showInkAnnotation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowInkAnnotation(Boolean value) {
        this.showInkAnnotation = value;
    }

    /**
     * Gets the value of the backupFile property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isBackupFile() {
        if (backupFile == null) {
            return false;
        } else {
            return backupFile;
        }
    }

    /**
     * Sets the value of the backupFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBackupFile(Boolean value) {
        this.backupFile = value;
    }

    /**
     * Gets the value of the saveExternalLinkValues property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSaveExternalLinkValues() {
        if (saveExternalLinkValues == null) {
            return true;
        } else {
            return saveExternalLinkValues;
        }
    }

    /**
     * Sets the value of the saveExternalLinkValues property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSaveExternalLinkValues(Boolean value) {
        this.saveExternalLinkValues = value;
    }

    /**
     * Gets the value of the updateLinks property.
     * 
     * @return
     *     possible object is
     *     {@link STUpdateLinks }
     *     
     */
    public STUpdateLinks getUpdateLinks() {
        if (updateLinks == null) {
            return STUpdateLinks.USER_SET;
        } else {
            return updateLinks;
        }
    }

    /**
     * Sets the value of the updateLinks property.
     * 
     * @param value
     *     allowed object is
     *     {@link STUpdateLinks }
     *     
     */
    public void setUpdateLinks(STUpdateLinks value) {
        this.updateLinks = value;
    }

    /**
     * Gets the value of the codeName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodeName() {
        return codeName;
    }

    /**
     * Sets the value of the codeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodeName(String value) {
        this.codeName = value;
    }

    /**
     * Gets the value of the hidePivotFieldList property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHidePivotFieldList() {
        if (hidePivotFieldList == null) {
            return false;
        } else {
            return hidePivotFieldList;
        }
    }

    /**
     * Sets the value of the hidePivotFieldList property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHidePivotFieldList(Boolean value) {
        this.hidePivotFieldList = value;
    }

    /**
     * Gets the value of the showPivotChartFilter property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShowPivotChartFilter() {
        if (showPivotChartFilter == null) {
            return false;
        } else {
            return showPivotChartFilter;
        }
    }

    /**
     * Sets the value of the showPivotChartFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShowPivotChartFilter(Boolean value) {
        this.showPivotChartFilter = value;
    }

    /**
     * Gets the value of the allowRefreshQuery property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAllowRefreshQuery() {
        if (allowRefreshQuery == null) {
            return false;
        } else {
            return allowRefreshQuery;
        }
    }

    /**
     * Sets the value of the allowRefreshQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAllowRefreshQuery(Boolean value) {
        this.allowRefreshQuery = value;
    }

    /**
     * Gets the value of the publishItems property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPublishItems() {
        if (publishItems == null) {
            return false;
        } else {
            return publishItems;
        }
    }

    /**
     * Sets the value of the publishItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPublishItems(Boolean value) {
        this.publishItems = value;
    }

    /**
     * Gets the value of the checkCompatibility property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCheckCompatibility() {
        if (checkCompatibility == null) {
            return false;
        } else {
            return checkCompatibility;
        }
    }

    /**
     * Sets the value of the checkCompatibility property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCheckCompatibility(Boolean value) {
        this.checkCompatibility = value;
    }

    /**
     * Gets the value of the autoCompressPictures property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAutoCompressPictures() {
        if (autoCompressPictures == null) {
            return true;
        } else {
            return autoCompressPictures;
        }
    }

    /**
     * Sets the value of the autoCompressPictures property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoCompressPictures(Boolean value) {
        this.autoCompressPictures = value;
    }

    /**
     * Gets the value of the refreshAllConnections property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRefreshAllConnections() {
        if (refreshAllConnections == null) {
            return false;
        } else {
            return refreshAllConnections;
        }
    }

    /**
     * Sets the value of the refreshAllConnections property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRefreshAllConnections(Boolean value) {
        this.refreshAllConnections = value;
    }

    /**
     * Gets the value of the defaultThemeVersion property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDefaultThemeVersion() {
        return defaultThemeVersion;
    }

    /**
     * Sets the value of the defaultThemeVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDefaultThemeVersion(Long value) {
        this.defaultThemeVersion = value;
    }

}
