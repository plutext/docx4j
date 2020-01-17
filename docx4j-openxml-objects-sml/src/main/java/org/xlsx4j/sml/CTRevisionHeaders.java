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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_RevisionHeaders complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_RevisionHeaders">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="header" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}CT_RevisionHeader" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="guid" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Guid" />
 *       &lt;attribute name="lastGuid" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Guid" />
 *       &lt;attribute name="shared" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="diskRevisions" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="history" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="trackRevisions" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="exclusive" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="revisionId" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="0" />
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}int" default="1" />
 *       &lt;attribute name="keepChangeHistory" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="protected" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="preserveHistory" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="30" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_RevisionHeaders", propOrder = {
    "header"
})
public class CTRevisionHeaders implements Child
{

    @XmlElement(required = true)
    protected List<CTRevisionHeader> header;
    @XmlAttribute(name = "guid", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String guid;
    @XmlAttribute(name = "lastGuid")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String lastGuid;
    @XmlAttribute(name = "shared")
    protected Boolean shared;
    @XmlAttribute(name = "diskRevisions")
    protected Boolean diskRevisions;
    @XmlAttribute(name = "history")
    protected Boolean history;
    @XmlAttribute(name = "trackRevisions")
    protected Boolean trackRevisions;
    @XmlAttribute(name = "exclusive")
    protected Boolean exclusive;
    @XmlAttribute(name = "revisionId")
    @XmlSchemaType(name = "unsignedInt")
    protected Long revisionId;
    @XmlAttribute(name = "version")
    protected Integer version;
    @XmlAttribute(name = "keepChangeHistory")
    protected Boolean keepChangeHistory;
    @XmlAttribute(name = "protected")
    protected Boolean _protected;
    @XmlAttribute(name = "preserveHistory")
    @XmlSchemaType(name = "unsignedInt")
    protected Long preserveHistory;
    @XmlTransient
    private Object parent;

    /**
     * Gets the value of the header property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the header property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHeader().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CTRevisionHeader }
     * 
     * 
     */
    public List<CTRevisionHeader> getHeader() {
        if (header == null) {
            header = new ArrayList<CTRevisionHeader>();
        }
        return this.header;
    }

    /**
     * Gets the value of the guid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGuid() {
        return guid;
    }

    /**
     * Sets the value of the guid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGuid(String value) {
        this.guid = value;
    }

    /**
     * Gets the value of the lastGuid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastGuid() {
        return lastGuid;
    }

    /**
     * Sets the value of the lastGuid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastGuid(String value) {
        this.lastGuid = value;
    }

    /**
     * Gets the value of the shared property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isShared() {
        if (shared == null) {
            return true;
        } else {
            return shared;
        }
    }

    /**
     * Sets the value of the shared property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setShared(Boolean value) {
        this.shared = value;
    }

    /**
     * Gets the value of the diskRevisions property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDiskRevisions() {
        if (diskRevisions == null) {
            return false;
        } else {
            return diskRevisions;
        }
    }

    /**
     * Sets the value of the diskRevisions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDiskRevisions(Boolean value) {
        this.diskRevisions = value;
    }

    /**
     * Gets the value of the history property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isHistory() {
        if (history == null) {
            return true;
        } else {
            return history;
        }
    }

    /**
     * Sets the value of the history property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHistory(Boolean value) {
        this.history = value;
    }

    /**
     * Gets the value of the trackRevisions property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTrackRevisions() {
        if (trackRevisions == null) {
            return true;
        } else {
            return trackRevisions;
        }
    }

    /**
     * Sets the value of the trackRevisions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTrackRevisions(Boolean value) {
        this.trackRevisions = value;
    }

    /**
     * Gets the value of the exclusive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isExclusive() {
        if (exclusive == null) {
            return false;
        } else {
            return exclusive;
        }
    }

    /**
     * Sets the value of the exclusive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setExclusive(Boolean value) {
        this.exclusive = value;
    }

    /**
     * Gets the value of the revisionId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getRevisionId() {
        if (revisionId == null) {
            return  0L;
        } else {
            return revisionId;
        }
    }

    /**
     * Sets the value of the revisionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRevisionId(Long value) {
        this.revisionId = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getVersion() {
        if (version == null) {
            return  1;
        } else {
            return version;
        }
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setVersion(Integer value) {
        this.version = value;
    }

    /**
     * Gets the value of the keepChangeHistory property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isKeepChangeHistory() {
        if (keepChangeHistory == null) {
            return true;
        } else {
            return keepChangeHistory;
        }
    }

    /**
     * Sets the value of the keepChangeHistory property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setKeepChangeHistory(Boolean value) {
        this.keepChangeHistory = value;
    }

    /**
     * Gets the value of the protected property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isProtected() {
        if (_protected == null) {
            return false;
        } else {
            return _protected;
        }
    }

    /**
     * Sets the value of the protected property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setProtected(Boolean value) {
        this._protected = value;
    }

    /**
     * Gets the value of the preserveHistory property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getPreserveHistory() {
        if (preserveHistory == null) {
            return  30L;
        } else {
            return preserveHistory;
        }
    }

    /**
     * Sets the value of the preserveHistory property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setPreserveHistory(Long value) {
        this.preserveHistory = value;
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
