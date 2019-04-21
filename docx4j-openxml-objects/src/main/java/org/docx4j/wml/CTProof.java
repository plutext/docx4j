/*
 *  Copyright 2007-2013, Plutext Pty Ltd.
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


package org.docx4j.wml; 

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_Proof complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Proof">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="spelling" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_Proof" />
 *       &lt;attribute name="grammar" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_Proof" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Proof")
public class CTProof implements Child
{

    @XmlAttribute(name = "spelling", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STProof spelling;
    @XmlAttribute(name = "grammar", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected STProof grammar;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the spelling property.
     * 
     * @return
     *     possible object is
     *     {@link STProof }
     *     
     */
    public STProof getSpelling() {
        return spelling;
    }

    /**
     * Sets the value of the spelling property.
     * 
     * @param value
     *     allowed object is
     *     {@link STProof }
     *     
     */
    public void setSpelling(STProof value) {
        this.spelling = value;
    }

    /**
     * Gets the value of the grammar property.
     * 
     * @return
     *     possible object is
     *     {@link STProof }
     *     
     */
    public STProof getGrammar() {
        return grammar;
    }

    /**
     * Sets the value of the grammar property.
     * 
     * @param value
     *     allowed object is
     *     {@link STProof }
     *     
     */
    public void setGrammar(STProof value) {
        this.grammar = value;
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
