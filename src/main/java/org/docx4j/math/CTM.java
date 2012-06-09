/*
 * Copyright 2012 Plutext Pty Ltd.
 * 
 * This file is part of docx4j.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.docx4j.math;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_M complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_M">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mPr" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_MPr" minOccurs="0"/>
 *         &lt;element name="mr" type="{http://schemas.openxmlformats.org/officeDocument/2006/math}CT_MR" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_M", propOrder = {
    "mPr",
    "mr"
})
public class CTM
    implements Child
{

    protected CTMPr mPr;
    @XmlElement(required = true)
    protected List<CTMR> mr;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the mPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTMPr }
     *     
     */
    public CTMPr getMPr() {
        return mPr;
    }

    /**
     * Sets the value of the mPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTMPr }
     *     
     */
    public void setMPr(CTMPr value) {
        this.mPr = value;
    }

    /**
     * Gets the value of the mr property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mr property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMr().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTMR }
     * 
     * 
     */
    public List<CTMR> getMr() {
        if (mr == null) {
            mr = new ArrayList<CTMR>();
        }
        return this.mr;
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
