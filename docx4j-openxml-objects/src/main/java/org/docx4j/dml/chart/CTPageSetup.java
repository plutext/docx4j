/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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


package org.docx4j.dml.chart;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.ppp.Child;


/**
 * <p>Java class for CT_PageSetup complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PageSetup"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="paperSize" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="1" /&gt;
 *       &lt;attribute name="firstPageNumber" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="1" /&gt;
 *       &lt;attribute name="orientation" type="{http://schemas.openxmlformats.org/drawingml/2006/chart}ST_PageSetupOrientation" default="default" /&gt;
 *       &lt;attribute name="blackAndWhite" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="draft" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="useFirstPageNumber" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="horizontalDpi" type="{http://www.w3.org/2001/XMLSchema}int" default="600" /&gt;
 *       &lt;attribute name="verticalDpi" type="{http://www.w3.org/2001/XMLSchema}int" default="600" /&gt;
 *       &lt;attribute name="copies" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="1" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_PageSetup")
public class CTPageSetup implements Child
{

    @XmlAttribute(name = "paperSize")
    @XmlSchemaType(name = "unsignedInt")
    protected Long paperSize;
    @XmlAttribute(name = "firstPageNumber")
    @XmlSchemaType(name = "unsignedInt")
    protected Long firstPageNumber;
    @XmlAttribute(name = "orientation")
    protected STPageSetupOrientation orientation;
    @XmlAttribute(name = "blackAndWhite")
    protected Boolean blackAndWhite;
    @XmlAttribute(name = "draft")
    protected Boolean draft;
    @XmlAttribute(name = "useFirstPageNumber")
    protected Boolean useFirstPageNumber;
    @XmlAttribute(name = "horizontalDpi")
    protected Integer horizontalDpi;
    @XmlAttribute(name = "verticalDpi")
    protected Integer verticalDpi;
    @XmlAttribute(name = "copies")
    @XmlSchemaType(name = "unsignedInt")
    protected Long copies;
    @XmlTransient
    private Object parent;

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
     *     {@link STPageSetupOrientation }
     *     
     */
    public STPageSetupOrientation getOrientation() {
        if (orientation == null) {
            return STPageSetupOrientation.DEFAULT;
        } else {
            return orientation;
        }
    }

    /**
     * Sets the value of the orientation property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPageSetupOrientation }
     *     
     */
    public void setOrientation(STPageSetupOrientation value) {
        this.orientation = value;
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
     *     {@link Integer }
     *     
     */
    public int getHorizontalDpi() {
        if (horizontalDpi == null) {
            return  600;
        } else {
            return horizontalDpi;
        }
    }

    /**
     * Sets the value of the horizontalDpi property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setHorizontalDpi(Integer value) {
        this.horizontalDpi = value;
    }

    /**
     * Gets the value of the verticalDpi property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getVerticalDpi() {
        if (verticalDpi == null) {
            return  600;
        } else {
            return verticalDpi;
        }
    }

    /**
     * Sets the value of the verticalDpi property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setVerticalDpi(Integer value) {
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
