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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.mce.AlternateContent;
import org.docx4j.sharedtypes.STConformanceClass;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Workbook complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Workbook">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fileVersion" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_FileVersion" minOccurs="0"/>
 *         &lt;element name="fileSharing" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_FileSharing" minOccurs="0"/>
 *         &lt;element name="workbookPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_WorkbookPr" minOccurs="0"/>
 *         &lt;element ref="{http://schemas.openxmlformats.org/markup-compatibility/2006}AlternateContent"/&gt;
 *         &lt;element name="workbookProtection" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_WorkbookProtection" minOccurs="0"/>
 *         &lt;element name="bookViews" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_BookViews" minOccurs="0"/>
 *         &lt;element name="sheets" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Sheets"/>
 *         &lt;element name="functionGroups" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_FunctionGroups" minOccurs="0"/>
 *         &lt;element name="externalReferences" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExternalReferences" minOccurs="0"/>
 *         &lt;element name="definedNames" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_DefinedNames" minOccurs="0"/>
 *         &lt;element name="calcPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CalcPr" minOccurs="0"/>
 *         &lt;element name="oleSize" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_OleSize" minOccurs="0"/>
 *         &lt;element name="customWorkbookViews" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CustomWorkbookViews" minOccurs="0"/>
 *         &lt;element name="pivotCaches" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_PivotCaches" minOccurs="0"/>
 *         &lt;element name="smartTagPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SmartTagPr" minOccurs="0"/>
 *         &lt;element name="smartTagTypes" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SmartTagTypes" minOccurs="0"/>
 *         &lt;element name="webPublishing" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_WebPublishing" minOccurs="0"/>
 *         &lt;element name="fileRecoveryPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_FileRecoveryPr" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="webPublishObjects" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_WebPublishObjects" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="conformance" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_ConformanceClass" />
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/markup-compatibility/2006}Ignorable"/&gt;
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Workbook", propOrder = {
    "fileVersion",
    "fileSharing",
    "workbookPr",
    "alternateContent",
    "workbookProtection",
    "bookViews",
    "sheets",
    "functionGroups",
    "externalReferences",
    "definedNames",
    "calcPr",
    "oleSize",
    "customWorkbookViews",
    "pivotCaches",
    "smartTagPr",
    "smartTagTypes",
    "webPublishing",
    "fileRecoveryPr",
    "webPublishObjects",
    "extLst"
})
@XmlRootElement(name = "workbook")
public class Workbook implements Child
{

