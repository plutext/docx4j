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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_PivotCacheDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PivotCacheDefinition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cacheSource" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CacheSource"/>
 *         &lt;element name="cacheFields" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CacheFields"/>
 *         &lt;element name="cacheHierarchies" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CacheHierarchies" minOccurs="0"/>
 *         &lt;element name="kpis" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PCDKPIs" minOccurs="0"/>
 *         &lt;element name="tupleCache" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_TupleCache" minOccurs="0"/>
 *         &lt;element name="calculatedItems" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CalculatedItems" minOccurs="0"/>
 *         &lt;element name="calculatedMembers" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CalculatedMembers" minOccurs="0"/>
 *         &lt;element name="dimensions" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Dimensions" minOccurs="0"/>
 *         &lt;element name="measureGroups" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_MeasureGroups" minOccurs="0"/>
 *         &lt;element name="maps" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_MeasureDimensionMaps" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}id"/>
 *       &lt;attribute name="invalid" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="saveData" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="refreshOnLoad" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="optimizeMemory" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="enableRefresh" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="refreshedBy" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute name="refreshedDate" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="backgroundQuery" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="missingItemsLimit" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="createdVersion" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="0" />
 *       &lt;attribute name="refreshedVersion" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="0" />
 *       &lt;attribute name="minRefreshableVersion" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="0" />
 *       &lt;attribute name="recordCount" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="upgradeOnRefresh" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="tupleCache" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="supportSubquery" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="supportAdvancedDrill" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PivotCacheDefinition", propOrder = {
    "cacheSource",
    "cacheFields",
    "cacheHierarchies",
    "kpis",
    "tupleCache",
    "calculatedItems",
    "calculatedMembers",
    "dimensions",
    "measureGroups",
    "maps",
    "extLst"
})
@XmlRootElement(name = "pivotCacheDefinition")
public class CTPivotCacheDefinition {

    @XmlElement(required = true)
    protected CTCacheSource cacheSource;
    @XmlElement(required = true)
    protected CTCacheFields cacheFields;
    protected CTCacheHierarchies cacheHierarchies;
    protected CTPCDKPIs kpis;
    protected CTTupleCache tupleCache;
    protected CTCalculatedItems calculatedItems;
    protected CTCalculatedMembers calculatedMembers;
    protected CTDimensions dimensions;
    protected CTMeasureGroups measureGroups;
    protected CTMeasureDimensionMaps maps;
    protected CTExtensionList extLst;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String id;
    @XmlAttribute
    protected Boolean invalid;
    @XmlAttribute
    protected Boolean saveData;
    @XmlAttribute
    protected Boolean refreshOnLoad;
    @XmlAttribute
    protected Boolean optimizeMemory;
    @XmlAttribute
    protected Boolean enableRefresh;
    @XmlAttribute
    protected String refreshedBy;
    @XmlAttribute
    protected Double refreshedDate;
    @XmlAttribute
    protected Boolean backgroundQuery;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long missingItemsLimit;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedByte")
    protected Short createdVersion;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedByte")
    protected Short refreshedVersion;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedByte")
    protected Short minRefreshableVersion;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long recordCount;
    @XmlAttribute
    protected Boolean upgradeOnRefresh;
    @XmlAttribute(name = "tupleCache")
    protected Boolean tupleCacheQ;
    @XmlAttribute
    protected Boolean supportSubquery;
    @XmlAttribute
    protected Boolean supportAdvancedDrill;

    /**
     * Gets the value of the cacheSource property.
     * 
     * @return
     *     possible object is
     *     {@link CTCacheSource }
     *     
     */
    public CTCacheSource getCacheSource() {
        return cacheSource;
    }

    /**
     * Sets the value of the cacheSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCacheSource }
     *     
     */
    public void setCacheSource(CTCacheSource value) {
        this.cacheSource = value;
    }

    /**
     * Gets the value of the cacheFields property.
     * 
     * @return
     *     possible object is
     *     {@link CTCacheFields }
     *     
     */
    public CTCacheFields getCacheFields() {
        return cacheFields;
    }

    /**
     * Sets the value of the cacheFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCacheFields }
     *     
     */
    public void setCacheFields(CTCacheFields value) {
        this.cacheFields = value;
    }

