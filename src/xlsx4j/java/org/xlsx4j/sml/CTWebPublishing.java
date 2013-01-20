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
 * <p>Java class for CT_WebPublishing complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_WebPublishing">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="css" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="thicket" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="longFileNames" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="vml" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="allowPng" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="targetScreenSize" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_TargetScreenSize" default="800x600" />
 *       &lt;attribute name="dpi" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="96" />
 *       &lt;attribute name="codePage" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="characterSet" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_WebPublishing")
public class CTWebPublishing implements Child
{

    @XmlAttribute(name = "css")
    protected Boolean css;
    @XmlAttribute(name = "thicket")
    protected Boolean thicket;
    @XmlAttribute(name = "longFileNames")
    protected Boolean longFileNames;
    @XmlAttribute(name = "vml")
    protected Boolean vml;
    @XmlAttribute(name = "allowPng")
    protected Boolean allowPng;
    @XmlAttribute(name = "targetScreenSize")
    protected String targetScreenSize;
    @XmlAttribute(name = "dpi")
    @XmlSchemaType(name = "unsignedInt")
    protected Long dpi;
    @XmlAttribute(name = "codePage")
    @XmlSchemaType(name = "unsignedInt")
    protected Long codePage;
    @XmlAttribute(name = "characterSet")
    protected String characterSet;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the css property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCss() {
        if (css == null) {
            return true;
        } else {
            return css;
        }
    }

    /**
     * Sets the value of the css property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCss(Boolean value) {
        this.css = value;
    }

    /**
     * Gets the value of the thicket property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isThicket() {
        if (thicket == null) {
            return true;
        } else {
            return thicket;
        }
    }

    /**
     * Sets the value of the thicket property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setThicket(Boolean value) {
        this.thicket = value;
    }

    /**
     * Gets the value of the longFileNames property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isLongFileNames() {
        if (longFileNames == null) {
            return true;
        } else {
            return longFileNames;
        }
    }

    /**
     * Sets the value of the longFileNames property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLongFileNames(Boolean value) {
        this.longFileNames = value;
    }

    /**
     * Gets the value of the vml property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isVml() {
        if (vml == null) {
            return false;
        } else {
            return vml;
        }
    }

    /**
     * Sets the value of the vml property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVml(Boolean value) {
        this.vml = value;
    }

    /**
     * Gets the value of the allowPng property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAllowPng() {
        if (allowPng == null) {
            return false;
        } else {
            return allowPng;
        }
    }

    /**
     * Sets the value of the allowPng property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAllowPng(Boolean value) {
        this.allowPng = value;
    }

    /**
     * Gets the value of the targetScreenSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetScreenSize() {
        if (targetScreenSize == null) {
            return "800x600";
        } else {
            return targetScreenSize;
        }
    }

    /**
     * Sets the value of the targetScreenSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetScreenSize(String value) {
        this.targetScreenSize = value;
    }

    /**
     * Gets the value of the dpi property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getDpi() {
        if (dpi == null) {
            return  96L;
        } else {
            return dpi;
        }
    }

    /**
     * Sets the value of the dpi property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDpi(Long value) {
        this.dpi = value;
    }

    /**
     * Gets the value of the codePage property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCodePage() {
        return codePage;
    }

    /**
     * Sets the value of the codePage property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCodePage(Long value) {
        this.codePage = value;
    }

    /**
     * Gets the value of the characterSet property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCharacterSet() {
        return characterSet;
    }

    /**
     * Sets the value of the characterSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCharacterSet(String value) {
        this.characterSet = value;
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
