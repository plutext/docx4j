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
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for CT_FontRel complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_FontRel">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}CT_Rel">
 *       &lt;attribute name="fontKey" type="{http://schemas.openxmlformats.org/wordprocessingml/2006/main}ST_Guid" />
 *       &lt;attribute name="subsetted" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_FontRel")
public class FontRel
    extends CTRel
{

    @XmlAttribute(name = "fontKey", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String fontKey;
    @XmlAttribute(name = "subsetted", namespace = "http://schemas.openxmlformats.org/wordprocessingml/2006/main")
    protected Boolean subsetted;

    /**
     * Gets the value of the fontKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFontKey() {
        return fontKey;
    }

    /**
     * Sets the value of the fontKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFontKey(String value) {
        this.fontKey = value;
    }

    /**
     * Gets the value of the subsetted property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSubsetted() {
        if (subsetted == null) {
            return true;
        } else {
            return subsetted;
        }
    }

    /**
     * Sets the value of the subsetted property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSubsetted(Boolean value) {
        this.subsetted = value;
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
