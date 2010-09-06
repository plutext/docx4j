/*
 *  Copyright 2010, Plutext Pty Ltd.
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


package org.xlsx4j.sml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_PCDSDTCEntries complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PCDSDTCEntries">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="m" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Missing"/>
 *         &lt;element name="n" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Number"/>
 *         &lt;element name="e" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Error"/>
 *         &lt;element name="s" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_String"/>
 *       &lt;/choice>
 *       &lt;attribute name="count" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PCDSDTCEntries", propOrder = {
    "mOrNOrE"
})
public class CTPCDSDTCEntries {

    @XmlElements({
        @XmlElement(name = "m", type = CTMissing.class),
        @XmlElement(name = "s", type = CTString.class),
        @XmlElement(name = "n", type = CTNumber.class),
        @XmlElement(name = "e", type = CTError.class)
    })
    protected List<Object> mOrNOrE;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long count;

    /**
     * Gets the value of the mOrNOrE property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mOrNOrE property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMOrNOrE().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTMissing }
     * {@link CTString }
     * {@link CTNumber }
     * {@link CTError }
     * 
     * 
     */
    public List<Object> getMOrNOrE() {
        if (mOrNOrE == null) {
            mOrNOrE = new ArrayList<Object>();
        }
        return this.mOrNOrE;
    }

    /**
     * Gets the value of the count property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCount() {
        return count;
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

}
