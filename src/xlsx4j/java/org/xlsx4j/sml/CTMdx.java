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
 * <p>Java class for CT_Mdx complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Mdx">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="t" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_MdxTuple"/>
 *         &lt;element name="ms" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_MdxSet"/>
 *         &lt;element name="p" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_MdxMemeberProp"/>
 *         &lt;element name="k" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_MdxKPI"/>
 *       &lt;/choice>
 *       &lt;attribute name="n" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="f" use="required" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_MdxFunctionType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Mdx", propOrder = {
    "t",
    "ms",
    "p",
    "k"
})
public class CTMdx implements Child
{

    protected CTMdxTuple t;
    protected CTMdxSet ms;
    protected CTMdxMemeberProp p;
    protected CTMdxKPI k;
    @XmlAttribute(name = "n", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long n;
    @XmlAttribute(name = "f", required = true)
    protected STMdxFunctionType f;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the t property.
     * 
     * @return
     *     possible object is
     *     {@link CTMdxTuple }
     *     
     */
    public CTMdxTuple getT() {
        return t;
    }

    /**
     * Sets the value of the t property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMdxTuple }
     *     
     */
    public void setT(CTMdxTuple value) {
        this.t = value;
    }

    /**
     * Gets the value of the ms property.
     * 
     * @return
     *     possible object is
     *     {@link CTMdxSet }
     *     
     */
    public CTMdxSet getMs() {
        return ms;
    }

    /**
     * Sets the value of the ms property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMdxSet }
     *     
     */
    public void setMs(CTMdxSet value) {
        this.ms = value;
    }

    /**
     * Gets the value of the p property.
     * 
     * @return
     *     possible object is
     *     {@link CTMdxMemeberProp }
     *     
     */
    public CTMdxMemeberProp getP() {
        return p;
    }

    /**
     * Sets the value of the p property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMdxMemeberProp }
     *     
     */
    public void setP(CTMdxMemeberProp value) {
        this.p = value;
    }

    /**
     * Gets the value of the k property.
     * 
     * @return
     *     possible object is
     *     {@link CTMdxKPI }
     *     
     */
    public CTMdxKPI getK() {
        return k;
    }

    /**
     * Sets the value of the k property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMdxKPI }
     *     
     */
    public void setK(CTMdxKPI value) {
        this.k = value;
    }

    /**
     * Gets the value of the n property.
     * 
     */
    public long getN() {
        return n;
    }

    /**
     * Sets the value of the n property.
     * 
     */
    public void setN(long value) {
        this.n = value;
    }

    /**
     * Gets the value of the f property.
     * 
     * @return
     *     possible object is
     *     {@link STMdxFunctionType }
     *     
     */
    public STMdxFunctionType getF() {
        return f;
    }

    /**
     * Sets the value of the f property.
     * 
     * @param value
     *     allowed object is
     *     {@link STMdxFunctionType }
     *     
     */
    public void setF(STMdxFunctionType value) {
        this.f = value;
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
