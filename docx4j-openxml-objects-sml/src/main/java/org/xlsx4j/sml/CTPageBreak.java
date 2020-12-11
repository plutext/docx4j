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
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PageBreak complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PageBreak">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="brk" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Break" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="count" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="manualBreakCount" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PageBreak", propOrder = {
    "brk"
})
public class CTPageBreak implements Child
{

    protected List<CTBreak> brk;
    @XmlAttribute(name = "count")
    @XmlSchemaType(name = "unsignedInt")
    protected Long count;
    @XmlAttribute(name = "manualBreakCount")
    @XmlSchemaType(name = "unsignedInt")
    protected Long manualBreakCount;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the brk property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the brk property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBrk().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTBreak }
     * 
     * 
     */
    public List<CTBreak> getBrk() {
        if (brk == null) {
            brk = new ArrayList<CTBreak>();
        }
        return this.brk;
    }

    /**
     * Gets the value of the count property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getCount() {
        if (count == null) {
            return  0L;
        } else {
            return count;
        }
    }

    /**
     * Sets the value of the count property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCount(Long value) {
        this.count = value;
    }

    /**
     * Gets the value of the manualBreakCount property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getManualBreakCount() {
        if (manualBreakCount == null) {
            return  0L;
        } else {
            return manualBreakCount;
        }
    }

    /**
     * Sets the value of the manualBreakCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setManualBreakCount(Long value) {
        this.manualBreakCount = value;
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
