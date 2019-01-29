/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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


package org.docx4j.vml.officedrawing;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.docx4j.vml.STExt;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_ShapeLayout complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_ShapeLayout">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="idmap" type="{urn:schemas-microsoft-com:office:office}CT_IdMap" minOccurs="0"/>
 *         &lt;element name="regrouptable" type="{urn:schemas-microsoft-com:office:office}CT_RegroupTable" minOccurs="0"/>
 *         &lt;element name="rules" type="{urn:schemas-microsoft-com:office:office}CT_Rules" minOccurs="0"/>
 *       &lt;/all>
 *       &lt;attGroup ref="{urn:schemas-microsoft-com:vml}AG_Ext"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_ShapeLayout", propOrder = {

})
public class CTShapeLayout
    implements Child
{

    protected CTIdMap idmap;
    protected CTRegroupTable regrouptable;
    protected CTRules rules;
    @XmlAttribute(name = "ext", namespace = "urn:schemas-microsoft-com:vml")
    protected STExt ext;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the idmap property.
     * 
     * @return
     *     possible object is
     *     {@link CTIdMap }
     *     
     */
    public CTIdMap getIdmap() {
        return idmap;
    }

    /**
     * Sets the value of the idmap property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTIdMap }
     *     
     */
    public void setIdmap(CTIdMap value) {
        this.idmap = value;
    }

    /**
     * Gets the value of the regrouptable property.
     * 
     * @return
     *     possible object is
     *     {@link CTRegroupTable }
     *     
     */
    public CTRegroupTable getRegrouptable() {
        return regrouptable;
    }

    /**
     * Sets the value of the regrouptable property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRegroupTable }
     *     
     */
    public void setRegrouptable(CTRegroupTable value) {
        this.regrouptable = value;
    }

    /**
     * Gets the value of the rules property.
     * 
     * @return
     *     possible object is
     *     {@link CTRules }
     *     
     */
    public CTRules getRules() {
        return rules;
    }

    /**
     * Sets the value of the rules property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTRules }
     *     
     */
    public void setRules(CTRules value) {
        this.rules = value;
    }

    /**
     * Gets the value of the ext property.
     * 
     * @return
     *     possible object is
     *     {@link STExt }
     *     
     */
    public STExt getExt() {
        return ext;
    }

    /**
     * Sets the value of the ext property.
     * 
     * @param value
     *     allowed object is
     *     {@link STExt }
     *     
     */
    public void setExt(STExt value) {
        this.ext = value;
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
