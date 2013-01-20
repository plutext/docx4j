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
 * <p>Java class for CT_DrawingHF complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DrawingHF">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}id use="required""/>
 *       &lt;attribute name="lho" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="lhe" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="lhf" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="cho" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="che" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="chf" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="rho" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="rhe" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="rhf" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="lfo" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="lfe" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="lff" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="cfo" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="cfe" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="cff" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="rfo" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="rfe" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="rff" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DrawingHF")
public class CTDrawingHF implements Child
{

    @XmlAttribute(name = "id", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships", required = true)
    protected String id;
    @XmlAttribute(name = "lho")
    @XmlSchemaType(name = "unsignedInt")
    protected Long lho;
    @XmlAttribute(name = "lhe")
    @XmlSchemaType(name = "unsignedInt")
    protected Long lhe;
    @XmlAttribute(name = "lhf")
    @XmlSchemaType(name = "unsignedInt")
    protected Long lhf;
    @XmlAttribute(name = "cho")
    @XmlSchemaType(name = "unsignedInt")
    protected Long cho;
    @XmlAttribute(name = "che")
    @XmlSchemaType(name = "unsignedInt")
    protected Long che;
    @XmlAttribute(name = "chf")
    @XmlSchemaType(name = "unsignedInt")
    protected Long chf;
    @XmlAttribute(name = "rho")
    @XmlSchemaType(name = "unsignedInt")
    protected Long rho;
    @XmlAttribute(name = "rhe")
    @XmlSchemaType(name = "unsignedInt")
    protected Long rhe;
    @XmlAttribute(name = "rhf")
    @XmlSchemaType(name = "unsignedInt")
    protected Long rhf;
    @XmlAttribute(name = "lfo")
    @XmlSchemaType(name = "unsignedInt")
    protected Long lfo;
    @XmlAttribute(name = "lfe")
    @XmlSchemaType(name = "unsignedInt")
    protected Long lfe;
    @XmlAttribute(name = "lff")
    @XmlSchemaType(name = "unsignedInt")
    protected Long lff;
    @XmlAttribute(name = "cfo")
    @XmlSchemaType(name = "unsignedInt")
    protected Long cfo;
    @XmlAttribute(name = "cfe")
    @XmlSchemaType(name = "unsignedInt")
    protected Long cfe;
    @XmlAttribute(name = "cff")
    @XmlSchemaType(name = "unsignedInt")
    protected Long cff;
    @XmlAttribute(name = "rfo")
    @XmlSchemaType(name = "unsignedInt")
    protected Long rfo;
    @XmlAttribute(name = "rfe")
    @XmlSchemaType(name = "unsignedInt")
    protected Long rfe;
    @XmlAttribute(name = "rff")
    @XmlSchemaType(name = "unsignedInt")
    protected Long rff;
    @XmlTransient
    private Object parent;

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
     * Gets the value of the lho property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getLho() {
        return lho;
    }

    /**
     * Sets the value of the lho property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setLho(Long value) {
        this.lho = value;
    }

    /**
     * Gets the value of the lhe property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getLhe() {
        return lhe;
    }

    /**
     * Sets the value of the lhe property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setLhe(Long value) {
        this.lhe = value;
    }

    /**
     * Gets the value of the lhf property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getLhf() {
        return lhf;
    }

    /**
     * Sets the value of the lhf property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setLhf(Long value) {
        this.lhf = value;
    }

    /**
     * Gets the value of the cho property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCho() {
        return cho;
    }

    /**
     * Sets the value of the cho property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCho(Long value) {
        this.cho = value;
    }

    /**
     * Gets the value of the che property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getChe() {
        return che;
    }

    /**
     * Sets the value of the che property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setChe(Long value) {
        this.che = value;
    }

    /**
     * Gets the value of the chf property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getChf() {
        return chf;
    }

    /**
     * Sets the value of the chf property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setChf(Long value) {
        this.chf = value;
    }

    /**
     * Gets the value of the rho property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getRho() {
        return rho;
    }

    /**
     * Sets the value of the rho property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRho(Long value) {
        this.rho = value;
    }

    /**
     * Gets the value of the rhe property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getRhe() {
        return rhe;
    }

    /**
     * Sets the value of the rhe property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRhe(Long value) {
        this.rhe = value;
    }

    /**
     * Gets the value of the rhf property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getRhf() {
        return rhf;
    }

    /**
     * Sets the value of the rhf property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRhf(Long value) {
        this.rhf = value;
    }

    /**
     * Gets the value of the lfo property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getLfo() {
        return lfo;
    }

    /**
     * Sets the value of the lfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setLfo(Long value) {
        this.lfo = value;
    }

    /**
     * Gets the value of the lfe property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getLfe() {
        return lfe;
    }

    /**
     * Sets the value of the lfe property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setLfe(Long value) {
        this.lfe = value;
    }

    /**
     * Gets the value of the lff property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getLff() {
        return lff;
    }

    /**
     * Sets the value of the lff property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setLff(Long value) {
        this.lff = value;
    }

    /**
     * Gets the value of the cfo property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCfo() {
        return cfo;
    }

    /**
     * Sets the value of the cfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCfo(Long value) {
        this.cfo = value;
    }

    /**
     * Gets the value of the cfe property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCfe() {
        return cfe;
    }

    /**
     * Sets the value of the cfe property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCfe(Long value) {
        this.cfe = value;
    }

    /**
     * Gets the value of the cff property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCff() {
        return cff;
    }

    /**
     * Sets the value of the cff property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCff(Long value) {
        this.cff = value;
    }

    /**
     * Gets the value of the rfo property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getRfo() {
        return rfo;
    }

    /**
     * Sets the value of the rfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRfo(Long value) {
        this.rfo = value;
    }

    /**
     * Gets the value of the rfe property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getRfe() {
        return rfe;
    }

    /**
     * Sets the value of the rfe property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRfe(Long value) {
        this.rfe = value;
    }

    /**
     * Gets the value of the rff property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getRff() {
        return rff;
    }

    /**
     * Sets the value of the rff property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRff(Long value) {
        this.rff = value;
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
