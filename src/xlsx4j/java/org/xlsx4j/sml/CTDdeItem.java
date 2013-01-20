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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_DdeItem complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_DdeItem">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="values" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_DdeValues" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" default="0" />
 *       &lt;attribute name="ole" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="advise" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="preferPic" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_DdeItem", propOrder = {
    "values"
})
public class CTDdeItem implements Child
{

    protected CTDdeValues values;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "ole")
    protected Boolean ole;
    @XmlAttribute(name = "advise")
    protected Boolean advise;
    @XmlAttribute(name = "preferPic")
    protected Boolean preferPic;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the values property.
     * 
     * @return
     *     possible object is
     *     {@link CTDdeValues }
     *     
     */
    public CTDdeValues getValues() {
        return values;
    }

    /**
     * Sets the value of the values property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDdeValues }
     *     
     */
    public void setValues(CTDdeValues value) {
        this.values = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        if (name == null) {
            return "0";
        } else {
            return name;
        }
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the ole property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOle() {
        if (ole == null) {
            return false;
        } else {
            return ole;
        }
    }

    /**
     * Sets the value of the ole property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOle(Boolean value) {
        this.ole = value;
    }

    /**
     * Gets the value of the advise property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAdvise() {
        if (advise == null) {
            return false;
        } else {
            return advise;
        }
    }

    /**
     * Sets the value of the advise property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAdvise(Boolean value) {
        this.advise = value;
    }

    /**
     * Gets the value of the preferPic property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPreferPic() {
        if (preferPic == null) {
            return false;
        } else {
            return preferPic;
        }
    }

    /**
     * Sets the value of the preferPic property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPreferPic(Boolean value) {
        this.preferPic = value;
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
