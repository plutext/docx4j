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
 * <p>Java class for CT_MetadataType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CT_MetadataType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" use="required" type="{http://schemas.openxmlformats.org/officeDocument/2006/sharedTypes}ST_Xstring" />
 *       &lt;attribute name="minSupportedVersion" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" />
 *       &lt;attribute name="ghostRow" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="ghostCol" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="edit" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="delete" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="copy" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="pasteAll" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="pasteFormulas" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="pasteValues" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="pasteFormats" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="pasteComments" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="pasteDataValidation" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="pasteBorders" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="pasteColWidths" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="pasteNumberFormats" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="merge" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="splitFirst" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="splitAll" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="rowColShift" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="clearAll" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="clearFormats" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="clearContents" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="clearComments" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="assign" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="coerce" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="adjust" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="cellMeta" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CT_MetadataType")
public class CTMetadataType implements Child
{

    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "minSupportedVersion", required = true)
    @XmlSchemaType(name = "unsignedInt")
    protected long minSupportedVersion;
    @XmlAttribute(name = "ghostRow")
    protected Boolean ghostRow;
    @XmlAttribute(name = "ghostCol")
    protected Boolean ghostCol;
    @XmlAttribute(name = "edit")
    protected Boolean edit;
    @XmlAttribute(name = "delete")
    protected Boolean delete;
    @XmlAttribute(name = "copy")
    protected Boolean copy;
    @XmlAttribute(name = "pasteAll")
    protected Boolean pasteAll;
    @XmlAttribute(name = "pasteFormulas")
    protected Boolean pasteFormulas;
    @XmlAttribute(name = "pasteValues")
    protected Boolean pasteValues;
    @XmlAttribute(name = "pasteFormats")
    protected Boolean pasteFormats;
    @XmlAttribute(name = "pasteComments")
    protected Boolean pasteComments;
    @XmlAttribute(name = "pasteDataValidation")
    protected Boolean pasteDataValidation;
    @XmlAttribute(name = "pasteBorders")
    protected Boolean pasteBorders;
    @XmlAttribute(name = "pasteColWidths")
    protected Boolean pasteColWidths;
    @XmlAttribute(name = "pasteNumberFormats")
    protected Boolean pasteNumberFormats;
    @XmlAttribute(name = "merge")
    protected Boolean merge;
    @XmlAttribute(name = "splitFirst")
    protected Boolean splitFirst;
    @XmlAttribute(name = "splitAll")
    protected Boolean splitAll;
    @XmlAttribute(name = "rowColShift")
    protected Boolean rowColShift;
    @XmlAttribute(name = "clearAll")
    protected Boolean clearAll;
    @XmlAttribute(name = "clearFormats")
    protected Boolean clearFormats;
    @XmlAttribute(name = "clearContents")
    protected Boolean clearContents;
    @XmlAttribute(name = "clearComments")
    protected Boolean clearComments;
    @XmlAttribute(name = "assign")
    protected Boolean assign;
    @XmlAttribute(name = "coerce")
    protected Boolean coerce;
    @XmlAttribute(name = "adjust")
    protected Boolean adjust;
    @XmlAttribute(name = "cellMeta")
    protected Boolean cellMeta;
    @XmlTransient
    private Object parent;

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
     * Gets the value of the minSupportedVersion property.
     * 
     */
    public long getMinSupportedVersion() {
        return minSupportedVersion;
    }

    /**
     * Sets the value of the minSupportedVersion property.
     * 
     */
    public void setMinSupportedVersion(long value) {
        this.minSupportedVersion = value;
    }

    /**
     * Gets the value of the ghostRow property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isGhostRow() {
        if (ghostRow == null) {
            return false;
        } else {
            return ghostRow;
        }
    }

    /**
     * Sets the value of the ghostRow property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setGhostRow(Boolean value) {
        this.ghostRow = value;
    }

    /**
     * Gets the value of the ghostCol property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isGhostCol() {
        if (ghostCol == null) {
            return false;
        } else {
            return ghostCol;
        }
    }

    /**
     * Sets the value of the ghostCol property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setGhostCol(Boolean value) {
        this.ghostCol = value;
    }

    /**
     * Gets the value of the edit property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEdit() {
        if (edit == null) {
            return false;
        } else {
            return edit;
        }
    }

    /**
     * Sets the value of the edit property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEdit(Boolean value) {
        this.edit = value;
    }

    /**
     * Gets the value of the delete property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDelete() {
        if (delete == null) {
            return false;
        } else {
            return delete;
        }
    }

    /**
     * Sets the value of the delete property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDelete(Boolean value) {
        this.delete = value;
    }

    /**
     * Gets the value of the copy property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCopy() {
        if (copy == null) {
            return false;
        } else {
            return copy;
        }
    }

    /**
     * Sets the value of the copy property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCopy(Boolean value) {
        this.copy = value;
    }

    /**
     * Gets the value of the pasteAll property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPasteAll() {
        if (pasteAll == null) {
            return false;
        } else {
            return pasteAll;
        }
    }

    /**
     * Sets the value of the pasteAll property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPasteAll(Boolean value) {
        this.pasteAll = value;
    }

    /**
     * Gets the value of the pasteFormulas property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPasteFormulas() {
        if (pasteFormulas == null) {
            return false;
        } else {
            return pasteFormulas;
        }
    }

    /**
     * Sets the value of the pasteFormulas property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPasteFormulas(Boolean value) {
        this.pasteFormulas = value;
    }

    /**
     * Gets the value of the pasteValues property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPasteValues() {
        if (pasteValues == null) {
            return false;
        } else {
            return pasteValues;
        }
    }

    /**
     * Sets the value of the pasteValues property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPasteValues(Boolean value) {
        this.pasteValues = value;
    }

    /**
     * Gets the value of the pasteFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPasteFormats() {
        if (pasteFormats == null) {
            return false;
        } else {
            return pasteFormats;
        }
    }

    /**
     * Sets the value of the pasteFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPasteFormats(Boolean value) {
        this.pasteFormats = value;
    }

    /**
     * Gets the value of the pasteComments property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPasteComments() {
        if (pasteComments == null) {
            return false;
        } else {
            return pasteComments;
        }
    }

    /**
     * Sets the value of the pasteComments property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPasteComments(Boolean value) {
        this.pasteComments = value;
    }

    /**
     * Gets the value of the pasteDataValidation property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPasteDataValidation() {
        if (pasteDataValidation == null) {
            return false;
        } else {
            return pasteDataValidation;
        }
    }

    /**
     * Sets the value of the pasteDataValidation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPasteDataValidation(Boolean value) {
        this.pasteDataValidation = value;
    }

    /**
     * Gets the value of the pasteBorders property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPasteBorders() {
        if (pasteBorders == null) {
            return false;
        } else {
            return pasteBorders;
        }
    }

    /**
     * Sets the value of the pasteBorders property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPasteBorders(Boolean value) {
        this.pasteBorders = value;
    }

    /**
     * Gets the value of the pasteColWidths property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPasteColWidths() {
        if (pasteColWidths == null) {
            return false;
        } else {
            return pasteColWidths;
        }
    }

    /**
     * Sets the value of the pasteColWidths property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPasteColWidths(Boolean value) {
        this.pasteColWidths = value;
    }

    /**
     * Gets the value of the pasteNumberFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPasteNumberFormats() {
        if (pasteNumberFormats == null) {
            return false;
        } else {
            return pasteNumberFormats;
        }
    }

    /**
     * Sets the value of the pasteNumberFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPasteNumberFormats(Boolean value) {
        this.pasteNumberFormats = value;
    }

    /**
     * Gets the value of the merge property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isMerge() {
        if (merge == null) {
            return false;
        } else {
            return merge;
        }
    }

    /**
     * Sets the value of the merge property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMerge(Boolean value) {
        this.merge = value;
    }

    /**
     * Gets the value of the splitFirst property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSplitFirst() {
        if (splitFirst == null) {
            return false;
        } else {
            return splitFirst;
        }
    }

    /**
     * Sets the value of the splitFirst property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSplitFirst(Boolean value) {
        this.splitFirst = value;
    }

    /**
     * Gets the value of the splitAll property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isSplitAll() {
        if (splitAll == null) {
            return false;
        } else {
            return splitAll;
        }
    }

    /**
     * Sets the value of the splitAll property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSplitAll(Boolean value) {
        this.splitAll = value;
    }

    /**
     * Gets the value of the rowColShift property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRowColShift() {
        if (rowColShift == null) {
            return false;
        } else {
            return rowColShift;
        }
    }

    /**
     * Sets the value of the rowColShift property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRowColShift(Boolean value) {
        this.rowColShift = value;
    }

    /**
     * Gets the value of the clearAll property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isClearAll() {
        if (clearAll == null) {
            return false;
        } else {
            return clearAll;
        }
    }

    /**
     * Sets the value of the clearAll property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setClearAll(Boolean value) {
        this.clearAll = value;
    }

    /**
     * Gets the value of the clearFormats property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isClearFormats() {
        if (clearFormats == null) {
            return false;
        } else {
            return clearFormats;
        }
    }

    /**
     * Sets the value of the clearFormats property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setClearFormats(Boolean value) {
        this.clearFormats = value;
    }

    /**
     * Gets the value of the clearContents property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isClearContents() {
        if (clearContents == null) {
            return false;
        } else {
            return clearContents;
        }
    }

    /**
     * Sets the value of the clearContents property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setClearContents(Boolean value) {
        this.clearContents = value;
    }

    /**
     * Gets the value of the clearComments property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isClearComments() {
        if (clearComments == null) {
            return false;
        } else {
            return clearComments;
        }
    }

    /**
     * Sets the value of the clearComments property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setClearComments(Boolean value) {
        this.clearComments = value;
    }

    /**
     * Gets the value of the assign property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAssign() {
        if (assign == null) {
            return false;
        } else {
            return assign;
        }
    }

    /**
     * Sets the value of the assign property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAssign(Boolean value) {
        this.assign = value;
    }

    /**
     * Gets the value of the coerce property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCoerce() {
        if (coerce == null) {
            return false;
        } else {
            return coerce;
        }
    }

    /**
     * Sets the value of the coerce property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCoerce(Boolean value) {
        this.coerce = value;
    }

    /**
     * Gets the value of the adjust property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAdjust() {
        if (adjust == null) {
            return false;
        } else {
            return adjust;
        }
    }

    /**
     * Sets the value of the adjust property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAdjust(Boolean value) {
        this.adjust = value;
    }

    /**
     * Gets the value of the cellMeta property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCellMeta() {
        if (cellMeta == null) {
            return false;
        } else {
            return cellMeta;
        }
    }

    /**
     * Sets the value of the cellMeta property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCellMeta(Boolean value) {
        this.cellMeta = value;
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
