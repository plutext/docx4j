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
 * <p>Java class for CT_PageSetup complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_PageSetup">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="paperSize" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="1" />
 *       &lt;attribute name="paperHeight" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_PositiveUniversalMeasure" />
 *       &lt;attribute name="paperWidth" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_PositiveUniversalMeasure" />
 *       &lt;attribute name="scale" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="100" />
 *       &lt;attribute name="firstPageNumber" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="1" />
 *       &lt;attribute name="fitToWidth" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="1" />
 *       &lt;attribute name="fitToHeight" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" default="1" />
 *       &lt;attribute name="pageOrder" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_PageOrder" default="downThenOver" />
 *       &lt;attribute name="orientation" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_Orientation" default="default" />
 *       &lt;attribute name="usePrinterDefaults" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="blackAndWhite" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="draft" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="cellComments" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_CellComments" default="none" />
 *       &lt;attribute name="useFirstPageNumber" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="errors" type="{http://schemas.openxmlformats.org/spreadsheetml/2006/main}ST_PrintError" default="displayed" />
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
@XmlType(name = "CT_PageSetup")
public class CTPageSetup implements Child
{

    @XmlAttribute(name = "paperSize")
    @XmlSchemaType(name = "unsignedInt")
    protected Long paperSize;
    @XmlAttribute(name = "paperHeight")
    protected String paperHeight;
    @XmlAttribute(name = "paperWidth")
    protected String paperWidth;
    @XmlAttribute(name = "scale")
    @XmlSchemaType(name = "unsignedInt")
    protected Long scale;
    @XmlAttribute(name = "firstPageNumber")
    @XmlSchemaType(name = "unsignedInt")
    protected Long firstPageNumber;
    @XmlAttribute(name = "fitToWidth")
    @XmlSchemaType(name = "unsignedInt")
    protected Long fitToWidth;
    @XmlAttribute(name = "fitToHeight")
    @XmlSchemaType(name = "unsignedInt")
    protected Long fitToHeight;
    @XmlAttribute(name = "pageOrder")
    protected STPageOrder pageOrder;
    @XmlAttribute(name = "orientation")
    protected STOrientation orientation;
    @XmlAttribute(name = "usePrinterDefaults")
    protected Boolean usePrinterDefaults;
    @XmlAttribute(name = "blackAndWhite")
    protected Boolean blackAndWhite;
    @XmlAttribute(name = "draft")
    protected Boolean draft;
    @XmlAttribute(name = "cellComments")
    protected STCellComments cellComments;
    @XmlAttribute(name = "useFirstPageNumber")
    protected Boolean useFirstPageNumber;
    @XmlAttribute(name = "errors")
    protected STPrintError errors;
    @XmlAttribute(name = "horizontalDpi")
    @XmlSchemaType(name = "unsignedInt")
    protected Long horizontalDpi;
    @XmlAttribute(name = "verticalDpi")
    @XmlSchemaType(name = "unsignedInt")
    protected Long verticalDpi;
    @XmlAttribute(name = "copies")
    @XmlSchemaType(name = "unsignedInt")
    protected Long copies;
    @XmlAttribute(name = "id", namespace = "http://schemas.openxmlformats.org/officeDocument/2006/relationships")
    protected String id;
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
     * Gets the value of the paperHeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPaperHeight() {
        return paperHeight;
    }

    /**
     * Sets the value of the paperHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPaperHeight(String value) {
        this.paperHeight = value;
    }

    /**
     * Gets the value of the paperWidth property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPaperWidth() {
        return paperWidth;
    }

    /**
     * Sets the value of the paperWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPaperWidth(String value) {
        this.paperWidth = value;
    }

    /**
     * Gets the value of the scale property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getScale() {
        if (scale == null) {
            return  100L;
        } else {
            return scale;
        }
    }

    /**
     * Sets the value of the scale property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setScale(Long value) {
        this.scale = value;
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
     * Gets the value of the fitToWidth property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getFitToWidth() {
        if (fitToWidth == null) {
            return  1L;
        } else {
            return fitToWidth;
        }
    }

    /**
     * Sets the value of the fitToWidth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setFitToWidth(Long value) {
        this.fitToWidth = value;
    }

    /**
     * Gets the value of the fitToHeight property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getFitToHeight() {
        if (fitToHeight == null) {
            return  1L;
        } else {
            return fitToHeight;
        }
    }

    /**
     * Sets the value of the fitToHeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setFitToHeight(Long value) {
        this.fitToHeight = value;
    }

    /**
     * Gets the value of the pageOrder property.
     * 
     * @return
     *     possible object is
     *     {@link STPageOrder }
     *     
     */
    public STPageOrder getPageOrder() {
        if (pageOrder == null) {
            return STPageOrder.DOWN_THEN_OVER;
        } else {
            return pageOrder;
        }
    }

    /**
     * Sets the value of the pageOrder property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPageOrder }
     *     
     */
    public void setPageOrder(STPageOrder value) {
        this.pageOrder = value;
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
     * Gets the value of the cellComments property.
     * 
     * @return
     *     possible object is
     *     {@link STCellComments }
     *     
     */
    public STCellComments getCellComments() {
        if (cellComments == null) {
            return STCellComments.NONE;
        } else {
            return cellComments;
        }
    }

    /**
     * Sets the value of the cellComments property.
     * 
     * @param value
     *     allowed object is
     *     {@link STCellComments }
     *     
     */
    public void setCellComments(STCellComments value) {
        this.cellComments = value;
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
     * Gets the value of the errors property.
     * 
     * @return
     *     possible object is
     *     {@link STPrintError }
     *     
     */
    public STPrintError getErrors() {
        if (errors == null) {
            return STPrintError.DISPLAYED;
        } else {
            return errors;
        }
    }

    /**
     * Sets the value of the errors property.
     * 
     * @param value
     *     allowed object is
     *     {@link STPrintError }
     *     
     */
    public void setErrors(STPrintError value) {
        this.errors = value;
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
     * Gets the value of the id property.
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
