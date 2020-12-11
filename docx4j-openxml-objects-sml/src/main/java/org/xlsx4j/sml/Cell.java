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

//import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Cell complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Cell">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="f" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_CellFormula" minOccurs="0"/>
 *         &lt;element name="v" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" minOccurs="0"/>
 *         &lt;element name="is" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Rst" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="r" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_CellRef" />
 *       &lt;attribute name="s" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="t" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_CellType" default="n" />
 *       &lt;attribute name="cm" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="vm" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="ph" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Cell", propOrder = {
    "f",
    "v",
    "is",
    "extLst"
})
public class Cell implements Child
{

    protected CTCellFormula f;
    protected String v;
    protected CTRst is;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "r")
    protected String r;
    @XmlAttribute(name = "s")
    @XmlSchemaType(name = "unsignedInt")
    protected Long s;
    @XmlAttribute(name = "t")
    protected STCellType t;
    @XmlAttribute(name = "cm")
    @XmlSchemaType(name = "unsignedInt")
    protected Long cm;
    @XmlAttribute(name = "vm")
    @XmlSchemaType(name = "unsignedInt")
    protected Long vm;
    @XmlAttribute(name = "ph")
    protected Boolean ph;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the f (Formula) property.
     * 
     * @return
     *     possible object is
     *     {@link CTCellFormula }
     *     
     */
    public CTCellFormula getF() {
        return f;
    }

    /**
     * Sets the value of the f property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTCellFormula }
     *     
     */
    public void setF(CTCellFormula value) {
        this.f = value;
    }

    /**
     * Gets the value of the v (Cell Value) property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getV() {
        return v;
    }

    /**
     * Sets the value of the v property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setV(String value) {
        this.v = value;
    }

    /**
     * Gets the value of the is (Rich Text Inline) property.
     * 
     * @return
     *     possible object is
     *     {@link CTRst }
     *     
     */
    public CTRst getIs() {
        return is;
    }

    /**
     * Sets the value of the is property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRst }
     *     
     */
    public void setIs(CTRst value) {
        this.is = value;
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
     * Gets the value of the r (Reference) property.  An A1 style reference to the location of this cell.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getR() {
        return r;
    }

    /**
     * Sets the value of the r property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setR(String value) {
        this.r = value;
    }

    /**
     * Gets the value of the s (Style Index) property.  The index of this cell's style. Style records are 
     * stored in the Styles Part.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getS() {
        if (s == null) {
            return  0L;
        } else {
            return s;
        }
    }

    /**
     * Sets the value of the s (Style Index) property.  The index of this cell's style. Style records are stored in the Styles Part.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setS(Long value) {
        this.s = value;
    }

    /**
     * Gets the value of the t (Cell Data Type) property.  An enumeration representing the cell's data type.
     * 
     * @return
     *     possible object is
     *     {@link STCellType }
     *     
     */
    public STCellType getT() {
        if (t == null) {
            return STCellType.N;
        } else {
            return t;
        }
    }

    /**
     * Sets the value of the t property.
     * 
     * @param value
     *     allowed object is
     *     {@link STCellType }
     *     
     */
    public void setT(STCellType value) {
        this.t = value;
    }

    /**
     * Gets the value of the cm property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getCm() {
        if (cm == null) {
            return  0L;
        } else {
            return cm;
        }
    }

    /**
     * Sets the value of the cm property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCm(Long value) {
        this.cm = value;
    }

    /**
     * Gets the value of the vm (Value Metadata Index) property.  The zero-based index of the value metadata record associated 
     * with this cell's value. Metadata records are stored in the Metadata Part. Value metadata is extra 
     * information stored at the cell level, but associated with the value rather than the cell itself. 
     * Value metadata is accessible via formula reference.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getVm() {
        if (vm == null) {
            return  0L;
        } else {
            return vm;
        }
    }

    /**
     * Sets the value of the vm property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setVm(Long value) {
        this.vm = value;
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
    
//     /**
//      * Get the WorksheetPart
//      * @return
//      * @since 3.3.3
//      */
//     public WorksheetPart getWorksheetPart() {
//     	return ((Row)getParent()).getWorksheetPart();
//     }
    
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
