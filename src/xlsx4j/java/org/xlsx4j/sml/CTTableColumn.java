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
 * <p>Java class for CT_TableColumn complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_TableColumn">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="calculatedColumnFormula" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_TableFormula" minOccurs="0"/>
 *         &lt;element name="totalsRowFormula" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_TableFormula" minOccurs="0"/>
 *         &lt;element name="xmlColumnPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_XmlColumnPr" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="uniqueName" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="name" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="totalsRowFunction" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_TotalsRowFunction" default="none" />
 *       &lt;attribute name="totalsRowLabel" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="queryTableFieldId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="headerRowDxfId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DxfId" />
 *       &lt;attribute name="dataDxfId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DxfId" />
 *       &lt;attribute name="totalsRowDxfId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_DxfId" />
 *       &lt;attribute name="headerRowCellStyle" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="dataCellStyle" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="totalsRowCellStyle" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_TableColumn", propOrder = {
    "calculatedColumnFormula",
    "totalsRowFormula",
    "xmlColumnPr",
    "extLst"
})
public class CTTableColumn implements Child
{

    protected CTTableFormula calculatedColumnFormula;
    protected CTTableFormula totalsRowFormula;
    protected CTXmlColumnPr xmlColumnPr;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "id", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long id;
    @XmlAttribute(name = "uniqueName")
    protected String uniqueName;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "totalsRowFunction")
    protected STTotalsRowFunction totalsRowFunction;
    @XmlAttribute(name = "totalsRowLabel")
    protected String totalsRowLabel;
    @XmlAttribute(name = "queryTableFieldId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long queryTableFieldId;
    @XmlAttribute(name = "headerRowDxfId")
    protected Long headerRowDxfId;
    @XmlAttribute(name = "dataDxfId")
    protected Long dataDxfId;
    @XmlAttribute(name = "totalsRowDxfId")
    protected Long totalsRowDxfId;
    @XmlAttribute(name = "headerRowCellStyle")
    protected String headerRowCellStyle;
    @XmlAttribute(name = "dataCellStyle")
    protected String dataCellStyle;
    @XmlAttribute(name = "totalsRowCellStyle")
    protected String totalsRowCellStyle;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the calculatedColumnFormula property.
     * 
     * @return
     *     possible object is
     *     {@link CTTableFormula }
     *     
     */
    public CTTableFormula getCalculatedColumnFormula() {
        return calculatedColumnFormula;
    }

    /**
     * Sets the value of the calculatedColumnFormula property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTableFormula }
     *     
     */
    public void setCalculatedColumnFormula(CTTableFormula value) {
        this.calculatedColumnFormula = value;
    }

    /**
     * Gets the value of the totalsRowFormula property.
     * 
     * @return
     *     possible object is
     *     {@link CTTableFormula }
     *     
     */
    public CTTableFormula getTotalsRowFormula() {
        return totalsRowFormula;
    }

    /**
     * Sets the value of the totalsRowFormula property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTableFormula }
     *     
     */
    public void setTotalsRowFormula(CTTableFormula value) {
        this.totalsRowFormula = value;
    }

    /**
     * Gets the value of the xmlColumnPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTXmlColumnPr }
     *     
     */
    public CTXmlColumnPr getXmlColumnPr() {
        return xmlColumnPr;
    }

    /**
     * Sets the value of the xmlColumnPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTXmlColumnPr }
     *     
     */
    public void setXmlColumnPr(CTXmlColumnPr value) {
        this.xmlColumnPr = value;
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
     * Gets the value of the id property.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(long value) {
        this.id = value;
    }

    /**
     * Gets the value of the uniqueName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUniqueName() {
        return uniqueName;
    }

    /**
     * Sets the value of the uniqueName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUniqueName(String value) {
        this.uniqueName = value;
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
     * Gets the value of the totalsRowFunction property.
     * 
     * @return
     *     possible object is
     *     {@link STTotalsRowFunction }
     *     
     */
    public STTotalsRowFunction getTotalsRowFunction() {
        if (totalsRowFunction == null) {
            return STTotalsRowFunction.NONE;
        } else {
            return totalsRowFunction;
        }
    }

    /**
     * Sets the value of the totalsRowFunction property.
     * 
     * @param value
     *     allowed object is
     *     {@link STTotalsRowFunction }
     *     
     */
    public void setTotalsRowFunction(STTotalsRowFunction value) {
        this.totalsRowFunction = value;
    }

    /**
     * Gets the value of the totalsRowLabel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTotalsRowLabel() {
        return totalsRowLabel;
    }

    /**
     * Sets the value of the totalsRowLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTotalsRowLabel(String value) {
        this.totalsRowLabel = value;
    }

    /**
     * Gets the value of the queryTableFieldId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getQueryTableFieldId() {
        return queryTableFieldId;
    }

    /**
     * Sets the value of the queryTableFieldId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setQueryTableFieldId(Long value) {
        this.queryTableFieldId = value;
    }

    /**
     * Gets the value of the headerRowDxfId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getHeaderRowDxfId() {
        return headerRowDxfId;
    }

    /**
     * Sets the value of the headerRowDxfId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setHeaderRowDxfId(Long value) {
        this.headerRowDxfId = value;
    }

    /**
     * Gets the value of the dataDxfId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDataDxfId() {
        return dataDxfId;
    }

    /**
     * Sets the value of the dataDxfId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDataDxfId(Long value) {
        this.dataDxfId = value;
    }

    /**
     * Gets the value of the totalsRowDxfId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getTotalsRowDxfId() {
        return totalsRowDxfId;
    }

    /**
     * Sets the value of the totalsRowDxfId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setTotalsRowDxfId(Long value) {
        this.totalsRowDxfId = value;
    }

    /**
     * Gets the value of the headerRowCellStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHeaderRowCellStyle() {
        return headerRowCellStyle;
    }

    /**
     * Sets the value of the headerRowCellStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHeaderRowCellStyle(String value) {
        this.headerRowCellStyle = value;
    }

    /**
     * Gets the value of the dataCellStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataCellStyle() {
        return dataCellStyle;
    }

    /**
     * Sets the value of the dataCellStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataCellStyle(String value) {
        this.dataCellStyle = value;
    }

    /**
     * Gets the value of the totalsRowCellStyle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTotalsRowCellStyle() {
        return totalsRowCellStyle;
    }

    /**
     * Sets the value of the totalsRowCellStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTotalsRowCellStyle(String value) {
        this.totalsRowCellStyle = value;
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