    protected FileVersion fileVersion;
    protected CTFileSharing fileSharing;
    protected WorkbookPr workbookPr;
    @XmlElement(name = "AlternateContent", namespace = "http://schemas.openxmlformats.org/markup-compatibility/2006", required = true)
    protected AlternateContent alternateContent;    
    protected CTWorkbookProtection workbookProtection;
    protected BookViews bookViews;
    @XmlElement(required = true)
    protected Sheets sheets;
    protected CTFunctionGroups functionGroups;
    protected CTExternalReferences externalReferences;
    protected DefinedNames definedNames;
    protected CTCalcPr calcPr;
    protected CTOleSize oleSize;
    protected CTCustomWorkbookViews customWorkbookViews;
    protected CTPivotCaches pivotCaches;
    protected CTSmartTagPr smartTagPr;
    protected CTSmartTagTypes smartTagTypes;
    protected CTWebPublishing webPublishing;
    protected List<CTFileRecoveryPr> fileRecoveryPr;
    protected CTWebPublishObjects webPublishObjects;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "conformance")
    protected STConformanceClass conformance;
    @XmlAttribute(name = "Ignorable", namespace = "http://schemas.openxmlformats.org/markup-compatibility/2006")
    protected String ignorable;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the fileVersion property.
     * 
     * @return
     *     possible object is
     *     {@link FileVersion }
     *     
     */
    public FileVersion getFileVersion() {
        return fileVersion;
    }

    /**
     * Sets the value of the fileVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link FileVersion }
     *     
     */
    public void setFileVersion(FileVersion value) {
        this.fileVersion = value;
    }

    /**
     * Gets the value of the fileSharing property.
     * 
     * @return
     *     possible object is
     *     {@link CTFileSharing }
     *     
     */
    public CTFileSharing getFileSharing() {
        return fileSharing;
    }

    /**
     * Sets the value of the fileSharing property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFileSharing }
     *     
     */
    public void setFileSharing(CTFileSharing value) {
        this.fileSharing = value;
    }

    /**
     * Gets the value of the workbookPr property.
     * 
     * @return
     *     possible object is
     *     {@link WorkbookPr }
     *     
     */
    public WorkbookPr getWorkbookPr() {
        return workbookPr;
    }

    /**
     * Sets the value of the workbookPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link WorkbookPr }
     *     
     */
    public void setWorkbookPr(WorkbookPr value) {
        this.workbookPr = value;
    }

    /**
     * Gets the value of the alternateContent property.
     * 
     * @return
     *     possible object is
     *     {@link AlternateContent }
     *     
     */
    public AlternateContent getAlternateContent() {
        return alternateContent;
    }

    /**
     * Sets the value of the alternateContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link AlternateContent }
     *     
     */
    public void setAlternateContent(AlternateContent value) {
        this.alternateContent = value;
    }
    
    /**
     * Gets the value of the workbookProtection property.
     * 
     * @return
     *     possible object is
     *     {@link CTWorkbookProtection }
     *     
     */
    public CTWorkbookProtection getWorkbookProtection() {
        return workbookProtection;
    }

    /**
     * Sets the value of the workbookProtection property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWorkbookProtection }
     *     
     */
    public void setWorkbookProtection(CTWorkbookProtection value) {
        this.workbookProtection = value;
    }

    /**
     * Gets the value of the bookViews property.
     * 
     * @return
     *     possible object is
     *     {@link BookViews }
     *     
     */
    public BookViews getBookViews() {
        return bookViews;
    }

    /**
     * Sets the value of the bookViews property.
     * 
     * @param value
     *     allowed object is
     *     {@link BookViews }
     *     
     */
    public void setBookViews(BookViews value) {
        this.bookViews = value;
    }

    /**
     * Gets the value of the sheets property.
     * 
     * @return
     *     possible object is
     *     {@link Sheets }
     *     
     */
    public Sheets getSheets() {
        return sheets;
    }

    /**
     * Sets the value of the sheets property.
     * 
     * @param value
     *     allowed object is
     *     {@link Sheets }
     *     
     */
    public void setSheets(Sheets value) {
        this.sheets = value;
    }

    /**
     * Gets the value of the functionGroups property.
     * 
     * @return
     *     possible object is
     *     {@link CTFunctionGroups }
     *     
     */
    public CTFunctionGroups getFunctionGroups() {
        return functionGroups;
    }

    /**
     * Sets the value of the functionGroups property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTFunctionGroups }
     *     
     */
    public void setFunctionGroups(CTFunctionGroups value) {
        this.functionGroups = value;
    }

    /**
     * Gets the value of the externalReferences property.
     * 
     * @return
     *     possible object is
     *     {@link CTExternalReferences }
     *     
     */
    public CTExternalReferences getExternalReferences() {
        return externalReferences;
    }

    /**
     * Sets the value of the externalReferences property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExternalReferences }
     *     
     */
    public void setExternalReferences(CTExternalReferences value) {
        this.externalReferences = value;
    }

    /**
     * Gets the value of the definedNames property.
     * 
     * @return
     *     possible object is
     *     {@link DefinedNames }
     *     
     */
    public DefinedNames getDefinedNames() {
        return definedNames;
    }

    /**
     * Sets the value of the definedNames property.
     * 
     * @param value
     *     allowed object is
     *     {@link DefinedNames }
     *     
     */
    public void setDefinedNames(DefinedNames value) {
        this.definedNames = value;
    }

    /**
     * Gets the value of the calcPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTCalcPr }
     *     
     */
    public CTCalcPr getCalcPr() {
        return calcPr;
    }

    /**
     * Sets the value of the calcPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCalcPr }
     *     
     */
    public void setCalcPr(CTCalcPr value) {
        this.calcPr = value;
    }

    /**
     * Gets the value of the oleSize property.
     * 
     * @return
     *     possible object is
     *     {@link CTOleSize }
     *     
     */
    public CTOleSize getOleSize() {
        return oleSize;
    }

    /**
     * Sets the value of the oleSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOleSize }
     *     
     */
    public void setOleSize(CTOleSize value) {
        this.oleSize = value;
    }

    /**
     * Gets the value of the customWorkbookViews property.
     * 
     * @return
     *     possible object is
     *     {@link CTCustomWorkbookViews }
     *     
     */
    public CTCustomWorkbookViews getCustomWorkbookViews() {
        return customWorkbookViews;
    }

    /**
     * Sets the value of the customWorkbookViews property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCustomWorkbookViews }
     *     
     */
    public void setCustomWorkbookViews(CTCustomWorkbookViews value) {
        this.customWorkbookViews = value;
    }

    /**
     * Gets the value of the pivotCaches property.
     * 
     * @return
     *     possible object is
     *     {@link CTPivotCaches }
     *     
     */
    public CTPivotCaches getPivotCaches() {
        return pivotCaches;
    }

    /**
     * Sets the value of the pivotCaches property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTPivotCaches }
     *     
     */
    public void setPivotCaches(CTPivotCaches value) {
        this.pivotCaches = value;
    }

    /**
     * Gets the value of the smartTagPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTSmartTagPr }
     *     
     */
    public CTSmartTagPr getSmartTagPr() {
        return smartTagPr;
    }

    /**
     * Sets the value of the smartTagPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSmartTagPr }
     *     
     */
    public void setSmartTagPr(CTSmartTagPr value) {
        this.smartTagPr = value;
    }

    /**
     * Gets the value of the smartTagTypes property.
     * 
     * @return
     *     possible object is
     *     {@link CTSmartTagTypes }
     *     
     */
    public CTSmartTagTypes getSmartTagTypes() {
        return smartTagTypes;
    }

    /**
     * Sets the value of the smartTagTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSmartTagTypes }
     *     
     */
    public void setSmartTagTypes(CTSmartTagTypes value) {
        this.smartTagTypes = value;
    }

    /**
     * Gets the value of the webPublishing property.
     * 
     * @return
     *     possible object is
     *     {@link CTWebPublishing }
     *     
     */
    public CTWebPublishing getWebPublishing() {
        return webPublishing;
    }

    /**
     * Sets the value of the webPublishing property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWebPublishing }
     *     
     */
    public void setWebPublishing(CTWebPublishing value) {
        this.webPublishing = value;
    }

    /**
     * Gets the value of the fileRecoveryPr property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fileRecoveryPr property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFileRecoveryPr().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTFileRecoveryPr }
     * 
     * 
     */
    public List<CTFileRecoveryPr> getFileRecoveryPr() {
        if (fileRecoveryPr == null) {
            fileRecoveryPr = new ArrayList<CTFileRecoveryPr>();
        }
        return this.fileRecoveryPr;
    }

    /**
     * Gets the value of the webPublishObjects property.
     * 
     * @return
     *     possible object is
     *     {@link CTWebPublishObjects }
     *     
     */
    public CTWebPublishObjects getWebPublishObjects() {
        return webPublishObjects;
    }

    /**
     * Sets the value of the webPublishObjects property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWebPublishObjects }
     *     
     */
    public void setWebPublishObjects(CTWebPublishObjects value) {
        this.webPublishObjects = value;
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
     * Gets the value of the conformance property.
     * 
     * @return
     *     possible object is
     *     {@link STConformanceClass }
     *     
     */
    public STConformanceClass getConformance() {
        return conformance;
    }

    /**
     * Sets the value of the conformance property.
     * 
     * @param value
     *     allowed object is
     *     {@link STConformanceClass }
     *     
     */
    public void setConformance(STConformanceClass value) {
        this.conformance = value;
    }

    /**
     * Gets the value of the ignorable property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIgnorable() {
        return ignorable;
    }

    /**
     * Sets the value of the ignorable property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIgnorable(String value) {
        this.ignorable = value;
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
