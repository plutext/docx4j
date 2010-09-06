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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CT_CsPageSetup complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_CsPageSetup">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="paperSize" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="1" />
 *       &lt;attribute name="firstPageNumber" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="1" />
 *       &lt;attribute name="orientation" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Orientation" default="default" />
 *       &lt;attribute name="usePrinterDefaults" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="blackAndWhite" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="draft" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="useFirstPageNumber" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="horizontalDpi" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="600" />
 *       &lt;attribute name="verticalDpi" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="600" />
 *       &lt;attribute name="copies" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="1" />
 *       &lt;attribute ref="{http://schemas.openxmlformats.org/officeDocument/2006/relationships}id"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_CsPageSetup")
public class CTCsPageSetup {

    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long paperSize;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long firstPageNumber;
    @XmlAttribute
    protected STOrientation orientation;
    @XmlAttribute
    protected Boolean usePrinterDefaults;
    @XmlAttribute
    protected Boolean blackAndWhite;
    @XmlAttribute
    protected Boolean draft;
    @XmlAttribute
    protected Boolean useFirstPageNumber;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long horizontalDpi;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long verticalDpi;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedInt")
    protected Long copies;
    @XmlAttribute(namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String id;

    /**
     * Gets the value of the paperSize property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getPaperSize() {
        if (paperSize == null) {
            return  1L;
        } else {
            return paperSize;
        }
    }

    /**
     * Sets the value of the paperSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setPaperSize(Long value) {
        this.paperSize = value;
    }

    /**
     * Gets the value of the firstPageNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getFirstPageNumber() {
        if (firstPageNumber == null) {
            return  1L;
        } else {
            return firstPageNumber;
        }
    }

    /**
     * Sets the value of the firstPageNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setFirstPageNumber(Long value) {
        this.firstPageNumber = value;
    }

    /**
     * Gets the value of the orientation property.
     * 
     * @return
     *     possible object is
     *     {@link STOrientation }
     *     
     */
    public STOrientation getOrientation() {
        if (orientation == null) {
            return STOrientation.DEFAULT;
        } else {
            return orientation;
        }
    }

    /**
     * Sets the value of the orientation property.
     * 
     * @param value
     *     allowed object is
     *     {@link STOrientation }
     *     
     */
    public void setOrientation(STOrientation value) {
        this.orientation = value;
    }

    /**
     * Gets the value of the usePrinterDefaults property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUsePrinterDefaults() {
        if (usePrinterDefaults == null) {
            return true;
        } else {
            return usePrinterDefaults;
        }
    }

    /**
     * Sets the value of the usePrinterDefaults property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUsePrinterDefaults(Boolean value) {
        this.usePrinterDefaults = value;
    }

    /**
     * Gets the value of the blackAndWhite property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isBlackAndWhite() {
        if (blackAndWhite == null) {
            return false;
        } else {
            return blackAndWhite;
        }
    }

    /**
     * Sets the value of the blackAndWhite property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBlackAndWhite(Boolean value) {
        this.blackAndWhite = value;
    }

    /**
     * Gets the value of the draft property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDraft() {
        if (draft == null) {
            return false;
        } else {
            return draft;
        }
    }

    /**
     * Sets the value of the draft property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDraft(Boolean value) {
        this.draft = value;
    }

    /**
     * Gets the value of the useFirstPageNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isUseFirstPageNumber() {
        if (useFirstPageNumber == null) {
            return false;
        } else {
            return useFirstPageNumber;
        }
    }

    /**
     * Sets the value of the useFirstPageNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUseFirstPageNumber(Boolean value) {
        this.useFirstPageNumber = value;
    }

    /**
     * Gets the value of the horizontalDpi property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getHorizontalDpi() {
        if (horizontalDpi == null) {
            return  600L;
        } else {
            return horizontalDpi;
        }
    }

    /**
     * Sets the value of the horizontalDpi property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setHorizontalDpi(Long value) {
        this.horizontalDpi = value;
    }

    /**
     * Gets the value of the verticalDpi property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getVerticalDpi() {
        if (verticalDpi == null) {
            return  600L;
        } else {
            return verticalDpi;
        }
    }

    /**
     * Sets the value of the verticalDpi property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setVerticalDpi(Long value) {
        this.verticalDpi = value;
    }

    /**
     * Gets the value of the copies property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getCopies() {
        if (copies == null) {
            return  1L;
        } else {
            return copies;
        }
    }

    /**
     * Sets the value of the copies property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCopies(Long value) {
        this.copies = value;
    }

    /**
     * Id
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
