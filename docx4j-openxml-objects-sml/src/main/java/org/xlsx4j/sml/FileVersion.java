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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_FileVersion complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_FileVersion">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="appName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="lastEdited" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="lowestEdited" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="rupBuild" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="codeName" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Guid" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_FileVersion")
public class FileVersion implements Child
{

    @XmlAttribute(name = "appName")
    protected String appName;
    @XmlAttribute(name = "lastEdited")
    protected String lastEdited;
    @XmlAttribute(name = "lowestEdited")
    protected String lowestEdited;
    @XmlAttribute(name = "rupBuild")
    protected String rupBuild;
    @XmlAttribute(name = "codeName")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String codeName;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the appName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Sets the value of the appName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAppName(String value) {
        this.appName = value;
    }

    /**
     * Gets the value of the lastEdited property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastEdited() {
        return lastEdited;
    }

    /**
     * Sets the value of the lastEdited property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastEdited(String value) {
        this.lastEdited = value;
    }

    /**
     * Gets the value of the lowestEdited property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLowestEdited() {
        return lowestEdited;
    }

    /**
     * Sets the value of the lowestEdited property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLowestEdited(String value) {
        this.lowestEdited = value;
    }

    /**
     * Gets the value of the rupBuild property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRupBuild() {
        return rupBuild;
    }

    /**
     * Sets the value of the rupBuild property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRupBuild(String value) {
        this.rupBuild = value;
    }

    /**
     * Gets the value of the codeName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodeName() {
        return codeName;
    }

    /**
     * Sets the value of the codeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodeName(String value) {
        this.codeName = value;
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
