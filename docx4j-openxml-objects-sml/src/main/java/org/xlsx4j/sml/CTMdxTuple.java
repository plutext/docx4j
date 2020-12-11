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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_MdxTuple complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_MdxTuple">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="n" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_MetadataStringIndex" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="c" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="ct" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="si" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="fi" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="bc" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_UnsignedIntHex" />
 *       &lt;attribute name="fc" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_UnsignedIntHex" />
 *       &lt;attribute name="i" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="u" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="st" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="b" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_MdxTuple", propOrder = {
    "n"
})
public class CTMdxTuple implements Child
{

    protected List<CTMetadataStringIndex> n;
    @XmlAttribute(name = "c")
    @XmlSchemaType(name = "unsignedInt")
    protected Long c;
    @XmlAttribute(name = "ct")
    protected String ct;
    @XmlAttribute(name = "si")
    @XmlSchemaType(name = "unsignedInt")
    protected Long si;
    @XmlAttribute(name = "fi")
    @XmlSchemaType(name = "unsignedInt")
    protected Long fi;
    @XmlAttribute(name = "bc")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] bc;
    @XmlAttribute(name = "fc")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] fc;
    @XmlAttribute(name = "i")
    protected Boolean i;
    @XmlAttribute(name = "u")
    protected Boolean u;
    @XmlAttribute(name = "st")
    protected Boolean st;
    @XmlAttribute(name = "b")
    protected Boolean b;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the n property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the n property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getN().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTMetadataStringIndex }
     * 
     * 
     */
    public List<CTMetadataStringIndex> getN() {
        if (n == null) {
            n = new ArrayList<CTMetadataStringIndex>();
        }
        return this.n;
    }

    /**
     * Gets the value of the c property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getC() {
        if (c == null) {
            return  0L;
        } else {
            return c;
        }
    }

    /**
     * Sets the value of the c property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setC(Long value) {
        this.c = value;
    }

    /**
     * Gets the value of the ct property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCt() {
        return ct;
    }

    /**
     * Sets the value of the ct property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCt(String value) {
        this.ct = value;
    }

    /**
     * Gets the value of the si property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getSi() {
        return si;
    }

    /**
     * Sets the value of the si property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setSi(Long value) {
        this.si = value;
    }

    /**
     * Gets the value of the fi property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getFi() {
        return fi;
    }

    /**
     * Sets the value of the fi property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setFi(Long value) {
        this.fi = value;
    }

    /**
     * Gets the value of the bc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getBc() {
        return bc;
    }

    /**
     * Sets the value of the bc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBc(byte[] value) {
        this.bc = value;
    }

    /**
     * Gets the value of the fc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getFc() {
        return fc;
    }

    /**
     * Sets the value of the fc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFc(byte[] value) {
        this.fc = value;
    }

    /**
     * Gets the value of the i property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isI() {
        if (i == null) {
            return false;
        } else {
            return i;
        }
    }

    /**
     * Sets the value of the i property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setI(Boolean value) {
        this.i = value;
    }

    /**
     * Gets the value of the u property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isU() {
        if (u == null) {
            return false;
        } else {
            return u;
        }
    }

    /**
     * Sets the value of the u property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setU(Boolean value) {
        this.u = value;
    }

    /**
     * Gets the value of the st property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSt() {
        if (st == null) {
            return false;
        } else {
            return st;
        }
    }

    /**
     * Sets the value of the st property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSt(Boolean value) {
        this.st = value;
    }

    /**
     * Gets the value of the b property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isB() {
        if (b == null) {
            return false;
        } else {
            return b;
        }
    }

    /**
     * Sets the value of the b property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setB(Boolean value) {
        this.b = value;
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
