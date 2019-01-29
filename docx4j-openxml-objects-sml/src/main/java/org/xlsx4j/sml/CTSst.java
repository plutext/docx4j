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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * 
 * <p>This element is the root of the Shared String Table, which serves as a collection of individual String Items (si).
 * 
 * <p>Java class for CT_Sst complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Sst">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="si" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Rst" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="count" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="uniqueCount" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Sst", propOrder = {
    "si",
    "extLst"
})
@XmlRootElement(name = "sst")
public class CTSst implements Child
{

    protected List<CTRst> si;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "count")
    @XmlSchemaType(name = "unsignedInt")
    protected Long count;
    @XmlAttribute(name = "uniqueCount")
    @XmlSchemaType(name = "unsignedInt")
    protected Long uniqueCount;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the si (String Item) property.
     * 
     * <p>This element is the representation of an individual string in the Shared String table.  
     * 
     * <p>If the string is just a simple string with formatting applied at the cell level, 
     * then the String Item (si) should contain a single text element used to express the string.  
     * However, if the string in the cell is more complex - i.e., has formatting applied at 
     * the character level - then the string item shall consist of multiple rich text runs which 
     * collectively are used to express the string.  
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the si property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSi().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTRst }
     * 
     * 
     */
    public List<CTRst> getSi() {
        if (si == null) {
            si = new ArrayList<CTRst>();
        }
        return this.si;
    }

    /**
     * Gets the value of the extLst property.
     * 
     * @return
     *     possible object is
     *     {@link CTExtensionList }
     *     
     */
    public CTExtensionList getExtLst() {
        return extLst;
    }

    /**
     * Sets the value of the extLst property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTExtensionList }
     *     
     */
    public void setExtLst(CTExtensionList value) {
        this.extLst = value;
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

    /**
     * Gets the value of the uniqueCount property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getUniqueCount() {
        return uniqueCount;
    }

    /**
     * Sets the value of the uniqueCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setUniqueCount(Long value) {
        this.uniqueCount = value;
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