    /**
     * Gets the value of the cacheHierarchies property.
     * 
     * @return
     *     possible object is
     *     {@link CTCacheHierarchies }
     *     
     */
    public CTCacheHierarchies getCacheHierarchies() {
        return cacheHierarchies;
    }

    /**
     * Sets the value of the cacheHierarchies property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCacheHierarchies }
     *     
     */
    public void setCacheHierarchies(CTCacheHierarchies value) {
        this.cacheHierarchies = value;
    }

    /**
     * Gets the value of the kpis property.
     * 
     * @return
     *     possible object is
     *     {@link CTPCDKPIs }
     *     
     */
    public CTPCDKPIs getKpis() {
        return kpis;
    }

    /**
     * Sets the value of the kpis property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPCDKPIs }
     *     
     */
    public void setKpis(CTPCDKPIs value) {
        this.kpis = value;
    }

    /**
     * Gets the value of the tupleCache property.
     * 
     * @return
     *     possible object is
     *     {@link CTTupleCache }
     *     
     */
    public CTTupleCache getTupleCache() {
        return tupleCache;
    }

    /**
     * Sets the value of the tupleCache property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTupleCache }
     *     
     */
    public void setTupleCache(CTTupleCache value) {
        this.tupleCache = value;
    }

    /**
     * Gets the value of the calculatedItems property.
     * 
     * @return
     *     possible object is
     *     {@link CTCalculatedItems }
     *     
     */
    public CTCalculatedItems getCalculatedItems() {
        return calculatedItems;
    }

    /**
     * Sets the value of the calculatedItems property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCalculatedItems }
     *     
     */
    public void setCalculatedItems(CTCalculatedItems value) {
        this.calculatedItems = value;
    }

    /**
     * Gets the value of the calculatedMembers property.
     * 
     * @return
     *     possible object is
     *     {@link CTCalculatedMembers }
     *     
     */
    public CTCalculatedMembers getCalculatedMembers() {
        return calculatedMembers;
    }

    /**
     * Sets the value of the calculatedMembers property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCalculatedMembers }
     *     
     */
    public void setCalculatedMembers(CTCalculatedMembers value) {
        this.calculatedMembers = value;
    }

    /**
     * Gets the value of the dimensions property.
     * 
     * @return
     *     possible object is
     *     {@link CTDimensions }
     *     
     */
    public CTDimensions getDimensions() {
        return dimensions;
    }

    /**
     * Sets the value of the dimensions property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDimensions }
     *     
     */
    public void setDimensions(CTDimensions value) {
        this.dimensions = value;
    }

    /**
     * Gets the value of the measureGroups property.
     * 
     * @return
     *     possible object is
     *     {@link CTMeasureGroups }
     *     
     */
    public CTMeasureGroups getMeasureGroups() {
        return measureGroups;
    }

    /**
     * Sets the value of the measureGroups property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMeasureGroups }
     *     
     */
    public void setMeasureGroups(CTMeasureGroups value) {
        this.measureGroups = value;
    }

    /**
     * Gets the value of the maps property.
     * 
     * @return
     *     possible object is
     *     {@link CTMeasureDimensionMaps }
     *     
     */
    public CTMeasureDimensionMaps getMaps() {
        return maps;
    }

