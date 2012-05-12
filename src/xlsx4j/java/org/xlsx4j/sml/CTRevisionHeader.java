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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for CT_RevisionHeader complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RevisionHeader">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sheetIdMap" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_SheetIdMap"/>
 *         &lt;element name="reviewedList" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ReviewedRevisions" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="guid" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Guid" />
 *       &lt;attribute name="dateTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="maxSheetId" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="userName" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Xstring" />
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}id use="required""/>
 *       &lt;attribute name="minRId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="maxRId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_RevisionHeader", propOrder = {
    "sheetIdMap",
    "reviewedList",
    "extLst"
})
public class CTRevisionHeader {

    @XmlElement(required = true)
    protected CTSheetIdMap sheetIdMap;
    protected CTReviewedRevisions reviewedList;
    protected CTExtensionList extLst;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String guid;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateTime;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long maxSheetId;
    @XmlAttribute(required = true)
    protected String userName;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships", required = true)
    protected String id;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long minRId;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long maxRId;

    /**
     * Gets the value of the sheetIdMap property.
     * 
     * @return
     *     possible object is
     *     {@link CTSheetIdMap }
     *     
     */
    public CTSheetIdMap getSheetIdMap() {
        return sheetIdMap;
    }

    /**
     * Sets the value of the sheetIdMap property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTSheetIdMap }
     *     
     */
    public void setSheetIdMap(CTSheetIdMap value) {
        this.sheetIdMap = value;
    }

    /**
     * Gets the value of the reviewedList property.
     * 
     * @return
     *     possible object is
     *     {@link CTReviewedRevisions }
     *     
     */
    public CTReviewedRevisions getReviewedList() {
        return reviewedList;
    }

    /**
     * Sets the value of the reviewedList property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTReviewedRevisions }
     *     
     */
    public void setReviewedList(CTReviewedRevisions value) {
        this.reviewedList = value;
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
     * Gets the value of the guid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGuid() {
        return guid;
    }

    /**
     * Sets the value of the guid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGuid(String value) {
        this.guid = value;
    }

    /**
     * Gets the value of the dateTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateTime() {
        return dateTime;
    }

    /**
     * Sets the value of the dateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateTime(XMLGregorianCalendar value) {
        this.dateTime = value;
    }

    /**
     * Gets the value of the maxSheetId property.
     * 
     */
    public long getMaxSheetId() {
        return maxSheetId;
    }

    /**
     * Sets the value of the maxSheetId property.
     * 
     */
    public void setMaxSheetId(long value) {
        this.maxSheetId = value;
    }

    /**
     * Gets the value of the userName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the value of the userName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserName(String value) {
        this.userName = value;
    }

    /**
     * Relationship ID
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
     * Gets the value of the minRId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getMinRId() {
        return minRId;
    }

    /**
     * Sets the value of the minRId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setMinRId(Long value) {
        this.minRId = value;
    }

    /**
     * Gets the value of the maxRId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getMaxRId() {
        return maxRId;
    }

    /**
     * Sets the value of the maxRId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setMaxRId(Long value) {
        this.maxRId = value;
    }

}
