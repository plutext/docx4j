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
 * <p>Java class for CT_Connection complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_Connection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dbPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_DbPr" minOccurs="0"/>
 *         &lt;element name="olapPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_OlapPr" minOccurs="0"/>
 *         &lt;element name="webPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_WebPr" minOccurs="0"/>
 *         &lt;element name="textPr" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_TextPr" minOccurs="0"/>
 *         &lt;element name="parameters" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_Parameters" minOccurs="0"/>
 *         &lt;element name="extLst" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_ExtensionList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="sourceFile" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="odcFile" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="keepAlive" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="interval" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="name" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="description" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="type" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="reconnectionMethod" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="1" />
 *       &lt;attribute name="refreshedVersion" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" />
 *       &lt;attribute name="minRefreshableVersion" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" default="0" />
 *       &lt;attribute name="savePassword" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="new" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="deleted" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="onlyUseConnectionFile" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="background" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="refreshOnLoad" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="saveData" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="credentials" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_CredMethod" default="integrated" />
 *       &lt;attribute name="singleSignOnId" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_Connection", propOrder = {
    "dbPr",
    "olapPr",
    "webPr",
    "textPr",
    "parameters",
    "extLst"
})
public class CTConnection implements Child
{

    protected CTDbPr dbPr;
    protected CTOlapPr olapPr;
    protected CTWebPr webPr;
    protected CTTextPr textPr;
    protected CTParameters parameters;
    protected CTExtensionList extLst;
    @XmlAttribute(name = "id", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long id;
    @XmlAttribute(name = "sourceFile")
    protected String sourceFile;
    @XmlAttribute(name = "odcFile")
    protected String odcFile;
    @XmlAttribute(name = "keepAlive")
    protected Boolean keepAlive;
    @XmlAttribute(name = "interval")
    @XmlSchemaType(name = "unsignedInt")
    protected Long interval;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "description")
    protected String description;
    @XmlAttribute(name = "type")
    @XmlSchemaType(name = "unsignedInt")
    protected Long type;
    @XmlAttribute(name = "reconnectionMethod")
    @XmlSchemaType(name = "unsignedInt")
    protected Long reconnectionMethod;
    @XmlAttribute(name = "refreshedVersion", required = true)
    @XmlSchemaType(name = "unsignedByte")
    protected short refreshedVersion;
    @XmlAttribute(name = "minRefreshableVersion")
    @XmlSchemaType(name = "unsignedByte")
    protected Short minRefreshableVersion;
    @XmlAttribute(name = "savePassword")
    protected Boolean savePassword;
    @XmlAttribute(name = "new")
    protected Boolean _new;
    @XmlAttribute(name = "deleted")
    protected Boolean deleted;
    @XmlAttribute(name = "onlyUseConnectionFile")
    protected Boolean onlyUseConnectionFile;
    @XmlAttribute(name = "background")
    protected Boolean background;
    @XmlAttribute(name = "refreshOnLoad")
    protected Boolean refreshOnLoad;
    @XmlAttribute(name = "saveData")
    protected Boolean saveData;
    @XmlAttribute(name = "credentials")
    protected STCredMethod credentials;
    @XmlAttribute(name = "singleSignOnId")
    protected String singleSignOnId;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the dbPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTDbPr }
     *     
     */
    public CTDbPr getDbPr() {
        return dbPr;
    }

    /**
     * Sets the value of the dbPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTDbPr }
     *     
     */
    public void setDbPr(CTDbPr value) {
        this.dbPr = value;
    }

    /**
     * Gets the value of the olapPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTOlapPr }
     *     
     */
    public CTOlapPr getOlapPr() {
        return olapPr;
    }

    /**
     * Sets the value of the olapPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTOlapPr }
     *     
     */
    public void setOlapPr(CTOlapPr value) {
        this.olapPr = value;
    }

    /**
     * Gets the value of the webPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTWebPr }
     *     
     */
    public CTWebPr getWebPr() {
        return webPr;
    }

    /**
     * Sets the value of the webPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTWebPr }
     *     
     */
    public void setWebPr(CTWebPr value) {
        this.webPr = value;
    }

    /**
     * Gets the value of the textPr property.
     * 
     * @return
     *     possible object is
     *     {@link CTTextPr }
     *     
     */
    public CTTextPr getTextPr() {
        return textPr;
    }

    /**
     * Sets the value of the textPr property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTTextPr }
     *     
     */
    public void setTextPr(CTTextPr value) {
        this.textPr = value;
    }

    /**
     * Gets the value of the parameters property.
     * 
     * @return
     *     possible object is
     *     {@link CTParameters }
     *     
     */
    public CTParameters getParameters() {
        return parameters;
    }

    /**
     * Sets the value of the parameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link CTParameters }
     *     
     */
    public void setParameters(CTParameters value) {
        this.parameters = value;
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
     * Gets the value of the id property.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(long value) {
        this.id = value;
    }

    /**
     * Gets the value of the sourceFile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceFile() {
        return sourceFile;
    }

    /**
     * Sets the value of the sourceFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceFile(String value) {
        this.sourceFile = value;
    }

    /**
     * Gets the value of the odcFile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOdcFile() {
        return odcFile;
    }

    /**
     * Sets the value of the odcFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOdcFile(String value) {
        this.odcFile = value;
    }

    /**
     * Gets the value of the keepAlive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isKeepAlive() {
        if (keepAlive == null) {
            return false;
        } else {
            return keepAlive;
        }
    }

    /**
     * Sets the value of the keepAlive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setKeepAlive(Boolean value) {
        this.keepAlive = value;
    }

    /**
     * Gets the value of the interval property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getInterval() {
        if (interval == null) {
            return  0L;
        } else {
            return interval;
        }
    }

    /**
     * Sets the value of the interval property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setInterval(Long value) {
        this.interval = value;
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
        return name;
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
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setType(Long value) {
        this.type = value;
    }

    /**
     * Gets the value of the reconnectionMethod property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getReconnectionMethod() {
        if (reconnectionMethod == null) {
            return  1L;
        } else {
            return reconnectionMethod;
        }
    }

    /**
     * Sets the value of the reconnectionMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setReconnectionMethod(Long value) {
        this.reconnectionMethod = value;
    }

    /**
     * Gets the value of the refreshedVersion property.
     * 
     */
    public short getRefreshedVersion() {
        return refreshedVersion;
    }

    /**
     * Sets the value of the refreshedVersion property.
     * 
     */
    public void setRefreshedVersion(short value) {
        this.refreshedVersion = value;
    }

    /**
     * Gets the value of the minRefreshableVersion property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public short getMinRefreshableVersion() {
        if (minRefreshableVersion == null) {
            return ((short) 0);
        } else {
            return minRefreshableVersion;
        }
    }

    /**
     * Sets the value of the minRefreshableVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setMinRefreshableVersion(Short value) {
        this.minRefreshableVersion = value;
    }

    /**
     * Gets the value of the savePassword property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSavePassword() {
        if (savePassword == null) {
            return false;
        } else {
            return savePassword;
        }
    }

    /**
     * Sets the value of the savePassword property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSavePassword(Boolean value) {
        this.savePassword = value;
    }

    /**
     * Gets the value of the new property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isNew() {
        if (_new == null) {
            return false;
        } else {
            return _new;
        }
    }

    /**
     * Sets the value of the new property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNew(Boolean value) {
        this._new = value;
    }

    /**
     * Gets the value of the deleted property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDeleted() {
        if (deleted == null) {
            return false;
        } else {
            return deleted;
        }
    }

    /**
     * Sets the value of the deleted property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDeleted(Boolean value) {
        this.deleted = value;
    }

    /**
     * Gets the value of the onlyUseConnectionFile property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOnlyUseConnectionFile() {
        if (onlyUseConnectionFile == null) {
            return false;
        } else {
            return onlyUseConnectionFile;
        }
    }

    /**
     * Sets the value of the onlyUseConnectionFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOnlyUseConnectionFile(Boolean value) {
        this.onlyUseConnectionFile = value;
    }

    /**
     * Gets the value of the background property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isBackground() {
        if (background == null) {
            return false;
        } else {
            return background;
        }
    }

    /**
     * Sets the value of the background property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBackground(Boolean value) {
        this.background = value;
    }

    /**
     * Gets the value of the refreshOnLoad property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRefreshOnLoad() {
        if (refreshOnLoad == null) {
            return false;
        } else {
            return refreshOnLoad;
        }
    }

    /**
     * Sets the value of the refreshOnLoad property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRefreshOnLoad(Boolean value) {
        this.refreshOnLoad = value;
    }

    /**
     * Gets the value of the saveData property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSaveData() {
        if (saveData == null) {
            return false;
        } else {
            return saveData;
        }
    }

    /**
     * Sets the value of the saveData property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSaveData(Boolean value) {
        this.saveData = value;
    }

    /**
     * Gets the value of the credentials property.
     * 
     * @return
     *     possible object is
     *     {@link STCredMethod }
     *     
     */
    public STCredMethod getCredentials() {
        if (credentials == null) {
            return STCredMethod.INTEGRATED;
        } else {
            return credentials;
        }
    }

    /**
     * Sets the value of the credentials property.
     * 
     * @param value
     *     allowed object is
     *     {@link STCredMethod }
     *     
     */
    public void setCredentials(STCredMethod value) {
        this.credentials = value;
    }

    /**
     * Gets the value of the singleSignOnId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSingleSignOnId() {
        return singleSignOnId;
    }

    /**
     * Sets the value of the singleSignOnId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSingleSignOnId(String value) {
        this.singleSignOnId = value;
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