    /**
     * Sets the value of the maps property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMeasureDimensionMaps }
     *     
     */
    public void setMaps(CTMeasureDimensionMaps value) {
        this.maps = value;
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
     * Relationship Identifier
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the invalid property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isInvalid() {
        if (invalid == null) {
            return false;
        } else {
            return invalid;
        }
    }

    /**
     * Sets the value of the invalid property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInvalid(Boolean value) {
        this.invalid = value;
    }

    /**
     * Gets the value of the saveData property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSaveData() {
        if (saveData == null) {
            return true;
        } else {
            return saveData;
        }
    }

    /**
     * Sets the value of the saveData property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSaveData(Boolean value) {
        this.saveData = value;
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
     * Gets the value of the optimizeMemory property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOptimizeMemory() {
        if (optimizeMemory == null) {
            return false;
        } else {
            return optimizeMemory;
        }
    }

    /**
     * Sets the value of the optimizeMemory property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOptimizeMemory(Boolean value) {
        this.optimizeMemory = value;
    }

    /**
     * Gets the value of the enableRefresh property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEnableRefresh() {
        if (enableRefresh == null) {
            return true;
        } else {
            return enableRefresh;
        }
    }

    /**
     * Sets the value of the enableRefresh property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnableRefresh(Boolean value) {
        this.enableRefresh = value;
    }

    /**
     * Gets the value of the refreshedBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefreshedBy() {
        return refreshedBy;
    }

    /**
     * Sets the value of the refreshedBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefreshedBy(String value) {
        this.refreshedBy = value;
    }

    /**
     * Gets the value of the refreshedDate property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getRefreshedDate() {
        return refreshedDate;
    }

    /**
     * Sets the value of the refreshedDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setRefreshedDate(Double value) {
        this.refreshedDate = value;
    }

    /**
     * Gets the value of the backgroundQuery property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isBackgroundQuery() {
        if (backgroundQuery == null) {
            return false;
        } else {
            return backgroundQuery;
        }
    }

    /**
     * Sets the value of the backgroundQuery property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBackgroundQuery(Boolean value) {
        this.backgroundQuery = value;
    }

    /**
     * Gets the value of the missingItemsLimit property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getMissingItemsLimit() {
        return missingItemsLimit;
    }

    /**
     * Sets the value of the missingItemsLimit property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setMissingItemsLimit(Long value) {
        this.missingItemsLimit = value;
    }

    /**
     * Gets the value of the createdVersion property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public short getCreatedVersion() {
        if (createdVersion == null) {
            return ((short) 0);
        } else {
            return createdVersion;
        }
    }

    /**
     * Sets the value of the createdVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setCreatedVersion(Short value) {
        this.createdVersion = value;
    }

    /**
     * Gets the value of the refreshedVersion property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public short getRefreshedVersion() {
        if (refreshedVersion == null) {
            return ((short) 0);
        } else {
            return refreshedVersion;
        }
    }

    /**
     * Sets the value of the refreshedVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setRefreshedVersion(Short value) {
        this.refreshedVersion = value;
    }

    /**
     * Gets the value of the minRefreshableVersion property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public short getMinRefreshableVersion() {
        if (minRefreshableVersion == null) {
            return ((short) 0);
        } else {
            return minRefreshableVersion;
        }
    }

    /**
     * Sets the value of the minRefreshableVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setMinRefreshableVersion(Short value) {
        this.minRefreshableVersion = value;
    }

    /**
     * Gets the value of the recordCount property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getRecordCount() {
        return recordCount;
    }

    /**
     * Sets the value of the recordCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRecordCount(Long value) {
        this.recordCount = value;
    }

    /**
     * Gets the value of the upgradeOnRefresh property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUpgradeOnRefresh() {
        if (upgradeOnRefresh == null) {
            return false;
        } else {
            return upgradeOnRefresh;
        }
    }

    /**
     * Sets the value of the upgradeOnRefresh property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUpgradeOnRefresh(Boolean value) {
        this.upgradeOnRefresh = value;
    }

    /**
     * Gets the value of the tupleCacheQ property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTupleCacheQ() {
        if (tupleCacheQ == null) {
            return false;
        } else {
            return tupleCacheQ;
        }
    }

    /**
     * Sets the value of the tupleCacheQ property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTupleCacheQ(Boolean value) {
        this.tupleCacheQ = value;
    }

    /**
     * Gets the value of the supportSubquery property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSupportSubquery() {
        if (supportSubquery == null) {
            return false;
        } else {
            return supportSubquery;
        }
    }

    /**
     * Sets the value of the supportSubquery property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSupportSubquery(Boolean value) {
        this.supportSubquery = value;
    }

    /**
     * Gets the value of the supportAdvancedDrill property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSupportAdvancedDrill() {
        if (supportAdvancedDrill == null) {
            return false;
        } else {
            return supportAdvancedDrill;
        }
    }

    /**
     * Sets the value of the supportAdvancedDrill property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSupportAdvancedDrill(Boolean value) {
        this.supportAdvancedDrill = value;
    }

}
