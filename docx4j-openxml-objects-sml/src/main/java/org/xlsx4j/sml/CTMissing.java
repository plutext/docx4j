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
 * <p>Java class for CT_Missing complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Missing">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="tpls" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Tuples" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="x" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_X" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="u" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="f" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="c" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="cp" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="in" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="bc" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_UnsignedIntHex" />
 *       &lt;attribute name="fc" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_UnsignedIntHex" />
 *       &lt;attribute name="i" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="un" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
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
@XmlType(name = "CT_Missing", propOrder = {
    "tpls",
    "x"
})
public class CTMissing implements Child
{

    protected List<CTTuples> tpls;
    protected List<CTX> x;
    @XmlAttribute(name = "u")
    protected Boolean u;
    @XmlAttribute(name = "f")
    protected Boolean f;
    @XmlAttribute(name = "c")
    protected String c;
    @XmlAttribute(name = "cp")
    @XmlSchemaType(name = "unsignedInt")
    protected Long cp;
    @XmlAttribute(name = "in")
    @XmlSchemaType(name = "unsignedInt")
    protected Long in;
    @XmlAttribute(name = "bc")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] bc;
    @XmlAttribute(name = "fc")
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    protected byte[] fc;
    @XmlAttribute(name = "i")
    protected Boolean i;
    @XmlAttribute(name = "un")
    protected Boolean un;
    @XmlAttribute(name = "st")
    protected Boolean st;
    @XmlAttribute(name = "b")
    protected Boolean b;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the tpls property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tpls property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTpls().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTTuples }
     * 
     * 
     */
    public List<CTTuples> getTpls() {
        if (tpls == null) {
            tpls = new ArrayList<CTTuples>();
        }
        return this.tpls;
    }

    /**
     * Gets the value of the x property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the x property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getX().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTX }
     * 
     * 
     */
    public List<CTX> getX() {
        if (x == null) {
            x = new ArrayList<CTX>();
        }
        return this.x;
    }

    /**
     * Gets the value of the u property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isU() {
        return u;
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
     * Gets the value of the f property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isF() {
        return f;
    }

    /**
     * Sets the value of the f property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setF(Boolean value) {
        this.f = value;
    }

    /**
     * Gets the value of the c property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getC() {
        return c;
    }

    /**
     * Sets the value of the c property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setC(String value) {
        this.c = value;
    }

    /**
     * Gets the value of the cp property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCp() {
        return cp;
    }

    /**
     * Sets the value of the cp property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCp(Long value) {
        this.cp = value;
    }

    /**
     * Gets the value of the in property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getIn() {
        return in;
    }

    /**
     * Sets the value of the in property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setIn(Long value) {
        this.in = value;
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
     * Gets the value of the un property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUn() {
        if (un == null) {
            return false;
        } else {
            return un;
        }
    }

    /**
     * Sets the value of the un property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUn(Boolean value) {
        this.un = value;
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
