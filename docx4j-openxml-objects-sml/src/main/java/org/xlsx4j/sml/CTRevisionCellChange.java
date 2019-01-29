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
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_RevisionCellChange complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RevisionCellChange">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="oc" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Cell" minOccurs="0"/>
 *         &lt;element name="nc" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Cell"/>
 *         &lt;element name="odxf" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Dxf" minOccurs="0"/>
 *         &lt;element name="ndxf" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Dxf" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}AG_RevData"/>
 *       &lt;attribute name="sId" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="odxf" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="xfDxf" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="s" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="dxf" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="numFmtId" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_NumFmtId" />
 *       &lt;attribute name="quotePrefix" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="oldQuotePrefix" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="ph" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="oldPh" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="endOfListFormulaUpdate" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_RevisionCellChange", propOrder = {
    "oc",
    "nc",
    "odxf",
    "ndxf",
    "extLst"
})
public class CTRevisionCellChange implements Child
{

    protected Cell oc;
    @XmlElement(required = true)
    protected Cell nc;
    protected CTDxf odxf;
    protected CTDxf ndxf;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "sId", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long sId;
    @XmlAttribute(name = "odxf")
    protected Boolean odxfQ;
    @XmlAttribute(name = "xfDxf")
    protected Boolean xfDxf;
    @XmlAttribute(name = "s")
    protected Boolean s;
    @XmlAttribute(name = "dxf")
    protected Boolean dxf;
    @XmlAttribute(name = "numFmtId")
    protected Long numFmtId;
    @XmlAttribute(name = "quotePrefix")
    protected Boolean quotePrefix;
    @XmlAttribute(name = "oldQuotePrefix")
    protected Boolean oldQuotePrefix;
    @XmlAttribute(name = "ph")
    protected Boolean ph;
    @XmlAttribute(name = "oldPh")
    protected Boolean oldPh;
    @XmlAttribute(name = "endOfListFormulaUpdate")
    protected Boolean endOfListFormulaUpdate;
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
     * Gets the value of the oc property.
     * 
     * @return
     *     possible object is
     *     {@link Cell }
     *     
     */
    public Cell getOc() {
        return oc;
    }

    /**
     * Sets the value of the oc property.
     * 
     * @param value
     *     allowed object is
     *     {@link Cell }
     *     
     */
    public void setOc(Cell value) {
        this.oc = value;
    }

    /**
     * Gets the value of the nc property.
     * 
     * @return
     *     possible object is
     *     {@link Cell }
     *     
     */
    public Cell getNc() {
        return nc;
    }

    /**
     * Sets the value of the nc property.
     * 
     * @param value
     *     allowed object is
     *     {@link Cell }
     *     
     */
    public void setNc(Cell value) {
        this.nc = value;
    }

    /**
     * Gets the value of the odxf property.
     * 
     * @return
     *     possible object is
     *     {@link CTDxf }
     *     
     */
    public CTDxf getOdxf() {
        return odxf;
    }

    /**
     * Sets the value of the odxf property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDxf }
     *     
     */
    public void setOdxf(CTDxf value) {
        this.odxf = value;
    }

    /**
     * Gets the value of the ndxf property.
     * 
     * @return
     *     possible object is
     *     {@link CTDxf }
     *     
     */
    public CTDxf getNdxf() {
        return ndxf;
    }

    /**
     * Sets the value of the ndxf property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDxf }
     *     
     */
    public void setNdxf(CTDxf value) {
        this.ndxf = value;
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
     * Gets the value of the sId property.
     * 
     */
    public long getSId() {
        return sId;
    }

    /**
     * Sets the value of the sId property.
     * 
     */
    public void setSId(long value) {
        this.sId = value;
    }

    /**
     * Gets the value of the odxfQ property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOdxfQ() {
        if (odxfQ == null) {
            return false;
        } else {
            return odxfQ;
        }
    }

    /**
     * Sets the value of the odxfQ property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOdxfQ(Boolean value) {
        this.odxfQ = value;
    }

    /**
     * Gets the value of the xfDxf property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isXfDxf() {
        if (xfDxf == null) {
            return false;
        } else {
            return xfDxf;
        }
    }

    /**
     * Sets the value of the xfDxf property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setXfDxf(Boolean value) {
        this.xfDxf = value;
    }

    /**
     * Gets the value of the s property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isS() {
        if (s == null) {
            return false;
        } else {
            return s;
        }
    }

    /**
     * Sets the value of the s property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setS(Boolean value) {
        this.s = value;
    }

    /**
     * Gets the value of the dxf property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDxf() {
        if (dxf == null) {
            return false;
        } else {
            return dxf;
        }
    }

    /**
     * Sets the value of the dxf property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDxf(Boolean value) {
        this.dxf = value;
    }

    /**
     * Gets the value of the numFmtId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getNumFmtId() {
        return numFmtId;
    }

    /**
     * Sets the value of the numFmtId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setNumFmtId(Long value) {
        this.numFmtId = value;
    }

    /**
     * Gets the value of the quotePrefix property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isQuotePrefix() {
        if (quotePrefix == null) {
            return false;
        } else {
            return quotePrefix;
        }
    }

    /**
     * Sets the value of the quotePrefix property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setQuotePrefix(Boolean value) {
        this.quotePrefix = value;
    }

    /**
     * Gets the value of the oldQuotePrefix property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOldQuotePrefix() {
        if (oldQuotePrefix == null) {
            return false;
        } else {
            return oldQuotePrefix;
        }
    }

    /**
     * Sets the value of the oldQuotePrefix property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOldQuotePrefix(Boolean value) {
        this.oldQuotePrefix = value;
    }

    /**
     * Gets the value of the ph property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPh() {
        if (ph == null) {
            return false;
        } else {
            return ph;
        }
    }

    /**
     * Sets the value of the ph property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPh(Boolean value) {
        this.ph = value;
    }

    /**
     * Gets the value of the oldPh property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOldPh() {
        if (oldPh == null) {
            return false;
        } else {
            return oldPh;
        }
    }

    /**
     * Sets the value of the oldPh property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOldPh(Boolean value) {
        this.oldPh = value;
    }

    /**
     * Gets the value of the endOfListFormulaUpdate property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEndOfListFormulaUpdate() {
        if (endOfListFormulaUpdate == null) {
            return false;
        } else {
            return endOfListFormulaUpdate;
        }
    }

    /**
     * Sets the value of the endOfListFormulaUpdate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEndOfListFormulaUpdate(Boolean value) {
        this.endOfListFormulaUpdate = value;
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
