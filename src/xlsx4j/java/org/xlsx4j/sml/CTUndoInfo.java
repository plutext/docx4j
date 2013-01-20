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
 * <p>Java class for CT_UndoInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_UndoInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="index" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="exp" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_FormulaExpression" />
 *       &lt;attribute name="ref3D" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="array" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="v" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="nf" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="cs" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="dr" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_RefA" />
 *       &lt;attribute name="dn" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="r" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_CellRef" />
 *       &lt;attribute name="sId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_UndoInfo")
public class CTUndoInfo implements Child
{

    @XmlAttribute(name = "index", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long index;
    @XmlAttribute(name = "exp", required = true)
    protected STFormulaExpression exp;
    @XmlAttribute(name = "ref3D")
    protected Boolean ref3D;
    @XmlAttribute(name = "array")
    protected Boolean array;
    @XmlAttribute(name = "v")
    protected Boolean v;
    @XmlAttribute(name = "nf")
    protected Boolean nf;
    @XmlAttribute(name = "cs")
    protected Boolean cs;
    @XmlAttribute(name = "dr", required = true)
    protected String dr;
    @XmlAttribute(name = "dn")
    protected String dn;
    @XmlAttribute(name = "r")
    protected String r;
    @XmlAttribute(name = "sId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long sId;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the index property.
     * 
     */
    public long getIndex() {
        return index;
    }

    /**
     * Sets the value of the index property.
     * 
     */
    public void setIndex(long value) {
        this.index = value;
    }

    /**
     * Gets the value of the exp property.
     * 
     * @return
     *     possible object is
     *     {@link STFormulaExpression }
     *     
     */
    public STFormulaExpression getExp() {
        return exp;
    }

    /**
     * Sets the value of the exp property.
     * 
     * @param value
     *     allowed object is
     *     {@link STFormulaExpression }
     *     
     */
    public void setExp(STFormulaExpression value) {
        this.exp = value;
    }

    /**
     * Gets the value of the ref3D property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRef3D() {
        if (ref3D == null) {
            return false;
        } else {
            return ref3D;
        }
    }

    /**
     * Sets the value of the ref3D property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRef3D(Boolean value) {
        this.ref3D = value;
    }

    /**
     * Gets the value of the array property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isArray() {
        if (array == null) {
            return false;
        } else {
            return array;
        }
    }

    /**
     * Sets the value of the array property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setArray(Boolean value) {
        this.array = value;
    }

    /**
     * Gets the value of the v property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isV() {
        if (v == null) {
            return false;
        } else {
            return v;
        }
    }

    /**
     * Sets the value of the v property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setV(Boolean value) {
        this.v = value;
    }

    /**
     * Gets the value of the nf property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNf() {
        if (nf == null) {
            return false;
        } else {
            return nf;
        }
    }

    /**
     * Sets the value of the nf property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNf(Boolean value) {
        this.nf = value;
    }

    /**
     * Gets the value of the cs property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCs() {
        if (cs == null) {
            return false;
        } else {
            return cs;
        }
    }

    /**
     * Sets the value of the cs property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCs(Boolean value) {
        this.cs = value;
    }

    /**
     * Gets the value of the dr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDr() {
        return dr;
    }

    /**
     * Sets the value of the dr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDr(String value) {
        this.dr = value;
    }

    /**
     * Gets the value of the dn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDn() {
        return dn;
    }

    /**
     * Sets the value of the dn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDn(String value) {
        this.dn = value;
    }

    /**
     * Gets the value of the r property.
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
     * Gets the value of the sId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getSId() {
        return sId;
    }

    /**
     * Sets the value of the sId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setSId(Long value) {
        this.sId = value;
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
