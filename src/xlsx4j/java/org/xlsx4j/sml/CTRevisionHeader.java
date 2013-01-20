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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;
import org.jvnet.jaxb2_commons.ppp.Child;


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
 *       &lt;attribute name="guid" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Guid" />
 *       &lt;attribute name="dateTime" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="maxSheetId" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="userName" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
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
public class CTRevisionHeader implements Child
{

    @XmlElement(required = true)
    protected CTSheetIdMap sheetIdMap;
    protected CTReviewedRevisions reviewedList;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "guid", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String guid;
    @XmlAttribute(name = "dateTime", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateTime;
    @XmlAttribute(name = "maxSheetId", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long maxSheetId;
    @XmlAttribute(name = "userName", required = true)
    protected String userName;
    @XmlAttribute(name = "id", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships", required = true)
    protected String id;
    @XmlAttribute(name = "minRId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long minRId;
    @XmlAttribute(name = "maxRId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long maxRId;
    @XmlTransient
    private Object parent;

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
     * Gets the value of the id property.
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
